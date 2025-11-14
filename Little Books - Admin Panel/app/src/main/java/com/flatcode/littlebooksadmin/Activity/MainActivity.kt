package com.flatcode.littlebooksadmin.Activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.flatcode.littlebooksadmin.Adapter.MainAdapter
import com.flatcode.littlebooksadmin.Model.Main
import com.flatcode.littlebooksadmin.Modelimport.Book
import com.flatcode.littlebooksadmin.Modelimport.User
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.Unit.CLASS
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.Unit.THEME
import com.flatcode.littlebooksadmin.Unit.VOID
import com.flatcode.littlebooksadmin.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity(), OnSharedPreferenceChangeListener {

    private var binding: ActivityMainBinding? = null
    var list: MutableList<Main>? = null
    var adapter: MainAdapter? = null
    var context: Context = this@MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        PreferenceManager.getDefaultSharedPreferences(baseContext)
            .registerOnSharedPreferenceChangeListener(this)
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        // Color Mode ----------------------------- Start
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        // Color Mode -------------------------------- End

        val sharedPreferences = PreferenceManager
            .getDefaultSharedPreferences(baseContext)
        if (sharedPreferences.getString(DATA.COLOR_OPTION, "ONE") == "ONE") {
            binding!!.toolbar.mode.setBackgroundResource(R.drawable.sun)
        } else if (sharedPreferences.getString(DATA.COLOR_OPTION, "NIGHT_ONE") == "NIGHT_ONE") {
            binding!!.toolbar.mode.setBackgroundResource(R.drawable.moon)
        }

        userInfo()
        binding!!.toolbar.image.setOnClickListener {
            VOID.IntentExtra(context, CLASS.PROFILE, DATA.PROFILE_ID, DATA.FirebaseUserUid)
        }

        //binding!!.recyclerView.setHasFixedSize(true)
        list = ArrayList()
        adapter = MainAdapter(context, list!!)
        binding!!.recyclerView.adapter = adapter

        nrItems()
    }

    var U = 0
    var P = 0
    var M = 0
    var B = 0
    var S = 0
    var FS = 0
    var FN = 0
    var FA = 0
    var AD = 0
    var E = 0
    var CA = 0

    private fun nrItems() {
        val reference = FirebaseDatabase.getInstance().getReference(DATA.USERS)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                U = 0
                P = 0
                AD = 0
                for (data in dataSnapshot.children) {
                    val item = data.getValue(User::class.java)!!
                    if (item.id != null) {
                        U++
                        if (item.booksCount >= 1) P++
                        if (!(item.adLoad == 0 && item.adClick == 0)) AD++
                    }
                }
                nrBooks()
            }

            private fun nrBooks() {
                val reference = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
                reference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        B = 0
                        M = 0
                        E = 0
                        for (data in dataSnapshot.children) {
                            val item = data.getValue(Book::class.java)!!
                            if (item.id != null) B++
                            if (item.publisher == DATA.FirebaseUserUid) M++
                            if (item.editorsChoice != 0) E++
                        }
                        nrCategories()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }

            private fun nrCategories() {
                val reference = FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES)
                reference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        CA = 0
                        for (data in dataSnapshot.children) {
                            val item = data.getValue(Book::class.java)!!
                            if (item.id != null) if (item.publisher == DATA.FirebaseUserUid) CA++
                        }
                        nrSliderShow()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }

            private fun nrSliderShow() {
                val reference = FirebaseDatabase.getInstance().getReference(DATA.SLIDER_SHOW)
                reference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        S = 0
                        S = dataSnapshot.childrenCount.toInt()
                        nrFollowers()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }

            private fun nrFollowers() {
                val reference = FirebaseDatabase.getInstance().getReference(DATA.FOLLOW)
                    .child(DATA.FirebaseUserUid).child(DATA.FOLLOWERS)
                reference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        FS = 0
                        FS = dataSnapshot.childrenCount.toInt()
                        nrFollowing()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }

            private fun nrFollowing() {
                val reference = FirebaseDatabase.getInstance().getReference(DATA.FOLLOW)
                    .child(DATA.FirebaseUserUid).child(DATA.FOLLOWING)
                reference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        FN = 0
                        FN = dataSnapshot.childrenCount.toInt()
                        nrFavorites()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }

            private fun nrFavorites() {
                val reference = FirebaseDatabase.getInstance().getReference(DATA.FAVORITES)
                    .child(DATA.FirebaseUserUid)
                reference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        FA = 0
                        FA = dataSnapshot.childrenCount.toInt()
                        IdeaPosts(U, P, M, B, S, FS, FN, FA, AD, E, CA)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun userInfo() {
        val reference =
            FirebaseDatabase.getInstance().getReference(DATA.USERS).child(DATA.FirebaseUserUid)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)!!
                VOID.Glide(true, context, user.profileImage!!, binding!!.toolbar.image)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun IdeaPosts(
        users: Int, publishers: Int, myBooks: Int, allBooks: Int, sliderShow: Int, followers: Int,
        following: Int, favorites: Int, ads: Int, editorsChoice: Int, categories: Int
    ) {
        list!!.clear()
        val item1 = Main(R.drawable.ic_person, "Users", users, CLASS.USERS)
        val item2 = Main(R.drawable.ic_add, "Add Book", 0, CLASS.BOOK_ADD)
        val item3 = Main(R.drawable.ic_book_white, "My Books", myBooks, CLASS.MY_BOOKS)
        val item4 = Main(R.drawable.ic_books, "All Books", allBooks, CLASS.ALL_BOOKS)
        val item5 = Main(R.drawable.ic_rank, "Top Publisher", publishers, CLASS.TOP_PUBLISHERS)
        val item6 = Main(R.drawable.ic_users, "Editors Choice", editorsChoice, CLASS.EDITORS_CHOICE)
        val item7 = Main(R.drawable.ic_add_category, "Add Category", 0, CLASS.CATEGORY_ADD)
        val item8 = Main(R.drawable.ic_category_gray, "Categories", categories, CLASS.CATEGORIES)
        val item9 = Main(R.drawable.ic_slider, "Slider Show", sliderShow, CLASS.SLIDER_SHOW)
        val item10 = Main(R.drawable.ic_followers, "Followers", followers, CLASS.FOLLOWERS)
        val item11 = Main(R.drawable.ic_following, "Following", following, CLASS.FOLLOWING)
        val item12 = Main(R.drawable.ic_star_selected, "Favorites", favorites, CLASS.FAVORITES)
        val item13 = Main(R.drawable.ic_ads, "AD's", ads, CLASS.ADS)
        val item14 = Main(R.drawable.ic_privacy_policy, "Privacy Policy", 0, CLASS.PRIVACY_POLICY)
        list!!.add(item1)
        list!!.add(item2)
        list!!.add(item3)
        list!!.add(item4)
        list!!.add(item5)
        list!!.add(item6)
        list!!.add(item7)
        list!!.add(item8)
        list!!.add(item9)
        list!!.add(item10)
        list!!.add(item11)
        list!!.add(item12)
        list!!.add(item13)
        list!!.add(item14)
        adapter!!.notifyDataSetChanged()
        binding!!.bar.visibility = View.GONE
        binding!!.recyclerView.visibility = View.VISIBLE
    }

    // Color Mode ----------------------------- Start
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "color_option") {
            recreate()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTINGS_CODE) {
            recreate()
        }
    }

    // Color Mode -------------------------------- End
    override fun onResume() {
        userInfo()
        nrItems()
        super.onResume()
    }

    companion object {
        private const val SETTINGS_CODE = 234
    }
}