package com.example.g14.coalesce.app.shoppinglist.internal

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.g14.coalesce.app.R

class ShoppingsAdapter(context: Context) : RecyclerView.Adapter<ShoppingsViewHolder>() {

    val inflater: LayoutInflater = LayoutInflater.from(context)

    var data: List<ShoppingsItem> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingsViewHolder {
        val view = inflater.inflate(R.layout.recycleritem_shopping_constraintlayout, parent, false)
//        view.setOnTouchListener { _, e -> gestureDetector.onTouchEvent(e) }
        return ShoppingsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShoppingsViewHolder, position: Int) {
        val item = data[position]
        with(holder) {
            priorityText = item.prio.toString()
            titleText = item.title
            checkBox = item.bought
        }
    }

    override fun getItemCount(): Int = data.size



    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(hasStableIds)
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }
}
