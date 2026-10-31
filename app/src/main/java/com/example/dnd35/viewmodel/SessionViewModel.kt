package com.example.dnd35.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnd35.backup.CharacterBackup
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.session.ActionRequest
import com.example.dnd35.session.Combatant
import com.example.dnd35.session.CombatState
import com.example.dnd35.session.JoinPayload
import com.example.dnd35.session.LocalNetworkUtil
import com.example.dnd35.session.Participant
import com.example.dnd35.session.QrCodeUtil
import com.example.dnd35.session.RequestKind
import com.example.dnd35.session.SessionClient
import com.example.dnd35.session.SessionClientState
import com.example.dnd35.session.SessionServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
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
        val pendingRequests: List<ActionRequest> = emptyList()
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
        val combat: CombatState? = null
    ) : PlayerSessionUiState()
    data class Disconnected(val reason: String) : PlayerSessionUiState()
}

/**
 * ViewModel da sessão de mesa offline: cria (Mestre) ou entra (Jogador) em uma sessão local,
 * sem depender de internet — apenas da rede já compartilhada pelos aparelhos (Wi-Fi local,
 * hotspot do próprio Mestre, etc.).
 */
class SessionViewModel(application: Application) : AndroidViewModel(application) {

    private var server: SessionServer? = null
    private var client: SessionClient? = null

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

    /** Mestre: cria e hospeda uma nova sessão, exibindo o QR Code para os jogadores escanearem. */
    fun hostSession(hostDisplayName: String) {
        stopHosting()
        val sessionId = JoinPayload.newSessionId()
        val name = hostDisplayName.ifBlank { "Mestre" }
        val newServer = SessionServer(sessionId, name)
        server = newServer
        val port = newServer.start()

        val ip = LocalNetworkUtil.findLocalIpAddress()
        if (ip == null) {
            _dmState.value = DmSessionUiState.Error(
                "Não foi possível encontrar o endereço deste aparelho na rede local. " +
                    "Confirme se o Wi-Fi está ligado (não precisa ter internet, só precisa " +
                    "estar na mesma rede dos jogadores)."
            )
            return
        }

        val payload = JoinPayload(sessionId, name, ip, port)
        val qrContent = payload.toJson()
        val bitmap = QrCodeUtil.generate(qrContent)

        viewModelScope.launchCollectDmState(newServer, sessionId, bitmap, qrContent)
    }

    private fun CoroutineScope.launchCollectDmState(
        activeServer: SessionServer,
        sessionId: String,
        bitmap: Bitmap,
        qrContent: String
    ) {
        launch {
            combine(
                activeServer.roster,
                activeServer.receivedCharacters,
                activeServer.combatState,
                activeServer.pendingRequests
            ) { participants, characters, combat, requests ->
                DmSessionUiState.Hosting(sessionId, bitmap, qrContent, participants, characters, combat, requests)
            }.collect { newState ->
                if (server === activeServer) {
                    _dmState.value = newState
                }
            }
        }
    }

    /** Mestre: aprova um pedido pendente de jogador, aplicando a mudança correspondente. */
    fun approveRequest(requestId: String) {
        server?.approveRequest(requestId)
    }

    /** Mestre: nega um pedido pendente de jogador (nada é aplicado). */
    fun denyRequest(requestId: String) {
        server?.denyRequest(requestId)
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
    fun stopHosting() {
        server?.stop()
        server = null
        _dmState.value = DmSessionUiState.Idle
    }

    /** Jogador: entra em uma sessão a partir de um QR Code já decodificado. */
    fun joinSession(payload: JoinPayload, displayName: String, character: CharacterEntity?) {
        joinSession(payload.hostAddress, payload.port, displayName, character)
    }

    /** Jogador: entra em uma sessão informando manualmente o IP e a porta do Mestre. */
    fun joinSession(hostAddress: String, port: Int, displayName: String, character: CharacterEntity?) {
        leaveSession()
        _playerRequestFeedback.value = null
        val name = displayName.ifBlank { "Jogador" }
        // Sem foto: o retrato não é necessário para o Mestre acompanhar a ficha durante a sessão,
        // e evita inflar a mensagem enviada pela rede local.
        val characterJson = character?.let { CharacterBackup.toJson(listOf(it)) { null } }
        _playerState.value = PlayerSessionUiState.Connecting
        val newClient = SessionClient()
        client = newClient
        viewModelScope.launchCollectClientState(newClient)
        newClient.connect(hostAddress, port, name, characterJson)
    }

    private fun CoroutineScope.launchCollectClientState(activeClient: SessionClient) {
        launch {
            activeClient.state.collect { state ->
                if (client === activeClient) {
                    _playerState.value = when (state) {
                        is SessionClientState.Connecting -> PlayerSessionUiState.Connecting
                        is SessionClientState.Connected -> PlayerSessionUiState.Connected(state.selfId, state.participants, state.combat)
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

    /** Jogador: sai da sessão atual, se houver uma. */
    fun leaveSession() {
        client?.disconnect()
        client = null
        _playerState.value = PlayerSessionUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        stopHosting()
        leaveSession()
    }
}
