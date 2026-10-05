package com.flatcode.littlebooksadmin.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooksadmin.model.User
import com.flatcode.littlebooksadmin.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _stats = MutableStateFlow<UserRepository.ProfileStats?>(null)
    val stats: StateFlow<UserRepository.ProfileStats?> = _stats.asStateFlow()

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing.asStateFlow()

    fun loadProfile(profileId: String) {
        viewModelScope.launch {
            _user.value = repository.getUserById(profileId)

            repository.getProfileStats(profileId).collect {
                _stats.value = it
            }
        }

        viewModelScope.launch {
            repository.isFollowing(profileId).collect {
                _isFollowing.value = it
            }
        }
    }

    fun toggleFollow(profileId: String) {
        viewModelScope.launch {
            repository.toggleFollow(profileId, _isFollowing.value)
        }
    }

    fun updateProfile(username: String, imageUri: Uri?, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateUserProfile(username, imageUri)
                onResult(true, "Successfully updated...")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Update failed")
            }
        }
    }
}