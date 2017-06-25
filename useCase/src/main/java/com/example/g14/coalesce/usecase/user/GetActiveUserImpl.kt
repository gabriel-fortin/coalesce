package com.example.g14.coalesce.usecase.user

import com.example.g14.coalesce.entity.IdType
import com.example.g14.coalesce.usecase.ObservableUseCase
import com.example.g14.coalesce.usecase.Repository
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Created by Gabriel Fortin
 */

class GetActiveUserImpl(val repo: Repository): ObservableUseCase<ActiveUserResult> {

    override fun execute(): Observable<ActiveUserResult> =
            repo
            .getCurrentUserId()
            .switchMap { optionalId ->
                optionalId
                .defaultValueOrMapper(
                        valueIfEmpty = Single.just(ActiveUserResult.NoUser()),
                        mapper = this::mapUserIdToActiveUserResult
                )
                .toObservable()
            }

    private fun mapUserIdToActiveUserResult(userId: IdType): Single<ActiveUserResult.Success> =
            repo
            .getUserBy(userId)
            .map { user -> ActiveUserResult.Success(user) }
}
