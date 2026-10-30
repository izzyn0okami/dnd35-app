package com.example

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

/**
 * Teste de integração em JVM puro: SessionServer/SessionClient usam apenas java.net
 * (ServerSocket/Socket), então rodam em loopback (127.0.0.1) sem precisar de Robolectric
 * nem de um aparelho/emulador Android de verdade.
 */
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
