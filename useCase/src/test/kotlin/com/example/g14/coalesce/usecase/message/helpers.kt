package com.example.g14.coalesce.usecase.message

import com.example.g14.coalesce.entity.Group
import com.example.g14.coalesce.entity.IdType
import com.example.g14.coalesce.entity.Message
import com.example.g14.coalesce.entity.User
import com.example.g14.coalesce.usecase.Repository
import com.example.g14.coalesce.usecase.group.ActiveGroupResult
import com.example.g14.coalesce.usecase.group.GetActiveGroup
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito
import java.util.*

/**
 * Created by Gabriel Fortin
 */

data class _MsgsArgs(
        val groupId: IdType,
        val quantity: Int,
        val timestamp: Long?)

fun anyIdType() = anyInt()

fun <T> Observable<T>.doNotComplete(): Observable<T> =
        this.concatWith { PublishSubject.create<T>() }

/*************************************************************************/

interface UserProvider {
    operator fun get(id: IdType): User
}
interface GroupProvider {
    operator fun get(id: IdType): Group
}
interface MessageProvider {
    operator fun get(id: IdType, u: User, g: Group): Message
}
interface MessagesGenerator {
    operator fun get(gId: IdType, quantity: Int, randGen: Random): List<Message>
}

object TestData {
    val user = object : UserProvider {
        override operator fun get(id: IdType) =
                User(id, "user $id", "")
    }

    val group = object : GroupProvider {
        override operator fun get(id: IdType): Group {
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

    val message = object: MessageProvider {
        override operator fun get(id: IdType, u: User, g: Group) =
                Message(id, "message $id", T_BASE+id, u, g)
    }

    val messageGenerator = object: MessagesGenerator {
        override operator fun get(gId: IdType, quantity: Int, randGen: Random): List<Message> {
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
}

/*************************************************************************/

fun setUpActiveGroupObservableMock(activeGroupUseCase: GetActiveGroup, vararg data: Group) {
    setUpActiveGroupObservableMock(activeGroupUseCase, data.map(ActiveGroupResult::Success))
}

fun setUpActiveGroupObservableMock(activeGroupUseCase: GetActiveGroup, data: List<ActiveGroupResult>) {
    Observable.fromIterable(data)
            .doNotComplete()
            .run { setUpActiveGroupObservableMock(activeGroupUseCase, this) }
}

fun setUpActiveGroupObservableMock(activeGroupUseCase: GetActiveGroup, observable: Observable<ActiveGroupResult>) {
    Mockito.`when`(activeGroupUseCase.execute())
            .thenReturn(observable)
}

/*************************************************************************/

fun setUpMessagesObservableMock(repo: Repository, vararg answers: Pair<_MsgsArgs, List<Message>>) {
    answers
            .map { (args, messages) ->
                val messagesObs = Observable.just(messages)
                        .doNotComplete()
                Pair(args, messagesObs)
            }
            .run { setUpMessagesObservableMock(repo, this) }
}

fun setUpMessagesObservableMock(repo: Repository, answers: List<Pair<_MsgsArgs, Observable<List<Message>>>>) {
    Mockito.`when`(repo.getMessages(anyIdType(), anyInt(), any()))
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
