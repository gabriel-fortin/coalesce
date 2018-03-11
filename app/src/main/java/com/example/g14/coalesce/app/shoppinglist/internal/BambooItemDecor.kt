package com.example.g14.coalesce.app.shoppinglist.internal

import android.graphics.*
import android.support.v7.widget.RecyclerView
import android.view.View

class BambooItemDecor : RecyclerView.ItemDecoration() {
    private val paint = Paint().apply {
        color = Color.MAGENTA
    }

    private val separatorSize = 25

    private val separatorPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
        color = Color.GRAY
        strokeCap = Paint.Cap.ROUND
        pathEffect = DashPathEffect(floatArrayOf(30f, 50f), 0f)
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView?, state: RecyclerView.State?) {
    }

    override fun getItemOffsets(outRect: Rect, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
        val childAdapterPosition = parent?.getChildAdapterPosition(view) ?: throw Exception("no adapter position :(")
//            parent.getChildViewHolder(view)

        // draw separator
        // TODO: draw separator between active an inactive items
        if (childAdapterPosition == 4) {
            outRect.top = separatorSize
        }
//            outRect.top = 13
//            outRect.bottom = 25
//            outRect.left = 130
//            outRect.right = 30
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        c.save()

        val childCount = parent.childCount

        // TODO: draw separator between active an inactive items
        if (childCount >= 4) {
            val child4 = parent.getChildAt(4)
            val child4Bounds = Rect()
            parent.getDecoratedBoundsWithMargins(child4, child4Bounds)
            val verticalPosition = child4Bounds.top + separatorSize/2f + 1f
            val path = Path().apply {
                moveTo(10f, verticalPosition)
                lineTo(child4.width - 20f, verticalPosition)
            }
            c.drawPath(path, separatorPaint)
        }

        // the following is an example of how to draw behind a recycleview item
//            for (i in 0..childCount-1) {
//                Log.d("AAA", "child $i")
//                val child = parent.getChildAt(i)
//                val prioTxt = (parent.findContainingViewHolder(child) as ShoppingsViewHolder?)
//                        ?.priorityText
//                        ?: "NO VIEW HOLDER"
//                Log.d("AAA", "priority text: $prioTxt")
//
//                if (prioTxt == "1") continue
//
//                val bounds = Rect()
//                        .apply { parent.getDecoratedBoundsWithMargins(child, this) }
//                val left = 10
//                val top = bounds.top + 1
//                val right = left + 200
//                val bottom = bounds.bottom - 1
//
//                val rect = Rect(left, top, right, bottom)
//                c.drawRect(rect, paint)
//            }

        c.restore()
    }
}
