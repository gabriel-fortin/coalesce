package com.example.g14.coalesce.usecase.message

import com.example.g14.coalesce.entity.Group
import com.example.g14.coalesce.entity.Message
import com.example.g14.coalesce.usecase.Repository
import com.example.g14.coalesce.usecase.group.ActiveGroupResult
import com.example.g14.coalesce.usecase.group.GetActiveGroup
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.junit.Assert.*
import org.mockito.Mockito.*
import java.util.*

/**
 * Created by Gabriel Fortin
 */

/** starting value for "timestamp" */
const val T_BASE: Long = 3000000L

class GetMessagesImplTest {
    /** DATA GENERATORS */
    private val group: GroupProvider = TestData.group
    private val genMessages: MessagesGenerator = TestData.messageGenerator

    /*************************************************************************/

    private lateinit var _repo: Repository
    private lateinit var _activeGroup: GetActiveGroup
    private lateinit var randomness: Random

    @Before
    fun setUp() {
        _repo = mock(Repository::class.java)
        _activeGroup = mock(GetActiveGroup::class.java)
        randomness = Random(14*14)
    }



    /*************************************************************************/


    @Test
    fun justOneGroup_justOneMessagesList() {
        // TEST DATA
        val timestamp = Long.MAX_VALUE
        val quantity = 14
        val groupId = 3

        val messages1 = genMessages[groupId, quantity, randomness]
        val messagesResult1 = MessagesResult.Success(messages1)

        // PREPARE MOCKS
        setUpActiveGroupObservableMock(_activeGroup, group[groupId])
        setUpMessagesObservableMock(_repo,
                _MsgsArgs(groupId, quantity, timestamp).to(messages1)
        )

        // EXECUTE
        val sutObserver = GetMessagesImpl(_repo, _activeGroup, timestamp, quantity)
                .execute()
                .test()

        // VERIFY
        sutObserver
                .assertNoErrors()
                .assertValues(messagesResult1)
                .assertNotTerminated()
                .assertSubscribed()
    }

    @Test
    fun whenOutputObservableIsDisposed_thenActiveGroupObservableIsDisposed() {
        // TEST DATA
        val groupBeforeDisposal = ActiveGroupResult.Success(group[12])
        val groupAfterDisposal = ActiveGroupResult.Success(group[13])
        val messagesBeforeDisposal = genMessages[12, 5, randomness]
        val messagesAfterDisposal = genMessages[13, 7, randomness]
        val messagesResultBeforeDisposal = MessagesResult.Success(messagesBeforeDisposal)

        // PREPARE MOCKS
        val activeGroupSubject = PublishSubject.create<ActiveGroupResult>()
        // this will not replay values after all subscribers disconnect
        val replayableObs = activeGroupSubject.replay().refCount()

        setUpActiveGroupObservableMock(_activeGroup, replayableObs)
        setUpMessagesObservableMock(_repo,
                _MsgsArgs(12, 10, null).to(messagesBeforeDisposal),
                _MsgsArgs(13, 10, null).to(messagesAfterDisposal)
        )

        // EXECUTE
        val sutObserver: TestObserver<MessagesResult> =
                GetMessagesImpl(_repo, _activeGroup)
                .execute()
                .test()

        activeGroupSubject.onNext(groupBeforeDisposal)

        // the only observer of 'replayableObs' sits in sut
        sutObserver.dispose()
        // so its replay buffer should be lost now

        // VERIFY
        // let's see how much can be replayed
        val replayObserver = replayableObs.test()
        activeGroupSubject.onNext(groupAfterDisposal)

        // only values emitted BEFORE second subscription to 'replayableObs' should be observed
        sutObserver
                .assertNoErrors()  // technically not needed but gives better output
                .assertValues(messagesResultBeforeDisposal)
                .assertNotTerminated()
                .assertSubscribed()

        // only values emitted AFTER second subscription to 'replayableObs' should be observed
        replayObserver.assertValues(groupAfterDisposal)
    }

