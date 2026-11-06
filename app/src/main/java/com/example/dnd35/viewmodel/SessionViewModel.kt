package com.example.dnd35.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnd35.backup.CharacterBackup
import com.example.dnd35.data.AppDatabase
import com.example.dnd35.data.HostedSessionEntity
import com.example.dnd35.diagnostics.CrashLogStore
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.CharacterInventoryItem
import com.example.dnd35.session.ActionRequest
import com.example.dnd35.session.Combatant
import com.example.dnd35.session.CombatState
import com.example.dnd35.session.JoinPayload
import com.example.dnd35.session.LocalNetworkUtil
import com.example.dnd35.session.Participant
import com.example.dnd35.session.PrivateChatEntry
import com.example.dnd35.session.QrCodeUtil
import com.example.dnd35.session.RequestKind
import com.example.dnd35.session.HostedSessionSnapshot
import com.example.dnd35.session.HostedSessionSnapshotCodec
import com.example.dnd35.session.SessionClient
import com.example.dnd35.session.SessionClientState
import com.example.dnd35.session.SessionServer
import com.example.dnd35.session.ShopSnapshot
import com.example.dnd35.session.ShopState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** Estado de tela do modo Mestre (hospedando uma sessão). */
sealed class DmSessionUiState {
    data object Idle : DmSessionUiState()
    data class Hosting(
        val sessionId: String,
        val qrBitmap: Bitmap,
        val qrPayload: String,
        val participants: List<Participant>,
        /** Fichas recebidas dos jogadores conectados, indexadas pelo id do participante. */
        val characters: Map<String, CharacterEntity> = emptyMap(),
        /** `null` quando não há combate ativo no momento. */
        val combat: CombatState? = null,
        /** Pedidos de jogadores aguardando aprovação do Mestre. */
        val pendingRequests: List<ActionRequest> = emptyList(),
        /** Conteúdo atual do baú de loot compartilhado da sessão. */
        val lootChest: List<CharacterInventoryItem> = emptyList(),
        /** `null` quando não há loja de NPC aberta no momento. */
        val shop: ShopState? = null,
        /** Log de mensagens privadas trocadas com os jogadores. */
        val privateChatLog: List<PrivateChatEntry> = emptyList()
    ) : DmSessionUiState()
    data class Error(val message: String) : DmSessionUiState()
}

/** Estado de tela do modo Jogador (entrando em uma sessão de outro aparelho). */
sealed class PlayerSessionUiState {
    data object Idle : PlayerSessionUiState()
    data object Connecting : PlayerSessionUiState()
    data class Connected(
        val selfId: String,
        val participants: List<Participant>,
        val combat: CombatState? = null,
        val lootChest: List<CharacterInventoryItem> = emptyList(),
        val shop: ShopSnapshot? = null,
        /** ID da ficha local vinculada no momento da entrada; não depende da ficha atualmente selecionada. */
        val characterId: Long? = null
    ) : PlayerSessionUiState()
    data class Disconnected(val reason: String) : PlayerSessionUiState()
}

/**
 * Agrupa os flows do servidor que não cabem no limite de 5 argumentos do `combine` padrão, para
 * poder combiná-los em duas etapas (ver [SessionViewModel.launchCollectDmState]).
 */
private data class DmExtraState(
    val combat: CombatState?,
    val pendingRequests: List<ActionRequest>,
    val lootChest: List<CharacterInventoryItem>,
    val shop: ShopState?,
    val privateChatLog: List<PrivateChatEntry>
)

private sealed interface HostedSessionPersistenceCommand {
    data class Save(val snapshot: HostedSessionSnapshot) : HostedSessionPersistenceCommand
    data class SaveAndWait(val snapshot: HostedSessionSnapshot, val completion: CompletableDeferred<Unit>) : HostedSessionPersistenceCommand
    data class EndAndWait(val sessionId: String, val completion: CompletableDeferred<Unit>) : HostedSessionPersistenceCommand
    data class Flush(val completion: CompletableDeferred<Unit>) : HostedSessionPersistenceCommand
}

