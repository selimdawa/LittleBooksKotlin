package com.flatcode.littlebooks.ui.publisher

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.ui.profile.ProfileActivity
import com.flatcode.littlebooks.filter.PublisherFilter
import com.flatcode.littlebooks.model.User
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.VOID
import com.flatcode.littlebooks.databinding.ItemPublisherBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.MessageFormat

class PublisherAdapter(private val context: Context, var list: ArrayList<User?>) :
    RecyclerView.Adapter<PublisherAdapter.ViewHolder>(), Filterable {

    var filterList: ArrayList<User?>
    private var filter: PublisherFilter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPublisherBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val pubId = DATA.EMPTY + item!!.id
        val username = DATA.EMPTY + item.username
        val profileImage = DATA.EMPTY + item.profileImage

        VOID.Glide_(true, context, profileImage, holder.binding.imageProfile)

        if (username == DATA.EMPTY) {
            holder.binding.username.visibility = View.GONE
        } else {
            holder.binding.username.visibility = View.VISIBLE
            holder.binding.username.text = username
        }

        if (pubId == DATA.FirebaseUserUid) {
            holder.binding.add.visibility = View.GONE
        } else {
            holder.binding.add.visibility = View.VISIBLE
        }

        NrFollowers(holder.binding.numberFollowers, item.id)
        NrBooks(holder.binding.numberBooks, item.id)
        isFollowing(holder.binding.add, item.id)

        holder.binding.add.setOnClickListener {
            if (holder.binding.add.tag == "add") {
                FirebaseDatabase.getInstance().reference.child(DATA.FOLLOW)
                    .child(DATA.FirebaseUserUid)
                    .child(DATA.FOLLOWING).child(item.id!!).setValue(true)
                FirebaseDatabase.getInstance().reference.child(DATA.FOLLOW).child(item.id!!)
                    .child(DATA.FOLLOWERS).child(DATA.FirebaseUserUid).setValue(true)
            } else {
                FirebaseDatabase.getInstance().reference.child(DATA.FOLLOW)
                    .child(DATA.FirebaseUserUid)
                    .child(DATA.FOLLOWING).child(item.id!!).removeValue()
                FirebaseDatabase.getInstance().reference.child(DATA.FOLLOW).child(item.id!!)
                    .child(DATA.FOLLOWERS).child(DATA.FirebaseUserUid).removeValue()
            }
        }

        holder.itemView.setOnClickListener {
            VOID.IntentExtra(context, ProfileActivity::class.java, DATA.PROFILE_ID, item.id)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = PublisherFilter(filterList, this)
        }
        return filter!!
    }

    class ViewHolder(val binding: ItemPublisherBinding) : RecyclerView.ViewHolder((binding as ViewBinding).root)

    private fun isFollowing(add: ImageView, userId: String?) {
        val reference = FirebaseDatabase.getInstance().reference.child(DATA.FOLLOW)
            .child(DATA.FirebaseUserUid).child(DATA.FOLLOWING)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(userId!!).exists()) {
                    add.setImageResource(R.drawable.ic_heart_selected)
                    add.tag = "added"
                } else {
                    add.setImageResource(R.drawable.ic_heart_unselected)
                    add.tag = "add"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun NrFollowers(numberConnected: TextView, userId: String?) {
        val reference = FirebaseDatabase.getInstance().reference.child(DATA.FOLLOW)
            .child(userId!!).child(DATA.FOLLOWERS)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                numberConnected.text = MessageFormat.format("{0}", dataSnapshot.childrenCount)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun NrBooks(numberConnected: TextView, userId: String?) {
        val reference = FirebaseDatabase.getInstance().reference.child(DATA.USERS).child(userId!!)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val number = DATA.EMPTY + dataSnapshot.child(DATA.BOOKS_COUNT).value
                numberConnected.text = number
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    init {
        filterList = list
    }
}
