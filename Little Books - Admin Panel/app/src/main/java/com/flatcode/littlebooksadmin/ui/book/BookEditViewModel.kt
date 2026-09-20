package com.flatcode.littlebooksadmin.ui.book

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.model.Category
import com.flatcode.littlebooksadmin.repository.BookRepository
import com.flatcode.littlebooksadmin.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookEditViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

    private val _book = MutableStateFlow<Resource<Book>>(Resource.Loading())
    val book: StateFlow<Resource<Book>> = _book

    private val _categories = MutableStateFlow<Resource<List<Category>>>(Resource.Loading())
    val categories: StateFlow<Resource<List<Category>>> = _categories

    private val _updateState = MutableStateFlow<Resource<Unit>?>(null)
    val updateState: StateFlow<Resource<Unit>?> = _updateState

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getCategories().collect {
                _categories.value = it
            }
        }
    }

    fun loadBook(bookId: String) {
        viewModelScope.launch {
            _book.value = repository.getBookById(bookId)
        }
    }

    fun updateBook(bookId: String, title: String, description: String, categoryId: String, imageUri: Uri? = null) {
        viewModelScope.launch {
            _updateState.value = Resource.Loading()
            val updates = mapOf(
                "title" to title,
                "description" to description,
                "categoryId" to categoryId
            )
            val result = repository.updateBook(bookId, updates)
            
            if (result is Resource.Success && imageUri != null) {
                val imageResult = repository.uploadBookImage(bookId, imageUri)
                _updateState.value = imageResult
            } else {
                _updateState.value = result
            }
        }
    }
}


