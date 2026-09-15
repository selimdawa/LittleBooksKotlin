package com.flatcode.littlebooks.repository

import android.content.Context
import android.net.Uri
import com.flatcode.littlebooks.Model.Book
import com.flatcode.littlebooks.Model.Comment
import com.flatcode.littlebooks.Unit.DATA
import com.flatcode.littlebooks.Unit.VOID
import com.flatcode.littlebooks.utils.Resource
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepository @Inject constructor(
    private val db: FirebaseDatabase
) {
    suspend fun getSliderCount(): Resource<Int> {
        return try {
            val snapshot = db.getReference(DATA.SLIDER_SHOW).get().await()
            Resource.Success(snapshot.childrenCount.toInt())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun getBooksBy(orderBy: String, limit: Int = 0): Resource<List<Book>> {
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
            if (orderBy != DATA.EDITORS_CHOICE && limit > 0) {
                list.reverse()
            }
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun getEditorsChoiceBooks(): Resource<List<Book>> {
        return try {
            val snapshot = db.getReference(DATA.BOOKS).orderByChild(DATA.EDITORS_CHOICE).get().await()
            val list = mutableListOf<Book>()
            for (data in snapshot.children) {
                val item = data.getValue(Book::class.java)
                if (item != null && (item.editorsChoice == 1 || item.editorsChoice == 2)) {
                    list.add(item)
                }
            }
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
            val snapshot = db.getReference(DATA.BOOKS).child(bookId).get().await()
            val book = snapshot.getValue(Book::class.java)
            if (book != null) Resource.Success(book)
            else Resource.Error("Book not found")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun addComment(bookId: String, commentData: Map<String, Any>): Resource<Unit> {
        return try {
            db.getReference(DATA.COMMENTS).child(bookId).push().setValue(commentData).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun toggleFavorite(userId: String, bookId: String, isFavorite: Boolean): Resource<Unit> {
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
                bookSnapshot.getValue(Book::class.java)?.let { bookList.add(it) }
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
                data.getValue(Comment::class.java)?.let { list.add(it) }
            }
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun getBooksFromFollowedPublishers(followedIds: List<String>, orderBy: String): Resource<List<Book>> {
        return try {
            val snapshot = db.getReference(DATA.BOOKS).orderByChild(orderBy).get().await()
            val list = mutableListOf<Book>()
            for (data in snapshot.children) {
                val item = data.getValue(Book::class.java)
                if (item != null && followedIds.contains(item.publisher)) {
                    list.add(item)
                }
            }
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun uploadBookFile(userId: String, bookUri: Uri, context: Context): Resource<String> {
        return try {
            val id = db.getReference(DATA.BOOKS).push().key ?: throw Exception("Could not generate book ID")
            val filePathAndName = "PDF/Books/$id"
            val extension = VOID.getFileExtension(bookUri, context)
            val reference = FirebaseStorage.getInstance().getReference("$filePathAndName.${extension}")
            val task = reference.putFile(bookUri).await()
            val downloadUrl = task.storage.downloadUrl.await()
            Resource.Success(downloadUrl.toString())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun uploadBookImage(userId: String, imageUri: Uri, context: Context): Resource<String> {
        return try {
            val filePathAndName = "BookImages/$userId"
            val extension = VOID.getFileExtension(imageUri, context)
            val reference = FirebaseStorage.getInstance().getReference("$filePathAndName.${extension}")
            val task = reference.putFile(imageUri).await()
            val downloadUrl = task.storage.downloadUrl.await()
            Resource.Success(downloadUrl.toString())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun addBook(bookData: Map<String, Any?>): Resource<String> {
        return try {
            val id = bookData[DATA.ID] as String
            db.getReference(DATA.BOOKS).child(id).setValue(bookData).await()
            Resource.Success(id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun updateBook(bookId: String, hashMap: Map<String, Any?>): Resource<Unit> {
        return try {
            db.getReference(DATA.BOOKS).child(bookId).updateChildren(hashMap).await()
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
                }
            }
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun getBookFile(pdfUrl: String): Resource<ByteArray> {
        return try {
            val reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            val bytes = reference.getBytes(DATA.MAX_BYTES_PDF.toLong()).await()
            Resource.Success(bytes)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun deleteBook(bookId: String, bookUrl: String): Resource<Unit> {
        return try {
            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageRef.delete().await()
            db.getReference(DATA.BOOKS).child(bookId).removeValue().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun incrementViewCount(bookId: String): Resource<Unit> {
        return try {
            val ref = db.getReference(DATA.BOOKS).child(bookId).child(DATA.VIEWS_COUNT)
            val currentCount = ref.get().await().getValue(Int::class.java) ?: 0
            ref.setValue(currentCount + 1).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }
}