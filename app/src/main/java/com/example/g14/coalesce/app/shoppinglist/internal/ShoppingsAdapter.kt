package com.example.g14.coalesce.app.shoppinglist.internal

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.g14.coalesce.app.R

typealias Interceptor = (ShoppingsViewHolder) -> Unit

class ShoppingsAdapter(val context: Context) : RecyclerView.Adapter<ShoppingsViewHolder>() {
    companion object {
        val tag: String = ShoppingsAdapter::class.java.simpleName
    }

    var data: List<ShoppingsItem> = listOf()

    private val creationInterceptors = mutableListOf<Interceptor>()

    fun addCreationInterceptor(onCreateViewHolderInterceptor: Interceptor): Boolean
            = creationInterceptors.add(onCreateViewHolderInterceptor)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingsViewHolder {
        val view = LayoutInflater.from(context)
                .inflate(R.layout.recycleritem_shopping_constraintlayout, parent, false)
        val viewHolder = ShoppingsViewHolder(view)
        creationInterceptors.forEach { interceptor -> interceptor(viewHolder) }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ShoppingsViewHolder, position: Int) {
        val item = data[position]
        with(holder) {
            reset()
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
