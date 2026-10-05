package com.flatcode.littlebooksadmin.ui.book

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import com.flatcode.littlebooksadmin.utils.BaseActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.databinding.ActivityEditorsChoiceBinding
import com.flatcode.littlebooksadmin.model.EditorsChoice
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditorsChoiceActivity : BaseActivity() {

    private lateinit var binding: ActivityEditorsChoiceBinding
    private val context: Context = this@EditorsChoiceActivity
    private var adapter: EditorsChoiceAdapter? = null
    private val list = mutableListOf<EditorsChoice>()
    private val editorsChoice = EditorsChoice()

    private val viewModel: BooksViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorsChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
        observeViewModel()
    }

    private fun initUI() {
        binding.toolbar.nameSpace.setText(R.string.editors_choice)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        adapter = EditorsChoiceAdapter()
        binding.recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.books.collect {
                    updateList()
                }
            }
        }
    }

    private fun updateList() {
        list.clear()
        repeat(50) {
            list.add(editorsChoice)
        }
        adapter!!.submitList(ArrayList(list))
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadEditorsChoiceBooks()
    }
}