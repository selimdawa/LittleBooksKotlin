package com.flatcode.littlebooksadmin.ui.book

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.repository.BookRepository
import com.flatcode.littlebooksadmin.utils.DATA
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BooksViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    fun loadBooks(orderBy: String = DATA.TIMESTAMP, publisherId: String? = null) {
        viewModelScope.launch {
            repository.getBooks(orderBy, publisherId).collect { list ->
                _books.value = list.sortBooksBy(orderBy)
            }
        }
    }

    fun loadFavorites(userId: String, orderBy: String = DATA.TIMESTAMP) {
        viewModelScope.launch {
            repository.getFavorites(userId).collect { list ->
                _books.value = list.sortBooksBy(orderBy)
            }
        }
    }

    fun loadBooksByCategory(categoryId: String, orderBy: String = DATA.TIMESTAMP) {
        viewModelScope.launch {
            repository.getBooksByCategory(categoryId, orderBy).collect { list ->
                _books.value = list.sortBooksBy(orderBy)
            }
        }
    }

    fun loadEditorsChoiceBooks() {
        viewModelScope.launch {
            repository.getEditorsChoiceBooks().collect { list ->
                _books.value = list.sortBooksBy(DATA.TIMESTAMP)
            }
        }
    }

    fun loadAvailableForEditorsChoice(orderBy: String = DATA.TIMESTAMP) {
        viewModelScope.launch {
            repository.getBooks(orderBy).collect { booksList ->
                _books.value = booksList.filter { it.editorsChoice == 0 }.sortBooksBy(orderBy)
            }
        }
    }

    private fun List<Book>.sortBooksBy(orderBy: String): List<Book> {
        return when (orderBy) {
            DATA.TITLE -> sortedBy { it.title?.lowercase() ?: "" }
            DATA.VIEWS_COUNT -> sortedByDescending { it.viewsCount }
            DATA.LOVES_COUNT -> sortedByDescending { it.lovesCount }
            DATA.DOWNLOADS_COUNT -> sortedByDescending { it.downloadsCount }
            DATA.TIMESTAMP -> sortedByDescending { it.timestamp }
            else -> sortedByDescending { it.timestamp }
        }
    }
}