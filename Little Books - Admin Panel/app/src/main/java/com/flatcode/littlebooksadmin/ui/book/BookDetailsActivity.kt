package com.flatcode.littlebooksadmin.ui.book

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooksadmin.ui.book.CommentAdapter
import com.flatcode.littlebooksadmin.Application
import com.flatcode.littlebooksadmin.model.Comment
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.VOID
import com.flatcode.littlebooksadmin.utils.Resource
import com.flatcode.littlebooksadmin.databinding.ActivityBookDetailsBinding
import com.flatcode.littlebooksadmin.databinding.DialogCommentAddBinding
import com.flatcode.littlebooksadmin.ui.book.BookDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookDetailsBinding
    private val context: Context = this@BookDetailsActivity
    private var bookId: String? = null
    private var bookTitle: String? = null
    private var bookUrl: String? = null
    private var dialog: ProgressDialog? = null
    private var list: ArrayList<Comment?> = arrayListOf()
    private var adapter: CommentAdapter? = null
    
    private val viewModel: BookDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBookDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bookId = intent.getStringExtra(DATA.BOOK_ID)

        initUI()
        observeViewModel()
        
        bookId?.let { viewModel.loadBookDetails(it) }
    }

    private fun initUI() {
        binding.toolbar.nameSpace.setText(R.string.details_books)
        binding.download.visibility = View.GONE
        dialog = ProgressDialog(context)
        dialog!!.setTitle("Please wait...")
        dialog!!.setCanceledOnTouchOutside(false)

        adapter = CommentAdapter(context, list)
        binding.recyclerView.adapter = adapter

        binding.love.setOnClickListener { VOID.checkLove(binding.love, bookId) }
        binding.favorite.setOnClickListener {
            VOID.checkFavorite(binding.favorite, bookId)
        }
        binding.toolbar.back.setOnClickListener { onBackPressed() }
        binding.read.setOnClickListener {
            VOID.IntentExtra(context, BookViewActivity::class.java, DATA.BOOK_ID, bookId)
        }
        binding.download.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                VOID.downloadBook(
                    context, DATA.EMPTY + bookId,
                    DATA.EMPTY + bookTitle, DATA.EMPTY + bookUrl
                )
            } else {
                resultPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        binding.addComment.setOnClickListener {
            if (DATA.FIREBASE_USER == null) {
                Toast.makeText(context, "You're not logged in...", Toast.LENGTH_SHORT).show()
            } else {
                addCommentDialog()
            }
        }
        
        VOID.isLoves(binding.love, bookId)
        VOID.nrLoves(binding.loves, bookId)
        VOID.isFavorite(binding.favorite, bookId, DATA.FirebaseUserUid)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.book.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> { }
                            is Resource.Success -> {
                                resource.data?.let { book ->
                                    bookTitle = book.title
                                    bookUrl = book.url
                                    binding.download.visibility = View.VISIBLE
                                    
                                    val date: String = Application.formatTimestamp(book.timestamp)
                                    VOID.loadCategory(book.categoryId, binding.category)
                                    VOID.loadPdfInfo(book.url, binding.size)
                                    
                                    VOID.Glide(false, context, book.image ?: DATA.BASIC, binding.image)
                                    VOID.Glide(false, context, book.image ?: DATA.BASIC, binding.cover)
                                    binding.title.text = book.title
                                    binding.description.text = book.description
                                    binding.views.text = book.viewsCount.toString()
                                    binding.downloads.text = book.downloadsCount.toString()
                                    binding.date.text = date
                                }
                            }
                            is Resource.Error -> {
                                Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                launch {
                    viewModel.publisher.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> { }
                            is Resource.Success -> {
                                resource.data?.let { user ->
                                    binding.publisherName.text = user.username
                                    VOID.Glide(true, context, user.profileImage ?: DATA.BASIC, binding.publisherImage)
                                    binding.userInfo.setOnClickListener {
                                        VOID.IntentExtra(context, ProfileActivity::class.java, DATA.PROFILE_ID, user.id)
                                    }
                                }
                            }
                            is Resource.Error -> { }
                        }
                    }
                }
                launch {
                    viewModel.comments.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> { }
                            is Resource.Success -> {
                                updateComments(resource.data ?: emptyList())
                            }
                            is Resource.Error -> { }
                        }
                    }
                }
                launch {
                    viewModel.addCommentState.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                dialog!!.setMessage("Adding comment...")
                                dialog!!.show()
                            }
                            is Resource.Success -> {
                                dialog!!.dismiss()
                                Toast.makeText(context, "Comment Added...", Toast.LENGTH_SHORT).show()
                            }
                            is Resource.Error -> {
                                dialog!!.dismiss()
                                Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                            }
                            null -> { }
                        }
                    }
                }
            }
        }
    }

    private fun updateComments(comments: List<com.flatcode.littlebooksadmin.model.Comment>) {
        list.clear()
        comments.forEach {
            val legacyComment = Comment(it.id, it.bookId, it.timestamp, it.comment, it.publisher)
            list.add(legacyComment)
        }
        adapter?.notifyDataSetChanged()
        binding.textComment.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun addCommentDialog() {
        val commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this))
        val builder = AlertDialog.Builder(this, R.style.CustomDialog)
        builder.setView(commentAddBinding.root)
        val alertDialog = builder.create()
        alertDialog.show()
        commentAddBinding.back.setOnClickListener { alertDialog.dismiss() }
        commentAddBinding.submit.setOnClickListener {
            val comment = commentAddBinding.comment.text.toString().trim()
            if (TextUtils.isEmpty(comment)) {
                Toast.makeText(context, "Enter your comment...", Toast.LENGTH_SHORT).show()
            } else {
                alertDialog.dismiss()
                bookId?.let { viewModel.addComment(it, comment) }
            }
        }
    }

    private val resultPermissionLauncher =
        registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                VOID.downloadBook(
                    context, DATA.EMPTY + bookId,
                    DATA.EMPTY + bookTitle, DATA.EMPTY + bookUrl
                )
            } else {
                Toast.makeText(context, "Permission was denied...", Toast.LENGTH_SHORT).show()
            }
        }
}


