package com.example.g14.coalesce.app.shoppinglist

import android.graphics.Canvas
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.ViewGroup

/**
 * To use this class your view holder must implement SwipeItemForOptions.ItemHelper
 */
class SwipeItemForOptions : ItemTouchHelper.Callback() {
    companion object {
        val tag: String = SwipeItemForOptions::class.java.simpleName
    }

    /**
     * Interface to be implemented by the view holder
     */
    interface ItemHelper {
        fun getViewToSwipe(): ViewGroup
        fun getMaxSwipeDistance(): Float
    }


    private val swipeThresholdRatio = .5f
    private lateinit var recView: RecyclerView
    private var userStartsSwiping = false
    private var initialDx = 0f
    private var initialObservedX = 0f
    private var swipeThresholdValue = 0.1f  // beginning value; never used

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder?): Int {
        recView = recyclerView
        return makeMovementFlags(0, ItemTouchHelper.RIGHT)
    }

    override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
        throw RuntimeException("not implemented")
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        Log.i(tag, "swiped")
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder?): Float {
        Log.d(tag, "get swipe threshold  ->  $swipeThresholdValue")
        return swipeThresholdValue
    }

    override fun onChildDraw(c: Canvas?, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                             dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) return

        if (viewHolder !is ItemHelper) {
            Log.w(tag, "view holder doesn't implement required interface")
            return
        }

        if (userStartsSwiping) {
            userStartsSwiping = false
            initialDx = dX
            swipeThresholdValue = swipeThresholdRatio * viewHolder.getMaxSwipeDistance() / recView.width
            if (dX == 0f) {
                initialObservedX = 0f
            } else {
                initialObservedX = viewHolder.getMaxSwipeDistance()
                swipeThresholdValue = 1 - swipeThresholdValue
            }
        }

        val ourDx = initialObservedX + (dX - initialDx)
        val ourDxLimited = Math.max(0f, Math.min(viewHolder.getMaxSwipeDistance(), ourDx))

        viewHolder.getViewToSwipe().translationX = ourDxLimited
        Log.v(tag, "dX:  ${dX.toInt()}   $isCurrentlyActive   initDx:  $initialDx" +
                "   ourDx:  $ourDx   eff tran X:  $ourDxLimited")
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            userStartsSwiping = true
        }
        val state = when (actionState) {
            ItemTouchHelper.ACTION_STATE_SWIPE -> "swipe"
            ItemTouchHelper.ACTION_STATE_DRAG -> "drag"
            ItemTouchHelper.ACTION_STATE_IDLE -> "idle"
            else -> "???"
        }
        Log.i(tag, "on-selected-change   $state")
    }

}
