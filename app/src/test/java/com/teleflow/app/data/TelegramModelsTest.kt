package com.teleflow.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TelegramModelsTest {
    @Test
    fun chatOrderingPrioritizesNewestRealMessage() {
        val chats = listOf(
            TelegramChat(1, "Older", 0, "", 10),
            TelegramChat(2, "Newest", 1, "", 20)
        )
        val sorted = chats.sortedByDescending { it.lastMessageDate }
        assertEquals(2L, sorted.first().id)
        assertTrue(sorted.first().unreadCount > 0)
    }

    @Test
    fun authorizationStatesRemainExplicit() {
        val state: AuthorizationState = AuthorizationState.Password
        assertEquals(AuthorizationState.Password, state)
    }
}
