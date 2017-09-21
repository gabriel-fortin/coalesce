package com.example.g14.coalesce.app.scratch

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import com.example.g14.coalesce.app.R

class BambooViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val prioTextTV = view.findViewById(R.id.priorityText) as TextView
    private val titleTextTV = view.findViewById(R.id.titleText) as TextView
    private val checkBoxCB = view.findViewById(R.id.buyingStateBox) as CheckBox
    val itemData = view.findViewById(R.id.itemData) as ViewGroup


    init {
        view.findViewById(R.id.moveToSortButton).setOnClickListener {
            Toast.makeText(view.context, "sort CLICK", Toast.LENGTH_SHORT).show()
        }
        view.findViewById(R.id.removeButton).setOnClickListener {
            Toast.makeText(view.context, "remove CLICK", Toast.LENGTH_SHORT).show()
        }
    }


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
