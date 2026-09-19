package com.flatcode.littlebooksadmin.ui.category

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
import com.flatcode.littlebooksadmin.ui.category.CategoriesAdapter
import com.flatcode.littlebooksadmin.model.Category
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.Resource
import com.flatcode.littlebooksadmin.databinding.ActivityPageStaggeredBinding
import com.flatcode.littlebooksadmin.ui.category.CategoriesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.MessageFormat

@AndroidEntryPoint
class CategoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPageStaggeredBinding
    private val context: Context = this@CategoriesActivity
    private var list: ArrayList<Category?> = arrayListOf()
    private var adapter: CategoriesAdapter? = null
    
    private val viewModel: CategoriesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPageStaggeredBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initUI()
        observeViewModel()
    }

    private fun initUI() {
        binding.toolbar.nameSpace.setText(R.string.categories)
        binding.toolbar.back.setOnClickListener { onBackPressed() }
        binding.toolbar.close.setOnClickListener { onBackPressed() }

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

        adapter = CategoriesAdapter()
        binding.recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categories.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            binding.progress.visibility = View.VISIBLE
                        }
                        is Resource.Success -> {
                            binding.progress.visibility = View.GONE
                            val categories = resource.data ?: emptyList()
                            updateList(categories)
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

    private fun updateList(categories: List<com.flatcode.littlebooksadmin.model.Category>) {
        list.clear()
        // Note: The adapter uses model.Category, I should eventually migrate it too.
        // For now, mapping back to ensure it works if types differ slightly.
        categories.forEach {
            val legacyCategory = Category(it.id, it.category, it.image, it.publisher, it.timestamp)
            list.add(legacyCategory)
        }
        
        binding.toolbar.number.text = MessageFormat.format("( {0} )", list.size)
        adapter!!.submitUnfilteredList(list.filterNotNull())
        
        if (list.isNotEmpty()) {
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

    override fun onResume() {
        super.onResume()
        viewModel.loadCategories(DATA.CATEGORY)
    }
}


