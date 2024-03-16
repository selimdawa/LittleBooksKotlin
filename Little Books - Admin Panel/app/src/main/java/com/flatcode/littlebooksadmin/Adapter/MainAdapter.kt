package com.flatcode.littlebooksadmin.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooksadmin.Model.Main
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.databinding.ItemMainBinding
import java.text.MessageFormat

class MainAdapter(private val context: Context, var list: List<Main>) :
    RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    private var binding: ItemMainBinding? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemMainBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding!!.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        val image = model.image
        val number = model.number
        val name = model.title
        //String id = list.getId();
        val c = model.c

        if (image != 0) {
            holder.image.setImageResource(image)
        } else {
            holder.image.setImageResource(R.drawable.ic_load)
        }

        if (number != 0) {
            holder.number.visibility = View.VISIBLE
            holder.number.text = MessageFormat.format("{0}{1}", DATA.EMPTY, number)
        } else {
            holder.number.visibility = View.GONE

        }

        holder.name.text = name

        holder.itemView.setOnClickListener {
            val intent = Intent(context, c)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView
        var number: TextView
        var image: ImageView
        var item: LinearLayout

        init {
            image = binding!!.image
            name = binding!!.name
            number = binding!!.number
            item = binding!!.item
        }
    }
}