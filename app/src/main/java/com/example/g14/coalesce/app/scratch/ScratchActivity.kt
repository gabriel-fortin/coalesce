package com.example.g14.coalesce.app.scratch

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.example.g14.coalesce.app.R
import kotlinx.android.synthetic.main.activity_scratch.*

class ScratchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scratch)

        shoppingRecycler.adapter = BambooAdapter(SAMPLE_DATA, this)
        shoppingRecycler.layoutManager = LinearLayoutManager(this)
                .apply { orientation = LinearLayoutManager.VERTICAL }
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
