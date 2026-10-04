package com.flatcode.littlebooks.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.databinding.FragmentSettingsBinding
import com.flatcode.littlebooks.model.Setting
import com.flatcode.littlebooks.ui.book.BookAddActivity
import com.flatcode.littlebooks.ui.book.FavoritesActivity
import com.flatcode.littlebooks.ui.book.MyBooksActivity
import com.flatcode.littlebooks.ui.profile.FollowersActivity
import com.flatcode.littlebooks.ui.profile.FollowingActivity
import com.flatcode.littlebooks.ui.profile.ProfileActivity
import com.flatcode.littlebooks.ui.profile.ProfileEditActivity
import com.flatcode.littlebooks.ui.publisher.ExplorePublishersActivity
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.dialogAboutApp
import com.flatcode.littlebooks.utils.dialogLogout
import com.flatcode.littlebooks.utils.loadImage
import com.flatcode.littlebooks.utils.openActivity
import com.flatcode.littlebooks.utils.rateApp
import com.flatcode.littlebooks.utils.shareApp
import com.flatcode.littlebooks.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var binding: FragmentSettingsBinding? = null
    private var adapter: SettingAdapter? = null
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        adapter = SettingAdapter { item ->
            when (item.id) {
                DATA.ABOUT_APP -> context?.dialogAboutApp()
                DATA.LOGOUT -> context?.dialogLogout()
                DATA.SHARE_APP -> context?.shareApp()
                DATA.RATE_APP -> context?.rateApp()
                else -> item.c?.let { targetClass ->
                    context?.let { ctx ->
                        val intent = Intent(ctx, targetClass)
                        ctx.startActivity(intent)
                    }
                }
            }
        }

        binding!!.recyclerView.adapter = adapter

        binding!!.toolbar.item.setOnClickListener {
            context?.openActivity<ProfileActivity>(
                clear = false, DATA.PROFILE_ID to DATA.FirebaseUserUid
            )
        }

        observeViewModel()

        return binding!!.root
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.user.collect { user ->
                        user?.let {
                            binding!!.toolbar.imageProfile.loadImage(true, it.profileImage)
                            binding!!.toolbar.username.text = it.username
                            binding!!.toolbar.email.text = it.email
                        }
                    }
                }

                launch {
                    combine(
                        viewModel.explorePublishersCount,
                        viewModel.booksCount,
                        viewModel.followersCount,
                        viewModel.followingCount,
                        viewModel.favoritesCount
                    ) { explore, books, followers, following, favorites ->
                        loadSettings(
                            explore, books, followers.toInt(), following.toInt(), favorites.toInt()
                        )
                    }.collect {}
                }
            }
        }
    }

    private fun loadSettings(
        explorePublishers: Int, myBooks: Int, followers: Int, following: Int, favorites: Int
    ) {
        val list = mutableListOf<Setting>()
        list.add(
            Setting(
                DATA.EDIT_PROFILE,
                "Edit Profile",
                R.drawable.ic_edit_white,
                0,
                ProfileEditActivity::class.java
            )
        )
        list.add(
            Setting(
                DATA.EXPLORE_PUBLISHERS,
                "Explore Publishers",
                R.drawable.ic_search_person,
                explorePublishers,
                ExplorePublishersActivity::class.java
            )
        )
        list.add(
            Setting(
                DATA.FOLLOWERS_ID,
                "Followers",
                R.drawable.ic_followers,
                followers,
                FollowersActivity::class.java
            )
        )
        list.add(
            Setting(
                DATA.FOLLOWING_ID,
                "Following",
                R.drawable.ic_following,
                following,
                FollowingActivity::class.java
            )
        )
        list.add(
            Setting(
                DATA.MY_BOOKS, "My books", R.drawable.ic_books, myBooks, MyBooksActivity::class.java
            )
        )
        list.add(
            Setting(
                DATA.ADD_BOOK, "Add book", R.drawable.ic_book_white, 0, BookAddActivity::class.java
            )
        )
        list.add(
            Setting(
                DATA.FAVORITES_ID,
                "Favorites",
                R.drawable.ic_star_selected,
                favorites,
                FavoritesActivity::class.java
            )
        )
        list.add(Setting(DATA.ABOUT_APP, "About App", R.drawable.ic_info, 0, null))
        list.add(Setting(DATA.LOGOUT, "Logout", R.drawable.ic_logout_white, 0, null))
        list.add(Setting(DATA.SHARE_APP, "Share App", R.drawable.ic_share, 0, null))
        list.add(Setting(DATA.RATE_APP, "Rate APP", R.drawable.ic_heart_selected, 0, null))
        list.add(
            Setting(
                DATA.PRIVACY_POLICY_ID,
                "Privacy Policy",
                R.drawable.ic_privacy_policy,
                0,
                PrivacyPolicyActivity::class.java
            )
        )
        adapter?.submitList(list)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadProfileData(
            DATA.FirebaseUserUid, DATA.FirebaseUserUid, DATA.FOLLOWERS, DATA.FOLLOWING
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}