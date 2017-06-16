package com.example.g14.coalesce.usecase

import io.reactivex.Notification
import io.reactivex.Observable

/**
 * Created by Gabriel Fortin
 */

class NullCarrier : Throwable()

fun <T, R> Observable<T?>.whenNullThen(
        specialValue: R,
        funForNonNullCase: (Observable<T>) -> Observable<R>
): Observable<R> =
        this
        .magicallyHideNull()
        .compose<R>(funForNonNullCase)
        .compose { transformHiddenNullTo(specialValue, it) }

private inline fun <T> Observable<T?>.magicallyHideNull(): Observable<T> =
        this.map { it ?: throw NullCarrier() }

private inline fun <R> transformHiddenNullTo(specialValue: R, obs: Observable<R>): Observable<R> =
        obs
        .materialize()
        .map<Notification<*>> { notification ->
            if (notification.isOnError && notification.error is NullCarrier) {
                Notification.createOnNext(specialValue)
            } else {
                notification
            }
        }
        .dematerialize<R>()
