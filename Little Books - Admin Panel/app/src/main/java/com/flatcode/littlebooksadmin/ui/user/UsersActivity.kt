package com.flatcode.littlebooksadmin.ui.user

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.databinding.ActivityUsersBinding
import com.flatcode.littlebooksadmin.model.User
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUsersBinding
    private val context: Context = this@UsersActivity
    private var adapter: PublisherAdapter? = null
    private var filterType: String = DATA.ALL

    private val viewModel: UsersViewModel by viewModels()

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (DATA.searchStatus) {
                binding.toolbar.toolbar.visibility = View.VISIBLE
                binding.toolbar.toolbarSearch.visibility = View.GONE
                DATA.searchStatus = false
                binding.toolbar.textSearch.setText(DATA.EMPTY)
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        enableEdgeToEdge()
        binding = ActivityUsersBinding.inflate(layoutInflater)
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
        binding.toolbar.nameSpace.setText(R.string.users)
        binding.toolbar.close.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

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
                } catch (_: Exception) {
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        adapter = PublisherAdapter()
        binding.recyclerView.adapter = adapter

        binding.all.setOnClickListener {
            filterType = DATA.ALL
            viewModel.loadUsers(DATA.USER_NAME)
        }
        binding.users.setOnClickListener {
            filterType = DATA.USER
            viewModel.loadUsers(DATA.USER_NAME)
        }
        binding.publishers.setOnClickListener {
            filterType = DATA.PUBLISHER
            viewModel.loadUsers(DATA.USER_NAME)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.users.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            binding.progress.visibility = View.VISIBLE
                        }

                        is Resource.Success -> {
                            binding.progress.visibility = View.GONE
                            val users = resource.data ?: emptyList()
                            updateList(users)
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

    private fun updateList(users: List<User>) {
        val filteredUsers = when (filterType) {
            DATA.ALL -> users
            DATA.USER -> users.filter { it.booksCount <= 0 }
            DATA.PUBLISHER -> users.filter { it.booksCount >= 1 }
            else -> users
        }

        binding.toolbar.number.text = getString(R.string.number_placeholder_no_parentheses, filteredUsers.size)
        adapter?.submitUnfilteredList(filteredUsers)

        if (filteredUsers.isNotEmpty()) {
            binding.recyclerView.visibility = View.VISIBLE
            binding.emptyText.visibility = View.GONE
        } else {
            binding.recyclerView.visibility = View.GONE
            binding.emptyText.visibility = View.VISIBLE
        }
    }


    override fun onResume() {
        super.onResume()
        viewModel.loadUsers(DATA.USER_NAME)
    }
}


