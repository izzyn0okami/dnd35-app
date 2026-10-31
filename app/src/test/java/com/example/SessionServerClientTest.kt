package com.example

import com.example.dnd35.backup.CharacterBackup
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.session.Combatant
import com.example.dnd35.session.CombatState
import com.example.dnd35.session.ParticipantRole
import com.example.dnd35.session.RequestKind
import com.example.dnd35.session.SessionClient
import com.example.dnd35.session.SessionClientState
import com.example.dnd35.session.SessionServer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Teste de integração em JVM puro (ServerSocket/Socket reais em loopback, sem emulador):
 * precisa do Robolectric mesmo assim porque SessionMessage usa org.json.JSONObject, que é
 * um stub sem mock fora do Robolectric (mesma causa documentada em SessionMessageTest).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SessionServerClientTest {

    @Test
    fun `jogador conecta, recebe Welcome e aparece no roster do Mestre`() = runBlocking {
        val server = SessionServer(sessionId = "sess-1", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Legolas")

                val connected = withTimeout(5_000) {
                    client.state.first { it is SessionClientState.Connected } as SessionClientState.Connected
                }
                assertEquals("sess-1", connected.sessionId)

                // O servidor deve propagar o novo participante no roster (Mestre + Jogador).
                val roster = withTimeout(5_000) {
                    server.roster.first { it.size == 2 }
                }
                assertTrue(roster.any { it.role == ParticipantRole.DM })
                val player = roster.first { it.role == ParticipantRole.PLAYER }
                assertEquals("Legolas", player.displayName)
                assertEquals(connected.selfId, player.id)
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `ao sair, jogador some do roster do Mestre`() = runBlocking {
        val server = SessionServer(sessionId = "sess-2", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val client = SessionClient()
            client.connect("127.0.0.1", port, "Gimli")
            withTimeout(5_000) { client.state.first { it is SessionClientState.Connected } }
            withTimeout(5_000) { server.roster.first { it.size == 2 } }

            client.disconnect()

            val rosterAfterLeave = withTimeout(5_000) { server.roster.first { it.size == 1 } }
            assertEquals(ParticipantRole.DM, rosterAfterLeave.single().role)
        } finally {
            server.stop()
        }
    }

    @Test
    fun `ficha enviada pelo jogador aparece do lado do Mestre`() = runBlocking {
        val server = SessionServer(sessionId = "sess-3", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val character = CharacterEntity(name = "Legolas", characterClass = "Patrulheiro", level = 5)
            val characterJson = CharacterBackup.toJson(listOf(character)) { null }

            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Legolas", characterJson)
                val connected = withTimeout(5_000) {
                    client.state.first { it is SessionClientState.Connected } as SessionClientState.Connected
                }

                val received = withTimeout(5_000) {
                    server.receivedCharacters.first { it.containsKey(connected.selfId) }
                }
                val entity = received.getValue(connected.selfId)
                assertEquals("Legolas", entity.name)
                assertEquals("Patrulheiro", entity.characterClass)
                assertEquals(5, entity.level)
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `jogador sem ficha nao aparece em receivedCharacters`() = runBlocking {
        val server = SessionServer(sessionId = "sess-4", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Observador")
                withTimeout(5_000) { client.state.first { it is SessionClientState.Connected } }
                // Dá tempo da mensagem (se houvesse) chegar antes de garantir que nada chegou.
                withTimeout(5_000) { server.roster.first { it.size == 2 } }
                assertTrue(server.receivedCharacters.value.isEmpty())
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `startCombat sincroniza estado de combate com o jogador conectado`() = runBlocking {
        val server = SessionServer(sessionId = "sess-5", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Legolas")
                val connected = withTimeout(5_000) {
                    client.state.first { it is SessionClientState.Connected } as SessionClientState.Connected
                }

                val combatants = listOf(
                    Combatant(id = connected.selfId, displayName = "Legolas", isNpc = false, initiative = 18, currentHp = 20, maxHp = 20),
                    Combatant(id = CombatState.npcCombatantId(1L), displayName = "Goblin", isNpc = true, initiative = 9, currentHp = 7, maxHp = 7)
                )
                server.startCombat(combatants)

                val clientCombat = withTimeout(5_000) {
                    client.state.mapNotNull { (it as? SessionClientState.Connected)?.combat }.first()
                }
                assertEquals(listOf(connected.selfId, CombatState.npcCombatantId(1L)), clientCombat.combatants.map { it.id })
                assertEquals(0, clientCombat.currentTurnIndex)
                assertEquals(1, clientCombat.round)
                assertEquals(connected.selfId, clientCombat.currentCombatantId)
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `applyDamage, nextTurn e endCombat sincronizam com o jogador`() = runBlocking {
        val server = SessionServer(sessionId = "sess-6", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Gimli")
                val connected = withTimeout(5_000) {
                    client.state.first { it is SessionClientState.Connected } as SessionClientState.Connected
                }

                server.startCombat(
                    listOf(
                        Combatant(id = connected.selfId, displayName = "Gimli", isNpc = false, initiative = 15, currentHp = 30, maxHp = 30)
                    )
                )
                withTimeout(5_000) {
                    client.state.mapNotNull { (it as? SessionClientState.Connected)?.combat }.first()
                }

                server.applyDamage(connected.selfId, 12)
                val afterDamage = withTimeout(5_000) {
                    client.state.mapNotNull { (it as? SessionClientState.Connected)?.combat }
                        .first { it.combatants.first().currentHp == 18 }
                }
                assertEquals(18, afterDamage.combatants.single().currentHp)

                server.nextTurn()
                val afterNextTurn = withTimeout(5_000) {
                    client.state.mapNotNull { (it as? SessionClientState.Connected)?.combat }
                        .first { it.round == 2 }
                }
                assertEquals(0, afterNextTurn.currentTurnIndex)
                assertEquals(2, afterNextTurn.round)

                server.endCombat()
                val afterEnd = withTimeout(5_000) {
                    client.state.first { (it as? SessionClientState.Connected)?.combat == null }
                } as SessionClientState.Connected
                assertEquals(null, afterEnd.combat)
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `jogador que entra durante combate ja recebe o estado atual`() = runBlocking {
        val server = SessionServer(sessionId = "sess-7", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            // Combate iniciado só com um NPC do Mestre, antes de qualquer jogador conectar.
            server.startCombat(
                listOf(Combatant(id = CombatState.npcCombatantId(2L), displayName = "Orc", isNpc = true, initiative = 11, currentHp = 15, maxHp = 15))
            )

            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Retardatário")
                val lateJoinCombat = withTimeout(5_000) {
                    client.state.mapNotNull { (it as? SessionClientState.Connected)?.combat }.first()
                }
                assertEquals(CombatState.npcCombatantId(2L), lateJoinCombat.combatants.single().id)
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `pedido de mudanca de PV aprovado aplica dano e avisa o jogador`() = runBlocking {
        val server = SessionServer(sessionId = "sess-8", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Legolas")
                val connected = withTimeout(5_000) {
                    client.state.first { it is SessionClientState.Connected } as SessionClientState.Connected
                }
                server.startCombat(
                    listOf(Combatant(id = connected.selfId, displayName = "Legolas", isNpc = false, initiative = 12, currentHp = 20, maxHp = 20))
                )
                withTimeout(5_000) { client.state.mapNotNull { (it as? SessionClientState.Connected)?.combat }.first() }

                client.requestAction(RequestKind.COMBAT_HP_CHANGE, "Pedido de dano (5 PV)", combatantId = connected.selfId, hpDelta = 5)

                val pending = withTimeout(5_000) { server.pendingRequests.first { it.isNotEmpty() } }
                val request = pending.single()
                assertEquals("Legolas", request.requesterName)
                assertEquals(RequestKind.COMBAT_HP_CHANGE, request.kind)

                server.approveRequest(request.id)

                val resolved = withTimeout(5_000) { client.requestResults.first() }
                assertTrue(resolved.approved)

                val combatAfter = withTimeout(5_000) {
                    client.state.mapNotNull { (it as? SessionClientState.Connected)?.combat }
                        .first { it.combatants.first().currentHp == 15 }
                }
                assertEquals(15, combatAfter.combatants.single().currentHp)
                assertTrue(server.pendingRequests.value.isEmpty())
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `pedido negado nao altera o estado e avisa o jogador`() = runBlocking {
        val server = SessionServer(sessionId = "sess-9", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Gimli")
                val connected = withTimeout(5_000) {
                    client.state.first { it is SessionClientState.Connected } as SessionClientState.Connected
                }
                server.startCombat(
                    listOf(Combatant(id = connected.selfId, displayName = "Gimli", isNpc = false, initiative = 10, currentHp = 30, maxHp = 30))
                )
                withTimeout(5_000) { client.state.mapNotNull { (it as? SessionClientState.Connected)?.combat }.first() }

                client.requestAction(RequestKind.COMBAT_HP_CHANGE, "Pedido de cura (10 PV)", combatantId = connected.selfId, hpDelta = -10)
                val request = withTimeout(5_000) { server.pendingRequests.first { it.isNotEmpty() } }.single()

                server.denyRequest(request.id, reason = "Sem poção de cura disponível")

                val resolved = withTimeout(5_000) { client.requestResults.first() }
                assertEquals(false, resolved.approved)
                assertEquals("Sem poção de cura disponível", resolved.reason)
                assertTrue(server.pendingRequests.value.isEmpty())
                assertEquals(30, server.combatState.value?.combatants?.single()?.currentHp)
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `pedido de atualizacao de ficha aprovado atualiza receivedCharacters`() = runBlocking {
        val server = SessionServer(sessionId = "sess-10", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val original = CharacterEntity(name = "Legolas", characterClass = "Patrulheiro", level = 5, currentHp = 30, maxHp = 40)
            val originalJson = CharacterBackup.toJson(listOf(original)) { null }

            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Legolas", originalJson)
                val connected = withTimeout(5_000) {
                    client.state.first { it is SessionClientState.Connected } as SessionClientState.Connected
                }
                withTimeout(5_000) { server.receivedCharacters.first { it.containsKey(connected.selfId) } }

                val updated = original.copy(currentHp = 25)
                val updatedJson = CharacterBackup.toJson(listOf(updated)) { null }
                client.requestAction(RequestKind.CHARACTER_UPDATE, "Atualização da ficha de Legolas", characterJson = updatedJson)

                val request = withTimeout(5_000) { server.pendingRequests.first { it.isNotEmpty() } }.single()
                assertEquals(RequestKind.CHARACTER_UPDATE, request.kind)

                server.approveRequest(request.id)
                withTimeout(5_000) { client.requestResults.first() }

                val finalCharacters = withTimeout(5_000) {
                    server.receivedCharacters.first { it[connected.selfId]?.currentHp == 25 }
                }
                assertEquals(25, finalCharacters.getValue(connected.selfId).currentHp)
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `conectar em porta sem servidor resulta em Disconnected, nao em excecao`() = runBlocking {
        val client = SessionClient()
        // Porta 1 é privilegiada/sem listener nos ambientes de teste: a conexão deve falhar
        // de forma controlada, nunca lançar uma exceção para fora do SessionClient.
        client.connect("127.0.0.1", 1, "Jogador")
        val result = withTimeout(5_000) {
            client.state.first { it is SessionClientState.Disconnected }
        }
        assertTrue((result as SessionClientState.Disconnected).reason.isNotBlank())
    }
}
