package com.example.g14.coalesce.usecase

import com.example.g14.coalesce.entity.IdType
import com.example.g14.coalesce.entity.User
import com.example.g14.coalesce.usecase.ActiveUserResult.*;
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Created by Gabriel Fortin
 */

sealed class ActiveUserResult {
    class Success(val user: User): ActiveUserResult()
    class NoUser(val reason: NoData? = null) : NoData, ActiveUserResult() {
        override fun reason(): NoData? = reason
    }
}

class GetActiveUser(val repo: Repository): ObservableUseCase<ActiveUserResult> {

    override fun execute(): Observable<ActiveUserResult> =
            repo
            .getCurrentUserId()
            .switchMap { optionalId ->
                optionalId
                .defaultValueOrMapper(
                        valueIfEmpty = Single.just(NoUser()),
                        mapper = this::mapUserIdToActiveUserResult
                )
                .toObservable()
            }

    private fun mapUserIdToActiveUserResult(userId: IdType): Single<Success> =
            repo
            .getUserBy(userId)
            .map { user -> Success(user) }
}
