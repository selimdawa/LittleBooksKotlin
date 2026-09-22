package com.flatcode.littlebooks.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooks.databinding.FragmentHomeBinding
import com.flatcode.littlebooks.ui.book.BooksCategoryActivity
import com.flatcode.littlebooks.ui.book.MoreBooksActivity
import com.flatcode.littlebooks.ui.category.CategoryAdapter
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.Resource
import com.flatcode.littlebooks.utils.loadBannerAdTwo
import com.flatcode.littlebooks.utils.openActivity
import com.flatcode.littlebooks.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var binding: FragmentHomeBinding? = null
    private val viewModel: HomeViewModel by viewModels()

    private var categoryAdapter: CategoryAdapter? = null

    private var editorsChoiceAdapter: MainBookAdapter? = null

    private var mostViewedAdapter: MainBookAdapter? = null

    private var mostLovedAdapter: MainBookAdapter? = null

    private var mostDownloadedAdapter: MainBookAdapter? = null

    private var newBooksAdapter: MainBookAdapter? = null

    private val bOne = false
    private val bTwo = true
    private val bThree = true
    private val bFour = true
    private val bFive = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupUI()
        observeViewModel()

        return binding!!.root
    }

    private fun setupUI() {
        context?.loadBannerAdTwo(
            binding!!.adView, DATA.BANNER_SMART_HOME, binding!!.adView2, DATA.BANNER_SMART_HOME_2
        )

        binding!!.showMore.setOnClickListener {
            context?.openActivity<MoreBooksActivity>(
                clear = false,
                DATA.SHOW_MORE_TYPE to DATA.EDITORS_CHOICE,
                DATA.SHOW_MORE_NAME to binding!!.name.text.toString(),
                DATA.SHOW_MORE_BOOLEAN to (DATA.EMPTY + bOne)
            )
        }
        binding!!.showMore2.setOnClickListener {
            context?.openActivity<MoreBooksActivity>(
                clear = false,
                DATA.SHOW_MORE_TYPE to DATA.VIEWS_COUNT,
                DATA.SHOW_MORE_NAME to binding!!.mostViews.text.toString(),
                DATA.SHOW_MORE_BOOLEAN to (DATA.EMPTY + bTwo)
            )
        }
        binding!!.showMore3.setOnClickListener {
            context?.openActivity<MoreBooksActivity>(
                clear = false,
                DATA.SHOW_MORE_TYPE to DATA.LOVES_COUNT,
                DATA.SHOW_MORE_NAME to binding!!.name3.text.toString(),
                DATA.SHOW_MORE_BOOLEAN to (DATA.EMPTY + bThree)
            )
        }
        binding!!.showMore4.setOnClickListener {
            context?.openActivity<MoreBooksActivity>(
                clear = false,
                DATA.SHOW_MORE_TYPE to DATA.DOWNLOADS_COUNT,
                DATA.SHOW_MORE_NAME to binding!!.name4.text.toString(),
                DATA.SHOW_MORE_BOOLEAN to (DATA.EMPTY + bFour)
            )
        }
        binding!!.showMore5.setOnClickListener {
            context?.openActivity<MoreBooksActivity>(
                clear = false,
                DATA.SHOW_MORE_TYPE to DATA.TIMESTAMP,
                DATA.SHOW_MORE_NAME to binding!!.name5.text.toString(),
                DATA.SHOW_MORE_BOOLEAN to (DATA.EMPTY + bFive)
            )
        }

        categoryAdapter = CategoryAdapter { item ->
            context?.openActivity<BooksCategoryActivity>(
                clear = false, DATA.CATEGORY_ID to item.id, DATA.CATEGORY_NAME to item.category
            )
        }
        binding!!.recyclerCategory.adapter = categoryAdapter

        editorsChoiceAdapter = MainBookAdapter(isDownloads = true, isLoves = false, isViews = true)
        binding!!.recyclerView.adapter = editorsChoiceAdapter

        mostViewedAdapter = MainBookAdapter(isDownloads = false, isLoves = true, isViews = false)
        binding!!.recyclerView2.adapter = mostViewedAdapter

        mostLovedAdapter = MainBookAdapter(isDownloads = false, isLoves = false, isViews = true)
        binding!!.recyclerView3.adapter = mostLovedAdapter

        mostDownloadedAdapter =
            MainBookAdapter(isDownloads = true, isLoves = false, isViews = false)
        binding!!.recyclerView4.adapter = mostDownloadedAdapter

        newBooksAdapter = MainBookAdapter(isDownloads = false, isLoves = true, isViews = true)
        binding!!.recyclerView5.adapter = newBooksAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.sliderImages.collect { resource ->
                        if (resource is Resource.Success) {
                            binding!!.imageSlider.setSliderAdapter(
                                ImageSliderAdapter(
                                    resource.data ?: emptyList()
                                )
                            )
                        }
                    }
                }
                launch {
                    viewModel.categories.collect { resource ->
                        handleResource(resource, { categoryAdapter?.submitList(it) })
                    }
                }
                launch {
                    viewModel.editorsChoiceBooks.collect { resource ->
                        handleResource(
                            resource,
                            { editorsChoiceAdapter?.submitList(it) },
                            binding!!.bar,
                            binding!!.recyclerView,
                            binding!!.empty
                        )
                    }
                }
                launch {
                    viewModel.mostViewedBooks.collect { resource ->
                        handleResource(
                            resource,
                            { mostViewedAdapter?.submitList(it) },
                            binding!!.bar2,
                            binding!!.recyclerView2,
                            binding!!.empty2
                        )
                    }
                }
                launch {
                    viewModel.mostLovedBooks.collect { resource ->
                        handleResource(
                            resource,
                            { mostLovedAdapter?.submitList(it) },
                            binding!!.bar3,
                            binding!!.recyclerView3,
                            binding!!.empty3
                        )
                    }
                }
                launch {
                    viewModel.mostDownloadedBooks.collect { resource ->
                        handleResource(
                            resource,
                            { mostDownloadedAdapter?.submitList(it) },
                            binding!!.bar4,
                            binding!!.recyclerView4,
                            binding!!.empty4
                        )
                    }
                }
                launch {
                    viewModel.newBooks.collect { resource ->
                        handleResource(
                            resource,
                            { newBooksAdapter?.submitList(it) },
                            binding!!.bar5,
                            binding!!.recyclerView5,
                            binding!!.empty5
                        )
                    }
                }
            }
        }
    }

    private fun <T> handleResource(
        resource: Resource<List<T>>,
        submitList: (List<T>) -> Unit,
        bar: View? = null,
        recyclerView: View? = null,
        empty: View? = null
    ) {
        when (resource) {
            is Resource.Success -> {
                val data = resource.data ?: emptyList()
                submitList(data)
                bar?.visibility = View.GONE
                if (data.isNotEmpty()) {
                    recyclerView?.visibility = View.VISIBLE
                    empty?.visibility = View.GONE
                } else {
                    recyclerView?.visibility = View.GONE
                    empty?.visibility = View.VISIBLE
                }
            }

            is Resource.Error -> {
                bar?.visibility = View.GONE
                recyclerView?.visibility = View.GONE
                empty?.visibility = View.VISIBLE
            }

            is Resource.Loading -> {
                bar?.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}



