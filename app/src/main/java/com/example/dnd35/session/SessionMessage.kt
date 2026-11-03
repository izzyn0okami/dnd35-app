package com.example.dnd35.session

import org.json.JSONArray
import org.json.JSONObject
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.IOException

/**
 * Protocolo de mensagens trocadas entre o Mestre (servidor) e os Jogadores (clientes) via socket
 * TCP, dentro da rede local. Cada mensagem é um JSON simples com um campo "type".
 *
 * Fase atual (conexão + lista de participantes): HELLO, WELCOME, ROSTER e BYE. Fases futuras
 * (PV, iniciativa, pedidos/aprovações, trocas) vão adicionar novos tipos a este mesmo canal,
 * sem precisar trocar o transporte.
 */
sealed class SessionMessage {

    /**
     * Jogador -> Mestre: pedido para entrar na sessão com um nome de exibição. [rejoinToken] é o
     * token recebido num [Welcome] anterior (ver abaixo) — mandado de volta quando a conexão cai
     * e o Jogador reconecta, para o Mestre reconhecer que é a mesma pessoa e devolver o mesmo id
     * de participante (em vez de criar um novo, duplicado). `null` = entrada nova (primeira vez
     * nesta sessão, token desconhecido, ou sessão foi reiniciada).
     */
    data class Hello(val displayName: String, val rejoinToken: String? = null) : SessionMessage()

    /**
     * Mestre -> Jogador: confirma a entrada e informa o id atribuído a esse participante.
     * [rejoinToken] é sempre mandado (mesmo numa entrada nova) — o Jogador deve guardá-lo e
     * reenviar num [Hello] futuro para reconectar como o mesmo participante.
     */
    data class Welcome(val participantId: String, val sessionId: String, val rejoinToken: String = "") : SessionMessage()

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

    /**
     * Jogador -> Mestre: pedido de uma ação que precisa de aprovação (ver [RequestKind]). O
     * Mestre atribui um id a esse pedido ao recebê-lo (ver [SessionServer.pendingRequests]) —
     * aqui ainda não existe id porque quem está pedindo não sabe se vai ser aceito.
     */
    data class ActionRequested(
        val kind: RequestKind,
        val summary: String,
        val characterJson: String? = null,
        val combatantId: String? = null,
        val hpDelta: Int? = null,
        val lootItemId: String? = null,
        val shopItemId: String? = null
    ) : SessionMessage()

    /** Mestre -> Jogador que pediu: resultado da aprovação (aceito ou negado, com motivo opcional). */
    data class RequestResolved(val requestId: String, val approved: Boolean, val reason: String = "") : SessionMessage()

    /**
     * Mestre -> todos: conteúdo atual do baú de loot compartilhado da sessão. `itemsJson` usa o
     * mesmo formato de [com.example.dnd35.model.CharacterJsonAdapters.serializeInventory] (uma
     * lista, mesmo que vazia), para reaproveitar o parser já existente do inventário.
     */
    data class LootChestUpdate(val itemsJson: String) : SessionMessage()

    /**
     * Mestre -> Jogador que pegou: item concedido do baú de loot, já removido de lá (ver
     * [LootChestUpdate] que acompanha esta mensagem). `itemJson` usa o mesmo formato de
     * inventário, com um único item na lista.
     */
    data class LootGranted(val itemJson: String) : SessionMessage()

    /**
     * Mestre -> todos: loja de NPC aberta no momento, com o nome do NPC e seus itens (preço vem
     * de `costGp` de cada um). `itemsJson` usa o mesmo formato de inventário do baú de loot.
     */
    data class ShopUpdate(val npcName: String, val itemsJson: String) : SessionMessage()

    /** Mestre -> todos: fecha a loja de NPC que estava aberta. */
    data object ShopClosed : SessionMessage()

    /**
     * Mestre -> Jogador que comprou: item concedido pela compra, já removido da loja (ver
     * [ShopUpdate] que acompanha esta mensagem). O custo pago é o próprio `costGp` do item.
     */
    data class ShopPurchaseGranted(val itemJson: String) : SessionMessage()

    /**
     * Mensagem privada: de um Jogador para o Mestre (não é repassada a mais ninguém), ou do
     * Mestre para um Jogador específico (só aquele participante recebe). Não precisa de
     * aprovação — é comunicação, não muda o estado do jogo.
     */
    data class PrivateMessage(val text: String) : SessionMessage()

