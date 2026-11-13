package com.example.dnd35.session

import com.example.dnd35.backup.CharacterBackup
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.CharacterInventoryItem
import com.example.dnd35.model.CharacterJsonAdapters
import org.json.JSONArray
import org.json.JSONObject

/**
 * Documento persistível do estado autoritativo do Mestre.
 *
 * Não contém sockets, flows ou estado de UI. A versão externa é validada antes de qualquer
 * restauração; versões futuras nunca são interpretadas parcialmente.
 */
data class MonsterCardShortcut(
    val instanceId: String,
    val creatureId: String
)

data class HostedSessionSnapshot(
    val sessionId: String,
    val hostDisplayName: String,
    val roster: List<Participant>,
    val receivedCharacters: Map<String, CharacterEntity>,
    val combat: CombatState?,
    val pendingRequests: List<ActionRequest>,
    val lootChest: List<CharacterInventoryItem>,
    val shop: ShopState?,
    val privateChatLog: List<PrivateChatEntry>,
    /** token de reconexão -> id persistente do participante */
    val rejoinTokens: Map<String, String>,
    /** participante -> grants serializados que ainda aguardam ACK */
    val pendingGrants: Map<String, List<SessionMessage>>,
    /** Atalhos de monstros escolhidos pelo Mestre; nunca são convertidos em fichas de personagem. */
    val monsterCards: List<MonsterCardShortcut> = emptyList()
)

object HostedSessionSnapshotCodec {
    const val VERSION = 1
    private const val ROOT_FORMAT = "dnd35-hosted-session"

    fun encode(snapshot: HostedSessionSnapshot): String {
        require(snapshot.sessionId.isNotBlank()) { "sessionId não pode ficar vazio" }
        val root = JSONObject()
            .put("format", ROOT_FORMAT)
            .put("version", VERSION)
            .put("sessionId", snapshot.sessionId)
            .put("hostDisplayName", snapshot.hostDisplayName)
            .put("roster", participantsToJson(snapshot.roster))
            .put("combat", snapshot.combat?.let { SessionMessage.CombatUpdate(it).toJson() } ?: JSONObject.NULL)

        val characters = JSONArray()
        snapshot.receivedCharacters.forEach { (participantId, character) ->
            characters.put(JSONObject()
                .put("participantId", participantId)
                .put("character", CharacterBackup.toJson(listOf(character)) { null }))
        }
        root.put("receivedCharacters", characters)

        val requests = JSONArray()
        snapshot.pendingRequests.forEach { requests.put(requestToJson(it)) }
        root.put("pendingRequests", requests)
        root.put("lootChest", CharacterJsonAdapters.serializeInventory(snapshot.lootChest))
        root.put("shop", snapshot.shop?.let { shopToJson(it) } ?: JSONObject.NULL)

        val chat = JSONArray()
        snapshot.privateChatLog.forEach { entry ->
            chat.put(JSONObject()
                .put("id", entry.id)
                .put("participantId", entry.participantId)
                .put("participantName", entry.participantName)
                .put("text", entry.text)
                .put("fromDm", entry.fromDm)
                .put("timestamp", entry.timestamp))
        }
        root.put("privateChatLog", chat)

        val tokens = JSONObject()
        snapshot.rejoinTokens.forEach { (token, participantId) -> tokens.put(token, participantId) }
        root.put("rejoinTokens", tokens)

        val grants = JSONObject()
        snapshot.pendingGrants.forEach { (participantId, messages) ->
            val entries = JSONArray()
            messages.forEach { entries.put(it.toJson()) }
            grants.put(participantId, entries)
        }
        root.put("pendingGrants", grants)
        root.put("monsterCards", JSONArray().apply {
            snapshot.monsterCards.forEach { card ->
                put(JSONObject().put("instanceId", card.instanceId).put("creatureId", card.creatureId))
            }
        })
        return root.toString()
    }

