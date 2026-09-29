package com.flatcode.littlebooks.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.flatcode.littlebooks.model.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query("SELECT * FROM books ORDER BY timestamp DESC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books ORDER BY viewsCount DESC LIMIT :limit")
    fun getMostViewedBooks(limit: Int): Flow<List<Book>>

    @Query("SELECT * FROM books ORDER BY viewsCount DESC")
    fun getMostViewedBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books ORDER BY lovesCount DESC")
    fun getMostLovedBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books ORDER BY downloadsCount DESC")
    fun getMostDownloadedBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books ORDER BY timestamp DESC LIMIT :limit")
    fun getLatestBooks(limit: Int): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE categoryId = :categoryId ORDER BY timestamp DESC")
    fun getBooksByCategory(categoryId: String): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE publisher = :publisherId ORDER BY timestamp DESC")
    fun getBooksByPublisher(publisherId: String): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :id")
    fun observeBookById(id: String): Flow<Book?>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: String): Book?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<Book>)

    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)

    @Query("DELETE FROM books")
    suspend fun deleteAllBooks()
}
