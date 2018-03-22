package com.example.g14.coalesce.app.shoppinglist.internal

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import com.example.g14.coalesce.app.R

class ShoppingsViewHolder(view: View) : RecyclerView.ViewHolder(view), SwipeItemForOptions.ItemHelper {
    companion object {
        val tag: String = ShoppingsViewHolder::class.java.simpleName
    }

    private val prioTextTV = view.findViewById(R.id.priorityText) as TextView
    private val titleTextTV = view.findViewById(R.id.titleText) as TextView
    private val checkBoxCB = view.findViewById(R.id.buyingStateBox) as CheckBox
    private val buttonsEnding = view.findViewById(R.id.buttonsEnding) as View
    val itemData = view.findViewById(R.id.itemData) as ViewGroup


    init {
        view.findViewById<View>(R.id.reorderButton).setOnClickListener {
            val msg = "sort CLICK"
            Log.d(tag, msg)
            Toast.makeText(view.context, msg, Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.removeButton).setOnClickListener {
            val msg = "remove CLICK"
            Log.d(tag, msg)
            Toast.makeText(view.context, msg, Toast.LENGTH_SHORT).show()
        }
    }


    var priorityText: String
        set(value) { prioTextTV.text = value }
        get() = prioTextTV.text.toString()

    var titleText: String
        set(value) { titleTextTV.text = value }
        get() = titleTextTV.text.toString()

    var checkBox: Boolean = false
//    var checkBox: Boolean
        set(value) {
            checkBoxCB.isChecked = value
        }
//        get() = checkBoxCB.isChecked

    override fun getViewToSwipe(): ViewGroup = itemData

    override fun getMaxSwipeDistance(): Float = buttonsEnding.left.toFloat()
}
