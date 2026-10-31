package com.example.dnd35.session

import com.example.dnd35.backup.CharacterBackup
import com.example.dnd35.model.CharacterEntity
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

    // Fichas que cada jogador mandou ao entrar (chave = id do participante). Só existe aqui, do
    // lado do Mestre — não é repassada a outros jogadores pelo ROSTER.
    private val _receivedCharacters = MutableStateFlow<Map<String, CharacterEntity>>(emptyMap())
    val receivedCharacters: StateFlow<Map<String, CharacterEntity>> = _receivedCharacters

    // Combate é estado global da sessão: PV/iniciativa/condições sincronizados com todos os
    // aparelhos. `null` = nenhum combate ativo no momento.
    private val _combatState = MutableStateFlow<CombatState?>(null)
    val combatState: StateFlow<CombatState?> = _combatState

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
        _receivedCharacters.value = emptyMap()
        _combatState.value = null
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
            // Quem entra no meio de um combate já começa vendo o estado atual.
            _combatState.value?.let { SessionWire.write(output, SessionMessage.CombatUpdate(it)) }

            while (true) {
                val msg = SessionWire.read(input) ?: break
                when (msg) {
                    is SessionMessage.Bye -> break
                    is SessionMessage.CharacterSheet -> receiveCharacterSheet(id, msg.characterJson)
                    // Fases futuras (PV/iniciativa/pedidos) vão tratar outros tipos aqui.
                    else -> Unit
                }
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
        _receivedCharacters.value = _receivedCharacters.value - id
        broadcastRoster()
    }

    /** Guarda a ficha mandada por um jogador; entradas inválidas são ignoradas silenciosamente. */
    private fun receiveCharacterSheet(participantId: String, characterJson: String) {
        val parsed = CharacterBackup.parse(characterJson) as? CharacterBackup.ParseResult.Success ?: return
        val entity = parsed.characters.firstOrNull()?.entity ?: return
        _receivedCharacters.value = _receivedCharacters.value + (participantId to entity)
    }

    private fun broadcastRoster() {
        broadcast(SessionMessage.Roster(_roster.value))
    }

    private fun broadcast(message: SessionMessage) {
        clients.values.forEach { handle ->
            try {
                SessionWire.write(handle.out, message)
            } catch (e: IOException) {
                // O próprio loop de leitura desse cliente vai detectar a queda e removê-lo.
            }
        }
    }

    // --- Combate (estado global, autoridade do Mestre) ---

    /** Inicia um combate novo com os combatentes informados (a ordem de iniciativa é calculada aqui). */
    fun startCombat(combatants: List<Combatant>) {
        val state = CombatState.start(combatants)
        _combatState.value = state
        broadcast(SessionMessage.CombatUpdate(state))
    }

    /** Aplica dano (positivo) ou cura (negativo) a um combatente e sincroniza com todos. */
    fun applyDamage(combatantId: String, amount: Int) {
        updateCombat { state ->
            state.copy(combatants = state.combatants.map { if (it.id == combatantId) it.withDamage(amount) else it })
        }
    }

    /** Avança para o próximo combatente vivo na ordem de iniciativa; passa de rodada ao dar a volta. */
    fun nextTurn() {
        updateCombat { state ->
            if (state.combatants.isEmpty()) return@updateCombat state
            val nextIndex = (state.currentTurnIndex + 1) % state.combatants.size
            val nextRound = if (nextIndex <= state.currentTurnIndex) state.round + 1 else state.round
            state.copy(currentTurnIndex = nextIndex, round = nextRound)
        }
    }

    /** Encerra o combate ativo (se houver) e avisa a todos. */
    fun endCombat() {
        if (_combatState.value == null) return
        _combatState.value = null
        broadcast(SessionMessage.EndCombat)
    }

    private inline fun updateCombat(transform: (CombatState) -> CombatState) {
        val current = _combatState.value ?: return
        val updated = transform(current)
        _combatState.value = updated
        broadcast(SessionMessage.CombatUpdate(updated))
    }

    companion object {
        const val DM_PARTICIPANT_ID = "dm"
    }
}
