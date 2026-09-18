package com.flatcode.littlebooks.ui.profile

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.flatcode.littlebooks.ui.publisher.PublisherAdapter
import com.flatcode.littlebooks.model.User
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.VOID
import com.flatcode.littlebooks.databinding.ActivityPageStaggeredBinding
import com.flatcode.littlebooks.utils.Resource
import com.flatcode.littlebooks.viewmodel.FollowViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.MessageFormat

@AndroidEntryPoint
class FollowersActivity : AppCompatActivity() {

    private var binding: ActivityPageStaggeredBinding? = null
    private val context: Context = this@FollowersActivity
    private var list = ArrayList<User?>()
    private var adapter: PublisherAdapter? = null

    private val viewModel: FollowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityPageStaggeredBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding!!.toolbar.item.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top + 10 // Original margin 10sp
            }
            insets
        }

        binding!!.toolbar.nameSpace.setText(R.string.followers)
        binding!!.toolbar.close.setOnClickListener { onBackPressed() }
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }
        VOID.BannerAd(context, binding!!.adView, DATA.BANNER_SMART_FOLLOWERS)

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
                } catch (e: Exception) {
                    //None
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        adapter = PublisherAdapter(context, list)
        binding!!.recyclerView.adapter = adapter

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.usersList.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            binding!!.progress.visibility = View.GONE
                            list.clear()
                            resource.data?.let { list.addAll(it) }
                            binding!!.toolbar.number.text = MessageFormat.format("( {0} )", list.size)
                            if (list.isNotEmpty()) {
                                binding!!.recyclerView.visibility = View.VISIBLE
                                binding!!.emptyText.visibility = View.GONE
                            } else {
                                binding!!.recyclerView.visibility = View.GONE
                                binding!!.emptyText.visibility = View.VISIBLE
                            }
                            adapter!!.notifyDataSetChanged()
                        }
                        is Resource.Error -> {
                            binding!!.progress.visibility = View.GONE
                            binding!!.recyclerView.visibility = View.GONE
                            binding!!.emptyText.visibility = View.VISIBLE
                        }
                        is Resource.Loading -> {
                            binding!!.progress.visibility = View.VISIBLE
                        }
                    }
                }
            }
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
        viewModel.loadFollowList(DATA.FirebaseUserUid, DATA.FOLLOWERS)
    }
}



