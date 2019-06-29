package org.fossasia.openevent.general.search.type

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.event_type_list.view.typeTextView
import org.fossasia.openevent.general.R

class SearchTypeAdapter : RecyclerView.Adapter<SearchTypeViewHolder>() {
    private val types = ArrayList<String>()
    private var checkedTypePosition = -1
    private var listener: TypeClickListener? = null

    fun addAll(typeList: List<String>) {
        if (types.isNotEmpty()) types.clear()
        types.addAll(typeList)
    }

    fun setListener(typeListener: TypeClickListener) {
        listener = typeListener
    }

    fun setCheckTypePosition(pos: Int) {
        checkedTypePosition = pos
    }

    override fun onBindViewHolder(holder: SearchTypeViewHolder, position: Int) {
        holder.bind(types[position], checkedTypePosition == position, listener)
    }

    override fun getItemCount(): Int {
        return types.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchTypeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.event_type_list, parent, false)
        return SearchTypeViewHolder(view)
    }
}

interface TypeClickListener {
    fun onClick(chosenType: String)
}

class SearchTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(typeValue: String, isChecked: Boolean, listener: TypeClickListener?) {
        val typeView = itemView.typeTextView
        if (typeView is CheckedTextView) {
            typeView.text = typeValue
            if (isChecked) {
                typeView.setCheckMarkDrawable(R.drawable.ic_checked)
                typeView.isChecked = true
            } else {
                typeView.checkMarkDrawable = null
                typeView.isChecked = false
            }
            if (listener != null) {
                typeView.setOnClickListener {
                    listener.onClick(typeView.text.toString())
                }
            }
        }
    }
}
