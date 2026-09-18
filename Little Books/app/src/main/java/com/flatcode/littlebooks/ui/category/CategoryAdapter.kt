package com.flatcode.littlebooks.ui.category

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.flatcode.littlebooks.ui.book.BooksCategoryActivity
import com.flatcode.littlebooks.model.Category
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.glide
import com.flatcode.littlebooks.utils.intentExtra2
import com.flatcode.littlebooks.databinding.ItemCategoryBinding

class CategoryAdapter(private val context: Context?, var list: ArrayList<Category?>) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemCategoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding!!.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val id = item!!.id
        val name = item.category
        val image = item.image

        binding!!.image.glide(false, image)

        holder.itemView.setOnClickListener {
            context?.intentExtra2(
                BooksCategoryActivity::class.java, DATA.CATEGORY_ID, id, DATA.CATEGORY_NAME, name
            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        var image: ImageView

        init {
            image = binding!!.image
        }
    }

    companion object {
        private var binding: ItemCategoryBinding? = null
    }
}



