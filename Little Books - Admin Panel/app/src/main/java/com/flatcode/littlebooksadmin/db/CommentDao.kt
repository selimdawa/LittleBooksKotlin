package com.flatcode.littlebooksadmin.db

import androidx.room.*
import com.flatcode.littlebooksadmin.model.Comment
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE bookId = :bookId ORDER BY timestamp DESC")
    fun getCommentsForBook(bookId: String): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<Comment>)

    @Query("DELETE FROM comments WHERE bookId = :bookId")
    suspend fun clearCommentsForBook(bookId: String)
}
