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
import com.flatcode.littlebooksadmin.Adapterimport.TopPublisherAdapter
import com.flatcode.littlebooksadmin.Modelimport.User
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.data.util.Resource
import com.flatcode.littlebooksadmin.databinding.ActivityPageStaggeredBinding
import com.flatcode.littlebooksadmin.ui.viewmodel.UsersViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.MessageFormat

@AndroidEntryPoint
class TopPublishersActivity : AppCompatActivity() {

    private var binding: ActivityPageStaggeredBinding? = null
    private val context: Context = this@TopPublishersActivity
    private var list: ArrayList<User?> = arrayListOf()
    private var adapter: TopPublisherAdapter? = null
    
    private val viewModel: UsersViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPageStaggeredBinding.inflate(layoutInflater)
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
        binding!!.toolbar.nameSpace.setText(R.string.top_publishers)
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

        adapter = TopPublisherAdapter(context, list, true)
        binding!!.recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.users.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            binding!!.progress.visibility = View.VISIBLE
                        }
                        is Resource.Success -> {
                            binding!!.progress.visibility = View.GONE
                            updateList(resource.data ?: emptyList())
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

    private fun updateList(users: List<com.flatcode.littlebooksadmin.data.model.User>) {
        list.clear()
        users.filter { it.booksCount >= 1 }.forEach {
            val legacyUser = User(
                it.id, it.username, it.profileImage, it.email, it.timestamp,
                it.version, it.booksCount, it.adLoad, it.adClick
            )
            list.add(legacyUser)
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
        viewModel.loadUsers(DATA.BOOKS_COUNT)
    }
}
