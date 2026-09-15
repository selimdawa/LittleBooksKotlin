package com.flatcode.littlebooksadmin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.data.model.User
import com.flatcode.littlebooksadmin.data.repository.UserRepository
import com.flatcode.littlebooksadmin.data.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _users = MutableStateFlow<Resource<List<User>>>(Resource.Loading())
    val users: StateFlow<Resource<List<User>>> = _users

    fun loadUsers(orderBy: String = DATA.TIMESTAMP) {
        viewModelScope.launch {
            repository.getUsers(orderBy).collect {
                _users.value = it
            }
        }
    }

    fun loadFollow(userId: String, type: String) {
        viewModelScope.launch {
            repository.getFollow(userId, type).collect {
                _users.value = it
            }
        }
    }
}
