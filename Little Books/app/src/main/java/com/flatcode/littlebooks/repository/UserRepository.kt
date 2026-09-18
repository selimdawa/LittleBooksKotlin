package com.flatcode.littlebooks.repository

import android.content.Context
import android.net.Uri
import com.flatcode.littlebooks.model.User
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.getFileExtension
import com.flatcode.littlebooks.db.UserDao
import com.flatcode.littlebooks.utils.Resource
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val db: FirebaseDatabase,
    private val userDao: UserDao
) {
    suspend fun getUserInfo(userId: String): Resource<User> {
        return try {
            val localUser = userDao.getUserById(userId).first()
            if (localUser != null) return Resource.Success(localUser)

            val snapshot = db.getReference(DATA.USERS).child(userId).get().await()
            val user = snapshot.getValue(User::class.java)
            if (user != null) {
                userDao.insertUser(user)
                Resource.Success(user)
            } else Resource.Error("User not found")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun updateUserInfo(userId: String, hashMap: Map<String, Any>): Resource<Unit> {
        return try {
            db.getReference(DATA.USERS).child(userId).updateChildren(hashMap).await()
            val snapshot = db.getReference(DATA.USERS).child(userId).get().await()
            snapshot.getValue(User::class.java)?.let { userDao.insertUser(it) }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun getFollowCount(userId: String, type: String): Resource<Long> {
        return try {
            val snapshot = db.getReference(DATA.FOLLOW).child(userId).child(type).get().await()
            Resource.Success(snapshot.childrenCount)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun getFavoriteCount(userId: String): Resource<Long> {
        return try {
            val snapshot = db.getReference(DATA.FAVORITES).child(userId).get().await()
            Resource.Success(snapshot.childrenCount)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun checkFollowing(currentUserId: String, targetUserId: String): Resource<Boolean> {
        return try {
            val snapshot = db.getReference(DATA.FOLLOW)
                .child(currentUserId).child(DATA.FOLLOWING).child(targetUserId).get().await()
            Resource.Success(snapshot.exists())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun followUser(currentUserId: String, targetUserId: String, follow: Boolean): Resource<Unit> {
        return try {
            val followingRef = db.getReference(DATA.FOLLOW).child(currentUserId).child(DATA.FOLLOWING).child(targetUserId)
            val followersRef = db.getReference(DATA.FOLLOW).child(targetUserId).child(DATA.FOLLOWERS).child(currentUserId)
            
            if (follow) {
                followingRef.setValue(true).await()
                followersRef.setValue(true).await()
            } else {
                followingRef.removeValue().await()
                followersRef.removeValue().await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun getFollowersOrFollowing(userId: String, type: String): Resource<List<User>> {
        return try {
            val followSnapshot = db.getReference(DATA.FOLLOW).child(userId).child(type).get().await()
            val userList = mutableListOf<User>()
            for (data in followSnapshot.children) {
                val followUserId = data.key ?: continue
                val userSnapshot = db.getReference(DATA.USERS).child(followUserId).get().await()
                userSnapshot.getValue(User::class.java)?.let { 
                    userList.add(it)
                    userDao.insertUser(it)
                }
            }
            Resource.Success(userList)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun getExplorePublishersCount(currentUserId: String): Resource<Int> {
        return try {
            val snapshot = db.getReference(DATA.USERS).get().await()
            var count = 0
            for (data in snapshot.children) {
                val user = data.getValue(User::class.java)
                if (user != null && user.id != currentUserId && user.booksCount >= 1) {
                    count++
                }
            }
            Resource.Success(count)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun getExplorePublishers(currentUserId: String): Resource<List<User>> {
        return try {
            val snapshot = db.getReference(DATA.USERS).get().await()
            val list = mutableListOf<User>()
            for (data in snapshot.children) {
                val user = data.getValue(User::class.java)
                if (user != null && user.id != currentUserId && user.booksCount >= 1) {
                    list.add(user)
                    userDao.insertUser(user)
                }
            }
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun uploadProfileImage(userId: String, imageUri: Uri, context: Context): Resource<String> {
        return try {
            val filePathAndName = "Images/Profile/$userId"
            val extension = imageUri.getFileExtension(context)
            val reference = FirebaseStorage.getInstance().getReference("$filePathAndName.${extension}")
            val task = reference.putFile(imageUri).await()
            val downloadUrl = task.storage.downloadUrl.await()
            Resource.Success(downloadUrl.toString())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }
}


