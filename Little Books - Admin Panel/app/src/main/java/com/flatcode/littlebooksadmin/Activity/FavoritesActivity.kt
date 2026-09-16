package com.flatcode.littlebooksadmin.Activity

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
import com.flatcode.littlebooksadmin.Adapter.StaggeredBookAdapter
import com.flatcode.littlebooksadmin.Modelimport.Book
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.data.util.Resource
import com.flatcode.littlebooksadmin.databinding.ActivityPageStaggeredSwitchBinding
import com.flatcode.littlebooksadmin.ui.viewmodel.BooksViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.MessageFormat

@AndroidEntryPoint
class FavoritesActivity : AppCompatActivity() {

    private var binding: ActivityPageStaggeredSwitchBinding? = null
    private val context: Context = this@FavoritesActivity
    private var list: ArrayList<Book?> = arrayListOf()
    private var adapter: StaggeredBookAdapter? = null
    private var type: String = DATA.TIMESTAMP
    
    private val viewModel: BooksViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPageStaggeredSwitchBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding!!.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initUI()
        observeViewModel()
    }

    private fun initUI() {
        binding!!.toolbar.nameSpace.setText(R.string.favorites)
        binding!!.toolbar.close.setOnClickListener { onBackPressed() }
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }

        binding!!.toolbar.search.setOnClickListener {
            binding!!.toolbar.toolbar.visibility = View.GONE
            binding!!.toolbar.toolbarSearch.visibility = View.VISIBLE
            DATA.searchStatus = true
        }
        
        binding!!.toolbar.textSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                try {
                    adapter!!.filter.filter(s)
                } catch (e: Exception) { }
            }
            override fun afterTextChanged(s: Editable) {}
        })

        adapter = StaggeredBookAdapter(context, list)
        binding!!.recyclerView.adapter = adapter

        binding!!.switchBar.all.setOnClickListener {
            type = DATA.TIMESTAMP
            viewModel.loadFavorites(DATA.FirebaseUserUid)
        }
        binding!!.switchBar.name.setOnClickListener {
            type = DATA.TITLE
            viewModel.loadFavorites(DATA.FirebaseUserUid)
        }
        binding!!.switchBar.mostViews.setOnClickListener {
            type = DATA.VIEWS_COUNT
            viewModel.loadFavorites(DATA.FirebaseUserUid)
        }
        binding!!.switchBar.mostLoves.setOnClickListener {
            type = DATA.LOVES_COUNT
            viewModel.loadFavorites(DATA.FirebaseUserUid)
        }
        binding!!.switchBar.mostDownloads.setOnClickListener {
            type = DATA.DOWNLOADS_COUNT
            viewModel.loadFavorites(DATA.FirebaseUserUid)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.books.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            binding!!.progress.visibility = View.VISIBLE
                        }
                        is Resource.Success -> {
                            binding!!.progress.visibility = View.GONE
                            val books = resource.data ?: emptyList()
                            updateList(books)
                        }
                        is Resource.Error -> {
                            binding!!.progress.visibility = View.GONE
                            Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun updateList(books: List<com.flatcode.littlebooksadmin.data.model.Book>) {
        list.clear()
        books.forEach {
            val legacyBook = Book(
                it.publisher, it.id, it.title, it.description, it.categoryId,
                it.url, it.image, it.timestamp, it.viewsCount, it.downloadsCount,
                it.lovesCount, it.editorsChoice
            )
            list.add(legacyBook)
        }
        
        binding!!.toolbar.number.text = MessageFormat.format("( {0} )", list.size)
        adapter!!.notifyDataSetChanged()
        
        if (list.isNotEmpty()) {
            binding!!.recyclerView.visibility = View.VISIBLE
            binding!!.emptyText.visibility = View.GONE
        } else {
            binding!!.recyclerView.visibility = View.GONE
            binding!!.emptyText.visibility = View.VISIBLE
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
        viewModel.loadFavorites(DATA.FirebaseUserUid)
    }
}
