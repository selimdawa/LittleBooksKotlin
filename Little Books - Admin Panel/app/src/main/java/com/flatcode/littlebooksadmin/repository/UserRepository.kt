package com.flatcode.littlebooksadmin.repository

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.model.User
import com.flatcode.littlebooksadmin.utils.DATA
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class UserRepository @Inject constructor(
    private val db: FirebaseDatabase
) {

    suspend fun getUserById(userId: String): User? {
        return try {
            val snapshot = db.getReference(DATA.USERS).child(userId).get().await()
            snapshot.getValue(User::class.java)
        } catch (_: Exception) {
            null
        }
    }

    fun getUsers(orderBy: String = DATA.TIMESTAMP): Flow<List<User>> = callbackFlow {
        val ref = db.getReference(DATA.USERS).orderByChild(orderBy)
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<User>()
                for (data in snapshot.children) {
                    try {
                        val user = data.getValue(User::class.java)
                        user?.let {
                            if (it.id.isEmpty()) {
                                it.id = data.key ?: ""
                            }
                            users.add(it)
                        }
                    } catch (_: Exception) {
                        // Skip corrupted user nodes
                    }
                }
                trySend(users.reversed())
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getProfileStats(userId: String): Flow<ProfileStats?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val booksSnapshot = snapshot.child(DATA.BOOKS)
                    var userBooksCount = 0
                    for (child in booksSnapshot.children) {
                        val book = child.getValue(Book::class.java)
                        if (book?.publisher == userId) userBooksCount++
                    }

                    val followersCount = snapshot.child(DATA.FOLLOW).child(userId)
                        .child(DATA.FOLLOWERS).childrenCount.toInt()
                    val followingCount = snapshot.child(DATA.FOLLOW).child(userId)
                        .child(DATA.FOLLOWING).childrenCount.toInt()
                    val favoritesCount =
                        snapshot.child(DATA.FAVORITES).child(userId).childrenCount.toInt()

                    trySend(
                        ProfileStats(
                            userBooksCount, followersCount, followingCount, favoritesCount
                        )
                    )
                } catch (_: Exception) {
                    trySend(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        db.reference.addValueEventListener(listener)
        awaitClose { db.reference.removeEventListener(listener) }
    }

    fun isFollowing(userId: String): Flow<Boolean> = callbackFlow {
        val ref = db.getReference(DATA.FOLLOW).child(DATA.FirebaseUserUid).child(DATA.FOLLOWING)
            .child(userId)
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun toggleFollow(userId: String, isFollowing: Boolean) {
        val myId = DATA.FirebaseUserUid
        val followingRef =
            db.getReference(DATA.FOLLOW).child(myId).child(DATA.FOLLOWING).child(userId)
        val followersRef =
            db.getReference(DATA.FOLLOW).child(userId).child(DATA.FOLLOWERS).child(myId)

        if (isFollowing) {
            followingRef.removeValue().await()
            followersRef.removeValue().await()
        } else {
            followingRef.setValue(true).await()
            followersRef.setValue(true).await()
        }
    }

    suspend fun updateUserProfile(username: String, imageUri: Uri?): Unit =
        suspendCancellableCoroutine { continuation ->
            val myId = DATA.FirebaseUserUid
            val updates = mutableMapOf<String, Any>(DATA.USER_NAME to username)

            if (imageUri != null) {
                MediaManager.get().upload(imageUri)
                    .unsigned(DATA.CLOUDINARY_UPLOAD_PRESET)
                    .option("folder", "Images/Profile/")
                    .option("public_id", myId).callback(object : UploadCallback {
                        override fun onStart(requestId: String?) {}
                        override fun onProgress(
                            requestId: String?, bytes: Long, totalBytes: Long
                        ) {
                        }

                        override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                            val downloadUrl = resultData?.get("secure_url") as? String
                            if (downloadUrl != null) {
                                updates[DATA.PROFILE_IMAGE] = downloadUrl
                            }
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    db.getReference(DATA.USERS).child(myId).updateChildren(updates)
                                        .await()
                                    continuation.resume(Unit)
                                } catch (e: Exception) {
                                    continuation.resumeWithException(e)
                                }
                            }
                        }

                        override fun onError(requestId: String?, error: ErrorInfo?) {
                            continuation.resumeWithException(
                                Exception(error?.description ?: "Upload failed")
                            )
                        }

                        override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                    }).dispatch()
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        db.getReference(DATA.USERS).child(myId).updateChildren(updates).await()
                        continuation.resume(Unit)
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }
            }
        }

    fun getFollow(userId: String, type: String): Flow<List<User>> = callbackFlow {
        val followRef = db.getReference(DATA.FOLLOW).child(userId).child(type)
        val listener = followRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val followIds = snapshot.children.mapNotNull { it.key }
                if (followIds.isEmpty()) {
                    trySend(emptyList())
                    return
                }

                val usersRef = db.getReference(DATA.USERS)
                usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(usersSnapshot: DataSnapshot) {
                        val users = mutableListOf<User>()
                        for (data in usersSnapshot.children) {
                            try {
                                val user = data.getValue(User::class.java)
                                if (user != null) {
                                    if (user.id.isEmpty()) {
                                        user.id = data.key ?: ""
                                    }
                                    if (followIds.contains(user.id)) {
                                        users.add(user)
                                    }
                                }
                            } catch (_: Exception) {
                            }
                        }
                        trySend(users.reversed())
                    }

                    override fun onCancelled(error: DatabaseError) {
                        close(error.toException())
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
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
