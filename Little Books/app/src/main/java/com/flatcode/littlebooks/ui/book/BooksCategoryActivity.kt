package com.flatcode.littlebooks.ui.book

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooks.model.Book
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.loadBannerAd
import com.flatcode.littlebooks.databinding.ActivityPageStaggeredSwitchBinding
import com.flatcode.littlebooks.utils.Resource
import com.flatcode.littlebooks.viewmodel.BookViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.MessageFormat

@AndroidEntryPoint
class BooksCategoryActivity : AppCompatActivity() {

    private var binding: ActivityPageStaggeredSwitchBinding? = null
    private val context: Context = this@BooksCategoryActivity
    private var adapter: StaggeredBookAdapter? = null
    private var categoryId: String? = null
    private var categoryName: String? = null
    private var type: String = DATA.TIMESTAMP

    private val viewModel: BookViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityPageStaggeredSwitchBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding!!.toolbar.item.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top + 10 // Original margin was 10sp
            }
            insets
        }

        val intent = intent
        categoryId = intent.getStringExtra(DATA.CATEGORY_ID)
        categoryName = intent.getStringExtra(DATA.CATEGORY_NAME)

        binding!!.toolbar.nameSpace.text = categoryName
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }

        binding!!.adView.loadBannerAd(context, DATA.BANNER_SMART_CATEGORY_BOOKS)
        binding!!.toolbar.search.setOnClickListener {
            binding!!.toolbar.toolbar.visibility = View.GONE
            binding!!.toolbar.toolbarSearch.visibility = View.VISIBLE
            DATA.searchStatus = true
        }

        binding!!.toolbar.close.setOnClickListener { onBackPressed() }
        binding!!.toolbar.textSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                try {
                    adapter!!.filter.filter(s)
                } catch (e: Exception) {
                    //None
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        adapter = StaggeredBookAdapter()
        binding!!.recyclerView.adapter = adapter

        binding!!.switchBar.all.setOnClickListener {
            type = DATA.TIMESTAMP
            loadBooks()
        }
        binding!!.switchBar.name.setOnClickListener {
            type = DATA.TITLE
            loadBooks()
        }
        binding!!.switchBar.mostViews.setOnClickListener {
            type = DATA.VIEWS_COUNT
            loadBooks()
        }
        binding!!.switchBar.mostLoves.setOnClickListener {
            type = DATA.LOVES_COUNT
            loadBooks()
        }
        binding!!.switchBar.mostDownloads.setOnClickListener {
            type = DATA.DOWNLOADS_COUNT
            loadBooks()
        }

        observeViewModel()
    }

    private fun loadBooks() {
        categoryId?.let { viewModel.loadBooksByCategory(it, type) }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.booksByCategory.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            binding!!.progress.visibility = View.GONE
                            val data = resource.data ?: emptyList()
                            binding!!.toolbar.number.text = MessageFormat.format("( {0} )", data.size)
                            if (data.isNotEmpty()) {
                                binding!!.recyclerView.visibility = View.VISIBLE
                                binding!!.emptyText.visibility = View.GONE
                                adapter!!.submitFullList(data.reversed() as List<Book>)
                            } else {
                                binding!!.recyclerView.visibility = View.GONE
                                binding!!.emptyText.visibility = View.VISIBLE
                                adapter!!.submitFullList(emptyList())
                            }
                        }
                        is Resource.Error -> {
                            binding!!.progress.visibility = View.GONE
                            binding!!.recyclerView.visibility = View.GONE
                            binding!!.emptyText.visibility = View.VISIBLE
                        }
                        is Resource.Loading -> {
                            binding!!.progress.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (DATA.searchStatus) {
            binding!!.toolbar.toolbar.visibility = View.VISIBLE
            binding!!.toolbar.toolbarSearch.visibility = View.GONE
            DATA.searchStatus = false
            binding!!.toolbar.textSearch.setText(DATA.EMPTY)
        } else super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        loadBooks()
    }
}



