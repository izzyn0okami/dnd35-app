package com.example

import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.CharacterInventoryItem
import com.example.dnd35.session.ActionRequest
import com.example.dnd35.session.Combatant
import com.example.dnd35.session.CombatState
import com.example.dnd35.session.HostedSessionSnapshot
import com.example.dnd35.session.HostedSessionSnapshotCodec
import com.example.dnd35.session.Participant
import com.example.dnd35.session.ParticipantRole
import com.example.dnd35.session.PrivateChatEntry
import com.example.dnd35.session.RequestKind
import com.example.dnd35.session.SessionMessage
import com.example.dnd35.session.SessionServer
import com.example.dnd35.session.ShopState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class HostedSessionSnapshotCodecTest {

    @Test
    fun `snapshot preserva estado completo da sessao e outbox`() {
        val character = CharacterEntity(
            name = "Legolas",
            characterClass = "Patrulheiro",
            level = 5,
            currentHp = 31,
            maxHp = 38,
            goldGp = 125f,
            inventoryJson = """[{"id":"old-item","name":"Arco longo","category":"Armas"}]"""
        )
        val loot = CharacterInventoryItem(id = "loot-1", name = "Poção de cura", category = "Itens de Aventura", quantity = 2)
        val shopItem = CharacterInventoryItem(id = "shop-1", name = "Adaga", category = "Armas", quantity = 3, costGp = 2f)
        val grant = SessionMessage.LootGranted("grant-1", """[{"id":"grant-1","name":"Poção","category":"Itens de Aventura"}]""", 77L)
        val snapshot = HostedSessionSnapshot(
            sessionId = "session-123",
            hostDisplayName = "Mestre",
            roster = listOf(
                Participant("dm", "Mestre", ParticipantRole.DM),
                Participant("p1", "Legolas", ParticipantRole.PLAYER, characterId = 77L, connected = false)
            ),
            receivedCharacters = mapOf("p1" to character),
            combat = CombatState(
                combatants = listOf(Combatant("p1", "Legolas", false, 18, 0, 38, tempHp = 4)),
                currentTurnIndex = 0,
                round = 4
            ),
            pendingRequests = listOf(
                ActionRequest("req-1", "p1", "Legolas", RequestKind.TAKE_LOOT, "Pegar poção", lootItemId = "loot-1")
            ),
            lootChest = listOf(loot),
            shop = ShopState(99L, "Vendedor", listOf(shopItem)),
            privateChatLog = listOf(PrivateChatEntry("chat-1", "p1", "Legolas", "Olá", false, 1234L)),
            rejoinTokens = mapOf("token-p1" to "p1"),
            pendingGrants = mapOf("p1" to listOf(grant))
        )

        val decoded = HostedSessionSnapshotCodec.decode(HostedSessionSnapshotCodec.encode(snapshot))

        assertEquals(snapshot.sessionId, decoded.sessionId)
        assertEquals(snapshot.hostDisplayName, decoded.hostDisplayName)
        assertEquals(77L, decoded.roster.single { it.id == "p1" }.characterId)
        assertEquals(false, decoded.roster.single { it.id == "p1" }.connected)
        assertEquals("Legolas", decoded.receivedCharacters.getValue("p1").name)
        assertEquals(125f, decoded.receivedCharacters.getValue("p1").goldGp)
        assertEquals(snapshot.combat, decoded.combat)
        assertEquals(snapshot.pendingRequests, decoded.pendingRequests)
        assertEquals(snapshot.lootChest, decoded.lootChest)
        assertEquals(snapshot.shop, decoded.shop)
        assertEquals(snapshot.privateChatLog, decoded.privateChatLog)
        assertEquals(snapshot.rejoinTokens, decoded.rejoinTokens)
        assertEquals(grant, decoded.pendingGrants.getValue("p1").single())
    }

    @Test
    fun `snapshot sem combate ou loja preserva valores nulos`() {
        val snapshot = HostedSessionSnapshot(
            sessionId = "session-456",
            hostDisplayName = "Mestre",
            roster = emptyList(),
            receivedCharacters = emptyMap(),
            combat = null,
            pendingRequests = emptyList(),
            lootChest = emptyList(),
            shop = null,
            privateChatLog = emptyList(),
            rejoinTokens = emptyMap(),
            pendingGrants = emptyMap()
        )

        val decoded = HostedSessionSnapshotCodec.decode(HostedSessionSnapshotCodec.encode(snapshot))

        assertNull(decoded.combat)
        assertNull(decoded.shop)
        assertEquals(emptyList<Participant>(), decoded.roster)
    }


    @Test
    fun `servidor restaura participantes desconectados e captura novamente o estado`() {
        val original = HostedSessionSnapshot(
            sessionId = "recoverable-session",
            hostDisplayName = "Mestre",
            roster = listOf(
                Participant("dm", "Mestre", ParticipantRole.DM),
                Participant("p1", "Legolas", ParticipantRole.PLAYER, characterId = 77L, connected = true)
            ),
            receivedCharacters = mapOf("p1" to CharacterEntity(name = "Legolas", currentHp = 12, maxHp = 20)),
            combat = null,
            pendingRequests = listOf(ActionRequest("req-1", "p1", "Legolas", RequestKind.TAKE_LOOT, "Pegar item")),
            lootChest = emptyList(),
            shop = null,
            privateChatLog = emptyList(),
            rejoinTokens = mapOf("rejoin-secret" to "p1"),
            pendingGrants = mapOf("p1" to listOf(
                SessionMessage.LootGranted("grant-1", """[{"id":"grant-1","name":"Poção","category":"Itens de Aventura"}]""", 77L)
            ))
        )
        val server = SessionServer(original.sessionId, original.hostDisplayName)

        try {
            server.restoreSnapshot(original)
            val restored = server.createSnapshot()

            assertEquals(77L, restored.roster.single { it.id == "p1" }.characterId)
            assertEquals(false, restored.roster.single { it.id == "p1" }.connected)
            assertEquals("Legolas", restored.receivedCharacters.getValue("p1").name)
            assertEquals(original.pendingRequests, restored.pendingRequests)
            assertEquals(original.rejoinTokens, restored.rejoinTokens)
            assertEquals(original.pendingGrants, restored.pendingGrants)
        } finally {
            server.stop()
        }
    }

    @Test
    fun `snapshot recusa versao futura em vez de interpretar parcialmente`() {
        val raw = HostedSessionSnapshotCodec.encode(
            HostedSessionSnapshot(
                sessionId = "s1",
                hostDisplayName = "Mestre",
                roster = emptyList(),
                receivedCharacters = emptyMap(),
                combat = null,
                pendingRequests = emptyList(),
                lootChest = emptyList(),
                shop = null,
                privateChatLog = emptyList(),
                rejoinTokens = emptyMap(),
                pendingGrants = emptyMap()
            )
        ).replace("\"version\":1", "\"version\":999")

        assertThrows(IllegalArgumentException::class.java) {
            HostedSessionSnapshotCodec.decode(raw)
        }
    }
}
