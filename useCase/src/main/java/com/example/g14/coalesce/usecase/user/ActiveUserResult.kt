package com.example.g14.coalesce.usecase.user

import com.example.g14.coalesce.entity.User
import com.example.g14.coalesce.usecase.NoData

/**
 * Created by Gabriel Fortin
 */

sealed class ActiveUserResult {
    data class Success(val user: User): ActiveUserResult()
    data class NoUser(val reason: NoData? = null) : NoData, ActiveUserResult() {
        override fun reason(): NoData? = reason
    }
}
