package com.example.g14.coalesce.app.scratch

import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.View
import com.example.g14.coalesce.app.R
import kotlinx.android.synthetic.main.activity_scratch.*

class ScratchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scratch)

        shoppingRecycler.adapter = BambooAdapter(SAMPLE_DATA, this)
        shoppingRecycler.layoutManager = LinearLayoutManager(this)
                .apply { orientation = LinearLayoutManager.VERTICAL }
//        shoppingRecycler.addItemDecoration()
        BambooTouchHelper(BambooCallback()).attachToRecyclerView(shoppingRecycler)
    }

    class BambooCallback : ItemTouchHelper.Callback() {
        override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
            return makeMovementFlags(0, ItemTouchHelper.RIGHT)
        }

        override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
            TODO("not implemented: ${javaClass.simpleName}.onMove")
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            Log.i("AAA", "SWIPE")
        }
    }

    class BambooTouchHelper(callback: ItemTouchHelper.Callback) : ItemTouchHelper(callback) {
    }

    class BambooItemDecor : RecyclerView.ItemDecoration() {
        override fun onDrawOver(c: Canvas?, parent: RecyclerView?, state: RecyclerView.State?) {
            super.onDrawOver(c, parent, state)
        }

        override fun onDrawOver(c: Canvas?, parent: RecyclerView?) {
            super.onDrawOver(c, parent)
        }

        override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            super.onDraw(c, parent, state)
        }

        override fun onDraw(c: Canvas?, parent: RecyclerView?) {
            super.onDraw(c, parent)
        }

        override fun getItemOffsets(outRect: Rect?, itemPosition: Int, parent: RecyclerView?) {
            super.getItemOffsets(outRect, itemPosition, parent)
        }

        override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
            super.getItemOffsets(outRect, view, parent, state)
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
