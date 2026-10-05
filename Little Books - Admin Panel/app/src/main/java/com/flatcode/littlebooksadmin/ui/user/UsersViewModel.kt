package com.flatcode.littlebooksadmin.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooksadmin.model.User
import com.flatcode.littlebooksadmin.repository.UserRepository
import com.flatcode.littlebooksadmin.utils.DATA
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

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