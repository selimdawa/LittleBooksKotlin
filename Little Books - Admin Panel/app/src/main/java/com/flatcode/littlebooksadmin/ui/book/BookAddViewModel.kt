package com.flatcode.littlebooksadmin.ui.book

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooksadmin.model.Category
import com.flatcode.littlebooksadmin.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookAddViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

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

    fun uploadBook(
        uri: Uri,
        title: String,
        description: String,
        categoryId: String,
        imageUri: Uri?,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val bookId = repository.uploadBook(uri, title, description, categoryId)
                if (imageUri != null) {
                    repository.uploadBookImage(bookId, imageUri)
                }
                onResult(true, "Successfully uploaded...")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Upload failed")
            }
        }
    }
}