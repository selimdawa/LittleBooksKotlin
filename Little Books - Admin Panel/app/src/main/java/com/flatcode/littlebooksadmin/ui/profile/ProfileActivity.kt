package com.flatcode.littlebooksadmin.ui.profile

import android.content.Context
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
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.VOID
import com.flatcode.littlebooksadmin.utils.Resource
import com.flatcode.littlebooksadmin.databinding.ActivityProfileBinding
import com.flatcode.littlebooksadmin.ui.profile.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.MessageFormat

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    private var binding: ActivityProfileBinding? = null
    private val context: Context = this@ProfileActivity
    private var profileId: String? = null
    
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding!!.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        profileId = intent.getStringExtra(DATA.PROFILE_ID)

        initUI()
        observeViewModel()
        
        profileId?.let { viewModel.loadProfile(it) }
    }

    private fun initUI() {
        binding!!.back.setOnClickListener { onBackPressed() }

        if (profileId == DATA.FirebaseUserUid) {
            binding!!.follow.visibility = View.GONE
            binding!!.editOrInfo.setImageResource(R.drawable.ic_edit_white)
            binding!!.editOrInfo.setOnClickListener { VOID.Intent1(context, ProfileEditActivity::class.java) }
        } else {
            binding!!.follow.visibility = View.VISIBLE
            binding!!.editOrInfo.setImageResource(R.drawable.ic_books)
            binding!!.editOrInfo.setOnClickListener {
                VOID.IntentExtra(context, ProfileInfoActivity::class.java, DATA.PROFILE_ID, profileId)
            }
        }

        binding!!.follow.setOnClickListener {
            profileId?.let { viewModel.toggleFollow(it) }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.user.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> { }
                            is Resource.Success -> {
                                resource.data?.let { user ->
                                    binding!!.username.text = user.username
                                    VOID.Glide(true, context, user.profileImage ?: DATA.BASIC, binding!!.profile)
                                }
                            }
                            is Resource.Error -> {
                                Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                launch {
                    viewModel.stats.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> { }
                            is Resource.Success -> {
                                resource.data?.let { stats ->
                                    binding!!.numberBooks.text = stats.booksCount.toString()
                                    binding!!.numberFollowers.text = stats.followersCount.toString()
                                    binding!!.numberFollowing.text = stats.followingCount.toString()
                                    binding!!.numberFavorites.text = stats.favoritesCount.toString()
                                }
                            }
                            is Resource.Error -> {
                                Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                launch {
                    viewModel.isFollowing.collect { isFollowing ->
                        if (isFollowing) {
                            binding!!.follow.setImageResource(R.drawable.ic_heart_selected)
                            binding!!.follow.tag = "added"
                        } else {
                            binding!!.follow.setImageResource(R.drawable.ic_heart_unselected)
                            binding!!.follow.tag = "add"
                        }
                    }
                }
            }
        }
    }
}


