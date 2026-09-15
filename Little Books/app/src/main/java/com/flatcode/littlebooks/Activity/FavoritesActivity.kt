package com.flatcode.littlebooks.Activity

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooks.Adapter.StaggeredBookAdapter
import com.flatcode.littlebooks.Model.Book
import com.flatcode.littlebooks.Unit.DATA
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
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

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