    fun decode(raw: String): HostedSessionSnapshot {
        val root = JSONObject(raw)
        require(root.optString("format") == ROOT_FORMAT) { "Formato de snapshot desconhecido" }
        val version = root.optInt("version", -1)
        require(version == VERSION) { "Versão de snapshot não suportada: $version" }

        val roster = participantsFromJson(root.getJSONArray("roster"))
        val combat = if (root.isNull("combat")) null else {
            (SessionMessage.fromJson(root.getString("combat")) as? SessionMessage.CombatUpdate)?.state
                ?: error("Combate inválido no snapshot")
        }

        val charactersJson = root.getJSONArray("receivedCharacters")
        val characters = linkedMapOf<String, CharacterEntity>()
        for (i in 0 until charactersJson.length()) {
            val entry = charactersJson.getJSONObject(i)
            val participantId = entry.getString("participantId")
            val parsed = CharacterBackup.parse(entry.getString("character"))
            val imported = (parsed as? CharacterBackup.ParseResult.Success)
                ?.characters?.firstOrNull()?.entity
                ?: error("Ficha inválida para participante $participantId")
            require(participantId.isNotBlank()) { "ID de participante vazio na ficha" }
            characters[participantId] = imported
        }

        val requestsJson = root.getJSONArray("pendingRequests")
        val requests = (0 until requestsJson.length()).map { requestFromJson(requestsJson.getJSONObject(it)) }

        val lootChest = decodeInventory(root.getString("lootChest"))
        val shop = if (root.isNull("shop")) null else shopFromJson(root.getJSONObject("shop"))

        val chatJson = root.getJSONArray("privateChatLog")
        val chat = (0 until chatJson.length()).map { i ->
            val entry = chatJson.getJSONObject(i)
            PrivateChatEntry(
                id = entry.getString("id"),
                participantId = entry.getString("participantId"),
                participantName = entry.getString("participantName"),
                text = entry.getString("text"),
                fromDm = entry.getBoolean("fromDm"),
                timestamp = entry.getLong("timestamp")
            )
        }

        val tokenObject = root.getJSONObject("rejoinTokens")
        val tokens = buildMap {
            val keys = tokenObject.keys()
            while (keys.hasNext()) {
                val token = keys.next()
                put(token, tokenObject.getString(token))
            }
        }

        val grantsObject = root.getJSONObject("pendingGrants")
        val grants = buildMap {
            val keys = grantsObject.keys()
            while (keys.hasNext()) {
                val participantId = keys.next()
                val entries = grantsObject.getJSONArray(participantId)
                val messages = (0 until entries.length()).map { index ->
                    SessionMessage.fromJson(entries.getString(index))
                        ?: error("Grant inválido para participante $participantId")
                }
                require(messages.all { it is SessionMessage.LootGranted || it is SessionMessage.ShopPurchaseGranted }) {
                    "Outbox contém mensagem que não é uma concessão de recurso"
                }
                put(participantId, messages)
            }
        }

        val monsterCardsJson = root.optJSONArray("monsterCards") ?: JSONArray()
        val monsterCards = (0 until monsterCardsJson.length()).map { index ->
            val item = monsterCardsJson.getJSONObject(index)
            MonsterCardShortcut(item.getString("instanceId"), item.getString("creatureId"))
        }

        val playerIds = roster.filter { it.role == ParticipantRole.PLAYER }.map { it.id }.toSet()
        require(roster.all { it.id.isNotBlank() }) { "Roster contém participante sem ID" }
        require(roster.map { it.id }.distinct().size == roster.size) { "Roster contém IDs duplicados" }
        require(roster.count { it.role == ParticipantRole.DM } <= 1) { "Roster contém mais de um Mestre" }
        require(characters.keys.all { it in playerIds }) { "Ficha recebida sem participante correspondente" }
        require(tokens.values.all { it in playerIds }) { "Token de reconexão aponta para participante ausente" }
        require(grants.keys.all { it in playerIds }) { "Outbox aponta para participante ausente" }
        require(requests.all { it.requesterId in playerIds }) { "Pedido pendente aponta para participante ausente" }
        require(monsterCards.all { it.instanceId.isNotBlank() && it.creatureId.isNotBlank() }) { "Atalho de monstro inválido" }
        require(monsterCards.map { it.instanceId }.distinct().size == monsterCards.size) { "Atalhos de monstro contêm IDs duplicados" }

        val sessionId = root.getString("sessionId")
        require(sessionId.isNotBlank()) { "sessionId vazio no snapshot" }
        return HostedSessionSnapshot(
            sessionId = sessionId,
            hostDisplayName = root.getString("hostDisplayName"),
            roster = roster,
            receivedCharacters = characters,
            combat = combat,
            pendingRequests = requests,
            lootChest = lootChest,
            shop = shop,
            privateChatLog = chat,
            rejoinTokens = tokens,
            pendingGrants = grants,
            monsterCards = monsterCards
        )
    }


