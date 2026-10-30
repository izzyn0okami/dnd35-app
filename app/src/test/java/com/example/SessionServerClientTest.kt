package com.example

import com.example.dnd35.backup.CharacterBackup
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.session.ParticipantRole
import com.example.dnd35.session.SessionClient
import com.example.dnd35.session.SessionClientState
import com.example.dnd35.session.SessionServer
import kotlinx.coroutines.flow.first
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