    /** Verifies if <code>repo.getMessages</code>'s observable is being disposed by checking
     * whether the replay buffer for this observable was dropped. Dropping the buffer occurs
     * when the observable is disposed.
     */
    @Test
    fun whenOutputObservableIsDisposed_thenMessagesObservableIsDisposed() {
        // TEST DATA
        val groupId = 7
        val messagesBeforeDisposal: List<Message> = genMessages[groupId, 3, randomness]
        val messagesAfterDisposal: List<Message> = genMessages[groupId, 1, randomness]
        val messagesResultBeforeDisposal = MessagesResult.Success(messagesBeforeDisposal)

        // PREPARE MOCKS
        val messagesSubject = PublishSubject.create<List<Message>>()
        // if all subscribers disconnect then replay values will be dropped
        val replayableObs = messagesSubject.replay().refCount()

        setUpActiveGroupObservableMock(_activeGroup, group[groupId])
        setUpMessagesObservableMock(_repo, listOf(
                _MsgsArgs(groupId, 10, null).to(replayableObs)
        ))

        // EXECUTE
        val sutObserver = GetMessagesImpl(_repo, _activeGroup)
                .execute()
                .test()

        // we expect that 'messagesBeforeDisposal' will not be replayed later
        messagesSubject.onNext(messagesBeforeDisposal)

        // after this the replay buffer should be lost
        sutObserver.dispose()

        // VERIFY
        // let's observe again; a new replay buffer should be in use
        val testObs = replayableObs.test()
        messagesSubject.onNext(messagesAfterDisposal)

        sutObserver
                .assertNoErrors()
                .assertValues(messagesResultBeforeDisposal)
                .assertNotTerminated()
                .assertSubscribed()

        // check that indeed the buffer was dropped
        testObs.assertValues(messagesAfterDisposal)
    }

    @Test
    fun whenActiveGroupChanges_thenNewResultIsYielded() {
        // TEST DATA
        val groupA = 2
        val groupB = 8
        val messagesA = genMessages[groupA, 5, randomness]
        val messagesB = genMessages[groupB, 6, randomness]
        val expectedValues = listOf(
                MessagesResult.Success(messagesA),
                MessagesResult.Success(messagesB)
        )

        // PREPARE MOCKS
        val groupSubject = BehaviorSubject.create<Group>()

        setUpActiveGroupObservableMock(_activeGroup, groupSubject.map<ActiveGroupResult>(ActiveGroupResult::Success))
        setUpMessagesObservableMock(_repo,
                _MsgsArgs(groupA, 10, null).to(messagesA),
                _MsgsArgs(groupB, 10, null).to(messagesB)
        )

        // EXECUTE
        val sutObserver = GetMessagesImpl(_repo, _activeGroup)
                .execute()
                .test()

        groupSubject.onNext(group[groupA])
        groupSubject.onNext(group[groupB])

        // VERIFY
        sutObserver
                .assertNoErrors()
                .assertValueSequence(expectedValues)
                .assertNotTerminated()
                .assertSubscribed()
    }

    @Test
    fun whenActiveGroupChanges_thenMessagesForPreviousGroupAreNotYielded() {
        // TEST DATA
        val groupA = 13
        val groupB = 14
        val messagesA1 = genMessages[groupA, 2, randomness]
        val messagesA2 = genMessages[groupA, 2, randomness]

        // PREPARE MOCKS
        val groupSubject = PublishSubject.create<ActiveGroupResult>()
        val messagesASubject = PublishSubject.create<List<Message>>()
        val messagesBSubject = PublishSubject.create<List<Message>>()

        setUpActiveGroupObservableMock(_activeGroup, groupSubject)
        setUpMessagesObservableMock(_repo, listOf(
                _MsgsArgs(groupA, 10, null).to(messagesASubject),
                _MsgsArgs(groupB, 10, null).to(messagesBSubject)
        ))

        // EXECUTE
        val sutObserver = GetMessagesImpl(_repo, _activeGroup)
                .execute()
                .test()

        groupSubject.onNext(ActiveGroupResult.Success(group[groupA]))
        messagesASubject.onNext(messagesA1)

        groupSubject.onNext(ActiveGroupResult.Success(group[groupB]))
        // those messages should not appear in the output (even more: their observable should be disposed)
        messagesASubject.onNext(messagesA2)

        // VERIFY
        sutObserver
                .assertNoErrors()
                .assertValues(
                        MessagesResult.Success(messagesA1)
                )
                .assertNotTerminated()
                .assertSubscribed()
    }

    @Test
    fun whenRepoHasNewMessages_thenNewMessagesAreYielded() {
        // TEST DATA
        val groupA = 13
        val messagesA1 = genMessages[groupA, 2, randomness]
        val messagesA2 = genMessages[groupA, 2, randomness]

        // PREPARE MOCKS
        val messagesASubject = PublishSubject.create<List<Message>>()

        setUpActiveGroupObservableMock(_activeGroup, group[groupA])
        setUpMessagesObservableMock(_repo, listOf(
                _MsgsArgs(groupA, 10, null).to(messagesASubject)
        ))

        // EXECUTE
        val sutObserver = GetMessagesImpl(_repo, _activeGroup)
                .execute()
                .test()

        messagesASubject.onNext(messagesA1)
        messagesASubject.onNext(messagesA2)

        // VERIFY
        sutObserver
                .assertNoErrors()
                .assertValues(
                        MessagesResult.Success(messagesA1),
                        MessagesResult.Success(messagesA2)
                )
                .assertNotTerminated()
                .assertSubscribed()
    }

