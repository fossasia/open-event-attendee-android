package org.fossasia.openevent.general.sessions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.common.SessionClickListener

class SessionRecyclerAdapter : RecyclerView.Adapter<SessionViewHolder>() {
    private val sessionList = ArrayList<Session>()
    var onSessionClick: SessionClickListener? = null

    fun addAll(sessionList: List<Session>) {
        if (sessionList.isNotEmpty())
            this.sessionList.clear()
        this.sessionList.addAll(sessionList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_session, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessionList[position]

        holder.apply {
            bind(session)
            sessionClickListener = onSessionClick
        }
    }

    override fun getItemCount(): Int {
        return sessionList.size
    }
}
