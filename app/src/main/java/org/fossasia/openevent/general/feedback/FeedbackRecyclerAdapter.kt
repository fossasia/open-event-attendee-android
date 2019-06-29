package org.fossasia.openevent.general.feedback

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.fossasia.openevent.general.R

const val LIMITED_FEEDBACK_NUMBER = 2

class FeedbackRecyclerAdapter(private val isLimited: Boolean = false) : RecyclerView.Adapter<FeedbackViewHolder>() {
    private val feedbackList = ArrayList<Feedback>()

    fun addAll(feedbackList: List<Feedback>) {
        if (feedbackList.isNotEmpty())
            this.feedbackList.clear()
        this.feedbackList.addAll(feedbackList)
        notifyDataSetChanged()
    }

    fun add(feedback: Feedback) {
        feedbackList.add(0, feedback)
        notifyItemInserted(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feedback, parent, false)
        return FeedbackViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val feedback = feedbackList[position]

        holder.bind(feedback)
    }

    override fun getItemCount(): Int {
        return if (isLimited && feedbackList.size > LIMITED_FEEDBACK_NUMBER)
            LIMITED_FEEDBACK_NUMBER
        else
            feedbackList.size
    }
}
