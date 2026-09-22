package com.flatcode.littlebooks.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.loadImage
import com.flatcode.littlebooks.utils.openActivity
import com.flatcode.littlebooks.databinding.ActivityProfileBinding
import com.flatcode.littlebooks.utils.Resource
import com.flatcode.littlebooks.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.MessageFormat

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    private var binding: ActivityProfileBinding? = null
    var context: Context = this@ProfileActivity
    var profileId: String? = null

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Top buttons container
            (binding!!.editOrInfo.parent as? ViewGroup)?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top + 20 // Original margin 20sp
            }
            // Bottom stats container
            (binding!!.numberBooks.parent?.parent as? ViewGroup)?.updatePadding(bottom = systemBars.bottom)
            insets
        }

        profileId = intent.getStringExtra(DATA.PROFILE_ID)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        if (profileId == DATA.FirebaseUserUid) {
            binding!!.follow.visibility = View.GONE
            binding!!.editOrInfo.setImageResource(R.drawable.ic_edit_white)
            binding!!.editOrInfo.setOnClickListener { context.openActivity<ProfileEditActivity>() }
        } else {
            binding!!.follow.visibility = View.VISIBLE
            binding!!.editOrInfo.setImageResource(R.drawable.ic_books)
            binding!!.editOrInfo.setOnClickListener {
                context.openActivity<ProfileInfoActivity>(false, DATA.PROFILE_ID to profileId)
            }
        }

        binding!!.follow.setOnClickListener {
            val isCurrentlyFollowing = binding!!.follow.tag == "added"
            viewModel.followUser(DATA.FirebaseUserUid, profileId!!, !isCurrentlyFollowing)
        }
        binding!!.back.setOnClickListener { onBackPressed() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.user.collect { resource ->
                        if (resource is Resource.Success) {
                            val user = resource.data
                            binding!!.username.text = user?.username
                            binding!!.profile.loadImage(true, user?.profileImage)
                        }
                    }
                }
                launch {
                    viewModel.booksCount.collect { resource ->
                        if (resource is Resource.Success) {
                            binding!!.numberBooks.text = MessageFormat.format("{0}", resource.data)
                        }
                    }
                }
                launch {
                    viewModel.followersCount.collect { resource ->
                        if (resource is Resource.Success) {
                            binding!!.numberFollowers.text = MessageFormat.format("{0}", resource.data)
                        }
                    }
                }
                launch {
                    viewModel.followingCount.collect { resource ->
                        if (resource is Resource.Success) {
                            binding!!.numberFollowing.text = MessageFormat.format("{0}", resource.data)
                        }
                    }
                }
                launch {
                    viewModel.favoritesCount.collect { resource ->
                        if (resource is Resource.Success) {
                            binding!!.numberFavorites.text = MessageFormat.format("{0}", resource.data)
                        }
                    }
                }
                launch {
                    viewModel.isFollowing.collect { resource ->
                        if (resource is Resource.Success) {
                            val following = resource.data == true
                            if (following) {
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

    private fun init() {
        profileId?.let {
            viewModel.loadProfileData(it, DATA.FirebaseUserUid, DATA.FOLLOWERS, DATA.FOLLOWING)
        }
    }

    override fun onResume() {
        super.onResume()
        init()
    }
}



