package com.flatcode.littlebooks.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import coil.load
import com.flatcode.littlebooks.databinding.ItemSliderBinding
import com.flatcode.littlebooks.ui.main.ImageSliderAdapter.SliderViewHolder
import com.flatcode.littlebooks.utils.DATA
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.smarteist.autoimageslider.SliderViewAdapter

class ImageSliderAdapter(var context: Context?, var setTotalCount: Int) :
    SliderViewAdapter<SliderViewHolder>() {

    var ImageLink: String? = null

    override fun onCreateViewHolder(parent: ViewGroup): SliderViewHolder {
        val binding = ItemSliderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SliderViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: SliderViewHolder, position: Int) {
        FirebaseDatabase.getInstance().getReference(DATA.SLIDER_SHOW)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val imagePath = (position + 1).toString()
                    if (snapshot.hasChild(imagePath)) {
                        ImageLink = snapshot.child(imagePath).value.toString()
                        viewHolder.binding.imageView.load(ImageLink)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun getCount(): Int {
        return setTotalCount
    }

    class SliderViewHolder(val binding: ItemSliderBinding) : ViewHolder(binding.root)
}
