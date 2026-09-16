package com.flatcode.littlebooksadmin.Activity

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.data.util.Resource
import com.flatcode.littlebooksadmin.databinding.ActivityBookViewBinding
import com.flatcode.littlebooksadmin.ui.viewmodel.BookEditViewModel
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.MessageFormat

@AndroidEntryPoint
class BookViewActivity : AppCompatActivity() {

    private var binding: ActivityBookViewBinding? = null
    private val context: Context = this@BookViewActivity
    private var bookId: String? = null
    
    private val viewModel: BookEditViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBookViewBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding!!.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bookId = intent.getStringExtra(DATA.BOOK_ID)

        initUI()
        observeViewModel()
        
        bookId?.let { viewModel.loadBook(it) }
    }

    private fun initUI() {
        binding!!.toolbar.numberPage.visibility = View.VISIBLE
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.book.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            binding!!.progressBar.visibility = View.VISIBLE
                        }
                        is Resource.Success -> {
                            resource.data?.url?.let { loadBookFromUrl(it) }
                        }
                        is Resource.Error -> {
                            binding!!.progressBar.visibility = View.GONE
                            Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun loadBookFromUrl(pdfUrl: String) {
        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        reference.getBytes(DATA.MAX_BYTES_PDF.toLong()).addOnSuccessListener { bytes: ByteArray? ->
            binding!!.progressBar.visibility = View.GONE
            binding!!.pdfView.fromBytes(bytes).swipeHorizontal(false)
                .onPageChange { page: Int, pageCount: Int ->
                    val correctPage = page + 1
                    binding!!.toolbar.numberPage.text =
                        MessageFormat.format("{0}/{1}", correctPage, pageCount)
                }.onError { t: Throwable ->
                    Toast.makeText(context, DATA.EMPTY + t.message, Toast.LENGTH_SHORT).show()
                }
                .onPageError { page: Int, t: Throwable ->
                    Toast.makeText(
                        context, "Error on page " + page + DATA.SPACE + t.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }.load()
        }.addOnFailureListener { 
            binding!!.progressBar.visibility = View.GONE 
            Toast.makeText(context, "Failed to load PDF", Toast.LENGTH_SHORT).show()
        }
    }
}
