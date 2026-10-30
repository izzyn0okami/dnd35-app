package com.example.dnd35.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnd35.session.JoinPayload
import com.example.dnd35.session.LocalNetworkUtil
import com.example.dnd35.session.Participant
import com.example.dnd35.session.QrCodeUtil
import com.example.dnd35.session.SessionClient
import com.example.dnd35.session.SessionClientState
import com.example.dnd35.session.SessionServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Estado de tela do modo Mestre (hospedando uma sessão). */
sealed class DmSessionUiState {
    data object Idle : DmSessionUiState()
    data class Hosting(
        val sessionId: String,
        val qrBitmap: Bitmap,
        val qrPayload: String,
        val participants: List<Participant>
    ) : DmSessionUiState()
    data class Error(val message: String) : DmSessionUiState()
}

/** Estado de tela do modo Jogador (entrando em uma sessão de outro aparelho). */
sealed class PlayerSessionUiState {
    data object Idle : PlayerSessionUiState()
    data object Connecting : PlayerSessionUiState()
    data class Connected(val selfId: String, val participants: List<Participant>) : PlayerSessionUiState()
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

        viewModelScope.launchCollectRoster(newServer, sessionId, bitmap, qrContent)
    }

    private fun CoroutineScope.launchCollectRoster(
        activeServer: SessionServer,
        sessionId: String,
        bitmap: Bitmap,
        qrContent: String
    ) {
        launch {
            activeServer.roster.collect { participants ->
                if (server === activeServer) {
                    _dmState.value = DmSessionUiState.Hosting(sessionId, bitmap, qrContent, participants)
                }
            }
        }
    }

    /** Mestre: encerra a sessão ativa, desconectando todos os jogadores. */
    fun stopHosting() {
        server?.stop()
        server = null
        _dmState.value = DmSessionUiState.Idle
    }

    /** Jogador: entra em uma sessão a partir de um QR Code já decodificado. */
    fun joinSession(payload: JoinPayload, displayName: String) {
        joinSession(payload.hostAddress, payload.port, displayName)
    }

    /** Jogador: entra em uma sessão informando manualmente o IP e a porta do Mestre. */
    fun joinSession(hostAddress: String, port: Int, displayName: String) {
        leaveSession()
        val name = displayName.ifBlank { "Jogador" }
        _playerState.value = PlayerSessionUiState.Connecting
        val newClient = SessionClient()
        client = newClient
        viewModelScope.launchCollectClientState(newClient)
        newClient.connect(hostAddress, port, name)
    }

    private fun CoroutineScope.launchCollectClientState(activeClient: SessionClient) {
        launch {
            activeClient.state.collect { state ->
                if (client === activeClient) {
                    _playerState.value = when (state) {
                        is SessionClientState.Connecting -> PlayerSessionUiState.Connecting
                        is SessionClientState.Connected -> PlayerSessionUiState.Connected(state.selfId, state.participants)
                        is SessionClientState.Disconnected -> PlayerSessionUiState.Disconnected(state.reason)
                    }
                }
            }
        }
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
