package com.example.g14.coalesce.usecase.group

import com.example.g14.coalesce.entity.Group
import com.example.g14.coalesce.entity.IdType
import com.example.g14.coalesce.entity.User
import io.reactivex.subjects.BehaviorSubject
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

/**
 * Created by Gabriel Fortin
 */

class GetTheOnlyActiveGroupTest {
    val gr1 = Group(51, setOf(user(3), user(2)))
    val gr2 = Group(52, setOf(user(3)))
    val gr3 = Group(52, setOf(user(3), user(4), user(5)))

    lateinit var groupsUseCaseMock: GetGroups
    lateinit var groupsStream: BehaviorSubject<GroupsResult>

    /** Helper function */
    fun user(id: IdType): User = User(id, "user $id", "")

    @Before
    fun setUp() {
        groupsStream = BehaviorSubject.create()
        groupsUseCaseMock = mock(GetGroups::class.java)
        `when`(groupsUseCaseMock.execute()).thenReturn(groupsStream)
    }

    @Test
    fun execute_simpleGroups() {
        // PREPARE
        // nothing to prepare

        // EXECUTE
        val testObserver = GetTheOnlyActiveGroup(groupsUseCaseMock)
                .execute()
                .test()

        groupsStream.onNext(GroupsResult.Success(setOf(gr1)))
        groupsStream.onNext(GroupsResult.Success(setOf(gr2)))
        groupsStream.onNext(GroupsResult.Success(setOf(gr3)))

        // VERIFY
        testObserver.assertValues(
                ActiveGroupResult.Success(gr1),
                ActiveGroupResult.Success(gr2),
                ActiveGroupResult.Success(gr3)
        )
        testObserver.assertNotTerminated()

        verify(groupsUseCaseMock).execute()
        verifyNoMoreInteractions(groupsUseCaseMock)
    }

    @Test
    fun execute_emptyGroup() {
        // PREPARE
        // nothing to prepare

        // EXECUTE
        val testObserver = GetTheOnlyActiveGroup(groupsUseCaseMock)
                .execute()
                .test()

        groupsStream.onNext(GroupsResult.NoGroups())
        groupsStream.onNext(GroupsResult.Success(setOf(gr1)))

        // VERIFY
        testObserver.assertValues(
                ActiveGroupResult.NoGroup(GroupsResult.NoGroups()),
                ActiveGroupResult.Success(gr1)
        )
        testObserver.assertNotTerminated()

        verify(groupsUseCaseMock).execute()
        verifyNoMoreInteractions(groupsUseCaseMock)
    }

}
