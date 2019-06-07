package org.fossasia.openevent.general.sponsor

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_sponsor.view.sponsorImageView
import kotlinx.android.synthetic.main.item_sponsor.view.sponsorTextView
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils

class SponsorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var sponsorClickListener: SponsorClickListener? = null

    fun bind(
        sponsor: Sponsor,
        isLastPreviewSponsor: Boolean,
        sponsorListSize: Int
    ) {

        Picasso.get()
            .load(sponsor.logoUrl)
            .placeholder(Utils.requireDrawable(itemView.context, R.drawable.ic_account_circle_grey))
            .error(Utils.requireDrawable(itemView.context, R.drawable.ic_account_circle_grey))
            .into(itemView.sponsorImageView)

        if (isLastPreviewSponsor) {
            itemView.sponsorImageView.foreground = itemView.context.getDrawable(R.drawable.foreground_black_blur)
            itemView.sponsorTextView.text = "+$sponsorListSize"
        }

        itemView.setOnClickListener {
            sponsorClickListener?.onClick()
        }
    }
}
