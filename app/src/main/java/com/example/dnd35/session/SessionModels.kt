package com.example.dnd35.session

import org.json.JSONObject
import java.util.UUID

/**
 * Modelos da sessão de mesa offline (Mestre + Jogadores na mesma rede local).
 *
 * Nada aqui depende de internet ou de qualquer serviço externo: a sessão existe apenas
 * enquanto os aparelhos estiverem conectados entre si (mesma rede Wi-Fi local, ou qualquer
 * outro meio que lhes dê IPs alcançáveis entre si, como Wi-Fi Direct ou um hotspot local).
 */
enum class ParticipantRole {
    DM,
    PLAYER
}

/**
 * Um participante conectado à sessão. Nesta fase (conexão/QR), o participante ainda não está
 * vinculado a uma ficha específica — isso é feito em uma fase seguinte, quando o Mestre associa
 * cada jogador a um personagem já cadastrado no aparelho dele.
 */
data class Participant(
    val id: String,
    val displayName: String,
    val role: ParticipantRole,
    val characterId: Long? = null
)

/** Informações necessárias para um jogador se conectar à sessão de um Mestre. */
data class JoinPayload(
    val sessionId: String,
    val hostDisplayName: String,
    val hostAddress: String,
    val port: Int
) {
    fun toJson(): String = JSONObject().apply {
        put("sessionId", sessionId)
        put("hostDisplayName", hostDisplayName)
        put("hostAddress", hostAddress)
        put("port", port)
    }.toString()

    companion object {
        private const val KEY_SESSION_ID = "sessionId"
        private const val KEY_HOST_NAME = "hostDisplayName"
        private const val KEY_HOST_ADDRESS = "hostAddress"
        private const val KEY_PORT = "port"

        /** Retorna `null` quando [raw] não é um QR/código válido desta ficha. */
        fun fromJson(raw: String): JoinPayload? {
            return try {
                val obj = JSONObject(raw)
                JoinPayload(
                    sessionId = obj.getString(KEY_SESSION_ID),
                    hostDisplayName = obj.getString(KEY_HOST_NAME),
                    hostAddress = obj.getString(KEY_HOST_ADDRESS),
                    port = obj.getInt(KEY_PORT)
                )
            } catch (e: Exception) {
                null
            }
        }

        fun newSessionId(): String = UUID.randomUUID().toString()
    }
}
