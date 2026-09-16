package com.flatcode.littlebooksadmin.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import com.flatcode.littlebooksadmin.Adapter.ADsInfoAdapter
import com.flatcode.littlebooksadmin.Modelimport.ADs
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.Unit.VOID
import com.flatcode.littlebooksadmin.data.util.Resource
import com.flatcode.littlebooksadmin.databinding.ActivityAdsInfoBinding
import com.flatcode.littlebooksadmin.ui.viewmodel.AdsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdsInfoActivity : AppCompatActivity() {

    private var binding: ActivityAdsInfoBinding? = null
    private val context: Context = this@AdsInfoActivity
    private var list: ArrayList<ADs?> = arrayListOf()
    private var adapter: ADsInfoAdapter? = null
    private var profileId: String? = null
    
    private val viewModel: AdsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAdsInfoBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding!!.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        profileId = intent.getStringExtra(DATA.PROFILE_ID)

        initUI()
        observeViewModel()
        
        profileId?.let { viewModel.loadUserAds(it) }
    }

    private fun initUI() {
        binding!!.toolbar.nameSpace.setText(R.string.info_ads)
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }

        adapter = ADsInfoAdapter(context, list, true)
        binding!!.recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.userInfo.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> { }
                            is Resource.Success -> {
                                resource.data?.let { user ->
                                    binding!!.username.text = user.username
                                    VOID.Glide(true, context, user.profileImage ?: DATA.BASIC, binding!!.profileImage)
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
    }

    private fun updateList(ads: List<com.flatcode.littlebooksadmin.data.model.ADs>) {
        list.clear()
        ads.forEach {
            val legacyAds = ADs(it.name, it.adsLoadedCount, it.adsClickedCount)
            list.add(legacyAds)
        }
        adapter!!.notifyDataSetChanged()
        
        if (list.isNotEmpty()) {
            binding!!.recyclerView.visibility = View.VISIBLE
            binding!!.emptyText.visibility = View.GONE
        } else {
            binding!!.recyclerView.visibility = View.GONE
            binding!!.emptyText.visibility = View.VISIBLE
        }
    }
}
