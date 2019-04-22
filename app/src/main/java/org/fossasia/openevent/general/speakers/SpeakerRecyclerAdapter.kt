package org.fossasia.openevent.general.speakers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.common.SpeakerClickListener

class SpeakerRecyclerAdapter : RecyclerView.Adapter<SpeakerViewHolder>() {
    private val speakersList = ArrayList<Speaker>()
    var onSpeakerClick: SpeakerClickListener? = null

    fun addAll(speakersList: List<Speaker>) {
        if (speakersList.isNotEmpty())
            this.speakersList.clear()
        this.speakersList.addAll(speakersList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpeakerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_speaker, parent, false)
        return SpeakerViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpeakerViewHolder, position: Int) {
        val speaker = speakersList[position]
        holder.apply {
            bind(speaker)
            speakerClickListener = onSpeakerClick
        }
    }

    override fun getItemCount(): Int {
        return speakersList.size
    }
}
