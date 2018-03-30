package com.example.g14.coalesce.app.shoppinglist

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent

/**
 * Created by Gabriel Fortin
 */
class ReorderGestureListener(
        private val viewHolder: RecyclerView.ViewHolder,
        private val callback: ReorderEventCallback)
    : GestureDetector.SimpleOnGestureListener() {
    companion object {
        val tag = ReorderGestureListener::class.java.simpleName
    }

    override fun onDown(e: MotionEvent?): Boolean {
        Log.d(tag, "onDown")
        callback.moveStarted(viewHolder)
        return true
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        Log.d(tag, "onDoubleTap")
        // TODO: this event is not triggered because the ItemTouchHelper attached to the
        //      RecyclerView is intercepting events
        callback.moveToTop(viewHolder)
        return true
    }
}
