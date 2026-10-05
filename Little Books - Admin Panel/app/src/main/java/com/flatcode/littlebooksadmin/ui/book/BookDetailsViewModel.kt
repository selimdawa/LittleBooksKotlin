package com.flatcode.littlebooksadmin.ui.book

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.model.Comment
import com.flatcode.littlebooksadmin.model.User
import com.flatcode.littlebooksadmin.repository.BookRepository
import com.flatcode.littlebooksadmin.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookDetailsViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book.asStateFlow()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _publisher = MutableStateFlow<User?>(null)
    val publisher: StateFlow<User?> = _publisher.asStateFlow()

    fun loadBookDetails(bookId: String) {
        viewModelScope.launch {
            bookRepository.incrementViews(bookId)

            val b = bookRepository.getBookById(bookId)
            _book.value = b

            b?.publisher?.let { loadPublisher(it) }

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

    fun addComment(bookId: String, text: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                bookRepository.addComment(bookId, text)
                onResult(true, "Comment added")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to add comment")
            }
        }
    }
}