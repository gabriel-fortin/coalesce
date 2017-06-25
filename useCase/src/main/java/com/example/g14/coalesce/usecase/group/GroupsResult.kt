package com.example.g14.coalesce.usecase.group

import com.example.g14.coalesce.entity.Group
import com.example.g14.coalesce.usecase.NoData

/**
 * Created by Gabriel Fortin
 */

sealed class GroupsResult {
    data class Success(val groups: List<Group>) : GroupsResult()
    data class NoGroups(val reason: NoData? = null) : NoData, GroupsResult() {
        override fun reason(): NoData? = reason
    }
}
