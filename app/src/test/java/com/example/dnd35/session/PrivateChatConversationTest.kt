package com.example.dnd35.session

import org.junit.Assert.assertEquals
import org.junit.Test

class PrivateChatConversationTest {
    @Test
    fun filtersByParticipantIdWhenNamesAreEqual() {
        val first = PrivateChatEntry(id = "1", participantId = "player-1", participantName = "Legolas", text = "Olá", fromDm = false)
        val second = PrivateChatEntry(id = "2", participantId = "player-2", participantName = "Legolas", text = "Oi", fromDm = false)
        val reply = PrivateChatEntry(id = "3", participantId = "player-1", participantName = "Legolas", text = "Tudo bem?", fromDm = true)

        assertEquals(listOf(first, reply), privateChatConversationFor("player-1", listOf(first, second, reply)))
        assertEquals(listOf(second), privateChatConversationFor("player-2", listOf(first, second, reply)))
    }

    @Test
    fun noSelectedParticipantDoesNotExposeAnyConversation() {
        val entry = PrivateChatEntry(participantId = "player-1", participantName = "Legolas", text = "Segredo", fromDm = false)

        assertEquals(emptyList<PrivateChatEntry>(), privateChatConversationFor(null, listOf(entry)))
        assertEquals(emptyList<PrivateChatEntry>(), privateChatConversationFor("", listOf(entry)))
    }
}
