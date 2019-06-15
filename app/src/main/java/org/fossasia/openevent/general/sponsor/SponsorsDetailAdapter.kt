package org.fossasia.openevent.general.sponsor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_sponsor_detail.view.sponsorDetailURL
import kotlinx.android.synthetic.main.item_sponsor_detail.view.sponsorDetailLogo
import kotlinx.android.synthetic.main.item_sponsor_detail.view.sponsorDetailDescription
import kotlinx.android.synthetic.main.item_sponsor_detail.view.sponsorDetailType
import kotlinx.android.synthetic.main.item_sponsor_detail.view.sponsorDetailName
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils

class SponsorsDetailAdapter : RecyclerView.Adapter<SponsorsDetailViewHolder>() {

    private val sponsorList = mutableListOf<Sponsor>()
    lateinit var onURLClickListener: SponsorURLClickListener

    fun addAll(newSponsors: List<Sponsor>) {
        if (sponsorList.isNotEmpty()) sponsorList.clear()
        sponsorList.addAll(SponsorUtil.sortSponsorByLevel(newSponsors))
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SponsorsDetailViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_sponsor_detail, parent, false)
        return SponsorsDetailViewHolder(itemView)
    }

    override fun getItemCount(): Int = sponsorList.size

    override fun onBindViewHolder(holder: SponsorsDetailViewHolder, position: Int) {
        holder.apply {
            bind(sponsorList[position])
            sponsorURLClickListener = onURLClickListener
        }
    }
}

class SponsorsDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    lateinit var sponsorURLClickListener: SponsorURLClickListener

    fun bind(sponsor: Sponsor) {
        Picasso.get()
            .load(sponsor.logoUrl)
            .placeholder(Utils.requireDrawable(itemView.context, R.drawable.ic_account_circle_grey))
            .error(Utils.requireDrawable(itemView.context, R.drawable.ic_account_circle_grey))
            .into(itemView.sponsorDetailLogo)

        itemView.sponsorDetailName.text = sponsor.name
        if (sponsor.type.isNullOrBlank()) {
            itemView.sponsorDetailType.visibility = View.GONE
        } else {
            itemView.sponsorDetailType.text = "Type: ${sponsor.type}"
            itemView.sponsorDetailType.visibility = View.VISIBLE
        }
        if (sponsor.description.isNullOrBlank()) {
            itemView.sponsorDetailDescription.visibility = View.GONE
        } else {
            itemView.sponsorDetailDescription.text = sponsor.description
            itemView.sponsorDetailDescription.visibility = View.VISIBLE
        }

        itemView.sponsorDetailURL.setOnClickListener {
            sponsorURLClickListener.onClick(sponsor.url)
        }
    }
}

interface SponsorURLClickListener {
    fun onClick(sponsorURL: String?)
}

interface SponsorClickListener {
    fun onClick()
}
