package com.flatcode.littlebooks.repository

import com.flatcode.littlebooks.db.CategoryDao
import com.flatcode.littlebooks.model.Category
import com.flatcode.littlebooks.utils.DATA
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val db: FirebaseDatabase, private val categoryDao: CategoryDao
) {
    fun getCategories(): Flow<List<Category>> {
        syncCategories()
        return categoryDao.getAllCategories()
    }

    fun getCategoryName(categoryId: String): Flow<String?> {
        syncCategories()
        return categoryDao.getAllCategories().map { list ->
            list.find { it.id == categoryId }?.category
        }
    }

    fun syncCategories() {
        db.getReference(DATA.CATEGORIES).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Category>()
                for (data in snapshot.children) {
                    val item = data.getValue(Category::class.java) ?: continue
                    if (item.category.isNullOrEmpty()) {
                        item.category = data.child("name").value?.toString()
                            ?: data.child("category").value?.toString()
                                    ?: data.child("title").value?.toString()
                    }
                    list.add(item)
                }
                CoroutineScope(Dispatchers.IO).launch {
                    categoryDao.insertCategories(list)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException(), "syncCategories failed")
            }
        })
    }
}
