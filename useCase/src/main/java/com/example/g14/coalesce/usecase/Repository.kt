package com.example.g14.coalesce.usecase

import com.example.g14.coalesce.entity.IdType
import com.example.g14.coalesce.entity.Message
import com.example.g14.coalesce.entity.User
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Created by Gabriel Fortin
 */

/** IMPORTANT:
 * Implementors of this class should ensure that all rx results yield events
 * on subscription as quickly as possible
 * e.g. by using .startWith()
 */
interface Repository {
    fun getCurrentUserId(): Observable<IdType?>
    fun setCurrentUserId(id: IdType): Completable
    fun getUserBy(id: IdType): Single<User>

    fun getMessages(groupId: IdType, quantity: Int, before: Long?): Observable<List<Message>>
    fun addMessage(message: Message): Completable
}
