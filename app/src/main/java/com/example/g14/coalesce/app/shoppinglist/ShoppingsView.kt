package com.example.g14.coalesce.app.shoppinglist

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import com.example.g14.coalesce.app.shoppinglist.internal.BambooItemDecor
import com.example.g14.coalesce.app.shoppinglist.internal.ReorderTouchHelperCallback
import com.example.g14.coalesce.app.shoppinglist.internal.ShoppingsAdapter
import com.example.g14.coalesce.app.shoppinglist.internal.ShoppingsItem

/**
 * Created by Gabriel Fortin
 */
class ShoppingsView
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : RecyclerView(context, attrs, defStyle) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    var adapter: ShoppingsAdapter
        get() = (super.getAdapter()
                ?: throw NullPointerException("somebody set the adapter by casting to super-class")
                ) as ShoppingsAdapter
        set(value) = super.setAdapter(value)

    init {
        adapter = ShoppingsAdapter(context)
        layoutManager = LinearLayoutManager(this.context)
                .apply { orientation = LinearLayoutManager.VERTICAL }
        addItemDecoration(BambooItemDecor())

        val itemTouchHelper = ItemTouchHelper(ReorderTouchHelperCallback(adapter))
        itemTouchHelper.attachToRecyclerView(this)

        val reorderingCallback = object : ReorderEventCallback {
            override fun moveStarted(vh: ViewHolder) {
                itemTouchHelper.startDrag(vh)
            }

            override fun moveToTop(vh: ViewHolder) {
                Log.d("reorder detection", "to top    from: $${vh.adapterPosition}")
                adapter.notifyItemMoved(vh.adapterPosition, 0)
            }
        }
        adapter.addCreationInterceptor { viewHolder ->
            val reorderListener = ReorderGestureListener(viewHolder, reorderingCallback)
            val reorderDetector = GestureDetector(context, reorderListener)
            // TODO?: move detector to view holder to store its reference?
            viewHolder.reorderUnderbutton.setOnTouchListener { _, event ->
                reorderDetector.onTouchEvent(event)
            }
        }
    }

    fun setData(data: List<ShoppingsItem>){
        adapter.data = data
    }

}
