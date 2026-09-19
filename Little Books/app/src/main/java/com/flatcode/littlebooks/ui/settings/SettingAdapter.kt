package com.flatcode.littlebooks.ui.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooks.databinding.ItemSettingBinding
import com.flatcode.littlebooks.model.Setting
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.dialogAboutApp
import com.flatcode.littlebooks.utils.dialogLogout
import com.flatcode.littlebooks.utils.openActivity
import com.flatcode.littlebooks.utils.rateApp
import com.flatcode.littlebooks.utils.shareApp
import java.text.MessageFormat

class SettingAdapter : ListAdapter<Setting, SettingAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSettingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    class ViewHolder(private val binding: ItemSettingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Setting) {
            val context = itemView.context
            val id = DATA.EMPTY + item.id
            val name = DATA.EMPTY + item.name
            val image = item.image
            val number = item.number
            val to = item.c

            binding.name.text = name
            binding.image.setImageResource(image)

            if (number != 0) {
                binding.number.visibility = View.VISIBLE
                binding.number.text = MessageFormat.format("{0}{1}", DATA.EMPTY, number)
            } else {
                binding.number.visibility = View.GONE
            }

            binding.item.setOnClickListener {
                when (id) {
                    "8" -> context.dialogAboutApp()
                    "9" -> context.dialogLogout()
                    "10" -> context.shareApp()
                    "11" -> context.rateApp()
                    else -> to?.let { context.openActivity(it) }
                }
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Setting>() {
            override fun areItemsTheSame(oldItem: Setting, newItem: Setting): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Setting, newItem: Setting): Boolean =
                oldItem == newItem
        }
    }
}