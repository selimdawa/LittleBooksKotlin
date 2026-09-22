package com.flatcode.littlebooksadmin.ui.book

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.model.Comment
import com.flatcode.littlebooksadmin.model.User
import com.flatcode.littlebooksadmin.repository.BookRepository
import com.flatcode.littlebooksadmin.repository.UserRepository
import com.flatcode.littlebooksadmin.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookDetailsViewModel @Inject constructor(
    private val bookRepository: BookRepository, private val userRepository: UserRepository
) : ViewModel() {

    private val _book = MutableStateFlow<Resource<Book>>(Resource.Loading())
    val book: StateFlow<Resource<Book>> = _book

    private val _comments = MutableStateFlow<Resource<List<Comment>>>(Resource.Loading())
    val comments: StateFlow<Resource<List<Comment>>> = _comments

    private val _publisher = MutableStateFlow<Resource<User>>(Resource.Loading())
    val publisher: StateFlow<Resource<User>> = _publisher

    private val _addCommentState = MutableStateFlow<Resource<Unit>?>(null)
    val addCommentState: StateFlow<Resource<Unit>?> = _addCommentState

    fun loadBookDetails(bookId: String) {
        viewModelScope.launch {
            bookRepository.incrementViews(bookId)

            val result = bookRepository.getBookById(bookId)
            _book.value = result

            if (result is Resource.Success) {
                result.data?.publisher?.let { loadPublisher(it) }
            }

            bookRepository.getComments(bookId).collect {
                _comments.value = it
            }
        }
    }

    private fun loadPublisher(userId: String) {
        viewModelScope.launch {
            _publisher.value = userRepository.getUserById(userId)
        }
    }

    fun addComment(bookId: String, text: String) {
        viewModelScope.launch {
            _addCommentState.value = Resource.Loading()
            _addCommentState.value = bookRepository.addComment(bookId, text)
        }
    }
}


