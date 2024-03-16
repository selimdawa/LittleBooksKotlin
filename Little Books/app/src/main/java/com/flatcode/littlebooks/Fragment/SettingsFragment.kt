package com.flatcode.littlebooks.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.flatcode.littlebooks.Adapter.SettingAdapter
import com.flatcode.littlebooks.Model.Book
import com.flatcode.littlebooks.Model.Setting
import com.flatcode.littlebooks.Model.User
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.Unit.CLASS
import com.flatcode.littlebooks.Unit.DATA
import com.flatcode.littlebooks.Unit.VOID
import com.flatcode.littlebooks.databinding.FragmentSettingsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Objects

class SettingsFragment : Fragment() {

    private var binding: FragmentSettingsBinding? = null
    private var list: ArrayList<Setting>? = null
    private var adapter: SettingAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(LayoutInflater.from(context), container, false)

        //binding!!.recyclerView.setHasFixedSize(true)
        list = ArrayList()
        adapter = SettingAdapter(context, list!!)
        binding!!.recyclerView.adapter = adapter

        binding!!.toolbar.item.setOnClickListener {
            VOID.IntentExtra(context, CLASS.PROFILE, DATA.PROFILE_ID, DATA.FirebaseUserUid)
        }
        return binding!!.root
    }

    var E = 0
    var M = 0
    var FS = 0
    var FN = 0
    var FA = 0
    private fun nrItems() {
        val reference = FirebaseDatabase.getInstance().getReference(DATA.USERS)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                E = 0
                for (snapshot in dataSnapshot.children) {
                    val item = snapshot.getValue(User::class.java)!!
                    if (item.id != DATA.FirebaseUserUid) if (item.booksCount >= 1) E++
                }
                nrBooks()
            }

            private fun nrBooks() {
                val reference = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
                reference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        M = 0
                        for (data in dataSnapshot.children) {
                            val item = data.getValue(Book::class.java)!!
                            if (item.publisher == DATA.FirebaseUserUid) M++
                        }
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
                        loadSettings(E, M, FS, FN, FA)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun loadUserInfo() {
        val reference = FirebaseDatabase.getInstance().getReference(DATA.USERS)
        reference.child(Objects.requireNonNull(DATA.FirebaseUserUid))
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val item = snapshot.getValue(User::class.java)!!
                    val profileImage = item.profileImage
                    val username = item.username
                    val contact = item.email

                    VOID.Glide_(true, context, profileImage, binding!!.toolbar.imageProfile)
                    binding!!.toolbar.username.text = username
                    binding!!.toolbar.email.text = contact
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadSettings(
        explorePublishers: Int, myBooks: Int, followers: Int, following: Int, favorites: Int
    ) {
        list!!.clear()
        val item = Setting("1", "Edit Profile", R.drawable.ic_edit_white, 0, CLASS.PROFILE_EDIT)
        val item2 = Setting(
            "2", "Explore Publishers", R.drawable.ic_search_person,
            explorePublishers, CLASS.EXPLORE_PUBLISHERS
        )
        val item3 = Setting("3", "Followers", R.drawable.ic_followers, followers, CLASS.FOLLOWERS)
        val item4 = Setting("4", "Following", R.drawable.ic_following, following, CLASS.FOLLOWING)
        val item5 = Setting("5", "My books", R.drawable.ic_books, myBooks, CLASS.MY_BOOKS)
        val item6 = Setting("6", "Add book", R.drawable.ic_book_white, 0, CLASS.BOOK_ADD)
        val item7 =
            Setting("7", "Favorites", R.drawable.ic_star_selected, favorites, CLASS.FAVORITES)
        val item8 = Setting("8", "About App", R.drawable.ic_info, 0, null)
        val item9 = Setting("9", "Logout", R.drawable.ic_logout_white, 0, null)
        val item10 = Setting("10", "Share App", R.drawable.ic_share, 0, null)
        val item11 = Setting("11", "Rate APP", R.drawable.ic_heart_selected, 0, null)
        val item12 =
            Setting("12", "Privacy Policy", R.drawable.ic_privacy_policy, 0, CLASS.PRIVACY_POLICY)
        list!!.add(item)
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
        adapter!!.notifyDataSetChanged()
    }

    override fun onResume() {
        loadUserInfo()
        nrItems()
        super.onResume()
    }
}