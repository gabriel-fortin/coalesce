package com.example.g14.coalesce.app.scratch

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.g14.coalesce.app.R

class BambooAdapter(val data: List<ScratchActivity.DataItem>, context: Context) : RecyclerView.Adapter<BambooViewHolder>() {

    val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): BambooViewHolder {
        val view = inflater.inflate(R.layout.recycleritem_shopping_constraintlayout, parent, false)
        return BambooViewHolder(view)
    }

    override fun onBindViewHolder(holder: BambooViewHolder, position: Int) {
        val item = data[position]
        with(holder) {
            setPriorityText(item.prio.toString())
            setTitleText(item.title)
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
