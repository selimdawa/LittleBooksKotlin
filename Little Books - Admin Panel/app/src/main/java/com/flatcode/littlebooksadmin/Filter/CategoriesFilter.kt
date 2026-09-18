package com.flatcode.littlebooksadmin.filter

import android.widget.Filter
import com.flatcode.littlebooksadmin.model.Category
import com.flatcode.littlebooksadmin.ui.category.CategoriesAdapter

class CategoriesFilter(
    private var filterList: List<Category?>,
    private var adapter: CategoriesAdapter
) : Filter() {
    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()
        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().uppercase()
            val filteredModels = ArrayList<Category?>()
            for (i in filterList.indices) {
                if (filterList[i]!!.category!!.uppercase().contains(constraint)) {
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        } else {
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        adapter.list = results.values as ArrayList<Category?>
        adapter.notifyDataSetChanged()
    }
}
