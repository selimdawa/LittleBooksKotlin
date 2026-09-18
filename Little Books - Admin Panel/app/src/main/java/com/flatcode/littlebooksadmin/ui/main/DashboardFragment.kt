package com.flatcode.littlebooksadmin.ui.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooksadmin.Activity.*
import com.flatcode.littlebooksadmin.ui.main.MainAdapter
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.VOID
import com.flatcode.littlebooksadmin.model.Main
import com.flatcode.littlebooksadmin.repository.MainRepository
import com.flatcode.littlebooksadmin.utils.Resource
import com.flatcode.littlebooksadmin.databinding.FragmentDashboardBinding
import com.flatcode.littlebooksadmin.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private var list: MutableList<Main> = mutableListOf()
    private var adapter: MainAdapter? = null
    private lateinit var mContext: Context

    private val viewModel: MainViewModel by viewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
        observeViewModel()
    }

    private fun initUI() {
        binding.toolbar.image.setOnClickListener {
            VOID.IntentExtra(mContext, ProfileActivity::class.java, DATA.PROFILE_ID, DATA.FirebaseUserUid)
        }

        adapter = MainAdapter(mContext, list)
        binding.recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.user.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> { }
                            is Resource.Success -> {
                                resource.data?.let { user ->
                                    VOID.Glide(true, mContext, user.profileImage ?: DATA.BASIC, binding.toolbar.image)
                                }
                            }
                            is Resource.Error -> {
                                Toast.makeText(mContext, resource.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                launch {
                    viewModel.stats.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                binding.bar.visibility = View.VISIBLE
                                binding.recyclerView.visibility = View.GONE
                            }
                            is Resource.Success -> {
                                resource.data?.let { stats ->
                                    updateDashboard(stats)
                                }
                            }
                            is Resource.Error -> {
                                binding.bar.visibility = View.GONE
                                Toast.makeText(mContext, resource.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateDashboard(stats: MainRepository.DashboardStats) {
        list.clear()
        list.add(Main(R.drawable.ic_person, "Users", stats.users, UsersActivity::class.java))
        list.add(Main(R.drawable.ic_add, "Add Book", 0, BookAddActivity::class.java))
        list.add(Main(R.drawable.ic_book_white, "My Books", stats.myBooks, MyBooksActivity::class.java))
        list.add(Main(R.drawable.ic_books, "All Books", stats.allBooks, AllBooksActivity::class.java))
        list.add(Main(R.drawable.ic_rank, "Top Publisher", stats.publishers, TopPublishersActivity::class.java))
        list.add(Main(R.drawable.ic_users, "Editors Choice", stats.editorsChoice, EditorsChoiceActivity::class.java))
        list.add(Main(R.drawable.ic_add_category, "Add Category", 0, CategoryAddActivity::class.java))
        list.add(Main(R.drawable.ic_category_gray, "Categories", stats.categories, CategoriesActivity::class.java))
        list.add(Main(R.drawable.ic_slider, "Slider Show", stats.sliderShow, SliderShowActivity::class.java))
        list.add(Main(R.drawable.ic_followers, "Followers", stats.followers, FollowersActivity::class.java))
        list.add(Main(R.drawable.ic_following, "Following", stats.following, FollowingActivity::class.java))
        list.add(Main(R.drawable.ic_star_selected, "Favorites", stats.favorites, FavoritesActivity::class.java))
        list.add(Main(R.drawable.ic_ads, "AD's", stats.ads, ADsActivity::class.java))
        list.add(Main(R.drawable.ic_privacy_policy, "Privacy Policy", 0, PrivacyPolicyActivity::class.java))
        
        adapter?.notifyDataSetChanged()
        binding.bar.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

