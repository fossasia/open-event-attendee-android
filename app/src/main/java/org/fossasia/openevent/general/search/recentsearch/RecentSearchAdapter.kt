package org.fossasia.openevent.general.search.recentsearch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.fossasia.openevent.general.R

class RecentSearchAdapter : RecyclerView.Adapter<RecentSearchViewHolder>() {
    private val recentSearchesList = mutableListOf<Pair<String, String>>()
    private var listener: RecentSearchListener? = null

    fun addAll(recentSearches: List<Pair<String, String>>) {
        if (recentSearchesList.isNotEmpty()) recentSearchesList.clear()
        recentSearchesList.addAll(recentSearches)
        notifyDataSetChanged()
    }

    fun setListener(listener: RecentSearchListener) {
        this.listener = listener
        notifyDataSetChanged()
    }

    fun removeRecentSearchAt(position: Int) {
        recentSearchesList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun addRecentSearch(position: Int, searches: Pair<String, String>) {
        recentSearchesList.add(position, searches)
        notifyItemInserted(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recent_search, parent, false)
        return RecentSearchViewHolder(view)
    }

    override fun getItemCount(): Int = recentSearchesList.size

    override fun onBindViewHolder(holder: RecentSearchViewHolder, position: Int) {
        holder.bind(recentSearchesList[position], position, listener)
    }
}

interface RecentSearchListener {
    fun removeSearch(position: Int, recentSearch: Pair<String, String>)
    fun clickSearch(searchText: String, searchLocation: String)
}
