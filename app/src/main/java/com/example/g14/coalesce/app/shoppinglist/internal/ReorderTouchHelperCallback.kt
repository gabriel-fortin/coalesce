package com.example.g14.coalesce.app.shoppinglist.internal

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log

/**
 * Created by Gabriel Fortin
 */
class ReorderTouchHelperCallback(
        private val adapterToNotify: RecyclerView.Adapter<out RecyclerView.ViewHolder>
) : ItemTouchHelper.SimpleCallback(VERTICAL_MOVEMENT_FLAGS, 0) {

    companion object {
        val VERTICAL_MOVEMENT_FLAGS = ItemTouchHelper.Callback
                .makeMovementFlags(ItemTouchHelper.DOWN xor ItemTouchHelper.UP, 0)
    }

    override fun onMove(
            rv: RecyclerView,
            src: RecyclerView.ViewHolder,
            dst: RecyclerView.ViewHolder)
            : Boolean {
        Log.d("reorder detection", "onMove   " +
                "${src.adapterPosition} -> ${dst.adapterPosition}")
        adapterToNotify.notifyItemMoved(src.adapterPosition, dst.adapterPosition)
        return true
    }

    // we want dragging to be triggered:
    // - only by a specific button, not the whole view
    // - by a click, not a long press
    override fun isLongPressDragEnabled(): Boolean = false

    // should never happen
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        throw NotImplementedError("tried to swipe while swipe flags are '0'")
    }

}
