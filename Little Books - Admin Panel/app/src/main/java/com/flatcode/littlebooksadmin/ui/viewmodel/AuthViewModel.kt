package com.flatcode.littlebooksadmin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooksadmin.data.repository.AuthRepository
import com.flatcode.littlebooksadmin.data.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<Unit>?>(null)
    val loginState: StateFlow<Resource<Unit>?> = _loginState

    private val _resetPasswordState = MutableStateFlow<Resource<Unit>?>(null)
    val resetPasswordState: StateFlow<Resource<Unit>?> = _resetPasswordState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            _loginState.value = repository.login(email, password)
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _resetPasswordState.value = Resource.Loading()
            _resetPasswordState.value = repository.resetPassword(email)
        }
    }

    fun isUserLoggedIn() = repository.isUserLoggedIn()
}
