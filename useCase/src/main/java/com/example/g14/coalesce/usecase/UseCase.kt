package com.example.g14.coalesce.usecase

import io.reactivex.Observable

/**
 * Created by Gabriel Fortin
 */

interface UseCase<out TResult> {
    fun execute() : TResult
}

/** Convenience type to flatten nested generic args */
interface ObservableUseCase<TResult> : UseCase<Observable<TResult>> {}
