package com.example.g14.coalesce.usecase.user

import com.example.g14.coalesce.usecase.ObservableUseCase
import io.reactivex.Observable

/**
 * Created by Gabriel Fortin
 */

interface GetActiveUser : ObservableUseCase<ActiveUserResult> {
    override fun execute(): Observable<ActiveUserResult>
}

