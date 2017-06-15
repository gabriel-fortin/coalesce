package com.example.g14.coalesce.usecase

import com.example.g14.coalesce.entity.Group
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

    fun getUserBy(userId: IdType): Single<User>
    fun addUser(user: User): Completable

    /* This method implies fetching also 'User' POJOs */
    fun getGroupBy(groupId: IdType): Single<Group>
    fun addGroup(group: Group): Completable

    fun getGroupIdsFor(userId: IdType): Observable<List<IdType>>
    /* This method implies fetching also 'User' POJOs */
    fun getGroupsFor(userId: IdType): Observable<List<Group>>
    /* This method implies fetching also 'User' POJOs */
    fun addUserToGroup(userId: IdType, groupId: IdType): Completable

    fun fetchGroups(groupIds: List<IdType>): Single<List<Group>>

    fun getMessages(groupId: IdType, quantity: Int, before: Long?): Observable<List<Message>>
    fun addMessage(message: Message): Completable
}

/*
 * Patterns:
 *      get*By(someId: IdType) -> Single<*>
 *           * = some data class
 *      get*s(someId: IdType, …) -> Observable<List<*>>
 *           * = some data class
 *      add*(pojo: ?) -> Completable
 *           * = some data class
 */
