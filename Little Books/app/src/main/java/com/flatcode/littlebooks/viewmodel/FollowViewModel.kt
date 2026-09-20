package com.flatcode.littlebooks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooks.model.Book
import com.flatcode.littlebooks.model.User
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.repository.BookRepository
import com.flatcode.littlebooks.repository.UserRepository
import com.flatcode.littlebooks.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FollowViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _usersList = MutableStateFlow<Resource<List<User>>>(Resource.Loading())
    val usersList: StateFlow<Resource<List<User>>> = _usersList

    private val _booksFromFollowed = MutableStateFlow<Resource<List<Book>>>(Resource.Loading())
    val booksFromFollowed: StateFlow<Resource<List<Book>>> = _booksFromFollowed

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
                val followedIds = followResult.data?.mapNotNull { it.id } ?: emptyList()
                if (followedIds.isNotEmpty()) {
                    _booksFromFollowed.value = bookRepository.getBooksFromFollowedPublishers(followedIds, orderBy)
                } else {
                    _booksFromFollowed.value = Resource.Success(emptyList())
                }
            } else if (followResult is Resource.Error) {
                _booksFromFollowed.value = Resource.Error(followResult.message ?: "Error loading followed users")
            }
        }
    }

    fun followUser(currentUserId: String, targetUserId: String, follow: Boolean) {
        viewModelScope.launch {
            userRepository.followUser(currentUserId, targetUserId, follow)
        }
    }
}


