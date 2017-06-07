package com.example.g14.coalesce.usecase

/**
 * Created by Gabriel Fortin
 */

// TODO: wrap result of 'execute' in RxJava's 'Flowable'

abstract class UseCase<out TResult: UseCaseResult> {

    /** Should only be called by an Executor */
    abstract fun execute(): TResult

}

abstract class UseCaseResult

