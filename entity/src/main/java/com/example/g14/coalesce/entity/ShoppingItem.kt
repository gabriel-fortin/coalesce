package com.example.g14.coalesce.entity

/**
 * Created by Gabriel Fortin
 */

data class ShoppingItem(
        val id: IdType,
        val name: String,
        val group: Group,
//        val purchased: Boolean,  // is it bought?
        val purchaseDate: Long?,  // when was it bought
        val repurchaseInterval: Long?  // in how much time will it be needed again
) {
    fun isActive(currentTime: Long): Boolean {
        if (purchaseDate == null) return true
        if (repurchaseInterval == null) return false

        return currentTime > purchaseDate + repurchaseInterval
    }
}