/**
 * ViewModel da sessão de mesa offline: cria (Mestre) ou entra (Jogador) em uma sessão local,
 * sem depender de internet — apenas da rede já compartilhada pelos aparelhos (Wi-Fi local,
 * hotspot do próprio Mestre, etc.).
 */
class SessionViewModel(application: Application) : AndroidViewModel(application) {

    private val hostedSessionDao = AppDatabase.getDatabase(application).hostedSessionDao()
    private val persistenceCommands = Channel<HostedSessionPersistenceCommand>(Channel.UNLIMITED)
    private var server: SessionServer? = null
    private var manualHostRequested = false

    init {
        viewModelScope.launch(Dispatchers.IO) {
            for (command in persistenceCommands) {
                try {
                    when (command) {
                        is HostedSessionPersistenceCommand.Save -> {
                            saveHostedSnapshot(command.snapshot)
                        }
                        is HostedSessionPersistenceCommand.SaveAndWait -> {
                            try {
                                saveHostedSnapshot(command.snapshot)
                                command.completion.complete(Unit)
                            } catch (error: Exception) {
                                command.completion.completeExceptionally(error)
                                throw error
                            }
                        }
                        is HostedSessionPersistenceCommand.EndAndWait -> {
                            try {
                                hostedSessionDao.markManuallyEnded(command.sessionId)
                                command.completion.complete(Unit)
                            } catch (error: Exception) {
                                command.completion.completeExceptionally(error)
                                throw error
                            }
                        }
                        is HostedSessionPersistenceCommand.Flush -> {
                            command.completion.complete(Unit)
                        }
                    }
                } catch (error: Exception) {
                    CrashLogStore.record("MULTIPLAYER_PERSISTENCE", "Falha ao persistir estado da sessão", error)
                }
            }
        }
        recoverHostedSession()
    }
    private var client: SessionClient? = null
    // Mantém o vínculo exato da ficha usada para entrar, mesmo se o jogador trocar de ficha ativa.
    private var linkedPlayerCharacterId: Long? = null

    private val _dmState = MutableStateFlow<DmSessionUiState>(DmSessionUiState.Idle)
    val dmState: StateFlow<DmSessionUiState> = _dmState

    private val _playerState = MutableStateFlow<PlayerSessionUiState>(PlayerSessionUiState.Idle)
    val playerState: StateFlow<PlayerSessionUiState> = _playerState

    // Mensagem de retorno do último pedido feito pelo Jogador (aprovado/negado pelo Mestre).
    // É um aviso pontual, por isso fica separado do estado contínuo de conexão.
    private val _playerRequestFeedback = MutableStateFlow<String?>(null)
    val playerRequestFeedback: StateFlow<String?> = _playerRequestFeedback

    /** Jogador: descarta o aviso de resultado do último pedido (depois que o jogador já leu). */
    fun clearRequestFeedback() {
        _playerRequestFeedback.value = null
    }

    // Concessão de loot aprovada — a UI persiste na ficha local vinculada ao participante.
    private val _lootGranted = MutableSharedFlow<com.example.dnd35.session.SessionItemGrant>(replay = 8, extraBufferCapacity = 4)
    val lootGranted: SharedFlow<com.example.dnd35.session.SessionItemGrant> = _lootGranted

    // Compra aprovada com saldo atualizado; a UI persiste item e moedas na ficha vinculada.
    private val _shopPurchaseGranted = MutableSharedFlow<com.example.dnd35.session.SessionItemGrant>(replay = 8, extraBufferCapacity = 4)
    val shopPurchaseGranted: SharedFlow<com.example.dnd35.session.SessionItemGrant> = _shopPurchaseGranted

    // Mensagens privadas recebidas do Mestre — evento, não estado contínuo.
    private val _playerPrivateMessages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val playerPrivateMessages: SharedFlow<String> = _playerPrivateMessages

    private suspend fun persistManualEndAndWait(sessionId: String) {
        val completion = CompletableDeferred<Unit>()
        persistenceCommands.send(HostedSessionPersistenceCommand.EndAndWait(sessionId, completion))
        completion.await()
    }

    private suspend fun flushPersistenceQueue() {
        val completion = CompletableDeferred<Unit>()
        persistenceCommands.send(HostedSessionPersistenceCommand.Flush(completion))
        completion.await()
    }

