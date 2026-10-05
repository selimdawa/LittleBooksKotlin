package com.flatcode.littlebooks.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.flatcode.littlebooks.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: String): Flow<User?>

    @Query("SELECT COUNT(*) FROM users WHERE id != :currentUserId AND booksCount >= 1")
    fun getExplorePublishersCount(currentUserId: String): Flow<Int>

    @Query("SELECT * FROM users WHERE id != :currentUserId AND booksCount >= 1")
    fun getExplorePublishers(currentUserId: String): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}