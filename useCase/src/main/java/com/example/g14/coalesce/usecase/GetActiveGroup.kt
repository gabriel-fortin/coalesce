package com.example.g14.coalesce.usecase

import com.example.g14.coalesce.entity.Group
import io.reactivex.Observable

/**
 * Created by Gabriel Fortin
 */

/** Depending on whether a user can belong to only one group or more
    the implementation will vary */
interface GetActiveGroup : ObservableUseCase<ActiveGroupResult>

sealed class ActiveGroupResult
class ActiveGroup(val group: Group): ActiveGroupResult()
class NoGroup(): ActiveGroupResult()


/** This implementation assumes a user can belong to only one group */
class GetTheOnlyActiveGroup(val repo: Repository) : GetActiveGroup {

    override fun execute(): Observable<ActiveGroupResult> =
        repo
        .getCurrentUserId()
        .whenNullThen(NoGroup()) { whenNotNull ->
            whenNotNull
            .flatMap(repo::getGroupsFor)
            .map(this::selectActiveGroup)
        }

    protected fun selectActiveGroup(listOfGroups: List<Group>): ActiveGroupResult {
        if (listOfGroups.size == 0) return NoGroup()
        if (listOfGroups.size == 1) return ActiveGroup(listOfGroups[0])

        val msg = "cannot select active group; not implemented for >1 groups"
        throw UnsupportedOperationException(msg)
    }
}