    private suspend fun persistSnapshotAndWait(snapshot: HostedSessionSnapshot) {
        val completion = CompletableDeferred<Unit>()
        persistenceCommands.send(HostedSessionPersistenceCommand.SaveAndWait(snapshot, completion))
        completion.await()
    }

    private suspend fun saveHostedSnapshot(snapshot: HostedSessionSnapshot) {
        hostedSessionDao.saveCheckpoint(
            HostedSessionEntity(
                sessionId = snapshot.sessionId,
                hostDisplayName = snapshot.hostDisplayName,
                status = HostedSessionEntity.STATUS_ACTIVE,
                snapshotVersion = HostedSessionSnapshotCodec.VERSION,
                snapshotJson = HostedSessionSnapshotCodec.encode(snapshot)
            )
        )
    }

    /** Recupera uma sessão cujo processo terminou sem encerramento manual. */
    private fun recoverHostedSession() {
        viewModelScope.launch {
            if (manualHostRequested || server != null) return@launch
            val stored = withContext(Dispatchers.IO) { hostedSessionDao.getRecoverableSession() } ?: return@launch
            if (manualHostRequested || server != null) return@launch
            val snapshot = try {
                require(stored.snapshotVersion == HostedSessionSnapshotCodec.VERSION) {
                    "A versão do snapshot salvo não é suportada."
                }
                HostedSessionSnapshotCodec.decode(stored.snapshotJson)
            } catch (error: Exception) {
                _dmState.value = DmSessionUiState.Error(
                    "A sessão salva foi encontrada, mas não pôde ser restaurada: " +
                        (error.message?.takeIf { it.isNotBlank() } ?: error.javaClass.simpleName)
                )
                return@launch
            }
            if (snapshot.sessionId != stored.sessionId || snapshot.hostDisplayName != stored.hostDisplayName) {
                _dmState.value = DmSessionUiState.Error("Os dados de identificação da sessão salva não correspondem ao snapshot.")
                return@launch
            }

            val restoredServer = SessionServer(snapshot.sessionId, snapshot.hostDisplayName, persistCriticalSnapshot = { critical ->
                persistSnapshotAndWait(critical)
            }) { updated ->
                persistenceCommands.trySend(HostedSessionPersistenceCommand.Save(updated))
            }
            try {
                restoredServer.restoreSnapshot(snapshot)
                val (port, ip) = withContext(Dispatchers.IO) {
                    restoredServer.start() to LocalNetworkUtil.findLocalIpAddress()
                }
                if (manualHostRequested || server != null) {
                    restoredServer.stop()
                    return@launch
                }
                if (ip == null) {
                    restoredServer.stop()
                    _dmState.value = DmSessionUiState.Error(
                        "A sessão foi recuperada, mas não foi possível encontrar o endereço IPv4 deste aparelho. " +
                            "Ative o Wi-Fi para gerar um novo QR Code."
                    )
                    return@launch
                }

                server = restoredServer
                val payload = JoinPayload(snapshot.sessionId, snapshot.hostDisplayName, ip, port)
                val qrContent = payload.toJson()
                val bitmap = withContext(Dispatchers.Default) { QrCodeUtil.generate(qrContent) }
                viewModelScope.launchCollectDmState(restoredServer, snapshot.sessionId, bitmap, qrContent)
                persistenceCommands.trySend(HostedSessionPersistenceCommand.Save(restoredServer.createSnapshot()))
            } catch (error: Exception) {
                restoredServer.stop()
                if (server === restoredServer) server = null
                _dmState.value = DmSessionUiState.Error(
                    "Não foi possível reabrir a sessão salva: " +
                        (error.message?.takeIf { it.isNotBlank() } ?: error.javaClass.simpleName)
                )
            }
        }
    }

