package com.example.g14.coalesce.usecase

import com.example.g14.coalesce.entity.User

/**
 * Created by Gabriel Fortin
 */

class GetActiveUser: UseCase<ActiveUserResult>() {

    override fun execute(): ActiveUserResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

sealed class ActiveUserResult(): UseCaseResult() {
    class NoUser: ActiveUserResult()
    class UserResult(val user: User): ActiveUserResult()
}
