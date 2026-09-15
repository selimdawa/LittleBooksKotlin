package com.flatcode.littlebooks.repository

import com.flatcode.littlebooks.Unit.DATA
import com.flatcode.littlebooks.utils.Resource
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseDatabase
) {
    suspend fun login(email: String, password: String): Resource<AuthResult> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Resource.Success(result)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun register(name: String, email: String, password: String): Resource<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val id = authResult.user?.uid ?: throw Exception("User ID is null")

            val hashMap = HashMap<String, Any>()
            hashMap[DATA.BOOKS_COUNT] = 0
            hashMap[DATA.AD_LOAD] = 0
            hashMap[DATA.AD_CLICK] = 0
            hashMap[DATA.EMAIL] = email
            hashMap[DATA.ID] = id
            hashMap[DATA.PROFILE_IMAGE] = DATA.BASIC
            hashMap[DATA.TIMESTAMP] = System.currentTimeMillis()
            hashMap[DATA.USER_NAME] = name
            hashMap[DATA.VERSION] = DATA.CURRENT_VERSION

            db.getReference(DATA.USERS).child(id).setValue(hashMap).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun forgetPassword(email: String): Resource<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser() = auth.currentUser
}