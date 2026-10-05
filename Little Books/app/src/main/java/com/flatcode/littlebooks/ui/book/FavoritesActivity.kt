package com.flatcode.littlebooks.ui.book

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.databinding.ActivityFavoritesBinding
import com.flatcode.littlebooks.utils.BaseActivity
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.viewmodel.BookViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.MessageFormat

@AndroidEntryPoint
class FavoritesActivity : BaseActivity() {

    private var binding: ActivityFavoritesBinding? = null
    private val activity: Activity = this@FavoritesActivity

    private var adapter: StaggeredBookAdapter? = null
    private val viewModel: BookViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        setupUI()
        setupAdapter()
        observeViewModel()
    }

    private fun setupUI() {
        binding!!.toolbar.nameSpace.setText(R.string.favorites)

        binding!!.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding!!.toolbar.close.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (DATA.searchStatus) {
                    binding!!.toolbar.root.getChildAt(0).visibility = View.VISIBLE
                    binding!!.toolbar.root.getChildAt(1).visibility = View.GONE
                    DATA.searchStatus = false
                    binding!!.toolbar.textSearch.setText(DATA.EMPTY)
                    viewModel.setSearchQuery(DATA.EMPTY)
                } else {
                    finish()
                }
            }
        })

        binding!!.toolbar.search.setOnClickListener {
            binding!!.toolbar.root.getChildAt(0).visibility = View.GONE
            binding!!.toolbar.root.getChildAt(1).visibility = View.VISIBLE
            DATA.searchStatus = true
        }

        binding!!.toolbar.textSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s.toString())
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun setupAdapter() {
        adapter = StaggeredBookAdapter()
        binding!!.recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredFavorites.collect { data ->
                    val binding = binding ?: return@collect
                    binding.progress.visibility = View.GONE
                    adapter?.submitList(data)
                    binding.toolbar.number.text = MessageFormat.format("( {0} )", data.size)

                    if (data.isEmpty()) {
                        binding.emptyText.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    } else {
                        binding.emptyText.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadFavorites(DATA.FirebaseUserUid)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}