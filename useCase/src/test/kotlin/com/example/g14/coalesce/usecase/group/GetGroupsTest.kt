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

//    fun makeSuccess(groupId: IdType, members: User...)


    @Before
    fun setUp() {
    }

    @Test
    fun execute_simpleLists() {
        // PREPARE
        val activeUserStream = BehaviorSubject.create<ActiveUserResult>()
                .apply { doOnNext {println("active user stream::onNext: $it")} }
                .apply { doOnComplete { println("active user stream::onComplete")} }
                .apply { doOnError { println("active user stream::onError")} }
                .apply { doOnSubscribe { println("active user stream::onSubscribe")} }
        val activeUserUseCase: GetActiveUser = mock(GetActiveUser::class.java)
        `when`(activeUserUseCase.execute()).thenReturn(activeUserStream)

        val repo: Repository = mock(Repository::class.java)
        `when`(repo.getGroupsFor(any(IdType::class.java))).thenAnswer { invoc ->
            val groupMappingFor = mapOf(
                    101 to Group(51, listOf(u1)),
                    102 to Group(52, listOf(u2, u99)),
                    103 to Group(53, listOf(u3_bis)),
                    104 to Group(54, listOf(u4, u99)))
            val groupResult: Group =
                    // pack argument into list to process it
                    listOf(invoc.getArgument<IdType>(0))
                    // find group for requested id
                    .map { groupMappingFor[it] }
                    // make sure it was found
                    .map { it ?: throw NullPointerException("missing mapping") }
                    // extract the result
                    .get(0)
            BehaviorSubject
                    .create<List<Group>>()
                    .apply { onNext(listOf(groupResult)) }
        }

        // EXECUTE
        val testObserver = GetGroupsImpl(repo, activeUserUseCase)
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
                GroupsResult.Success(listOf(Group(51, listOf(u1)))),
                GroupsResult.Success(listOf(Group(52, listOf(u2, u99)))),
                GroupsResult.Success(listOf(Group(53, listOf(u3)))),
                GroupsResult.Success(listOf(Group(54, listOf(u4, u99))))
        )
        testObserver.assertNotTerminated()

        verify(repo).getGroupsFor(101)
        verify(repo).getGroupsFor(102)
        verify(repo).getGroupsFor(103)
        verify(repo).getGroupsFor(104)
        verifyNoMoreInteractions(repo)

        verify(activeUserUseCase).execute()
        verifyNoMoreInteractions(activeUserUseCase)

    }

}
