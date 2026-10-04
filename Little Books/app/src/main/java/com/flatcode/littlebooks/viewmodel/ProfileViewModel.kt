package com.flatcode.littlebooks.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooks.model.User
import com.flatcode.littlebooks.repository.BookRepository
import com.flatcode.littlebooks.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository, private val bookRepository: BookRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _booksCount = MutableStateFlow(0)
    val booksCount: StateFlow<Int> = _booksCount

    private val _followersCount = MutableStateFlow(0L)
    val followersCount: StateFlow<Long> = _followersCount

    private val _followingCount = MutableStateFlow(0L)
    val followingCount: StateFlow<Long> = _followingCount

    private val _favoritesCount = MutableStateFlow(0L)
    val favoritesCount: StateFlow<Long> = _favoritesCount

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing

    private val _explorePublishersCount = MutableStateFlow(0)
    val explorePublishersCount: StateFlow<Int> = _explorePublishersCount

    private val _explorePublishers = MutableStateFlow<List<User>>(emptyList())

    private val _uploadStatus = MutableStateFlow<Result<String>?>(null)
    val uploadStatus: StateFlow<Result<String>?> = _uploadStatus

    val filteredExplorePublishers: StateFlow<List<User>> =
        combine(_explorePublishers, _searchQuery) { list, query ->
            if (query.isNotEmpty()) {
                list.filter { it.username?.contains(query, ignoreCase = true) == true }
            } else {
                list
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadProfileData(
        profileId: String,
        currentUserId: String,
        followTypeFollowers: String,
        followTypeFollowing: String
    ) {
        viewModelScope.launch {
            userRepository.getUserInfo(profileId).collect {
                _user.value = it
            }
        }
        viewModelScope.launch {
            _booksCount.value = bookRepository.getBooksCountByPublisher(profileId)
            _followersCount.value = userRepository.getFollowCount(profileId, followTypeFollowers)
            _followingCount.value = userRepository.getFollowCount(profileId, followTypeFollowing)
            _favoritesCount.value = userRepository.getFavoriteCount(profileId)
            _isFollowing.value = userRepository.checkFollowing(currentUserId, profileId)
            _explorePublishersCount.value = userRepository.getExplorePublishersCount(currentUserId)
        }
    }

    fun loadExplorePublishers(currentUserId: String) {
        viewModelScope.launch {
            _explorePublishers.value = userRepository.getExplorePublishers(currentUserId)
        }
    }

    fun followUser(currentUserId: String, targetUserId: String, follow: Boolean) {
        viewModelScope.launch {
            val result = userRepository.followUser(currentUserId, targetUserId, follow)
            if (result.isSuccess) {
                _isFollowing.value = follow
            }
        }
    }

    fun updateUserInfo(userId: String, hashMap: Map<String, Any>) {
        viewModelScope.launch {
            userRepository.updateUserInfo(userId, hashMap)
        }
    }

    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            _uploadStatus.value = userRepository.uploadProfileImage(imageUri)
        }
    }
}