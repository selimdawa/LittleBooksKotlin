package com.flatcode.littlebooks.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import coil.load
import com.flatcode.littlebooks.databinding.ItemSliderBinding
import com.flatcode.littlebooks.utils.DATA
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.smarteist.autoimageslider.SliderViewAdapter

class ImageSliderAdapter(private val setTotalCount: Int) :
    SliderViewAdapter<ImageSliderAdapter.SliderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup): SliderViewHolder {
        val binding = ItemSliderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SliderViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: SliderViewHolder, position: Int) {
        val imagePath = (position + 1).toString()
        FirebaseDatabase.getInstance().getReference(DATA.SLIDER_SHOW).child(imagePath)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val imageLink = snapshot.value?.toString()
                    if (!imageLink.isNullOrEmpty()) {
                        viewHolder.binding.imageView.load(imageLink)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun getCount(): Int = setTotalCount

    class SliderViewHolder(val binding: ItemSliderBinding) : ViewHolder(binding.root)
}
