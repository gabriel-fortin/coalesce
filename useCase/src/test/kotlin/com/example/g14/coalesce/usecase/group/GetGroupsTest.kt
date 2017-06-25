package com.example.g14.coalesce.usecase.group

import com.example.g14.coalesce.entity.Group
import com.example.g14.coalesce.entity.IdType
import com.example.g14.coalesce.entity.User
import com.example.g14.coalesce.usecase.Repository
import com.example.g14.coalesce.usecase.user.ActiveUserResult
import com.example.g14.coalesce.usecase.user.GetActiveUser
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
    lateinit var repoGetGroupsForMapping: Map<IdType, List<Group>>

    lateinit var activeUserUseCaseMock: GetActiveUser
    lateinit var activeUserStream: BehaviorSubject<ActiveUserResult>

    @Before
    fun setUp() {
        prepareRepoMock()
        prepareActiveUserUseCase()
    }

    fun makeGroup(groupId: IdType, vararg members: User) =
            Group(groupId, members.toSet())

    fun prepareRepoMock() {
        repoMock = mock(Repository::class.java)
        `when`(repoMock.getGroupsFor(any(IdType::class.java))).thenAnswer { invoc ->
            val groupsList: List<Group> =
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
                    .apply { onNext(groupsList) }
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
                101 to listOf(makeGroup(51, u1)),
                102 to listOf(makeGroup(52, u2, u99)),
                103 to listOf(makeGroup(53, u3)),
                104 to listOf(makeGroup(54, u4, u99)))

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

    @Test
    fun execute_noGroups() {
        // PREPARE
        repoGetGroupsForMapping = mapOf(
                101 to emptyList(),
                102 to listOf(makeGroup(52, u2, u3))
        )

        // EXECUTE
        val testObserver = GetGroupsImpl(repoMock, activeUserUseCaseMock)
                .execute()
                .test()

        activeUserStream.onNext(ActiveUserResult.Success(u1))
        activeUserStream.onNext(ActiveUserResult.Success(u2))

        // VERIFY
        testObserver.assertValues(
                GroupsResult.NoGroups(null),
                GroupsResult.Success(listOf(makeGroup(52, u2, u3)))
        )
        testObserver.assertNotTerminated()

        verifyRepoQueries(101..102)
        verifyActiveUserInteractions()
    }

    @Test
    fun execute_fatGroup() {
        // PREPARE
        repoGetGroupsForMapping = mapOf(
                101 to listOf(makeGroup(51, u1)),
//                102 to listOf(makeGroup(52)),  // <- does not make sense
                103 to listOf(makeGroup(53, u3, u2, u99)),
                104 to listOf(makeGroup(54, u4)))

        // EXECUTE
        val testObserver = GetGroupsImpl(repoMock, activeUserUseCaseMock)
                .execute()
                .test()

        activeUserStream.apply {
            onNext(ActiveUserResult.Success(u1))
            onNext(ActiveUserResult.Success(u3))
            onNext(ActiveUserResult.Success(u4))
        }

        // VERIFY
        testObserver.assertValues(
                GroupsResult.Success(listOf(makeGroup(51, u1))),
                GroupsResult.Success(listOf(makeGroup(53, u2, u99, u3))),
                GroupsResult.Success(listOf(makeGroup(54, u4)))
        )
        testObserver.assertNotTerminated()

        verifyRepoQueries(listOf(101, 103, 104))
        verifyActiveUserInteractions()
    }

    @Test
    fun execute_manyGroups() {
        // PREPARE
        repoGetGroupsForMapping = mapOf(
                101 to listOf(
                        makeGroup(51, u1, u2),
                        makeGroup(61, u1, u99)),
                103 to listOf(makeGroup(53, u3)))

        // EXECUTE
        val testObserver = GetGroupsImpl(repoMock, activeUserUseCaseMock)
                .execute()
                .test()

        activeUserStream.apply {
            onNext(ActiveUserResult.Success(u1))
            onNext(ActiveUserResult.Success(u3))
        }

        // VERIFY
        testObserver.assertValues(
                GroupsResult.Success(listOf(
                        makeGroup(61, u1, u99),
                        makeGroup(51, u1, u2))),
                GroupsResult.Success(listOf(makeGroup(53, u3)))
        )
        testObserver.assertNotTerminated()

        verifyRepoQueries(listOf(101, 103))
        verifyActiveUserInteractions()

    }

    @Test
    fun execute_groupGetsUpdated() {
        // PREPARE
        val groupsFor101Stream = BehaviorSubject.create<List<Group>>()
        val groupsFor102Stream = BehaviorSubject.create<List<Group>>()
        repoMock = mock(Repository::class.java)
        `when`(repoMock.getGroupsFor(101)).thenReturn(groupsFor101Stream)
        `when`(repoMock.getGroupsFor(102)).thenReturn(groupsFor102Stream)

        // EXECUTE
        val testObserver = GetGroupsImpl(repoMock, activeUserUseCaseMock)
                .execute()
                .test()

        activeUserStream.onNext(ActiveUserResult.Success(u1))
        groupsFor101Stream.onNext(listOf(makeGroup(51, u1, u3)))
        groupsFor101Stream.onNext(listOf(makeGroup(51, u1, u4)))
        activeUserStream.onNext(ActiveUserResult.Success(u2))
        groupsFor102Stream.onNext(listOf(makeGroup(52, u2, u99)))


        // VERIFY
        testObserver.assertValues(
                GroupsResult.Success(listOf(makeGroup(51, u1, u3))),
                GroupsResult.Success(listOf(makeGroup(51, u1, u4))),
                GroupsResult.Success(listOf(makeGroup(52, u2, u99)))
        )
        testObserver.assertNotTerminated()

        verifyRepoQueries(101..102)
        verifyActiveUserInteractions()
    }

    @Test
    fun execute_concurrency() {
        // PREPARE
        val groupsFor101Stream = BehaviorSubject.create<List<Group>>()
        val groupsFor102Stream = BehaviorSubject.create<List<Group>>()
        repoMock = mock(Repository::class.java)
        `when`(repoMock.getGroupsFor(101)).thenReturn(groupsFor101Stream)
        `when`(repoMock.getGroupsFor(102)).thenReturn(groupsFor102Stream)


        // EXECUTE
        val testObserver = GetGroupsImpl(repoMock, activeUserUseCaseMock)
                .execute()
                .test()

        activeUserStream.onNext(ActiveUserResult.Success(u1))
        activeUserStream.onNext(ActiveUserResult.Success(u2))
        // late returning result for 'u1' (should be ignored)
        groupsFor101Stream.onNext(listOf(makeGroup(51, u1)))
        groupsFor102Stream.onNext(listOf(makeGroup(52, u2)))


        // VERIFY
        testObserver.assertValues(
                // we expect group '51' to be ignored

                GroupsResult.Success(listOf(makeGroup(52, u2)))
        )
        testObserver.assertNotTerminated()

        verifyRepoQueries(101..102)
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
