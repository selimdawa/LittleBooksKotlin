package com.flatcode.littlebooks.repository

import com.flatcode.littlebooks.utils.DATA
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth, private val db: FirebaseDatabase
) {
    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val id = authResult.user?.uid ?: return Result.failure(Exception("User ID is null"))

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
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun forgetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUser() = auth.currentUser
}
