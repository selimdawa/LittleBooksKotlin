package com.flatcode.littlebooks.ui.book

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.databinding.ActivityBookDetailsBinding
import com.flatcode.littlebooks.databinding.DialogCommentAddBinding
import com.flatcode.littlebooks.model.Comment
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.Resource
import com.flatcode.littlebooks.utils.glide
import com.flatcode.littlebooks.utils.glideBlur
import com.flatcode.littlebooks.utils.loadCategory
import com.flatcode.littlebooks.utils.loadPdfInfo
import com.flatcode.littlebooks.utils.openActivity
import com.flatcode.littlebooks.viewmodel.BookViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookDetailsActivity : AppCompatActivity() {

    private var binding: ActivityBookDetailsBinding? = null
    var context: Context = this@BookDetailsActivity
    var bookId: String? = null

    private var adapterComment: CommentAdapter? = null

    private val viewModel: BookViewModel by viewModels()
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailsBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding!!.toolbar.item.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }
            insets
        }

        bookId = intent.getStringExtra(DATA.BOOK_ID)

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Please wait")
        progressDialog!!.setCanceledOnTouchOutside(false)

        binding!!.toolbar.back.setOnClickListener { onBackPressed() }
        binding!!.favorite.setOnClickListener {
            val isFavorite = binding!!.favorite.tag == "added"
            viewModel.toggleFavorite(DATA.FirebaseUserUid, bookId!!, !isFavorite)
        }
        binding!!.read.setOnClickListener {
            context.openActivity<BookViewActivity>(false, DATA.BOOK_ID to bookId)
        }
        binding!!.addComment.setOnClickListener { addCommentDialog() }

        adapterComment = CommentAdapter()
        binding!!.recyclerView.adapter = adapterComment

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
                        when (resource) {
                            is Resource.Success -> {
                                val book = resource.data
                                binding!!.toolbar.nameSpace.text = book?.title
                                binding!!.title.text = book?.title
                                binding!!.description.text = book?.description
                                binding!!.views.text = DATA.EMPTY + book?.viewsCount
                                binding!!.downloads.text = DATA.EMPTY + book?.downloadsCount
                                // binding!!.pages.text = DATA.EMPTY + book?.pagesCount // layout doesn't have pages count text view?
                                binding!!.category.loadCategory(DATA.EMPTY + book?.categoryId)
                                binding!!.image.glide(false, book?.url)
                                binding!!.cover.glideBlur(false, DATA.EMPTY + book?.url, 50)
                                binding!!.size.loadPdfInfo(DATA.EMPTY + book?.url)
                            }

                            is Resource.Error -> {
                                Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                            }

                            is Resource.Loading -> {
                                // Handle loading
                            }
                        }
                    }
                }
                launch {
                    viewModel.isFavorite.collect { resource ->
                        if (resource is Resource.Success) {
                            val isFav = resource.data == true
                            if (isFav) {
                                binding!!.favorite.setImageResource(R.drawable.ic_star_selected)
                                binding!!.favorite.tag = "added"
                            } else {
                                binding!!.favorite.setImageResource(R.drawable.ic_star_unselected)
                                binding!!.favorite.tag = "add"
                            }
                        }
                    }
                }
                launch {
                    viewModel.comments.collect { resource ->
                        if (resource is Resource.Success) {
                            adapterComment?.submitList(resource.data as List<Comment>)
                        }
                    }
                }
            }
        }
    }

    private fun addCommentDialog() {
        val commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this))
        val builder = AlertDialog.Builder(this)
        builder.setView(commentAddBinding.root)
        val alertDialog = builder.create()
        alertDialog.show()
        commentAddBinding.back.setOnClickListener { alertDialog.dismiss() }
        commentAddBinding.submit.setOnClickListener {
            val comment = commentAddBinding.comment.text.toString().trim()
            if (comment.isEmpty()) {
                Toast.makeText(context, "Enter comment...", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.addComment(bookId!!, comment, DATA.FirebaseUserUid)
                alertDialog.dismiss()
            }
        }
    }
}