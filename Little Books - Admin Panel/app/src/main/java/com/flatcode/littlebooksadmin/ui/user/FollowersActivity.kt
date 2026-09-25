package com.flatcode.littlebooksadmin.ui.user

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.flatcode.littlebooksadmin.utils.BaseActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooksadmin.model.User
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.Resource
import com.flatcode.littlebooksadmin.databinding.ActivityPageStaggeredBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FollowersActivity : BaseActivity() {

    private lateinit var binding: ActivityPageStaggeredBinding
    private val context: Context = this@FollowersActivity
    private var adapter: PublisherAdapter? = null
    
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
        binding = ActivityPageStaggeredBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
        observeViewModel()
    }


    private fun initUI() {
        binding.toolbar.nameSpace.setText(R.string.followers)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.toolbar.close.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

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
                } catch (_: Exception) { }
            }
            override fun afterTextChanged(s: Editable) {}
        })

        adapter = PublisherAdapter()
        binding.recyclerView.adapter = adapter
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
        binding.toolbar.number.text = getString(R.string.number_placeholder, users.size)
        adapter?.submitUnfilteredList(users)
        
        if (users.isNotEmpty()) {
            binding.recyclerView.visibility = View.VISIBLE
            binding.emptyText.visibility = View.GONE
        } else {
            binding.recyclerView.visibility = View.GONE
            binding.emptyText.visibility = View.VISIBLE
        }
    }



    override fun onResume() {
        super.onResume()
        viewModel.loadFollow(DATA.FirebaseUserUid, DATA.FOLLOWERS)
    }
}
