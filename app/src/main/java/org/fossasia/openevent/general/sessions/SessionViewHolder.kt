package org.fossasia.openevent.general.sessions

import android.graphics.Color
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_session.view.mircolocation
import kotlinx.android.synthetic.main.item_session.view.sessionType
import kotlinx.android.synthetic.main.item_session.view.sessiontime
import kotlinx.android.synthetic.main.item_session.view.shortAbstract
import kotlinx.android.synthetic.main.item_session.view.title
import kotlinx.android.synthetic.main.item_session.view.trackDetail
import kotlinx.android.synthetic.main.item_session.view.trackText
import kotlinx.android.synthetic.main.item_session.view.trackIcon
import org.fossasia.openevent.general.common.SessionClickListener
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.utils.nullToEmpty
import org.fossasia.openevent.general.utils.stripHtml

class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var sessionClickListener: SessionClickListener? = null

    fun bind(session: Session) {
        itemView.title.text = session.title
        session.sessionType.let {
            itemView.sessionType.text = it?.name
        }
        session.microlocation.let {
            itemView.mircolocation.text = it?.name
        }

        session.track.let {
            if (it == null)
                itemView.trackDetail.visibility = View.GONE
            else {
                itemView.trackText.text = it.name
                itemView.trackIcon.setColorFilter(Color.parseColor(it.color))
            }
        }
        when (session.startsAt.isNullOrBlank()) {
            true -> itemView.sessiontime.isVisible = false
            false -> {
                val formattedDateTime = EventUtils.getEventDateTime(session.startsAt, "")
                val formattedTime = EventUtils.getFormattedTime(formattedDateTime)
                val formattedDate = EventUtils.getFormattedDateShort(formattedDateTime)
                val timezone = EventUtils.getFormattedTimeZone(formattedDateTime)
                itemView.sessiontime.text = "$formattedTime $timezone/ $formattedDate"
            }
        }
        val shortBio = session.shortAbstract.nullToEmpty().stripHtml()
        when (shortBio.isNullOrBlank()) {
            true -> itemView.shortAbstract.isVisible = false
            false -> itemView.shortAbstract.text = shortBio
        }

        itemView.setOnClickListener {
            sessionClickListener?.onClick(session.id)
        }
    }
}
