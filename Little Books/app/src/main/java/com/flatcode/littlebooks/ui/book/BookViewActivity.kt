package com.flatcode.littlebooks.ui.book

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.databinding.ActivityBookViewBinding
import com.flatcode.littlebooks.utils.BaseActivity
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.viewmodel.BookViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookViewActivity : BaseActivity() {

    private var binding: ActivityBookViewBinding? = null
    var context: Context = this@BookViewActivity
    var bookId: String? = null

    private val viewModel: BookViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookViewBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        val intent = intent
        bookId = intent.getStringExtra(DATA.BOOK_ID)

        binding!!.toolbar.numberPage.visibility = View.VISIBLE
        binding!!.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        observeViewModel()
        loadBookDetails()
    }

    private fun loadBookDetails() {
        bookId?.let { viewModel.loadBookDetails(it, DATA.FirebaseUserUid) }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.bookDetails
                        .map { it?.url }
                        .distinctUntilChanged()
                        .collect { url ->
                            url?.let { viewModel.loadBookFile(it) }
                        }
                }
                launch {
                    viewModel.bookFile.collect { result ->
                        result?.let {
                            binding!!.progressBar.visibility = View.GONE
                            if (it.isSuccess) {
                                binding!!.pdfView.fromBytes(it.getOrNull()).swipeHorizontal(false)
                                    .onPageChange { page: Int, pageCount: Int ->
                                        val correctPage = page + 1
                                        binding!!.toolbar.numberPage.text =
                                            getString(R.string.page_format, correctPage, pageCount)
                                    }.load()
                            } else {
                                Toast.makeText(
                                    context,
                                    it.exceptionOrNull()?.message ?: "Error loading file",
                                    Toast.LENGTH_SHORT
                                ).show()
                                viewModel.resetBookFile()
                            }
                        }
                    }
                }
            }
        }
    }
}