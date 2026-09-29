package com.flatcode.littlebooks.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.flatcode.littlebooks.model.Category
import com.flatcode.littlebooks.model.InterestedEntity
import com.flatcode.littlebooks.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface InterestedDao {

    @Query("SELECT categories.* FROM categories INNER JOIN interested ON categories.id = interested.itemId WHERE interested.userId = :userId AND interested.databaseName = :databaseName ORDER BY categories.timestamp DESC")
    fun getInterestedCategories(userId: String, databaseName: String): Flow<List<Category>>

    @Query("SELECT users.* FROM users INNER JOIN interested ON users.id = interested.itemId WHERE interested.userId = :userId AND interested.databaseName = :databaseName ORDER BY users.timestamp DESC")
    fun getInterestedPublishers(userId: String, databaseName: String): Flow<List<User>>

    @Query("SELECT COUNT(*) FROM interested WHERE userId = :userId AND databaseName = :databaseName")
    fun getInterestedCount(userId: String, databaseName: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterested(interested: InterestedEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterestedList(list: List<InterestedEntity>)

    @Query("DELETE FROM interested WHERE userId = :userId AND databaseName = :databaseName AND itemId = :itemId")
    suspend fun deleteInterested(userId: String, databaseName: String, itemId: String)

    @Query("DELETE FROM interested WHERE userId = :userId AND databaseName = :databaseName")
    suspend fun deleteAllInterestedForUser(userId: String, databaseName: String)
}
