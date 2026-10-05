package com.flatcode.littlebooksadmin.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooksadmin.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val success = repository.login(email, password)
            if (success) {
                onResult(true, "Successfully logged in...")
            } else {
                onResult(false, "Login failed")
            }
        }
    }

    fun isUserLoggedIn() = repository.isUserLoggedIn()
}