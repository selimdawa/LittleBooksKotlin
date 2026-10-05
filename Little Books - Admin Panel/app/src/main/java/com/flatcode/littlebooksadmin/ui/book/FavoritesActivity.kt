package com.flatcode.littlebooksadmin.ui.book

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.flatcode.littlebooksadmin.utils.BaseActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.databinding.ActivityPageLinearSwitchBinding
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.utils.DATA
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoritesActivity : BaseActivity() {

    private lateinit var binding: ActivityPageLinearSwitchBinding
    private val context: Context = this@FavoritesActivity
    private var adapter: LinearBookAdapter? = null
    private var type: String = DATA.TIMESTAMP

    private val viewModel: BooksViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPageLinearSwitchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (DATA.searchStatus) {
                    binding.toolbar.root.getChildAt(0).visibility = View.VISIBLE
                    binding.toolbar.root.getChildAt(1).visibility = View.GONE
                    DATA.searchStatus = false
                    binding.toolbar.textSearch.setText(DATA.EMPTY)
                } else {
                    finish()
                }
            }
        })

        initUI()
        observeViewModel()
    }

    private fun initUI() {
        binding.toolbar.nameSpace.setText(R.string.favorites)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.toolbar.close.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.toolbar.search.setOnClickListener {
            binding.toolbar.root.getChildAt(0).visibility = View.GONE
            binding.toolbar.root.getChildAt(1).visibility = View.VISIBLE
            DATA.searchStatus = true
        }

        binding.toolbar.textSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                try {
                    adapter?.filter?.filter(s)
                } catch (_: Exception) {
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        adapter = LinearBookAdapter()
        binding.recyclerView.adapter = adapter

        binding.switchBar.all.setOnClickListener {
            type = DATA.TIMESTAMP
            viewModel.loadFavorites(DATA.FirebaseUserUid)
        }
        binding.switchBar.name.setOnClickListener {
            type = DATA.TITLE
            viewModel.loadFavorites(DATA.FirebaseUserUid)
        }
        binding.switchBar.mostViews.setOnClickListener {
            type = DATA.VIEWS_COUNT
            viewModel.loadFavorites(DATA.FirebaseUserUid)
        }
        binding.switchBar.mostLoves.setOnClickListener {
            type = DATA.LOVES_COUNT
            viewModel.loadFavorites(DATA.FirebaseUserUid)
        }
        binding.switchBar.mostDownloads.setOnClickListener {
            type = DATA.DOWNLOADS_COUNT
            viewModel.loadFavorites(DATA.FirebaseUserUid)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.books.collect { books ->
                    binding.progress.visibility = View.GONE
                    updateList(books)
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

    override fun onResume() {
        super.onResume()
        viewModel.loadFavorites(DATA.FirebaseUserUid)
    }
}