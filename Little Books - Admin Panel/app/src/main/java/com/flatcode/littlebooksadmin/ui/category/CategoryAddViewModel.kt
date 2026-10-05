package com.flatcode.littlebooksadmin.ui.category

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooksadmin.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryAddViewModel @Inject constructor(
    private val repository: CategoryRepository
) : ViewModel() {

    fun addCategory(name: String, imageUri: Uri?, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.addCategory(name, imageUri)
                onResult(true, "Successfully added...")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to add category")
            }
        }
    }

    fun updateCategory(categoryId: String, name: String, imageUri: Uri?, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateCategory(categoryId, name, imageUri)
                onResult(true, "Successfully updated...")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to update category")
            }
        }
    }
}