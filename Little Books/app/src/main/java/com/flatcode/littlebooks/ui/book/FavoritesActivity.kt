package com.flatcode.littlebooks.ui.book

import android.content.Context
import android.os.Bundle
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
import androidx.viewbinding.ViewBinding
import com.flatcode.littlebooks.model.Book
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.databinding.ActivityFavoritesBinding
import com.flatcode.littlebooks.utils.Resource
import com.flatcode.littlebooks.viewmodel.BookViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoritesActivity : AppCompatActivity() {

    private var binding: ActivityFavoritesBinding? = null
    var context: Context = this@FavoritesActivity
    
    private var adapter: StaggeredBookAdapter? = null
    
    private val viewModel: BookViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val binding = ActivityFavoritesBinding.inflate(layoutInflater)
        this.binding = binding
        val view = (binding as ViewBinding).root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.back.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }
            insets
        }

        adapter = StaggeredBookAdapter()
        binding.recyclerView.adapter = adapter

        binding.back.setOnClickListener { onBackPressed() }

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favorites.collect { resource ->
                    val binding = binding ?: return@collect
                    when (resource) {
                        is Resource.Success -> {
                            binding.bar.visibility = View.GONE
                            val data = resource.data ?: emptyList()
                            adapter?.submitList(data)
                            if (data.isEmpty()) {
                                binding.empty.visibility = View.VISIBLE
                                binding.recyclerView.visibility = View.GONE
                            } else {
                                binding.empty.visibility = View.GONE
                                binding.recyclerView.visibility = View.VISIBLE
                            }
                        }
                        is Resource.Error -> {
                            binding.bar.visibility = View.GONE
                            binding.empty.visibility = View.VISIBLE
                            binding.recyclerView.visibility = View.GONE
                        }
                        is Resource.Loading -> {
                            binding.bar.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadFavorites(DATA.FirebaseUserUid)
    }
}
