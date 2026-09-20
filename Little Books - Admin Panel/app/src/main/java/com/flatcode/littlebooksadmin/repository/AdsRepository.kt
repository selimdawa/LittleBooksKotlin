package com.flatcode.littlebooksadmin.repository

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.flatcode.littlebooksadmin.model.ADs
import com.flatcode.littlebooksadmin.model.User
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
class AdsRepository @Inject constructor(
    private val db: FirebaseDatabase
) {

    fun getAdsUsers(orderBy: String = DATA.AD_LOAD): Flow<Resource<List<User>>> = callbackFlow {
        trySend(Resource.Loading())
        val ref = db.getReference(DATA.USERS).orderByChild(orderBy)
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<User>()
                for (data in snapshot.children) {
                    val user = data.getValue(User::class.java)
                    user?.let {
                        if (it.adLoad != 0 || it.adClick != 0) {
                            users.add(it)
                        }
                    }
                }
                trySend(Resource.Success(users.reversed()))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.message))
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getUserAds(userId: String, orderBy: String = DATA.NAME): Flow<Resource<List<ADs>>> =
        callbackFlow {
            trySend(Resource.Loading())
            val ref = db.getReference(DATA.AD_S).child(userId).orderByChild(orderBy)
            val listener = ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val adsList = mutableListOf<ADs>()
                    for (data in snapshot.children) {
                        val ads = data.getValue(ADs::class.java)
                        ads?.let { adsList.add(it) }
                    }
                    trySend(Resource.Success(adsList))
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(Resource.Error(error.message))
                }
            })
            awaitClose { ref.removeEventListener(listener) }
        }

    fun getSliderShow(): Flow<Resource<Map<String, String>>> = callbackFlow {
        trySend(Resource.Loading())
        val ref = db.getReference(DATA.SLIDER_SHOW)
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sliders = mutableMapOf<String, String>()
                for (data in snapshot.children) {
                    val key = data.key ?: continue
                    val value = data.value?.toString() ?: continue
                    sliders[key] = value
                }
                trySend(Resource.Success(sliders))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.message))
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun updateSliderImage(number: Int, imageUri: Uri): Resource<Unit> =
        suspendCancellableCoroutine { continuation ->
            MediaManager.get().upload(imageUri).option("folder", "Images/SliderShow/")
                .option("public_id", number.toString()).callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                        val downloadUrl = resultData?.get("secure_url") as? String
                        if (downloadUrl != null) {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    db.getReference(DATA.SLIDER_SHOW).child(number.toString())
                                        .setValue(downloadUrl).await()
                                    continuation.resume(Resource.Success(Unit))
                                } catch (e: Exception) {
                                    continuation.resume(
                                        Resource.Error(
                                            e.message ?: "Database update failed"
                                        )
                                    )
                                }
                            }
                        } else {
                            continuation.resume(Resource.Error("Cloudinary upload failed: secure_url is null"))
                        }
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        continuation.resume(
                            Resource.Error(
                                error?.description ?: "Cloudinary upload failed"
                            )
                        )
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                }).dispatch()
        }
}