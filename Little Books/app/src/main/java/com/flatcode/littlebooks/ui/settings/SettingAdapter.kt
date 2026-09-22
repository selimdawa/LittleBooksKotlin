package com.flatcode.littlebooks.ui.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.flatcode.littlebooks.databinding.ItemSettingBinding
import com.flatcode.littlebooks.model.Setting
import com.flatcode.littlebooks.ui.BaseListAdapter
import com.flatcode.littlebooks.utils.DATA
import java.text.MessageFormat

class SettingAdapter(
    private val onItemClick: (Setting) -> Unit
) : BaseListAdapter<Setting, ItemSettingBinding>(DiffCallback) {

    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemSettingBinding {
        return ItemSettingBinding.inflate(inflater, parent, false)
    }

    override fun bind(binding: ItemSettingBinding, item: Setting, position: Int) {
        binding.name.text = item.name
        binding.image.setImageResource(item.image)

        if (item.number != 0) {
            binding.number.visibility = View.VISIBLE
            binding.number.text = MessageFormat.format("{0}{1}", DATA.EMPTY, item.number)
        } else {
            binding.number.visibility = View.GONE
        }

        binding.root.setOnClickListener { onItemClick(item) }
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