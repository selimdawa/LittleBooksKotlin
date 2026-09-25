package com.flatcode.littlebooksadmin.ui.ads

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.flatcode.littlebooksadmin.utils.BaseActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.databinding.ActivityAdsInfoBinding
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.Resource
import com.flatcode.littlebooksadmin.utils.loadImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdsInfoActivity : BaseActivity() {

    private lateinit var binding: ActivityAdsInfoBinding
    private val context: Context = this@AdsInfoActivity
    private var adapter: ADsInfoAdapter? = null
    private var profileId: String? = null

    private val viewModel: AdsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdsInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profileId = intent.getStringExtra(DATA.PROFILE_ID)

        initUI()
        observeViewModel()

        profileId?.let { viewModel.loadUserAds(it) }
    }

    private fun initUI() {
        binding.toolbar.nameSpace.setText(R.string.info_ads)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        adapter = ADsInfoAdapter()
        binding.recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.userInfo.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {}
                            is Resource.Success -> {
                                resource.data?.let { user ->
                                    binding.username.text = user.username
                                    binding.profileImage.loadImage(
                                        isUser = true, url = user.profileImage ?: DATA.BASIC
                                    )
                                }
                            }

                            is Resource.Error -> {
                                Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                launch {
                    viewModel.userAds.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                binding.progress.visibility = View.VISIBLE
                            }

                            is Resource.Success -> {
                                binding.progress.visibility = View.GONE
                                val ads = resource.data ?: emptyList()
                                adapter?.submitUnfilteredList(ads)
                                if (ads.isNotEmpty()) {
                                    binding.recyclerView.visibility = View.VISIBLE
                                    binding.emptyText.visibility = View.GONE
                                } else {
                                    binding.recyclerView.visibility = View.GONE
                                    binding.emptyText.visibility = View.VISIBLE
                                }
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
    }
}