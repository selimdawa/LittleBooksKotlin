package com.flatcode.littlebooksadmin.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooksadmin.model.User
import com.flatcode.littlebooksadmin.repository.UserRepository
import com.flatcode.littlebooksadmin.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _user = MutableStateFlow<Resource<User>>(Resource.Loading())
    val user: StateFlow<Resource<User>> = _user

    private val _stats = MutableStateFlow<Resource<UserRepository.ProfileStats>>(Resource.Loading())
    val stats: StateFlow<Resource<UserRepository.ProfileStats>> = _stats

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing

    private val _updateState = MutableStateFlow<Resource<Unit>?>(null)
    val updateState: StateFlow<Resource<Unit>?> = _updateState

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

    fun updateProfile(username: String, imageUri: Uri?, extension: String?) {
        viewModelScope.launch {
            _updateState.value = Resource.Loading()
            _updateState.value = repository.updateUserProfile(username, imageUri, extension)
        }
    }
}


