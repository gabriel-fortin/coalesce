package com.example.g14.coalesce.usecase

import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer

/**
 * Created by Gabriel Fortin
 */

fun <T> Observable<T?>.magicallyHideNull(): Observable<T> =
        this.map { it ?: throw NullCarrier() }

fun <T> Observable<out T>.magicallyReplaceHiddenNullWith(valueForNull: T): Observable<T> =
        this.compose(object: ObservableTransformer<T, T> {

            override fun apply(upstream: Observable<T>): ObservableSource<T> =
                    upstream
                            .materialize()
                            .map { unhide(it) ?: valueForNull }
                            .dematerialize()

            private fun unhide(notification: Notification<T>): Notification<T>? =
                    if (notification.isOnError && notification.error is NullCarrier) {
                        null
                    } else {
                        notification
                    }

        })

class NullCarrier : Throwable()
