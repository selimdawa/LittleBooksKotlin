package com.flatcode.littlebooks.ui.book

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.databinding.ActivityBookViewBinding
import com.flatcode.littlebooks.utils.Resource
import com.flatcode.littlebooks.viewmodel.BookViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.MessageFormat

@AndroidEntryPoint
class BookViewActivity : AppCompatActivity() {

    private var binding: ActivityBookViewBinding? = null
    var context: Context = this@BookViewActivity
    var bookId: String? = null

    private val viewModel: BookViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityBookViewBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding!!.toolbar.item.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top + 10 // Assuming same toolbar as BookAdd
            }
            insets
        }

        val intent = intent
        bookId = intent.getStringExtra(DATA.BOOK_ID)

        binding!!.toolbar.numberPage.visibility = View.VISIBLE
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }

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
                    viewModel.bookDetails.collect { resource ->
                        if (resource is Resource.Success) {
                            resource.data?.url?.let { viewModel.loadBookFile(it) }
                        }
                    }
                }
                launch {
                    viewModel.bookFile.collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                binding!!.progressBar.visibility = View.GONE
                                binding!!.pdfView.fromBytes(resource.data).swipeHorizontal(false)
                                    .onPageChange { page: Int, pageCount: Int ->
                                        val correctPage = page + 1
                                        binding!!.toolbar.numberPage.text =
                                            MessageFormat.format("{0}/{1}", correctPage, pageCount)
                                    }.onError { t: Throwable ->
                                        Toast.makeText(
                                            context, DATA.EMPTY + t.message, Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .onPageError { page: Int, t: Throwable ->
                                        Toast.makeText(
                                            context, "Error on page " + page + DATA.SPACE +
                                                    t.message, Toast.LENGTH_SHORT
                                        ).show()
                                    }.load()
                            }
                            is Resource.Error -> {
                                binding!!.progressBar.visibility = View.GONE
                                Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                            }
                            is Resource.Loading -> {
                                binding!!.progressBar.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        }
    }
}



