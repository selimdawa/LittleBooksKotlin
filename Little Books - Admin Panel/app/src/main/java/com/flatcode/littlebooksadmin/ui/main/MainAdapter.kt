package com.flatcode.littlebooksadmin.ui.main

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.databinding.ItemMainBinding
import com.flatcode.littlebooksadmin.model.Main

class MainAdapter : ListAdapter<Main, MainAdapter.ViewHolder>(MainDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMainBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = getItem(position)
        val context = holder.itemView.context
        val image = model.image
        val number = model.number
        val name = model.title
        val c = model.c

        holder.binding.image.setImageResource(if (image != 0) image else R.drawable.ic_load)

        holder.binding.number.visibility = if (number != 0) View.VISIBLE else View.GONE
        if (number != 0) {
            holder.binding.number.text = number.toString()
        }

        holder.binding.name.text = name

        holder.itemView.setOnClickListener {
            val intent = Intent(context, c)
            context.startActivity(intent)
        }
    }

    class ViewHolder(val binding: ItemMainBinding) : RecyclerView.ViewHolder(binding.root)
}

class MainDiffCallback : DiffUtil.ItemCallback<Main>() {
    override fun areItemsTheSame(oldItem: Main, newItem: Main): Boolean {
        return oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItem: Main, newItem: Main): Boolean {
        return oldItem == newItem
    }
}
