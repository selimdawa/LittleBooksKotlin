package com.flatcode.littlebooksadmin.ui.book

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.flatcode.littlebooksadmin.ui.book.EditorsChoiceBookAdapter
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.Resource
import com.flatcode.littlebooksadmin.databinding.ActivityEditorsChoiceAddBinding
import com.flatcode.littlebooksadmin.ui.book.BooksViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.MessageFormat

@AndroidEntryPoint
class EditorsChoiceAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditorsChoiceAddBinding
    private var activity: Activity? = null
    private val context: Context = also { activity = it as Activity }
    private var adapter: EditorsChoiceBookAdapter? = null
    private var editorsChoiceId: String? = null
    private var type: String = DATA.TIMESTAMP
    private var oldBookId: String? = null
    
    private val viewModel: BooksViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditorsChoiceAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        editorsChoiceId = intent.getStringExtra(DATA.EDITORS_CHOICE_ID)
        oldBookId = intent.getStringExtra(DATA.OLD_BOOK_ID)
        val id = editorsChoiceId!!.toInt()

        initUI(id)
        observeViewModel()
        
        viewModel.loadAvailableForEditorsChoice(type)
    }

    private fun initUI(id: Int) {
        binding.toolbar.nameSpace.setText(R.string.editors_choice)
        binding.toolbar.close.setOnClickListener { onBackPressed() }
        binding.toolbar.back.setOnClickListener { onBackPressed() }

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
                } catch (e: Exception) { }
            }
            override fun afterTextChanged(s: Editable) {}
        })

        adapter = EditorsChoiceBookAdapter(activity, oldBookId, id)
        binding.recyclerView.adapter = adapter

        binding.all.setOnClickListener {
            type = DATA.TIMESTAMP
            viewModel.loadAvailableForEditorsChoice(type)
        }
        binding.mostViews.setOnClickListener {
            type = DATA.VIEWS_COUNT
            viewModel.loadAvailableForEditorsChoice(type)
        }
        binding.mostLoves.setOnClickListener {
            type = DATA.LOVES_COUNT
            viewModel.loadAvailableForEditorsChoice(type)
        }
        binding.mostDownloads.setOnClickListener {
            type = DATA.DOWNLOADS_COUNT
            viewModel.loadAvailableForEditorsChoice(type)
        }
        binding.favorites.setOnClickListener {
            viewModel.loadFavorites(DATA.FirebaseUserUid)
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
        binding.toolbar.number.text = MessageFormat.format("( {0} )", books.size)
        adapter?.submitUnfilteredList(books)

        if (books.isNotEmpty()) {
            binding.recyclerView.visibility = View.VISIBLE
            binding.emptyText.visibility = View.GONE
        } else {
            binding.recyclerView.visibility = View.GONE
            binding.emptyText.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        if (DATA.searchStatus) {
            binding.toolbar.toolbar.visibility = View.VISIBLE
            binding.toolbar.toolbarSearch.visibility = View.GONE
            DATA.searchStatus = false
            binding.toolbar.textSearch.setText(DATA.EMPTY)
        } else super.onBackPressed()
    }
}


