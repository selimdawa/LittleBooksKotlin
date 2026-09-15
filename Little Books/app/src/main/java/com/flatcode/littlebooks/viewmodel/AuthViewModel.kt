package com.flatcode.littlebooks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooks.repository.AuthRepository
import com.flatcode.littlebooks.utils.Resource
import com.google.firebase.auth.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _loginStatus = MutableStateFlow<Resource<AuthResult>?>(null)
    val loginStatus: StateFlow<Resource<AuthResult>?> = _loginStatus

    private val _registerStatus = MutableStateFlow<Resource<Unit>?>(null)
    val registerStatus: StateFlow<Resource<Unit>?> = _registerStatus

    private val _forgetPasswordStatus = MutableStateFlow<Resource<Unit>?>(null)
    val forgetPasswordStatus: StateFlow<Resource<Unit>?> = _forgetPasswordStatus

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginStatus.value = Resource.Loading()
            _loginStatus.value = repository.login(email, password)
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _registerStatus.value = Resource.Loading()
            _registerStatus.value = repository.register(name, email, password)
        }
    }

    fun forgetPassword(email: String) {
        viewModelScope.launch {
            _forgetPasswordStatus.value = Resource.Loading()
            _forgetPasswordStatus.value = repository.forgetPassword(email)
        }
    }
    
    fun logout() {
        repository.logout()
    }
    
    fun resetStatus() {
        _loginStatus.value = null
        _registerStatus.value = null
        _forgetPasswordStatus.value = null
    }

    fun getCurrentUser() = repository.getCurrentUser()
}