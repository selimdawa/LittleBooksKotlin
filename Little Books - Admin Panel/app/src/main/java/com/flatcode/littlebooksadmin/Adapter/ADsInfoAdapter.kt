package com.flatcode.littlebooksadmin.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooksadmin.Filter.ADsInfoFilter
import com.flatcode.littlebooksadmin.Modelimport.ADs
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.databinding.ItemInfoAdsBinding
import java.text.MessageFormat

class ADsInfoAdapter(private val context: Context, var list: ArrayList<ADs?>, isUser: Boolean) :
    RecyclerView.Adapter<ADsInfoAdapter.ViewHolder>(), Filterable {

    private var binding: ItemInfoAdsBinding? = null
    var filterList: ArrayList<ADs?>
    private var filter: ADsInfoFilter? = null
    var isUser: Boolean

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemInfoAdsBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding!!.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val name = item!!.name
        val adsLoadedCount = item.adsLoadedCount
        val adsClickedCount = item.adsClickedCount

        if (name != null) {
            holder.name.text = name
        }

        holder.numberADsLoad.text = MessageFormat.format("{0}{1}", DATA.EMPTY, adsLoadedCount)
        holder.numberADsClick.text = MessageFormat.format("{0}{1}", DATA.EMPTY, adsClickedCount)
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

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
        var numberADsClick: TextView
        var numberADsLoad: TextView
        var name: TextView
        var item: LinearLayout

        init {
            numberADsClick = binding!!.numberADsClick
            numberADsLoad = binding!!.numberADsLoad
            name = binding!!.name
            item = binding!!.item
        }
    }

    init {
        filterList = list
        this.isUser = isUser
    }
}