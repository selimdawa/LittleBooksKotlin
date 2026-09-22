package com.flatcode.littlebooks.ui.publisher

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
import com.flatcode.littlebooks.model.User
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.loadBannerAd
import com.flatcode.littlebooks.databinding.ActivityPageStaggeredBinding
import com.flatcode.littlebooks.utils.Resource
import com.flatcode.littlebooks.utils.openActivity
import com.flatcode.littlebooks.viewmodel.ProfileViewModel
import com.flatcode.littlebooks.ui.profile.ProfileActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.MessageFormat

@AndroidEntryPoint
class ExplorePublishersActivity : AppCompatActivity() {

    private var binding: ActivityPageStaggeredBinding? = null
    private val context: Context = this@ExplorePublishersActivity
    private var adapter: PublisherAdapter? = null

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityPageStaggeredBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding!!.toolbar.item.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top + 10 // Original margin was 10sp
            }
            insets
        }

        binding!!.toolbar.nameSpace.setText(R.string.explore_publishers)
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }
        binding!!.toolbar.close.setOnClickListener { onBackPressed() }
        binding!!.adView.loadBannerAd(context, DATA.BANNER_SMART_EXPLORE_PUBLISHERS)

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

        adapter = PublisherAdapter(
            onItemClick = { item ->
                context.openActivity<ProfileActivity>(false, DATA.PROFILE_ID to item.id)
            },
            onFollowClick = { item, isFollowing ->
                viewModel.followUser(DATA.FirebaseUserUid, item.id, !isFollowing)
            }
        )
        binding!!.recyclerView.adapter = adapter

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.explorePublishers.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            binding!!.progress.visibility = View.GONE
                            val data = resource.data ?: emptyList()
                            binding!!.toolbar.number.text = MessageFormat.format("( {0} )", data.size)
                            adapter!!.submitFullList(data as List<User>)
                            if (data.isNotEmpty()) {
                                binding!!.recyclerView.visibility = View.VISIBLE
                                binding!!.emptyText.visibility = View.GONE
                            } else {
                                binding!!.recyclerView.visibility = View.GONE
                                binding!!.emptyText.visibility = View.VISIBLE
                            }
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
        viewModel.loadExplorePublishers(DATA.FirebaseUserUid)
    }
}



