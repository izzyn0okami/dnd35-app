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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.session.JoinPayload
import com.example.dnd35.session.Participant
import com.example.dnd35.session.ParticipantRole
import com.example.dnd35.viewmodel.CharacterViewModel
import com.example.dnd35.viewmodel.DmSessionUiState
import com.example.dnd35.viewmodel.PlayerSessionUiState
import com.example.dnd35.viewmodel.SessionViewModel
import com.example.ui.theme.DndCrimson
import com.example.ui.theme.DndGold
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

private enum class SessionHubMode { SELECT, DM, PLAYER }

/**
 * Tela de "Sessão de Mesa": ponto de entrada para o modo Mestre (hospedar) ou Jogador (entrar),
 * totalmente offline — a única rede usada é a que os aparelhos já compartilham localmente.
 */
@Composable
fun SessionHubScreen(viewModel: SessionViewModel, characterViewModel: CharacterViewModel, onClose: () -> Unit) {
    var mode by remember { mutableStateOf(SessionHubMode.SELECT) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
            SessionHubMode.SELECT -> ModeSelectContent(onPickDm = { mode = SessionHubMode.DM }, onPickPlayer = { mode = SessionHubMode.PLAYER })
            SessionHubMode.DM -> DmHostContent(viewModel, characterViewModel)
            SessionHubMode.PLAYER -> PlayerJoinContent(viewModel, characterViewModel)
        }
    }
}

@Composable
private fun ModeSelectContent(onPickDm: () -> Unit, onPickPlayer: () -> Unit) {
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
private fun DmHostContent(viewModel: SessionViewModel, characterViewModel: CharacterViewModel) {
    val state by viewModel.dmState.collectAsStateWithLifecycle()
    var hostName by remember { mutableStateOf("Mestre") }

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
            Text(
                "Peça para os jogadores escanearem este código no app deles.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Image(
                bitmap = s.qrBitmap.asImageBitmap(),
                contentDescription = "QR Code da sessão",
                modifier = Modifier.size(220.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            ParticipantRoster(s.participants, s.characters)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = { viewModel.stopHosting() }) {
                Text("Encerrar sessão", color = DndCrimson)
            }
            Spacer(modifier = Modifier.height(24.dp))
            NpcManagementSection(characterViewModel)
        }
        is DmSessionUiState.Error -> {
            Text(s.message, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { viewModel.hostSession(hostName) }) { Text("Tentar novamente") }
        }
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

@Composable
private fun PlayerJoinContent(viewModel: SessionViewModel, characterViewModel: CharacterViewModel) {
    val state by viewModel.playerState.collectAsStateWithLifecycle()
    val myCharacters by characterViewModel.allCharacters.collectAsStateWithLifecycle()
    val activeCharacter by characterViewModel.activeCharacter.collectAsStateWithLifecycle()
    var displayName by remember { mutableStateOf("Jogador") }
    var manualHost by remember { mutableStateOf("") }
    var manualPort by remember { mutableStateOf("") }
    var selectedCharacter by remember(activeCharacter) { mutableStateOf(activeCharacter) }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val raw = result.contents
        if (raw != null) {
            JoinPayload.fromJson(raw)?.let { payload ->
                viewModel.joinSession(payload, displayName, selectedCharacter)
            }
        }
    }

    when (val s = state) {
        is PlayerSessionUiState.Idle, is PlayerSessionUiState.Disconnected -> {
            if (s is PlayerSessionUiState.Disconnected && s.reason.isNotBlank()) {
                Text(s.reason, color = MaterialTheme.colorScheme.error)
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
                onValueChange = { manualPort = it },
                label = { Text("Porta") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    val port = manualPort.toIntOrNull()
                    if (manualHost.isNotBlank() && port != null) {
                        viewModel.joinSession(manualHost.trim(), port, displayName, selectedCharacter)
                    }
                },
                enabled = manualHost.isNotBlank() && manualPort.toIntOrNull() != null
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
            Text("Conectado!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DndGold)
            Spacer(modifier = Modifier.height(12.dp))
            ParticipantRoster(s.participants, emptyMap())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = { viewModel.leaveSession() }) {
                Text("Sair da sessão", color = DndCrimson)
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
        LazyColumn(modifier = Modifier.height((participants.size.coerceAtLeast(1) * 56).dp.coerceAtMost(280.dp))) {
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
                        Text(
                            if (participant.role == ParticipantRole.DM) "Mestre" else "Jogador",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (participant.role == ParticipantRole.DM) DndGold else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
