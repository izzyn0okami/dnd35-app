package com.example.dnd35.session

import com.example.dnd35.backup.CharacterBackup
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.CharacterInventoryItem
import com.example.dnd35.model.CharacterJsonAdapters
import com.example.dnd35.diagnostics.CrashLogStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
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
    private val hostDisplayName: String
) {
    private var serverSocket: ServerSocket? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val clients = ConcurrentHashMap<String, ClientHandle>()
    // In-memory outbox: grants remain until the player confirms local persistence.
    private val pendingGrants = ConcurrentHashMap<String, ConcurrentHashMap<String, SessionMessage>>()
    // Serialize compound session mutations: pending requests/grants and combat state.
    // In particular, combat state changes and their broadcasts must share one critical section.
    private val stateLock = Any()

    // Token de reconexão (ver SessionMessage.Hello/Welcome) -> id de participante. Permite que um
    // Jogador cuja conexão caiu (Wi-Fi instável, app em segundo plano) volte como o MESMO
    // participante em vez de duplicar entrada no roster/combate ao reconectar.
    private val rejoinTokens = ConcurrentHashMap<String, String>()

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

    // Pedidos de jogadores aguardando aprovação do Mestre (ver RequestKind). Diferente das ações
    // do próprio Mestre (combate acima), que mutam o estado direto.
    private val _pendingRequests = MutableStateFlow<List<ActionRequest>>(emptyList())
    val pendingRequests: StateFlow<List<ActionRequest>> = _pendingRequests

    // Baú de loot compartilhado: itens que o Mestre disponibiliza (ex.: depois de um combate) e
    // qualquer jogador pode pedir para pegar (via RequestKind.TAKE_LOOT). Não pertence a nenhuma
    // ficha — só vira item de alguém quando um pedido de pegar é aprovado.
    private val _lootChest = MutableStateFlow<List<CharacterInventoryItem>>(emptyList())
    val lootChest: StateFlow<List<CharacterInventoryItem>> = _lootChest

    // Loja de NPC aberta pelo Mestre a partir do inventário de uma de suas fichas marcadas como
    // NPC (ver ShopModels.kt). `null` = nenhuma loja aberta no momento.
    private val _shop = MutableStateFlow<ShopState?>(null)
    val shop: StateFlow<ShopState?> = _shop

    // Log de mensagens privadas trocadas na sessão (Mestre <-> um Jogador específico). Visível
    // só para o Mestre — cada Jogador só vê as próprias, entregues direto via PrivateMessage.
    private val _privateChatLog = MutableStateFlow<List<PrivateChatEntry>>(emptyList())
    val privateChatLog: StateFlow<List<PrivateChatEntry>> = _privateChatLog

    private class ClientHandle(val id: String, val socket: Socket, val out: DataOutputStream, val outbound: Channel<SessionMessage>)


    /**
     * Captura um snapshot consistente de todo o estado em memória.
     */
    fun createSnapshot(): HostedSessionSnapshot = synchronized(stateLock) {
        HostedSessionSnapshot(
            sessionId = sessionId,
            hostDisplayName = hostDisplayName,
            roster = _roster.value.toList(),
            receivedCharacters = _receivedCharacters.value.toMap(),
            combat = _combatState.value,
            pendingRequests = _pendingRequests.value.toList(),
            lootChest = _lootChest.value.toList(),
            shop = _shop.value,
            privateChatLog = _privateChatLog.value.toList(),
            rejoinTokens = rejoinTokens.toMap(),
            pendingGrants = pendingGrants.mapValues { (_, grants) -> grants.values.toList() }
        )
    }

    /**
     * Restaura o domínio antes de abrir a porta TCP. Conexões antigas não sobrevivem à morte
     * do processo; jogadores voltam desconectados, mas conservam IDs, fichas, tokens e outbox.
     */
    fun restoreSnapshot(snapshot: HostedSessionSnapshot) = synchronized(stateLock) {
        require(snapshot.sessionId == sessionId) { "O snapshot pertence a outra sessão." }
        require(snapshot.hostDisplayName == hostDisplayName) { "O nome do Mestre não corresponde ao snapshot." }

        _roster.value = listOf(dmParticipant) + snapshot.roster
            .filterNot { it.role == ParticipantRole.DM }
            .map { it.copy(connected = false) }
        _receivedCharacters.value = snapshot.receivedCharacters.toMap()
        _combatState.value = snapshot.combat
        _pendingRequests.value = snapshot.pendingRequests.toList()
        _lootChest.value = snapshot.lootChest.toList()
        _shop.value = snapshot.shop
        _privateChatLog.value = snapshot.privateChatLog.toList()

        rejoinTokens.clear()
        rejoinTokens.putAll(snapshot.rejoinTokens.filterValues { participantId ->
            _roster.value.any { it.id == participantId && it.role == ParticipantRole.PLAYER }
        })
        pendingGrants.clear()
        snapshot.pendingGrants.forEach { (participantId, messages) ->
            if (_roster.value.any { it.id == participantId && it.role == ParticipantRole.PLAYER }) {
                val grants = ConcurrentHashMap<String, SessionMessage>()
                messages.forEach { message ->
                    val grantId = when (message) {
                        is SessionMessage.LootGranted -> message.grantId
                        is SessionMessage.ShopPurchaseGranted -> message.grantId
                        else -> error("Tipo de mensagem inválido no outbox")
                    }
                    grants[grantId] = message
                }
                if (grants.isNotEmpty()) pendingGrants[participantId] = grants
            }
        }
    }

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
        clients.values.forEach { handle -> handle.outbound.close(); runCatching { handle.socket.close() } }
        clients.clear()
        synchronized(stateLock) {
            pendingGrants.clear()
            rejoinTokens.clear()
            _roster.value = listOf(dmParticipant)
            _receivedCharacters.value = emptyMap()
            _combatState.value = null
            _pendingRequests.value = emptyList()
            _lootChest.value = emptyList()
            _shop.value = null
            _privateChatLog.value = emptyList()
        }
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
        var handle: ClientHandle? = null
        try {
            val first = SessionWire.read(input) ?: return
            val hello = first as? SessionMessage.Hello ?: return

            // Token de uma conexão anterior reconhecido -> é uma reconexão, reusa o mesmo id (e
            // com ele, o mesmo combatente em combate, ficha já recebida, pedidos pendentes etc.).
            // Token ausente/desconhecido (primeira vez, ou sessão foi reiniciada) -> participante novo.
            val existingId = hello.rejoinToken?.let { rejoinTokens[it] }
            val id = existingId ?: UUID.randomUUID().toString()
            val token = if (existingId != null) hello.rejoinToken!! else UUID.randomUUID().toString().also { rejoinTokens[it] = id }
            participantId = id

            val newHandle = ClientHandle(id, socket, output, Channel(OUTBOUND_QUEUE_CAPACITY))
            handle = newHandle
            clients.put(id, newHandle)?.let { stale -> stale.outbound.close(); runCatching { stale.socket.close() } }
            scope.launch {
                try {
                    for (message in newHandle.outbound) SessionWire.write(newHandle.out, message)
                } catch (e: Exception) {
                    CrashLogStore.record("MULTIPLAYER_SERVER", "Falha na fila de saída do participante $id", e)
                    runCatching { newHandle.socket.close() }
                }
            }
            enqueue(newHandle, SessionMessage.Welcome(id, sessionId, token))
            val linkedCharacterId = hello.characterId?.takeIf { it > 0L }
                ?: _roster.value.firstOrNull { it.id == id }?.characterId
            addParticipant(Participant(id, hello.displayName, ParticipantRole.PLAYER, characterId = linkedCharacterId))
            synchronized(stateLock) {
                // Snapshot inicial e broadcasts de alterações usam o mesmo lock: evita enviar
                // CombatUpdate/ShopUpdate antigos depois de uma atualização mais recente.
                _combatState.value?.let { enqueue(newHandle, SessionMessage.CombatUpdate(it)) }
                if (_lootChest.value.isNotEmpty()) {
                    enqueue(newHandle, SessionMessage.LootChestUpdate(CharacterJsonAdapters.serializeInventory(_lootChest.value)))
                }
                _shop.value?.let {
                    enqueue(newHandle, SessionMessage.ShopUpdate(it.npcName, CharacterJsonAdapters.serializeInventory(it.items)))
                }
                pendingGrants[id]?.values?.forEach { enqueue(newHandle, it) }
            }

            while (true) {
                val msg = SessionWire.read(input) ?: break
                when (msg) {
                    is SessionMessage.Bye -> break
                    is SessionMessage.CharacterSheet -> {
                        // CharacterSheet pode inicializar a ficha na primeira entrada ou completar
                        // uma entrada interrompida antes do primeiro snapshot. Se o Mestre já tem
                        // uma ficha, a mensagem reenviada na reconexão nunca pode sobrescrevê-la:
                        // atualizações posteriores exigem aprovação via CHARACTER_UPDATE.
                        synchronized(stateLock) {
                            if (!_receivedCharacters.value.containsKey(id)) {
                                receiveCharacterSheet(id, msg.characterJson)
                            }
                        }
                    }
                    is SessionMessage.ActionRequested -> receiveActionRequest(id, msg)
                    is SessionMessage.PrivateMessage -> receivePrivateMessage(id, msg.text)
                    is SessionMessage.GrantAcknowledged -> acknowledgeGrant(id, msg.grantId)
                    else -> Unit
                }
            }
        } catch (e: IOException) {
            CrashLogStore.record("MULTIPLAYER_SERVER", "Falha de comunicação com participante ${participantId ?: "não identificado"}", e)
        } catch (e: Exception) {
            CrashLogStore.record("MULTIPLAYER_SERVER", "Erro inesperado no processamento de conexão", e)
        } finally {
            val pid = participantId
            val h = handle
            // Só marca como desconectado se essa ainda for a conexão "atual" desse participante
            // (remove atômico por valor) — evita que uma conexão antiga, ao perceber a queda com
            // atraso, derrube por engano uma reconexão que já está em andamento (ver acima).
            if (pid != null && h != null && clients.remove(pid, h)) {
                markDisconnected(pid)
            }
            h?.outbound?.close()
            runCatching { socket.close() }
        }
    }

    private fun addParticipant(participant: Participant) = synchronized(stateLock) {
        _roster.value = _roster.value.filterNot { it.id == participant.id } + participant
        broadcastRoster()
    }

    /**
     * Marca um participante como desconectado SEM tirá-lo do roster nem descartar a última ficha
     * recebida — ele pode reconectar (mesmo id, via token) e o Mestre continua vendo quem ele era
     * enquanto isso. Pedidos pendentes são preservados e só podem ser aprovados quando o
     * participante estiver conectado; concessões já aprovadas permanecem no outbox até a
     * confirmação de persistência pelo cliente. Ver [stop] para a limpeza ao encerrar a sessão.
     */
    private fun markDisconnected(id: String) = synchronized(stateLock) {
        _roster.value = _roster.value.map { if (it.id == id) it.copy(connected = false) else it }
        // Keep pending requests while the participant is offline. They remain visible to the
        // DM and can be resolved after reconnection; resource requests must not be approved
        // while their recipient cannot receive the resulting grant.
        broadcastRoster()
    }

    /** Guarda a ficha mandada por um jogador; entradas inválidas são ignoradas silenciosamente. */
    private fun receiveCharacterSheet(participantId: String, characterJson: String): Boolean {
        val parsed = CharacterBackup.parse(characterJson) as? CharacterBackup.ParseResult.Success ?: return false
        val incoming = parsed.characters.firstOrNull()?.entity ?: return false
        _receivedCharacters.value = _receivedCharacters.value + (participantId to incoming)
        return true
    }

    private fun registerPendingGrant(participantId: String, grantId: String, message: SessionMessage) {
        pendingGrants.computeIfAbsent(participantId) { ConcurrentHashMap() }[grantId] = message
    }

    private fun acknowledgeGrant(participantId: String, grantId: String) = synchronized(stateLock) {
        // Serialize ACK removal with grant registration in approveRequest(). Otherwise an ACK
        // could observe an empty map while a new grant is being inserted, then remove that map
        // and accidentally discard the newly registered grant from the in-memory outbox.
        val grants = pendingGrants[participantId] ?: return@synchronized
        grants.remove(grantId)
        if (grants.isEmpty()) pendingGrants.remove(participantId, grants)
    }

    private fun broadcastRoster() {
        broadcast(SessionMessage.Roster(_roster.value))
    }

    private fun broadcast(message: SessionMessage) {
        clients.values.forEach { enqueue(it, message) }
    }

    private fun enqueue(handle: ClientHandle, message: SessionMessage): Boolean {
        if (handle.outbound.trySend(message).isSuccess) return true
        CrashLogStore.record("MULTIPLAYER_SERVER", "Fila de saída indisponível ou saturada para ${handle.id}")
        handle.outbound.close()
        runCatching { handle.socket.close() }
        return false
    }

    // --- Combate (estado global, autoridade do Mestre) ---

    /** Inicia um combate novo com os combatentes informados (a ordem de iniciativa é calculada aqui). */
    fun startCombat(combatants: List<Combatant>) = synchronized(stateLock) {
        val state = CombatState.start(combatants)
        _combatState.value = state
        broadcast(SessionMessage.CombatUpdate(state))
    }

    /** Aplica dano (positivo) ou cura (negativo) a um combatente e sincroniza com todos. */
    fun applyDamage(combatantId: String, amount: Int) = synchronized(stateLock) {
        val current = _combatState.value ?: return@synchronized
        val updatedCombatants = current.combatants.map {
            if (it.id == combatantId) it.withDamage(amount) else it
        }
        val updated = current.copy(combatants = updatedCombatants)
        _combatState.value = updated

        // Mantém a última ficha aprovada do jogador alinhada ao PV autoritativo do combate.
        // O mesmo lock impede que a atualização concorra com a aprovação de um snapshot de ficha.
        val changedCombatant = updatedCombatants.firstOrNull { it.id == combatantId }
        if (changedCombatant != null && !changedCombatant.isNpc) {
            val approvedSheet = _receivedCharacters.value[combatantId]
            if (approvedSheet != null &&
                (approvedSheet.currentHp != changedCombatant.currentHp ||
                    approvedSheet.tempHp != changedCombatant.tempHp)
            ) {
                _receivedCharacters.value = _receivedCharacters.value + (
                    combatantId to approvedSheet.copy(
                        currentHp = changedCombatant.currentHp,
                        tempHp = changedCombatant.tempHp
                    )
                )
            }
        }
        broadcast(SessionMessage.CombatUpdate(updated))
    }

    /** Avança para o próximo combatente com PV positivos e sincroniza a nova ordem/rodada. */
    fun nextTurn() {
        updateCombat { state -> state.nextTurn() }
    }

    /** Encerra o combate ativo (se houver) e avisa a todos. */
    fun endCombat() = synchronized(stateLock) {
        if (_combatState.value == null) return@synchronized
        _combatState.value = null
        broadcast(SessionMessage.EndCombat)
    }

    private inline fun updateCombat(transform: (CombatState) -> CombatState) = synchronized(stateLock) {
        val current = _combatState.value ?: return@synchronized
        val updated = transform(current)
        _combatState.value = updated
        broadcast(SessionMessage.CombatUpdate(updated))
    }

    // --- Baú de loot (estado global, autoridade do Mestre) ---

    /** Adiciona um item ao baú compartilhado e sincroniza com todos. */
    fun addLootItem(item: CharacterInventoryItem) = synchronized(stateLock) {
        _lootChest.value = _lootChest.value + item
        broadcastLootChest()
    }

    /** Remove um item do baú diretamente (não confundir com um pedido de jogador aprovado). */
    fun removeLootItem(itemId: String) = synchronized(stateLock) {
        if (_lootChest.value.none { it.id == itemId }) return@synchronized
        _lootChest.value = _lootChest.value.filterNot { it.id == itemId }
        broadcastLootChest()
    }

    private fun broadcastLootChest() {
        broadcast(SessionMessage.LootChestUpdate(CharacterJsonAdapters.serializeInventory(_lootChest.value)))
    }

    // --- Loja de NPC (estado global, autoridade do Mestre) ---

    /**
     * Abre (ou atualiza) a loja com os itens informados, tirados do inventário de um NPC do
     * Mestre no momento da chamada. Comprar não altera a ficha real do NPC — é só uma vitrine.
     */
    fun openShop(npcCharacterId: Long, npcName: String, items: List<CharacterInventoryItem>) = synchronized(stateLock) {
        val state = ShopState(npcCharacterId, npcName, items)
        _shop.value = state
        broadcastShop()
    }

    /** Fecha a loja aberta (se houver) e avisa a todos. */
    fun closeShop() = synchronized(stateLock) {
        if (_shop.value == null) return@synchronized
        _shop.value = null
        broadcast(SessionMessage.ShopClosed)
    }

    private fun broadcastShop() {
        val state = _shop.value ?: return
        broadcast(SessionMessage.ShopUpdate(state.npcName, CharacterJsonAdapters.serializeInventory(state.items)))
    }

    // --- Mensagens privadas (comunicação, não muda o estado do jogo) ---

    /** Guarda no log a mensagem que um Jogador mandou para o Mestre (não é repassada a mais ninguém). */
    private fun receivePrivateMessage(participantId: String, text: String) = synchronized(stateLock) {
        val name = _roster.value.firstOrNull { it.id == participantId }?.displayName ?: "Jogador"
        _privateChatLog.value = _privateChatLog.value + PrivateChatEntry(participantId = participantId, participantName = name, text = text, fromDm = false)
    }

    /** Mestre manda uma mensagem privada para um Jogador específico — só ele recebe. */
    fun sendPrivateMessage(participantId: String, text: String) {
        val name = synchronized(stateLock) {
            val participantName = _roster.value.firstOrNull { it.id == participantId }?.displayName
                ?: return@synchronized null
            _privateChatLog.value = _privateChatLog.value + PrivateChatEntry(
                participantId = participantId,
                participantName = participantName,
                text = text,
                fromDm = true
            )
            participantName
        } ?: return
        sendTo(participantId, SessionMessage.PrivateMessage(text))
    }

    // --- Pedidos/aprovações (ações iniciadas pelo Jogador, dependem do Mestre) ---

    /** Guarda um pedido de jogador na fila de pendentes, aguardando aprovação do Mestre. */
    private fun receiveActionRequest(participantId: String, msg: SessionMessage.ActionRequested) {
        // A player-submitted snapshot is never authoritative merely because a request
        // arrived. Resource validation uses the last server-approved sheet and its purse;
        // CHARACTER_UPDATE is applied only in approveRequest().

        val requesterName = _roster.value.firstOrNull { it.id == participantId }?.displayName ?: "Jogador"
        val request = ActionRequest(
            id = UUID.randomUUID().toString(),
            requesterId = participantId,
            requesterName = requesterName,
            kind = msg.kind,
            summary = msg.summary,
            characterJson = msg.characterJson,
            combatantId = msg.combatantId,
            hpDelta = msg.hpDelta,
            lootItemId = msg.lootItemId,
            shopItemId = msg.shopItemId
        )
        synchronized(stateLock) {
            _pendingRequests.value = _pendingRequests.value + request
        }
    }

    /** Aprova um pedido pendente: aplica a mudança correspondente e avisa quem pediu. */
    fun approveRequest(requestId: String) = synchronized(stateLock) {
        val request = _pendingRequests.value.firstOrNull { it.id == requestId } ?: return@synchronized
        val requester = _roster.value.firstOrNull { it.id == request.requesterId }
        if (requester?.connected != true) return@synchronized
        when (request.kind) {
            RequestKind.CHARACTER_UPDATE -> {
                val applied = request.characterJson?.let {
                    receiveCharacterSheet(request.requesterId, it)
                } == true
                if (applied) {
                    resolveRequest(request, approved = true)
                } else {
                    resolveRequest(request, approved = false, reason = "Snapshot de ficha inválido.")
                }
            }
            RequestKind.COMBAT_HP_CHANGE -> {
                val combatantId = request.combatantId
                val delta = request.hpDelta
                if (combatantId != null && delta != null) applyDamage(combatantId, delta)
                resolveRequest(request, approved = true)
            }
            RequestKind.TAKE_LOOT -> {
                val item = request.lootItemId?.let { id -> _lootChest.value.firstOrNull { it.id == id } }
                if (item == null) {
                    // Outro jogador pode ter pego o item antes, ou o Mestre já removeu do baú.
                    resolveRequest(request, approved = false, reason = "Item não está mais disponível no baú.")
                } else {
                    val characterId = _roster.value.firstOrNull { it.id == request.requesterId }?.characterId
                    if (characterId == null || _receivedCharacters.value[request.requesterId] == null) {
                        resolveRequest(request, approved = false, reason = "Vincule uma ficha válida antes de retirar itens do baú.")
                    } else {
                        val grantId = UUID.randomUUID().toString()
                        val grantedItem = item.copy(id = grantId)
                        val grant = SessionMessage.LootGranted(
                            grantId = grantId,
                            itemJson = CharacterJsonAdapters.serializeInventory(listOf(grantedItem)),
                            characterId = characterId
                        )
                        registerPendingGrant(request.requesterId, grantId, grant)
                        _lootChest.value = _lootChest.value.filterNot { it.id == item.id }
                        broadcastLootChest()
                        sendTo(request.requesterId, grant)
                        resolveRequest(request, approved = true)
                    }
                }
            }
            RequestKind.BUY_FROM_SHOP -> {
                val shopState = _shop.value
                val item = request.shopItemId?.let { id -> shopState?.items?.firstOrNull { it.id == id } }
                if (shopState == null || item == null || item.quantity <= 0) {
                    // A loja pode ter fechado, ou outro jogador já comprou esse item antes.
                    resolveRequest(request, approved = false, reason = "Item não está mais disponível na loja.")
                } else {
                    val characterId = _roster.value.firstOrNull { it.id == request.requesterId }?.characterId
                    val character = _receivedCharacters.value[request.requesterId]
                    if (characterId == null || character == null) {
                        resolveRequest(request, approved = false, reason = "Vincule uma ficha válida antes de comprar.")
                    } else {
                        val purse = SessionCurrency.debit(character, item.costGp)
                        if (purse == null) {
                            resolveRequest(request, approved = false, reason = "Saldo insuficiente ou preço/saldo inválido.")
                        } else {
                            val grantId = UUID.randomUUID().toString()
                            val grantedItem = item.copy(id = grantId, quantity = 1)
                            val grant = SessionMessage.ShopPurchaseGranted(
                                grantId = grantId,
                                itemJson = CharacterJsonAdapters.serializeInventory(listOf(grantedItem)),
                                characterId = characterId,
                                goldGp = purse.goldGp,
                                silverSp = purse.silverSp,
                                copperCp = purse.copperCp,
                                platinumPp = purse.platinumPp
                            )
                            registerPendingGrant(request.requesterId, grantId, grant)
                            _receivedCharacters.value = _receivedCharacters.value +
                                (request.requesterId to purse.toCharacterUpdate(character))
                            val remainingStock = if (item.quantity > 1) {
                                shopState.items.map { stock ->
                                    if (stock.id == item.id) stock.copy(quantity = stock.quantity - 1) else stock
                                }
                            } else {
                                shopState.items.filterNot { it.id == item.id }
                            }
                            _shop.value = shopState.copy(items = remainingStock)
                            broadcastShop()
                            sendTo(request.requesterId, grant)
                            resolveRequest(request, approved = true)
                        }
                    }
                }
            }
        }
    }

    /** Nega um pedido pendente: nada é aplicado, só avisa quem pediu (com motivo opcional). */
    fun denyRequest(requestId: String, reason: String = "") = synchronized(stateLock) {
        val request = _pendingRequests.value.firstOrNull { it.id == requestId } ?: return@synchronized
        resolveRequest(request, approved = false, reason = reason)
    }

    private fun resolveRequest(request: ActionRequest, approved: Boolean, reason: String = "") {
        _pendingRequests.value = _pendingRequests.value.filterNot { it.id == request.id }
        sendTo(request.requesterId, SessionMessage.RequestResolved(request.id, approved, reason))
    }

    /**
     * Mestre: força o encerramento da conexão de um participante agora (ex.: remover alguém que
     * não vai mais jogar), fechando só o socket dele — diferente de [stop], que encerra a sessão
     * inteira. Não revoga o token de reconexão: se o Jogador tentar de novo, ainda é reconhecido
     * como o mesmo participante (mesma regra de uma queda de rede comum).
     */
    fun disconnectParticipant(participantId: String) {
        clients[participantId]?.let { handle -> runCatching { handle.socket.close() } }
    }

    private fun sendTo(participantId: String, message: SessionMessage): Boolean {
        val handle = clients[participantId] ?: return false
        return enqueue(handle, message)
    }

    companion object {
        const val DM_PARTICIPANT_ID = "dm"
        private const val OUTBOUND_QUEUE_CAPACITY = 64
    }
}
