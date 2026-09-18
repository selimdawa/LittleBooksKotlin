package com.flatcode.littlebooksadmin.ui.category

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooksadmin.repository.CategoryRepository
import com.flatcode.littlebooksadmin.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryAddViewModel @Inject constructor(
    private val repository: CategoryRepository
) : ViewModel() {

    private val _addState = MutableStateFlow<Resource<Unit>?>(null)
    val addState: StateFlow<Resource<Unit>?> = _addState

    fun addCategory(name: String, imageUri: Uri?, extension: String?) {
        viewModelScope.launch {
            _addState.value = Resource.Loading()
            _addState.value = repository.addCategory(name, imageUri, extension)
        }
    }
}


