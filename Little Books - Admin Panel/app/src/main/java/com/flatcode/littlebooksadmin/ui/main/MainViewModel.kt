package com.flatcode.littlebooksadmin.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooksadmin.model.User
import com.flatcode.littlebooksadmin.repository.MainRepository
import com.flatcode.littlebooksadmin.utils.DATA
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _stats = MutableStateFlow<MainRepository.DashboardStats?>(null)
    val stats: StateFlow<MainRepository.DashboardStats?> = _stats.asStateFlow()

    init {
        fetchData()
    }

    fun fetchData(forceLoading: Boolean = false) {
        viewModelScope.launch {
            _user.value = repository.getUserInfo(DATA.FirebaseUserUid)
            _stats.value = repository.getDashboardStats()
        }
    }
}