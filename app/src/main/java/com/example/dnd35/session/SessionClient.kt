package com.example.dnd35.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

/** Estado da conexão do lado do Jogador. */
sealed class SessionClientState {
    data object Connecting : SessionClientState()
    data class Connected(
        val selfId: String,
        val sessionId: String,
        val participants: List<Participant> = emptyList()
    ) : SessionClientState()
    data class Disconnected(val reason: String) : SessionClientState()
}

/**
 * Lado do Jogador: conecta-se por TCP ao servidor do Mestre, envia um HELLO e passa a acompanhar
 * a lista de participantes ([state]) conforme o Mestre a atualiza. Nenhuma informação sai da rede
 * local: apenas o endereço IP/porta do Mestre, obtidos por QR Code ou digitados manualmente.
 */
class SessionClient {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var socket: Socket? = null
    private var output: DataOutputStream? = null

    private val _state = MutableStateFlow<SessionClientState>(SessionClientState.Connecting)
    val state: StateFlow<SessionClientState> = _state

    /**
     * @param characterJson ficha do jogador já serializada (mesmo formato do backup de fichas),
     * mandada ao Mestre logo após a entrada ser confirmada. `null` entra na sessão sem vincular
     * nenhuma ficha (por exemplo, um observador).
     */
    fun connect(host: String, port: Int, displayName: String, characterJson: String? = null) {
        scope.launch {
            try {
                val sock = Socket()
                sock.connect(InetSocketAddress(host, port), CONNECT_TIMEOUT_MS)
                socket = sock

                val input = DataInputStream(sock.getInputStream())
                val out = DataOutputStream(sock.getOutputStream())
                output = out

                SessionWire.write(out, SessionMessage.Hello(displayName))

                val welcome = SessionWire.read(input) as? SessionMessage.Welcome
                if (welcome == null) {
                    _state.value = SessionClientState.Disconnected("Resposta inesperada do Mestre.")
                    return@launch
                }
                if (characterJson != null) {
                    SessionWire.write(out, SessionMessage.CharacterSheet(characterJson))
                }
                _state.value = SessionClientState.Connected(welcome.participantId, welcome.sessionId)

                while (true) {
                    val msg = SessionWire.read(input) ?: break
                    when (msg) {
                        is SessionMessage.Roster -> {
                            val current = _state.value
                            if (current is SessionClientState.Connected) {
                                _state.value = current.copy(participants = msg.participants)
                            }
                        }
                        is SessionMessage.Bye -> break
                        else -> Unit
                    }
                }
                _state.value = SessionClientState.Disconnected("Conexão encerrada pelo Mestre.")
            } catch (e: IOException) {
                _state.value = SessionClientState.Disconnected(e.message ?: "Não foi possível conectar.")
            }
        }
    }

    /** Sai da sessão de forma educada (avisa o Mestre) e libera os recursos do cliente. */
    fun disconnect() {
        runCatching { output?.let { SessionWire.write(it, SessionMessage.Bye()) } }
        runCatching { socket?.close() }
        scope.cancel()
    }

    companion object {
        private const val CONNECT_TIMEOUT_MS = 5000
    }
}
