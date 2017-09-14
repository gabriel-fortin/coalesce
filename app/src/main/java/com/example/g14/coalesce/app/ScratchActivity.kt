package com.example.g14.coalesce.app

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_scratch.*

class ScratchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scratch)

        shoppingRecycler.adapter = MyAdapter(SAMPLE_DATA, this)
        shoppingRecycler.layoutManager = LinearLayoutManager(this)
                .apply { orientation = LinearLayoutManager.VERTICAL }
    }

    class MyAdapter(val data: List<DataItem>, context: Context) : RecyclerView.Adapter<MyViewHolder>() {

        val inflater: LayoutInflater = LayoutInflater.from(context)

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyViewHolder {
            val view = inflater.inflate(R.layout.recycleritem_shopping_constraintlayout, parent, false)
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
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

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val prioText = view.findViewById(R.id.priorityText) as TextView
        private val titleText = view.findViewById(R.id.titleText) as TextView
        private val checkbox = view.findViewById(R.id.buyingStateBox) as CheckBox

        fun setPriorityText(text: String) {
            prioText.text = text
        }

        fun setTitleText(text: String) {
            titleText.text = text
        }

        var checkBox: Boolean = false
            set(value) {
                checkbox.isChecked = value
            }
    }

    data class DataItem(val title: String, val prio: Int, val bought: Boolean)

    val SAMPLE_DATA: List<DataItem> = listOf(
            DataItem("papryka", 1, false),
            DataItem("ogórek", 1, false),
            DataItem("twaróg", 2, false),
            DataItem("cheddar", 1, true),
            DataItem("seler naciowy", 1, true),
            DataItem("single cream", 2, false),
            DataItem("mięso mielone", 1, true),
            DataItem("kalafior", 1, false),
            DataItem("brokuł", 3, false),
            DataItem("masło orzechowe", 1, false)
    )

}
