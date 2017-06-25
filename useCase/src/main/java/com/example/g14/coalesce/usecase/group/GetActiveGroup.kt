package com.example.g14.coalesce.usecase.group

import com.example.g14.coalesce.usecase.ObservableUseCase
import io.reactivex.Observable

/**
 * Created by Gabriel Fortin
 */

/**
 * Depending on whether a user can belong to only one group or more
 * the implementation will vary
 */
interface GetActiveGroup : ObservableUseCase<ActiveGroupResult> {
    override fun execute(): Observable<ActiveGroupResult>
}
