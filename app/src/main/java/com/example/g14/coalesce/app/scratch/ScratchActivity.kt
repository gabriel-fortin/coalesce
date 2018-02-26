package com.example.g14.coalesce.app.scratch

import android.graphics.*
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.View
import com.example.g14.coalesce.app.R
import com.example.g14.coalesce.app.shoppinglist.ShopItemAdapter
import com.example.g14.coalesce.app.shoppinglist.SwipeItemForOptions
import kotlinx.android.synthetic.main.activity_scratch.*

val tag: String = ScratchActivity::class.java.simpleName

class ScratchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scratch)

        shoppingRecycler.adapter = ShopItemAdapter(SAMPLE_DATA, this)
        shoppingRecycler.layoutManager = LinearLayoutManager(this)
                .apply { orientation = LinearLayoutManager.VERTICAL }

        shoppingRecycler.addItemDecoration(BambooItemDecor())
        ItemTouchHelper(SwipeItemForOptions()).attachToRecyclerView(shoppingRecycler)
    }


    inner class BambooItemDecor : RecyclerView.ItemDecoration() {
        val paint = Paint().apply {
            color = Color.MAGENTA
        }

        override fun onDrawOver(c: Canvas, parent: RecyclerView?, state: RecyclerView.State?) {
        }

        override fun getItemOffsets(outRect: Rect, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
            val childAdapterPosition = parent?.getChildAdapterPosition(view) ?: throw Exception("no adapter position :(")
//            parent.getChildViewHolder(view)
            if (childAdapterPosition == 4) {
                outRect.top = 25
            }
//            outRect.top = 13
//            outRect.bottom = 25
//            outRect.left = 130
//            outRect.right = 30
        }

        override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            Log.i("AAA", "decoration ON DRAW")
            c.save()
//            c.drawARGB(150, 0, 0, 0)

            val childCount = parent.childCount
            Log.i("AAA", "child count: $childCount")

            if (childCount >= 4) {
                val child4 = parent.getChildAt(4)
                val child4Bounds = Rect()
                parent.getDecoratedBoundsWithMargins(child4, child4Bounds)
                val paint = Paint().apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 10f
                    color = Color.GRAY
                    strokeCap = Paint.Cap.ROUND
                    pathEffect = DashPathEffect(floatArrayOf(30f, 50f), 0f)
                }
                val verticalPosition = child4Bounds.top + 13.5f
                val path = Path().apply {
                    moveTo(10f, verticalPosition)
                    lineTo(child4.width - 20f, verticalPosition)
                }
                c.drawPath(path, paint)
            }

            // the following is an example of how to draw behind a recycleview item
//            for (i in 0..childCount-1) {
//                Log.d("AAA", "child $i")
//                val child = parent.getChildAt(i)
//                val prioTxt = (parent.findContainingViewHolder(child) as ShopItemViewHolder?)
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