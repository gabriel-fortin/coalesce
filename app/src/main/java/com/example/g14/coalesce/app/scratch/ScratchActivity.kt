package com.example.g14.coalesce.app.scratch

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import com.example.g14.coalesce.app.R
import com.example.g14.coalesce.app.shoppinglist.BambooItemDecor
import com.example.g14.coalesce.app.shoppinglist.SwipeItemForOptions
import kotlinx.android.synthetic.main.activity_scratch.*

val tag: String = ScratchActivity::class.java.simpleName

class ScratchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scratch)

        shoppingsComponent.setData(SAMPLE_DATA)
//        shoppingsComponent.theInit()
//        shoppingsComponent.layoutManager = LinearLayoutManager(this)
//                .apply { orientation = LinearLayoutManager.VERTICAL }
//
//        shoppingsComponent.addItemDecoration(BambooItemDecor())
//        ItemTouchHelper(SwipeItemForOptions()).attachToRecyclerView(shoppingsComponent)
//
//        shoppingsComponent.setData(SAMPLE_DATA)
    }


    data class ShoppingListItem(val title: String, val prio: Int, val bought: Boolean)

    val SAMPLE_DATA: List<ShoppingListItem> = listOf(
            ShoppingListItem("papryka", 1, false),
            ShoppingListItem("ogórek", 1, false),
            ShoppingListItem("twaróg", 2, false),
            ShoppingListItem("cheddar", 1, true),
            ShoppingListItem("seler naciowy", 1, true),
            ShoppingListItem("single cream", 2, false),
            ShoppingListItem("mięso mielone", 1, true),
            ShoppingListItem("kalafior", 1, false),
            ShoppingListItem("brokuł", 3, false),
            ShoppingListItem("masło orzechowe", 1, false)
    )

}
