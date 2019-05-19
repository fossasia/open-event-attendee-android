package org.fossasia.openevent.general.sponsor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.fossasia.openevent.general.R

const val MAXIMUM_PREVIEW_SPONSOR = 8

class SponsorRecyclerAdapter : RecyclerView.Adapter<SponsorViewHolder>() {
    private val sponsorList = ArrayList<Sponsor>()
    var onSponsorClick: SponsorClickListener? = null

    fun addAll(newSponsors: List<Sponsor>) {
        if (sponsorList.isNotEmpty()) sponsorList.clear()
        sponsorList.addAll(SponsorUtil.sortSponsorByLevel(newSponsors))
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: SponsorViewHolder, position: Int) {
        holder.apply {
            bind(sponsorList[position], sponsorList.size > MAXIMUM_PREVIEW_SPONSOR &&
                position == MAXIMUM_PREVIEW_SPONSOR - 1, sponsorList.size)
            sponsorClickListener = onSponsorClick
        }
    }

    override fun getItemCount(): Int {
        return if (sponsorList.size > MAXIMUM_PREVIEW_SPONSOR) MAXIMUM_PREVIEW_SPONSOR else sponsorList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SponsorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sponsor, parent, false)
        return SponsorViewHolder(view)
    }
}
