package com.flatcode.littlebooksadmin.ui.book

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooksadmin.Application
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.databinding.ActivityBookDetailsBinding
import com.flatcode.littlebooksadmin.databinding.DialogCommentAddBinding
import com.flatcode.littlebooksadmin.model.Comment
import com.flatcode.littlebooksadmin.ui.profile.ProfileActivity
import com.flatcode.littlebooksadmin.utils.BaseActivity
import com.flatcode.littlebooksadmin.utils.DATA
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
class BookDetailsActivity : BaseActivity() {

    private lateinit var binding: ActivityBookDetailsBinding
    private val context: Context = this@BookDetailsActivity
    private var bookId: String? = null
    private var bookTitle: String? = null
    private var bookUrl: String? = null
    private var adapter: CommentAdapter? = null
    private var dialog: AlertDialog? = null

    private val viewModel: BookDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookId = intent.getStringExtra(DATA.BOOK_ID)

        initUI()
        observeViewModel()

        bookId?.let { viewModel.loadBookDetails(it) }
    }

    private fun initUI() {
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.download.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                resultPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                context.downloadBook(
                    DATA.EMPTY + bookId, DATA.EMPTY + bookTitle, DATA.EMPTY + bookUrl
                )
            }
        }
        binding.read.setOnClickListener {
            context.openActivity<BookViewActivity>(
                extras = arrayOf(DATA.BOOK_ID to bookId)
            )
        }
        binding.addComment.setOnClickListener { addCommentDialog() }

        adapter = CommentAdapter()
        binding.commentsRecyclerView.adapter = adapter

        binding.love.isLoves(bookId)
        binding.loves.nrLoves(bookId)
        binding.favorite.isFavorite(bookId, DATA.FirebaseUserUid)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.book.collect { book ->
                        book?.let {
                            bookTitle = it.title
                            bookUrl = it.url
                            binding.download.visibility = View.VISIBLE

                            val date: String = Application.formatTimestamp(it.timestamp)
                            binding.category.loadCategory(it.categoryId)
                            binding.size.loadPdfInfo(it.url)

                            binding.image.loadImage(
                                isUser = false, url = it.image ?: DATA.BASIC
                            )
                            binding.cover.loadImage(
                                isUser = false, url = it.image ?: DATA.BASIC
                            )
                            binding.title.text = it.title
                            binding.description.text = it.description
                            binding.views.text = it.viewsCount.toString()
                            binding.downloads.text = it.downloadsCount.toString()
                            binding.date.text = date
                        }
                    }
                }
                launch {
                    viewModel.publisher.collect { user ->
                        user?.let {
                            binding.publisherName.text = it.username
                            binding.publisherImage.loadImage(
                                isUser = true, url = it.profileImage ?: DATA.BASIC
                            )
                            binding.userInfo.setOnClickListener {
                                context.openActivity<ProfileActivity>(extras = arrayOf(DATA.PROFILE_ID to it.id))
                            }
                        }
                    }
                }
                launch {
                    viewModel.comments.collect { comments ->
                        updateComments(comments)
                    }
                }
            }
        }
    }

    private fun updateComments(comments: List<Comment>) {
        adapter?.submitList(comments)
        binding.textComment.visibility = if (comments.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun addCommentDialog() {
        val commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this))
        val builder = AlertDialog.Builder(this)
        builder.setView(commentAddBinding.root)
        val alertDialog = builder.create()

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        alertDialog.show()

        val widthPx = (320 * resources.displayMetrics.density).toInt()
        alertDialog.window?.setLayout(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT)

        commentAddBinding.comment.requestFocus()
        commentAddBinding.comment.postDelayed({
            val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(commentAddBinding.comment, 0)
        }, 200)

        commentAddBinding.back.setOnClickListener { alertDialog.dismiss() }
        commentAddBinding.submit.setOnClickListener {
            val comment = commentAddBinding.comment.text.toString().trim()
            if (TextUtils.isEmpty(comment)) {
                Toast.makeText(context, R.string.enter_comment, Toast.LENGTH_SHORT).show()
            } else {
                alertDialog.dismiss()
                dialog = AlertDialog.Builder(context).apply {
                    setMessage(getString(R.string.adding_comment))
                }.show()
                bookId?.let { id ->
                    viewModel.addComment(id, comment) { success, message ->
                        dialog?.dismiss()
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }
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