package com.example.g14.coalesce.usecase.message

import com.example.g14.coalesce.entity.Group
import com.example.g14.coalesce.entity.IdType
import com.example.g14.coalesce.entity.Message
import com.example.g14.coalesce.entity.User
import com.example.g14.coalesce.usecase.Repository
import com.example.g14.coalesce.usecase.group.ActiveGroupResult
import com.example.g14.coalesce.usecase.group.GetActiveGroup
import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import java.util.*

/**
 * Created by Gabriel Fortin
 */

/** starting value for "User" ids */
const val U_BASE: IdType = 100
/** starting value for "Group" ids */
const val G_BASE: IdType = 20000
/** starting value for "timestamp" */
const val T_BASE: Long = 3000000L

class GetMessagesImplTest {
    /** DATA GENERATORS */
    private val user = object {
        operator fun get(id: IdType) =
                User(id, "user ${id}", "")
    }
    private val group = object {
        operator fun get(id: IdType): Group {
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


    lateinit var _repo: Repository
    lateinit var _activeGroup: GetActiveGroup

    @Before
    fun setUp() {
        _repo = mock(Repository::class.java)
        _activeGroup = mock(GetActiveGroup::class.java)
    }


    @Test
    fun switchingActiveGroup() {
//    fun whenGroupExists_whenMessagesForGroupArePresent_thenMessagesAreReturned() {
        // PREPARE
        val messagesLimit = 1 //14

        val _repo = mock(Repository::class.java)
        val _activeGroup = mock(GetActiveGroup::class.java)
        val sut: GetMessages = GetMessagesImpl(_repo, _activeGroup, null, messagesLimit)

        val activeGroups = listOf(group[1], group[2])
        val activeGroupsObs: Observable<ActiveGroupResult> =
                activeGroups
                .map(ActiveGroupResult::Success)
                .toObservable<ActiveGroupResult>()
                        .doOnNext { println("active group - onNext") }
                        .doOnError { println("active group - onError"); it.printStackTrace() }
                        .doOnComplete { println("active group - onComplete") }

        `when`(_activeGroup.execute())
                .thenReturn(activeGroupsObs)

        `when`(_repo.getMessages(eq(group[1].id), /*quantity:*/ anyInt(), /*timeLimit:*/ any<Long>()))
                .thenAnswer { inv ->
                    val quantity = inv.getArgument<Int>(1)
                    val groupId = inv.getArgument<IdType>(0)
                    val timeLimit = inv.getArgument<Long>(2) ?: Long.MAX_VALUE

                    genMessages[groupId, quantity, randomness]
                            .filter { it.timestamp < timeLimit }
                            .let { Observable.just(it) }
                            as Observable<List<Message>>
                }
        `when`(_repo.getMessages(eq(group[2].id), /*quantity:*/ anyInt(), /*timeLimit:*/ any<Long>()))
                .thenAnswer { inv ->
                    val quantity = inv.getArgument<Int>(1)
                    val groupId = inv.getArgument<IdType>(0)
                    val timeLimit = inv.getArgument<Long>(2) ?: Long.MAX_VALUE

                    genMessages[groupId, quantity, randomness]
                            .filter { it.timestamp < timeLimit }
                            .let { Observable.just(it) }
                            as Observable<List<Message>>
                }
        `when`(_repo.getMessages(anyInt(), anyInt(), anyLong()))
                .thenThrow(RuntimeException("Aaa, we didn't handle this case!"))

        // EXECUTE
        val actual: Observable<MessagesResult> = sut.execute()
                .doOnNext { msgsRes ->
                    val info = if (msgsRes is MessagesResult.Success) "${msgsRes.list.size} shouts"
                            else "no messages"
                    println("SUT - onNext - $info")
                }
                .doOnComplete { println("SUT - onComplete")}
                .doOnError { println("SUT - onError - $it"); it.printStackTrace() }
        val testObs = actual.test()

        // VERIFY
        val expectedValues: Array<MessagesResult> = activeGroups  // List<Group>
                .map { g -> genMessages[g.id, messagesLimit, randomness] }  // List<List<Message>>
                .map { msgs -> MessagesResult.Success(msgs) }   // List<MessagesResult>
                .toTypedArray()
        println("EXPECTED VALUES:")
        expectedValues.forEach { msgRes ->
            when (msgRes) {
                is MessagesResult.Success -> {
                    println("There are ${msgRes.list.size} messages:")
                    msgRes.list.forEach { msg ->
                        println("  ${msg.sender.name}  says  ${msg.text}")
                    }
                }
                is MessagesResult.NoMessages -> println("no messages")
            }
        }
//        println(expectedValues)
        testObs.assertValueCount(expectedValues.size)
        testObs.assertValues(*expectedValues)
        testObs.assertNotComplete()


//        TODO("finish implementing...")
    }


    @Test
    fun justOneGroup_justOneMessagesList() {
        val timestamp = Long.MAX_VALUE
        val quantity = 14
        val groupId = 3

        val g = group[groupId]
        val u1 = g.members.elementAt(0)
        val u2 = g.members.elementAt(1)
        val messages1 = listOf(
                message[17, u1, g],
                message[24, u2, g],
                message[35, u1, g],
                message[36, u2, g])
        val messagesResult1 = MessagesResult.Success(messages1)

        val messagesSubject1 = BehaviorSubject.createDefault<List<Message>>(messages1)
        val activeGroupsSubject = PublishSubject.create<ActiveGroupResult>()

        `when`(_repo.getMessages(eq(groupId), eq(quantity), eq(timestamp)))
                .thenReturn(messagesSubject1.hide())
        `when`(_activeGroup.execute())
                .thenReturn(activeGroupsSubject.hide())



        val sut: GetMessages = GetMessagesImpl(_repo, _activeGroup, timestamp, quantity)

        val testObserver = sut.execute().test()

        activeGroupsSubject.onNext(ActiveGroupResult.Success(g))






        val expectedValues: List<MessagesResult> = listOf(messagesResult1)

        testObserver.assertValueSequence(expectedValues)
        testObserver.assertNotComplete()



    }

    @Test
    fun whenOutputObservableIsDisposed_thenActiveGroupObservableIsDisposed() {
        // TEST DATA
        val messagesAnswer = arrayOf(
                emptyList<Message>(),  // not used
                emptyList<Message>(),
                genMessages[2, 1, Random(123L)]
        )
        val valueBeforeReSubscription = ActiveGroupResult.Success(group[1])
        val valueAfterReSubscription = ActiveGroupResult.Success(group[2])

        // PREPARE MOCKS
        val activeGroupSubject = PublishSubject.create<ActiveGroupResult>()
        // this will not replay values after all subscribers disconnect
        val replayableObs = activeGroupSubject.replay().refCount()

        `when`(_activeGroup.execute())
                .thenReturn(replayableObs)
        `when`(_repo.getMessages(anyIdType(), anyInt(), any()))
                .thenAnswer { inv ->
                    val groupId = inv.getArgument<IdType>(0) as Int
                    BehaviorSubject.createDefault(messagesAnswer[groupId])
                }

        // EXECUTE
        val sutDisposable = GetMessagesImpl(_repo, _activeGroup)
                .execute()
                .subscribe()

        activeGroupSubject.onNext(valueBeforeReSubscription)

        // the only observer of 'replayableObs' sits in sut
        sutDisposable.dispose()
        // so its replay buffer should be lost now

        // let's see how much can be replayed
        val testObs = replayableObs.test()
        activeGroupSubject.onNext(valueAfterReSubscription)

        // VERIFY
        // only values emitted after second subscription to 'replayableObs' should be observed
        testObs.assertValues(valueAfterReSubscription)
    }

    /** Verifies if <code>repo.getMessages</code>'s observable is being disposed by checking
     * whether the replay buffer for this observable was dropped. Dropping the buffer occurs
     * when the observable is disposed.
     */
    @Test
    fun whenOutputObservableIsDisposed_thenMessagesObservableIsDisposed() {
        // TEST DATA
        val g: Group = group[1]
        val rand = Random(333)
        val messagesBeforeDisposal: List<Message> = genMessages[g.id, 3, rand]
        val messagesAfterDisposal: List<Message> = genMessages[g.id, 1, rand]

        // PREPARE MOCKS
        val messagesSubject = PublishSubject.create<List<Message>>()
        // if all subscribers disconnect then replay values will be dropped
        val replayableObs = messagesSubject.replay().refCount()

        val activeGroupSub = BehaviorSubject
                .createDefault<ActiveGroupResult>(ActiveGroupResult.Success(g))

        `when`(_activeGroup.execute())
                .thenReturn(activeGroupSub)
        `when`(_repo.getMessages(eq(g.id), anyInt(), isNull()))
                .thenAnswer { inv ->
                    val quantity = inv.getArgument<Int>(1)
                    replayableObs.map { it.take(quantity) }
                }

        // EXECUTE
        val sutDisposable = GetMessagesImpl(_repo, _activeGroup)
                .execute()
                .subscribe()

        // we expect that 'messagesBeforeDisposal' will not be replayed later
        messagesSubject.onNext(messagesBeforeDisposal)

        // after this the replay buffer should be lost
        sutDisposable.dispose()

        // VERIFY
        // let's observe again; a new replay buffer should be in use
        val testObs = replayableObs.test()

        // put something in it
        messagesSubject.onNext(messagesAfterDisposal)

        // check that indeed the buffer was dropped
        testObs.assertValues(messagesAfterDisposal)
    }

    @Test
    fun whenActiveGroupChanges_thenNewResultIsYielded() {
        // TEST DATA
        val r = Random(111)
        val messages2 = genMessages[2, 5, r]
        val messages8 = genMessages[8, 6, r]
        val expectedValues: List<MessagesResult> =
                arrayOf(messages2, messages8)
                        .map(MessagesResult::Success)

        // PREPARE MOCKS
        val groupSubject = BehaviorSubject.create<Group>()
        val messagesSubject2 = BehaviorSubject.createDefault(messages2)
                .publish()
                .autoConnect()
        val messagesSubject8 = BehaviorSubject.createDefault(messages8)
                .publish()
                .autoConnect()

        `when`(_activeGroup.execute())
                .thenReturn(groupSubject.map(ActiveGroupResult::Success))
        `when`(_repo.getMessages(anyIdType(), anyInt(), isNull()))
                .thenAnswer { inv ->
                    val groupId = inv.getArgument<IdType>(0)
                    when (groupId) {
                        2 -> messagesSubject2
                        8 -> messagesSubject8
                        else -> throw RuntimeException("unexpected arg when calling _repo.getMessages(…)")
                    }
                }

        // EXECUTE
        val testObs = GetMessagesImpl(_repo, _activeGroup)
                .execute()
                .test()

        groupSubject.onNext(group[2])
        groupSubject.onNext(group[8])

        // VERIFY
        testObs.assertValueSequence(expectedValues)
        testObs.assertNotComplete()
    }

    @Test
    fun whenActiveGroupChanges_thenMessagesForPreviousGroupAreNotYielded() {
        TODO("test not implemented!!!1")
    }

    @Test
    fun whenRepoHasNewMessages_thenNewMessagesAreYielded() {
        TODO("test not implemented!!!1")
    }

    @Test
    fun whenTimestampProvided_thenRepoIsQueriedWithThisTimestamp() {
        TODO("test not implemented!!!1")
    }

    @Test
    fun whenQuantityProvided_thenRepoIsQueriedWithThisQuantity() {
        TODO("test not implemented!!!1")
    }

    @Test
    fun whenQuantityNotProvided_thenRepoIsQueriedWithNonnullPositiveQuantity() {
        TODO("test not implemented!!!1")
    }

    @Test
    fun whenActiveGroupIsNoGroup_thenNoMessagesIsYielded() {
        TODO("test not implemented!!!1")
    }

    fun anyIdType() = ArgumentMatchers.anyInt()

}
