package com.example.g14.coalesce.entity

/**
 * Created by Gabriel Fortin
 */

data class Message(
        val id: IdType,
        val text: String,
        val timestamp: Long,
        val sender: User,
        val group: Group
)
