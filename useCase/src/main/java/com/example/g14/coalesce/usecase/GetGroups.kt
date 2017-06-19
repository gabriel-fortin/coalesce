package com.example.g14.coalesce.usecase

import com.example.g14.coalesce.entity.Group
import com.example.g14.coalesce.usecase.GroupsResult.NoGroups
import io.reactivex.Observable

/**
 * Created by Gabriel Fortin
 */

sealed class GroupsResult {
    class Success(val groups: List<Group>) : GroupsResult()
    class NoGroups(val reason: NoData? = null) : NoData, GroupsResult() {
        override fun reason(): NoData? = reason
    }
}

class GetGroups(
        val repo: Repository,
        val activeUserUseCase: GetActiveUser
): ObservableUseCase<GroupsResult> {

    override fun execute(): Observable<GroupsResult> {
        val activeUserRes = activeUserUseCase.execute()
        val success: Observable<GroupsResult> =
                activeUserRes
                .ofType(ActiveUserResult.Success::class.java)
                .flatMap { repo.getGroupsFor(it.user.id) }
                .map(GroupsResult::Success)
        val failure: Observable<GroupsResult> =
                activeUserRes
                .ofType(NoData::class.java)
                .map(::NoGroups)
        return Observable.merge(success, failure)
    }
}
