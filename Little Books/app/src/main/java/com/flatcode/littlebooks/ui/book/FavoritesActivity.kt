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
    
    private var list = ArrayList<Book?>()
    private var adapter: StaggeredBookAdapter? = null
    
    private val viewModel: BookViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Since I can't find activity_favorites.xml, I'll try to guess common IDs
            // or apply to the root if appropriate. 
            // Most activities here use a toolbar or similar at the top.
            // I'll try to apply top margin to the first child if possible, 
            // but without the XML it's risky.
            // However, the code uses binding!!.back, binding!!.bar, binding!!.empty, binding!!.recyclerView.
            // I'll apply top insets to 'back' if it's at the top.
            binding!!.back.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }
            insets
        }

        adapter = StaggeredBookAdapter(context, list)
        binding!!.recyclerView.adapter = adapter

        binding!!.back.setOnClickListener { onBackPressed() }

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favorites.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            binding!!.bar.visibility = View.GONE
                            list.clear()
                            resource.data?.let { list.addAll(it) }
                            adapter?.notifyDataSetChanged()
                            if (list.isEmpty()) {
                                binding!!.empty.visibility = View.VISIBLE
                                binding!!.recyclerView.visibility = View.GONE
                            } else {
                                binding!!.empty.visibility = View.GONE
                                binding!!.recyclerView.visibility = View.VISIBLE
                            }
                        }
                        is Resource.Error -> {
                            binding!!.bar.visibility = View.GONE
                            binding!!.empty.visibility = View.VISIBLE
                            binding!!.recyclerView.visibility = View.GONE
                        }
                        is Resource.Loading -> {
                            binding!!.bar.visibility = View.VISIBLE
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



