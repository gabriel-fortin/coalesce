package com.example.g14.coalesce.usecase.user

import com.example.g14.coalesce.entity.User
import com.example.g14.coalesce.usecase.Optional
import com.example.g14.coalesce.usecase.Repository
import io.reactivex.subjects.BehaviorSubject
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*

/**
 * Created by Gabriel Fortin
 */

class GetActiveUserTest {
    @Before
    fun setUp() {
    }

    @Test
    fun execute_fewIdsWithThreadingTwist() {
        // PREPARE MOCKS
        val repoMock = mock(Repository::class.java)

        val user101Stream: BehaviorSubject<User> = BehaviorSubject.create<User>()
        val user102Stream: BehaviorSubject<User> = BehaviorSubject.create<User>()
        val user103Stream: BehaviorSubject<User> = BehaviorSubject.create<User>()

        // "repo.getUserBy(…)"
        `when`(repoMock.getUserBy(101)).thenReturn(user101Stream.firstOrError())
        `when`(repoMock.getUserBy(102)).thenReturn(user102Stream.firstOrError())
        `when`(repoMock.getUserBy(103)).thenReturn(user103Stream.firstOrError())

        // "repo.getCurrentUserId()"
        val repoCurrentUserIdStream = BehaviorSubject.create<Optional<Int>>()
        `when`(repoMock.getCurrentUserId()).thenReturn(repoCurrentUserIdStream)

        val u1 = makeUser(101)
        val u2 = makeUser(102)
        val u3 = makeUser(103)

        // EXECUTE
        val testObserver = GetActiveUserImpl(repoMock).execute().test()

        repoCurrentUserIdStream.onNext(Optional.of(101))
        // next user id appears before a 'User' object was returned for the previous
        repoCurrentUserIdStream.onNext(Optional.of(102))
        // 'u1' object should be ignored by the use case as it is provided too late
        user101Stream.onNext(u1)
        user102Stream.onNext(u2)
        // the last one comes normally
        repoCurrentUserIdStream.onNext(Optional.of(103))
        user103Stream.onNext(u3)

        // VERIFY
        testObserver.assertValueCount(2)
        testObserver.assertValueAt(0, { it is ActiveUserResult.Success && it.user==u2 })
        testObserver.assertValueAt(1, { it is ActiveUserResult.Success && it.user==u3 })
        testObserver.assertNotTerminated()

        verify(repoMock).getCurrentUserId()
        verify(repoMock, times(3)).getUserBy(any(Int::class.java))
        verifyNoMoreInteractions(repoMock)
    }

    @Test
    fun execute_nullValues() {
        // PREPARE MOCKS
        val repoMock = mock(Repository::class.java)

        // "repo.getUserBy(…)"
        val user101Stream: BehaviorSubject<User> = BehaviorSubject.create<User>()
        `when`(repoMock.getUserBy(101)).thenReturn(user101Stream.firstOrError())

        // "repo.getCurrentUserId()"
        val repoCurrentUserIdStream = BehaviorSubject.create<Optional<Int>>()
        `when`(repoMock.getCurrentUserId()).thenReturn(repoCurrentUserIdStream)

        val u1 = makeUser(101)

        // EXECUTE
        val testObserver = GetActiveUserImpl(repoMock).execute().test()

        repoCurrentUserIdStream.onNext(Optional.empty())
        repoCurrentUserIdStream.onNext(Optional.of(101))
        user101Stream.onNext(u1)
        repoCurrentUserIdStream.onNext(Optional.empty())

        // VERIFY
        testObserver.assertValueCount(3)
        testObserver.assertValueAt(0, { value ->
            value is ActiveUserResult.NoUser && value.reason == null })
        testObserver.assertValueAt(1, { value ->
            value is ActiveUserResult.Success && value.user == u1 })
        testObserver.assertValueAt(2, { value ->
            value is ActiveUserResult.NoUser && value.reason == null })
        testObserver.assertNotTerminated()

        verify(repoMock).getCurrentUserId()
        verify(repoMock).getUserBy(any(Int::class.java))
        verifyNoMoreInteractions(repoMock)
    }

    private fun makeUser(id: Int): User = User(id, "user $id", "")

}
