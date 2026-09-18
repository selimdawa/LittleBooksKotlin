package com.flatcode.littlebooks.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooks.ui.book.LinearBookAdapter
import com.flatcode.littlebooks.model.Book
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.VOID
import com.flatcode.littlebooks.databinding.FragmentFollowersBinding
import com.flatcode.littlebooks.utils.Resource
import com.flatcode.littlebooks.viewmodel.FollowViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FollowersFragment : Fragment() {

    private var binding: FragmentFollowersBinding? = null
    private val viewModel: FollowViewModel by viewModels()
    private var list = ArrayList<Book?>()
    private var adapter: LinearBookAdapter? = null
    private var type: String = DATA.TIMESTAMP

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFollowersBinding.inflate(inflater, container, false)

        VOID.BannerAd(context, binding!!.adView, DATA.BANNER_SMART_FOLLOWERS_BOOKS)

        adapter = LinearBookAdapter(context, list, false)
        binding!!.recyclerView.adapter = adapter

        binding!!.switchBar.all.setOnClickListener {
            type = DATA.TIMESTAMP
            viewModel.loadBooksFromFollowed(DATA.FirebaseUserUid, type)
        }
        binding!!.switchBar.mostViews.setOnClickListener {
            type = DATA.VIEWS_COUNT
            viewModel.loadBooksFromFollowed(DATA.FirebaseUserUid, type)
        }
        binding!!.switchBar.mostLoves.setOnClickListener {
            type = DATA.LOVES_COUNT
            viewModel.loadBooksFromFollowed(DATA.FirebaseUserUid, type)
        }
        binding!!.switchBar.mostDownloads.setOnClickListener {
            type = DATA.DOWNLOADS_COUNT
            viewModel.loadBooksFromFollowed(DATA.FirebaseUserUid, type)
        }

        observeViewModel()

        return binding!!.root
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.booksFromFollowed.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            binding!!.progress.visibility = View.GONE
                            list.clear()
                            resource.data?.let { list.addAll(it) }
                            adapter?.notifyDataSetChanged()
                            if (list.isNotEmpty()) {
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

    override fun onResume() {
        super.onResume()
        viewModel.loadBooksFromFollowed(DATA.FirebaseUserUid, type)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}



