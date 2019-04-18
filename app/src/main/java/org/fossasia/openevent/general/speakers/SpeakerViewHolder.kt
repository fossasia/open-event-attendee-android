package org.fossasia.openevent.general.speakers

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_speaker.view.shortBioTv
import kotlinx.android.synthetic.main.item_speaker.view.speakerImgView
import kotlinx.android.synthetic.main.item_speaker.view.speakerNameTv
import kotlinx.android.synthetic.main.item_speaker.view.speakerOrgTv
import org.fossasia.openevent.general.CircleTransform
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.stripHtml

class SpeakerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(speaker: Speaker) {
        itemView.speakerNameTv.text = speaker.name
        itemView.speakerOrgTv.text = speaker.organisation
        itemView.shortBioTv.text = speaker.shortBiography?.stripHtml()

        Picasso.get()
            .load(speaker.photoUrl)
            .placeholder(Utils.requireDrawable(itemView.context, R.drawable.ic_account_circle_grey))
            .transform(CircleTransform())
            .into(itemView.speakerImgView)
    }
}
