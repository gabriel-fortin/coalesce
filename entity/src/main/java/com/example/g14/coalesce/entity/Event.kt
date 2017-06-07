package com.example.g14.coalesce.entity

/**
 * Created by Gabriel Fortin
 */

data class Event(
        val id: IdType,
        val type: Any?, // TODO: decide type
        val timestamp: Long,
        val relatedItem: IdType
)
