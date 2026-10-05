package com.flatcode.littlebooks.repository

import android.net.Uri
import com.flatcode.littlebooks.db.FavoriteDao
import com.flatcode.littlebooks.db.InterestedDao
import com.flatcode.littlebooks.db.UserDao
import com.flatcode.littlebooks.model.FavoriteEntity
import com.flatcode.littlebooks.model.InterestedEntity
import com.flatcode.littlebooks.model.User
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.cloudinaryUpload
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val db: FirebaseDatabase,
    private val userDao: UserDao,
    private val favoriteDao: FavoriteDao,
    private val interestedDao: InterestedDao
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    fun getUserInfo(userId: String): Flow<User?> {
        syncUser(userId)
        return userDao.getUserById(userId)
    }

    private fun syncUser(userId: String) {
        if (userId.isEmpty()) return
        db.getReference(DATA.USERS).child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(User::class.java)?.let { user ->
                        repositoryScope.launch {
                            userDao.insertUser(user)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), "syncUser failed")
                }
            })
    }

    suspend fun updateUserInfo(userId: String, hashMap: Map<String, Any>): Result<Unit> {
        return try {
            db.getReference(DATA.USERS).child(userId).updateChildren(hashMap).await()
            val snapshot = db.getReference(DATA.USERS).child(userId).get().await()
            snapshot.getValue(User::class.java)?.let { userDao.insertUser(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getFollowCount(userId: String, type: String): Flow<Int> {
        syncFollow(userId, type)
        return interestedDao.getInterestedCount(userId, type)
    }

    private fun syncFollow(userId: String, type: String) {
        if (userId.isEmpty()) return
        db.getReference(DATA.FOLLOW).child(userId).child(type)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { it.key }
                        .map { InterestedEntity(userId, type, it) }
                    repositoryScope.launch {
                        interestedDao.deleteAllInterestedForUser(userId, type)
                        interestedDao.insertInterestedList(list)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), "syncFollow failed for $type")
                }
            })
    }

    fun getFavoritesCount(userId: String): Flow<Int> {
        syncFavorites(userId)
        return favoriteDao.getTotalFavoriteCount(userId)
    }

    private fun syncFavorites(userId: String) {
        if (userId.isEmpty()) return
        db.getReference(DATA.FAVORITES).child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val favList =
                        snapshot.children.mapNotNull { it.key }.map { FavoriteEntity(userId, it) }
                    repositoryScope.launch {
                        favoriteDao.deleteAllFavoritesForUser(userId)
                        favoriteDao.insertFavorites(favList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), "syncFavorites failed")
                }
            })
    }

    suspend fun checkFollowing(currentUserId: String, targetUserId: String): Boolean {
        return try {
            val snapshot = db.getReference(DATA.FOLLOW).child(currentUserId).child(DATA.FOLLOWING)
                .child(targetUserId).get().await()
            snapshot.exists()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun followUser(
        currentUserId: String, targetUserId: String, follow: Boolean
    ): Result<Unit> {
        return try {
            val followingRef =
                db.getReference(DATA.FOLLOW).child(currentUserId).child(DATA.FOLLOWING)
                    .child(targetUserId)
            val followersRef =
                db.getReference(DATA.FOLLOW).child(targetUserId).child(DATA.FOLLOWERS)
                    .child(currentUserId)

            if (follow) {
                followingRef.setValue(true).await()
                followersRef.setValue(true).await()
            } else {
                followingRef.removeValue().await()
                followersRef.removeValue().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFollowersOrFollowing(userId: String, type: String): List<User> {
        return try {
            val followSnapshot =
                db.getReference(DATA.FOLLOW).child(userId).child(type).get().await()
            val userList = mutableListOf<User>()
            for (data in followSnapshot.children) {
                val followUserId = data.key ?: continue
                val userSnapshot = db.getReference(DATA.USERS).child(followUserId).get().await()
                userSnapshot.getValue(User::class.java)?.let {
                    userList.add(it)
                    userDao.insertUser(it)
                }
            }
            userList
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getExplorePublishersCount(currentUserId: String): Flow<Int> {
        syncUsers()
        return userDao.getExplorePublishersCount(currentUserId)
    }

    private fun syncUsers() {
        db.getReference(DATA.USERS).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<User>()
                for (data in snapshot.children) {
                    val user = data.getValue(User::class.java) ?: continue
                    list.add(user)
                }
                repositoryScope.launch {
                    userDao.insertUsers(list)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException(), "syncUsers failed")
            }
        })
    }

    suspend fun getExplorePublishers(currentUserId: String): List<User> {
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
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun uploadProfileImage(imageUri: Uri): Result<String> {
        return try {
            val url = cloudinaryUpload(imageUri)
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}