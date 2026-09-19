package com.flatcode.littlebooksadmin.filter

import android.widget.Filter
import com.flatcode.littlebooksadmin.model.User
import com.flatcode.littlebooksadmin.ui.user.PublisherAdapter

class PublisherFilter(
    private var adapter: PublisherAdapter
) : Filter() {
    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var charSequence = constraint
        val results = FilterResults()
        if (charSequence != null && charSequence.isNotEmpty()) {
            charSequence = charSequence.toString().uppercase()
            val filteredModels = ArrayList<User>()
            for (item in adapter.unfilteredList) {
                if (item.username?.uppercase()?.contains(charSequence) == true) {
                    filteredModels.add(item)
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        } else {
            results.count = adapter.unfilteredList.size
            results.values = adapter.unfilteredList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        adapter.submitFilteredList(results.values as? List<User>)
    }
}
