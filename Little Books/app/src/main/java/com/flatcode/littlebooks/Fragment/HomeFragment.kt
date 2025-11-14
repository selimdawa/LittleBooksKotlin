package com.flatcode.littlebooks.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooks.Adapter.CategoryAdapter
import com.flatcode.littlebooks.Adapter.ImageSliderAdapter
import com.flatcode.littlebooks.Adapter.MainBookAdapter
import com.flatcode.littlebooks.Model.Book
import com.flatcode.littlebooks.Model.Category
import com.flatcode.littlebooks.Unit.CLASS
import com.flatcode.littlebooks.Unit.DATA
import com.flatcode.littlebooks.Unit.VOID
import com.flatcode.littlebooks.databinding.FragmentHomeBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private var binding: FragmentHomeBinding? = null
    private var list: ArrayList<Book?>? = null
    private var list2: ArrayList<Book?>? = null
    private var list3: ArrayList<Book?>? = null
    private var list4: ArrayList<Book?>? = null
    private var list5: ArrayList<Book?>? = null
    private var adapter: MainBookAdapter? = null
    private var adapter2: MainBookAdapter? = null
    private var adapter3: MainBookAdapter? = null
    private var adapter4: MainBookAdapter? = null
    private var adapter5: MainBookAdapter? = null
    private val B_one = false
    private val B_two = true
    private val B_three = true
    private val B_four = true
    private val B_five = true
    var TotalCounts = 0
    private var categoryList: ArrayList<Category?>? = null
    private var categoryAdapter: CategoryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(LayoutInflater.from(context), container, false)

        VOID.BannerAdTwo(
            context, binding!!.adView, DATA.BANNER_SMART_HOME,
            binding!!.adView2, DATA.BANNER_SMART_HOME_2
        )

        binding!!.showMore.setOnClickListener {
            VOID.IntentExtra3(
                context, CLASS.MORE_BOOKS, DATA.SHOW_MORE_TYPE, DATA.EDITORS_CHOICE,
                DATA.SHOW_MORE_NAME, binding!!.name.text.toString(),
                DATA.SHOW_MORE_BOOLEAN, DATA.EMPTY + B_one
            )
        }
        binding!!.showMore2.setOnClickListener {
            VOID.IntentExtra3(
                context, CLASS.MORE_BOOKS, DATA.SHOW_MORE_TYPE,
                DATA.VIEWS_COUNT, DATA.SHOW_MORE_NAME, binding!!.mostViews.text.toString(),
                DATA.SHOW_MORE_BOOLEAN, DATA.EMPTY + B_two
            )
        }
        binding!!.showMore3.setOnClickListener {
            VOID.IntentExtra3(
                context, CLASS.MORE_BOOKS, DATA.SHOW_MORE_TYPE,
                DATA.LOVES_COUNT, DATA.SHOW_MORE_NAME, binding!!.name3.text.toString(),
                DATA.SHOW_MORE_BOOLEAN, DATA.EMPTY + B_three
            )
        }
        binding!!.showMore4.setOnClickListener {
            VOID.IntentExtra3(
                context, CLASS.MORE_BOOKS, DATA.SHOW_MORE_TYPE,
                DATA.DOWNLOADS_COUNT, DATA.SHOW_MORE_NAME, binding!!.name4.text.toString(),
                DATA.SHOW_MORE_BOOLEAN, DATA.EMPTY + B_four
            )
        }
        binding!!.showMore5.setOnClickListener {
            VOID.IntentExtra3(
                context, CLASS.MORE_BOOKS, DATA.SHOW_MORE_TYPE,
                DATA.TIMESTAMP, DATA.SHOW_MORE_NAME, binding!!.name5.text.toString(),
                DATA.SHOW_MORE_BOOLEAN, DATA.EMPTY + B_five
            )
        }

        //RecyclerView Category
        //binding.recyclerCategory.setHasFixedSize(true);
        categoryList = ArrayList()
        categoryAdapter = CategoryAdapter(context, categoryList!!)
        binding!!.recyclerCategory.adapter = categoryAdapter

        //RecyclerView Editor's Choice
        //binding.recyclerView.setHasFixedSize(true);
        list = ArrayList()
        adapter = MainBookAdapter(context, list!!, true, false, true)
        binding!!.recyclerView.adapter = adapter

        //RecyclerView Views Count
        //binding.recyclerView2.setHasFixedSize(true);
        list2 = ArrayList()
        adapter2 = MainBookAdapter(context, list2!!, false, true, false)
        binding!!.recyclerView2.adapter = adapter2

        //RecyclerView Loves Count
        //binding.recyclerView3.setHasFixedSize(true);
        list3 = ArrayList()
        adapter3 = MainBookAdapter(context, list3!!, false, false, true)
        binding!!.recyclerView3.adapter = adapter3

        //RecyclerView Downloads Count
        //binding.recyclerView4.setHasFixedSize(true);
        list4 = ArrayList()
        adapter4 = MainBookAdapter(context, list4!!, true, false, false)
        binding!!.recyclerView4.adapter = adapter4

        //RecyclerView New Books
        //binding.recyclerView5.setHasFixedSize(true);
        list5 = ArrayList()
        adapter5 = MainBookAdapter(context, list5!!, false, true, true)
        binding!!.recyclerView5.adapter = adapter5

        FirebaseDatabase.getInstance().getReference(DATA.SLIDER_SHOW)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val counts = snapshot.childrenCount
                    TotalCounts = counts.toInt()
                    binding!!.imageSlider.sliderAdapter = ImageSliderAdapter(context, TotalCounts)
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        init()
        return binding!!.root
    }

    private fun init() {
        loadCategories()
        loadPostEditorsChoice(
            DATA.EDITORS_CHOICE, list, adapter, binding!!.bar,
            binding!!.recyclerView, binding!!.empty
        )
        loadPostBy(
            DATA.VIEWS_COUNT, list2, adapter2, binding!!.bar2,
            binding!!.recyclerView2, binding!!.empty2
        )
        loadPostBy(
            DATA.LOVES_COUNT, list3, adapter3, binding!!.bar3,
            binding!!.recyclerView3, binding!!.empty3
        )
        loadPostBy(
            DATA.DOWNLOADS_COUNT, list4, adapter4, binding!!.bar4,
            binding!!.recyclerView4, binding!!.empty4
        )
        loadPostBy(
            DATA.TIMESTAMP, list5, adapter5, binding!!.bar5,
            binding!!.recyclerView5, binding!!.empty5
        )
    }

    private fun loadCategories() {
        val ref = FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryList!!.clear()
                for (data in snapshot.children) {
                    val category = data.getValue(Category::class.java)
                    categoryList!!.add(category)
                }
                categoryAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadPostBy(
        orderBy: String?, list: ArrayList<Book?>?, adapter: MainBookAdapter?,
        bar: ProgressBar, recyclerView: RecyclerView, empty: TextView
    ) {
        val ref: Query = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
        ref.orderByChild(orderBy!!).limitToLast(DATA.ORDER_MAIN)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    list!!.clear()
                    for (data in snapshot.children) {
                        val item = data.getValue(Book::class.java)!!
                        if (orderBy != DATA.EDITORS_CHOICE) list.add(item)
                    }
                    adapter!!.notifyDataSetChanged()
                    bar.visibility = View.GONE
                    if (list.isNotEmpty()) {
                        recyclerView.visibility = View.VISIBLE
                        empty.visibility = View.GONE
                        if (orderBy != DATA.EDITORS_CHOICE) list.reverse()
                    } else {
                        recyclerView.visibility = View.GONE
                        empty.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadPostEditorsChoice(
        orderBy: String?, list: ArrayList<Book?>?, adapter: MainBookAdapter?,
        bar: ProgressBar, recyclerView: RecyclerView, empty: TextView
    ) {
        val ref: Query = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
        ref.orderByChild(orderBy!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list!!.clear()
                for (data in snapshot.children) {
                    val item = data.getValue(Book::class.java)!!
                    if (orderBy == DATA.EDITORS_CHOICE) {
                        if (item.editorsChoice == 1 || item.editorsChoice == 2) list.add(item)
                    }
                }
                adapter!!.notifyDataSetChanged()
                bar.visibility = View.GONE
                if (list.isNotEmpty()) {
                    recyclerView.visibility = View.VISIBLE
                    empty.visibility = View.GONE
                    if (orderBy != DATA.EDITORS_CHOICE) list.reverse()
                } else {
                    recyclerView.visibility = View.GONE
                    empty.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}