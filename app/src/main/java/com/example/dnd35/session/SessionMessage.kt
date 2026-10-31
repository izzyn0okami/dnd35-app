package com.example.dnd35.session

import org.json.JSONArray
import org.json.JSONObject
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException

/**
 * Protocolo de mensagens trocadas entre o Mestre (servidor) e os Jogadores (clientes) via socket
 * TCP, dentro da rede local. Cada mensagem é um JSON simples com um campo "type".
 *
 * Fase atual (conexão + lista de participantes): HELLO, WELCOME, ROSTER e BYE. Fases futuras
 * (PV, iniciativa, pedidos/aprovações, trocas) vão adicionar novos tipos a este mesmo canal,
 * sem precisar trocar o transporte.
 */
sealed class SessionMessage {

    /** Jogador -> Mestre: pedido para entrar na sessão com um nome de exibição. */
    data class Hello(val displayName: String) : SessionMessage()

    /** Mestre -> Jogador: confirma a entrada e informa o id atribuído a esse participante. */
    data class Welcome(val participantId: String, val sessionId: String) : SessionMessage()

    /** Mestre -> todos: lista atualizada de participantes conectados. */
    data class Roster(val participants: List<Participant>) : SessionMessage()

    /** Qualquer direção: encerramento da conexão (saída voluntária ou sessão encerrada). */
    data class Bye(val reason: String = "") : SessionMessage()

    /**
     * Jogador -> Mestre: ficha completa do personagem desse jogador, enviada logo após o
     * HELLO. `characterJson` é o mesmo formato usado no backup/exportação de fichas
     * ([com.example.dnd35.backup.CharacterBackup]), com uma única ficha dentro do envelope.
     * Só o Mestre guarda esse conteúdo (não é repassado aos outros jogadores no ROSTER).
     */
    data class CharacterSheet(val characterJson: String) : SessionMessage()

    /**
     * Mestre -> todos: estado de combate atual (quem está na iniciativa, de quem é a vez,
     * PV/condições de cada combatente). `null` em [CombatState] (fora deste tipo, representado
     * por não mandar a mensagem) significa "sem combate ativo" — ver [EndCombat].
     */
    data class CombatUpdate(val state: CombatState) : SessionMessage()

    /** Mestre -> todos: encerra o combate ativo (sem combatentes/iniciativa). */
    data object EndCombat : SessionMessage()

    fun toJson(): String {
        val obj = JSONObject()
        when (this) {
            is Hello -> obj.put("type", TYPE_HELLO).put("displayName", displayName)
            is Welcome -> obj.put("type", TYPE_WELCOME)
                .put("participantId", participantId)
                .put("sessionId", sessionId)
            is Roster -> obj.put("type", TYPE_ROSTER).put("participants", JSONArray().also { arr ->
                participants.forEach { p ->
                    arr.put(
                        JSONObject()
                            .put("id", p.id)
                            .put("displayName", p.displayName)
                            .put("role", p.role.name)
                            .put("characterId", p.characterId ?: JSONObject.NULL)
                    )
                }
            })
            is Bye -> obj.put("type", TYPE_BYE).put("reason", reason)
            is CharacterSheet -> obj.put("type", TYPE_CHARACTER_SHEET).put("characterJson", characterJson)
            is CombatUpdate -> obj.put("type", TYPE_COMBAT_UPDATE)
                .put("currentTurnIndex", state.currentTurnIndex)
                .put("round", state.round)
                .put("combatants", JSONArray().also { arr ->
                    state.combatants.forEach { c ->
                        arr.put(
                            JSONObject()
                                .put("id", c.id)
                                .put("displayName", c.displayName)
                                .put("isNpc", c.isNpc)
                                .put("initiative", c.initiative)
                                .put("currentHp", c.currentHp)
                                .put("maxHp", c.maxHp)
                                .put("tempHp", c.tempHp)
                                .put("conditions", JSONArray(c.conditions))
                        )
                    }
                })
            is EndCombat -> obj.put("type", TYPE_END_COMBAT)
        }
        return obj.toString()
    }

