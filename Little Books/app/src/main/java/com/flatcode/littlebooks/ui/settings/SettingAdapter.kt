package com.flatcode.littlebooks.ui.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooks.model.Setting
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.dialogAboutApp
import com.flatcode.littlebooks.utils.dialogLogout
import com.flatcode.littlebooks.utils.intent1
import com.flatcode.littlebooks.utils.rateApp
import com.flatcode.littlebooks.utils.shareApp
import com.flatcode.littlebooks.databinding.ItemSettingBinding
import java.text.MessageFormat

class SettingAdapter(private val context: Context?, private val list: ArrayList<Setting>) :
    RecyclerView.Adapter<SettingAdapter.ViewHolder>() {

    private var binding: ItemSettingBinding? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemSettingBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding!!.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val id = DATA.EMPTY + item.id
        val name = DATA.EMPTY + item.name
        val image = item.image
        val number = item.number
        val to = item.c

        holder.name.text = name
        holder.image.setImageResource(image)

        if (number != 0) {
            holder.number.visibility = View.VISIBLE
            holder.number.text = MessageFormat.format("{0}{1}", DATA.EMPTY, number)
        } else {
            holder.number.visibility = View.GONE
        }

        holder.item.setOnClickListener {
            when (id) {
                "8" -> context?.dialogAboutApp()
                "9" -> context?.dialogLogout()
                "10" -> context?.shareApp()
                "11" -> context?.rateApp()
                else -> context?.intent1(to)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(
        view!!
    ) {
        var image: ImageView
        var name: TextView
        var number: TextView
        var item: LinearLayout

        init {
            image = binding!!.image
            number = binding!!.number
            name = binding!!.name
            item = binding!!.item
        }
    }
}



