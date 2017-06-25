package com.example.g14.coalesce.usecase.group

import com.example.g14.coalesce.entity.Group
import com.example.g14.coalesce.usecase.*
import io.reactivex.Observable

/**
 * Created by Gabriel Fortin
 */

/** This implementation assumes a user can belong to only one group */
class GetTheOnlyActiveGroup(
        val repo: Repository,
        val groupsUseCase: GetGroups
) : GetActiveGroup {

    override fun execute(): Observable<ActiveGroupResult> {
        val groupsRes = groupsUseCase.execute()
        val success: Observable<ActiveGroupResult> = groupsRes
                .ofType(GroupsResult.Success::class.java)
                .map { selectActiveGroup(it.groups) }
        val failure: Observable<ActiveGroupResult> = groupsRes
                .ofType(NoData::class.java)
                .map(ActiveGroupResult::NoGroup)
        return Observable.merge(success, failure)
    }

    protected fun selectActiveGroup(setOfGroups: Set<Group>): ActiveGroupResult {
        if (setOfGroups.size == 0) return ActiveGroupResult.NoGroup()
        if (setOfGroups.size == 1) return ActiveGroupResult.Success(setOfGroups.first())

        val msg = "cannot select active group; not implemented for >1 groupsUseCase"
        throw UnsupportedOperationException(msg)
    }
}
