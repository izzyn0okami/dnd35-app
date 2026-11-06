package com.example.dnd35.session

import com.example.dnd35.model.CharacterInventoryItem
import com.example.dnd35.model.CharacterJsonAdapters
import com.example.dnd35.diagnostics.CrashLogStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
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
        val participants: List<Participant> = emptyList(),
        val combat: CombatState? = null,
        /** Conteúdo atual do baú de loot compartilhado da sessão. */
        val lootChest: List<CharacterInventoryItem> = emptyList(),
        /** `null` quando não há loja de NPC aberta no momento. */
        val shop: ShopSnapshot? = null
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
    @Volatile private var socket: Socket? = null
    @Volatile private var output: DataOutputStream? = null

    // Parâmetros da última chamada a connect(), guardados só para reconnect() poder repetir a
    // mesma conexão sem o chamador precisar lembrar host/porta/nome de novo.
    private var lastHost: String? = null
    private var lastPort: Int = 0
    private var lastDisplayName: String = ""
    private var lastCharacterJson: String? = null
    private var lastCharacterId: Long? = null

    // Token recebido no último WELCOME (ver SessionMessage.Hello/Welcome) — mandado de volta ao
    // reconectar para o Mestre reconhecer que é o mesmo participante, não um novo.
    private var rejoinToken: String? = null

    private val _state = MutableStateFlow<SessionClientState>(SessionClientState.Connecting)
    val state: StateFlow<SessionClientState> = _state

    // Resultado de pedidos feitos por este Jogador (aprovado/negado pelo Mestre). É um evento,
    // não um estado contínuo — por isso SharedFlow em vez de StateFlow.
    private val _requestResults = MutableSharedFlow<SessionMessage.RequestResolved>(extraBufferCapacity = 4)
    val requestResults: SharedFlow<SessionMessage.RequestResolved> = _requestResults

    // Item concedido ao pegar algo do baú de loot (pedido aprovado) — evento, não estado
    // contínuo, já que vira um item novo no inventário local assim que é recebido.
    private val _lootGranted = MutableSharedFlow<SessionItemGrant>(extraBufferCapacity = 4)
    val lootGranted: SharedFlow<SessionItemGrant> = _lootGranted

    // Item concedido ao comprar algo da loja de NPC (pedido aprovado) — mesmo motivo do loot:
    // evento, não estado contínuo.
    private val _shopPurchases = MutableSharedFlow<SessionItemGrant>(extraBufferCapacity = 4)
    val shopPurchases: SharedFlow<SessionItemGrant> = _shopPurchases

    // Mensagens privadas recebidas do Mestre — evento, não estado contínuo.
    private val _privateMessages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val privateMessages: SharedFlow<String> = _privateMessages

    /**
     * @param characterJson ficha do jogador já serializada (mesmo formato do backup de fichas),
     * mandada ao Mestre logo após a entrada ser confirmada. `null` entra na sessão sem vincular
     * nenhuma ficha (por exemplo, um observador).
     */
    fun connect(host: String, port: Int, displayName: String, characterJson: String? = null, characterId: Long? = null) {
        lastHost = host
        lastPort = port
        lastDisplayName = displayName
        lastCharacterJson = characterJson
        lastCharacterId = characterId?.takeIf { it > 0L }
        rejoinToken = null // entrada nova: nunca reconecta como um participante de uma sessão anterior.
        connectInternal(host, port, displayName, characterJson, lastCharacterId, rejoinToken = null)
    }

    /**
     * Tenta reconectar com os mesmos parâmetros da última chamada a [connect] (mesmo host/porta/
     * nome/ficha), mandando o token recebido no WELCOME anterior para o Mestre devolver o mesmo
     * id de participante. Não faz nada e retorna `false` se [connect] nunca foi chamado antes.
     */
    fun reconnect(): Boolean {
        val host = lastHost ?: return false
        connectInternal(host, lastPort, lastDisplayName, lastCharacterJson, lastCharacterId, rejoinToken)
        return true
    }

    private fun connectInternal(host: String, port: Int, displayName: String, characterJson: String?, characterId: Long?, rejoinToken: String?) {
        _state.value = SessionClientState.Connecting
        // Fecha uma conexão anterior ainda viva, se houver — evita duas leituras concorrentes
        // (o laço antigo e o novo) brigando pelas mesmas variáveis de instância caso reconnect()
        // seja chamado antes do laço de leitura anterior perceber a própria queda sozinho.
        runCatching { socket?.close() }
        scope.launch {
            try {
                val sock = Socket()
                sock.connect(InetSocketAddress(host, port), CONNECT_TIMEOUT_MS)
                socket = sock

                val input = DataInputStream(sock.getInputStream())
                val out = DataOutputStream(sock.getOutputStream())
                output = out

                SessionWire.write(out, SessionMessage.Hello(displayName, rejoinToken, characterId))

                val welcome = SessionWire.read(input) as? SessionMessage.Welcome
                if (welcome == null) {
                    _state.value = SessionClientState.Disconnected("Resposta inesperada do Mestre.")
                    return@launch
                }
                this@SessionClient.rejoinToken = welcome.rejoinToken
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
                        is SessionMessage.CombatUpdate -> {
                            val current = _state.value
                            if (current is SessionClientState.Connected) {
                                _state.value = current.copy(combat = msg.state)
                            }
                        }
                        is SessionMessage.EndCombat -> {
                            val current = _state.value
                            if (current is SessionClientState.Connected) {
                                _state.value = current.copy(combat = null)
                            }
                        }
                        is SessionMessage.RequestResolved -> _requestResults.tryEmit(msg)
                        is SessionMessage.LootChestUpdate -> {
                            val current = _state.value
                            if (current is SessionClientState.Connected) {
                                _state.value = current.copy(lootChest = CharacterJsonAdapters.deserializeInventory(msg.itemsJson))
                            }
                        }
                        is SessionMessage.LootGranted -> {
                            CharacterJsonAdapters.deserializeInventory(msg.itemJson).firstOrNull()?.let { item ->
                                _lootGranted.tryEmit(SessionItemGrant(msg.grantId, msg.characterId, item))
                            }
                        }
                        is SessionMessage.ShopUpdate -> {
                            val current = _state.value
                            if (current is SessionClientState.Connected) {
                                _state.value = current.copy(
                                    shop = ShopSnapshot(msg.npcName, CharacterJsonAdapters.deserializeInventory(msg.itemsJson))
                                )
                            }
                        }
                        is SessionMessage.ShopClosed -> {
                            val current = _state.value
                            if (current is SessionClientState.Connected) {
                                _state.value = current.copy(shop = null)
                            }
                        }
                        is SessionMessage.ShopPurchaseGranted -> {
                            CharacterJsonAdapters.deserializeInventory(msg.itemJson).firstOrNull()?.let { item ->
                                val purse = SessionCoinPurse(msg.goldGp, msg.silverSp, msg.copperCp, msg.platinumPp)
                                _shopPurchases.tryEmit(SessionItemGrant(msg.grantId, msg.characterId, item, purse))
                            }
                        }
                        is SessionMessage.PrivateMessage -> _privateMessages.tryEmit(msg.text)
                        is SessionMessage.Bye -> break
                        else -> Unit
                    }
                }
                _state.value = SessionClientState.Disconnected("Conexão encerrada pelo Mestre.")
            } catch (e: IOException) {
                CrashLogStore.record("MULTIPLAYER_CLIENT", "Conexão interrompida ou protocolo inválido", e)
                _state.value = SessionClientState.Disconnected(
                    e.message?.takeIf { it.isNotBlank() } ?: "Falha de comunicação com o Mestre."
                )
            } catch (e: Exception) {
                CrashLogStore.record("MULTIPLAYER_CLIENT", "Erro inesperado no cliente multiplayer", e)
                _state.value = SessionClientState.Disconnected(
                    "Erro inesperado na comunicação: ${e.message ?: e.javaClass.simpleName}"
                )
            }
        }
    }

    /**
     * Manda ao Mestre um pedido de ação que depende de aprovação (ver [RequestKind]) — o
     * resultado chega depois em [requestResults]. Não faz nada se a conexão já caiu.
     */
    fun requestAction(
        kind: RequestKind,
        summary: String,
        characterJson: String? = null,
        combatantId: String? = null,
        hpDelta: Int? = null,
        lootItemId: String? = null,
        shopItemId: String? = null
    ) {
        val out = output ?: return
        scope.launch {
            runCatching {
                SessionWire.write(out, SessionMessage.ActionRequested(kind, summary, characterJson, combatantId, hpDelta, lootItemId, shopItemId))
            }.onFailure { CrashLogStore.record("MULTIPLAYER_CLIENT", "Falha ao enviar pedido de ação", it) }
        }
    }

    /** Confirma ao Mestre que o grant foi persistido na ficha local. */
    fun acknowledgeGrant(grantId: String) {
        val out = output ?: return
        scope.launch {
            runCatching { SessionWire.write(out, SessionMessage.GrantAcknowledged(grantId)) }
                .onFailure { CrashLogStore.record("MULTIPLAYER_CLIENT", "Falha ao confirmar entrega de recurso", it) }
        }
    }

    /** Manda uma mensagem privada ao Mestre (não passa pela fila de pedidos/aprovações). */
    fun sendPrivateMessage(text: String) {
        val out = output ?: return
        scope.launch {
            runCatching { SessionWire.write(out, SessionMessage.PrivateMessage(text)) }
                .onFailure { CrashLogStore.record("MULTIPLAYER_CLIENT", "Falha ao enviar mensagem privada", it) }
        }
    }

    /** Sai da sessão de forma educada (avisa o Mestre) e libera os recursos do cliente. */
    fun disconnect() {
        // Pode ser chamado pela interface: o BYE é enviado em I/O e o socket fecha em seguida.
        val currentOutput = output
        val currentSocket = socket
        output = null
        socket = null

        if (currentSocket == null) {
            scope.cancel()
            return
        }

        scope.launch {
            try {
                currentOutput?.let { SessionWire.write(it, SessionMessage.Bye()) }
            } catch (e: Exception) {
                CrashLogStore.record("MULTIPLAYER_CLIENT", "Falha ao enviar encerramento da sessão", e)
            } finally {
                runCatching { currentSocket.close() }
                scope.cancel()
            }
        }
    }

    companion object {
        private const val CONNECT_TIMEOUT_MS = 5000
    }
}
