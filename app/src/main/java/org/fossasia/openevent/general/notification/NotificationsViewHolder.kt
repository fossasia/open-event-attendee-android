package org.fossasia.openevent.general.notification

import android.text.method.LinkMovementMethod
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_card_notification.view.*
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.utils.stripHtml

class NotificationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(
        notification: Notification

    ) {
        itemView.title.text = notification.title
        itemView.message.text = notification.message.stripHtml()
        itemView.message.movementMethod = LinkMovementMethod.getInstance()
        notification.receivedAt?.let {
            val dayDiff = EventUtils.getDayDifferenceFromToday(it)
            val formattedDateTime = EventUtils.getEventDateTime(it, null)
            itemView.time.text = when (dayDiff) {
                0L -> EventUtils.getFormattedTime(formattedDateTime)
                in 1..6 -> EventUtils.getFormattedWeekDay(formattedDateTime)
                else -> EventUtils.getFormattedDateWithoutWeekday(formattedDateTime)
            }
        }
    }
}
