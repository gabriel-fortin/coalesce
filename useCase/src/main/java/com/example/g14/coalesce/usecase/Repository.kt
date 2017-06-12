package com.example.g14.coalesce.usecase

import com.example.g14.coalesce.entity.Message
import io.reactivex.Observable

/**
 * Created by Gabriel Fortin
 */

// TODO: move to package "com.example.g14.coalesce"?
interface Repository {
    fun getActiveUser(): GetActiveUser

    fun getMessages(): GetMessages

    fun getMoreMessages(beforeDate: Long, quantity: Int = 5): GetMessages
}

