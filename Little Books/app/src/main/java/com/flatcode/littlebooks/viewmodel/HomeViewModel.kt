package com.flatcode.littlebooks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooks.model.Book
import com.flatcode.littlebooks.model.Category
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.repository.BookRepository
import com.flatcode.littlebooks.repository.CategoryRepository
import com.flatcode.littlebooks.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _sliderImages = MutableStateFlow<Resource<List<String>>>(Resource.Loading())
    val sliderImages: StateFlow<Resource<List<String>>> = _sliderImages

    private val _categories = MutableStateFlow<Resource<List<Category>>>(Resource.Loading())
    val categories: StateFlow<Resource<List<Category>>> = _categories

    private val _editorsChoiceBooks = MutableStateFlow<Resource<List<Book>>>(Resource.Loading())
    val editorsChoiceBooks: StateFlow<Resource<List<Book>>> = _editorsChoiceBooks

    private val _mostViewedBooks = MutableStateFlow<Resource<List<Book>>>(Resource.Loading())
    val mostViewedBooks: StateFlow<Resource<List<Book>>> = _mostViewedBooks

    private val _mostLovedBooks = MutableStateFlow<Resource<List<Book>>>(Resource.Loading())
    val mostLovedBooks: StateFlow<Resource<List<Book>>> = _mostLovedBooks

    private val _mostDownloadedBooks = MutableStateFlow<Resource<List<Book>>>(Resource.Loading())
    val mostDownloadedBooks: StateFlow<Resource<List<Book>>> = _mostDownloadedBooks

    private val _newBooks = MutableStateFlow<Resource<List<Book>>>(Resource.Loading())
    val newBooks: StateFlow<Resource<List<Book>>> = _newBooks

    init {
        fetchHomeData()
    }

    fun fetchHomeData() {
        viewModelScope.launch {
            _sliderImages.value = bookRepository.getSliderImages()
            _categories.value = categoryRepository.getCategories()
            _editorsChoiceBooks.value = bookRepository.getEditorsChoiceBooks()
            
            _mostViewedBooks.value = bookRepository.getBooksBy(DATA.VIEWS_COUNT, DATA.ORDER_MAIN)
            _mostLovedBooks.value = bookRepository.getBooksBy(DATA.LOVES_COUNT, DATA.ORDER_MAIN)
            _mostDownloadedBooks.value = bookRepository.getBooksBy(DATA.DOWNLOADS_COUNT, DATA.ORDER_MAIN)
            _newBooks.value = bookRepository.getBooksBy(DATA.TIMESTAMP, DATA.ORDER_MAIN)
        }
    }
}


