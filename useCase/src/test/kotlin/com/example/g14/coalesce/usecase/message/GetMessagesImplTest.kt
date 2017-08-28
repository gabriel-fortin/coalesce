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
                message[36, u2, g]
        )
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

    fun anyIdType() = ArgumentMatchers.anyInt()

}
