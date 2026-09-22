package com.flatcode.littlebooks.repository

import android.net.Uri
import com.flatcode.littlebooks.db.BookDao
import com.flatcode.littlebooks.db.CommentDao
import com.flatcode.littlebooks.model.Book
import com.flatcode.littlebooks.model.Comment
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.Resource
import com.flatcode.littlebooks.utils.cloudinaryUpload
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepository @Inject constructor(
    private val db: FirebaseDatabase,
    private val bookDao: BookDao,
    private val commentDao: CommentDao
) {

    suspend fun syncBooksFromRemote(orderBy: String = "", limit: Int = 0): Resource<Unit> {
        return try {
            var query: Query = db.getReference(DATA.BOOKS)
            if (orderBy.isNotEmpty()) {
                query = query.orderByChild(orderBy)
            }
            if (limit > 0) {
                query = query.limitToLast(limit)
            }

            val snapshot = query.get().await()
            val list = mutableListOf<Book>()
            for (data in snapshot.children) {
                data.getValue(Book::class.java)?.let { list.add(it) }
            }
            bookDao.insertBooks(list)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Sync failed")
        }
    }

    suspend fun getSliderImages(): Resource<List<String>> {
        return try {
            val snapshot = db.getReference(DATA.SLIDER_SHOW).get().await()
            val list = mutableListOf<String>()
            for (data in snapshot.children) {
                data.value?.toString()?.let { list.add(it) }
            }
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun getBooksBy(orderBy: String, limit: Int = 0): Resource<List<Book>> {
        return try {
            syncBooksFromRemote(orderBy, limit)
            val list = bookDao.getAllBooks().first()
            Resource.Success(list)
        } catch (e: Exception) {
            // Fallback to local if remote fails
            val localList = bookDao.getAllBooks().first()
            if (localList.isNotEmpty()) Resource.Success(localList)
            else Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun getEditorsChoiceBooks(): Resource<List<Book>> {
        return try {
            val snapshot =
                db.getReference(DATA.BOOKS).orderByChild(DATA.EDITORS_CHOICE).get().await()
            val list = mutableListOf<Book>()
            for (data in snapshot.children) {
                val item = data.getValue(Book::class.java)
                if (item != null && (item.editorsChoice == 1 || item.editorsChoice == 2)) {
                    list.add(item)
                }
            }
            bookDao.insertBooks(list)
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun getBooksCountByPublisher(publisherId: String): Resource<Int> {
        return try {
            val snapshot = db.getReference(DATA.BOOKS).get().await()
            var count = 0
            for (data in snapshot.children) {
                val item = data.getValue(Book::class.java)
                if (item?.publisher == publisherId) count++
            }
            Resource.Success(count)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun getBookDetails(bookId: String): Resource<Book> {
        return try {
            val localBook = bookDao.getBookById(bookId)
            if (localBook != null) return Resource.Success(localBook)

            val snapshot = db.getReference(DATA.BOOKS).child(bookId).get().await()
            val book = snapshot.getValue(Book::class.java)
            if (book != null) {
                bookDao.insertBook(book)
                Resource.Success(book)
            } else Resource.Error("Book not found")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun addComment(bookId: String, commentData: Map<String, Any>): Resource<Unit> {
        return try {
            db.getReference(DATA.COMMENTS).child(bookId).push().setValue(commentData).await()
            // Note: We don't have the comment object here easily without parsing map, 
            // usually we'd insert into local db after a successful remote add or sync.
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun toggleFavorite(
        userId: String, bookId: String, isFavorite: Boolean
    ): Resource<Unit> {
        return try {
            val ref = db.getReference(DATA.FAVORITES).child(userId).child(bookId)
            if (isFavorite) ref.setValue(true).await()
            else ref.removeValue().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun checkFavorite(userId: String, bookId: String): Resource<Boolean> {
        return try {
            val snapshot = db.getReference(DATA.FAVORITES).child(userId).child(bookId).get().await()
            Resource.Success(snapshot.exists())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun getFavorites(userId: String): Resource<List<Book>> {
        return try {
            val favSnapshot = db.getReference(DATA.FAVORITES).child(userId).get().await()
            val bookList = mutableListOf<Book>()
            for (data in favSnapshot.children) {
                val bookId = data.key ?: continue
                val bookSnapshot = db.getReference(DATA.BOOKS).child(bookId).get().await()
                bookSnapshot.getValue(Book::class.java)?.let {
                    bookList.add(it)
                    bookDao.insertBook(it)
                }
            }
            Resource.Success(bookList)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun getComments(bookId: String): Resource<List<Comment>> {
        return try {
            val snapshot = db.getReference(DATA.COMMENTS).child(bookId).get().await()
            val list = mutableListOf<Comment>()
            for (data in snapshot.children) {
                data.getValue(Comment::class.java)?.let {
                    list.add(it)
                    commentDao.insertComment(it)
                }
            }
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun getBooksFromFollowedPublishers(
        followedIds: List<String>, orderBy: String
    ): Resource<List<Book>> {
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
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun uploadBookFile(bookUri: Uri): Resource<String> {
        return cloudinaryUpload(bookUri)
    }

    suspend fun uploadBookImage(imageUri: Uri): Resource<String> {
        return cloudinaryUpload(imageUri)
    }

    suspend fun addBook(bookData: Map<String, Any?>): Resource<String> {
        return try {
            val id = bookData[DATA.ID] as String
            db.getReference(DATA.BOOKS).child(id).setValue(bookData).await()
            // We should ideally create a Book object from bookData and insert to Room
            Resource.Success(id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun updateBook(bookId: String, hashMap: Map<String, Any?>): Resource<Unit> {
        return try {
            db.getReference(DATA.BOOKS).child(bookId).updateChildren(hashMap).await()
            // Fetch updated book and sync to Room
            val snapshot = db.getReference(DATA.BOOKS).child(bookId).get().await()
            snapshot.getValue(Book::class.java)?.let { bookDao.insertBook(it) }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun getBooksByPublisher(publisherId: String, orderBy: String): Resource<List<Book>> {
        return try {
            val snapshot = db.getReference(DATA.BOOKS).orderByChild(orderBy).get().await()
            val list = mutableListOf<Book>()
            for (data in snapshot.children) {
                val item = data.getValue(Book::class.java)
                if (item?.publisher == publisherId) {
                    list.add(item)
                    bookDao.insertBook(item)
                }
            }
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun getBooksByCategory(categoryId: String, orderBy: String): Resource<List<Book>> {
        return try {
            val snapshot = db.getReference(DATA.BOOKS).orderByChild(orderBy).get().await()
            val list = mutableListOf<Book>()
            for (data in snapshot.children) {
                val item = data.getValue(Book::class.java)
                if (item?.categoryId == categoryId) {
                    list.add(item)
                    bookDao.insertBook(item)
                }
            }
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    fun getBookFile(pdfUrl: String): Resource<ByteArray> {
        return try {
            val bytes = URL(pdfUrl).readBytes()
            Resource.Success(bytes)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun incrementViewCount(bookId: String): Resource<Unit> {
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
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }
}