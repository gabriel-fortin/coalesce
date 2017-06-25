package com.example.g14.coalesce.usecase.group

import com.example.g14.coalesce.entity.Group
import com.example.g14.coalesce.usecase.NoData

/**
 * Created by Gabriel Fortin
 */

sealed class ActiveGroupResult {
    data class Success(val group: Group) : ActiveGroupResult()
    data class NoGroup(val reason: NoData? = null) : NoData, ActiveGroupResult() {
        override fun reason(): NoData? = reason
    }
}
