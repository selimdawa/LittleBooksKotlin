package com.flatcode.littlebooksadmin.ui.book

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.flatcode.littlebooksadmin.utils.BaseActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooksadmin.databinding.ActivityBookViewBinding
import com.flatcode.littlebooksadmin.utils.DATA
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL

@AndroidEntryPoint
class BookViewActivity : BaseActivity() {

    private lateinit var binding: ActivityBookViewBinding
    private val context: Context = this@BookViewActivity
    private var bookId: String? = null

    private val viewModel: BookDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookId = intent.getStringExtra(DATA.BOOK_ID)

        initUI()
        observeViewModel()

        bookId?.let { viewModel.loadBookDetails(it) }
    }

    private fun initUI() {
        binding.toolbar.numberPage.visibility = View.VISIBLE
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.book.collect { book ->
                    binding.progressBar.visibility = View.GONE
                    book?.url?.let { loadBookFromUrl(it) }
                }
            }
        }
    }

    private fun loadBookFromUrl(pdfUrl: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL(pdfUrl)
                val inputStream = url.openStream()
                launch(Dispatchers.Main) {
                    binding.pdfView.fromStream(inputStream)
                        .swipeHorizontal(false)
                        .onPageChange { page, pageCount ->
                            binding.toolbar.numberPage.text = "${page + 1} / $pageCount"
                        }
                        .onError {
                            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                        }
                        .onPageError { page, _ ->
                            Toast.makeText(context, "Error on page $page", Toast.LENGTH_SHORT).show()
                        }
                        .load()
                    binding.progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}