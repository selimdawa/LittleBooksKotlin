package com.flatcode.littlebooksadmin.filter

import android.widget.Filter
import com.flatcode.littlebooksadmin.model.User
import com.flatcode.littlebooksadmin.ui.user.TopPublisherAdapter

class TopPublisherFilter(
    private val adapter: TopPublisherAdapter
) : Filter() {
    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var query = constraint
        val results = FilterResults()
        val filterList = adapter.unfilteredList
        if (query != null && query.isNotEmpty()) {
            query = query.toString().uppercase()
            val filteredModels = ArrayList<User?>()
            for (item in filterList) {
                if (item?.username?.uppercase()?.contains(query) == true) {
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
        adapter.submitFilteredList(results.values as? List<User?>)
    }
}
