package com.flatcode.littlebooksadmin.ui.book

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooksadmin.model.Category
import com.flatcode.littlebooksadmin.repository.BookRepository
import com.flatcode.littlebooksadmin.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookAddViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<Resource<List<Category>>>(Resource.Loading())
    val categories: StateFlow<Resource<List<Category>>> = _categories

    private val _uploadState = MutableStateFlow<Resource<String>?>(null)
    val uploadState: StateFlow<Resource<String>?> = _uploadState

    private val _imageUploadState = MutableStateFlow<Resource<Unit>?>(null)
    val imageUploadState: StateFlow<Resource<Unit>?> = _imageUploadState

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
        uri: Uri, title: String, description: String, categoryId: String, imageUri: Uri?
    ) {
        viewModelScope.launch {
            _uploadState.value = Resource.Loading()
            val result = repository.uploadBook(uri, title, description, categoryId)
            _uploadState.value = result

            if (result is Resource.Success && imageUri != null) {
                uploadBookImage(result.data!!, imageUri)
            }
        }
    }

    private fun uploadBookImage(bookId: String, uri: Uri) {
        viewModelScope.launch {
            _imageUploadState.value = Resource.Loading()
            val result = repository.uploadBookImage(bookId, uri)
            _imageUploadState.value = result
        }
    }
}


