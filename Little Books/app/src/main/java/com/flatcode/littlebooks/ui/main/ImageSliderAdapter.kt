package com.flatcode.littlebooks.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import coil.load
import com.flatcode.littlebooks.databinding.ItemSliderBinding
import io.selimdawa.autoimageslider.adapter.SliderViewAdapter

class ImageSliderAdapter(private val imageUrls: List<String>) :
    SliderViewAdapter<ImageSliderAdapter.SliderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val binding = ItemSliderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SliderViewHolder(binding)
    }

    override fun onBind(viewHolder: SliderViewHolder, position: Int) {
        val imageLink = imageUrls[position]
        if (imageLink.isNotEmpty()) {
            viewHolder.binding.imageView.load(imageLink)
        }
    }

    override fun getItemCount(): Int = imageUrls.size

    class SliderViewHolder(val binding: ItemSliderBinding) : ViewHolder(binding.root)
}