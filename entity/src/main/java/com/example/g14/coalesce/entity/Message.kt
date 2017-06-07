package com.example.g14.coalesce.entity

/**
 * Created by Gabriel Fortin
 */

data class Messsage(
        val id: IdType,
        val text: String,
        val timestamp: Long,
        val sender: User,
        val syncStatus: SyncStatus
)

data class SyncStatus(
        val TODO: String  // TODO
)
