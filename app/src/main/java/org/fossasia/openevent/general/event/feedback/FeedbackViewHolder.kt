package org.fossasia.openevent.general.event.feedback

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_feedback.view.commentTv
import kotlinx.android.synthetic.main.item_feedback.view.ratingBar

class FeedbackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(feedback: Feedback) {
        itemView.commentTv.text = feedback.comment
        itemView.ratingBar.rating = feedback.rating?.toFloat() ?: 0f
    }
}
