package com.example.g14.coalesce.app.shoppinglist

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.g14.coalesce.app.R
import com.example.g14.coalesce.app.scratch.ShoppingListItem

class ShopItemAdapter(context: Context) : RecyclerView.Adapter<ShopItemViewHolder>() {

    val inflater: LayoutInflater = LayoutInflater.from(context)

    var data: List<ShoppingListItem> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ShopItemViewHolder {
        val view = inflater.inflate(R.layout.recycleritem_shopping_constraintlayout, parent, false)
        return ShopItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShopItemViewHolder, position: Int) {
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
