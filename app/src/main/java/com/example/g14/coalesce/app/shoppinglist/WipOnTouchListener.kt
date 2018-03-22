package com.example.g14.coalesce.app.shoppinglist

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent

/**
 * Created by Gabriel Fortin
 */
class WipOnTouchListener(context: Context) : RecyclerView.OnItemTouchListener {
    companion object {
        val tag : String = WipOnTouchListener::class.java.simpleName + "%%%"
    }

    private val gestureDetector = GestureDetector(context, WipGestureListener())

    override fun onTouchEvent(rv: RecyclerView?, e: MotionEvent?) {
        Log.v(tag, "onTouchEvent")
    }

    override fun onInterceptTouchEvent(rv: RecyclerView?, e: MotionEvent?): Boolean {
        Log.v(tag, "onInterceptTouchEvent")
        gestureDetector.onTouchEvent(e)
        return false
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        Log.v(tag, "onRequestDisallowInterceptTouchEvent")

    }
}
