package com.flatcode.littlebooksadmin.filter

import android.widget.Filter
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.ui.book.StaggeredBookAdapter

class StaggeredFilter(
    private val adapter: StaggeredBookAdapter
) : Filter() {
    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var query = constraint
        val results = FilterResults()
        val filterList = adapter.unfilteredList
        if (query != null && query.isNotEmpty()) {
            query = query.toString().uppercase()
            val filteredModels = ArrayList<Book>()
            for (item in filterList) {
                if (item.title?.uppercase()?.contains(query) == true) {
                    filteredModels.add(item)
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

    @Suppress("UNCHECKED_CAST")
    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        adapter.submitFilteredList(results.values as? List<Book>)
    }
}
