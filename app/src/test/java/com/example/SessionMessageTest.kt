package com.example

import com.example.dnd35.model.CharacterInventoryItem
import com.example.dnd35.model.CharacterJsonAdapters
import com.example.dnd35.session.Combatant
import com.example.dnd35.session.CombatState
import com.example.dnd35.session.JoinPayload
import com.example.dnd35.session.Participant
import com.example.dnd35.session.ParticipantRole
import com.example.dnd35.session.RequestKind
import com.example.dnd35.session.SessionMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Robolectric: mesmo não usando nenhuma classe exclusiva do Android, org.json.JSONObject
// (usado em SessionMessage/JoinPayload) é um stub em testes de JVM puro e lança
// "Method put in org.json.JSONObject not mocked" sem o Robolectric mockar essas classes.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SessionMessageTest {

    @Test
    fun `Hello sem rejoinToken serializa e desserializa corretamente`() {
        val msg = SessionMessage.Hello("Aragorn")
        val roundTrip = SessionMessage.fromJson(msg.toJson())
        assertEquals(msg, roundTrip)
        assertNull((roundTrip as SessionMessage.Hello).rejoinToken)
    }

    @Test
    fun `Hello com rejoinToken preserva o token (reconexao)`() {
        val msg = SessionMessage.Hello("Aragorn", rejoinToken = "token-123", characterId = 42L)
        val roundTrip = SessionMessage.fromJson(msg.toJson()) as SessionMessage.Hello
        assertEquals("token-123", roundTrip.rejoinToken)
        assertEquals(42L, roundTrip.characterId)
    }

    @Test
    fun `Welcome preserva o rejoinToken`() {
        val msg = SessionMessage.Welcome(participantId = "p1", sessionId = "s1", rejoinToken = "token-abc")
        val roundTrip = SessionMessage.fromJson(msg.toJson()) as SessionMessage.Welcome
        assertEquals(msg, roundTrip)
        assertEquals("token-abc", roundTrip.rejoinToken)
    }

    @Test
    fun `Roster nao expoe IDs locais de ficha e preserva estado de conexao`() {
        val participants = listOf(
            Participant("dm", "Mestre", ParticipantRole.DM),
            Participant("p1", "Legolas", ParticipantRole.PLAYER, characterId = 42L),
            Participant("p2", "Gimli", ParticipantRole.PLAYER, connected = false)
        )
        val msg = SessionMessage.Roster(participants)
        val roundTrip = SessionMessage.fromJson(msg.toJson()) as SessionMessage.Roster
        assertNull(roundTrip.participants.single { it.id == "p1" }.characterId)
        assertEquals(false, roundTrip.participants.single { it.id == "p2" }.connected)
    }

    @Test
    fun `Bye com motivo vazio por padrao`() {
        val msg = SessionMessage.Bye()
        val roundTrip = SessionMessage.fromJson(msg.toJson()) as SessionMessage.Bye
        assertEquals("", roundTrip.reason)
    }

    @Test
    fun `CharacterSheet preserva o JSON da ficha`() {
        val msg = SessionMessage.CharacterSheet("""{"app":"ficha-dnd35","characters":[]}""")
        val roundTrip = SessionMessage.fromJson(msg.toJson()) as SessionMessage.CharacterSheet
        assertEquals(msg.characterJson, roundTrip.characterJson)
    }

    @Test
    fun `CombatUpdate preserva combatentes, turno e rodada`() {
        val state = CombatState(
            combatants = listOf(
                Combatant(id = "p1", displayName = "Legolas", isNpc = false, initiative = 18, currentHp = 20, maxHp = 20, tempHp = 5, conditions = listOf("Abençoado")),
                Combatant(id = CombatState.npcCombatantId(99L), displayName = "Goblin", isNpc = true, initiative = 10, currentHp = 7, maxHp = 7)
            ),
            currentTurnIndex = 1,
            round = 3
        )
        val msg = SessionMessage.CombatUpdate(state)
        val roundTrip = SessionMessage.fromJson(msg.toJson()) as SessionMessage.CombatUpdate
        assertEquals(state, roundTrip.state)
    }

    @Test
    fun `EndCombat serializa e desserializa corretamente`() {
        val roundTrip = SessionMessage.fromJson(SessionMessage.EndCombat.toJson())
        assertEquals(SessionMessage.EndCombat, roundTrip)
    }

    @Test
    fun `nextTurn ignora somente combatentes mortos e inclui PV zero e negativos acima de -10`() {
        val state = CombatState.start(
            listOf(
                Combatant("a", "A", false, 20, -10, 20),
                Combatant("b", "B", false, 15, 0, 12),
                Combatant("c", "C", false, 10, -1, 10),
                Combatant("d", "D", false, 5, -9, 8),
                Combatant("e", "E", false, 1, -11, 8)
            )
        )

        val afterFirst = state.nextTurn()
        assertEquals("b", afterFirst.currentCombatantId)
        assertEquals(1, afterFirst.round)

        val afterSecond = afterFirst.nextTurn()
        assertEquals("c", afterSecond.currentCombatantId)
        assertEquals(1, afterSecond.round)

        val afterThird = afterSecond.nextTurn()
        assertEquals("d", afterThird.currentCombatantId)
        assertEquals(1, afterThird.round)

        val afterWrap = afterThird.nextTurn()
        assertEquals("b", afterWrap.currentCombatantId)
        assertEquals(2, afterWrap.round)
    }

    @Test
    fun `nextTurn sem combatentes vivos deixa turno sem alvo e nao incrementa rodada`() {
        val state = CombatState.start(
            listOf(
                Combatant("a", "A", false, 20, -10, 20),
                Combatant("b", "B", false, 10, -11, 10)
            )
        )

        val next = state.nextTurn()
        assertEquals(-1, next.currentTurnIndex)
        assertNull(next.currentCombatantId)
        assertEquals(1, next.round)
    }

    @Test
    fun `nextTurn com um unico combatente vivo avanca para a proxima rodada`() {
        val state = CombatState.start(
            listOf(
                Combatant("a", "A", false, 20, -10, 20),
                Combatant("b", "B", false, 10, 0, 10)
            )
        )

        val next = state.nextTurn()
        assertEquals("b", next.currentCombatantId)
        assertEquals(1, next.round)
        val nextRound = next.nextTurn()
        assertEquals("b", nextRound.currentCombatantId)
        assertEquals(2, nextRound.round)
    }

    @Test
    fun `ActionRequested de mudanca de PV preserva todos os campos`() {
        val msg = SessionMessage.ActionRequested(
            kind = RequestKind.COMBAT_HP_CHANGE,
            summary = "Pedido de dano (5 PV) em combate",
            combatantId = "p1",
            hpDelta = 5
        )
        val roundTrip = SessionMessage.fromJson(msg.toJson()) as SessionMessage.ActionRequested
        assertEquals(msg, roundTrip)
    }

    @Test
    fun `ActionRequested de atualizacao de ficha preserva o JSON e omite campos de combate`() {
        val msg = SessionMessage.ActionRequested(
            kind = RequestKind.CHARACTER_UPDATE,
            summary = "Atualização da ficha de Legolas",
            characterJson = """{"app":"ficha-dnd35","characters":[]}"""
        )
        val roundTrip = SessionMessage.fromJson(msg.toJson()) as SessionMessage.ActionRequested
        assertEquals(msg, roundTrip)
        assertNull(roundTrip.combatantId)
        assertNull(roundTrip.hpDelta)
    }

    @Test
    fun `RequestResolved preserva aprovacao e motivo`() {
        val approved = SessionMessage.RequestResolved("req-1", approved = true)
        assertEquals(approved, SessionMessage.fromJson(approved.toJson()))

        val denied = SessionMessage.RequestResolved("req-2", approved = false, reason = "Sem itens suficientes")
        assertEquals(denied, SessionMessage.fromJson(denied.toJson()))
    }

    @Test
    fun `ActionRequested de pegar loot preserva o lootItemId e omite os demais campos`() {
        val msg = SessionMessage.ActionRequested(
            kind = RequestKind.TAKE_LOOT,
            summary = "Pedido de pegar \"Poção de cura\" do baú de loot",
            lootItemId = "item-1"
        )
        val roundTrip = SessionMessage.fromJson(msg.toJson()) as SessionMessage.ActionRequested
        assertEquals(msg, roundTrip)
        assertNull(roundTrip.characterJson)
        assertNull(roundTrip.combatantId)
        assertNull(roundTrip.hpDelta)
    }

    @Test
    fun `LootChestUpdate preserva a lista de itens`() {
        val items = listOf(
            CharacterInventoryItem(id = "item-1", name = "Poção de cura", category = "Itens de Aventura", quantity = 2, weightLb = 0.5f, costGp = 50f)
        )
        val msg = SessionMessage.LootChestUpdate(CharacterJsonAdapters.serializeInventory(items))
        val roundTrip = SessionMessage.fromJson(msg.toJson()) as SessionMessage.LootChestUpdate
        assertEquals(items, CharacterJsonAdapters.deserializeInventory(roundTrip.itemsJson))
    }

    @Test
    fun `LootGranted preserva o item concedido`() {
        val item = CharacterInventoryItem(id = "item-2", name = "Espada longa +1", category = "Armas", quantity = 1)
        val msg = SessionMessage.LootGranted(grantId = "grant-1", itemJson = CharacterJsonAdapters.serializeInventory(listOf(item)), characterId = 42L)
        val roundTrip = SessionMessage.fromJson(msg.toJson()) as SessionMessage.LootGranted
        assertEquals(item, CharacterJsonAdapters.deserializeInventory(roundTrip.itemJson).single())
        assertEquals(42L, roundTrip.characterId)
        assertEquals("grant-1", roundTrip.grantId)
    }

    @Test
    fun `ActionRequested de comprar preserva quantidade solicitada`() {
        val msg = SessionMessage.ActionRequested(
            kind = RequestKind.BUY_FROM_SHOP,
            summary = "Comprar três unidades",
            shopItemId = "shop-item-1",
            quantity = 3
        )
        val roundTrip = SessionMessage.fromJson(msg.toJson()) as SessionMessage.ActionRequested
        assertEquals(msg, roundTrip)
        assertEquals(3, roundTrip.quantity)
    }

    @Test
    fun `ActionRequested de comprar da loja preserva o shopItemId e omite os demais campos`() {
        val msg = SessionMessage.ActionRequested(
            kind = RequestKind.BUY_FROM_SHOP,
            summary = "Pedido de comprar \"Poção de cura\" (50.0 po)",
            shopItemId = "shop-item-1"
        )
        val roundTrip = SessionMessage.fromJson(msg.toJson()) as SessionMessage.ActionRequested
        assertEquals(msg, roundTrip)
        assertNull(roundTrip.characterJson)
        assertNull(roundTrip.combatantId)
        assertNull(roundTrip.hpDelta)
        assertNull(roundTrip.lootItemId)
    }

    @Test
    fun `ShopUpdate preserva o nome do NPC e a lista de itens`() {
        val items = listOf(
            CharacterInventoryItem(id = "shop-item-1", name = "Poção de cura", category = "Itens de Aventura", quantity = 3, costGp = 50f)
        )
        val msg = SessionMessage.ShopUpdate("Vendedor Gundren", CharacterJsonAdapters.serializeInventory(items))
        val roundTrip = SessionMessage.fromJson(msg.toJson()) as SessionMessage.ShopUpdate
        assertEquals("Vendedor Gundren", roundTrip.npcName)
        assertEquals(items, CharacterJsonAdapters.deserializeInventory(roundTrip.itemsJson))
    }

    @Test
    fun `ShopClosed serializa e desserializa corretamente`() {
        val roundTrip = SessionMessage.fromJson(SessionMessage.ShopClosed.toJson())
        assertEquals(SessionMessage.ShopClosed, roundTrip)
    }

    @Test
    fun `ShopPurchaseGranted preserva o item comprado`() {
        val item = CharacterInventoryItem(id = "shop-item-2", name = "Adaga +1", category = "Armas", quantity = 1, costGp = 320f)
        val msg = SessionMessage.ShopPurchaseGranted(
            grantId = "grant-2",
            itemJson = CharacterJsonAdapters.serializeInventory(listOf(item)),
            characterId = 42L,
            goldGp = 1f,
            silverSp = 2f,
            copperCp = 3f,
            platinumPp = 4f
        )
        val roundTrip = SessionMessage.fromJson(msg.toJson()) as SessionMessage.ShopPurchaseGranted
        assertEquals(item, CharacterJsonAdapters.deserializeInventory(roundTrip.itemJson).single())
        assertEquals(42L, roundTrip.characterId)
        assertEquals("grant-2", roundTrip.grantId)
        assertEquals(1f, roundTrip.goldGp)
        assertEquals(2f, roundTrip.silverSp)
        assertEquals(3f, roundTrip.copperCp)
        assertEquals(4f, roundTrip.platinumPp)
    }

    @Test
    fun `GrantAcknowledged preserva o identificador da entrega`() {
        val msg = SessionMessage.GrantAcknowledged("grant-42")
        assertEquals(msg, SessionMessage.fromJson(msg.toJson()))
    }

    @Test
    fun `PrivateMessage preserva o texto`() {
        val msg = SessionMessage.PrivateMessage("Cuidado com a armadilha na próxima sala.")
        val roundTrip = SessionMessage.fromJson(msg.toJson()) as SessionMessage.PrivateMessage
        assertEquals(msg.text, roundTrip.text)
    }

    @Test
    fun `JSON invalido ou tipo desconhecido retorna null`() {
        assertNull(SessionMessage.fromJson("{not json"))
        assertNull(SessionMessage.fromJson("""{"type":"ALGO_DESCONHECIDO"}"""))
    }

    @Test
    fun `JoinPayload serializa e desserializa corretamente`() {
        val payload = JoinPayload(
            sessionId = "abc-123",
            hostDisplayName = "Mestre",
            hostAddress = "192.168.0.10",
            port = 54321
        )
        val roundTrip = JoinPayload.fromJson(payload.toJson())
        assertEquals(payload, roundTrip)
    }

    @Test
    fun `JoinPayload invalido retorna null em vez de lancar excecao`() {
        assertNull(JoinPayload.fromJson("isso nao e um QR da ficha"))
        assertNull(JoinPayload.fromJson("""{"sessionId":"s1"}"""))
    }

    @Test
    fun `JoinPayload rejeita endereco IPv4 e porta invalidos`() {
        val valid = JoinPayload("s1", "Mestre", "192.168.0.10", 54321)
        assertTrue(valid.isValid())
        assertNull(JoinPayload.fromJson(valid.copy(hostAddress = "999.168.0.10").toJson()))
        assertNull(JoinPayload.fromJson(valid.copy(hostAddress = "192.168.0").toJson()))
        assertNull(JoinPayload.fromJson(valid.copy(port = 0).toJson()))
        assertNull(JoinPayload.fromJson(valid.copy(port = 65536).toJson()))
        assertNull(JoinPayload.fromJson(valid.copy(sessionId = " ").toJson()))
    }

    @Test
    fun `validacao do endereco manual aceita apenas IPv4 completo`() {
        assertTrue(JoinPayload.isValidHostAddress("192.168.0.10"))
        assertTrue(JoinPayload.isValidHostAddress("10.0.0.255"))
        assertTrue(!JoinPayload.isValidHostAddress("192.168.0"))
        assertTrue(!JoinPayload.isValidHostAddress("256.1.1.1"))
        assertTrue(!JoinPayload.isValidHostAddress("192.168.0.1:54321"))
        assertTrue(!JoinPayload.isValidHostAddress("192.168.0.1 "))
    }

    @Test
    fun `newSessionId gera valores diferentes a cada chamada`() {
        val a = JoinPayload.newSessionId()
        val b = JoinPayload.newSessionId()
        assertTrue(a.isNotBlank())
        assertTrue(a != b)
    }
}
