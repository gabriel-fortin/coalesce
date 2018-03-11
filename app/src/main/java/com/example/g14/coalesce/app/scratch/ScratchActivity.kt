package com.example.g14.coalesce.app.scratch

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.g14.coalesce.app.R
import com.example.g14.coalesce.app.shoppinglist.ShoppingsItem
import kotlinx.android.synthetic.main.activity_scratch.*


class ScratchActivity : AppCompatActivity() {
    companion object {
        val tag: String = ScratchActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scratch)

        shoppingsComponent.setData(SAMPLE_DATA)
    }


    val SAMPLE_DATA: List<ShoppingsItem> = listOf(
            ShoppingsItem("papryka", 1, false),
            ShoppingsItem("ogórek", 1, false),
            ShoppingsItem("twaróg", 2, false),
            ShoppingsItem("cheddar", 1, true),
            ShoppingsItem("seler naciowy", 1, true),
            ShoppingsItem("single cream", 2, false),
            ShoppingsItem("mięso mielone", 1, true),
            ShoppingsItem("kalafior", 1, false),
            ShoppingsItem("brokuł", 3, false),
            ShoppingsItem("masło orzechowe", 1, false)
    )

}
