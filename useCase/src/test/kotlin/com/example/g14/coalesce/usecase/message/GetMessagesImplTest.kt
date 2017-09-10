package com.example.g14.coalesce.usecase.message

import com.example.g14.coalesce.entity.Group
import com.example.g14.coalesce.entity.IdType
import com.example.g14.coalesce.entity.Message
import com.example.g14.coalesce.entity.User
import com.example.g14.coalesce.usecase.Repository
import com.example.g14.coalesce.usecase.group.ActiveGroupResult
import com.example.g14.coalesce.usecase.group.GetActiveGroup
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
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
    private val user = object {
        operator fun get(id: IdType) =
                User(id, "user $id", "")
    }
    private val group = object {
        operator fun get(id: IdType): Group {
            @Suppress("USELESS_CAST")
            val users =
                    (1..(id as Int))  // cast IdType to Int (even if unnecessary for current IdType)
                    .map { user[(id as Int)*10 + it] }
                    .toSet()
            return Group(id, users)
            // what user ids would be yielded in a resulting group
            //      2 -> {21, 22}
            //      3 -> {31, 32, 33}
        }
    }
    private val message = object {
        operator fun get(id: IdType, u: User, g: Group) =
                Message(id, "message $id", T_BASE+id, u, g)
    }
    private val genMessages = object {
        operator fun get(gId: IdType, quantity: Int, randGen: Random): List<Message> {
            @Suppress("USELESS_CAST")
            val group = group[gId as Int]
            val randomUserIn = { g: Group ->
                val randomIndex = randGen.nextInt(g.members.size)
                g.members.asIterable().elementAt(randomIndex)
            }

            return (1..quantity)
                    .map { i -> message[i, randomUserIn(group), group] }
                    .toList()
        }
    }
    private val randomness = Random(14*14)

    /*************************************************************************/

    lateinit var _repo: Repository
    lateinit var _activeGroup: GetActiveGroup

    @Before
    fun setUp() {
        _repo = mock(Repository::class.java)
        _activeGroup = mock(GetActiveGroup::class.java)
    }

    fun setUpActiveGroupObservableMock(vararg data: Group) {
        setUpActiveGroupObservableMock(data.map(ActiveGroupResult::Success))
    }

    fun setUpActiveGroupObservableMock(data: List<ActiveGroupResult>) {
        Observable.fromIterable(data)
                .doNotComplete()
                .run { setUpActiveGroupObservableMock(this) }
    }

    fun setUpActiveGroupObservableMock(observable: Observable<ActiveGroupResult>) {
        `when`(_activeGroup.execute())
                .thenReturn(observable)
    }

    fun setUpMessagesObservableMock(vararg answers: Pair<_MsgsArgs, List<Message>>) {
        answers
                .map { (args, messages) ->
                    val messagesObs = Observable.just(messages)
                            .doNotComplete()
                    Pair(args, messagesObs)
                }
                .run { setUpMessagesObservableMock(this) }
    }

    fun setUpMessagesObservableMock(answers: List<Pair<_MsgsArgs, Observable<List<Message>>>>) {
        `when`(_repo.getMessages(anyIdType(), anyInt(), any()))
                .thenAnswer { inv ->
                    val actualArgs = _MsgsArgs(
                            inv.getArgument<IdType>(0),
                            inv.getArgument<Int>(1),
                            inv.getArgument<Long?>(2)
                    )
                    answers.firstOrNull { (args, _) -> args == actualArgs }
                            // try to return observable for matched args
                            ?.second
                            // otherwise throw an exception
                            ?: throw RuntimeException("no answer provided for args $actualArgs")
                }
    }

    data class _MsgsArgs(
            val groupId: IdType,
            val quantity: Int,
            val timestamp: Long?)

    fun <T> Observable<T>.doNotComplete(): Observable<T> =
            this.concatWith { PublishSubject.create<T>() }

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
        setUpActiveGroupObservableMock(group[groupId])
        setUpMessagesObservableMock(
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

        setUpActiveGroupObservableMock(replayableObs)
        setUpMessagesObservableMock(
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

        setUpActiveGroupObservableMock(group[groupId])
        setUpMessagesObservableMock( listOf(
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

        setUpActiveGroupObservableMock(groupSubject.map<ActiveGroupResult>(ActiveGroupResult::Success))
        setUpMessagesObservableMock(
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

        setUpActiveGroupObservableMock(groupSubject)
        setUpMessagesObservableMock( listOf(
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

        setUpActiveGroupObservableMock(group[groupA])
        setUpMessagesObservableMock( listOf(
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
        setUpActiveGroupObservableMock(group[groupId])
        // only a call to '_repo.getMessages' with here provided args will not throw
        setUpMessagesObservableMock(
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
        setUpActiveGroupObservableMock(group[groupId])
        // only a call to '_repo.getMessages' with here provided args will not throw
        setUpMessagesObservableMock(
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
        setUpActiveGroupObservableMock(group[groupId])
        // only a call to '_repo.getMessages' with here provided args will not throw
        setUpMessagesObservableMock(
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
        setUpActiveGroupObservableMock(group[groupId])
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
        setUpActiveGroupObservableMock( listOf(
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

    fun anyIdType() = ArgumentMatchers.anyInt()

}
