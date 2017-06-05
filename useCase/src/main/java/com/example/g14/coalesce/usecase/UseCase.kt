package com.example.g14.coalesce.usecase

/**
 * Created by Gabriel Fortin
 */

// TODO: wrap result of 'execute' in RxJava's 'Flowable'

abstract class UseCase<TResult: UseCaseResult> {

    abstract fun execute(): TResult

}

open class UseCaseResult() {
//    class Empty
//    class Body
}

