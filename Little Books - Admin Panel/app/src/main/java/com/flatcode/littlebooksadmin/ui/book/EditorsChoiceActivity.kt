package com.flatcode.littlebooksadmin.ui.book

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooksadmin.ui.book.EditorsChoiceAdapter
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.model.EditorsChoice
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.utils.Resource
import com.flatcode.littlebooksadmin.databinding.ActivityEditorsChoiceBinding
import com.flatcode.littlebooksadmin.ui.book.BooksViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditorsChoiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditorsChoiceBinding
    private val context: Context = this@EditorsChoiceActivity
    private var list: ArrayList<EditorsChoice> = arrayListOf()
    private var adapter: EditorsChoiceAdapter? = null
    private val editorsChoice = EditorsChoice()
    
    private val viewModel: BooksViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditorsChoiceBinding.inflate(layoutInflater)
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
        binding.toolbar.nameSpace.setText(R.string.editors_choice)
        binding.toolbar.back.setOnClickListener { onBackPressed() }

        adapter = EditorsChoiceAdapter(context, list)
        binding.recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.books.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> { }
                        is Resource.Success -> {
                            updateList()
                        }
                        is Resource.Error -> {
                            Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun updateList() {
        list.clear()
        for (i in 0..49) {
            list.add(editorsChoice)
        }
        adapter!!.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadEditorsChoiceBooks()
    }
}


