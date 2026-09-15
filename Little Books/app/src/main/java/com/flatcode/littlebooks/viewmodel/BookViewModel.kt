package com.flatcode.littlebooks.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooks.Model.Book
import com.flatcode.littlebooks.Model.Comment
import com.flatcode.littlebooks.Unit.DATA
import com.flatcode.littlebooks.repository.BookRepository
import com.flatcode.littlebooks.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

    private val _bookDetails = MutableStateFlow<Resource<Book>>(Resource.Loading())
    val bookDetails: StateFlow<Resource<Book>> = _bookDetails

    private val _isFavorite = MutableStateFlow<Resource<Boolean>>(Resource.Loading())
    val isFavorite: StateFlow<Resource<Boolean>> = _isFavorite

    private val _favorites = MutableStateFlow<Resource<List<Book>>>(Resource.Loading())
    val favorites: StateFlow<Resource<List<Book>>> = _favorites

    private val _comments = MutableStateFlow<Resource<List<Comment>>>(Resource.Loading())
    val comments: StateFlow<Resource<List<Comment>>> = _comments

    private val _addBookStatus = MutableStateFlow<Resource<String>?>(null)
    val addBookStatus: StateFlow<Resource<String>?> = _addBookStatus

    private val _uploadFileStatus = MutableStateFlow<Resource<String>?>(null)
    val uploadFileStatus: StateFlow<Resource<String>?> = _uploadFileStatus

    private val _uploadImageStatus = MutableStateFlow<Resource<String>?>(null)
    val uploadImageStatus: StateFlow<Resource<String>?> = _uploadImageStatus

    private val _booksByPublisher = MutableStateFlow<Resource<List<Book>>>(Resource.Loading())
    val booksByPublisher: StateFlow<Resource<List<Book>>> = _booksByPublisher

    private val _allBooks = MutableStateFlow<Resource<List<Book>>>(Resource.Loading())
    val allBooks: StateFlow<Resource<List<Book>>> = _allBooks

    private val _booksByCategory = MutableStateFlow<Resource<List<Book>>>(Resource.Loading())
    val booksByCategory: StateFlow<Resource<List<Book>>> = _booksByCategory

    private val _bookFile = MutableStateFlow<Resource<ByteArray>>(Resource.Loading())
    val bookFile: StateFlow<Resource<ByteArray>> = _bookFile

    private val _deleteStatus = MutableStateFlow<Resource<Unit>?>(null)
    val deleteStatus: StateFlow<Resource<Unit>?> = _deleteStatus

    fun loadBookDetails(bookId: String, userId: String) {
        viewModelScope.launch {
            _bookDetails.value = Resource.Loading()
            _bookDetails.value = repository.getBookDetails(bookId)
            _isFavorite.value = repository.checkFavorite(userId, bookId)
            _comments.value = repository.getComments(bookId)
        }
    }

    fun toggleFavorite(userId: String, bookId: String, isFav: Boolean) {
        viewModelScope.launch {
            val result = repository.toggleFavorite(userId, bookId, isFav)
            if (result is Resource.Success) {
                _isFavorite.value = Resource.Success(isFav)
            }
        }
    }

    fun loadFavorites(userId: String) {
        viewModelScope.launch {
            _favorites.value = Resource.Loading()
            _favorites.value = repository.getFavorites(userId)
        }
    }

    fun addComment(bookId: String, comment: String, userId: String) {
        viewModelScope.launch {
            val commentData = HashMap<String, Any>()
            commentData[DATA.ID] = DATA.EMPTY + System.currentTimeMillis()
            commentData[DATA.BOOK_ID] = bookId
            commentData[DATA.TIMESTAMP] = System.currentTimeMillis()
            commentData[DATA.COMMENT] = comment
            commentData[DATA.PUBLISHER] = userId
            
            val result = repository.addComment(bookId, commentData)
            if (result is Resource.Success) {
                _comments.value = repository.getComments(bookId)
            }
        }
    }

    fun uploadBookFile(userId: String, bookUri: Uri, context: Context) {
        viewModelScope.launch {
            _uploadFileStatus.value = Resource.Loading()
            _uploadFileStatus.value = repository.uploadBookFile(userId, bookUri, context)
        }
    }

    fun uploadBookImage(userId: String, imageUri: Uri, context: Context) {
        viewModelScope.launch {
            _uploadImageStatus.value = Resource.Loading()
            _uploadImageStatus.value = repository.uploadBookImage(userId, imageUri, context)
        }
    }

    fun addBook(bookData: HashMap<String, Any?>) {
        viewModelScope.launch {
            _addBookStatus.value = Resource.Loading()
            _addBookStatus.value = repository.addBook(bookData)
        }
    }

    fun updateBook(bookId: String, hashMap: HashMap<String, Any?>) {
        viewModelScope.launch {
            repository.updateBook(bookId, hashMap)
        }
    }

    fun loadBooksByPublisher(publisherId: String, orderBy: String) {
        viewModelScope.launch {
            _booksByPublisher.value = Resource.Loading()
            _booksByPublisher.value = repository.getBooksByPublisher(publisherId, orderBy)
        }
    }

    fun loadBooksBy(orderBy: String) {
        viewModelScope.launch {
            _allBooks.value = Resource.Loading()
            if (orderBy == DATA.EDITORS_CHOICE) {
                _allBooks.value = repository.getEditorsChoiceBooks()
            } else {
                _allBooks.value = repository.getBooksBy(orderBy)
            }
        }
    }

    fun loadBooksByCategory(categoryId: String, orderBy: String) {
        viewModelScope.launch {
            _booksByCategory.value = Resource.Loading()
            _booksByCategory.value = repository.getBooksByCategory(categoryId, orderBy)
        }
    }

    fun loadBookFile(pdfUrl: String) {
        viewModelScope.launch {
            _bookFile.value = Resource.Loading()
            _bookFile.value = repository.getBookFile(pdfUrl)
        }
    }

    fun deleteBook(bookId: String, bookUrl: String) {
        viewModelScope.launch {
            _deleteStatus.value = Resource.Loading()
            _deleteStatus.value = repository.deleteBook(bookId, bookUrl)
        }
    }
    
    fun incrementViewCount(bookId: String) {
        viewModelScope.launch {
            repository.incrementViewCount(bookId)
        }
    }

    fun resetActionStatus() {
        _addBookStatus.value = null
        _uploadFileStatus.value = null
        _uploadImageStatus.value = null
        _deleteStatus.value = null
    }
}