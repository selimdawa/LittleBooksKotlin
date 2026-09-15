package com.flatcode.littlebooks.repository

import com.flatcode.littlebooks.Model.Category
import com.flatcode.littlebooks.Unit.DATA
import com.flatcode.littlebooks.utils.Resource
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val db: FirebaseDatabase
) {
    suspend fun getCategories(): Resource<List<Category>> {
        return try {
            val snapshot = db.getReference(DATA.CATEGORIES).get().await()
            val list = mutableListOf<Category>()
            for (data in snapshot.children) {
                data.getValue(Category::class.java)?.let { list.add(it) }
            }
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }
}