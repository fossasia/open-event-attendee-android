package org.fossasia.openevent.general.sessions

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_session.view.mircolocation
import kotlinx.android.synthetic.main.item_session.view.sessionType
import kotlinx.android.synthetic.main.item_session.view.sessiontime
import kotlinx.android.synthetic.main.item_session.view.shortAbstract
import kotlinx.android.synthetic.main.item_session.view.title
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.utils.nullToEmpty
import org.fossasia.openevent.general.utils.stripHtml

class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(session: Session) {
        itemView.title.text = session.title
        session.sessionType.let {
            itemView.sessionType.text = it?.name
        }
        session.microlocation.let {
            itemView.mircolocation.text = it?.name
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
    }
}
