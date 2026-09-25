package com.flatcode.littlebooksadmin.ui.profile

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.flatcode.littlebooksadmin.utils.BaseActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.databinding.ActivityPageStaggeredSwitchBinding
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.ui.book.BooksViewModel
import com.flatcode.littlebooksadmin.ui.book.StaggeredBookAdapter
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileInfoActivity : BaseActivity() {

    private lateinit var binding: ActivityPageStaggeredSwitchBinding
    private val context: Context = this@ProfileInfoActivity
    private var adapter: StaggeredBookAdapter? = null
    private var type: String = DATA.TIMESTAMP
    private var profileId: String? = null

    private val viewModel: BooksViewModel by viewModels()

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (DATA.searchStatus) {
                binding.toolbar.toolbar.visibility = View.VISIBLE
                binding.toolbar.toolbarSearch.visibility = View.GONE
                DATA.searchStatus = false
                binding.toolbar.textSearch.setText(DATA.EMPTY)
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding = ActivityPageStaggeredSwitchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profileId = intent.getStringExtra(DATA.PROFILE_ID)

        initUI()
        observeViewModel()

        profileId?.let { viewModel.loadBooks(type, it) }
    }


    private fun initUI() {
        binding.toolbar.nameSpace.setText(R.string.publishers_books)
        binding.toolbar.close.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.toolbar.search.setOnClickListener {
            binding.toolbar.toolbar.visibility = View.GONE
            binding.toolbar.toolbarSearch.visibility = View.VISIBLE
            DATA.searchStatus = true
        }

        binding.toolbar.textSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                try {
                    adapter!!.filter.filter(s)
                } catch (_: Exception) {
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        adapter = StaggeredBookAdapter()
        binding.recyclerView.adapter = adapter

        binding.switchBar.all.setOnClickListener {
            type = DATA.TIMESTAMP
            profileId?.let { viewModel.loadBooks(type, it) }
        }
        binding.switchBar.name.setOnClickListener {
            type = DATA.TITLE
            profileId?.let { viewModel.loadBooks(type, it) }
        }
        binding.switchBar.mostViews.setOnClickListener {
            type = DATA.VIEWS_COUNT
            profileId?.let { viewModel.loadBooks(type, it) }
        }
        binding.switchBar.mostLoves.setOnClickListener {
            type = DATA.LOVES_COUNT
            profileId?.let { viewModel.loadBooks(type, it) }
        }
        binding.switchBar.mostDownloads.setOnClickListener {
            type = DATA.DOWNLOADS_COUNT
            profileId?.let { viewModel.loadBooks(type, it) }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.books.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            binding.progress.visibility = View.VISIBLE
                        }

                        is Resource.Success -> {
                            binding.progress.visibility = View.GONE
                            updateList(resource.data ?: emptyList())
                        }

                        is Resource.Error -> {
                            binding.progress.visibility = View.GONE
                            Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun updateList(books: List<Book>) {
        binding.toolbar.number.text = getString(R.string.number_placeholder, books.size)
        adapter?.submitUnfilteredList(books)

        if (books.isNotEmpty()) {
            binding.recyclerView.visibility = View.VISIBLE
            binding.emptyText.visibility = View.GONE
        } else {
            binding.recyclerView.visibility = View.GONE
            binding.emptyText.visibility = View.VISIBLE
        }
    }
}