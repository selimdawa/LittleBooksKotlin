package com.flatcode.littlebooks.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.flatcode.littlebooks.model.Book
import com.flatcode.littlebooks.model.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT books.* FROM books INNER JOIN favorites ON books.id = favorites.bookId WHERE favorites.userId = :userId ORDER BY books.timestamp DESC")
    fun getFavoriteBooks(userId: String): Flow<List<Book>>

    @Query("SELECT COUNT(books.id) FROM books INNER JOIN favorites ON books.id = favorites.bookId WHERE favorites.userId = :userId")
    fun getFavoriteCount(userId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM favorites WHERE userId = :userId")
    fun getTotalFavoriteCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorites(favorites: List<FavoriteEntity>)

    @Query("DELETE FROM favorites WHERE userId = :userId AND bookId = :bookId")
    suspend fun deleteFavorite(userId: String, bookId: String)

    @Query("DELETE FROM favorites WHERE userId = :userId")
    suspend fun deleteAllFavoritesForUser(userId: String)
}
