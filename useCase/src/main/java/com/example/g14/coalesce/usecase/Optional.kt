package com.example.g14.coalesce.usecase

/**
 * Created by Gabriel Fortin
 */

class Optional<out T: Any> private constructor(val value: T?) {

    companion object Factory {
        private val NULL: Optional<Nothing> = Optional(null)

        fun <R: Any> empty(): Optional<R> = NULL

        fun <R: Any> of(v: R): Optional<R> = Optional(v)
    }

    fun <S: Any> defaultValueOrMapper(valueIfEmpty: S, mapper: (T) -> S): S =
            if (value == null) valueIfEmpty
            else mapper(value)

}
