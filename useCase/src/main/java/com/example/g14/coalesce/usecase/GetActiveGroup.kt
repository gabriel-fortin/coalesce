package com.example.g14.coalesce.usecase

import com.example.g14.coalesce.entity.Group
import com.example.g14.coalesce.usecase.ActiveGroupResult.NoGroup;
import io.reactivex.Observable

/**
 * Created by Gabriel Fortin
 */

sealed class ActiveGroupResult {
    class Success(val group: Group) : ActiveGroupResult()
    class NoGroup(val reason: NoData? = null) : NoData, ActiveGroupResult() {
        override fun reason(): NoData? = reason
    }
}

/** Depending on whether a user can belong to only one group or more
the implementation will vary */
interface GetActiveGroup : ObservableUseCase<ActiveGroupResult>


/** This implementation assumes a user can belong to only one group */
class GetTheOnlyActiveGroup(val repo: Repository) : GetActiveGroup {

    override fun execute(): Observable<ActiveGroupResult> =
        repo
        .getCurrentUserId()
        .whenNullThen(NoGroup(ActiveUserResult.NoUser())) { whenNotNull ->
            whenNotNull
            .flatMap(repo::getGroupsFor)
            .map(this::selectActiveGroup)
        }

    protected fun selectActiveGroup(listOfGroups: List<Group>): ActiveGroupResult {
        if (listOfGroups.size == 0) return NoGroup()
        if (listOfGroups.size == 1) return ActiveGroupResult.Success(listOfGroups[0])

        val msg = "cannot select active group; not implemented for >1 groups"
        throw UnsupportedOperationException(msg)
    }
}
