package com.example.dnd35.ui.session

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.CharacterInventoryItem
import com.example.dnd35.session.ActionRequest
import com.example.dnd35.session.Combatant
import com.example.dnd35.session.CombatState
import com.example.dnd35.session.JoinPayload
import com.example.dnd35.session.Participant
import com.example.dnd35.session.ParticipantRole
import com.example.dnd35.session.PrivateChatEntry
import com.example.dnd35.session.ShopSnapshot
import com.example.dnd35.session.ShopState
import com.example.dnd35.model.CharacterJsonAdapters
import com.example.dnd35.viewmodel.CharacterViewModel
import com.example.dnd35.viewmodel.DmSessionUiState
import com.example.dnd35.viewmodel.PlayerSessionUiState
import com.example.dnd35.viewmodel.SessionViewModel
import com.example.ui.theme.DndCrimson
import com.example.ui.theme.DndGold
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

private enum class SessionHubMode { SELECT, DM, PLAYER }
private enum class SessionAreaTab { SESSION, COMBAT, RESOURCES }
private enum class SessionFloatingPanel { COMBAT, LOOT, SHOP }

/**
 * Tela de "Sessão de Mesa": ponto de entrada para o modo Mestre (hospedar) ou Jogador (entrar),
 * totalmente offline — a única rede usada é a que os aparelhos já compartilham localmente.
 */
@Composable
fun SessionHubScreen(viewModel: SessionViewModel, characterViewModel: CharacterViewModel, onClose: () -> Unit) {
    var mode by remember { mutableStateOf(SessionHubMode.SELECT) }
    var selectedArea by remember { mutableStateOf(SessionAreaTab.SESSION) }
    val dmState by viewModel.dmState.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val activeSessionMode = when {
        dmState is DmSessionUiState.Hosting -> SessionHubMode.DM
        playerState is PlayerSessionUiState.Connected ||
            playerState is PlayerSessionUiState.Connecting ||
            playerState is PlayerSessionUiState.Disconnected -> SessionHubMode.PLAYER
        else -> null
    }
    val activeSessionLabel = when (activeSessionMode) {
        SessionHubMode.DM -> "Você já está hospedando uma sessão."
        SessionHubMode.PLAYER -> when (playerState) {
            is PlayerSessionUiState.Connected -> "Você já está conectado a uma sessão."
            is PlayerSessionUiState.Connecting -> "Sua conexão com a sessão está em andamento."
            else -> "Sua sessão anterior foi desconectada e pode ser retomada."
        }
        else -> null
    }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                when (mode) {
                    SessionHubMode.SELECT -> onClose()
                    else -> mode = SessionHubMode.SELECT
                }
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Sessão de Mesa (offline)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        when (mode) {
            SessionHubMode.SELECT -> ModeSelectContent(
                activeSessionLabel = activeSessionLabel,
                onReturnToSession = { mode = activeSessionMode ?: SessionHubMode.SELECT },
                onPickDm = { mode = SessionHubMode.DM },
                onPickPlayer = { mode = SessionHubMode.PLAYER }
            )
            SessionHubMode.DM -> DmHostContent(viewModel, characterViewModel, selectedArea) { selectedArea = it }
            SessionHubMode.PLAYER -> PlayerJoinContent(viewModel, characterViewModel, selectedArea) { selectedArea = it }
        }
    }
}