    /** Mestre: cria e hospeda uma nova sessão, exibindo o QR Code para os jogadores escanearem. */
    fun hostSession(hostDisplayName: String) {
        manualHostRequested = true
        stopHosting(manual = true)
        val sessionId = JoinPayload.newSessionId()
        val name = hostDisplayName.ifBlank { "Mestre" }
        val newServer = SessionServer(sessionId, name, persistCriticalSnapshot = { critical ->
            persistSnapshotAndWait(critical)
        }) { snapshot ->
            persistenceCommands.trySend(HostedSessionPersistenceCommand.Save(snapshot))
        }
        server = newServer

        // Abrir o ServerSocket e consultar a interface de rede são operações de I/O:
        // executá-las fora da thread principal evita outro NetworkOnMainThreadException.
        viewModelScope.launch {
            try {
                val (port, ip) = withContext(Dispatchers.IO) {
                    newServer.start() to LocalNetworkUtil.findLocalIpAddress()
                }
                if (server !== newServer) {
                    newServer.stop()
                    return@launch
                }

                if (ip == null) {
                    newServer.stop()
                    server = null
                    _dmState.value = DmSessionUiState.Error(
                        "Não foi possível encontrar o endereço deste aparelho na rede local. " +
                            "Confirme se o Wi-Fi está ligado (não precisa ter internet, só precisa " +
                            "estar na mesma rede dos jogadores)."
                    )
                    return@launch
                }

                val payload = JoinPayload(sessionId, name, ip, port)
                val qrContent = payload.toJson()
                val bitmap = QrCodeUtil.generate(qrContent)
                viewModelScope.launchCollectDmState(newServer, sessionId, bitmap, qrContent)
                persistenceCommands.trySend(HostedSessionPersistenceCommand.Save(newServer.createSnapshot()))
            } catch (e: Exception) {
                newServer.stop()
                if (server === newServer) {
                    server = null
                    _dmState.value = DmSessionUiState.Error(
                        "Não foi possível iniciar a sessão local: " +
                            (e.message?.takeIf { it.isNotBlank() } ?: e.javaClass.simpleName)
                    )
                }
            }
        }
    }

    private fun CoroutineScope.launchCollectDmState(
        activeServer: SessionServer,
        sessionId: String,
        bitmap: Bitmap,
        qrContent: String
    ) {
        launch {
            val extra = combine(
                activeServer.combatState,
                activeServer.pendingRequests,
                activeServer.lootChest,
                activeServer.shop,
                activeServer.privateChatLog
            ) { combat, requests, lootChest, shop, chatLog ->
                DmExtraState(combat, requests, lootChest, shop, chatLog)
            }
            combine(activeServer.roster, activeServer.receivedCharacters, extra) { participants, characters, ex ->
                DmSessionUiState.Hosting(
                    sessionId, bitmap, qrContent, participants, characters,
                    ex.combat, ex.pendingRequests, ex.lootChest, ex.shop, ex.privateChatLog
                )
            }.collect { newState ->
                if (server === activeServer) {
                    _dmState.value = newState
                }
            }
        }
    }

    /** Mestre: abre (ou atualiza) a loja com os itens informados, tirados de um NPC seu. */
    fun openShop(npcCharacterId: Long, npcName: String, items: List<CharacterInventoryItem>) {
        server?.openShop(npcCharacterId, npcName, items)
    }

    /** Mestre: fecha a loja de NPC aberta no momento. */
    fun closeShop() {
        server?.closeShop()
    }

    /** Mestre: manda uma mensagem privada para um Jogador específico (só ele recebe). */
    fun sendPrivateMessageToPlayer(participantId: String, text: String) {
        server?.sendPrivateMessage(participantId, text)
    }

    /** Mestre: aprova um pedido pendente de jogador, aplicando a mudança correspondente. */
    fun approveRequest(requestId: String) {
        val activeServer = server ?: return
        viewModelScope.launch(Dispatchers.IO) { activeServer.approveRequest(requestId) }
    }

    /** Mestre: nega um pedido pendente de jogador (nada é aplicado). */
    fun denyRequest(requestId: String) {
        server?.denyRequest(requestId)
    }

    /** Mestre: adiciona um item ao baú de loot compartilhado. */
    fun addLootItem(item: CharacterInventoryItem) {
        server?.addLootItem(item)
    }

    /** Mestre: remove um item do baú de loot diretamente (sem precisar de pedido de jogador). */
    fun removeLootItem(itemId: String) {
        server?.removeLootItem(itemId)
    }

    /** Mestre: inicia um combate novo com os combatentes informados. */
    fun startCombat(combatants: List<Combatant>) {
        server?.startCombat(combatants)
    }

