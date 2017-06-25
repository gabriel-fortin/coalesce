package com.example.g14.coalesce.usecase.group

import com.example.g14.coalesce.usecase.NoData
import com.example.g14.coalesce.usecase.Repository
import com.example.g14.coalesce.usecase.user.ActiveUserResult
import com.example.g14.coalesce.usecase.user.GetActiveUser
import io.reactivex.Observable

/**
 * Created by Gabriel Fortin
 */

class GetGroupsImpl(
        val repo: Repository,
        val activeUserUseCase: GetActiveUser
): GetGroups {

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
                .map(GroupsResult::NoGroups)
        return Observable.merge(success, failure)
    }
}
