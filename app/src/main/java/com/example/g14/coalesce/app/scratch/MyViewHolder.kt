package com.example.g14.coalesce.app.scratch

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import com.example.g14.coalesce.app.R

class BambooViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val prioTextTV = view.findViewById(R.id.priorityText) as TextView
    private val titleTextTV = view.findViewById(R.id.titleText) as TextView
    private val checkBoxCB = view.findViewById(R.id.buyingStateBox) as CheckBox

    var priorityText: String
        set(value) { prioTextTV.text = value }
        get() = prioTextTV.text.toString()

    var titleText: String
        set(value) { titleTextTV.text = value }
        get() = titleTextTV.text.toString()

    var checkBox: Boolean = false
        set(value) {
            checkBoxCB.isChecked = value
        }
}
