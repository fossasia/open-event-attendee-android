package org.fossasia.openevent.general.speakers

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_speaker.view.shortBioTv
import kotlinx.android.synthetic.main.item_speaker.view.speakerImgView
import kotlinx.android.synthetic.main.item_speaker.view.speakerNameTv
import kotlinx.android.synthetic.main.item_speaker.view.speakerOrgTv
import org.fossasia.openevent.general.CircleTransform
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.common.SpeakerClickListener
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.nullToEmpty
import org.fossasia.openevent.general.utils.stripHtml
import timber.log.Timber

class SpeakerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var speakerClickListener: SpeakerClickListener? = null

    fun bind(speaker: Speaker) {
        itemView.speakerNameTv.text = speaker.name
        itemView.speakerOrgTv.text = speaker.organisation
        val shortBio = speaker.shortBiography.nullToEmpty().stripHtml()
        when (shortBio.isNullOrBlank()) {
            true -> itemView.shortBioTv.isVisible = false
            false -> itemView.shortBioTv.text = shortBio
        }

        Picasso.get()
            .load(speaker.photoUrl)
            .placeholder(Utils.requireDrawable(itemView.context, R.drawable.ic_account_circle_grey))
            .transform(CircleTransform())
            .into(itemView.speakerImgView)

        itemView.setOnClickListener {
            speakerClickListener?.onClick(speaker.id)
                ?: Timber.e("Speaker Click listener on ${this::class.java.canonicalName} is null")
        }
    }
}
