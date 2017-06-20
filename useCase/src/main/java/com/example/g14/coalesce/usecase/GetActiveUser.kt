package com.example.g14.coalesce.usecase

import com.example.g14.coalesce.entity.User
import com.example.g14.coalesce.usecase.ActiveUserResult.*;
import io.reactivex.Observable

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
            .switchMap {
                if (it == null) Observable.just(NoUser())
                else repo
                        .getUserBy(it)
                        .toObservable()
                        .map { user -> Success(user) }
            }
}
