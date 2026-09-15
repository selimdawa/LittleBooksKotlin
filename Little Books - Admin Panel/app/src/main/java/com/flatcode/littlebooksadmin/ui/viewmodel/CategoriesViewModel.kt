package com.flatcode.littlebooksadmin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooksadmin.data.model.Category
import com.flatcode.littlebooksadmin.data.repository.CategoryRepository
import com.flatcode.littlebooksadmin.data.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val repository: CategoryRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<Resource<List<Category>>>(Resource.Loading())
    val categories: StateFlow<Resource<List<Category>>> = _categories

    init {
        loadCategories()
    }

    fun loadCategories(orderBy: String = "category") {
        viewModelScope.launch {
            repository.getCategories(orderBy).collect {
                _categories.value = it
            }
        }
    }
}
