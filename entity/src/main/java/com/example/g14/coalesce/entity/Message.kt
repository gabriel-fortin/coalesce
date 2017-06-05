package com.example.g14.coalesce.entity

/**
 * Created by Gabriel Fortin
 */

data class Messsage(
        val id: Int,
        val text: String,
        val timestamp: Long,
        val sender: User,
        val syncStatus: SyncStatus
)

data class SyncStatus(
        val TODO: String  // TODO
)
