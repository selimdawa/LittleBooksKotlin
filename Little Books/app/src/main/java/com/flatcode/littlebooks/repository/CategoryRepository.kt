package com.flatcode.littlebooks.repository

import com.flatcode.littlebooks.db.CategoryDao
import com.flatcode.littlebooks.model.Category
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.Resource
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val db: FirebaseDatabase, private val categoryDao: CategoryDao
) {
    suspend fun getCategories(): Resource<List<Category>> {
        return try {
            val snapshot = db.getReference(DATA.CATEGORIES).get().await()
            val list = mutableListOf<Category>()
            for (data in snapshot.children) {
                data.getValue(Category::class.java)?.let { list.add(it) }
            }
            categoryDao.insertCategories(list)
            Resource.Success(list)
        } catch (e: Exception) {
            val localList = categoryDao.getAllCategories().first()
            if (localList.isNotEmpty()) Resource.Success(localList)
            else Resource.Error(e.message ?: "An unknown error occurred")
        }
    }
}