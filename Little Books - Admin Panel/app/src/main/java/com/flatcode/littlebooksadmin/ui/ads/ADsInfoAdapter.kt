package com.flatcode.littlebooksadmin.ui.ads

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooksadmin.filter.ADsInfoFilter
import com.flatcode.littlebooksadmin.model.ADs
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.databinding.ItemInfoAdsBinding
import java.text.MessageFormat

class ADsInfoAdapter(private val context: Context, var list: ArrayList<ADs?>, isUser: Boolean) :
    RecyclerView.Adapter<ADsInfoAdapter.ViewHolder>(), Filterable {

    var filterList: ArrayList<ADs?>
    private var filter: ADsInfoFilter? = null
    var isUser: Boolean

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInfoAdsBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val name = item!!.name
        val adsLoadedCount = item.adsLoadedCount
        val adsClickedCount = item.adsClickedCount

        if (name != null) {
            holder.binding.name.text = name
        }

        holder.binding.numberADsLoad.text = MessageFormat.format("{0}{1}", DATA.EMPTY, adsLoadedCount)
        holder.binding.numberADsClick.text = MessageFormat.format("{0}{1}", DATA.EMPTY, adsClickedCount)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = ADsInfoFilter(filterList, this)
        }
        return filter!!
    }

    inner class ViewHolder(val binding: ItemInfoAdsBinding) : RecyclerView.ViewHolder(binding.root)

    init {
        filterList = list
        this.isUser = isUser
    }
}

