package com.example.g14.coalesce.usecase.group

import com.example.g14.coalesce.entity.Group
import com.example.g14.coalesce.entity.IdType
import com.example.g14.coalesce.entity.User
import com.example.g14.coalesce.usecase.Repository
import com.example.g14.coalesce.usecase.user.ActiveUserResult
import com.example.g14.coalesce.usecase.user.GetActiveUser
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.junit.Before
import org.junit.Test

import org.mockito.Mockito.*

/**
 * Created by Gabriel Fortin
 */

class GetGroupsTest {
    val u1 = User(101, "user 101", "")
    val u1_bis = User(101, "user 101", "")
    val u2 = User(102, "user 102", "")
    val u2_bis = User(102, "user 102", "")
    val u3 = User(103, "user 103", "")
    val u3_bis = User(103, "user 103", "")
    val u4 = User(104, "user 104", "")
    val u99 = User(199, "user 199", "")

    lateinit var repoMock: Repository
    lateinit var repoGetGroupsForMapping: Map<IdType, Group>

    lateinit var activeUserUseCaseMock: GetActiveUser
    lateinit var activeUserStream: BehaviorSubject<ActiveUserResult>

    @Before
    fun setUp() {
        prepareRepoMock()
        prepareActiveUserUseCase()
    }

    fun makeGroup(groupId: IdType, vararg members: User) =
            Group(groupId, members.asList())

    fun prepareRepoMock() {
        repoMock = mock(Repository::class.java)
        `when`(repoMock.getGroupsFor(any(IdType::class.java))).thenAnswer { invoc ->
            val group: Group =
                    // pack argument into list to process it
                    listOf(invoc.getArgument<IdType>(0))
                            // find group for requested id
                            .map { repoGetGroupsForMapping[it] }
                            // make sure it was found
                            .map { it ?: throw NullPointerException("missing mapping entry") }
                            // extract the result from list
                            .get(0)
            BehaviorSubject
                    .create<List<Group>>()
                    .apply { onNext(listOf(group)) }
        }
    }

    fun prepareActiveUserUseCase() {
        activeUserStream = BehaviorSubject.create<ActiveUserResult>()
                .apply { doOnNext {println("active user stream::onNext: $it")} }
                .apply { doOnComplete { println("active user stream::onComplete")} }
                .apply { doOnError { println("active user stream::onError")} }
                .apply { doOnSubscribe { println("active user stream::onSubscribe")} }
        activeUserUseCaseMock = mock(GetActiveUser::class.java)
        `when`(activeUserUseCaseMock.execute()).thenReturn(activeUserStream)
    }

    @Test
    fun execute_simpleLists() {
        // PREPARE
        repoGetGroupsForMapping = mapOf(
                101 to makeGroup(51, u1),
                102 to makeGroup(52, u2, u99),
                103 to makeGroup(53, u3),
                104 to makeGroup(54, u4, u99))

        // EXECUTE
        val testObserver = GetGroupsImpl(repoMock, activeUserUseCaseMock)
                .execute()
                .test()

        activeUserStream.apply {
            onNext(ActiveUserResult.Success(u1))
            onNext(ActiveUserResult.Success(u2))
            onNext(ActiveUserResult.Success(u3))
            onNext(ActiveUserResult.Success(u4))
        }

        // VERIFY
        testObserver.assertValues(
                GroupsResult.Success(listOf(makeGroup(51, u1))),
                GroupsResult.Success(listOf(makeGroup(52, u2, u99))),
                // here, we compare to an equal element 'u3_bis'
                GroupsResult.Success(listOf(makeGroup(53, u3_bis))),
                GroupsResult.Success(listOf(makeGroup(54, u4, u99)))
        )
        testObserver.assertNotTerminated()

        verifyRepoQueries(101..104)
        verifyActiveUserInteractions()
    }
    fun verifyRepoQueries(ids: Iterable<IdType>) {
        ids.forEach {
            verify(repoMock).getGroupsFor(it)
        }
        verifyNoMoreInteractions(repoMock)
    }

    fun verifyActiveUserInteractions() {
        verify(activeUserUseCaseMock).execute()
        verifyNoMoreInteractions(activeUserUseCaseMock)
    }

}
