package com.flatcode.littlebooksadmin.ui.ads

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.databinding.ActivityAdsBinding
import com.flatcode.littlebooksadmin.model.User
import com.flatcode.littlebooksadmin.utils.BaseActivity
import com.flatcode.littlebooksadmin.utils.DATA
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ADsActivity : BaseActivity() {

    private lateinit var binding: ActivityAdsBinding
    private val context: Context = this@ADsActivity
    private var list: ArrayList<User?> = arrayListOf()
    private var adapter: ADsUserAdapter? = null
    private var type: String = DATA.AD_LOAD

    private val viewModel: AdsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (DATA.searchStatus) {
                    binding.toolbar.root.getChildAt(0).visibility = View.VISIBLE
                    binding.toolbar.root.getChildAt(1).visibility = View.GONE
                    DATA.searchStatus = false
                    binding.toolbar.textSearch.setText(DATA.EMPTY)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        initUI()
        observeViewModel()
    }

    private fun initUI() {
        binding.toolbar.nameSpace.setText(R.string.users_ads)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.toolbar.close.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.toolbar.search.setOnClickListener {
            binding.toolbar.root.getChildAt(0).visibility = View.GONE
            binding.toolbar.root.getChildAt(1).visibility = View.VISIBLE
            DATA.searchStatus = true
        }

        binding.toolbar.textSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                try {
                    adapter?.filter?.filter(s)
                } catch (_: Exception) {
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        adapter = ADsUserAdapter(true)
        binding.recyclerView.adapter = adapter

        binding.name.setOnClickListener {
            type = DATA.USER_NAME
            viewModel.loadAdsUsers(type)
        }
        binding.adLoad.setOnClickListener {
            type = DATA.AD_LOAD
            viewModel.loadAdsUsers(type)
        }
        binding.adClick.setOnClickListener {
            type = DATA.AD_CLICK
            viewModel.loadAdsUsers(type)
        }
        binding.timestamp.setOnClickListener {
            type = DATA.TIMESTAMP
            viewModel.loadAdsUsers(type)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.adsUsers.collect { users ->
                    binding.progress.visibility = View.GONE
                    updateList(users)
                }
            }
        }
    }

    private fun updateList(users: List<User>) {
        list.clear()
        list.addAll(users)

        binding.toolbar.number.text = getString(R.string.number_placeholder, list.size)
        adapter?.submitUnfilteredList(list)

        if (list.isNotEmpty()) {
            binding.recyclerView.visibility = View.VISIBLE
            binding.emptyText.visibility = View.GONE
        } else {
            binding.recyclerView.visibility = View.GONE
            binding.emptyText.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadAdsUsers(type)
    }
}