package com.flatcode.littlebooksadmin.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.model.User
import com.flatcode.littlebooksadmin.repository.MainRepository
import com.flatcode.littlebooksadmin.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {

    private val _user = MutableStateFlow<Resource<User>>(Resource.Loading())
    val user: StateFlow<Resource<User>> = _user

    private val _stats = MutableStateFlow<Resource<MainRepository.DashboardStats>>(Resource.Loading())
    val stats: StateFlow<Resource<MainRepository.DashboardStats>> = _stats

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            _user.value = Resource.Loading()
            _stats.value = Resource.Loading()
            
            _user.value = repository.getUserInfo(DATA.FirebaseUserUid)
            _stats.value = repository.getDashboardStats()
        }
    }
}
