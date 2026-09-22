package com.flatcode.littlebooksadmin.repository

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.flatcode.littlebooksadmin.db.CategoryDao
import com.flatcode.littlebooksadmin.model.Category
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.Resource
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

@Singleton
class CategoryRepository @Inject constructor(
    private val db: FirebaseDatabase, private val categoryDao: CategoryDao
) {

    fun getCategories(orderBy: String = DATA.CATEGORY): Flow<Resource<List<Category>>> =
        callbackFlow {
            trySend(Resource.Loading())
            val ref = db.getReference(DATA.CATEGORIES).orderByChild(orderBy)
            val listener = ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categories = mutableListOf<Category>()
                    for (data in snapshot.children) {
                        val category = data.getValue(Category::class.java)
                        category?.let { categories.add(it) }
                    }
                    this@callbackFlow.launch {
                        categoryDao.insertCategories(categories)
                    }
                    trySend(Resource.Success(categories.reversed()))
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(Resource.Error(error.message))
                }
            })
            awaitClose { ref.removeEventListener(listener) }
        }

    suspend fun addCategory(name: String, imageUri: Uri?): Resource<Unit> =
        suspendCancellableCoroutine { continuation ->
            val ref = db.getReference(DATA.CATEGORIES)
            val id = ref.push().key
            if (id == null) {
                continuation.resume(Resource.Error("Could not generate ID"))
                return@suspendCancellableCoroutine
            }

            if (imageUri == null) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val category = Category(
                            id = id,
                            category = name,
                            image = DATA.BASIC,
                            publisher = DATA.FirebaseUserUid,
                            timestamp = System.currentTimeMillis()
                        )
                        ref.child(id).setValue(category).await()
                        categoryDao.insertCategory(category)
                        continuation.resume(Resource.Success(Unit))
                    } catch (e: Exception) {
                        continuation.resume(Resource.Error(e.message ?: "Database error"))
                    }
                }
            } else {
                MediaManager.get().upload(imageUri).option("folder", "CategoryImages/")
                    .option("public_id", id).callback(object : UploadCallback {
                        override fun onStart(requestId: String?) {}
                        override fun onProgress(
                            requestId: String?, bytes: Long, totalBytes: Long
                        ) {
                        }

                        override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                            val downloadUrl = resultData?.get("secure_url") as? String
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val category = Category(
                                        id = id,
                                        category = name,
                                        image = downloadUrl ?: DATA.BASIC,
                                        publisher = DATA.FirebaseUserUid,
                                        timestamp = System.currentTimeMillis()
                                    )
                                    ref.child(id).setValue(category).await()
                                    categoryDao.insertCategory(category)
                                    continuation.resume(Resource.Success(Unit))
                                } catch (e: Exception) {
                                    continuation.resume(
                                        Resource.Error(
                                            e.message ?: "Database error"
                                        )
                                    )
                                }
                            }
                        }

                        override fun onError(requestId: String?, error: ErrorInfo?) {
                            continuation.resume(
                                Resource.Error(
                                    error?.description ?: "Upload failed"
                                )
                            )
                        }

                        override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                    }).dispatch()
            }
        }
}


