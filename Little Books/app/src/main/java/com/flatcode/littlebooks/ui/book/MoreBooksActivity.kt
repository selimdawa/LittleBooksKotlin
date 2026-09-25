package com.flatcode.littlebooks.ui.book

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.databinding.ActivityPageLinearBinding
import com.flatcode.littlebooks.utils.BaseActivity
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.Resource
import com.flatcode.littlebooks.utils.loadBannerAd
import com.flatcode.littlebooks.utils.openActivity
import com.flatcode.littlebooks.viewmodel.BookViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MoreBooksActivity : BaseActivity() {

    private var binding: ActivityPageLinearBinding? = null
    private val context: Context = this@MoreBooksActivity
    private var adapter: LinearBookAdapter? = null
    private var type: String? = null
    private var name: String? = null
    private var isReverse: String? = null
    private var recyclerView: RecyclerView? = null

    private val viewModel: BookViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPageLinearBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        val intent = intent
        type = intent.getStringExtra(DATA.SHOW_MORE_TYPE)
        name = intent.getStringExtra(DATA.SHOW_MORE_NAME)
        isReverse = intent.getStringExtra(DATA.SHOW_MORE_BOOLEAN)

        binding!!.toolbar.nameSpace.text = name
        binding!!.toolbar.close.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding!!.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding!!.adView.loadBannerAd(context, DATA.BANNER_SMART_MORE_BOOKS)

        if (isReverse == "true") {
            recyclerView = binding!!.recyclerViewReverse
        } else if (isReverse == "false") {
            recyclerView = binding!!.recyclerView
        }
        binding!!.toolbar.search.setOnClickListener {
            binding!!.toolbar.toolbar.visibility = View.GONE
            binding!!.toolbar.toolbarSearch.visibility = View.VISIBLE
            DATA.searchStatus = true
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (DATA.searchStatus) {
                    binding!!.toolbar.toolbar.visibility = View.VISIBLE
                    binding!!.toolbar.toolbarSearch.visibility = View.GONE
                    DATA.searchStatus = false
                    binding!!.toolbar.textSearch.setText(DATA.EMPTY)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
        binding!!.toolbar.textSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s.toString())
            }

            override fun afterTextChanged(s: Editable) {}
        })

        adapter = LinearBookAdapter(isUser = false) { item ->
            context.openActivity<BookDetailsActivity>(clear = false, DATA.BOOK_ID to item.id)
        }
        recyclerView!!.adapter = adapter

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredAllBooks.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            binding!!.progress.visibility = View.GONE
                            val data = resource.data ?: emptyList()
                            binding!!.toolbar.number.text =
                                getString(R.string.count_format, data.size)
                            adapter!!.submitList(data)
                            if (data.isNotEmpty()) {
                                recyclerView!!.visibility = View.VISIBLE
                                binding!!.emptyText.visibility = View.GONE
                            } else {
                                recyclerView!!.visibility = View.GONE
                                binding!!.emptyText.visibility = View.VISIBLE
                            }
                        }

                        is Resource.Error -> {
                            binding!!.progress.visibility = View.GONE
                            recyclerView!!.visibility = View.GONE
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

    override fun onResume() {
        super.onResume()
        type?.let { viewModel.loadBooksBy(it) }
    }
}