package com.flatcode.littlebooks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooks.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _loginStatus = MutableStateFlow<Result<Unit>?>(null)
    val loginStatus: StateFlow<Result<Unit>?> = _loginStatus

    private val _registerStatus = MutableStateFlow<Result<Unit>?>(null)
    val registerStatus: StateFlow<Result<Unit>?> = _registerStatus

    private val _forgetPasswordStatus = MutableStateFlow<Result<Unit>?>(null)
    val forgetPasswordStatus: StateFlow<Result<Unit>?> = _forgetPasswordStatus

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginStatus.value = repository.login(email, password)
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _registerStatus.value = repository.register(name, email, password)
        }
    }

    fun forgetPassword(email: String) {
        viewModelScope.launch {
            _forgetPasswordStatus.value = repository.forgetPassword(email)
        }
    }

    fun resetStatus() {
        _loginStatus.value = null
        _registerStatus.value = null
        _forgetPasswordStatus.value = null
    }

    fun getCurrentUser() = repository.getCurrentUser()
}
