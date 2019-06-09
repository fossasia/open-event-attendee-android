package org.fossasia.openevent.general.feedback

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_feedback.view.commentTv
import kotlinx.android.synthetic.main.item_feedback.view.ratingBar

const val MAX_COMMENT_LINE = 3

class FeedbackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(feedback: Feedback) {
        itemView.commentTv.text = feedback.comment
        itemView.ratingBar.rating = feedback.rating?.toFloat() ?: 0f

        itemView.commentTv.setOnClickListener {
            itemView.commentTv.maxLines =
                if (itemView.commentTv.maxLines == MAX_COMMENT_LINE) Int.MAX_VALUE else MAX_COMMENT_LINE
        }
    }
}
