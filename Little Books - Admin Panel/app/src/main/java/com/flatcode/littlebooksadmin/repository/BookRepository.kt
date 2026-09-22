package com.flatcode.littlebooksadmin.repository

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.flatcode.littlebooksadmin.db.BookDao
import com.flatcode.littlebooksadmin.db.CommentDao
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.model.Category
import com.flatcode.littlebooksadmin.model.Comment
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.Resource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BookRepository @Inject constructor(
    private val db: FirebaseDatabase,
    private val bookDao: BookDao,
    private val commentDao: CommentDao
) {

    fun getCategories(): Flow<Resource<List<Category>>> = callbackFlow {
        trySend(Resource.Loading())
        val ref = db.getReference(DATA.CATEGORIES)
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categories = mutableListOf<Category>()
                for (data in snapshot.children) {
                    val category = data.getValue(Category::class.java)
                    category?.let { categories.add(it) }
                }
                trySend(Resource.Success(categories))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.message))
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getBooks(
        orderBy: String = DATA.TIMESTAMP,
        publisherId: String? = null
    ): Flow<Resource<List<Book>>> = callbackFlow {
        trySend(Resource.Loading())
        val ref = db.getReference(DATA.BOOKS).orderByChild(orderBy)
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val books = mutableListOf<Book>()
                for (data in snapshot.children) {
                    val book = data.getValue(Book::class.java)
                    book?.let {
                        if (publisherId == null || it.publisher == publisherId) {
                            books.add(it)
                        }
                    }
                }
                this@callbackFlow.launch {
                    bookDao.insertBooks(books)
                }
                trySend(Resource.Success(books.reversed()))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.message))
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getFavorites(userId: String): Flow<Resource<List<Book>>> = callbackFlow {
        trySend(Resource.Loading())
        val favoritesRef = db.getReference(DATA.FAVORITES).child(userId)
        val listener = favoritesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val favoriteIds = snapshot.children.mapNotNull { it.key }
                if (favoriteIds.isEmpty()) {
                    trySend(Resource.Success(emptyList()))
                    return
                }

                val booksRef = db.getReference(DATA.BOOKS)
                booksRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(booksSnapshot: DataSnapshot) {
                        val books = mutableListOf<Book>()
                        for (data in booksSnapshot.children) {
                            val book = data.getValue(Book::class.java)
                            if (book != null && favoriteIds.contains(book.id)) {
                                books.add(book)
                            }
                        }
                        trySend(Resource.Success(books.reversed()))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        trySend(Resource.Error(error.message))
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.message))
            }
        })
        awaitClose { favoritesRef.removeEventListener(listener) }
    }

    suspend fun uploadBook(
        uri: Uri,
        title: String,
        description: String,
        categoryId: String
    ): Resource<String> = suspendCancellableCoroutine { continuation ->
        val ref = db.getReference(DATA.BOOKS)
        val id = ref.push().key
        if (id == null) {
            continuation.resume(Resource.Error("Could not generate ID"))
            return@suspendCancellableCoroutine
        }

        MediaManager.get().upload(uri)
            .option("folder", "PDF/Books/")
            .option("public_id", id)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val downloadUrl = resultData?.get("secure_url") as? String
                    if (downloadUrl != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val book = Book(
                                    publisher = DATA.FirebaseUserUid,
                                    id = id,
                                    title = title,
                                    description = description,
                                    categoryId = categoryId,
                                    url = downloadUrl,
                                    timestamp = System.currentTimeMillis(),
                                    image = DATA.BASIC
                                )

                                ref.child(id).setValue(book).await()
                                bookDao.insertBook(book)

                                // Increment book count for user
                                incrementUserBookCount(DATA.FirebaseUserUid)

                                continuation.resume(Resource.Success(id))
                            } catch (e: Exception) {
                                continuation.resume(
                                    Resource.Error(
                                        e.message ?: "Database update failed"
                                    )
                                )
                            }
                        }
                    } else {
                        continuation.resume(Resource.Error("Cloudinary upload failed: secure_url is null"))
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    continuation.resume(
                        Resource.Error(
                            error?.description ?: "Cloudinary upload failed"
                        )
                    )
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    suspend fun uploadBookImage(
        bookId: String,
        uri: Uri
    ): Resource<Unit> = suspendCancellableCoroutine { continuation ->
        MediaManager.get().upload(uri)
            .option("folder", "BookImages/")
            .option("public_id", bookId)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val downloadUrl = resultData?.get("secure_url") as? String
                    if (downloadUrl != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val ref = db.getReference(DATA.BOOKS).child(bookId)
                                ref.child(DATA.IMAGE).setValue(downloadUrl).await()
                                continuation.resume(Resource.Success(Unit))
                            } catch (e: Exception) {
                                continuation.resume(
                                    Resource.Error(
                                        e.message ?: "Database update failed"
                                    )
                                )
                            }
                        }
                    } else {
                        continuation.resume(Resource.Error("Cloudinary upload failed: secure_url is null"))
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    continuation.resume(
                        Resource.Error(
                            error?.description ?: "Cloudinary upload failed"
                        )
                    )
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    suspend fun getBookById(bookId: String): Resource<Book> {
        return try {
            val cachedBook = bookDao.getBookById(bookId)
            if (cachedBook != null) return Resource.Success(cachedBook)

            val snapshot = db.getReference(DATA.BOOKS).child(bookId).get().await()
            val book = snapshot.getValue(Book::class.java)
            if (book != null) {
                bookDao.insertBook(book)
                Resource.Success(book)
            } else Resource.Error("Book not found")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun updateBook(bookId: String, updates: Map<String, Any?>): Resource<Unit> {
        return try {
            db.getReference(DATA.BOOKS).child(bookId).updateChildren(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Update failed")
        }
    }

    fun getComments(bookId: String): Flow<Resource<List<Comment>>> = callbackFlow {
        trySend(Resource.Loading())
        val ref = db.getReference(DATA.BOOKS).child(bookId).child(DATA.COMMENTS)
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val comments = mutableListOf<Comment>()
                for (data in snapshot.children) {
                    val comment = data.getValue(Comment::class.java)
                    comment?.let { comments.add(it) }
                }
                this@callbackFlow.launch {
                    commentDao.insertComments(comments)
                }
                trySend(Resource.Success(comments))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.message))
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun addComment(bookId: String, commentText: String): Resource<Unit> {
        return try {
            val ref = db.getReference(DATA.BOOKS).child(bookId).child(DATA.COMMENTS)
            val id = ref.push().key ?: return Resource.Error("Could not generate ID")

            val comment = Comment(
                id = id,
                bookId = bookId,
                timestamp = System.currentTimeMillis(),
                comment = commentText,
                publisher = DATA.FirebaseUserUid
            )

            ref.child(id).setValue(comment).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Comment failed")
        }
    }

    suspend fun incrementViews(bookId: String) {
        val ref = db.getReference(DATA.BOOKS).child(bookId).child(DATA.VIEWS_COUNT)
        val snapshot = ref.get().await()
        val current = snapshot.getValue(Int::class.java) ?: 0
        ref.setValue(current + 1).await()
    }

    fun getBooksByCategory(
        categoryId: String,
        orderBy: String = DATA.TIMESTAMP
    ): Flow<Resource<List<Book>>> = callbackFlow {
        trySend(Resource.Loading())
        val ref = db.getReference(DATA.BOOKS).orderByChild(orderBy)
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val books = mutableListOf<Book>()
                for (data in snapshot.children) {
                    val book = data.getValue(Book::class.java)
                    if (book?.categoryId == categoryId) {
                        books.add(book)
                    }
                }
                this@callbackFlow.launch {
                    bookDao.insertBooks(books)
                }
                trySend(Resource.Success(books.reversed()))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.message))
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getEditorsChoiceBooks(): Flow<Resource<List<Book>>> = callbackFlow {
        trySend(Resource.Loading())
        val ref = db.getReference(DATA.BOOKS)
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val books = mutableListOf<Book>()
                for (data in snapshot.children) {
                    val book = data.getValue(Book::class.java)
                    if (book != null && book.editorsChoice != 0) {
                        books.add(book)
                    }
                }
                this@callbackFlow.launch {
                    bookDao.insertBooks(books)
                }
                trySend(Resource.Success(books))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.message))
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    private suspend fun incrementUserBookCount(userId: String) {
        val ref = db.getReference(DATA.USERS).child(userId).child(DATA.BOOKS_COUNT)
        val snapshot = ref.get().await()
        val currentCount = snapshot.getValue(Int::class.java) ?: 0
        ref.setValue(currentCount + 1).await()
    }
}


