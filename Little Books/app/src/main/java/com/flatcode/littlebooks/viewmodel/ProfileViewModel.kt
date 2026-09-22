package com.flatcode.littlebooks.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooks.model.User
import com.flatcode.littlebooks.repository.BookRepository
import com.flatcode.littlebooks.repository.UserRepository
import com.flatcode.littlebooks.utils.Resource
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

    private val _user = MutableStateFlow<Resource<User>>(Resource.Loading())
    val user: StateFlow<Resource<User>> = _user

    private val _booksCount = MutableStateFlow<Resource<Int>>(Resource.Loading())
    val booksCount: StateFlow<Resource<Int>> = _booksCount

    private val _followersCount = MutableStateFlow<Resource<Long>>(Resource.Loading())
    val followersCount: StateFlow<Resource<Long>> = _followersCount

    private val _followingCount = MutableStateFlow<Resource<Long>>(Resource.Loading())
    val followingCount: StateFlow<Resource<Long>> = _followingCount

    private val _favoritesCount = MutableStateFlow<Resource<Long>>(Resource.Loading())
    val favoritesCount: StateFlow<Resource<Long>> = _favoritesCount

    private val _isFollowing = MutableStateFlow<Resource<Boolean>>(Resource.Loading())
    val isFollowing: StateFlow<Resource<Boolean>> = _isFollowing

    private val _explorePublishersCount = MutableStateFlow<Resource<Int>>(Resource.Loading())
    val explorePublishersCount: StateFlow<Resource<Int>> = _explorePublishersCount

    private val _explorePublishers = MutableStateFlow<Resource<List<User>>>(Resource.Loading())

    private val _uploadStatus = MutableStateFlow<Resource<String>?>(null)
    val uploadStatus: StateFlow<Resource<String>?> = _uploadStatus

    val filteredExplorePublishers: StateFlow<Resource<List<User>>> =
        combine(_explorePublishers, _searchQuery) { resource, query ->
            if (resource is Resource.Success && query.isNotEmpty()) {
                val filtered = resource.data?.filter {
                    it.username?.contains(query, ignoreCase = true) == true
                }
                Resource.Success(filtered ?: emptyList())
            } else {
                resource
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, Resource.Loading())

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
            _user.value = userRepository.getUserInfo(profileId)
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
            _explorePublishers.value = Resource.Loading()
            _explorePublishers.value = userRepository.getExplorePublishers(currentUserId)
        }
    }

    fun followUser(currentUserId: String, targetUserId: String, follow: Boolean) {
        viewModelScope.launch {
            val result = userRepository.followUser(currentUserId, targetUserId, follow)
            if (result is Resource.Success) {
                _isFollowing.value = Resource.Success(follow)
            }
        }
    }

    fun updateUserInfo(userId: String, hashMap: Map<String, Any>) {
        viewModelScope.launch {
            userRepository.updateUserInfo(userId, hashMap)
            _user.value = userRepository.getUserInfo(userId)
        }
    }

    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            _uploadStatus.value = Resource.Loading()
            _uploadStatus.value = userRepository.uploadProfileImage(imageUri)
        }
    }
}