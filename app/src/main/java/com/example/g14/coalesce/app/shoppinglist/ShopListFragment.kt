package com.example.g14.coalesce.app.shoppinglist

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.g14.coalesce.app.R

/**
 * Created by Gabriel Fortin
 */

class ShopListFragment : Fragment() {
    companion object {
        fun newInstance(): ShopListFragment {
            return ShopListFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.recycleritem_shopping_constraintlayout, container)
        val recyclerView = rootView.findViewById(R.id.shoppingItems)


        return rootView
    }
}
