package com.flatcode.littlebooks.data.local.dao

import androidx.room.*
import com.flatcode.littlebooks.Model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()
}