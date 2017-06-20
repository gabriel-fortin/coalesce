package com.example.g14.coalesce.usecase

import com.example.g14.coalesce.entity.User
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.BehaviorSubject
import org.junit.Assert.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.*

/**
 * Created by Gabriel Fortin
 */

class GetActiveUserTest {
    @org.junit.Before
    fun setUp() {
    }

    @org.junit.Test
    fun execute_fewIdsWithThreadingTwist() {
        // PREPARE MOCKS
        val repoMock = Mockito.mock(Repository::class.java)

        val user101Stream: BehaviorSubject<User> = BehaviorSubject.create<User>()
        val user102Stream: BehaviorSubject<User> = BehaviorSubject.create<User>()
        val user103Stream: BehaviorSubject<User> = BehaviorSubject.create<User>()

        `when`(repoMock.getUserBy(101)).thenReturn(user101Stream.firstOrError())
        `when`(repoMock.getUserBy(102)).thenReturn(user102Stream.firstOrError())
        `when`(repoMock.getUserBy(103)).thenReturn(user103Stream.firstOrError())

        val currentUserStream = BehaviorSubject.create<Int>()
        `when`(repoMock.getCurrentUserId()).thenReturn(currentUserStream)

        val u1 = makeUser(101)
        val u2 = makeUser(102)
        val u3 = makeUser(103)

        // EXECUTE
        val testObserver = TestObserver.create<ActiveUserResult>()
        GetActiveUser(repoMock).execute().subscribe(testObserver)

        currentUserStream.onNext(101)
        // next user id appears before a 'User' object was returned for the previous
        currentUserStream.onNext(102)
        // 'u1' object should be ignored as is provided too late
        user101Stream.onNext(u1)
        user102Stream.onNext(u2)
        // the last one comes normally
        currentUserStream.onNext(103)
        user103Stream.onNext(u3)
        currentUserStream.onComplete()

        // VERIFY
        testObserver.assertValueCount(2)
        testObserver.assertValueAt(0, { it is ActiveUserResult.Success && it.user==u2 })
        testObserver.assertValueAt(1, { it is ActiveUserResult.Success && it.user==u3 })
        assertTrue(testObserver.values().zip(arrayOf(u2, u3)).all { (r, u) ->
            r is ActiveUserResult.Success && r.user==u
        })
        verify(repoMock).getCurrentUserId()
        verify(repoMock, times(3)).getUserBy(any(Int::class.java))
        verifyNoMoreInteractions(repoMock)
    }

    private fun makeUser(id: Int): User = User(id, "user $id", "")

    fun ActiveUserResult.Success.equals(arg: Any): Boolean {
        return arg is ActiveUserResult.Success
                && arg.user.equals(this.user)
    }

}