    companion object {
        const val TYPE_HELLO = "HELLO"
        const val TYPE_WELCOME = "WELCOME"
        const val TYPE_ROSTER = "ROSTER"
        const val TYPE_BYE = "BYE"
        const val TYPE_CHARACTER_SHEET = "CHARACTER_SHEET"
        const val TYPE_COMBAT_UPDATE = "COMBAT_UPDATE"
        const val TYPE_END_COMBAT = "END_COMBAT"

        fun fromJson(raw: String): SessionMessage? {
            return try {
                val obj = JSONObject(raw)
                when (obj.getString("type")) {
                    TYPE_HELLO -> Hello(obj.getString("displayName"))
                    TYPE_WELCOME -> Welcome(obj.getString("participantId"), obj.getString("sessionId"))
                    TYPE_ROSTER -> {
                        val arr = obj.getJSONArray("participants")
                        val list = buildList {
                            for (i in 0 until arr.length()) {
                                val p = arr.getJSONObject(i)
                                add(
                                    Participant(
                                        id = p.getString("id"),
                                        displayName = p.getString("displayName"),
                                        role = ParticipantRole.valueOf(p.getString("role")),
                                        characterId = if (p.isNull("characterId")) null else p.getLong("characterId")
                                    )
                                )
                            }
                        }
                        Roster(list)
                    }
                    TYPE_BYE -> Bye(obj.optString("reason", ""))
                    TYPE_CHARACTER_SHEET -> CharacterSheet(obj.getString("characterJson"))
                    TYPE_COMBAT_UPDATE -> {
                        val arr = obj.getJSONArray("combatants")
                        val combatants = buildList {
                            for (i in 0 until arr.length()) {
                                val c = arr.getJSONObject(i)
                                val conditionsArr = c.optJSONArray("conditions")
                                val conditions = buildList {
                                    if (conditionsArr != null) {
                                        for (j in 0 until conditionsArr.length()) add(conditionsArr.getString(j))
                                    }
                                }
                                add(
                                    Combatant(
                                        id = c.getString("id"),
                                        displayName = c.getString("displayName"),
                                        isNpc = c.getBoolean("isNpc"),
                                        initiative = c.getInt("initiative"),
                                        currentHp = c.getInt("currentHp"),
                                        maxHp = c.getInt("maxHp"),
                                        tempHp = c.optInt("tempHp", 0),
                                        conditions = conditions
                                    )
                                )
                            }
                        }
                        CombatUpdate(
                            CombatState(
                                combatants = combatants,
                                currentTurnIndex = obj.optInt("currentTurnIndex", 0),
                                round = obj.optInt("round", 1)
                            )
                        )
                    }
                    TYPE_END_COMBAT -> EndCombat
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}

/**
 * Framing simples sobre o socket: cada mensagem é escrita/lida como um inteiro de 4 bytes com o
 * tamanho em bytes seguido do conteúdo UTF-8. Evita qualquer ambiguidade com quebras de linha
 * dentro do JSON e funciona de forma idêntica em testes de JVM puro e no app em si.
 */
object SessionWire {
    fun write(out: DataOutputStream, message: SessionMessage) {
        val bytes = message.toJson().toByteArray(Charsets.UTF_8)
        out.writeInt(bytes.size)
        out.write(bytes)
        out.flush()
    }

    /** Retorna `null` se a conexão foi fechada corretamente (fim do stream). */
    fun read(input: DataInputStream): SessionMessage? {
        val length = try {
            input.readInt()
        } catch (e: EOFException) {
            return null
        }
        if (length <= 0 || length > MAX_MESSAGE_BYTES) return null
        val bytes = ByteArray(length)
        input.readFully(bytes)
        return SessionMessage.fromJson(String(bytes, Charsets.UTF_8))
    }

    private const val MAX_MESSAGE_BYTES = 1 shl 20 // 1 MiB — generoso para listas de participantes
}