    /** Mestre: aplica dano (positivo) ou cura (negativo) direto num combatente. */
    fun applyCombatDamage(combatantId: String, amount: Int) {
        server?.applyDamage(combatantId, amount)
    }

    /** Mestre: avança para o próximo combatente na ordem de iniciativa. */
    fun nextCombatTurn() {
        server?.nextTurn()
    }

    /** Mestre: encerra o combate ativo. */
    fun endCombat() {
        server?.endCombat()
    }

    /** Mestre: encerra a sessão ativa, desconectando todos os jogadores. */
    fun stopHosting(manual: Boolean = true) {
        val activeServer = server
        val activeSessionId = activeServer?.createSnapshot()?.sessionId
        activeServer?.stop()
        server = null
        var endFailed = false
        if (manual && activeSessionId != null) {
            runCatching {
                kotlinx.coroutines.runBlocking(Dispatchers.IO) {
                    persistManualEndAndWait(activeSessionId)
                }
            }.onFailure { error ->
                endFailed = true
                CrashLogStore.record("MULTIPLAYER_PERSISTENCE", "Falha ao marcar encerramento manual da sessão", error)
            }
        }
        _dmState.value = if (endFailed) {
            DmSessionUiState.Error("Não foi possível confirmar o encerramento da sessão no armazenamento local.")
        } else {
            DmSessionUiState.Idle
        }
    }

    /** Jogador: entra em uma sessão a partir de um QR Code já decodificado. */
    fun joinSession(payload: JoinPayload, displayName: String, character: CharacterEntity?) {
        if (!payload.isValid()) {
            _playerState.value = PlayerSessionUiState.Disconnected(
                "O QR Code contém dados inválidos. Solicite ao Mestre um novo código."
            )
            return
        }
        val existingClient = client
        if (existingClient?.reconnectTo(payload.hostAddress, payload.port, payload.sessionId) == true) {
            _playerState.value = PlayerSessionUiState.Connecting
            return
        }
        joinSession(payload.hostAddress, payload.port, displayName, character)
    }

    /** Jogador: entra em uma sessão informando manualmente o IP e a porta do Mestre. */
    fun joinSession(hostAddress: String, port: Int, displayName: String, character: CharacterEntity?) {
        if (!JoinPayload.isValidHostAddress(hostAddress) || port !in 1..65535) {
            _playerState.value = PlayerSessionUiState.Disconnected(
                "Endereço IPv4 ou porta inválidos. Confira os dados informados pelo Mestre."
            )
            return
        }
        leaveSession()
        linkedPlayerCharacterId = character?.id?.takeIf { it > 0L }
        _playerRequestFeedback.value = null
        val name = displayName.ifBlank { "Jogador" }
        // Sem foto: o retrato não é necessário para o Mestre acompanhar a ficha durante a sessão,
        // e evita inflar a mensagem enviada pela rede local.
        val characterJson = character?.let { CharacterBackup.toJson(listOf(it)) { null } }
        _playerState.value = PlayerSessionUiState.Connecting
        val newClient = SessionClient()
        client = newClient
        viewModelScope.launchCollectClientState(newClient)
        newClient.connect(hostAddress, port, name, characterJson, character?.id)
    }

    private fun CoroutineScope.launchCollectClientState(activeClient: SessionClient) {
        launch {
            activeClient.state.collect { state ->
                if (client === activeClient) {
                    _playerState.value = when (state) {
                        is SessionClientState.Connecting -> PlayerSessionUiState.Connecting
                        is SessionClientState.Connected -> PlayerSessionUiState.Connected(state.selfId, state.participants, state.combat, state.lootChest, state.shop, linkedPlayerCharacterId)
                        is SessionClientState.Disconnected -> PlayerSessionUiState.Disconnected(state.reason)
                    }
                }
            }
        }
        launch {
            activeClient.requestResults.collect { resolved ->
                if (client === activeClient) {
                    _playerRequestFeedback.value = if (resolved.approved) {
                        "Pedido aprovado pelo Mestre."
                    } else {
                        "Pedido negado pelo Mestre." + if (resolved.reason.isNotBlank()) " (${resolved.reason})" else ""
                    }
                }
            }
        }
        launch {
            activeClient.lootGranted.collect { grant ->
                if (client === activeClient) {
                    _lootGranted.tryEmit(grant)
                }
            }
        }
        launch {
            activeClient.shopPurchases.collect { grant ->
                if (client === activeClient) {
                    _shopPurchaseGranted.tryEmit(grant)
                }
            }
        }
        launch {
            activeClient.privateMessages.collect { text ->
                if (client === activeClient) {
                    _playerPrivateMessages.tryEmit(text)
                }
            }
        }
    }

