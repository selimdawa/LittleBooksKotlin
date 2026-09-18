package com.flatcode.littlebooksadmin.filter

import android.widget.Filter
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.ui.book.LinearBookAdapter

class MoreBooksFilter(
    private var filterList: List<Book?>,
    private var adapter: LinearBookAdapter
) : Filter() {
    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()
        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().uppercase()
            val filteredModels = ArrayList<Book?>()
            for (i in filterList.indices) {
                if (filterList[i]!!.title!!.uppercase().contains(constraint)) {
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
        adapter.list = results.values as ArrayList<Book?>
        adapter.notifyDataSetChanged()
    }
}
