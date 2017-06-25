package com.example.g14.coalesce.usecase.group

import com.example.g14.coalesce.usecase.Repository
import com.example.g14.coalesce.usecase.user.ActiveUserResult
import com.example.g14.coalesce.usecase.user.ActiveUserResult.NoUser
import com.example.g14.coalesce.usecase.group.GroupsResult.NoGroups
import com.example.g14.coalesce.usecase.user.GetActiveUser
import io.reactivex.Observable

/**
 * Created by Gabriel Fortin
 */

class GetGroupsImpl(
        val repo: Repository,
        val activeUserUseCase: GetActiveUser
): GetGroups {

    override fun execute(): Observable<GroupsResult> =
            activeUserUseCase
            .execute()
            .switchMap<GroupsResult> { when (it) {
                is NoUser -> Observable.just(NoGroups(it))
                is ActiveUserResult.Success ->
                        repo
                        .getGroupsFor(it.user.id)
                        .map {
                            if (it.isEmpty()) NoGroups()
                            else GroupsResult.Success(it.toSet())
                        }
                }
            }

}
