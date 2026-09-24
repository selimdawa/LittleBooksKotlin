package com.flatcode.littlebooksadmin.ui.book

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooksadmin.Application
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.databinding.ActivityBookDetailsBinding
import com.flatcode.littlebooksadmin.databinding.DialogCommentAddBinding
import com.flatcode.littlebooksadmin.ui.profile.ProfileActivity
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.Resource
import com.flatcode.littlebooksadmin.utils.createProgressDialog
import com.flatcode.littlebooksadmin.utils.checkFavorite
import com.flatcode.littlebooksadmin.utils.checkLove
import com.flatcode.littlebooksadmin.utils.downloadBook
import com.flatcode.littlebooksadmin.utils.isFavorite
import com.flatcode.littlebooksadmin.utils.isLoves
import com.flatcode.littlebooksadmin.utils.loadCategory
import com.flatcode.littlebooksadmin.utils.loadImage
import com.flatcode.littlebooksadmin.utils.loadPdfInfo
import com.flatcode.littlebooksadmin.utils.nrLoves
import com.flatcode.littlebooksadmin.utils.openActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookDetailsBinding
    private val context: Context = this@BookDetailsActivity
    private var bookId: String? = null
    private var bookTitle: String? = null
    private var bookUrl: String? = null
    private var dialog: AlertDialog? = null
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
        dialog = createProgressDialog(getString(R.string.please_wait))

        adapter = CommentAdapter()
        binding.recyclerView.adapter = adapter

        binding.love.setOnClickListener { binding.love.checkLove(bookId) }
        binding.favorite.setOnClickListener {
            binding.favorite.checkFavorite(bookId)
        }
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.read.setOnClickListener {
            context.openActivity<BookViewActivity>(extras = arrayOf(DATA.BOOK_ID to bookId))
        }
        binding.download.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.downloadBook(
                    DATA.EMPTY + bookId, DATA.EMPTY + bookTitle, DATA.EMPTY + bookUrl
                )
            } else {
                if (ContextCompat.checkSelfPermission(
                        context, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    context.downloadBook(
                        DATA.EMPTY + bookId, DATA.EMPTY + bookTitle, DATA.EMPTY + bookUrl
                    )
                } else {
                    resultPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }
        binding.addComment.setOnClickListener {
            if (DATA.FIREBASE_USER == null) {
                Toast.makeText(context, R.string.not_logged_in, Toast.LENGTH_SHORT).show()
            } else {
                addCommentDialog()
            }
        }

        binding.love.isLoves(bookId)
        binding.loves.nrLoves(bookId)
        binding.favorite.isFavorite(bookId, DATA.FirebaseUserUid)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.book.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {}
                            is Resource.Success -> {
                                resource.data?.let { book ->
                                    bookTitle = book.title
                                    bookUrl = book.url
                                    binding.download.visibility = View.VISIBLE

                                    val date: String = Application.formatTimestamp(book.timestamp)
                                    binding.category.loadCategory(book.categoryId)
                                    binding.size.loadPdfInfo(book.url)

                                    binding.image.loadImage(
                                        isUser = false, url = book.image ?: DATA.BASIC
                                    )
                                    binding.cover.loadImage(
                                        isUser = false, url = book.image ?: DATA.BASIC
                                    )
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
                            is Resource.Loading -> {}
                            is Resource.Success -> {
                                resource.data?.let { user ->
                                    binding.publisherName.text = user.username
                                    binding.publisherImage.loadImage(
                                        isUser = true, url = user.profileImage ?: DATA.BASIC
                                    )
                                    binding.userInfo.setOnClickListener {
                                        context.openActivity<ProfileActivity>(extras = arrayOf(DATA.PROFILE_ID to user.id))
                                    }
                                }
                            }

                            is Resource.Error -> {}
                        }
                    }
                }
                launch {
                    viewModel.comments.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {}
                            is Resource.Success -> {
                                updateComments(resource.data ?: emptyList())
                            }

                            is Resource.Error -> {}
                        }
                    }
                }
                launch {
                    viewModel.addCommentState.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                dialog = createProgressDialog(
                                    getString(R.string.adding_comment)
                                )
                                dialog!!.show()
                            }

                            is Resource.Success -> {
                                dialog!!.dismiss()
                                Toast.makeText(context, R.string.comment_added, Toast.LENGTH_SHORT)
                                    .show()
                            }

                            is Resource.Error -> {
                                dialog!!.dismiss()
                                Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                            }

                            null -> {}
                        }
                    }
                }
            }
        }
    }

    private fun updateComments(comments: List<com.flatcode.littlebooksadmin.model.Comment>) {
        adapter?.submitList(comments)
        binding.textComment.visibility = if (comments.isEmpty()) View.GONE else View.VISIBLE
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
                Toast.makeText(context, R.string.enter_comment, Toast.LENGTH_SHORT).show()
            } else {
                alertDialog.dismiss()
                bookId?.let { viewModel.addComment(it, comment) }
            }
        }
    }

    private val resultPermissionLauncher =
        registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                context.downloadBook(
                    DATA.EMPTY + bookId, DATA.EMPTY + bookTitle, DATA.EMPTY + bookUrl
                )
            } else {
                Toast.makeText(context, R.string.permission_denied, Toast.LENGTH_SHORT).show()
            }
        }
}