package com.flatcode.littlebooks.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooks.model.Book
import com.flatcode.littlebooks.model.Comment
import com.flatcode.littlebooks.model.User
import com.flatcode.littlebooks.repository.BookRepository
import com.flatcode.littlebooks.repository.CategoryRepository
import com.flatcode.littlebooks.repository.UserRepository
import com.flatcode.littlebooks.utils.DATA
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    private val repository: BookRepository,
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    private val _bookDetails = MutableStateFlow<Book?>(null)
    val bookDetails: StateFlow<Book?> = _bookDetails

    private val _publisherUser = MutableStateFlow<User?>(null)
    val publisherUser: StateFlow<User?> = _publisherUser

    private val _categoryName = MutableStateFlow("")
    val categoryName: StateFlow<String> = _categoryName

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    private val _favorites = MutableStateFlow<List<Book>>(emptyList())

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _addBookStatus = MutableStateFlow<Result<String>?>(null)
    val addBookStatus: StateFlow<Result<String>?> = _addBookStatus

    private val _uploadFileStatus = MutableStateFlow<Result<String>?>(null)
    val uploadFileStatus: StateFlow<Result<String>?> = _uploadFileStatus

    private val _uploadImageStatus = MutableStateFlow<Result<String>?>(null)
    val uploadImageStatus: StateFlow<Result<String>?> = _uploadImageStatus

    private val _booksByPublisher = MutableStateFlow<List<Book>>(emptyList())

    private val _allBooks = MutableStateFlow<List<Book>>(emptyList())

    private val _booksByCategory = MutableStateFlow<List<Book>>(emptyList())

    private val _bookFile = MutableStateFlow<Result<ByteArray>?>(null)
    val bookFile: StateFlow<Result<ByteArray>?> = _bookFile

    val filteredAllBooks: StateFlow<List<Book>> = combine(_allBooks, _searchQuery) { list, query ->
        filterList(list, query)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val filteredBooksByCategory: StateFlow<List<Book>> =
        combine(_booksByCategory, _searchQuery) { list, query ->
            filterList(list, query)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val filteredBooksByPublisher: StateFlow<List<Book>> =
        combine(_booksByPublisher, _searchQuery) { list, query ->
            filterList(list, query)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val filteredFavorites: StateFlow<List<Book>> =
        combine(_favorites, _searchQuery) { list, query ->
            filterList(list, query)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private fun filterList(list: List<Book>, query: String): List<Book> {
        return if (query.isNotEmpty()) {
            list.filter { it.title?.contains(query, ignoreCase = true) == true }
        } else {
            list
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadBookDetails(bookId: String, userId: String) {
        viewModelScope.launch {
            repository.observeBookById(bookId).collect {
                _bookDetails.value = it
            }
        }
        viewModelScope.launch {
            repository.getComments(bookId).collect {
                _comments.value = it
            }
        }
        viewModelScope.launch {
            _isFavorite.value = repository.checkFavorite(userId, bookId)
            repository.incrementViewCount(bookId)
        }
    }

    fun loadPublisherInfo(publisherId: String) {
        if (publisherId.isEmpty()) return
        viewModelScope.launch {
            userRepository.getUserInfo(publisherId).collect {
                _publisherUser.value = it
            }
        }
    }

    fun loadCategoryName(categoryId: String) {
        if (categoryId.isEmpty()) return
        viewModelScope.launch {
            categoryRepository.getCategoryName(categoryId).collect { name ->
                if (!name.isNullOrEmpty()) {
                    _categoryName.value = name
                }
            }
        }
    }

    fun loadBooksBy(orderBy: String) {
        viewModelScope.launch {
            repository.getBooks(orderBy).collect {
                _allBooks.value = it
            }
        }
    }

    fun toggleFavorite(userId: String, bookId: String, isFav: Boolean) {
        viewModelScope.launch {
            val result = repository.toggleFavorite(userId, bookId, isFav)
            if (result.isSuccess) {
                _isFavorite.value = isFav
            }
        }
    }

    fun loadFavorites(userId: String) {
        viewModelScope.launch {
            repository.getFavoriteBooks(userId).collect {
                _favorites.value = it
            }
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

            repository.addComment(bookId, commentData)
        }
    }

    fun uploadBookFile(bookUri: Uri) {
        viewModelScope.launch {
            _uploadFileStatus.value = repository.uploadBookFile(bookUri)
        }
    }

    fun uploadBookImage(imageUri: Uri) {
        viewModelScope.launch {
            _uploadImageStatus.value = repository.uploadBookImage(imageUri)
        }
    }

    fun addBook(bookData: HashMap<String, Any?>) {
        viewModelScope.launch {
            _addBookStatus.value = repository.addBook(bookData)
        }
    }

    fun updateBook(bookId: String, hashMap: HashMap<String, Any?>) {
        viewModelScope.launch {
            repository.updateBook(bookId, hashMap)
        }
    }

    fun updateBookSize(book: Book) {
        viewModelScope.launch {
            repository.updateBookInRoom(book)
        }
    }

    fun loadBooksByPublisher(publisherId: String) {
        viewModelScope.launch {
            repository.getBooksByPublisher(publisherId).collect {
                _booksByPublisher.value = it
            }
        }
    }

    fun loadBooksByCategory(categoryId: String) {
        viewModelScope.launch {
            repository.getBooksByCategory(categoryId).collect {
                _booksByCategory.value = it
            }
        }
    }

    fun loadBookFile(pdfUrl: String) {
        viewModelScope.launch {
            _bookFile.value = repository.getBookFile(pdfUrl)
        }
    }

    fun resetActionStatus() {
        _addBookStatus.value = null
        _uploadFileStatus.value = null
        _uploadImageStatus.value = null
    }
}