    private fun participantsToJson(participants: List<Participant>): JSONArray = JSONArray().apply {
        participants.forEach { participant ->
            put(JSONObject()
                .put("id", participant.id)
                .put("displayName", participant.displayName)
                .put("role", participant.role.name)
                .put("characterId", participant.characterId ?: JSONObject.NULL)
                .put("connected", participant.connected))
        }
    }

    private fun participantsFromJson(array: JSONArray): List<Participant> =
        (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            Participant(
                id = item.getString("id"),
                displayName = item.getString("displayName"),
                role = ParticipantRole.valueOf(item.getString("role")),
                characterId = if (item.isNull("characterId")) null else item.getLong("characterId"),
                connected = item.getBoolean("connected")
            )
        }

    private fun requestToJson(request: ActionRequest) = JSONObject()
        .put("id", request.id)
        .put("requesterId", request.requesterId)
        .put("requesterName", request.requesterName)
        .put("kind", request.kind.name)
        .put("summary", request.summary)
        .put("characterJson", request.characterJson ?: JSONObject.NULL)
        .put("combatantId", request.combatantId ?: JSONObject.NULL)
        .put("hpDelta", request.hpDelta ?: JSONObject.NULL)
        .put("lootItemId", request.lootItemId ?: JSONObject.NULL)
        .put("shopItemId", request.shopItemId ?: JSONObject.NULL)

    private fun requestFromJson(json: JSONObject) = ActionRequest(
        id = json.getString("id"),
        requesterId = json.getString("requesterId"),
        requesterName = json.getString("requesterName"),
        kind = RequestKind.valueOf(json.getString("kind")),
        summary = json.getString("summary"),
        characterJson = json.optNullableString("characterJson"),
        combatantId = json.optNullableString("combatantId"),
        hpDelta = if (json.isNull("hpDelta")) null else json.getInt("hpDelta"),
        lootItemId = json.optNullableString("lootItemId"),
        shopItemId = json.optNullableString("shopItemId")
    )

    private fun shopToJson(shop: ShopState) = JSONObject()
        .put("npcCharacterId", shop.npcCharacterId)
        .put("npcName", shop.npcName)
        .put("items", CharacterJsonAdapters.serializeInventory(shop.items))

    private fun shopFromJson(json: JSONObject) = ShopState(
        npcCharacterId = json.getLong("npcCharacterId"),
        npcName = json.getString("npcName"),
        items = decodeInventory(json.getString("items"))
    )


    private fun decodeInventory(raw: String): List<CharacterInventoryItem> {
        val expectedCount = JSONArray(raw).length()
        val decoded = CharacterJsonAdapters.deserializeInventory(raw)
        require(decoded.size == expectedCount) { "Inventário incompleto ou inválido no snapshot" }
        return decoded
    }

    private fun JSONObject.optNullableString(key: String): String? =
        if (!has(key) || isNull(key)) null else getString(key)
}