    @Test
    fun whenTimestampProvided_thenRepoIsQueriedWithThisTimestamp() {
        // TEST DATA
        val groupId = 13
        val timestamp = 9284L
        val messages = genMessages[groupId, 2, randomness]

        // PREPARE MOCKS
        setUpActiveGroupObservableMock(_activeGroup, group[groupId])
        // only a call to '_repo.getMessages' with here provided args will not throw
        setUpMessagesObservableMock(_repo,
                _MsgsArgs(groupId, 10, timestamp).to(messages)
        )

        // EXECUTE
        val sutObserver = GetMessagesImpl(_repo, _activeGroup, timestamp = timestamp)
                .execute()
                .test()

        // VERIFY
        // verification is performed by the choice of args passed to 'setUpMessagesObservableMock'

        sutObserver
                .assertNoErrors()
                .assertNotTerminated()
                .assertSubscribed()
    }

    @Test
    fun whenTimestampNotProvided_thenRepoIsQueriedWithNullTimestamp() {
        // TEST DATA
        val groupId = 13
        val timestamp = null
        val messages = genMessages[groupId, 2, randomness]

        // PREPARE MOCKS
        setUpActiveGroupObservableMock(_activeGroup, group[groupId])
        // only a call to '_repo.getMessages' with here provided args will not throw
        setUpMessagesObservableMock(_repo,
                _MsgsArgs(groupId, 10, timestamp).to(messages)
        )

        // EXECUTE
        val sutObserver = GetMessagesImpl(_repo, _activeGroup, timestamp = timestamp)
                .execute()
                .test()

        // VERIFY
        // verification is performed by the choice of args passed to 'setUpMessagesObservableMock'

        sutObserver
                .assertNoErrors()
                .assertNotTerminated()
                .assertSubscribed()
    }

    @Test
    fun whenQuantityProvided_thenRepoIsQueriedWithThisQuantity() {
        // TEST DATA
        val groupId = 13
        val quantity = 4
        val messages = genMessages[groupId, 2, randomness]

        // PREPARE MOCKS
        setUpActiveGroupObservableMock(_activeGroup, group[groupId])
        // only a call to '_repo.getMessages' with here provided args will not throw
        setUpMessagesObservableMock(_repo,
                _MsgsArgs(groupId, quantity, null).to(messages)
        )

        // EXECUTE
        val sutObserver = GetMessagesImpl(_repo, _activeGroup, quantity = quantity)
                .execute()
                .test()

        // VERIFY
        // verification is performed by the choice of args passed to 'setUpMessagesObservableMock'

        sutObserver
                .assertNoErrors()
                .assertNotTerminated()
                .assertSubscribed()
    }

    @Test
    fun whenQuantityNotProvided_thenRepoIsQueriedWithNonnullPositiveQuantity() {
        // TEST DATA
        val groupId = 13
        val whatever = BehaviorSubject.createDefault(emptyList<Message>())

        // PREPARE MOCKS
        setUpActiveGroupObservableMock(_activeGroup, group[groupId])
        `when`(_repo.getMessages(anyIdType(), anyInt(), any()))
                .thenReturn(whatever)

        // EXECUTE
        val sutObserver = GetMessagesImpl(_repo, _activeGroup)
                .execute()
                .test()

        // VERIFY
        val quantityCaptor = ArgumentCaptor.forClass(Int::class.java)
        verify(_repo).getMessages(eq(groupId), quantityCaptor.capture(), any())
        assertTrue(quantityCaptor.value > 0)

        sutObserver
                .assertNoErrors()
                .assertNotTerminated()
                .assertSubscribed()
    }

    @Test
    fun whenActiveGroupIsNoGroup_thenNoMessagesIsYielded() {
        // TEST DATA
        val whatever = BehaviorSubject.createDefault(emptyList<Message>())

        // PREPARE MOCKS
        setUpActiveGroupObservableMock(_activeGroup,  listOf(
                ActiveGroupResult.NoGroup()
        ))
        `when`(_repo.getMessages(anyIdType(), anyInt(), any()))
                .thenReturn(whatever)

        // EXECUTE
        val sutObserver = GetMessagesImpl(_repo, _activeGroup)
                .execute()
                .test()

        // VERIFY
        sutObserver
                .assertNoErrors()
                .assertValues(
                        MessagesResult.NoMessages(ActiveGroupResult.NoGroup())
                )
                .assertNotTerminated()
                .assertSubscribed()
        verifyNoMoreInteractions(_repo)

    }


}
