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
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooks.Activity.MoreBooksActivity
import com.flatcode.littlebooks.Adapter.CategoryAdapter
import com.flatcode.littlebooks.Adapter.ImageSliderAdapter
import com.flatcode.littlebooks.Adapter.MainBookAdapter
import com.flatcode.littlebooks.Model.Book
import com.flatcode.littlebooks.Model.Category
import com.flatcode.littlebooks.Unit.DATA
import com.flatcode.littlebooks.Unit.VOID
import com.flatcode.littlebooks.databinding.FragmentHomeBinding
import com.flatcode.littlebooks.utils.Resource
import com.flatcode.littlebooks.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var binding: FragmentHomeBinding? = null
    private val viewModel: HomeViewModel by viewModels()

    private var categoryList = ArrayList<Category?>()
    private var categoryAdapter: CategoryAdapter? = null

    private var editorsChoiceList = ArrayList<Book?>()
    private var editorsChoiceAdapter: MainBookAdapter? = null

    private var mostViewedList = ArrayList<Book?>()
    private var mostViewedAdapter: MainBookAdapter? = null

    private var mostLovedList = ArrayList<Book?>()
    private var mostLovedAdapter: MainBookAdapter? = null

    private var mostDownloadedList = ArrayList<Book?>()
    private var mostDownloadedAdapter: MainBookAdapter? = null

    private var newBooksList = ArrayList<Book?>()
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
        VOID.BannerAdTwo(
            context, binding!!.adView, DATA.BANNER_SMART_HOME,
            binding!!.adView2, DATA.BANNER_SMART_HOME_2
        )

        binding!!.showMore.setOnClickListener {
            VOID.IntentExtra3(
                context, MoreBooksActivity::class.java, DATA.SHOW_MORE_TYPE, DATA.EDITORS_CHOICE,
                DATA.SHOW_MORE_NAME, binding!!.name.text.toString(),
                DATA.SHOW_MORE_BOOLEAN, DATA.EMPTY + B_one
            )
        }
        binding!!.showMore2.setOnClickListener {
            VOID.IntentExtra3(
                context, MoreBooksActivity::class.java, DATA.SHOW_MORE_TYPE,
                DATA.VIEWS_COUNT, DATA.SHOW_MORE_NAME, binding!!.mostViews.text.toString(),
                DATA.SHOW_MORE_BOOLEAN, DATA.EMPTY + B_two
            )
        }
        binding!!.showMore3.setOnClickListener {
            VOID.IntentExtra3(
                context, MoreBooksActivity::class.java, DATA.SHOW_MORE_TYPE,
                DATA.LOVES_COUNT, DATA.SHOW_MORE_NAME, binding!!.name3.text.toString(),
                DATA.SHOW_MORE_BOOLEAN, DATA.EMPTY + B_three
            )
        }
        binding!!.showMore4.setOnClickListener {
            VOID.IntentExtra3(
                context, MoreBooksActivity::class.java, DATA.SHOW_MORE_TYPE,
                DATA.DOWNLOADS_COUNT, DATA.SHOW_MORE_NAME, binding!!.name4.text.toString(),
                DATA.SHOW_MORE_BOOLEAN, DATA.EMPTY + B_four
            )
        }
        binding!!.showMore5.setOnClickListener {
            VOID.IntentExtra3(
                context, MoreBooksActivity::class.java, DATA.SHOW_MORE_TYPE,
                DATA.TIMESTAMP, DATA.SHOW_MORE_NAME, binding!!.name5.text.toString(),
                DATA.SHOW_MORE_BOOLEAN, DATA.EMPTY + B_five
            )
        }

        categoryAdapter = CategoryAdapter(context, categoryList)
        binding!!.recyclerCategory.adapter = categoryAdapter

        editorsChoiceAdapter = MainBookAdapter(context, editorsChoiceList, true, false, true)
        binding!!.recyclerView.adapter = editorsChoiceAdapter

        mostViewedAdapter = MainBookAdapter(context, mostViewedList, false, true, false)
        binding!!.recyclerView2.adapter = mostViewedAdapter

        mostLovedAdapter = MainBookAdapter(context, mostLovedList, false, false, true)
        binding!!.recyclerView3.adapter = mostLovedAdapter

        mostDownloadedAdapter = MainBookAdapter(context, mostDownloadedList, true, false, false)
        binding!!.recyclerView4.adapter = mostDownloadedAdapter

        newBooksAdapter = MainBookAdapter(context, newBooksList, false, true, true)
        binding!!.recyclerView5.adapter = newBooksAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.sliderCount.collect { resource ->
                        if (resource is Resource.Success) {
                            binding!!.imageSlider.sliderAdapter = ImageSliderAdapter(context, resource.data!!)
                        }
                    }
                }
                launch {
                    viewModel.categories.collect { resource ->
                        handleResource(resource, categoryList, categoryAdapter)
                    }
                }
                launch {
                    viewModel.editorsChoiceBooks.collect { resource ->
                        handleResource(resource, editorsChoiceList, editorsChoiceAdapter, binding!!.bar, binding!!.recyclerView, binding!!.empty)
                    }
                }
                launch {
                    viewModel.mostViewedBooks.collect { resource ->
                        handleResource(resource, mostViewedList, mostViewedAdapter, binding!!.bar2, binding!!.recyclerView2, binding!!.empty2)
                    }
                }
                launch {
                    viewModel.mostLovedBooks.collect { resource ->
                        handleResource(resource, mostLovedList, mostLovedAdapter, binding!!.bar3, binding!!.recyclerView3, binding!!.empty3)
                    }
                }
                launch {
                    viewModel.mostDownloadedBooks.collect { resource ->
                        handleResource(resource, mostDownloadedList, mostDownloadedAdapter, binding!!.bar4, binding!!.recyclerView4, binding!!.empty4)
                    }
                }
                launch {
                    viewModel.newBooks.collect { resource ->
                        handleResource(resource, newBooksList, newBooksAdapter, binding!!.bar5, binding!!.recyclerView5, binding!!.empty5)
                    }
                }
            }
        }
    }

    private fun <T> handleResource(
        resource: Resource<List<T>>,
        list: ArrayList<T?>,
        adapter: RecyclerView.Adapter<*>?,
        bar: View? = null,
        recyclerView: View? = null,
        empty: View? = null
    ) {
        when (resource) {
            is Resource.Success -> {
                list.clear()
                resource.data?.let { list.addAll(it) }
                adapter?.notifyDataSetChanged()
                bar?.visibility = View.GONE
                if (list.isNotEmpty()) {
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