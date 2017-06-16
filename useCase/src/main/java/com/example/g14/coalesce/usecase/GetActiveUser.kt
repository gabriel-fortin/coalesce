package com.example.g14.coalesce.usecase

import com.example.g14.coalesce.entity.User
import io.reactivex.Observable

/**
 * Created by Gabriel Fortin
 */

class GetActiveUser(val repo: Repository): ObservableUseCase<ActiveUserResult> {

    override fun execute(): Observable<ActiveUserResult> =
            repo
            .getCurrentUserId()
            .whenNullThen(NoUser()) { whenNotNull ->
                whenNotNull
                .flatMap { repo.getUserBy(it).toObservable() }
                .map<ActiveUserResult> { user -> LoggedIn(user) }
            }
}


sealed class ActiveUserResult
class NoUser : ActiveUserResult()
class LoggedIn(val user: User): ActiveUserResult()
