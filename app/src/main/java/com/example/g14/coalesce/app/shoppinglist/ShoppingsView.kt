package com.example.g14.coalesce.app.shoppinglist

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.AttributeSet
import kotlinx.android.synthetic.main.activity_scratch.view.*
import com.example.g14.coalesce.app.scratch.ShoppingListItem

/**
 * Created by Gabriel Fortin
 */
class ShoppingsView(context: Context, attrs: AttributeSet?, defStyle: Int) : RecyclerView(context, attrs, defStyle) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    val adapter: ShopItemAdapter = ShopItemAdapter(context)

    // nie pokazuje się nic; inicjalizacja jest w złym momencie może?

    init {
        theInit()
    }

    fun theInit() {

        layoutManager = LinearLayoutManager(context)
                .apply { orientation = LinearLayoutManager.VERTICAL }

//        addItemDecoration(BambooItemDecor())
//        ItemTouchHelper(SwipeItemForOptions()).attachToRecyclerView(this)
    }


    fun setData(data: List<ScratchActivity.ShoppingListItem>){
        adapter.data = data
        adapter.notifyDataSetChanged()
    }
}
