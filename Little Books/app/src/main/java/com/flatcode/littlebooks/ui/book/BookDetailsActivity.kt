package com.flatcode.littlebooks.ui.book

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.databinding.ActivityBookDetailsBinding
import com.flatcode.littlebooks.databinding.DialogCommentAddBinding
import com.flatcode.littlebooks.model.User
import com.flatcode.littlebooks.ui.profile.ProfileActivity
import com.flatcode.littlebooks.utils.BaseActivity
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.ProgressDialog
import com.flatcode.littlebooks.utils.formatTimestamp
import com.flatcode.littlebooks.utils.loadCategory
import com.flatcode.littlebooks.utils.loadImage
import com.flatcode.littlebooks.utils.loadPdfInfo
import com.flatcode.littlebooks.utils.openActivity
import com.flatcode.littlebooks.viewmodel.BookViewModel
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@AndroidEntryPoint
class BookDetailsActivity : BaseActivity() {

    private var binding: ActivityBookDetailsBinding? = null
    var context: Context = this@BookDetailsActivity
    var bookId: String? = null

    private var adapterComment: CommentAdapter? = null

    private val viewModel: BookViewModel by viewModels()
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailsBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        bookId = intent.getStringExtra(DATA.BOOK_ID)

        progressDialog = ProgressDialog(this).apply {
            setTitle("Please wait...")
            setCanceledOnTouchOutside(false)
        }

        binding!!.toolbar.back.setOnClickListener { finish() }
        binding!!.favorite.setOnClickListener {
            val isFavorite = binding!!.favorite.tag == "added"
            viewModel.toggleFavorite(DATA.FirebaseUserUid, bookId!!, !isFavorite)
        }
        binding!!.read.setOnClickListener {
            context.openActivity<BookViewActivity>(clear = false, DATA.BOOK_ID to bookId)
        }
        binding!!.addComment.setOnClickListener { addCommentDialog() }

        adapterComment = CommentAdapter { }
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
                    viewModel.bookDetails.collect { book ->
                        book?.let {
                            binding!!.toolbar.nameSpace.text = it.title
                            binding!!.title.text = it.title
                            binding!!.description.text = it.description
                            binding!!.views.text = it.viewsCount.toString()
                            binding!!.downloads.text = it.downloadsCount.toString()
                            binding!!.loves.text = it.lovesCount.toString()
                            binding!!.date.text = it.timestamp.formatTimestamp()
                            binding!!.category.loadCategory(it.categoryId ?: "")
                            binding!!.image.loadImage(false, it.image)
                            binding!!.cover.loadImage(false, it.image)
                            binding!!.size.loadPdfInfo(it.url)

                            val publisherId = it.publisher
                            if (!publisherId.isNullOrEmpty()) {
                                loadPublisherInfo(publisherId)
                            }
                        }
                    }
                }
                launch {
                    viewModel.isFavorite.collect { isFav ->
                        if (isFav) {
                            binding!!.favorite.setImageResource(R.drawable.ic_star_selected)
                            binding!!.favorite.tag = "added"
                        } else {
                            binding!!.favorite.setImageResource(R.drawable.ic_star_unselected)
                            binding!!.favorite.tag = "add"
                        }
                    }
                }
                launch {
                    viewModel.comments.collect { list ->
                        adapterComment?.submitList(list)
                    }
                }
            }
        }
    }

    private fun loadPublisherInfo(publisherId: String) {
        lifecycleScope.launch {
            val ref = FirebaseDatabase.getInstance().getReference(DATA.USERS).child(publisherId)
            try {
                val snapshot = ref.get().await()
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    binding!!.publisherName.text = it.username
                    binding!!.publisherImage.loadImage(true, it.profileImage)
                    binding!!.userInfo.setOnClickListener { _ ->
                        context.openActivity<ProfileActivity>(
                            clear = false, DATA.PROFILE_ID to it.id
                        )
                    }
                }
            } catch (_: Exception) {
            }
        }
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
            if (comment.isEmpty()) {
                Toast.makeText(context, "Enter comment...", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.addComment(bookId!!, comment, DATA.FirebaseUserUid)
                alertDialog.dismiss()
            }
        }
    }
}