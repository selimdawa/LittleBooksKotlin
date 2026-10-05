package com.flatcode.littlebooks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooks.model.Book
import com.flatcode.littlebooks.model.Category
import com.flatcode.littlebooks.repository.BookRepository
import com.flatcode.littlebooks.repository.CategoryRepository
import com.flatcode.littlebooks.utils.DATA
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookRepository: BookRepository, categoryRepository: CategoryRepository
) : ViewModel() {

    private val _sliderImages = MutableStateFlow<List<String>>(emptyList())
    val sliderImages: StateFlow<List<String>> = _sliderImages

    val categories: StateFlow<List<Category>> = categoryRepository.getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val editorsChoiceBooks: StateFlow<List<Book>> = bookRepository.getEditorsChoiceBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostViewedBooks: StateFlow<List<Book>> =
        bookRepository.getBooks(DATA.VIEWS_COUNT, DATA.ORDER_MAIN)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostLovedBooks: StateFlow<List<Book>> =
        bookRepository.getBooks(DATA.LOVES_COUNT, DATA.ORDER_MAIN)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostDownloadedBooks: StateFlow<List<Book>> =
        bookRepository.getBooks(DATA.DOWNLOADS_COUNT, DATA.ORDER_MAIN)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val newBooks: StateFlow<List<Book>> = bookRepository.getBooks(DATA.TIMESTAMP, DATA.ORDER_MAIN)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        fetchSliderImages()
    }

    private fun fetchSliderImages() {
        viewModelScope.launch {
            bookRepository.getSliderImages().collect { list ->
                if (list.isNotEmpty()) {
                    _sliderImages.value = list
                } else {
                    val fallbackImages = editorsChoiceBooks.value.mapNotNull { it.image }
                        .filter { it.isNotEmpty() && it != DATA.BASIC }
                    _sliderImages.value = fallbackImages
                }
            }
        }
    }
}