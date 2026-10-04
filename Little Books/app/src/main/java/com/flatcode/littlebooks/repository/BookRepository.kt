package com.flatcode.littlebooks.repository

import android.net.Uri
import com.flatcode.littlebooks.db.BookDao
import com.flatcode.littlebooks.db.CommentDao
import com.flatcode.littlebooks.db.FavoriteDao
import com.flatcode.littlebooks.db.SliderDao
import com.flatcode.littlebooks.model.Book
import com.flatcode.littlebooks.model.Comment
import com.flatcode.littlebooks.model.FavoriteEntity
import com.flatcode.littlebooks.model.SliderEntity
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.cloudinaryUpload
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepository @Inject constructor(
    private val db: FirebaseDatabase,
    private val bookDao: BookDao,
    private val commentDao: CommentDao,
    private val favoriteDao: FavoriteDao,
    private val sliderDao: SliderDao
) {

    fun getBooks(orderBy: String, limit: Int? = null): Flow<List<Book>> {
        syncBooksFromRemote()
        val l = limit ?: 100
        return when (orderBy) {
            DATA.VIEWS_COUNT -> bookDao.getMostViewedBooks(l)
            DATA.LOVES_COUNT -> bookDao.getMostLovedBooks(l)
            DATA.DOWNLOADS_COUNT -> bookDao.getMostDownloadedBooks(l)
            DATA.EDITORS_CHOICE -> bookDao.getAllBooks().map { list ->
                list.filter { it.editorsChoice == 1 || it.editorsChoice == 2 }.take(l)
            }

            else -> bookDao.getLatestBooks(l)
        }
    }

    fun getEditorsChoiceBooks(): Flow<List<Book>> {
        return getBooks(DATA.EDITORS_CHOICE, DATA.ORDER_MAIN)
    }

    fun getBooksByCategory(categoryId: String): Flow<List<Book>> {
        syncBooksFromRemote()
        return bookDao.getBooksByCategory(categoryId)
    }

    fun getBooksByPublisher(publisherId: String): Flow<List<Book>> {
        syncBooksFromRemote()
        return bookDao.getBooksByPublisher(publisherId)
    }

    fun observeBookById(bookId: String): Flow<Book?> {
        syncBookById(bookId)
        return bookDao.observeBookById(bookId)
    }

    private fun syncBookById(bookId: String) {
        if (bookId.isEmpty()) return
        db.getReference(DATA.BOOKS).child(bookId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(Book::class.java)?.let { book ->
                        CoroutineScope(Dispatchers.IO).launch {
                            bookDao.insertBook(book)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), "syncBookById failed")
                }
            })
    }

    fun syncBooksFromRemote() {
        db.getReference(DATA.BOOKS).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Book>()
                for (data in snapshot.children) {
                    val item = data.getValue(Book::class.java) ?: continue
                    list.add(item)
                }
                CoroutineScope(Dispatchers.IO).launch {
                    bookDao.insertBooks(list)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException(), "syncBooksFromRemote failed")
            }
        })
    }

    fun getFavoriteBooks(userId: String): Flow<List<Book>> {
        syncFavorites(userId)
        return favoriteDao.getFavoriteBooks(userId)
    }

    private fun syncFavorites(userId: String) {
        if (userId.isEmpty()) return
        db.getReference(DATA.FAVORITES).child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val favList =
                        snapshot.children.mapNotNull { it.key }.map { FavoriteEntity(userId, it) }
                    CoroutineScope(Dispatchers.IO).launch {
                        favoriteDao.deleteAllFavoritesForUser(userId)
                        favoriteDao.insertFavorites(favList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), "syncFavorites failed")
                }
            })
    }

    suspend fun toggleFavorite(
        userId: String, bookId: String, isFavorite: Boolean
    ): Result<Unit> {
        return try {
            val ref = db.getReference(DATA.FAVORITES).child(userId).child(bookId)
            if (isFavorite) {
                ref.setValue(true).await()
                favoriteDao.insertFavorite(FavoriteEntity(userId, bookId))
            } else {
                ref.removeValue().await()
                favoriteDao.deleteFavorite(userId, bookId)
            }
            Result.success(Unit)
        } catch (_: Exception) {
            if (isFavorite) {
                favoriteDao.insertFavorite(FavoriteEntity(userId, bookId))
            } else {
                favoriteDao.deleteFavorite(userId, bookId)
            }
            Result.success(Unit)
        }
    }

    suspend fun getSliderImages(): List<String> {
        return try {
            val snapshot = db.getReference(DATA.SLIDER_SHOW).get().await()
            val list = mutableListOf<String>()
            val sliderList = mutableListOf<SliderEntity>()
            var index = 0
            for (data in snapshot.children) {
                val url = data.value?.toString()
                if (!url.isNullOrEmpty()) {
                    list.add(url)
                    sliderList.add(SliderEntity(index.toString(), url, index++))
                }
            }
            sliderDao.deleteAllSliderImages()
            sliderDao.insertSliderImages(sliderList)
            list
        } catch (_: Exception) {
            sliderDao.getSliderImages().first().map { it.image }
        }
    }

    suspend fun getBooksCountByPublisher(publisherId: String): Int {
        return try {
            val snapshot = db.getReference(DATA.BOOKS).get().await()
            var count = 0
            for (data in snapshot.children) {
                val item = data.getValue(Book::class.java)
                if (item?.publisher == publisherId) count++
            }
            count
        } catch (_: Exception) {
            0
        }
    }

    suspend fun addComment(bookId: String, commentData: Map<String, Any>): Result<Unit> {
        return try {
            db.getReference(DATA.COMMENTS).child(bookId).push().setValue(commentData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkFavorite(userId: String, bookId: String): Boolean {
        return try {
            val snapshot = db.getReference(DATA.FAVORITES).child(userId).child(bookId).get().await()
            snapshot.exists()
        } catch (_: Exception) {
            false
        }
    }

    fun getComments(bookId: String): Flow<List<Comment>> {
        syncComments(bookId)
        return commentDao.getCommentsForBook(bookId)
    }

    private fun syncComments(bookId: String) {
        if (bookId.isEmpty()) return
        db.getReference(DATA.COMMENTS).child(bookId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Comment>()
                    for (data in snapshot.children) {
                        data.getValue(Comment::class.java)?.let {
                            list.add(it)
                        }
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        commentDao.insertComments(list)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), "syncComments failed")
                }
            })
    }

    suspend fun getBooksFromFollowedPublishers(
        followedIds: List<String>, orderBy: String
    ): List<Book> {
        return try {
            val snapshot = db.getReference(DATA.BOOKS).orderByChild(orderBy).get().await()
            val list = mutableListOf<Book>()
            for (data in snapshot.children) {
                val item = data.getValue(Book::class.java)
                if (item != null && followedIds.contains(item.publisher)) {
                    list.add(item)
                    bookDao.insertBook(item)
                }
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun uploadBookFile(bookUri: Uri): Result<String> {
        return try {
            val url = cloudinaryUpload(bookUri)
            if (url.isNotEmpty()) {
                Result.success(url)
            } else {
                Result.failure(Exception("Upload file failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadBookImage(imageUri: Uri): Result<String> {
        return try {
            val url = cloudinaryUpload(imageUri)
            if (url.isNotEmpty()) {
                Result.success(url)
            } else {
                Result.failure(Exception("Upload image failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addBook(bookData: Map<String, Any?>): Result<String> {
        return try {
            val id = bookData[DATA.ID] as String
            db.getReference(DATA.BOOKS).child(id).setValue(bookData).await()
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBook(bookId: String, hashMap: Map<String, Any?>): Result<Unit> {
        return try {
            db.getReference(DATA.BOOKS).child(bookId).updateChildren(hashMap).await()
            val snapshot = db.getReference(DATA.BOOKS).child(bookId).get().await()
            snapshot.getValue(Book::class.java)?.let { bookDao.insertBook(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getBookFile(pdfUrl: String): Result<ByteArray> {
        return try {
            val bytes = URL(pdfUrl).readBytes()
            Result.success(bytes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun incrementViewCount(bookId: String): Result<Unit> {
        return try {
            val ref = db.getReference(DATA.BOOKS).child(bookId).child(DATA.VIEWS_COUNT)
            val currentCount = ref.get().await().getValue(Int::class.java) ?: 0
            val newCount = currentCount + 1
            ref.setValue(newCount).await()

            val book = bookDao.getBookById(bookId)
            book?.let {
                it.viewsCount = newCount
                bookDao.updateBook(it)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}