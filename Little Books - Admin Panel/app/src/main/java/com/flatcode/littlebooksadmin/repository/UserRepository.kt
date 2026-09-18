package com.flatcode.littlebooksadmin.repository

import android.net.Uri
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.model.User
import com.flatcode.littlebooksadmin.utils.Resource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val db: FirebaseDatabase
) {

    suspend fun getUserById(userId: String): Resource<User> {
        return try {
            val snapshot = db.getReference(DATA.USERS).child(userId).get().await()
            val user = snapshot.getValue(User::class.java)
            if (user != null) Resource.Success(user) else Resource.Error("User not found")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    fun getUsers(orderBy: String = DATA.TIMESTAMP): Flow<Resource<List<User>>> = callbackFlow {
        trySend(Resource.Loading())
        val ref = db.getReference(DATA.USERS).orderByChild(orderBy)
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<User>()
                for (data in snapshot.children) {
                    val user = data.getValue(User::class.java)
                    user?.let { users.add(it) }
                }
                trySend(Resource.Success(users.reversed()))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.message))
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getProfileStats(userId: String): Flow<Resource<ProfileStats>> = callbackFlow {
        trySend(Resource.Loading())
        val ref = db.reference
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val booksSnapshot = snapshot.child(DATA.BOOKS)
                    var userBooksCount = 0
                    for (child in booksSnapshot.children) {
                        val book = child.getValue(Book::class.java)
                        if (book?.publisher == userId) userBooksCount++
                    }

                    val followersCount = snapshot.child(DATA.FOLLOW).child(userId).child(DATA.FOLLOWERS).childrenCount.toInt()
                    val followingCount = snapshot.child(DATA.FOLLOW).child(userId).child(DATA.FOLLOWING).childrenCount.toInt()
                    val favoritesCount = snapshot.child(DATA.FAVORITES).child(userId).childrenCount.toInt()

                    trySend(Resource.Success(ProfileStats(userBooksCount, followersCount, followingCount, favoritesCount)))
                } catch (e: Exception) {
                    trySend(Resource.Error(e.message ?: "Stats error"))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.message))
            }
        }
        
        db.reference.addValueEventListener(listener)
        awaitClose { db.reference.removeEventListener(listener) }
    }

    fun isFollowing(userId: String): Flow<Boolean> = callbackFlow {
        val ref = db.getReference(DATA.FOLLOW).child(DATA.FirebaseUserUid).child(DATA.FOLLOWING).child(userId)
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.exists())
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun toggleFollow(userId: String, isFollowing: Boolean): Resource<Unit> {
        return try {
            val myId = DATA.FirebaseUserUid
            val followingRef = db.getReference(DATA.FOLLOW).child(myId).child(DATA.FOLLOWING).child(userId)
            val followersRef = db.getReference(DATA.FOLLOW).child(userId).child(DATA.FOLLOWERS).child(myId)
            
            if (isFollowing) {
                followingRef.removeValue().await()
                followersRef.removeValue().await()
            } else {
                followingRef.setValue(true).await()
                followersRef.setValue(true).await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Follow error")
        }
    }

    suspend fun updateUserProfile(username: String, imageUri: Uri?, extension: String?): Resource<Unit> {
        return try {
            val myId = DATA.FirebaseUserUid
            val updates = mutableMapOf<String, Any>(DATA.USER_NAME to username)
            
            if (imageUri != null && extension != null) {
                val filePath = "Images/Profile/$myId.$extension"
                val storageRef = FirebaseStorage.getInstance().getReference(filePath)
                val uploadTask = storageRef.putFile(imageUri).await()
                val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
                updates[DATA.PROFILE_IMAGE] = downloadUrl
            }
            
            db.getReference(DATA.USERS).child(myId).updateChildren(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Update failed")
        }
    }

    fun getFollow(userId: String, type: String): Flow<Resource<List<User>>> = callbackFlow {
        trySend(Resource.Loading())
        val followRef = db.getReference(DATA.FOLLOW).child(userId).child(type)
        val listener = followRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val followIds = snapshot.children.mapNotNull { it.key }
                if (followIds.isEmpty()) {
                    trySend(Resource.Success(emptyList()))
                    return
                }

                val usersRef = db.getReference(DATA.USERS)
                usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(usersSnapshot: DataSnapshot) {
                        val users = mutableListOf<User>()
                        for (data in usersSnapshot.children) {
                            val user = data.getValue(User::class.java)
                            if (user != null && followIds.contains(user.id)) {
                                users.add(user)
                            }
                        }
                        trySend(Resource.Success(users.reversed()))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        trySend(Resource.Error(error.message))
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.message))
            }
        })
        awaitClose { followRef.removeEventListener(listener) }
    }

    data class ProfileStats(
        val booksCount: Int,
        val followersCount: Int,
        val followingCount: Int,
        val favoritesCount: Int
    )
}


