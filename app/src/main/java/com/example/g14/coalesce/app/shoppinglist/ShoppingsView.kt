package com.example.g14.coalesce.app.shoppinglist

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import com.example.g14.coalesce.app.shoppinglist.internal.BambooItemDecor
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
    }

    fun setData(data: List<ShoppingsItem>){
        adapter.data = data
    }

}
