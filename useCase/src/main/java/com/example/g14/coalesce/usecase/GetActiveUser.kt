package com.example.g14.coalesce.usecase

import com.example.g14.coalesce.entity.IdType
import com.example.g14.coalesce.entity.User
import com.example.g14.coalesce.usecase.ActiveUserResult.*
import io.reactivex.Observable

/**
 * Created by Gabriel Fortin
 */

class GetActiveUser(val repo: Repository): ObservableUseCase<ActiveUserResult> {

    override fun execute(): Observable<ActiveUserResult> =
            repo
            .getCurrentUserId()
            .flatMap { when (it) {
                    is IdType -> repo
                            .getUserBy(it)
                            .map { user -> LoggedIn(user) }
                            .toObservable()
                    else -> Observable.just(NoUser())
                }
            }

}

sealed class ActiveUserResult {
    class NoUser : ActiveUserResult()
    class LoggedIn(val user: User): ActiveUserResult()
}
