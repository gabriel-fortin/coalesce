package com.example.g14.coalesce.app.shoppinglist

import android.support.v7.widget.RecyclerView

/**
 * Created by Gabriel Fortin
 */
interface ReorderEventCallback {
    fun moveStarted(vh: RecyclerView.ViewHolder)
    fun moveToTop(vh: RecyclerView.ViewHolder)
}