@Composable
private fun SessionAreaNavigation(selected: SessionAreaTab, onSelect: (SessionAreaTab) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        listOf(SessionAreaTab.SESSION to "Sessão", SessionAreaTab.COMBAT to "Combate", SessionAreaTab.RESOURCES to "Recursos").forEach { (tab, label) ->
            TextButton(onClick = { onSelect(tab) }) {
                Text(label, color = if (selected == tab) DndGold else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (selected == tab) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Composable
private fun ModeSelectContent(
    activeSessionLabel: String?,
    onReturnToSession: () -> Unit,
    onPickDm: () -> Unit,
    onPickPlayer: () -> Unit
) {
    if (activeSessionLabel != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(activeSessionLabel, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = onReturnToSession, modifier = Modifier.fillMaxWidth()) {
                    Text("Retornar à sessão existente")
                }
                Text(
                    "A sessão será retomada sem criar outra conexão.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    Text(
        "Nenhum dado sai da rede local: os aparelhos precisam estar na mesma rede Wi-Fi " +
            "(mesmo sem internet) para se conectarem.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(24.dp))

    Card(
        modifier = Modifier.fillMaxWidth().testTag("session_pick_dm"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(Icons.Default.Shield, contentDescription = null, tint = DndGold, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Sou o Mestre", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Cria uma sessão nesta mesa e mostra um QR Code para os jogadores entrarem.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = onPickDm) { Text("Criar sessão") }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Card(
        modifier = Modifier.fillMaxWidth().testTag("session_pick_player"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(Icons.Default.Person, contentDescription = null, tint = DndGold, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Sou Jogador", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Escaneia o QR Code do Mestre (ou digita o endereço dele) para entrar na sessão.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = onPickPlayer) { Text("Entrar em sessão") }
        }
    }
}

@Composable
private fun DmHostContent(viewModel: SessionViewModel, characterViewModel: CharacterViewModel, selectedArea: SessionAreaTab, onSelectArea: (SessionAreaTab) -> Unit) {
    val state by viewModel.dmState.collectAsStateWithLifecycle()
    var hostName by remember { mutableStateOf("Mestre") }
    var showEndConfirmation by remember { mutableStateOf(false) }

    when (val s = state) {
        is DmSessionUiState.Idle -> {
            OutlinedTextField(
                value = hostName,
                onValueChange = { hostName = it },
                label = { Text("Seu nome (aparece para os jogadores)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { viewModel.hostSession(hostName) }, modifier = Modifier.testTag("start_hosting_button")) {
                Icon(Icons.Default.MeetingRoom, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Criar sessão")
            }
            Spacer(modifier = Modifier.height(24.dp))
            NpcManagementSection(characterViewModel)
        }
        is DmSessionUiState.Hosting -> {
            Text("Sessão ativa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            when (selectedArea) {
                SessionAreaTab.SESSION -> {
                    Text("Peça para os jogadores escanearem este código no app deles.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Image(bitmap = s.qrBitmap.asImageBitmap(), contentDescription = "QR Code da sessão", modifier = Modifier.size(220.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    ParticipantRoster(s.participants, s.characters)
                    if (s.pendingRequests.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        PendingRequestsSection(requests = s.pendingRequests, onApprove = { viewModel.approveRequest(it) }, onDeny = { viewModel.denyRequest(it) })
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    DmPrivateChatSection(players = s.participants.filter { it.role == ParticipantRole.PLAYER && it.connected }, chatLog = s.privateChatLog,
                        onSend = { participantId, text -> viewModel.sendPrivateMessageToPlayer(participantId, text) })
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(onClick = { showEndConfirmation = true }) { Text("Encerrar sessão", color = DndCrimson) }
                    Spacer(modifier = Modifier.height(20.dp))
                    NpcManagementSection(characterViewModel)
                }
                SessionAreaTab.COMBAT -> CombatSection(viewModel, characterViewModel, s)
                SessionAreaTab.RESOURCES -> {
                    if (s.pendingRequests.isNotEmpty()) {
                        PendingRequestsSection(
                            requests = s.pendingRequests,
                            onApprove = { viewModel.approveRequest(it) },
                            onDeny = { viewModel.denyRequest(it) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    LootChestSection(items = s.lootChest, isDm = true, onAddItem = { viewModel.addLootItem(it) }, onRemoveItem = { viewModel.removeLootItem(it) })
                    Spacer(modifier = Modifier.height(16.dp))
                    DmShopSection(shop = s.shop, characterViewModel = characterViewModel,
                        onOpenShop = { npcId, npcName, items -> viewModel.openShop(npcId, npcName, items) }, onCloseShop = { viewModel.closeShop() })
                }
            }
        }
        is DmSessionUiState.Error -> {
            Text(s.message, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { viewModel.hostSession(hostName) }) { Text("Tentar novamente") }
        }
    }

    if (showEndConfirmation) {
        AlertDialog(
            onDismissRequest = { showEndConfirmation = false },
            title = { Text("Encerrar sessão?") },
            text = {
                Text("Todos os jogadores serão desconectados. A sessão será marcada como encerrada e não poderá ser retomada.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showEndConfirmation = false
                    viewModel.stopHosting()
                }) { Text("Encerrar") }
            },
            dismissButton = {
                TextButton(onClick = { showEndConfirmation = false }) { Text("Cancelar") }
            }
        )
    }
}

/** Permite ao Mestre marcar suas próprias fichas locais como NPC (monstros/figurantes). */
@Composable
private fun NpcManagementSection(characterViewModel: CharacterViewModel) {
    val myCharacters by characterViewModel.allCharacters.collectAsStateWithLifecycle()
    if (myCharacters.isEmpty()) return

    Column {
        Text(
            "Minhas fichas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Marque como NPC as fichas que são suas (monstros, figurantes) para diferenciar " +
                "das fichas dos jogadores. Isso não afeta a ficha de forma alguma fora da sessão.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        myCharacters.forEach { character ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(character.name, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${character.characterClass} Nv ${character.level}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(if (character.isNpc) "NPC" else "Jogador próprio", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = character.isNpc,
                    onCheckedChange = { checked -> characterViewModel.setNpcFlag(character.id, checked) }
                )
            }
        }
    }
}

/** Candidato a combatente: um jogador conectado (com ou sem ficha) ou uma ficha NPC do Mestre. */
private data class CombatCandidate(
    val id: String,
    val displayName: String,
    val isNpc: Boolean,
    val currentHp: Int?,
    val maxHp: Int?,
    val tempHp: Int = 0
)

/**
 * Seção de combate do Mestre: inicia um combate novo (escolhendo combatentes e iniciativa) ou,
 * se já houver um ativo, mostra a ordem de turnos com controles de PV, já que o Mestre é a
 * autoridade e suas mudanças são aplicadas direto (sem passar por aprovação).
 */
@Composable
private fun CombatSection(
    viewModel: SessionViewModel,
    characterViewModel: CharacterViewModel,
    hosting: DmSessionUiState.Hosting
) {
    val myCharacters by characterViewModel.allCharacters.collectAsStateWithLifecycle()
    var showStartDialog by remember { mutableStateOf(false) }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Shield, contentDescription = null, tint = DndGold, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Combate", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))

        val combat = hosting.combat
        if (combat == null) {
            Button(onClick = { showStartDialog = true }) {
                Text("Iniciar combate")
            }
        } else {
            CombatTracker(
                combat = combat,
                isDm = true,
                onDamage = { id, amount -> viewModel.applyCombatDamage(id, amount) },
                onNextTurn = { viewModel.nextCombatTurn() },
                onEndCombat = { viewModel.endCombat() }
            )
        }
    }

    if (showStartDialog) {
        val players = hosting.participants.filter { it.role == ParticipantRole.PLAYER && it.connected }
        val playerCandidates = players.map { p ->
            val sheet = hosting.characters[p.id]
            CombatCandidate(
                id = p.id,
                displayName = p.displayName,
                isNpc = false,
                currentHp = sheet?.currentHp,
                maxHp = sheet?.maxHp,
                tempHp = sheet?.tempHp ?: 0
            )
        }
        val npcCandidates = myCharacters.filter { it.isNpc }.map { c ->
            CombatCandidate(
                id = CombatState.npcCombatantId(c.id),
                displayName = c.name,
                isNpc = true,
                currentHp = c.currentHp,
                maxHp = c.maxHp,
                tempHp = c.tempHp
            )
        }
        StartCombatDialog(
            candidates = playerCandidates + npcCandidates,
            onDismiss = { showStartDialog = false },
            onConfirm = { combatants ->
                viewModel.startCombat(combatants)
                showStartDialog = false
            }
        )
    }
}

/** Diálogo de seleção de combatentes (jogadores conectados + NPCs do Mestre) e sua iniciativa. */
@Composable
private fun StartCombatDialog(
    candidates: List<CombatCandidate>,
    onDismiss: () -> Unit,
    onConfirm: (List<Combatant>) -> Unit
) {
    // id do candidato -> texto digitado de iniciativa; presença no mapa = selecionado.
    val selections = remember { mutableStateMapOf<String, String>() }
    val manualCurrentHp = remember { mutableStateMapOf<String, String>() }
    val manualMaxHp = remember { mutableStateMapOf<String, String>() }
    val selectedCandidates = candidates.filter { selections.containsKey(it.id) }
    val validSelections = selectedCandidates.isNotEmpty() && selectedCandidates.all { candidate ->
        val initiativeValid = selections[candidate.id]?.toIntOrNull() != null
        val hasSheetHp = candidate.currentHp != null && candidate.maxHp != null
        val manualCurrent = manualCurrentHp[candidate.id]?.toIntOrNull()
        val manualMax = manualMaxHp[candidate.id]?.toIntOrNull()
        initiativeValid && (hasSheetHp || (manualCurrent != null && manualMax != null && manualMax > 0 && manualCurrent <= manualMax))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Iniciar combate") },
        text = {
            if (candidates.isEmpty()) {
                Text(
                    "Nenhum jogador conectado nem ficha marcada como NPC. Conecte jogadores ou " +
                        "marque alguma ficha como NPC em \"Minhas fichas\" antes de iniciar."
                )
            } else {
                Column(
                    modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "Marque quem participa e informe a iniciativa de cada um. Jogadores sem ficha precisam informar os PV manualmente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    candidates.forEach { candidate ->
                        val checked = selections.containsKey(candidate.id)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) selections[candidate.id] = "" else selections.remove(candidate.id)
                                }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    candidate.displayName + if (candidate.isNpc) " (NPC)" else "",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    if (candidate.currentHp != null && candidate.maxHp != null)
                                        "PV ${candidate.currentHp}/${candidate.maxHp}"
                                    else "PV não informado",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (checked) {
                                OutlinedTextField(
                                    value = selections[candidate.id] ?: "",
                                    onValueChange = { selections[candidate.id] = it },
                                    label = { Text("Iniciativa") },
                                    modifier = Modifier.width(96.dp)
                                )
                            }
                        }
                        if (checked && (candidate.currentHp == null || candidate.maxHp == null)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = manualCurrentHp[candidate.id] ?: "",
                                    onValueChange = { manualCurrentHp[candidate.id] = it },
                                    label = { Text("PV atuais") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = manualMaxHp[candidate.id] ?: "",
                                    onValueChange = { manualMaxHp[candidate.id] = it },
                                    label = { Text("PV máximos") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = validSelections,
                onClick = {
                    val combatants = selectedCandidates.map { candidate ->
                        Combatant(
                            id = candidate.id,
                            displayName = candidate.displayName,
                            isNpc = candidate.isNpc,
                            initiative = selections.getValue(candidate.id).toInt(),
                            currentHp = candidate.currentHp ?: manualCurrentHp.getValue(candidate.id).toInt(),
                            maxHp = candidate.maxHp ?: manualMaxHp.getValue(candidate.id).toInt(),
                            tempHp = candidate.tempHp
                        )
                    }
                    onConfirm(combatants)
                }
            ) {
                Text("Iniciar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

/**
 * Mostra a ordem de iniciativa e o PV de cada combatente, destacando de quem é a vez. Para o
 * Mestre ([isDm] = true), inclui controles de dano/cura diretos e avanço de turno. Para o
 * Jogador, é somente leitura, exceto no próprio combatente ([selfId]), onde pode *pedir* uma
 * mudança de PV ao Mestre (via [onRequestHpChange]) — o pedido só vale depois de aprovado.
 */
@Composable
private fun CombatTracker(
    combat: CombatState,
    isDm: Boolean,
    selfId: String? = null,
    onDamage: ((String, Int) -> Unit)? = null,
    onRequestHpChange: ((String, Int) -> Unit)? = null,
    onNextTurn: (() -> Unit)? = null,
    onEndCombat: (() -> Unit)? = null
) {
    Column {
        Text("Rodada ${combat.round}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        if (combat.combatants.isNotEmpty() && combat.currentCombatantId == null) {
            val allDead = combat.combatants.none { it.currentHp > CombatState.DEAD_HP_THRESHOLD }
            Text(
                if (allDead) {
                    "Todos os combatentes estão mortos (−10 PV ou menos). Encerre o combate para continuar."
                } else {
                    "O combatente atual está morto. Avance para o próximo turno."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        combat.combatants.forEach { combatant ->
            val isCurrentTurn = combatant.id == combat.currentCombatantId
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrentTurn) {
                        DndGold.copy(alpha = 0.18f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            (if (isCurrentTurn) "▶ " else "") + combatant.displayName + if (combatant.isNpc) " (NPC)" else "",
                            fontWeight = FontWeight.Bold
                        )
                        Text("Iniciativa ${combatant.initiative}", style = MaterialTheme.typography.labelSmall)
                    }
                    Text(
                        "PV ${combatant.currentHp}/${combatant.maxHp}" +
                            if (combatant.tempHp > 0) " (+${combatant.tempHp} temp)" else "",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (isDm && onDamage != null) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(onClick = { onDamage(combatant.id, 5) }, modifier = Modifier.weight(1f)) { Text("Dano 5") }
                                TextButton(onClick = { onDamage(combatant.id, 1) }, modifier = Modifier.weight(1f)) { Text("Dano 1") }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(onClick = { onDamage(combatant.id, -1) }, modifier = Modifier.weight(1f)) { Text("Cura 1") }
                                TextButton(onClick = { onDamage(combatant.id, -5) }, modifier = Modifier.weight(1f)) { Text("Cura 5") }
                            }
                        }
                    } else if (!isDm && onRequestHpChange != null && combatant.id == selfId) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(onClick = { onRequestHpChange(combatant.id, 5) }, modifier = Modifier.weight(1f)) { Text("Pedir dano 5") }
                                TextButton(onClick = { onRequestHpChange(combatant.id, 1) }, modifier = Modifier.weight(1f)) { Text("Pedir dano 1") }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(onClick = { onRequestHpChange(combatant.id, -1) }, modifier = Modifier.weight(1f)) { Text("Pedir cura 1") }
                                TextButton(onClick = { onRequestHpChange(combatant.id, -5) }, modifier = Modifier.weight(1f)) { Text("Pedir cura 5") }
                            }
                        }
                    }
                }
            }
        }
        if (isDm && onNextTurn != null && onEndCombat != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Button(onClick = onNextTurn) {
                    Icon(Icons.Default.SkipNext, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Próximo turno")
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = onEndCombat) {
                    Text("Encerrar combate", color = DndCrimson)
                }
            }
        }
    }
}

/**
 * Lista de pedidos de jogadores aguardando decisão do Mestre (ver [RequestKind]). Aprovar aplica
 * a mudança correspondente (PV em combate, ou nova versão da ficha); negar não aplica nada.
 */
@Composable
private fun PendingRequestsSection(
    requests: List<ActionRequest>,
    onApprove: (String) -> Unit,
    onDeny: (String) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = DndGold, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Pedidos pendentes (${requests.size})", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        requests.forEach { request ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(request.requesterName, fontWeight = FontWeight.SemiBold)
                    Text(request.summary, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row {
                        Button(onClick = { onApprove(request.id) }) { Text("Aprovar") }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = { onDeny(request.id) }) { Text("Negar", color = DndCrimson) }
                    }
                }
            }
        }
    }
}

/**
 * Baú de loot compartilhado da sessão. Para o Mestre ([isDm] = true), permite adicionar e
 * remover itens direto (é a autoridade); para o Jogador, é somente leitura com um botão
 * "Pegar" por item, que só vira item de verdade na ficha depois do Mestre aprovar o pedido.
 */
@Composable
private fun LootChestSection(
    items: List<CharacterInventoryItem>,
    isDm: Boolean,
    onAddItem: ((CharacterInventoryItem) -> Unit)? = null,
    onRemoveItem: ((String) -> Unit)? = null,
    onRequestTake: ((CharacterInventoryItem) -> Unit)? = null
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Inventory2, contentDescription = null, tint = DndGold, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Baú de loot", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (items.isEmpty()) {
            Text(
                "O baú está vazio.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            items.forEach { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.name + if (item.quantity > 1) " x${item.quantity}" else "",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Peso ${item.weightLb} lb • ${item.costGp} po",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isDm && onRemoveItem != null) {
                            TextButton(onClick = { onRemoveItem(item.id) }) { Text("Remover", color = DndCrimson) }
                        } else if (!isDm && onRequestTake != null) {
                            Button(onClick = { onRequestTake(item) }) { Text("Pegar") }
                        }
                    }
                }
            }
        }
        if (isDm && onAddItem != null) {
            Spacer(modifier = Modifier.height(12.dp))
            AddLootItemForm(onAddItem)
        }
    }
}

/** Formulário simples para o Mestre adicionar um item novo ao baú de loot. */
@Composable
private fun AddLootItemForm(onAddItem: (CharacterInventoryItem) -> Unit) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Itens de Aventura") }
    var quantity by remember { mutableStateOf("1") }
    var weight by remember { mutableStateOf("0") }
    var cost by remember { mutableStateOf("0") }

    val parsedQuantity = quantity.toIntOrNull()?.takeIf { it > 0 }
    val parsedWeight = weight.replace(',', '.').toFloatOrNull()?.takeIf { it.isFinite() && it >= 0f }
    val parsedCost = cost.replace(',', '.').toFloatOrNull()?.takeIf { it.isFinite() && it >= 0f }
    val valid = name.isNotBlank() && category.isNotBlank() &&
        parsedQuantity != null && parsedWeight != null && parsedCost != null

    Column {
        Text("Adicionar item ao baú", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome do item") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Categoria") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it.take(7) },
            label = { Text("Quantidade") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = parsedQuantity == null
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it.take(12) },
            label = { Text("Peso por unidade (lb)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = parsedWeight == null
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = cost,
            onValueChange = { cost = it.take(12) },
            label = { Text("Preço por unidade (po)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = parsedCost == null
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                val qty = parsedQuantity ?: return@Button
                val itemWeight = parsedWeight ?: return@Button
                val itemCost = parsedCost ?: return@Button
                if (name.isBlank() || category.isBlank()) return@Button
                onAddItem(
                    CharacterInventoryItem(
                        name = name.trim(),
                        category = category.trim(),
                        quantity = qty,
                        weightLb = itemWeight,
                        costGp = itemCost
                    )
                )
                name = ""
                quantity = "1"
                weight = "0"
                cost = "0"
            },
            enabled = valid
        ) {
            Text("Adicionar ao baú")
        }
    }
}

/**
 * Loja de NPC: o Mestre escolhe uma de suas fichas marcadas como NPC e abre a loja usando o
 * inventário dela como estoque (preço = `costGp` de cada item). Comprar não altera a ficha
 * real do NPC — é só um instantâneo da sessão, igual ao baú de loot.
 */
@Composable
private fun DmShopSection(
    shop: ShopState?,
    characterViewModel: CharacterViewModel,
    onOpenShop: (Long, String, List<CharacterInventoryItem>) -> Unit,
    onCloseShop: () -> Unit
) {
    val myCharacters by characterViewModel.allCharacters.collectAsStateWithLifecycle()
    val npcCandidates = myCharacters.filter { it.isNpc }

    Column {
        Text("Loja de NPC", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        if (shop == null) {
            if (npcCandidates.isEmpty()) {
                Text(
                    "Marque alguma ficha como NPC (acima) para poder abrir uma loja com o inventário dela.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                var expanded by remember { mutableStateOf(false) }
                var selectedNpc by remember(npcCandidates) { mutableStateOf(npcCandidates.firstOrNull()) }
                Box {
                    OutlinedButton(onClick = { expanded = true }) {
                        Text(selectedNpc?.name ?: "Escolher NPC")
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        npcCandidates.forEach { npc ->
                            DropdownMenuItem(
                                text = { Text(npc.name) },
                                onClick = {
                                    selectedNpc = npc
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    selectedNpc?.let { npc ->
                        val items = CharacterJsonAdapters.deserializeInventory(npc.inventoryJson)
                        onOpenShop(npc.id, npc.name, items)
                    }
                }, enabled = selectedNpc != null) {
                    Text("Abrir loja")
                }
            }
        } else {
            Text("Loja aberta: ${shop.npcName}", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            if (shop.items.isEmpty()) {
                Text(
                    "Essa loja não tem mais itens à venda.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                shop.items.forEach { item ->
                    Text(
                        "• ${item.name}" + (if (item.quantity > 1) " x${item.quantity}" else "") + " — ${item.costGp} po",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onCloseShop) {
                Text("Fechar loja", color = DndCrimson)
            }
        }
    }
}

/** Mensagens privadas do Mestre: escolhe um jogador e manda texto que só ele recebe. */
@Composable
private fun DmPrivateChatSection(
    players: List<Participant>,
    chatLog: List<PrivateChatEntry>,
    onSend: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedPlayer by remember(players) { mutableStateOf(players.firstOrNull()) }
    var message by remember { mutableStateOf("") }

    Column {
        Text("Mensagens privadas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        if (players.isEmpty()) {
            Text(
                "Nenhum jogador conectado ainda.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return
        }
        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(selectedPlayer?.displayName ?: "Escolher jogador")
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                players.forEach { player ->
                    DropdownMenuItem(
                        text = { Text(player.displayName) },
                        onClick = {
                            selectedPlayer = player
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Mensagem") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = {
                selectedPlayer?.let { player ->
                    onSend(player.id, message)
                    message = ""
                }
            },
            enabled = selectedPlayer != null && message.isNotBlank()
        ) {
            Text("Enviar")
        }
        if (chatLog.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text("Histórico", style = MaterialTheme.typography.labelMedium)
            chatLog.takeLast(10).forEach { entry ->
                Text(
                    (if (entry.fromDm) "Você → ${entry.participantName}: " else "${entry.participantName}: ") + entry.text,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Loja de NPC do lado do Jogador: lista somente leitura com um botão "Comprar" por item, que
 * manda um pedido de compra ao Mestre (não desconta ouro nem adiciona o item até ser aprovado).
 */
@Composable
private fun PlayerShopSection(shop: ShopSnapshot, onRequestBuy: (CharacterInventoryItem) -> Unit) {
    Column {
        Text("Loja: ${shop.npcName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        if (shop.items.isEmpty()) {
            Text(
                "Não há mais itens à venda.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            shop.items.forEach { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.name + if (item.quantity > 1) " x${item.quantity}" else "",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "${item.costGp} po",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(onClick = { onRequestBuy(item) }) { Text("Comprar") }
                    }
                }
            }
        }
    }
}

/** Mensagem privada do Jogador ao Mestre, mais o histórico recebido nesta sessão. */
@Composable
private fun PlayerPrivateChatSection(receivedMessages: List<String>, onSend: (String) -> Unit) {
    var message by remember { mutableStateOf("") }

    Column {
        Text("Mensagem privada ao Mestre", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Mensagem") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = {
                if (message.isNotBlank()) {
                    onSend(message)
                    message = ""
                }
            },
            enabled = message.isNotBlank()
        ) {
            Text("Enviar")
        }
        if (receivedMessages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text("Mensagens do Mestre", style = MaterialTheme.typography.labelMedium)
            receivedMessages.takeLast(10).forEach { text ->
                Text("Mestre: $text", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun PlayerJoinContent(viewModel: SessionViewModel, characterViewModel: CharacterViewModel, selectedArea: SessionAreaTab, onSelectArea: (SessionAreaTab) -> Unit) {
    val state by viewModel.playerState.collectAsStateWithLifecycle()
    val myCharacters by characterViewModel.allCharacters.collectAsStateWithLifecycle()
    val activeCharacter by characterViewModel.activeCharacter.collectAsStateWithLifecycle()
    var displayName by remember { mutableStateOf("Jogador") }
    var manualHost by remember { mutableStateOf("") }
    var manualPort by remember { mutableStateOf("") }
    var selectedCharacter by remember(activeCharacter) { mutableStateOf(activeCharacter) }
    val linkedCharacter = myCharacters.firstOrNull { it.id == selectedCharacter?.id }
    var joinFeedback by remember { mutableStateOf<String?>(null) }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val raw = result.contents
        if (raw != null) {
            val payload = JoinPayload.fromJson(raw)
            if (payload == null) {
                joinFeedback = "QR Code inválido ou incompatível. Solicite ao Mestre um novo código."
            } else {
                joinFeedback = null
                viewModel.joinSession(payload, displayName, linkedCharacter)
            }
        }
    }

    when (val s = state) {
        is PlayerSessionUiState.Idle, is PlayerSessionUiState.Disconnected -> {
            joinFeedback?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (s is PlayerSessionUiState.Disconnected) {
                if (s.reason.isNotBlank()) {
                    Text(s.reason, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Button(onClick = { viewModel.reconnect() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Reconectar")
                }
                Text(
                    "Se o Mestre recuperou a sessão após reiniciar, escaneie o novo QR Code para manter seu participante. Também é possível entrar novamente preenchendo os dados abaixo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Seu nome (aparece para o Mestre)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            CharacterPicker(
                characters = myCharacters,
                selected = selectedCharacter,
                onSelect = { selectedCharacter = it }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val options = ScanOptions()
                        .setOrientationLocked(false)
                        .setBeepEnabled(false)
                        .setPrompt("Aponte para o QR Code do Mestre")
                    scanLauncher.launch(options)
                },
                modifier = Modifier.fillMaxWidth().testTag("scan_qr_button")
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Escanear QR Code")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Ou, se preferir, digite o endereço mostrado na tela do Mestre:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = manualHost,
                onValueChange = { manualHost = it },
                label = { Text("Endereço IP do Mestre") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = manualPort,
                onValueChange = { manualPort = it.filter(Char::isDigit).take(5) },
                label = { Text("Porta") },
                modifier = Modifier.fillMaxWidth()
            )
            val manualPortValue = manualPort.toIntOrNull()
            val validManualHost = JoinPayload.isValidHostAddress(manualHost.trim())
            val validManualPort = manualPortValue != null && manualPortValue in 1..65535
            if (manualHost.isNotBlank() && !validManualHost) {
                Text(
                    "Informe um endereço IPv4 válido (quatro números entre 0 e 255).",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (manualPort.isNotBlank() && !validManualPort) {
                Text(
                    "A porta deve estar entre 1 e 65535.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    if (validManualHost && validManualPort) {
                        viewModel.joinSession(manualHost.trim(), manualPortValue!!, displayName, linkedCharacter)
                    }
                },
                enabled = validManualHost && validManualPort
            ) {
                Text("Entrar manualmente")
            }
        }
        is PlayerSessionUiState.Connecting -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(color = DndGold, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Conectando ao Mestre...")
            }
        }
        is PlayerSessionUiState.Connected -> {
            val feedback by viewModel.playerRequestFeedback.collectAsStateWithLifecycle()
            val receivedDmMessages = remember { mutableStateListOf<String>() }
            LaunchedEffect(viewModel) { viewModel.playerPrivateMessages.collect { text -> receivedDmMessages.add(text) } }
            Text("Conectado!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DndGold)
            feedback?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(message, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    TextButton(onClick = { viewModel.clearRequestFeedback() }) { Text("OK") }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            when (selectedArea) {
                SessionAreaTab.SESSION -> {
                    ParticipantRoster(s.participants, emptyMap())
                    Spacer(modifier = Modifier.height(16.dp))
                    PlayerPrivateChatSection(receivedMessages = receivedDmMessages, onSend = { text -> viewModel.sendPrivateMessageToDm(text) })
                    if (selectedCharacter != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(onClick = { selectedCharacter?.let { viewModel.requestCharacterUpdate(it, "Atualização da ficha de ${it.name}") } }) {
                            Text("Pedir atualização da ficha")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(onClick = { viewModel.leaveSession() }) { Text("Sair da sessão", color = DndCrimson) }
                }
                SessionAreaTab.COMBAT -> {
                    val combat = s.combat
                    if (combat == null) Text("O Mestre ainda não iniciou um combate.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    else CombatTracker(combat = combat, isDm = false, selfId = s.selfId, onRequestHpChange = { combatantId, delta ->
                        val summary = if (delta > 0) "Pedido de dano (${delta} PV) em combate" else "Pedido de cura (${-delta} PV) em combate"
                        viewModel.requestCombatHpChange(combatantId, delta, summary)
                    })
                }
                SessionAreaTab.RESOURCES -> {
                        LootChestSection(items = s.lootChest, isDm = false, onRequestTake = { item ->
                            viewModel.requestTakeLoot(
                                item.id,
                                "Pedido de pegar " + item.name + " do baú de loot",
                                linkedCharacter
                            )
                        })
                        s.shop?.let { shop ->
                            Spacer(modifier = Modifier.height(16.dp))
                            PlayerShopSection(shop = shop, onRequestBuy = { item ->
                                viewModel.requestBuyFromShop(
                                    item.id,
                                    "Pedido de comprar " + item.name + " (" + item.costGp + " po)",
                                    linkedCharacter
                                )
                            })
                        }
                }
            }
        }
    }
}


/**
 * Ações multiplayer contextuais sobre a ficha principal. Cada painel tem seu próprio ciclo
 * de abertura/fechamento; fechar um painel nunca altera o estado da sessão.
 */
@Composable
fun SessionFloatingPanels(
    viewModel: SessionViewModel,
    characterViewModel: CharacterViewModel
) {
    val dmState by viewModel.dmState.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val myCharacters by characterViewModel.allCharacters.collectAsStateWithLifecycle()
    val hosting = dmState as? DmSessionUiState.Hosting
    val connected = playerState as? PlayerSessionUiState.Connected
    val linkedCharacter = myCharacters.firstOrNull { it.id == connected?.characterId }
    var activePanel by remember { mutableStateOf<SessionFloatingPanel?>(null) }

    // No lado do Jogador, o painel fecha assim que o Mestre retira sua disponibilidade.
    LaunchedEffect(connected?.combat, connected?.lootChest, connected?.shop) {
        if (connected != null) {
            if (connected.combat == null && activePanel == SessionFloatingPanel.COMBAT) {
                activePanel = null
            }
            if (connected.lootChest.isEmpty() && activePanel == SessionFloatingPanel.LOOT) {
                activePanel = null
            }
            if (connected.shop == null && activePanel == SessionFloatingPanel.SHOP) {
                activePanel = null
            }
        }
    }

    if (hosting != null || connected != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
            Card(
                modifier = Modifier.padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hosting != null || connected?.combat != null) {
                        TextButton(
                            onClick = { activePanel = SessionFloatingPanel.COMBAT },
                            modifier = Modifier.testTag("session_quick_combat")
                        ) { Text("Combate") }
                    }
                    if (hosting != null || connected?.lootChest?.isNotEmpty() == true) {
                        TextButton(
                            onClick = { activePanel = SessionFloatingPanel.LOOT },
                            modifier = Modifier.testTag("session_quick_loot")
                        ) { Text("Loot") }
                    }
                    if (hosting != null || connected?.shop != null) {
                        TextButton(
                            onClick = { activePanel = SessionFloatingPanel.SHOP },
                            modifier = Modifier.testTag("session_quick_shop")
                        ) { Text("Loja") }
                    }
                }
            }
        }
    }

    val panel = activePanel
    if (panel != null && (hosting != null || connected != null)) {
        Dialog(onDismissRequest = { activePanel = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .heightIn(max = 620.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            when (panel) {
                                SessionFloatingPanel.COMBAT -> "Combate"
                                SessionFloatingPanel.LOOT -> "Baú de loot"
                                SessionFloatingPanel.SHOP -> "Loja de NPC"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { activePanel = null }) { Text("Fechar") }
                    }
                    Column(
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                    ) {
                        when (panel) {
                            SessionFloatingPanel.COMBAT -> {
                                when {
                                    hosting != null -> CombatSection(viewModel, characterViewModel, hosting)
                                    connected != null && connected.combat != null -> CombatTracker(
                                        combat = connected.combat,
                                        isDm = false,
                                        selfId = connected.selfId,
                                        onRequestHpChange = { combatantId, delta ->
                                            val summary = if (delta > 0) {
                                                "Pedido de dano ($delta PV) em combate"
                                            } else {
                                                "Pedido de cura (" + (-delta) + " PV) em combate"
                                            }
                                            viewModel.requestCombatHpChange(combatantId, delta, summary)
                                        }
                                    )
                                    else -> Text(
                                        "O Mestre ainda não iniciou um combate.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            SessionFloatingPanel.LOOT -> {
                                when {
                                    hosting != null -> LootChestSection(
                                        items = hosting.lootChest,
                                        isDm = true,
                                        onAddItem = { viewModel.addLootItem(it) },
                                        onRemoveItem = { viewModel.removeLootItem(it) }
                                    )
                                    connected != null -> LootChestSection(
                                        items = connected.lootChest,
                                        isDm = false,
                                        onRequestTake = { item ->
                                            viewModel.requestTakeLoot(
                                                item.id,
                                                "Pedido de pegar " + item.name + " do baú de loot",
                                                linkedCharacter
                                            )
                                        }
                                    )
                                }
                            }
                            SessionFloatingPanel.SHOP -> {
                                when {
                                    hosting != null -> DmShopSection(
                                        shop = hosting.shop,
                                        characterViewModel = characterViewModel,
                                        onOpenShop = { npcId, npcName, items ->
                                            viewModel.openShop(npcId, npcName, items)
                                        },
                                        onCloseShop = { viewModel.closeShop() }
                                    )
                                    connected != null && connected.shop != null -> PlayerShopSection(
                                        shop = connected.shop,
                                        onRequestBuy = { item ->
                                            viewModel.requestBuyFromShop(
                                                item.id,
                                                "Pedido de comprar " + item.name + " (" + item.costGp + " po)",
                                                linkedCharacter
                                            )
                                        }
                                    )
                                    else -> Text(
                                        "O Mestre ainda não disponibilizou uma loja.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Escolha de qual ficha local mandar ao Mestre (padrão: a ficha ativa do jogador). */
@Composable
private fun CharacterPicker(
    characters: List<CharacterEntity>,
    selected: CharacterEntity?,
    onSelect: (CharacterEntity?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    if (characters.isEmpty()) {
        Text(
            "Nenhuma ficha cadastrada neste aparelho — você vai entrar sem vincular uma ficha.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    Column {
        Text("Ficha a enviar ao Mestre", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    selected?.let { "${it.name} (${it.characterClass} Nv ${it.level})" } ?: "Nenhuma",
                    fontWeight = FontWeight.SemiBold
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Escolher ficha")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                characters.forEach { character ->
                    DropdownMenuItem(
                        text = { Text("${character.name} (${character.characterClass} Nv ${character.level})") },
                        onClick = {
                            onSelect(character)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ParticipantRoster(participants: List<Participant>, characters: Map<String, CharacterEntity>) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Groups, contentDescription = null, tint = DndGold, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Participantes (${participants.size})", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.height((participants.size.coerceAtLeast(1) * 72).dp.coerceAtMost(360.dp))) {
            items(participants, key = { it.id }) { participant ->
                val character = characters[participant.id]
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(participant.displayName, fontWeight = FontWeight.SemiBold)
                            if (character != null) {
                                Text(
                                    "${character.characterClass} Nv ${character.level} • PV ${character.currentHp}/${character.maxHp}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else if (participant.role == ParticipantRole.PLAYER) {
                                Text(
                                    "Sem ficha vinculada",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                if (participant.role == ParticipantRole.DM) "Mestre" else "Jogador",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (participant.role == ParticipantRole.DM) DndGold else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!participant.connected) {
                                Text(
                                    "Desconectado",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DndCrimson
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
