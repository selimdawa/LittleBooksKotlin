package com.flatcode.littlebooksadmin.data.repository

import android.net.Uri
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.data.local.CategoryDao
import com.flatcode.littlebooksadmin.data.model.Category
import com.flatcode.littlebooksadmin.data.util.Resource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val db: FirebaseDatabase,
    private val storage: FirebaseStorage,
    private val categoryDao: CategoryDao
) {

    fun getCategories(orderBy: String = DATA.CATEGORY): Flow<Resource<List<Category>>> = callbackFlow {
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

    suspend fun addCategory(name: String, imageUri: Uri?, extension: String?): Resource<Unit> {
        return try {
            val ref = db.getReference(DATA.CATEGORIES)
            val id = ref.push().key ?: return Resource.Error("Could not generate ID")
            
            var imageUrl = DATA.BASIC
            if (imageUri != null && extension != null) {
                val filePath = "CategoryImages/$id.$extension"
                val storageRef = storage.getReference(filePath)
                val uploadTask = storageRef.putFile(imageUri).await()
                imageUrl = uploadTask.storage.downloadUrl.await().toString()
            }
            
            val category = Category(
                id = id,
                category = name,
                image = imageUrl,
                publisher = DATA.FirebaseUserUid,
                timestamp = System.currentTimeMillis()
            )
            
            ref.child(id).setValue(category).await()
            categoryDao.insertCategory(category)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun deleteCategory(id: String): Resource<Unit> {
        return try {
            db.getReference(DATA.CATEGORIES).child(id).removeValue().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}
