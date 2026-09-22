package com.flatcode.littlebooksadmin.ui.book

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.repository.BookRepository
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BooksViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

    private val _books = MutableStateFlow<Resource<List<Book>>>(Resource.Loading())
    val books: StateFlow<Resource<List<Book>>> = _books

    fun loadBooks(orderBy: String = DATA.TIMESTAMP, publisherId: String? = null) {
        viewModelScope.launch {
            repository.getBooks(orderBy, publisherId).collect {
                _books.value = it
            }
        }
    }

    fun loadFavorites(userId: String) {
        viewModelScope.launch {
            repository.getFavorites(userId).collect {
                _books.value = it
            }
        }
    }

    fun loadBooksByCategory(categoryId: String, orderBy: String = DATA.TIMESTAMP) {
        viewModelScope.launch {
            repository.getBooksByCategory(categoryId, orderBy).collect {
                _books.value = it
            }
        }
    }

    fun loadEditorsChoiceBooks() {
        viewModelScope.launch {
            repository.getEditorsChoiceBooks().collect {
                _books.value = it
            }
        }
    }

    fun loadAvailableForEditorsChoice(orderBy: String = DATA.TIMESTAMP) {
        viewModelScope.launch {
            repository.getBooks(orderBy).collect { resource ->
                if (resource is Resource.Success) {
                    val available = resource.data?.filter { it.editorsChoice == 0 } ?: emptyList()
                    _books.value = Resource.Success(available)
                } else {
                    _books.value = resource
                }
            }
        }
    }
}


