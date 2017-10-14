package com.example.g14.coalesce.app.scratch

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.example.g14.coalesce.app.R
import kotlinx.android.synthetic.main.activity_scratch.*

val tag = ScratchActivity::class.java.simpleName

class ScratchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scratch)

        shoppingRecycler.adapter = BambooAdapter(SAMPLE_DATA, this)
        shoppingRecycler.layoutManager = LinearLayoutManager(this)
                .apply { orientation = LinearLayoutManager.VERTICAL }

        shoppingRecycler.addItemDecoration(BambooItemDecor())
        ItemTouchHelper(SwipeItemForOptions()).attachToRecyclerView(shoppingRecycler)
    }


    class SwipeItemForOptions: ItemTouchHelper.Callback() {

        /**
         * Interface to be implemented by the view holder
         */
        interface ItemHelper {
            fun getViewToSwipe(): ViewGroup
            fun getMaxSwipeDistance(): Float
        }

        val desiredSwipeThreshold = .5f
        lateinit var recView: RecyclerView
        var userStartsSwiping = false
        var initialDx = 0f
        var initialObservedX = 0f
        var swipeThreshold = 0.1f  // beginning value; never used

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
            Log.d(tag, "get swipe threshold  ->  $swipeThreshold")
            return swipeThreshold
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
                swipeThreshold = desiredSwipeThreshold * viewHolder.getMaxSwipeDistance() / recView.width
                if (dX == 0f) {
                    initialObservedX = 0f
                } else {
                    initialObservedX = viewHolder.getMaxSwipeDistance()
                    swipeThreshold = 1 - swipeThreshold
                }
            }

            val ourDx = initialObservedX + (dX - initialDx)
            val ourDxLimited = Math.max(0f, Math.min(viewHolder.getMaxSwipeDistance(), ourDx))

            viewHolder.getViewToSwipe().translationX = ourDxLimited
            Log.v("HHH", "dX:  ${dX.toInt()}   $isCurrentlyActive   initDx:  $initialDx" +
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
            Log.i("HHH", "on-selected-change   $state")
        }

    }


    inner class BambooItemDecor : RecyclerView.ItemDecoration() {
        val paint = Paint().apply {
            color = Color.MAGENTA
        }

        override fun onDrawOver(c: Canvas, parent: RecyclerView?, state: RecyclerView.State?) {
        }

        override fun getItemOffsets(outRect: Rect, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
            outRect.top = 13
            outRect.bottom = 25
//            outRect.left = 130
//            outRect.right = 30
        }

        override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            Log.i("AAA", "decoration ON DRAW")
            c.save()
//            c.drawARGB(150, 0, 0, 0)

            val childCount = parent.childCount
            Log.i("AAA", "child count: $childCount")

            for (i in 0..childCount-1) {
                Log.d("AAA", "child $i")
                val child = parent.getChildAt(i)
                val prioTxt = (parent.findContainingViewHolder(child) as BambooViewHolder?)
                        ?.priorityText
                        ?: "NO VIEW HOLDER"
                Log.d("AAA", "priority text: $prioTxt")

                if (prioTxt == "1") continue

                val bounds = Rect()
                        .apply { parent.getDecoratedBoundsWithMargins(child, this) }
                val left = 10
                val top = bounds.top + 1
                val right = left + 200
                val bottom = bounds.bottom - 1

                val rect = Rect(left, top, right, bottom)
                c.drawRect(rect, paint)
            }

            c.restore()
        }
    }


    data class DataItem(val title: String, val prio: Int, val bought: Boolean)

    val SAMPLE_DATA: List<DataItem> = listOf(
            DataItem("papryka", 1, false),
            DataItem("ogórek", 1, false),
            DataItem("twaróg", 2, false),
            DataItem("cheddar", 1, true),
            DataItem("seler naciowy", 1, true),
            DataItem("single cream", 2, false),
            DataItem("mięso mielone", 1, true),
            DataItem("kalafior", 1, false),
            DataItem("brokuł", 3, false),
            DataItem("masło orzechowe", 1, false)
    )

}