    fun toJson(): String {
        val obj = JSONObject()
        when (this) {
            is Hello -> obj.put("type", TYPE_HELLO)
                .put("displayName", displayName)
                .put("rejoinToken", rejoinToken ?: JSONObject.NULL)
            is Welcome -> obj.put("type", TYPE_WELCOME)
                .put("participantId", participantId)
                .put("sessionId", sessionId)
                .put("rejoinToken", rejoinToken)
            is Roster -> obj.put("type", TYPE_ROSTER).put("participants", JSONArray().also { arr ->
                participants.forEach { p ->
                    arr.put(
                        JSONObject()
                            .put("id", p.id)
                            .put("displayName", p.displayName)
                            .put("role", p.role.name)
                            .put("characterId", p.characterId ?: JSONObject.NULL)
                            .put("connected", p.connected)
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
            is ActionRequested -> obj.put("type", TYPE_ACTION_REQUESTED)
                .put("kind", kind.name)
                .put("summary", summary)
                .put("characterJson", characterJson ?: JSONObject.NULL)
                .put("combatantId", combatantId ?: JSONObject.NULL)
                .put("hpDelta", hpDelta ?: JSONObject.NULL)
                .put("lootItemId", lootItemId ?: JSONObject.NULL)
                .put("shopItemId", shopItemId ?: JSONObject.NULL)
            is RequestResolved -> obj.put("type", TYPE_REQUEST_RESOLVED)
                .put("requestId", requestId)
                .put("approved", approved)
                .put("reason", reason)
            is LootChestUpdate -> obj.put("type", TYPE_LOOT_CHEST_UPDATE).put("itemsJson", itemsJson)
            is LootGranted -> obj.put("type", TYPE_LOOT_GRANTED).put("itemJson", itemJson)
            is ShopUpdate -> obj.put("type", TYPE_SHOP_UPDATE).put("npcName", npcName).put("itemsJson", itemsJson)
            is ShopClosed -> obj.put("type", TYPE_SHOP_CLOSED)
            is ShopPurchaseGranted -> obj.put("type", TYPE_SHOP_PURCHASE_GRANTED).put("itemJson", itemJson)
            is PrivateMessage -> obj.put("type", TYPE_PRIVATE_MESSAGE).put("text", text)
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
        const val TYPE_ACTION_REQUESTED = "ACTION_REQUESTED"
        const val TYPE_REQUEST_RESOLVED = "REQUEST_RESOLVED"
        const val TYPE_LOOT_CHEST_UPDATE = "LOOT_CHEST_UPDATE"
        const val TYPE_LOOT_GRANTED = "LOOT_GRANTED"
        const val TYPE_SHOP_UPDATE = "SHOP_UPDATE"
        const val TYPE_SHOP_CLOSED = "SHOP_CLOSED"
        const val TYPE_SHOP_PURCHASE_GRANTED = "SHOP_PURCHASE_GRANTED"
        const val TYPE_PRIVATE_MESSAGE = "PRIVATE_MESSAGE"

        fun fromJson(raw: String): SessionMessage? {
            return try {
                val obj = JSONObject(raw)
                when (obj.getString("type")) {
                    TYPE_HELLO -> Hello(
                        obj.getString("displayName"),
                        if (obj.isNull("rejoinToken")) null else obj.getString("rejoinToken")
                    )
                    TYPE_WELCOME -> Welcome(
                        obj.getString("participantId"),
                        obj.getString("sessionId"),
                        obj.optString("rejoinToken", "")
                    )
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
                                        characterId = if (p.isNull("characterId")) null else p.getLong("characterId"),
                                        connected = if (p.has("connected")) p.getBoolean("connected") else true
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
                    TYPE_ACTION_REQUESTED -> ActionRequested(
                        kind = RequestKind.valueOf(obj.getString("kind")),
                        summary = obj.getString("summary"),
                        characterJson = if (obj.isNull("characterJson")) null else obj.getString("characterJson"),
                        combatantId = if (obj.isNull("combatantId")) null else obj.getString("combatantId"),
                        hpDelta = if (obj.isNull("hpDelta")) null else obj.getInt("hpDelta"),
                        lootItemId = if (obj.isNull("lootItemId")) null else obj.getString("lootItemId"),
                        shopItemId = if (obj.isNull("shopItemId")) null else obj.getString("shopItemId")
                    )
                    TYPE_REQUEST_RESOLVED -> RequestResolved(
                        requestId = obj.getString("requestId"),
                        approved = obj.getBoolean("approved"),
                        reason = obj.optString("reason", "")
                    )
                    TYPE_LOOT_CHEST_UPDATE -> LootChestUpdate(obj.getString("itemsJson"))
                    TYPE_LOOT_GRANTED -> LootGranted(obj.getString("itemJson"))
                    TYPE_SHOP_UPDATE -> ShopUpdate(obj.getString("npcName"), obj.getString("itemsJson"))
                    TYPE_SHOP_CLOSED -> ShopClosed
                    TYPE_SHOP_PURCHASE_GRANTED -> ShopPurchaseGranted(obj.getString("itemJson"))
                    TYPE_PRIVATE_MESSAGE -> PrivateMessage(obj.getString("text"))
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
class SessionProtocolException(message: String, cause: Throwable? = null) : IOException(message, cause)

object SessionWire {
    fun write(out: DataOutputStream, message: SessionMessage) {
        val bytes = message.toJson().toByteArray(Charsets.UTF_8)
        // Uma conexão pode receber mensagens de várias corrotinas. O frame inteiro
        // (tamanho + payload + flush) precisa ser atômico para não intercalar bytes.
        synchronized(out) {
            out.writeInt(bytes.size)
            out.write(bytes)
            out.flush()
        }
    }

    /** Retorna `null` se a conexão foi fechada corretamente (fim do stream). */
    fun read(input: DataInputStream): SessionMessage? {
        // Só EOF antes do primeiro byte representa fechamento limpo. EOF no meio do
        // cabeçalho ou do payload é corrupção/interrupção e precisa aparecer no diagnóstico.
        val length = try {
            val first = input.read()
            if (first == -1) return null
            (first shl 24) or
                (input.readUnsignedByte() shl 16) or
                (input.readUnsignedByte() shl 8) or
                input.readUnsignedByte()
        } catch (e: EOFException) {
            throw SessionProtocolException("Cabeçalho TCP incompleto.", e)
        }
        if (length <= 0 || length > MAX_MESSAGE_BYTES) {
            throw SessionProtocolException("Tamanho de mensagem inválido: $length bytes.")
        }
        val bytes = ByteArray(length)
        try {
            input.readFully(bytes)
        } catch (e: EOFException) {
            throw SessionProtocolException("Payload TCP incompleto: esperado $length bytes.", e)
        }
        return SessionMessage.fromJson(String(bytes, Charsets.UTF_8))
            ?: throw SessionProtocolException("Mensagem recebida contém JSON inválido ou tipo desconhecido.")
    }

    private const val MAX_MESSAGE_BYTES = 1 shl 20 // 1 MiB — generoso para listas de participantes
}
