package com.example.dnd35.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Lado do Mestre: servidor TCP local que aceita conexões dos Jogadores na mesma rede.
 *
 * O Mestre é a autoridade sobre o estado da sessão — por isso o servidor mora aqui, e a lista de
 * participantes ([roster]) é a fonte da verdade que é replicada para todos os clientes conectados.
 */
class SessionServer(
    private val sessionId: String,
    hostDisplayName: String
) {
    private var serverSocket: ServerSocket? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val clients = ConcurrentHashMap<String, ClientHandle>()

    private val dmParticipant = Participant(
        id = DM_PARTICIPANT_ID,
        displayName = hostDisplayName,
        role = ParticipantRole.DM
    )

    private val _roster = MutableStateFlow(listOf(dmParticipant))
    val roster: StateFlow<List<Participant>> = _roster

    private class ClientHandle(val id: String, val socket: Socket, val out: DataOutputStream)

    /** Inicia o servidor em uma porta livre escolhida pelo sistema e retorna essa porta. */
    fun start(): Int {
        val socket = ServerSocket(0)
        serverSocket = socket
        scope.launch { acceptLoop(socket) }
        return socket.localPort
    }

    /** Encerra a sessão: fecha todas as conexões e para de aceitar novas. */
    fun stop() {
        runCatching { serverSocket?.close() }
        clients.values.forEach { runCatching { it.socket.close() } }
        clients.clear()
        _roster.value = listOf(dmParticipant)
    }

    private suspend fun acceptLoop(socket: ServerSocket) {
        while (!socket.isClosed) {
            val client = try {
                socket.accept()
            } catch (e: IOException) {
                break
            }
            scope.launch { handleClient(client) }
        }
    }

    private fun handleClient(socket: Socket) {
        val input = DataInputStream(socket.getInputStream())
        val output = DataOutputStream(socket.getOutputStream())
        var participantId: String? = null
        try {
            val first = SessionWire.read(input) ?: return
            val hello = first as? SessionMessage.Hello ?: return

            val id = UUID.randomUUID().toString()
            participantId = id
            clients[id] = ClientHandle(id, socket, output)

            SessionWire.write(output, SessionMessage.Welcome(id, sessionId))
            addParticipant(Participant(id, hello.displayName, ParticipantRole.PLAYER))
            broadcastRoster()

            while (true) {
                val msg = SessionWire.read(input) ?: break
                if (msg is SessionMessage.Bye) break
                // Fases futuras (PV/iniciativa/pedidos) vão tratar outros tipos aqui.
            }
        } catch (e: IOException) {
            // Conexão caiu de forma inesperada — removida abaixo como qualquer outra saída.
        } finally {
            participantId?.let { removeParticipant(it) }
            runCatching { socket.close() }
        }
    }

    private fun addParticipant(participant: Participant) {
        _roster.value = _roster.value.filterNot { it.id == participant.id } + participant
    }

    private fun removeParticipant(id: String) {
        clients.remove(id)
        _roster.value = _roster.value.filterNot { it.id == id }
        broadcastRoster()
    }

    private fun broadcastRoster() {
        val message = SessionMessage.Roster(_roster.value)
        clients.values.forEach { handle ->
            try {
                SessionWire.write(handle.out, message)
            } catch (e: IOException) {
                // O próprio loop de leitura desse cliente vai detectar a queda e removê-lo.
            }
        }
    }

    companion object {
        const val DM_PARTICIPANT_ID = "dm"
    }
}
