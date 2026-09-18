package com.flatcode.littlebooks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooks.model.Category
import com.flatcode.littlebooks.repository.CategoryRepository
import com.flatcode.littlebooks.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: CategoryRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<Resource<List<Category>>>(Resource.Loading())
    val categories: StateFlow<Resource<List<Category>>> = _categories

    init {
        fetchCategories()
    }

    fun fetchCategories() {
        viewModelScope.launch {
            _categories.value = Resource.Loading()
            _categories.value = repository.getCategories()
        }
    }
}


