package com.example

import com.example.dnd35.session.Combatant
import com.example.dnd35.session.CombatState
import com.example.dnd35.session.JoinPayload
import com.example.dnd35.session.Participant
import com.example.dnd35.session.ParticipantRole
import com.example.dnd35.session.RequestKind
import com.example.dnd35.session.SessionMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Robolectric: mesmo não usando nenhuma classe exclusiva do Android, org.json.JSONObject
// (usado em SessionMessage/JoinPayload) é um stub em testes de JVM puro e lança
// "Method put in org.json.JSONObject not mocked" sem o Robolectric mockar essas classes.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
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
    fun `CharacterSheet preserva o JSON da ficha`() {
        val msg = SessionMessage.CharacterSheet("""{"app":"ficha-dnd35","characters":[]}""")
        val roundTrip = SessionMessage.fromJson(msg.toJson()) as SessionMessage.CharacterSheet
        assertEquals(msg.characterJson, roundTrip.characterJson)
    }

    @Test
    fun `CombatUpdate preserva combatentes, turno e rodada`() {
        val state = CombatState(
            combatants = listOf(
                Combatant(id = "p1", displayName = "Legolas", isNpc = false, initiative = 18, currentHp = 20, maxHp = 20, tempHp = 5, conditions = listOf("Abençoado")),
                Combatant(id = CombatState.npcCombatantId(99L), displayName = "Goblin", isNpc = true, initiative = 10, currentHp = 7, maxHp = 7)
            ),
            currentTurnIndex = 1,
            round = 3
        )
        val msg = SessionMessage.CombatUpdate(state)
        val roundTrip = SessionMessage.fromJson(msg.toJson()) as SessionMessage.CombatUpdate
        assertEquals(state, roundTrip.state)
    }

    @Test
    fun `EndCombat serializa e desserializa corretamente`() {
        val roundTrip = SessionMessage.fromJson(SessionMessage.EndCombat.toJson())
        assertEquals(SessionMessage.EndCombat, roundTrip)
    }

    @Test
    fun `ActionRequested de mudanca de PV preserva todos os campos`() {
        val msg = SessionMessage.ActionRequested(
            kind = RequestKind.COMBAT_HP_CHANGE,
            summary = "Pedido de dano (5 PV) em combate",
            combatantId = "p1",
            hpDelta = 5
        )
        val roundTrip = SessionMessage.fromJson(msg.toJson()) as SessionMessage.ActionRequested
        assertEquals(msg, roundTrip)
    }

    @Test
    fun `ActionRequested de atualizacao de ficha preserva o JSON e omite campos de combate`() {
        val msg = SessionMessage.ActionRequested(
            kind = RequestKind.CHARACTER_UPDATE,
            summary = "Atualização da ficha de Legolas",
            characterJson = """{"app":"ficha-dnd35","characters":[]}"""
        )
        val roundTrip = SessionMessage.fromJson(msg.toJson()) as SessionMessage.ActionRequested
        assertEquals(msg, roundTrip)
        assertNull(roundTrip.combatantId)
        assertNull(roundTrip.hpDelta)
    }

    @Test
    fun `RequestResolved preserva aprovacao e motivo`() {
        val approved = SessionMessage.RequestResolved("req-1", approved = true)
        assertEquals(approved, SessionMessage.fromJson(approved.toJson()))

        val denied = SessionMessage.RequestResolved("req-2", approved = false, reason = "Sem itens suficientes")
        assertEquals(denied, SessionMessage.fromJson(denied.toJson()))
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
