package com.flatcode.littlebooks.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooks.Adapter.SettingAdapter
import com.flatcode.littlebooks.Model.Setting
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.Unit.CLASS
import com.flatcode.littlebooks.Unit.DATA
import com.flatcode.littlebooks.Unit.VOID
import com.flatcode.littlebooks.databinding.FragmentSettingsBinding
import com.flatcode.littlebooks.utils.Resource
import com.flatcode.littlebooks.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var binding: FragmentSettingsBinding? = null
    private var list = ArrayList<Setting>()
    private var adapter: SettingAdapter? = null
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        adapter = SettingAdapter(context, list)
        binding!!.recyclerView.adapter = adapter

        binding!!.toolbar.item.setOnClickListener {
            VOID.IntentExtra(context, CLASS.PROFILE, DATA.PROFILE_ID, DATA.FirebaseUserUid)
        }

        observeViewModel()

        return binding!!.root
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.user.collect { resource ->
                        if (resource is Resource.Success) {
                            val user = resource.data
                            VOID.Glide_(true, context, user?.profileImage, binding!!.toolbar.imageProfile)
                            binding!!.toolbar.username.text = user?.username
                            binding!!.toolbar.email.text = user?.email
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
                        if (explore is Resource.Success && books is Resource.Success &&
                            followers is Resource.Success && following is Resource.Success &&
                            favorites is Resource.Success
                        ) {
                            loadSettings(
                                explore.data ?: 0,
                                books.data ?: 0,
                                followers.data?.toInt() ?: 0,
                                following.data?.toInt() ?: 0,
                                favorites.data?.toInt() ?: 0
                            )
                        }
                    }.collect {}
                }
            }
        }
    }

    private fun loadSettings(
        explorePublishers: Int, myBooks: Int, followers: Int, following: Int, favorites: Int
    ) {
        list.clear()
        list.add(Setting("1", "Edit Profile", R.drawable.ic_edit_white, 0, CLASS.PROFILE_EDIT))
        list.add(Setting("2", "Explore Publishers", R.drawable.ic_search_person, explorePublishers, CLASS.EXPLORE_PUBLISHERS))
        list.add(Setting("3", "Followers", R.drawable.ic_followers, followers, CLASS.FOLLOWERS))
        list.add(Setting("4", "Following", R.drawable.ic_following, following, CLASS.FOLLOWING))
        list.add(Setting("5", "My books", R.drawable.ic_books, myBooks, CLASS.MY_BOOKS))
        list.add(Setting("6", "Add book", R.drawable.ic_book_white, 0, CLASS.BOOK_ADD))
        list.add(Setting("7", "Favorites", R.drawable.ic_star_selected, favorites, CLASS.FAVORITES))
        list.add(Setting("8", "About App", R.drawable.ic_info, 0, null))
        list.add(Setting("9", "Logout", R.drawable.ic_logout_white, 0, null))
        list.add(Setting("10", "Share App", R.drawable.ic_share, 0, null))
        list.add(Setting("11", "Rate APP", R.drawable.ic_heart_selected, 0, null))
        list.add(Setting("12", "Privacy Policy", R.drawable.ic_privacy_policy, 0, CLASS.PRIVACY_POLICY))
        adapter!!.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        DATA.FirebaseUserUid?.let {
            viewModel.loadProfileData(it, it, DATA.FOLLOWERS, DATA.FOLLOWING)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}