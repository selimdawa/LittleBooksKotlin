package com.flatcode.littlebooksadmin.ui.book

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.model.Category
import com.flatcode.littlebooksadmin.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookEditViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

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

    fun updateBook(
        bookId: String,
        title: String,
        description: String,
        categoryId: String,
        imageUri: Uri? = null,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val updates = mapOf(
                    "title" to title, "description" to description, "categoryId" to categoryId
                )
                repository.updateBook(bookId, updates)
                if (imageUri != null) {
                    repository.uploadBookImage(bookId, imageUri)
                }
                onResult(true, "Successfully updated...")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Update failed")
            }
        }
    }
}