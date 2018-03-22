package com.example.g14.coalesce.app.shoppinglist

import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent

/**
 * Created by Gabriel Fortin
 *
 *      WORK IN PROGRESS!
 *
 */
open class WipGestureListener : GestureDetector.SimpleOnGestureListener() {
    companion object {
        val tag: String = WipGestureListener::class.java.simpleName + " ~~"
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        Log.v(tag, "onSingleTapUp")
        return super.onSingleTapUp(e)
    }

    override fun onDown(e: MotionEvent?): Boolean {
        Log.v(tag, "onDown")
        return super.onDown(e)
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        Log.v(tag, "onFling")
        return super.onFling(e1, e2, velocityX, velocityY)
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        Log.v(tag, "onDoubleTap")
        return super.onDoubleTap(e)
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        Log.v(tag, "onScroll")
        return super.onScroll(e1, e2, distanceX, distanceY)
    }

    override fun onContextClick(e: MotionEvent?): Boolean {
        Log.v(tag, "onContextClick")
        return super.onContextClick(e)
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        Log.v(tag, "onSingleTapConfirmed")
        return super.onSingleTapConfirmed(e)
    }

    override fun onShowPress(e: MotionEvent?) {
        Log.v(tag, "onShowPress")
        super.onShowPress(e)
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        Log.v(tag, "onDoubleTapEvent")
        return super.onDoubleTapEvent(e)
    }

    override fun onLongPress(e: MotionEvent?) {
        Log.v(tag, "onLongPress")
        super.onLongPress(e)
    }
}
