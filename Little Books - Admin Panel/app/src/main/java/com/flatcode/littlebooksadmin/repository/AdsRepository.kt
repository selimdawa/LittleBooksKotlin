package com.flatcode.littlebooksadmin.repository

import com.flatcode.littlebooksadmin.model.ADs
import com.flatcode.littlebooksadmin.model.User
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.Resource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
}