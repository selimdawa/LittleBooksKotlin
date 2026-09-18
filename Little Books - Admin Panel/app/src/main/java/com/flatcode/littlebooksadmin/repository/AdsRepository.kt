package com.flatcode.littlebooksadmin.repository

import android.net.Uri
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.model.ADs
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

    fun getUserAds(userId: String, orderBy: String = DATA.NAME): Flow<Resource<List<ADs>>> = callbackFlow {
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

    suspend fun updateSliderImage(number: Int, imageUri: Uri, extension: String): Resource<Unit> {
        return try {
            val filePath = "Images/SliderShow/$number.$extension"
            val storageRef = FirebaseStorage.getInstance().getReference(filePath)
            val uploadTask = storageRef.putFile(imageUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
            
            db.getReference(DATA.SLIDER_SHOW).child(number.toString()).setValue(downloadUrl).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Upload failed")
        }
    }
}


