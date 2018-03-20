package com.example.g14.coalesce.app.shoppinglist.internal

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

    var dummyCounter: Byte = 0


    private val swipeThresholdRatio = .5f
    private lateinit var recView: RecyclerView
    private var userStartsSwiping = -1
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

//    override fun isItemViewSwipeEnabled(): Boolean = true

    override fun onChildDraw(c: Canvas?, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                             dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) return

        if (viewHolder !is ItemHelper) {
            Log.w(tag, "view holder doesn't implement required interface")
            return
        }

        if (userStartsSwiping == 0) {
            userStartsSwiping++
            initialDx = dX
            swipeThresholdValue = swipeThresholdRatio * viewHolder.getMaxSwipeDistance() / recView.width
            if (dX == 0f) {
                initialObservedX = 0f
            } else {
                initialObservedX = viewHolder.getMaxSwipeDistance()
                swipeThresholdValue = 1 - swipeThresholdValue
            }
        } else if (userStartsSwiping == 1) {
            userStartsSwiping++
            if (!isCurrentlyActive) {
                // TODO: should ignore input from now on until 'onSwiped' is called
            }
        }

        val ourDx = initialObservedX + (dX - initialDx)
        val ourDxLimited = Math.max(0f, Math.min(viewHolder.getMaxSwipeDistance(), ourDx))

        viewHolder.getViewToSwipe().translationX = ourDxLimited
        Log.v(tag, "[${dummyCounter++}]  dX:${"%4d".format(dX.toInt())}   " +
                "act: ${if (isCurrentlyActive) "y" else "n"}   initDx:  $initialDx" +
                "   ourDx:  ${"%.1f".format(ourDx)}   tran X:  $ourDxLimited")
    }

    override fun onChildDrawOver(c: Canvas?, recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            userStartsSwiping = 0
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
