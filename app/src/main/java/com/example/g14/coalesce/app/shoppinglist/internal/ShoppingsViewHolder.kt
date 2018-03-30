package com.example.g14.coalesce.app.shoppinglist.internal

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.example.g14.coalesce.app.R

/**
 * For a view in a recycler's item view this class exposes:
 *  - the view itself, or
 *  - fields of interest from that view.
 *
 * This class does not (should not) set any listeners or other logic; just expose its content.
 */
class ShoppingsViewHolder(view: View)
    : RecyclerView.ViewHolder(view), SwipeItemForOptions.ItemHelper {
    companion object {
        val tag: String = ShoppingsViewHolder::class.java.simpleName
    }

    private val prioTextTV = view.findViewById(R.id.priorityText) as TextView
    private val titleTextTV = view.findViewById(R.id.titleText) as TextView
    private val checkBoxCB = view.findViewById(R.id.buyingStateBox) as CheckBox
    private val underbuttonsEnding = view.findViewById(R.id.buttonsEnding) as View

    val itemData = view.findViewById(R.id.itemData) as ViewGroup

    val reorderUnderbutton = view.findViewById(R.id.reorderButton) as ImageButton
    val deleteUnderbutton = view.findViewById(R.id.removeButton) as ImageButton

    init {
        reorderUnderbutton.setOnClickListener {
            val msg = "sort CLICK"
            Log.d(tag, msg)
            Toast.makeText(view.context, msg, Toast.LENGTH_SHORT).show()
        }
        // TODO: delete toast when real implementation is in place
        deleteUnderbutton.setOnClickListener {
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
        set(value) {
            checkBoxCB.isChecked = value
        }

    override fun getViewToSwipe(): ViewGroup = itemData

    override fun getMaxSwipeDistance(): Float = underbuttonsEnding.left.toFloat()
}
