package com.example

import android.os.StrictMode
import com.example.dnd35.backup.CharacterBackup
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.CharacterInventoryItem
import com.example.dnd35.session.Combatant
import com.example.dnd35.session.CombatState
import com.example.dnd35.session.ParticipantRole
import com.example.dnd35.session.RequestKind
import com.example.dnd35.session.SessionClient
import com.example.dnd35.session.SessionClientState
import com.example.dnd35.session.SessionServer
import com.example.dnd35.session.SessionMessage
import com.example.dnd35.session.SessionWire
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Teste de integração em JVM puro (ServerSocket/Socket reais em loopback, sem emulador):
 * precisa do Robolectric mesmo assim porque SessionMessage usa org.json.JSONObject, que é
 * um stub sem mock fora do Robolectric (mesma causa documentada em SessionMessageTest).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SessionServerClientTest {

    @Test
    fun `escritas concorrentes preservam cada frame TCP`() {
        val bytes = ByteArrayOutputStream()
        val output = DataOutputStream(bytes)
        val workers = 8
        val messagesPerWorker = 100
        val executor = Executors.newFixedThreadPool(workers)
        try {
            val tasks = (0 until workers).map { worker ->
                executor.submit {
                    repeat(messagesPerWorker) { index ->
                        SessionWire.write(output, SessionMessage.PrivateMessage("$worker:$index"))
                    }
                }
            }
            tasks.forEach { it.get() }
        } finally {
            executor.shutdownNow()
        }

        val input = DataInputStream(ByteArrayInputStream(bytes.toByteArray()))
        val received = mutableSetOf<String>()
        repeat(workers * messagesPerWorker) {
            val message = SessionWire.read(input) as SessionMessage.PrivateMessage
            assertTrue("Mensagem duplicada: ${message.text}", received.add(message.text))
        }
        assertEquals(workers * messagesPerWorker, received.size)
        assertEquals(null, SessionWire.read(input))
    }

    @Test
    fun `frame com tamanho invalido gera erro de protocolo em vez de desconexao silenciosa`() {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).writeInt(0)
        val input = DataInputStream(ByteArrayInputStream(bytes.toByteArray()))
        try {
            SessionWire.read(input)
            throw AssertionError("Era esperado SessionProtocolException.")
        } catch (expected: com.example.dnd35.session.SessionProtocolException) {
            assertTrue(expected.message!!.contains("inválido"))
        }
    }

    @Test
    fun `jogador conecta, recebe Welcome e aparece no roster do Mestre`() = runBlocking {
        val server = SessionServer(sessionId = "sess-1", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Legolas")

                val connected = withTimeout(15_000) {
                    client.state.first { it is SessionClientState.Connected } as SessionClientState.Connected
                }
                assertEquals("sess-1", connected.sessionId)

                // O servidor deve propagar o novo participante no roster (Mestre + Jogador).
                val roster = withTimeout(15_000) {
                    server.roster.first { it.size == 2 }
                }
                assertTrue(roster.any { it.role == ParticipantRole.DM })
                val player = roster.first { it.role == ParticipantRole.PLAYER }
                assertEquals("Legolas", player.displayName)
                assertEquals(connected.selfId, player.id)
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `ao sair, jogador continua no roster do Mestre mas marcado como desconectado`() = runBlocking {
        // Diferente de simplesmente sumir: o jogador pode reconectar (ver testes de reconexão
        // abaixo), então o Mestre continua vendo quem ele era, só sinalizado como offline.
        val server = SessionServer(sessionId = "sess-2", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val client = SessionClient()
            client.connect("127.0.0.1", port, "Gimli")
            withTimeout(15_000) { client.state.first { it is SessionClientState.Connected } }
            withTimeout(15_000) { server.roster.first { it.size == 2 } }

            client.disconnect()

            val rosterAfterLeave = withTimeout(15_000) {
                server.roster.first { roster -> roster.any { it.role == ParticipantRole.PLAYER && !it.connected } }
            }
            assertEquals(2, rosterAfterLeave.size)
            val player = rosterAfterLeave.single { it.role == ParticipantRole.PLAYER }
            assertEquals("Gimli", player.displayName)
            assertEquals(false, player.connected)
        } finally {
            server.stop()
        }
    }

    @Test
    fun `ficha enviada pelo jogador aparece do lado do Mestre`() = runBlocking {
        val server = SessionServer(sessionId = "sess-3", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val character = CharacterEntity(name = "Legolas", characterClass = "Patrulheiro", level = 5)
            val characterJson = CharacterBackup.toJson(listOf(character)) { null }

            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Legolas", characterJson)
                val connected = withTimeout(15_000) {
                    client.state.first { it is SessionClientState.Connected } as SessionClientState.Connected
                }

                val received = withTimeout(15_000) {
                    server.receivedCharacters.first { it.containsKey(connected.selfId) }
                }
                val entity = received.getValue(connected.selfId)
                assertEquals("Legolas", entity.name)
                assertEquals("Patrulheiro", entity.characterClass)
                assertEquals(5, entity.level)
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `jogador sem ficha nao aparece em receivedCharacters`() = runBlocking {
        val server = SessionServer(sessionId = "sess-4", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Observador")
                withTimeout(15_000) { client.state.first { it is SessionClientState.Connected } }
                // Dá tempo da mensagem (se houvesse) chegar antes de garantir que nada chegou.
                withTimeout(15_000) { server.roster.first { it.size == 2 } }
                assertTrue(server.receivedCharacters.value.isEmpty())
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `startCombat sincroniza estado de combate com o jogador conectado`() = runBlocking {
        val server = SessionServer(sessionId = "sess-5", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Legolas")
                val connected = withTimeout(15_000) {
                    client.state.first { it is SessionClientState.Connected } as SessionClientState.Connected
                }

                val combatants = listOf(
                    Combatant(id = connected.selfId, displayName = "Legolas", isNpc = false, initiative = 18, currentHp = 20, maxHp = 20),
                    Combatant(id = CombatState.npcCombatantId(1L), displayName = "Goblin", isNpc = true, initiative = 9, currentHp = 7, maxHp = 7)
                )
                server.startCombat(combatants)

                val clientCombat = withTimeout(15_000) {
                    client.state.mapNotNull { (it as? SessionClientState.Connected)?.combat }.first()
                }
                assertEquals(listOf(connected.selfId, CombatState.npcCombatantId(1L)), clientCombat.combatants.map { it.id })
                assertEquals(0, clientCombat.currentTurnIndex)
                assertEquals(1, clientCombat.round)
                assertEquals(connected.selfId, clientCombat.currentCombatantId)
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `broadcast do servidor nao executa escrita de socket na thread chamadora`() = runBlocking {
        val server = SessionServer(sessionId = "sess-main-thread", hostDisplayName = "Mestre")
        val port = withContext(Dispatchers.IO) { server.start() }
        val client = SessionClient()
        try {
            client.connect("127.0.0.1", port, "Legolas")
            val connected = withTimeout(15_000) {
                client.state.first { it is SessionClientState.Connected } as SessionClientState.Connected
            }
            val previousPolicy = StrictMode.getThreadPolicy()
            try {
                StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectNetwork().penaltyDeath().build())
                server.startCombat(
                    listOf(Combatant(id = connected.selfId, displayName = "Legolas", isNpc = false, initiative = 17, currentHp = 20, maxHp = 20))
                )
            } finally {
                StrictMode.setThreadPolicy(previousPolicy)
            }
            val receivedCombat = withTimeout(15_000) {
                client.state.mapNotNull { (it as? SessionClientState.Connected)?.combat }.first()
            }
            assertEquals(connected.selfId, receivedCombat.combatants.single().id)
        } finally {
            client.disconnect()
            server.stop()
        }
    }

    @Test
    fun `PV do combate atualizam a ficha aprovada no servidor e preservam PV temporarios`() = runBlocking {
        val server = SessionServer(sessionId = "sess-combat-sheet", hostDisplayName = "Mestre")
        val port = server.start()
        val character = CharacterEntity(
            name = "Gimli",
            currentHp = 20,
            maxHp = 30,
            tempHp = 4
        )
        val client = SessionClient()
        try {
            client.connect(
                "127.0.0.1",
                port,
                "Gimli",
                CharacterBackup.toJson(listOf(character)) { null }
            )
            val connected = withTimeout(15_000) {
                client.state.first { it is SessionClientState.Connected } as SessionClientState.Connected
            }
            withTimeout(15_000) {
                server.receivedCharacters.first { it.containsKey(connected.selfId) }
            }

            server.startCombat(
                listOf(
                    Combatant(
                        id = connected.selfId,
                        displayName = "Gimli",
                        isNpc = false,
                        initiative = 12,
                        currentHp = 20,
                        maxHp = 30,
                        tempHp = 4
                    )
                )
            )
            server.applyDamage(connected.selfId, 10)
            val afterDamage = withTimeout(15_000) {
                server.receivedCharacters.first {
                    it[connected.selfId]?.currentHp == 14 && it[connected.selfId]?.tempHp == 0
                }
            }
            assertEquals(14, afterDamage.getValue(connected.selfId).currentHp)
            assertEquals(0, afterDamage.getValue(connected.selfId).tempHp)

            server.applyDamage(connected.selfId, -5)
            val afterHealing = withTimeout(15_000) {
                server.receivedCharacters.first { it[connected.selfId]?.currentHp == 19 }
            }
            assertEquals(19, afterHealing.getValue(connected.selfId).currentHp)
            assertEquals(30, afterHealing.getValue(connected.selfId).maxHp)
        } finally {
            client.disconnect()
            server.stop()
        }
    }

    @Test
    fun `applyDamage, nextTurn e endCombat sincronizam com o jogador`() = runBlocking {
        val server = SessionServer(sessionId = "sess-6", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Gimli")
                val connected = withTimeout(15_000) {
                    client.state.first { it is SessionClientState.Connected } as SessionClientState.Connected
                }

                server.startCombat(
                    listOf(
                        Combatant(id = connected.selfId, displayName = "Gimli", isNpc = false, initiative = 15, currentHp = 30, maxHp = 30)
                    )
                )
                withTimeout(15_000) {
                    client.state.mapNotNull { (it as? SessionClientState.Connected)?.combat }.first()
                }

                server.applyDamage(connected.selfId, 12)
                val afterDamage = withTimeout(15_000) {
                    client.state.mapNotNull { (it as? SessionClientState.Connected)?.combat }
                        .first { it.combatants.first().currentHp == 18 }
                }
                assertEquals(18, afterDamage.combatants.single().currentHp)

                server.nextTurn()
                val afterNextTurn = withTimeout(15_000) {
                    client.state.mapNotNull { (it as? SessionClientState.Connected)?.combat }
                        .first { it.round == 2 }
                }
                assertEquals(0, afterNextTurn.currentTurnIndex)
                assertEquals(2, afterNextTurn.round)

                server.endCombat()
                val afterEnd = withTimeout(15_000) {
                    client.state.first { (it as? SessionClientState.Connected)?.combat == null }
                } as SessionClientState.Connected
                assertEquals(null, afterEnd.combat)
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `mutacoes concorrentes de dano e turno nao perdem atualizacoes`() {
        val server = SessionServer(sessionId = "sess-combat-concurrency", hostDisplayName = "Mestre")
        val combatantId = "player-concurrent"
        val damageOperations = 1_000
        val turnOperations = 1_000
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)
        try {
            server.startCombat(
                listOf(
                    Combatant(
                        id = combatantId,
                        displayName = "Gimli",
                        isNpc = false,
                        initiative = 15,
                        currentHp = damageOperations,
                        maxHp = damageOperations
                    )
                )
            )
            val damageTask = executor.submit {
                start.await()
                repeat(damageOperations) { server.applyDamage(combatantId, 1) }
            }
            val turnTask = executor.submit {
                start.await()
                repeat(turnOperations) { server.nextTurn() }
            }

            start.countDown()
            damageTask.get()
            turnTask.get()

            val finalState = server.combatState.value
            assertEquals(damageOperations + 1, finalState?.round)
            assertEquals(0, finalState?.combatants?.single()?.currentHp)
        } finally {
            start.countDown()
            executor.shutdownNow()
            server.stop()
        }
    }

    @Test
    fun `jogador que entra durante combate ja recebe o estado atual`() = runBlocking {
        val server = SessionServer(sessionId = "sess-7", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            // Combate iniciado só com um NPC do Mestre, antes de qualquer jogador conectar.
            server.startCombat(
                listOf(Combatant(id = CombatState.npcCombatantId(2L), displayName = "Orc", isNpc = true, initiative = 11, currentHp = 15, maxHp = 15))
            )

            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Retardatário")
                val lateJoinCombat = withTimeout(15_000) {
                    client.state.mapNotNull { (it as? SessionClientState.Connected)?.combat }.first()
                }
                assertEquals(CombatState.npcCombatantId(2L), lateJoinCombat.combatants.single().id)
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `pedido de mudanca de PV aprovado aplica dano e avisa o jogador`() = runBlocking {
        val server = SessionServer(sessionId = "sess-8", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Legolas")
                val connected = withTimeout(15_000) {
                    client.state.first { it is SessionClientState.Connected } as SessionClientState.Connected
                }
                server.startCombat(
                    listOf(Combatant(id = connected.selfId, displayName = "Legolas", isNpc = false, initiative = 12, currentHp = 20, maxHp = 20))
                )
                withTimeout(15_000) { client.state.mapNotNull { (it as? SessionClientState.Connected)?.combat }.first() }

                client.requestAction(RequestKind.COMBAT_HP_CHANGE, "Pedido de dano (5 PV)", combatantId = connected.selfId, hpDelta = 5)

                val pending = withTimeout(15_000) { server.pendingRequests.first { it.isNotEmpty() } }
                val request = pending.single()
                assertEquals("Legolas", request.requesterName)
                assertEquals(RequestKind.COMBAT_HP_CHANGE, request.kind)

                // CoroutineStart.UNDISPATCHED garante que a inscrição em requestResults já
                // aconteceu antes de aprovar — sem isso, o teste fica à mercê da sorte entre o
                // laço de leitura do cliente (socket real, outra thread) e o agendamento deste
                // coletor no event loop do runBlocking (replay=0: quem chega depois perde o evento).
                val resolvedDeferred = async(start = CoroutineStart.UNDISPATCHED) {
                    withTimeout(15_000) { client.requestResults.first() }
                }
                server.approveRequest(request.id)

                val resolved = resolvedDeferred.await()
                assertTrue(resolved.approved)

                val combatAfter = withTimeout(15_000) {
                    client.state.mapNotNull { (it as? SessionClientState.Connected)?.combat }
                        .first { it.combatants.first().currentHp == 15 }
                }
                assertEquals(15, combatAfter.combatants.single().currentHp)
                assertTrue(server.pendingRequests.value.isEmpty())
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `pedido negado nao altera o estado e avisa o jogador`() = runBlocking {
        val server = SessionServer(sessionId = "sess-9", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Gimli")
                val connected = withTimeout(15_000) {
                    client.state.first { it is SessionClientState.Connected } as SessionClientState.Connected
                }
                server.startCombat(
                    listOf(Combatant(id = connected.selfId, displayName = "Gimli", isNpc = false, initiative = 10, currentHp = 30, maxHp = 30))
                )
                withTimeout(15_000) { client.state.mapNotNull { (it as? SessionClientState.Connected)?.combat }.first() }

                client.requestAction(RequestKind.COMBAT_HP_CHANGE, "Pedido de cura (10 PV)", combatantId = connected.selfId, hpDelta = -10)
                val request = withTimeout(15_000) { server.pendingRequests.first { it.isNotEmpty() } }.single()

                val resolvedDeferred = async(start = CoroutineStart.UNDISPATCHED) {
                    withTimeout(15_000) { client.requestResults.first() }
                }
                server.denyRequest(request.id, reason = "Sem poção de cura disponível")

                val resolved = resolvedDeferred.await()
                assertEquals(false, resolved.approved)
                assertEquals("Sem poção de cura disponível", resolved.reason)
                assertTrue(server.pendingRequests.value.isEmpty())
                assertEquals(30, server.combatState.value?.combatants?.single()?.currentHp)
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `pedido de atualizacao de ficha aprovado atualiza receivedCharacters`() = runBlocking {
        val server = SessionServer(sessionId = "sess-10", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val original = CharacterEntity(name = "Legolas", characterClass = "Patrulheiro", level = 5, currentHp = 30, maxHp = 40)
            val originalJson = CharacterBackup.toJson(listOf(original)) { null }

            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Legolas", originalJson)
                val connected = withTimeout(15_000) {
                    client.state.first { it is SessionClientState.Connected } as SessionClientState.Connected
                }
                withTimeout(15_000) { server.receivedCharacters.first { it.containsKey(connected.selfId) } }

                val updated = original.copy(currentHp = 25)
                val updatedJson = CharacterBackup.toJson(listOf(updated)) { null }
                client.requestAction(RequestKind.CHARACTER_UPDATE, "Atualização da ficha de Legolas", characterJson = updatedJson)

                val request = withTimeout(15_000) { server.pendingRequests.first { it.isNotEmpty() } }.single()
                assertEquals(RequestKind.CHARACTER_UPDATE, request.kind)
                // The requested snapshot is not applied before the DM's decision.
                assertEquals(30, server.receivedCharacters.value[connected.selfId]?.currentHp)

                val resolvedDeferred = async(start = CoroutineStart.UNDISPATCHED) {
                    withTimeout(15_000) { client.requestResults.first() }
                }
                server.approveRequest(request.id)
                resolvedDeferred.await()

                val finalCharacters = withTimeout(15_000) {
                    server.receivedCharacters.first { it[connected.selfId]?.currentHp == 25 }
                }
                assertEquals(25, finalCharacters.getValue(connected.selfId).currentHp)
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `pedido de atualizacao com snapshot invalido e negado sem alterar ficha`() = runBlocking {
        val server = SessionServer(sessionId = "sess-invalid-character-update", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val original = CharacterEntity(
                name = "Legolas",
                characterClass = "Patrulheiro",
                level = 5,
                currentHp = 30,
                maxHp = 40
            )
            val originalJson = CharacterBackup.toJson(listOf(original)) { null }
            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Legolas", originalJson)
                val connected = withTimeout(15_000) {
                    client.state.first { it is SessionClientState.Connected } as SessionClientState.Connected
                }
                withTimeout(15_000) { server.receivedCharacters.first { it.containsKey(connected.selfId) } }

                client.requestAction(
                    RequestKind.CHARACTER_UPDATE,
                    "Atualização inválida da ficha",
                    characterJson = "{snapshot-invalido"
                )
                val request = withTimeout(15_000) {
                    server.pendingRequests.first { it.any { pending -> pending.kind == RequestKind.CHARACTER_UPDATE } }
                }.single()

                val resolvedDeferred = async(start = CoroutineStart.UNDISPATCHED) {
                    withTimeout(15_000) { client.requestResults.first() }
                }
                server.approveRequest(request.id)
                val resolved = resolvedDeferred.await()

                assertEquals(false, resolved.approved)
                assertEquals("Snapshot de ficha inválido.", resolved.reason)
                assertEquals(30, server.receivedCharacters.value[connected.selfId]?.currentHp)
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `aprovacao duplicada de compra debita e concede apenas uma vez`() = runBlocking {
        val server = SessionServer(sessionId = "sess-duplicate-approval", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val original = CharacterEntity(
                id = 1701L,
                name = "Legolas",
                characterClass = "Patrulheiro",
                level = 5,
                currentHp = 30,
                maxHp = 40,
                goldGp = 1000f
            )
            val originalJson = CharacterBackup.toJson(listOf(original)) { null }
            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Legolas", originalJson, original.id)
                val connected = withTimeout(15_000) {
                    client.state.first { it is SessionClientState.Connected } as SessionClientState.Connected
                }
                withTimeout(15_000) { server.receivedCharacters.first { it.containsKey(connected.selfId) } }

                val shopItem = CharacterInventoryItem(
                    id = "unique-shop-item",
                    name = "Poção",
                    category = "Itens de Aventura",
                    quantity = 1,
                    costGp = 100f
                )
                server.openShop(1L, "Loja", listOf(shopItem))
                client.requestAction(
                    RequestKind.BUY_FROM_SHOP,
                    "Compra de poção",
                    shopItemId = "unique-shop-item"
                )
                val request = withTimeout(15_000) {
                    server.pendingRequests.first { it.any { pending -> pending.kind == RequestKind.BUY_FROM_SHOP } }
                }.single { it.kind == RequestKind.BUY_FROM_SHOP }

                val grantDeferred = async(start = CoroutineStart.UNDISPATCHED) {
                    withTimeout(15_000) { client.shopPurchases.first() }
                }
                val approvals = listOf(
                    async(Dispatchers.Default) { server.approveRequest(request.id) },
                    async(Dispatchers.Default) { server.approveRequest(request.id) }
                )
                approvals.awaitAll()
                val grant = grantDeferred.await()

                val stored = server.receivedCharacters.value.getValue(connected.selfId)
                // O servidor normaliza o saldo em platina: 1.000 PO - 100 PO = 90 PP.
                assertEquals(0f, stored.goldGp)
                assertEquals(90f, stored.platinumPp)
                assertTrue(server.shop.value?.items?.isEmpty() == true)
                assertEquals(request.requesterId, connected.selfId)
                assertEquals("Poção", grant.item.name)
                assertEquals(90f, grant.coinPurse?.platinumPp)
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `snapshot enviado na reconexao nao sobrescreve ficha ja recebida pelo Mestre`() = runBlocking {
        val server = SessionServer(sessionId = "sess-reconnect-sheet-authority", hostDisplayName = "Mestre")
        val port = server.start()
        var firstSocket: Socket? = null
        var reconnectSocket: Socket? = null
        try {
            val original = CharacterEntity(
                id = 1801L,
                name = "Legolas",
                characterClass = "Patrulheiro",
                level = 5,
                currentHp = 30,
                maxHp = 40
            )
            val originalJson = CharacterBackup.toJson(listOf(original)) { null }
            val alteredJson = CharacterBackup.toJson(listOf(original.copy(currentHp = 5))) { null }

            firstSocket = Socket("127.0.0.1", port)
            val firstInput = DataInputStream(firstSocket.getInputStream())
            val firstOutput = DataOutputStream(firstSocket.getOutputStream())
            SessionWire.write(firstOutput, SessionMessage.Hello("Legolas", characterId = original.id))
            val firstWelcome = withContext(Dispatchers.IO) {
                withTimeout(15_000) { SessionWire.read(firstInput) as SessionMessage.Welcome }
            }
            SessionWire.write(firstOutput, SessionMessage.CharacterSheet(originalJson))
            withTimeout(15_000) {
                server.receivedCharacters.first { it[firstWelcome.participantId]?.currentHp == 30 }
            }
            firstSocket.close()
            withTimeout(15_000) {
                server.roster.first { roster -> roster.any { it.id == firstWelcome.participantId && !it.connected } }
            }

            reconnectSocket = Socket("127.0.0.1", port)
            val reconnectInput = DataInputStream(reconnectSocket.getInputStream())
            val reconnectOutput = DataOutputStream(reconnectSocket.getOutputStream())
            SessionWire.write(
                reconnectOutput,
                SessionMessage.Hello("Legolas", firstWelcome.rejoinToken, original.id)
            )
            val reconnectWelcome = withContext(Dispatchers.IO) {
                withTimeout(15_000) { SessionWire.read(reconnectInput) as SessionMessage.Welcome }
            }
            assertEquals(firstWelcome.participantId, reconnectWelcome.participantId)

            // Simula um cliente alterado/adulterado: a reconexão repete CharacterSheet com PV
            // diferentes. A mensagem privada seguinte confirma que o servidor já leu o snapshot.
            SessionWire.write(reconnectOutput, SessionMessage.CharacterSheet(alteredJson))
            SessionWire.write(reconnectOutput, SessionMessage.PrivateMessage("reconnect-snapshot-sent"))
            withTimeout(15_000) {
                server.privateChatLog.first { entries -> entries.any { it.text == "reconnect-snapshot-sent" } }
            }

            val stored = server.receivedCharacters.value.getValue(firstWelcome.participantId)
            assertEquals(30, stored.currentHp)
            assertEquals(2, server.roster.value.size)
        } finally {
            runCatching { firstSocket?.close() }
            runCatching { reconnectSocket?.close() }
            server.stop()
        }
    }

    @Test
    fun `pedido de compra nao aplica snapshot do jogador antes da aprovacao`() = runBlocking {
        val server = SessionServer(sessionId = "sess-resource-snapshot", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val original = CharacterEntity(
                name = "Legolas",
                characterClass = "Patrulheiro",
                level = 5,
                currentHp = 30,
                maxHp = 40,
                goldGp = 1000f
            )
            val originalJson = CharacterBackup.toJson(listOf(original)) { null }
            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Legolas", originalJson)
                val connected = withTimeout(15_000) {
                    client.state.first { it is SessionClientState.Connected } as SessionClientState.Connected
                }
                withTimeout(15_000) { server.receivedCharacters.first { it.containsKey(connected.selfId) } }

                val unapprovedSnapshot = CharacterBackup.toJson(
                    listOf(original.copy(currentHp = 5, goldGp = 1f))
                ) { null }
                client.requestAction(
                    RequestKind.BUY_FROM_SHOP,
                    "Pedido de compra",
                    characterJson = unapprovedSnapshot,
                    shopItemId = "item-pending"
                )

                withTimeout(15_000) {
                    server.pendingRequests.first { requests -> requests.any { it.kind == RequestKind.BUY_FROM_SHOP } }
                }
                val stored = server.receivedCharacters.value.getValue(connected.selfId)
                assertEquals(30, stored.currentHp)
                assertEquals(1000f, stored.goldGp)
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `pedido negado de atualizacao de ficha preserva snapshot anterior`() = runBlocking {
        val server = SessionServer(sessionId = "sess-character-update-denied", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val original = CharacterEntity(name = "Legolas", characterClass = "Patrulheiro", level = 5, currentHp = 30, maxHp = 40)
            val originalJson = CharacterBackup.toJson(listOf(original)) { null }
            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Legolas", originalJson)
                val connected = withTimeout(15_000) {
                    client.state.first { it is SessionClientState.Connected } as SessionClientState.Connected
                }
                withTimeout(15_000) { server.receivedCharacters.first { it.containsKey(connected.selfId) } }

                val updatedJson = CharacterBackup.toJson(listOf(original.copy(currentHp = 5))) { null }
                client.requestAction(
                    RequestKind.CHARACTER_UPDATE,
                    "Atualização da ficha de Legolas",
                    characterJson = updatedJson
                )
                val request = withTimeout(15_000) {
                    server.pendingRequests.first { it.isNotEmpty() }
                }.single()

                val resolvedDeferred = async(start = CoroutineStart.UNDISPATCHED) {
                    withTimeout(15_000) { client.requestResults.first() }
                }
                server.denyRequest(request.id, reason = "Atualização não autorizada")
                val resolved = resolvedDeferred.await()

                assertEquals(false, resolved.approved)
                assertEquals("Atualização não autorizada", resolved.reason)
                assertEquals(30, server.receivedCharacters.value[connected.selfId]?.currentHp)
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `addLootItem e removeLootItem sincronizam o bau com o jogador conectado`() = runBlocking {
        val server = SessionServer(sessionId = "sess-11", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Legolas")
                withTimeout(15_000) { client.state.first { it is SessionClientState.Connected } }

                val item = CharacterInventoryItem(name = "Poção de cura", category = "Itens de Aventura", quantity = 3)
                server.addLootItem(item)

                val chestAfterAdd = withTimeout(15_000) {
                    client.state.mapNotNull { (it as? SessionClientState.Connected)?.lootChest }.first { it.isNotEmpty() }
                }
                assertEquals(listOf(item), chestAfterAdd)

                server.removeLootItem(item.id)
                val chestAfterRemove = withTimeout(15_000) {
                    client.state.mapNotNull { (it as? SessionClientState.Connected)?.lootChest }.first { it.isEmpty() }
                }
                assertTrue(chestAfterRemove.isEmpty())
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `jogador que entra com o bau ja populado recebe o conteudo atual`() = runBlocking {
        val server = SessionServer(sessionId = "sess-12", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val item = CharacterInventoryItem(name = "Adaga", category = "Armas", quantity = 1)
            server.addLootItem(item)

            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Retardatário")
                val chest = withTimeout(15_000) {
                    client.state.mapNotNull { (it as? SessionClientState.Connected)?.lootChest }.first { it.isNotEmpty() }
                }
                assertEquals(listOf(item), chest)
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `pedido de pegar loot aprovado remove do bau e concede o item ao jogador`() = runBlocking {
        val server = SessionServer(sessionId = "sess-13", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val item = CharacterInventoryItem(name = "Elmo élfico", category = "Armaduras", quantity = 1)
            server.addLootItem(item)

            val character = CharacterEntity(id = 1301L, name = "Legolas", characterClass = "Patrulheiro", level = 5)
            val characterJson = CharacterBackup.toJson(listOf(character)) { null }
            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Legolas", characterJson, character.id)
                withTimeout(15_000) {
                    client.state.first { it is SessionClientState.Connected }
                    client.state.mapNotNull { (it as? SessionClientState.Connected)?.lootChest }.first { it.isNotEmpty() }
                }

                client.requestAction(
                    RequestKind.TAKE_LOOT,
                    "Pedido de pegar \"Elmo élfico\"",
                    characterJson = characterJson,
                    lootItemId = item.id
                )
                val request = withTimeout(15_000) { server.pendingRequests.first { it.isNotEmpty() } }.single()
                assertEquals(RequestKind.TAKE_LOOT, request.kind)
                assertEquals(item.id, request.lootItemId)

                // LootGranted e RequestResolved são mandados em sequência, quase simultâneos —
                // os dois coletores precisam já estar inscritos antes do Mestre aprovar, senão o
                // laço de leitura do cliente (que processa as duas mensagens de uma vez, sem
                // esperar ninguém) pode emitir e descartar uma delas antes de alguém ouvir.
                // CoroutineStart.UNDISPATCHED garante a inscrição de verdade antes de prosseguir,
                // em vez de contar com a sorte de timing entre threads.
                val grantedDeferred = async(start = CoroutineStart.UNDISPATCHED) { withTimeout(15_000) { client.lootGranted.first() } }
                val resolvedDeferred = async(start = CoroutineStart.UNDISPATCHED) { withTimeout(15_000) { client.requestResults.first() } }

                server.approveRequest(request.id)

                val granted = grantedDeferred.await()
                assertEquals(item.copy(id = granted.grantId), granted.item)
                assertEquals(character.id, granted.characterId)
                assertTrue(granted.grantId.isNotBlank())

                val resolved = resolvedDeferred.await()
                assertTrue(resolved.approved)

                assertTrue(server.lootChest.value.isEmpty())
                val chestAfter = withTimeout(15_000) {
                    client.state.mapNotNull { (it as? SessionClientState.Connected)?.lootChest }.first { it.isEmpty() }
                }
                assertTrue(chestAfter.isEmpty())
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `pedido de pegar loot de item ja removido e negado automaticamente`() = runBlocking {
        val server = SessionServer(sessionId = "sess-14", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val item = CharacterInventoryItem(name = "Corda (15m)", category = "Itens de Aventura", quantity = 1)
            server.addLootItem(item)

            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Gimli")
                withTimeout(15_000) {
                    client.state.mapNotNull { (it as? SessionClientState.Connected)?.lootChest }.first { it.isNotEmpty() }
                }

                client.requestAction(RequestKind.TAKE_LOOT, "Pedido de pegar \"Corda (15m)\"", lootItemId = item.id)
                val request = withTimeout(15_000) { server.pendingRequests.first { it.isNotEmpty() } }.single()

                // O Mestre removeu o item direto do baú antes de decidir o pedido.
                server.removeLootItem(item.id)

                val resolvedDeferred = async(start = CoroutineStart.UNDISPATCHED) {
                    withTimeout(15_000) { client.requestResults.first() }
                }
                server.approveRequest(request.id)

                val resolved = resolvedDeferred.await()
                assertEquals(false, resolved.approved)
                assertTrue(resolved.reason.isNotBlank())
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `openShop e closeShop sincronizam a loja com o jogador conectado`() = runBlocking {
        val server = SessionServer(sessionId = "sess-15", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Legolas")
                withTimeout(15_000) { client.state.first { it is SessionClientState.Connected } }

                val item = CharacterInventoryItem(name = "Poção de cura", category = "Itens de Aventura", quantity = 5, costGp = 50f)
                server.openShop(99L, "Vendedor Gundren", listOf(item))

                val shopAfterOpen = withTimeout(15_000) {
                    client.state.mapNotNull { (it as? SessionClientState.Connected)?.shop }.first()
                }
                assertEquals("Vendedor Gundren", shopAfterOpen.npcName)
                assertEquals(listOf(item), shopAfterOpen.items)

                server.closeShop()
                val stateAfterClose = withTimeout(15_000) {
                    client.state.mapNotNull { it as? SessionClientState.Connected }.first { it.shop == null }
                }
                assertEquals(null, stateAfterClose.shop)
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `jogador que entra com a loja ja aberta recebe o estoque atual`() = runBlocking {
        val server = SessionServer(sessionId = "sess-16", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val item = CharacterInventoryItem(name = "Adaga +1", category = "Armas", quantity = 1, costGp = 320f)
            server.openShop(42L, "Ferreiro Baldrik", listOf(item))

            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Retardatário")
                val shop = withTimeout(15_000) {
                    client.state.mapNotNull { (it as? SessionClientState.Connected)?.shop }.first()
                }
                assertEquals("Ferreiro Baldrik", shop.npcName)
                assertEquals(listOf(item), shop.items)
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `pedido de comprar da loja aprovado remove do estoque e concede o item ao jogador`() = runBlocking {
        val server = SessionServer(sessionId = "sess-17", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val item = CharacterInventoryItem(name = "Elmo élfico", category = "Armaduras", quantity = 1, costGp = 500f)
            server.openShop(7L, "Vendedor Gundren", listOf(item))

            val character = CharacterEntity(
                id = 1701L,
                name = "Legolas",
                characterClass = "Patrulheiro",
                level = 5,
                goldGp = 1000f
            )
            val characterJson = CharacterBackup.toJson(listOf(character)) { null }
            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Legolas", characterJson, character.id)
                withTimeout(15_000) {
                    client.state.first { it is SessionClientState.Connected }
                    client.state.mapNotNull { (it as? SessionClientState.Connected)?.shop }.first()
                }

                client.requestAction(
                    RequestKind.BUY_FROM_SHOP,
                    "Pedido de comprar \"Elmo élfico\"",
                    characterJson = characterJson,
                    shopItemId = item.id
                )
                val request = withTimeout(15_000) { server.pendingRequests.first { it.isNotEmpty() } }.single()
                assertEquals(RequestKind.BUY_FROM_SHOP, request.kind)
                assertEquals(item.id, request.shopItemId)

                // Mesmo cuidado documentado no teste de loot: ShopPurchaseGranted e RequestResolved
                // chegam quase juntos de uma única chamada a approveRequest, então os dois
                // coletores precisam estar inscritos antes de disparar a aprovação (UNDISPATCHED
                // garante isso de verdade, em vez de contar com a sorte de timing entre threads).
                val grantedDeferred = async(start = CoroutineStart.UNDISPATCHED) { withTimeout(15_000) { client.shopPurchases.first() } }
                val resolvedDeferred = async(start = CoroutineStart.UNDISPATCHED) { withTimeout(15_000) { client.requestResults.first() } }

                server.approveRequest(request.id)

                val granted = grantedDeferred.await()
                assertEquals(item.copy(id = granted.grantId), granted.item)
                assertEquals(character.id, granted.characterId)
                assertEquals(0f, granted.coinPurse?.goldGp)
                assertEquals(50f, granted.coinPurse?.platinumPp)

                val resolved = resolvedDeferred.await()
                assertTrue(resolved.approved)

                assertTrue(server.shop.value?.items?.isEmpty() ?: false)
                val stateAfter = withTimeout(15_000) {
                    client.state.mapNotNull { it as? SessionClientState.Connected }.first { it.shop?.items?.isEmpty() == true }
                }
                assertTrue(stateAfter.shop!!.items.isEmpty())
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `pedido de comprar item ja removido da loja e negado automaticamente`() = runBlocking {
        val server = SessionServer(sessionId = "sess-18", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val item = CharacterInventoryItem(name = "Corda (15m)", category = "Itens de Aventura", quantity = 1, costGp = 10f)
            server.openShop(3L, "Vendedor Gundren", listOf(item))

            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Gimli")
                withTimeout(15_000) {
                    client.state.mapNotNull { (it as? SessionClientState.Connected)?.shop }.first()
                }

                client.requestAction(RequestKind.BUY_FROM_SHOP, "Pedido de comprar \"Corda (15m)\"", shopItemId = item.id)
                val request = withTimeout(15_000) { server.pendingRequests.first { it.isNotEmpty() } }.single()

                // O Mestre fechou a loja antes de decidir o pedido.
                server.closeShop()

                val resolvedDeferred = async(start = CoroutineStart.UNDISPATCHED) {
                    withTimeout(15_000) { client.requestResults.first() }
                }
                server.approveRequest(request.id)

                val resolved = resolvedDeferred.await()
                assertEquals(false, resolved.approved)
                assertTrue(resolved.reason.isNotBlank())
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `mensagem privada do jogador ao Mestre aparece no log do servidor`() = runBlocking {
        val server = SessionServer(sessionId = "sess-19", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Legolas")
                withTimeout(15_000) { client.state.first { it is SessionClientState.Connected } }

                client.sendPrivateMessage("Mestre, posso falar com você a sós?")

                val log = withTimeout(15_000) { server.privateChatLog.first { it.isNotEmpty() } }
                val entry = log.single()
                assertEquals("Legolas", entry.participantName)
                assertEquals("Mestre, posso falar com você a sós?", entry.text)
                assertEquals(false, entry.fromDm)
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `mensagem privada do Mestre chega so ao jogador destinatario`() = runBlocking {
        val server = SessionServer(sessionId = "sess-20", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val targetClient = SessionClient()
            val otherClient = SessionClient()
            try {
                targetClient.connect("127.0.0.1", port, "Legolas")
                val targetConnected = withTimeout(15_000) {
                    targetClient.state.first { it is SessionClientState.Connected } as SessionClientState.Connected
                }
                // sendPrivateMessage busca o nome do jogador no roster do servidor — esperar o
                // cliente ficar "Connected" não garante que o servidor já processou addParticipant
                // (mesma corrida documentada no primeiro teste da classe), então espera o roster
                // também antes de mandar a mensagem.
                withTimeout(15_000) { server.roster.first { roster -> roster.any { it.id == targetConnected.selfId } } }
                otherClient.connect("127.0.0.1", port, "Gimli")
                withTimeout(15_000) { otherClient.state.first { it is SessionClientState.Connected } }

                // CoroutineStart.UNDISPATCHED: diferente do padrão usado para duas mensagens em
                // sequência (ver os testes de loot/loja acima), aqui existe só uma emissão — então
                // em vez de confiar na sorte de timing entre o laço de leitura do cliente (outra
                // thread, socket real) e o agendamento deste coletor no event loop do runBlocking,
                // força a inscrição no SharedFlow a acontecer de verdade antes de mandar a mensagem.
                val received = async(start = CoroutineStart.UNDISPATCHED) {
                    withTimeout(15_000) { targetClient.privateMessages.first() }
                }
                server.sendPrivateMessage(targetConnected.selfId, "Psiu, o baú ali tem uma armadilha.")

                assertEquals("Psiu, o baú ali tem uma armadilha.", received.await())

                val log = withTimeout(15_000) { server.privateChatLog.first { it.isNotEmpty() } }
                assertEquals(true, log.single().fromDm)
            } finally {
                targetClient.disconnect()
                otherClient.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `reconectar com o token do WELCOME recupera o mesmo id de participante`() = runBlocking {
        val server = SessionServer(sessionId = "sess-21", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Legolas")
                val connected = withTimeout(15_000) {
                    client.state.first { it is SessionClientState.Connected } as SessionClientState.Connected
                }
                withTimeout(15_000) { server.roster.first { it.size == 2 } }

                // Simula uma queda de Wi-Fi (não um client.disconnect() voluntário, que encerraria
                // de vez o cliente e impediria o reconnect() abaixo): o socket cai sem BYE.
                server.disconnectParticipant(connected.selfId)
                withTimeout(15_000) {
                    server.roster.first { roster -> roster.any { it.id == connected.selfId && !it.connected } }
                }

                client.reconnect()
                val reconnected = withTimeout(15_000) {
                    client.state.first { it is SessionClientState.Connected } as SessionClientState.Connected
                }
                assertEquals(connected.selfId, reconnected.selfId)

                val rosterAfterReconnect = withTimeout(15_000) {
                    server.roster.first { roster -> roster.any { it.id == connected.selfId && it.connected } }
                }
                // Continua sendo só Mestre + Legolas — não duplicou a entrada no roster.
                assertEquals(2, rosterAfterReconnect.size)
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `reconectar preserva a ficha ja recebida pelo Mestre`() = runBlocking {
        val server = SessionServer(sessionId = "sess-22", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val character = CharacterEntity(name = "Legolas", characterClass = "Patrulheiro", level = 5)
            val characterJson = CharacterBackup.toJson(listOf(character)) { null }

            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Legolas", characterJson)
                val connected = withTimeout(15_000) {
                    client.state.first { it is SessionClientState.Connected } as SessionClientState.Connected
                }
                withTimeout(15_000) { server.receivedCharacters.first { it.containsKey(connected.selfId) } }

                server.disconnectParticipant(connected.selfId)
                withTimeout(15_000) {
                    server.roster.first { roster -> roster.any { it.id == connected.selfId && !it.connected } }
                }

                // O Mestre continua vendo a última ficha que recebeu, mesmo com o jogador offline.
                assertEquals("Legolas", server.receivedCharacters.value[connected.selfId]?.name)

                client.reconnect()
                withTimeout(15_000) { client.state.first { it is SessionClientState.Connected } }
                withTimeout(15_000) {
                    server.roster.first { roster -> roster.any { it.id == connected.selfId && it.connected } }
                }
                assertEquals("Legolas", server.receivedCharacters.value[connected.selfId]?.name)
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `reconexao completa ficha quando primeira entrada caiu antes do snapshot`() = runBlocking {
        val server = SessionServer(sessionId = "sess-23a", hostDisplayName = "Mestre")
        val port = server.start()
        var firstSocket: Socket? = null
        var secondSocket: Socket? = null
        try {
            val character = CharacterEntity(
                id = 2301L,
                name = "Legolas",
                characterClass = "Patrulheiro",
                level = 5
            )
            val characterJson = CharacterBackup.toJson(listOf(character)) { null }

            // Primeira conexão recebe o WELCOME, mas cai antes de enviar CharacterSheet.
            firstSocket = Socket("127.0.0.1", port).apply { soTimeout = 15_000 }
            val firstInput = DataInputStream(firstSocket.getInputStream())
            val firstOutput = DataOutputStream(firstSocket.getOutputStream())
            SessionWire.write(firstOutput, SessionMessage.Hello("Legolas", characterId = character.id))
            val firstWelcome = withContext(Dispatchers.IO) {
                SessionWire.read(firstInput) as SessionMessage.Welcome
            }
            withTimeout(15_000) {
                server.roster.first { roster -> roster.any { it.id == firstWelcome.participantId } }
            }
            assertEquals(null, server.receivedCharacters.value[firstWelcome.participantId])
            firstSocket.close()
            withTimeout(15_000) {
                server.roster.first { roster -> roster.any { it.id == firstWelcome.participantId && !it.connected } }
            }

            // A reconexão usa o mesmo token e finalmente envia a primeira ficha.
            secondSocket = Socket("127.0.0.1", port).apply { soTimeout = 15_000 }
            val secondInput = DataInputStream(secondSocket.getInputStream())
            val secondOutput = DataOutputStream(secondSocket.getOutputStream())
            SessionWire.write(
                secondOutput,
                SessionMessage.Hello("Legolas", firstWelcome.rejoinToken, character.id)
            )
            val secondWelcome = withContext(Dispatchers.IO) {
                SessionWire.read(secondInput) as SessionMessage.Welcome
            }
            assertEquals(firstWelcome.participantId, secondWelcome.participantId)
            SessionWire.write(secondOutput, SessionMessage.CharacterSheet(characterJson))

            val received = withTimeout(15_000) {
                server.receivedCharacters.first { it.containsKey(firstWelcome.participantId) }
            }
            assertEquals("Legolas", received[firstWelcome.participantId]?.name)
            assertEquals(character.id, server.roster.value.first { it.id == firstWelcome.participantId }.characterId)
        } finally {
            runCatching { firstSocket?.close() }
            runCatching { secondSocket?.close() }
            server.stop()
        }
    }

    @Test
    fun `reconnect sem ter conectado antes nao faz nada`() = runBlocking {
        val client = SessionClient()
        assertEquals(false, client.reconnect())
    }

    @Test
    fun `reconexao rapida nao deixa conexao antiga derrubar a nova por engano`() = runBlocking {
        val server = SessionServer(sessionId = "sess-24", hostDisplayName = "Mestre")
        val port = server.start()
        try {
            val client = SessionClient()
            try {
                client.connect("127.0.0.1", port, "Legolas")
                val connected = withTimeout(15_000) {
                    client.state.first { it is SessionClientState.Connected } as SessionClientState.Connected
                }
                withTimeout(15_000) { server.roster.first { it.size == 2 } }

                // Reconecta sem esperar o servidor perceber a queda da conexão antiga — o socket
                // antigo só vai ser fechado quando o novo registrar o mesmo id (ver handleClient).
                client.reconnect()
                val reconnected = withTimeout(15_000) {
                    client.state.first { it is SessionClientState.Connected } as SessionClientState.Connected
                }
                assertEquals(connected.selfId, reconnected.selfId)

                // Mesmo que a conexão antiga perceba a própria queda com atraso, ela não pode
                // marcar esse mesmo id como desconectado agora que já existe uma reconexão válida.
                val rosterAfter = withTimeout(15_000) {
                    server.roster.first { roster -> roster.any { it.id == connected.selfId && it.connected } }
                }
                assertEquals(2, rosterAfter.size)
            } finally {
                client.disconnect()
            }
        } finally {
            server.stop()
        }
    }

    @Test
    fun `mensagens privadas simultaneas nao perdem entradas no log do mestre`() = runBlocking {
        val server = SessionServer(sessionId = "sess-private-chat-concurrency", hostDisplayName = "Mestre")
        val port = server.start()
        val clients = List(6) { SessionClient() }
        val messagesPerClient = 40
        val expectedMessages = clients.size * messagesPerClient
        try {
            clients.forEachIndexed { index, client ->
                client.connect("127.0.0.1", port, "Jogador $index")
            }
            clients.forEach { client ->
                withTimeout(15_000) {
                    client.state.first { it is SessionClientState.Connected }
                }
            }

            // Mantém concorrência real entre conexões, mas aguarda cada lote antes do próximo.
            // O método público de envio é fire-and-forget; disparar 240 corrotinas de uma vez
            // tornava o teste dependente do agendamento do dispatcher, não apenas do lock do servidor.
            repeat(messagesPerClient) { messageIndex ->
                clients.forEachIndexed { clientIndex, client ->
                    client.sendPrivateMessage("mensagem-$clientIndex-$messageIndex")
                }
                val expectedSoFar = (messageIndex + 1) * clients.size
                withTimeout(30_000) {
                    server.privateChatLog.first { it.size >= expectedSoFar }
                }
            }

            val entries = server.privateChatLog.value
            assertEquals(expectedMessages, entries.size)
            assertEquals(expectedMessages, entries.map { it.text }.toSet().size)
        } finally {
            clients.forEach { it.disconnect() }
            server.stop()
        }
    }

    @Test
    fun `conectar em porta sem servidor resulta em Disconnected, nao em excecao`() = runBlocking {
        val client = SessionClient()
        // Porta 1 é privilegiada/sem listener nos ambientes de teste: a conexão deve falhar
        // de forma controlada, nunca lançar uma exceção para fora do SessionClient.
        client.connect("127.0.0.1", 1, "Jogador")
        val result = withTimeout(15_000) {
            client.state.first { it is SessionClientState.Disconnected }
        }
        assertTrue((result as SessionClientState.Disconnected).reason.isNotBlank())
    }

    @Test
    fun `grant pendente e reenviado ate ACK e removido depois da confirmacao`() = runBlocking {
        val server = SessionServer(sessionId = "sess-grant-replay-ack", hostDisplayName = "Mestre")
        val port = server.start()
        var firstSocket: Socket? = null
        var secondSocket: Socket? = null
        var thirdSocket: Socket? = null

        suspend fun readGrant(input: DataInputStream): SessionMessage.LootGranted =
            withContext(Dispatchers.IO) {
                while (true) {
                    val message = SessionWire.read(input) ?: throw AssertionError("Conexão encerrada antes da concessão.")
                    if (message is SessionMessage.LootGranted) return@withContext message
                }
                throw AssertionError("Grant não recebido.")
            }

        try {
            val character = CharacterEntity(
                id = 1901L,
                name = "Legolas",
                characterClass = "Patrulheiro",
                level = 5
            )
            val characterJson = CharacterBackup.toJson(listOf(character)) { null }
            val item = CharacterInventoryItem(name = "Anel de proteção", category = "Itens Maravilhosos", quantity = 1)
            server.addLootItem(item)

            firstSocket = Socket("127.0.0.1", port).apply { soTimeout = 15_000 }
            val firstInput = DataInputStream(firstSocket.getInputStream())
            val firstOutput = DataOutputStream(firstSocket.getOutputStream())
            SessionWire.write(firstOutput, SessionMessage.Hello("Legolas", characterId = character.id))
            val firstWelcome = withContext(Dispatchers.IO) {
                SessionWire.read(firstInput) as SessionMessage.Welcome
            }
            SessionWire.write(firstOutput, SessionMessage.CharacterSheet(characterJson))
            withTimeout(15_000) {
                server.receivedCharacters.first { it.containsKey(firstWelcome.participantId) }
            }

            SessionWire.write(
                firstOutput,
                SessionMessage.ActionRequested(
                    kind = RequestKind.TAKE_LOOT,
                    summary = "Pegar Anel de proteção",
                    lootItemId = item.id
                )
            )
            val request = withTimeout(15_000) {
                server.pendingRequests.first { requests -> requests.any { it.kind == RequestKind.TAKE_LOOT } }
            }.single { it.kind == RequestKind.TAKE_LOOT }

            server.approveRequest(request.id)
            val originalGrant = readGrant(firstInput)
            assertEquals(character.id, originalGrant.characterId)
            assertTrue(originalGrant.grantId.isNotBlank())
            firstSocket.close()
            withTimeout(15_000) {
                server.roster.first { roster -> roster.any { it.id == firstWelcome.participantId && !it.connected } }
            }

            // Sem ACK, a reconexão com o mesmo token deve receber o mesmo grant novamente.
            secondSocket = Socket("127.0.0.1", port).apply { soTimeout = 15_000 }
            val secondInput = DataInputStream(secondSocket.getInputStream())
            val secondOutput = DataOutputStream(secondSocket.getOutputStream())
            SessionWire.write(
                secondOutput,
                SessionMessage.Hello("Legolas", firstWelcome.rejoinToken, character.id)
            )
            val secondWelcome = withContext(Dispatchers.IO) {
                SessionWire.read(secondInput) as SessionMessage.Welcome
            }
            assertEquals(firstWelcome.participantId, secondWelcome.participantId)
            val replayedGrant = readGrant(secondInput)
            assertEquals(originalGrant.grantId, replayedGrant.grantId)
            assertEquals(originalGrant.itemJson, replayedGrant.itemJson)

            // A mensagem privada seguinte serve como barreira: quando ela chega ao servidor,
            // o ACK anterior já foi lido e processado na mesma conexão TCP.
            SessionWire.write(secondOutput, SessionMessage.GrantAcknowledged(originalGrant.grantId))
            SessionWire.write(secondOutput, SessionMessage.PrivateMessage("grant-ack-processed"))
            withTimeout(15_000) {
                server.privateChatLog.first { entries -> entries.any { it.text == "grant-ack-processed" } }
            }
            secondSocket.close()
            withTimeout(15_000) {
                server.roster.first { roster -> roster.any { it.id == firstWelcome.participantId && !it.connected } }
            }

            // Após ACK, uma nova reconexão não deve receber novamente a concessão confirmada.
            thirdSocket = Socket("127.0.0.1", port).apply { soTimeout = 1_000 }
            val thirdInput = DataInputStream(thirdSocket.getInputStream())
            val thirdOutput = DataOutputStream(thirdSocket.getOutputStream())
            SessionWire.write(
                thirdOutput,
                SessionMessage.Hello("Legolas", firstWelcome.rejoinToken, character.id)
            )
            val thirdWelcome = withContext(Dispatchers.IO) {
                SessionWire.read(thirdInput) as SessionMessage.Welcome
            }
            assertEquals(firstWelcome.participantId, thirdWelcome.participantId)

            val unexpectedGrant = withContext(Dispatchers.IO) {
                try {
                    while (true) {
                        val message = SessionWire.read(thirdInput) ?: break
                        if (message is SessionMessage.LootGranted) return@withContext message
                    }
                    null
                } catch (_: java.net.SocketTimeoutException) {
                    null
                }
            }
            assertEquals(null, unexpectedGrant)
        } finally {
            runCatching { firstSocket?.close() }
            runCatching { secondSocket?.close() }
            runCatching { thirdSocket?.close() }
            server.stop()
        }
    }


    @Test
    fun `adicoes concorrentes ao bau nao perdem itens`() = runBlocking {
        val server = SessionServer(sessionId = "sess-loot-concurrency", hostDisplayName = "Mestre")
        try {
            val items = (1..200).map { index ->
                CharacterInventoryItem(
                    id = "loot-$index",
                    name = "Item $index",
                    category = "Equipamento",
                    quantity = 1
                )
            }

            items.map { item ->
                async(Dispatchers.Default) { server.addLootItem(item) }
            }.awaitAll()

            assertEquals(items.map { it.id }.toSet(), server.lootChest.value.map { it.id }.toSet())
            assertEquals(items.size, server.lootChest.value.size)
        } finally {
            server.stop()
        }
    }


}
