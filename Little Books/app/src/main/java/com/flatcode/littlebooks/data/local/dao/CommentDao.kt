package com.flatcode.littlebooks.data.local.dao

import androidx.room.*
import com.flatcode.littlebooks.Model.Comment
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE bookId = :bookId")
    fun getCommentsForBook(bookId: String): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)

    @Delete
    suspend fun deleteComment(comment: Comment)
}