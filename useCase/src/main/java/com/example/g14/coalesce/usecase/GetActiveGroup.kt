package com.example.g14.coalesce.usecase

import com.example.g14.coalesce.entity.Group
import com.example.g14.coalesce.entity.IdType
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.ObservableTransformer

/**
 * Created by Gabriel Fortin
 */

/** Depending on whether a user can belong to only one group or more
    the implementation will vary */
interface GetActiveGroup : ObservableUseCase<ActiveGroupResult>

sealed class ActiveGroupResult
class ActiveGroup(val group: Group): ActiveGroupResult()
class NoGroup(): ActiveGroupResult()


const val TRANSFORM_TO_NO_GROUP: String = "no group"

/** This implementation assumes a user can belong to only one group */
class GetTheOnlyActiveGroup(
        val repo: Repository
) : GetActiveGroup {

    override fun execute(): Observable<ActiveGroupResult> =
        repo
        .getCurrentUserId()
        .map(this::transformNullToNullException)
        .flatMap(repo::getGroupsFor)
        .map { listOfGroups ->
            when (listOfGroups.size) {
                0 -> NoGroup()
                1 -> ActiveGroup(listOfGroups[0])
                else -> selectActiveGroup(listOfGroups)
            }
        }
        .compose(transformNullExceptionToNoGroup)

    private fun transformNullToNullException(maybeId: IdType?): IdType =
            maybeId ?: throw NullPointerException(TRANSFORM_TO_NO_GROUP)

    private object transformNullExceptionToNoGroup :
            ObservableTransformer<ActiveGroupResult, ActiveGroupResult> {
        override fun apply(upstream: Observable<ActiveGroupResult>):
                Observable<ActiveGroupResult> =
            upstream
            .materialize()
            .map {
                if (it.isOnError && it.error.message == TRANSFORM_TO_NO_GROUP) {
                    Notification.createOnNext(NoGroup())
                } else {
                    it
                }
            }
            .dematerialize<ActiveGroupResult>()
    }

    protected fun selectActiveGroup(listOfGroups: List<Group>): ActiveGroupResult {
        val msg = "cannot select active group; not implemented for >1 groups"
        throw UnsupportedOperationException(msg)
    }
}