    /** Jogador: pede ao Mestre uma mudança de PV num combate ativo (ex.: usar uma poção). */
    fun requestCombatHpChange(combatantId: String, delta: Int, summary: String) {
        client?.requestAction(RequestKind.COMBAT_HP_CHANGE, summary, combatantId = combatantId, hpDelta = delta)
    }

    /** Jogador: pede ao Mestre para atualizar a ficha vinculada com uma versão mais recente. */
    fun requestCharacterUpdate(character: CharacterEntity, summary: String) {
        val characterJson = CharacterBackup.toJson(listOf(character)) { null }
        client?.requestAction(RequestKind.CHARACTER_UPDATE, summary, characterJson = characterJson)
    }

    /** Jogador: pede ao Mestre para pegar um item do baú de loot compartilhado. */
    fun requestTakeLoot(itemId: String, summary: String, character: CharacterEntity?) {
        val snapshot = character?.let { CharacterBackup.toJson(listOf(it)) { null } }
        client?.requestAction(RequestKind.TAKE_LOOT, summary, characterJson = snapshot, lootItemId = itemId)
    }

    /** Jogador: pede ao Mestre para comprar um item da loja de NPC aberta no momento. */
    fun requestBuyFromShop(itemId: String, summary: String, character: CharacterEntity?) {
        val snapshot = character?.let { CharacterBackup.toJson(listOf(it)) { null } }
        client?.requestAction(RequestKind.BUY_FROM_SHOP, summary, characterJson = snapshot, shopItemId = itemId)
    }

    /** Jogador: confirma que um grant foi salvo na ficha local. */
    fun acknowledgeResourceGrant(grantId: String) {
        client?.acknowledgeGrant(grantId)
    }

    /** Jogador: manda uma mensagem privada ao Mestre (não passa por aprovação). */
    fun sendPrivateMessageToDm(text: String) {
        client?.sendPrivateMessage(text)
    }

    /**
     * Jogador: tenta reconectar depois de uma queda inesperada (Wi-Fi instável, app em segundo
     * plano), com os mesmos host/porta/nome/ficha da última entrada — o Mestre reconhece o token
     * da sessão anterior e devolve o mesmo id de participante (sem duplicar no roster/combate).
     * Não faz nada se nunca chegou a entrar em uma sessão (não confundir com reentrar depois de
     * um [leaveSession] voluntário, que usa [joinSession] de novo).
     */
    fun reconnect() {
        val current = client ?: return
        _playerState.value = PlayerSessionUiState.Connecting
        current.reconnect()
    }

    /** Jogador: sai da sessão atual, se houver uma. */
    fun leaveSession() {
        client?.disconnect()
        client = null
        linkedPlayerCharacterId = null
        _playerState.value = PlayerSessionUiState.Idle
    }

    override fun onCleared() {
        val finalSnapshot = server?.stopAndCreateSnapshot()
        server = null
        if (finalSnapshot != null) {
            runCatching {
                kotlinx.coroutines.runBlocking(Dispatchers.IO) {
                    persistSnapshotAndWait(finalSnapshot)
                }
            }.onFailure { error ->
                CrashLogStore.record("MULTIPLAYER_PERSISTENCE", "Falha no checkpoint final ao encerrar o ViewModel", error)
            }
        }
        runCatching {
            kotlinx.coroutines.runBlocking(Dispatchers.IO) { flushPersistenceQueue() }
        }.onFailure { error ->
            CrashLogStore.record("MULTIPLAYER_PERSISTENCE", "Falha ao concluir a fila de persistência", error)
        }
        super.onCleared()
        persistenceCommands.close()
        leaveSession()
    }
}
