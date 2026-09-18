package com.flatcode.littlebooks.db

import androidx.room.*
import com.flatcode.littlebooks.model.Comment
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


