package com.flatcode.littlebooks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooks.model.Book
import com.flatcode.littlebooks.model.User
import com.flatcode.littlebooks.repository.BookRepository
import com.flatcode.littlebooks.repository.UserRepository
import com.flatcode.littlebooks.utils.DATA
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

    private val _usersList = MutableStateFlow<List<User>>(emptyList())

    private val _booksFromFollowed = MutableStateFlow<List<Book>>(emptyList())
    private val _sortType = MutableStateFlow(DATA.TIMESTAMP)

    val booksFromFollowed: StateFlow<List<Book>> =
        combine(_booksFromFollowed, _sortType) { list, sortType ->
            when (sortType) {
                DATA.TITLE -> list.sortedBy { it.title?.lowercase() ?: "" }
                DATA.VIEWS_COUNT -> list.sortedByDescending { it.viewsCount }
                DATA.LOVES_COUNT -> list.sortedByDescending { it.lovesCount }
                DATA.DOWNLOADS_COUNT -> list.sortedByDescending { it.downloadsCount }
                DATA.TIMESTAMP -> list.sortedByDescending { it.timestamp }
                else -> list.sortedByDescending { it.timestamp }
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSortType(type: String) {
        _sortType.value = type
    }

    val filteredUsersList: StateFlow<List<User>> =
        combine(_usersList, _searchQuery) { list, query ->
            if (query.isNotEmpty()) {
                list.filter { it.username?.contains(query, ignoreCase = true) == true }
            } else {
                list
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadFollowList(userId: String, type: String) {
        viewModelScope.launch {
            _usersList.value = userRepository.getFollowersOrFollowing(userId, type)
        }
    }

    fun loadBooksFromFollowed(userId: String, orderBy: String) {
        viewModelScope.launch {
            val users = userRepository.getFollowersOrFollowing(userId, DATA.FOLLOWING)
            val followedIds = users.map { it.id }
            if (followedIds.isNotEmpty()) {
                _booksFromFollowed.value =
                    bookRepository.getBooksFromFollowedPublishers(followedIds, orderBy)
            } else {
                _booksFromFollowed.value = emptyList()
            }
        }
    }

    fun followUser(currentUserId: String, targetUserId: String, follow: Boolean) {
        viewModelScope.launch {
            userRepository.followUser(currentUserId, targetUserId, follow)
        }
    }
}