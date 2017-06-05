package com.example.g14.coalesce.usecase

/**
 * Created by Gabriel Fortin
 */

// TODO: move to package "com.example.g14.coalesce"?
interface Repository {
//class Repository {

    class Executor() {
        fun <TResult: UseCaseResult> executeUseCase(useCase: UseCase<TResult>): TResult {
            TODO("execute the use case in an appropriate scheduler")
            return useCase.execute()
        }
    }

    fun getActiveUser(): ActiveUserResult

}

class ThrowAwayRepository: Repository {

    override fun getActiveUser(): ActiveUserResult {
        val result: ActiveUserResult = Repository.Executor().executeUseCase(GetActiveUser())
        when(result) {
            is ActiveUserResult.NoUser -> TODO("show login screen")
            is ActiveUserResult.UserResult -> {
                println("this is user: ${result.user}")
                return result
            }
        }
    }
}
