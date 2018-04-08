package com.example.g14.coalesce.app.shoppinglist.internal

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.example.g14.coalesce.app.R
import kotlin.math.max
import kotlin.math.min

/**
 * For a view in a recycler's item view this class exposes:
 *  - the view itself, or
 *  - fields of interest from that view.
 *
 * This class does not (should not) set any listeners or other logic; just expose its content.
 */
// TODO: update javadoc
class ShoppingsViewHolder(view: View)
    : RecyclerView.ViewHolder(view), SwipeItemForOptions.ItemHelper {
    companion object {
        val tag: String = ShoppingsViewHolder::class.java.simpleName
    }

    private val prioTextTV = view.findViewById(R.id.priorityText) as TextView
    private val titleTextTV = view.findViewById(R.id.titleText) as TextView
    private val checkBoxCB = view.findViewById(R.id.buyingStateBox) as CheckBox
    private val underbuttonsEnding = view.findViewById(R.id.buttonsEnding) as View

    val frontView = view.findViewById(R.id.itemData) as ViewGroup
    val underMenu = view.findViewById(R.id.underMenu) as ViewGroup

    val reorderUnderbutton = view.findViewById(R.id.reorderButton) as ImageButton
    val deleteUnderbutton = view.findViewById(R.id.removeButton) as ImageButton

    private var initialTranslationX = 0f

    init {
        // TODO: delete toast when real implementation is in place
        deleteUnderbutton.setOnClickListener {
            val msg = "remove CLICK"
            Log.d(tag, msg)
            Toast.makeText(view.context, msg, Toast.LENGTH_SHORT).show()
        }

        // TODO: extract as class (nested class in this class?)
        val gestureDetector = GestureDetector(view.context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent?): Boolean {
                beginSwiping(view)
                return super.onDown(e)
            }

            override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                // when scroll detected: prevent ItemTouchHelper on the RecyclerView to mess with our swipe
                view.parent.requestDisallowInterceptTouchEvent(true)

                return continueSwiping(e1, e2, view)
            }
        })
        view.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_UP) {
                finishSwiping()
            }
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    fun reset() {
        frontView.translationX = 0f
        underMenu.visibility = View.INVISIBLE

        priorityText = "×"
        titleText = "×"
        checkBox = false
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

    override fun getViewToSwipe(): ViewGroup = frontView

    override fun getMaxSwipeDistance(): Float = underbuttonsEnding.left.toFloat()

    private fun beginSwiping(view: View) {
        Log.i(tag, "beginSwiping   initTranX: %4.1f".format(frontView.translationX))
        initialTranslationX = frontView.translationX
    }

    private fun continueSwiping(e1: MotionEvent, e2: MotionEvent, view: View): Boolean {
        Log.v(tag, "continueSwiping   "
                + "diff: %3.1f     (%4.1f - %4.1f)"
                .format(e2.x - e1.x, e1.x, e2.x))

        underMenu.visibility = View.VISIBLE
        val swipeDistance = initialTranslationX + e2.x - e1.x
        frontView.translationX = max(0f, min(600f, swipeDistance))

        return true
    }

    private fun finishSwiping() {
        Log.i(tag, "finishSwiping")
        if (frontView.translationX > 0.5 * getMaxSwipeDistance()) {
            frontView.translationX = getMaxSwipeDistance()
        } else {
            frontView.translationX = 0f
            // underMenu shouldn't be clickable if not visible
            underMenu.visibility = View.INVISIBLE
        }
    }
}
