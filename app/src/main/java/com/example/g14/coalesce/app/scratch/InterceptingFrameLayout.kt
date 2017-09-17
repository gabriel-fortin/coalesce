package com.example.g14.coalesce.app.scratch

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.recycleritem_shopping_constraintlayout.view.*

/**
 * Created by Gabriel Fortin
 */

class InterceptingFrameLayout : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    data class Point(val x: Float, val y: Float) {
        fun isInBall(radius: Float, pX: Float, pY: Float): Boolean {
            val xxx = (pX - x) * (pX - x) + (pY - y) * (pY - y)
            Log.v("FFF_2", "inBall:  ${xxx/100}  <  ${radius*radius/100}  ?")
            return xxx < radius * radius
        }
        fun isRatherHorizontal(pX: Float, pY: Float): Boolean {
            Log.v("FFF_2", "(${x.toInt()}, ${y.toInt()}) ~~ (${pX.toInt()}, ${pY.toInt()})")
            return Math.abs(pX - x) > Math.abs(pY - y)
        }
    }

    init {
    }

    var startingPoint: Point? = null
    var swipingStarted = false
    var swipingAbandoned = false
    var swipingBeginsNow = false
    var initialTranslationX = 0f

    private fun startTrackingMovements(ev: MotionEvent) {
        startingPoint = Point(ev.x, ev.y)
    }

    private fun stopTrackingMovements(ev: MotionEvent) {
        swipingStarted = false
        swipingAbandoned = false
        startingPoint = null
    }

    private fun consumeMoveEvent(ev: MotionEvent) {
        if (swipingAbandoned) return

        if (!swipingStarted) {
            if (startingPoint!!.isInBall(50f, ev.x, ev.y)) {
                Log.v("FFF_2", "should move?  not yet")
                return
            }

            if (startingPoint!!.isRatherHorizontal(ev.x, ev.y)) {
                Log.d("FFF_2", "should move?  YES")
                swipingStarted = true
            } else {
                Log.d("FFF_2", "should move?  no")
                swipingAbandoned = true
                return
            }
            swipingBeginsNow = true
        } else {
            swipingBeginsNow = false
        }

        performSwiping(ev)
    }

    private fun performSwiping(ev: MotionEvent) {
        val userTransX = ev.x - startingPoint!!.x
        Log.v("FFF_2", "userTranslationX: $userTransX")

        if (swipingBeginsNow) {
            initialTranslationX = itemData.translationX
        }

        val proposedTransX = initialTranslationX + userTransX

        itemData.translationX = Math.max(0f, proposedTransX)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                Log.d("FFF_1", "intercept: action down")
                startTrackingMovements(ev)
            }
            MotionEvent.ACTION_UP -> {
                Log.d("FFF_1", "intercept: action up")
                stopTrackingMovements(ev)
            }
            MotionEvent.ACTION_CANCEL -> {
                Log.d("FFF_1", "intercept: action cancel")
                stopTrackingMovements(ev)
            }
            MotionEvent.ACTION_MOVE -> {
                Log.v("FFF_1", "intercept: action move")
                consumeMoveEvent(ev)
            }
        }
        return false
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                Log.i("FFF_1", "touch: action down")
                // tracking already started in 'onInterceptTouchEvent'
            }
            MotionEvent.ACTION_UP -> {
                Log.i("FFF_1", "touch: action up")
                stopTrackingMovements(ev)
            }
            MotionEvent.ACTION_CANCEL -> {
                Log.i("FFF_1", "touch: action cancel")
                stopTrackingMovements(ev)
            }
            MotionEvent.ACTION_MOVE -> {
                Log.v("FFF_1", "touch: action move")
                consumeMoveEvent(ev)
            }
        }
        return true
    }

}
