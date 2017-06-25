package com.example.g14.coalesce.usecase.group

import com.example.g14.coalesce.usecase.ObservableUseCase
import io.reactivex.Observable

/**
 * Created by Gabriel Fortin
 */

interface GetGroups : ObservableUseCase<GroupsResult> {
    override fun execute(): Observable<GroupsResult>
}
