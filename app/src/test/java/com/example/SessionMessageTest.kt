package com.example

import com.example.dnd35.session.JoinPayload
import com.example.dnd35.session.Participant
import com.example.dnd35.session.ParticipantRole
import com.example.dnd35.session.SessionMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testes puros de JVM (sem Robolectric): o protocolo da sessão só usa org.json e java.net,
 * nada de android.util.Log, então não precisa de mock de Android aqui.
 */
class SessionMessageTest {

    @Test
    fun `Hello serializa e desserializa corretamente`() {
        val msg = SessionMessage.Hello("Aragorn")
        val roundTrip = SessionMessage.fromJson(msg.toJson())
        assertEquals(msg, roundTrip)
    }

    @Test
    fun `Welcome serializa e desserializa corretamente`() {
        val msg = SessionMessage.Welcome(participantId = "p1", sessionId = "s1")
        val roundTrip = SessionMessage.fromJson(msg.toJson())
        assertEquals(msg, roundTrip)
    }

    @Test
    fun `Roster preserva a lista de participantes, incluindo characterId nulo`() {
        val participants = listOf(
            Participant("dm", "Mestre", ParticipantRole.DM),
            Participant("p1", "Legolas", ParticipantRole.PLAYER, characterId = 42L)
        )
        val msg = SessionMessage.Roster(participants)
        val roundTrip = SessionMessage.fromJson(msg.toJson()) as SessionMessage.Roster
        assertEquals(participants, roundTrip.participants)
    }

    @Test
    fun `Bye com motivo vazio por padrao`() {
        val msg = SessionMessage.Bye()
        val roundTrip = SessionMessage.fromJson(msg.toJson()) as SessionMessage.Bye
        assertEquals("", roundTrip.reason)
    }

    @Test
    fun `JSON invalido ou tipo desconhecido retorna null`() {
        assertNull(SessionMessage.fromJson("{not json"))
        assertNull(SessionMessage.fromJson("""{"type":"ALGO_DESCONHECIDO"}"""))
    }

    @Test
    fun `JoinPayload serializa e desserializa corretamente`() {
        val payload = JoinPayload(
            sessionId = "abc-123",
            hostDisplayName = "Mestre",
            hostAddress = "192.168.0.10",
            port = 54321
        )
        val roundTrip = JoinPayload.fromJson(payload.toJson())
        assertEquals(payload, roundTrip)
    }

    @Test
    fun `JoinPayload invalido retorna null em vez de lancar excecao`() {
        assertNull(JoinPayload.fromJson("isso nao e um QR da ficha"))
        assertNull(JoinPayload.fromJson("""{"sessionId":"s1"}"""))
    }

    @Test
    fun `newSessionId gera valores diferentes a cada chamada`() {
        val a = JoinPayload.newSessionId()
        val b = JoinPayload.newSessionId()
        assertTrue(a.isNotBlank())
        assertTrue(a != b)
    }
}
