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
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooks.ui.book.MoreBooksActivity
import com.flatcode.littlebooks.ui.category.CategoryAdapter
import com.flatcode.littlebooks.model.Book
import com.flatcode.littlebooks.model.Category
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.bannerAdTwo
import com.flatcode.littlebooks.utils.intentExtra3
import com.flatcode.littlebooks.databinding.FragmentHomeBinding
import com.flatcode.littlebooks.utils.Resource
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

    private val B_one = false
    private val B_two = true
    private val B_three = true
    private val B_four = true
    private val B_five = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupUI()
        observeViewModel()

        return binding!!.root
    }

    private fun setupUI() {
        context?.bannerAdTwo(
            binding!!.adView, DATA.BANNER_SMART_HOME,
            binding!!.adView2, DATA.BANNER_SMART_HOME_2
        )

        binding!!.showMore.setOnClickListener {
            context?.intentExtra3(
                MoreBooksActivity::class.java, DATA.SHOW_MORE_TYPE, DATA.EDITORS_CHOICE,
                DATA.SHOW_MORE_NAME, binding!!.name.text.toString(),
                DATA.SHOW_MORE_BOOLEAN, DATA.EMPTY + B_one
            )
        }
        binding!!.showMore2.setOnClickListener {
            context?.intentExtra3(
                MoreBooksActivity::class.java, DATA.SHOW_MORE_TYPE,
                DATA.VIEWS_COUNT, DATA.SHOW_MORE_NAME, binding!!.mostViews.text.toString(),
                DATA.SHOW_MORE_BOOLEAN, DATA.EMPTY + B_two
            )
        }
        binding!!.showMore3.setOnClickListener {
            context?.intentExtra3(
                MoreBooksActivity::class.java, DATA.SHOW_MORE_TYPE,
                DATA.LOVES_COUNT, DATA.SHOW_MORE_NAME, binding!!.name3.text.toString(),
                DATA.SHOW_MORE_BOOLEAN, DATA.EMPTY + B_three
            )
        }
        binding!!.showMore4.setOnClickListener {
            context?.intentExtra3(
                MoreBooksActivity::class.java, DATA.SHOW_MORE_TYPE,
                DATA.DOWNLOADS_COUNT, DATA.SHOW_MORE_NAME, binding!!.name4.text.toString(),
                DATA.SHOW_MORE_BOOLEAN, DATA.EMPTY + B_four
            )
        }
        binding!!.showMore5.setOnClickListener {
            context?.intentExtra3(
                MoreBooksActivity::class.java, DATA.SHOW_MORE_TYPE,
                DATA.TIMESTAMP, DATA.SHOW_MORE_NAME, binding!!.name5.text.toString(),
                DATA.SHOW_MORE_BOOLEAN, DATA.EMPTY + B_five
            )
        }

        categoryAdapter = CategoryAdapter()
        binding!!.recyclerCategory.adapter = categoryAdapter

        editorsChoiceAdapter = MainBookAdapter(true, false, true)
        binding!!.recyclerView.adapter = editorsChoiceAdapter

        mostViewedAdapter = MainBookAdapter(false, true, false)
        binding!!.recyclerView2.adapter = mostViewedAdapter

        mostLovedAdapter = MainBookAdapter(false, false, true)
        binding!!.recyclerView3.adapter = mostLovedAdapter

        mostDownloadedAdapter = MainBookAdapter(true, false, false)
        binding!!.recyclerView4.adapter = mostDownloadedAdapter

        newBooksAdapter = MainBookAdapter(false, true, true)
        binding!!.recyclerView5.adapter = newBooksAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.sliderImages.collect { resource ->
                        if (resource is Resource.Success) {
                            binding!!.imageSlider.setSliderAdapter(ImageSliderAdapter(resource.data ?: emptyList()))
                        }
                    }
                }
                launch {
                    viewModel.categories.collect { resource ->
                        handleResource(resource, categoryAdapter)
                    }
                }
                launch {
                    viewModel.editorsChoiceBooks.collect { resource ->
                        handleResource(resource, editorsChoiceAdapter, binding!!.bar, binding!!.recyclerView, binding!!.empty)
                    }
                }
                launch {
                    viewModel.mostViewedBooks.collect { resource ->
                        handleResource(resource, mostViewedAdapter, binding!!.bar2, binding!!.recyclerView2, binding!!.empty2)
                    }
                }
                launch {
                    viewModel.mostLovedBooks.collect { resource ->
                        handleResource(resource, mostLovedAdapter, binding!!.bar3, binding!!.recyclerView3, binding!!.empty3)
                    }
                }
                launch {
                    viewModel.mostDownloadedBooks.collect { resource ->
                        handleResource(resource, mostDownloadedAdapter, binding!!.bar4, binding!!.recyclerView4, binding!!.empty4)
                    }
                }
                launch {
                    viewModel.newBooks.collect { resource ->
                        handleResource(resource, newBooksAdapter, binding!!.bar5, binding!!.recyclerView5, binding!!.empty5)
                    }
                }
            }
        }
    }

    private fun <T> handleResource(
        resource: Resource<List<T>>,
        adapter: RecyclerView.Adapter<*>?,
        bar: View? = null,
        recyclerView: View? = null,
        empty: View? = null
    ) {
        when (resource) {
            is Resource.Success -> {
                val data = resource.data ?: emptyList()
                when (adapter) {
                    is CategoryAdapter -> adapter.submitList(data as List<Category>)
                    is MainBookAdapter -> adapter.submitFullList(data as List<Book>)
                }
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



