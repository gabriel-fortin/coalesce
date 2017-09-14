package com.example.g14.coalesce.app.scratch

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import com.example.g14.coalesce.app.R

class BambooViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val prioText = view.findViewById(R.id.priorityText) as TextView
    private val titleText = view.findViewById(R.id.titleText) as TextView
    private val checkbox = view.findViewById(R.id.buyingStateBox) as CheckBox

    fun setPriorityText(text: String) {
        prioText.text = text
    }

    fun setTitleText(text: String) {
        titleText.text = text
    }

    var checkBox: Boolean = false
        set(value) {
            checkbox.isChecked = value
        }
}
