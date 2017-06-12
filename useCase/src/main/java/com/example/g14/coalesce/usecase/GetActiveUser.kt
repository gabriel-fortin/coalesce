package com.example.g14.coalesce.usecase

import com.example.g14.coalesce.entity.User
import io.reactivex.Observable

/**
 * Created by Gabriel Fortin
 */

interface GetActiveUser: ObservableUseCase<ActiveUserResult>{

    override fun execute(): Observable<ActiveUserResult> {
        TODO("not implemented")
    }

}

sealed class ActiveUserResult() {
    class NoUser : ActiveUserResult()
    class LoggedIn(val user: User): ActiveUserResult()
}
