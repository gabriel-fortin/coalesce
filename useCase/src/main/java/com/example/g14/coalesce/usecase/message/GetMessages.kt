package com.example.g14.coalesce.usecase.message

import com.example.g14.coalesce.entity.Message
import com.example.g14.coalesce.usecase.NoData
import com.example.g14.coalesce.usecase.ObservableUseCase
import com.example.g14.coalesce.usecase.Repository
import com.example.g14.coalesce.usecase.group.ActiveGroupResult
import com.example.g14.coalesce.usecase.group.GetActiveGroup
import io.reactivex.Observable

/**
 * Created by Gabriel Fortin
 */

sealed class MessagesResult {
    data class Success(val list: List<Message>) : MessagesResult()
    data class NoMessages(val reason: NoData?) : NoData, MessagesResult() {
        override fun reason(): NoData? = reason
    }
}

interface GetMessages : ObservableUseCase<MessagesResult> {
    override fun execute(): Observable<MessagesResult>
}

class GetMessagesImpl(
        val repo: Repository,
        val activeGroupUseCase: GetActiveGroup,
        val timestamp: Long? = null,
        val quantity: Int = 10
) : GetMessages {

    fun fromBefore(timestamp: Long): GetMessages =
            GetMessagesImpl(repo, activeGroupUseCase, timestamp, quantity)

    fun limitedTo(quantity: Int): GetMessages =
            GetMessagesImpl(repo, activeGroupUseCase, timestamp, quantity)

    override fun execute(): Observable<MessagesResult> {
        val activeGroup = activeGroupUseCase.execute()
        val successGroups: Observable<MessagesResult> =
                activeGroup
                .ofType(ActiveGroupResult.Success::class.java)
                .flatMap { repo.getMessages(it.group.id, quantity, before = timestamp) }
                .map(MessagesResult::Success)
        val failGroups: Observable<MessagesResult> =
                activeGroup
                .ofType(ActiveGroupResult.NoGroup::class.java)
                .map(MessagesResult::NoMessages)
        return Observable.merge(successGroups, failGroups)
    }

}
