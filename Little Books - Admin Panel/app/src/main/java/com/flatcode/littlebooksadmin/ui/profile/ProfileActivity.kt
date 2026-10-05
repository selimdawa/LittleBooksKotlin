package com.flatcode.littlebooksadmin.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.flatcode.littlebooksadmin.utils.BaseActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.databinding.ActivityProfileBinding
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.loadImage
import com.flatcode.littlebooksadmin.utils.openActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val context: Context = this@ProfileActivity
    private var profileId: String? = null

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profileId = intent.getStringExtra(DATA.PROFILE_ID)

        initUI()
        observeViewModel()

        profileId?.let { viewModel.loadProfile(it) }
    }

    private fun initUI() {
        binding.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        if (profileId == DATA.FirebaseUserUid) {
            binding.follow.visibility = View.GONE
            binding.editOrInfo.setImageResource(R.drawable.ic_edit_white)
            binding.editOrInfo.setOnClickListener { context.openActivity<ProfileEditActivity>() }
        } else {
            binding.follow.visibility = View.VISIBLE
            binding.editOrInfo.setImageResource(R.drawable.ic_books)
            binding.editOrInfo.setOnClickListener {
                context.openActivity<ProfileInfoActivity>(extras = arrayOf(DATA.PROFILE_ID to profileId))
            }
        }

        binding.follow.setOnClickListener {
            profileId?.let { viewModel.toggleFollow(it) }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.user.collect { user ->
                        user?.let {
                            binding.username.text = it.username
                            binding.profile.loadImage(
                                isUser = true, url = it.profileImage ?: DATA.BASIC
                            )
                        }
                    }
                }
                launch {
                    viewModel.stats.collect { stats ->
                        stats?.let {
                            binding.numberBooks.text = it.booksCount.toString()
                            binding.numberFollowers.text = it.followersCount.toString()
                            binding.numberFollowing.text = it.followingCount.toString()
                            binding.numberFavorites.text = it.favoritesCount.toString()
                        }
                    }
                }
                launch {
                    viewModel.isFollowing.collect { isFollowing ->
                        if (isFollowing) {
                            binding.follow.setImageResource(R.drawable.ic_heart_selected)
                            binding.follow.tag = "added"
                        } else {
                            binding.follow.setImageResource(R.drawable.ic_heart_unselected)
                            binding.follow.tag = "add"
                        }
                    }
                }
            }
        }
    }
}