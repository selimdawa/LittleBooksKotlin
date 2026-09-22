package com.flatcode.littlebooks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooks.model.Book
import com.flatcode.littlebooks.model.User
import com.flatcode.littlebooks.repository.BookRepository
import com.flatcode.littlebooks.repository.UserRepository
import com.flatcode.littlebooks.utils.DATA
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
class FollowViewModel @Inject constructor(
    private val userRepository: UserRepository, private val bookRepository: BookRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    private val _usersList = MutableStateFlow<Resource<List<User>>>(Resource.Loading())

    private val _booksFromFollowed = MutableStateFlow<Resource<List<Book>>>(Resource.Loading())
    val booksFromFollowed: StateFlow<Resource<List<Book>>> = _booksFromFollowed

    val filteredUsersList: StateFlow<Resource<List<User>>> =
        combine(_usersList, _searchQuery) { resource, query ->
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

    fun loadFollowList(userId: String, type: String) {
        viewModelScope.launch {
            _usersList.value = Resource.Loading()
            _usersList.value = userRepository.getFollowersOrFollowing(userId, type)
        }
    }

    fun loadBooksFromFollowed(userId: String, orderBy: String) {
        viewModelScope.launch {
            _booksFromFollowed.value = Resource.Loading()
            val followResult = userRepository.getFollowersOrFollowing(userId, DATA.FOLLOWING)
            if (followResult is Resource.Success) {
                val followedIds = followResult.data?.map { it.id } ?: emptyList()
                if (followedIds.isNotEmpty()) {
                    _booksFromFollowed.value =
                        bookRepository.getBooksFromFollowedPublishers(followedIds, orderBy)
                } else {
                    _booksFromFollowed.value = Resource.Success(emptyList())
                }
            } else if (followResult is Resource.Error) {
                _booksFromFollowed.value =
                    Resource.Error(followResult.message ?: "Error loading followed users")
            }
        }
    }

    fun followUser(currentUserId: String, targetUserId: String, follow: Boolean) {
        viewModelScope.launch {
            userRepository.followUser(currentUserId, targetUserId, follow)
        }
    }
}


