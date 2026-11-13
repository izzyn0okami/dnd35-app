package com.example.dnd35.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import android.widget.Toast
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dnd35.backup.Dnd35FileKind
import com.example.dnd35.backup.Dnd35FileTypes
import com.example.dnd35.diagnostics.CrashLogStore
import com.example.dnd35.pdf.CharacterPdfExporter
import com.example.dnd35.ui.dialogs.CreditsDialog
import com.example.dnd35.ui.dialogs.NewCharacterDialog
import com.example.dnd35.ui.tabs.FeatsTab
import com.example.dnd35.ui.tabs.InventoryTab
import com.example.dnd35.ui.tabs.OverviewCombatTab
import com.example.dnd35.ui.tabs.SkillsTab
import com.example.dnd35.ui.tabs.SpellsTab
import com.example.dnd35.ui.tabs.CreatureCatalogTab
import com.example.dnd35.ui.session.SessionHubScreen
import com.example.dnd35.ui.session.SessionFloatingPanels
import com.example.dnd35.viewmodel.CharacterViewModel
import com.example.dnd35.viewmodel.DmSessionUiState
import com.example.dnd35.viewmodel.SessionViewModel
import com.example.dnd35.viewmodel.PlayerSessionUiState
import com.example.ui.theme.DndCrimson
import com.example.ui.theme.DndGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DndMainScreen(
    viewModel: CharacterViewModel,
    sessionViewModel: SessionViewModel? = null,
    incomingFileUri: Uri? = null,
    onIncomingFileHandled: () -> Unit = {}
) {
    val context = LocalContext.current
    val characters by viewModel.allCharacters.collectAsStateWithLifecycle()
    val activeCharacter by viewModel.activeCharacter.collectAsStateWithLifecycle()
    val computedStats by viewModel.computedStats.collectAsStateWithLifecycle()

    val dmSessionState = sessionViewModel?.dmState?.collectAsStateWithLifecycle()?.value ?: DmSessionUiState.Idle
    // O bestiário integral é uma ferramenta do Mestre e só aparece enquanto há combate ativo.
    val canAccessBestiary = dmSessionState is DmSessionUiState.Hosting && dmSessionState.combat != null

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showCharacterMenu by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showNewCharModal by remember { mutableStateOf(false) }
    var showDeleteConfirmModal by remember { mutableStateOf(false) }
    var exportAllPending by remember { mutableStateOf(false) }
    var showSessionHub by remember { mutableStateOf(false) }
    var showCreditsDialog by remember { mutableStateOf(false) }
    var incomingItemImportUri by remember { mutableStateOf<Uri?>(null) }
    var showIncomingItemTargetDialog by remember { mutableStateOf(false) }
    var selectedIncomingItemTargetId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(canAccessBestiary) {
        if (!canAccessBestiary && selectedTab == 5) selectedTab = 0
    }

    LaunchedEffect(viewModel) {
        viewModel.backupMessages.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    // A coleta fica no nível da tela principal, não dentro do painel da sessão:
    // os PV continuam sendo persistidos mesmo quando o jogador está em outra aba.
    LaunchedEffect(sessionViewModel, viewModel) {
        val activeSession = sessionViewModel ?: return@LaunchedEffect
        activeSession.playerState.collect { sessionState ->
            val connected = sessionState as? PlayerSessionUiState.Connected ?: return@collect
            val characterId = connected.characterId ?: return@collect
            val ownCombatant = connected.combat?.combatants
                ?.firstOrNull { it.id == connected.selfId && !it.isNpc }
                ?: return@collect
            viewModel.applySessionCombatHp(
                characterId = characterId,
                currentHp = ownCombatant.currentHp,
                tempHp = ownCombatant.tempHp
            )
        }
    }

    // Concessões de loot e compras são processadas mesmo quando nenhum painel está aberto.
    LaunchedEffect(sessionViewModel, viewModel) {
        val activeSession = sessionViewModel ?: return@LaunchedEffect
        activeSession.lootGranted.collect { grant ->
            val applied = viewModel.applySessionGrant(grant)
            if (applied) activeSession.acknowledgeResourceGrant(grant.grantId)
            Toast.makeText(
                context,
                if (applied) "Você recebeu \"${grant.item.name}\" do baú de loot."
                else "A entrega de loot continua pendente: não foi possível atualizar a ficha vinculada.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    LaunchedEffect(sessionViewModel, viewModel) {
        val activeSession = sessionViewModel ?: return@LaunchedEffect
        activeSession.shopPurchaseGranted.collect { grant ->
            val applied = viewModel.applySessionGrant(grant)
            if (applied) activeSession.acknowledgeResourceGrant(grant.grantId)
            Toast.makeText(
                context,
                if (applied) "Compra concluída: \"${grant.item.name}\" por ${grant.item.costGp} po."
                else "A compra continua pendente: não foi possível atualizar a ficha vinculada.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(Dnd35FileTypes.CHARACTER_MIME)
    ) { uri ->
        if (uri != null) viewModel.exportBackup(uri, exportAllPending)
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.importBackup(uri)
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.clickable { showCharacterMenu = true }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = activeCharacter?.name ?: "D&D 3.5",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Alternar Personagem")
                                }
                                if (activeCharacter != null) {
                                    Text(
                                        text = "${activeCharacter?.characterClass} Nv ${activeCharacter?.level} • Salvamento Automático",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Dropdown com lista de fichas
                        DropdownMenu(
                            expanded = showCharacterMenu,
                            onDismissRequest = { showCharacterMenu = false }
                        ) {
                            Text(
                                "Suas Fichas Salvas:",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            for (c in characters) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "${c.name} (${c.characterClass} Nv ${c.level})",
                                            fontWeight = if (c.id == activeCharacter?.id) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        viewModel.selectCharacter(c.id)
                                        showCharacterMenu = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Criar Novo Personagem", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                },
                                onClick = {
                                    showCharacterMenu = false
                                    showNewCharModal = true
                                }
                            )
                        }
                    }
                },
                actions = {
                    // Botão Sessão de Mesa (Mestre/Jogador, offline, mesma rede local)
                    if (sessionViewModel != null) {
                        IconButton(
                            onClick = { showSessionHub = true },
                            modifier = Modifier.testTag("session_hub_button")
                        ) {
                            Icon(
                                Icons.Default.Wifi,
                                contentDescription = "Sessão de Mesa",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Botão Exportar PDF Oficial
                    IconButton(
                        onClick = {
                            activeCharacter?.let { char ->
                                CharacterPdfExporter.exportAndSharePdf(context, char)
                            }
                        },
                        modifier = Modifier.testTag("export_pdf_button")
                    ) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = "Exportar PDF",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Botão Novo Personagem
                    IconButton(onClick = { showNewCharModal = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Novo Personagem")
                    }

                    // Menu Mais Opções
                    IconButton(onClick = { showOverflowMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Mais Opções")
                    }

                    DropdownMenu(
                        expanded = showOverflowMenu,
                        onDismissRequest = { showOverflowMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Exportar logs de diagnóstico")
                                }
                            },
                            onClick = {
                                showOverflowMenu = false
                                CrashLogStore.shareLogs(context)
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Duplicar Ficha")
                                }
                            },
                            onClick = {
                                viewModel.duplicateCurrentCharacter()
                                showOverflowMenu = false
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Exportar Ficha (arquivo)")
                                }
                            },
                            enabled = activeCharacter != null,
                            onClick = {
                                showOverflowMenu = false
                                activeCharacter?.let { char ->
                                    exportAllPending = false
                                    exportLauncher.launch(backupFileName(char.name))
                                }
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Exportar Todas as Fichas")
                                }
                            },
                            enabled = characters.isNotEmpty(),
                            onClick = {
                                showOverflowMenu = false
                                exportAllPending = true
                                exportLauncher.launch(backupFileName(null))
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Importar Fichas (arquivo)")
                                }
                            },
                            onClick = {
                                showOverflowMenu = false
                                importLauncher.launch(Dnd35FileTypes.CHARACTER_IMPORT_MIME_TYPES)
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Créditos e licenças")
                                }
                            },
                            modifier = Modifier.testTag("menu_credits"),
                            onClick = {
                                showOverflowMenu = false
                                showCreditsDialog = true
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Excluir Personagem", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            onClick = {
                                showOverflowMenu = false
                                showDeleteConfirmModal = true
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_bottom_nav"),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                val items = buildList {
                    add(Triple("Personagem", Icons.Default.Shield, 0))
                    add(Triple("Perícias", Icons.Default.Star, 1))
                    add(Triple("Carga & Itens", Icons.Default.FitnessCenter, 2))
                    add(Triple("Talentos", Icons.Default.MilitaryTech, 3))
                    add(Triple("Grimório", Icons.Default.AutoStories, 4))
                    if (canAccessBestiary) add(Triple("Monstros", Icons.Default.BugReport, 5))
                }

                items.forEach { (label, icon, index) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            // A sessão permanece conectada no SessionViewModel ao consultar a ficha.
                            showSessionHub = false
                        },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            indicatorColor = DndGold
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val char = activeCharacter
            val calc = computedStats

            if (selectedTab == 5 && canAccessBestiary) {
                CreatureCatalogTab(modifier = Modifier.fillMaxSize())
            } else if (char == null && characters.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .testTag("empty_character_state"),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Sua primeira ficha começa aqui",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Crie um personagem para começar a usar sua ficha de D&D 3.5.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = { showNewCharModal = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Criar personagem")
                    }
                }
            } else if (char == null || calc == null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Carregando Grimório D&D 3.5...")
                }
            } else {
                key(char.id) {
                    when (selectedTab) {
                        0 -> OverviewCombatTab(viewModel = viewModel, stats = calc, character = char)
                        1 -> SkillsTab(viewModel = viewModel, stats = calc, character = char)
                        2 -> InventoryTab(viewModel = viewModel, stats = calc, character = char)
                        3 -> FeatsTab(viewModel = viewModel, stats = calc, character = char)
                        4 -> SpellsTab(viewModel = viewModel, stats = calc, character = char)
                    }
                }
            }

            // A ficha permanece como conteúdo principal; as ferramentas multiplayer são contextuais.
            if (sessionViewModel != null) {
                SessionFloatingPanels(viewModel = sessionViewModel, characterViewModel = viewModel)
            }
        }
    }

    if (showSessionHub && sessionViewModel != null) {
        Dialog(onDismissRequest = { showSessionHub = false }) {
            Card(modifier = Modifier.fillMaxWidth().heightIn(max = 640.dp)) {
                SessionHubScreen(
                    viewModel = sessionViewModel,
                    characterViewModel = viewModel,
                    onClose = { showSessionHub = false }
                )
            }
        }
    }

    if (showCreditsDialog) {
        CreditsDialog(onDismiss = { showCreditsDialog = false })
    }

    if (showNewCharModal) {
        NewCharacterDialog(
            onConfirm = { name, charClass, race, baseStr, baseDex, baseCon, baseInt, baseWis, baseCha ->
                viewModel.createCharacter(name, charClass, race, baseStr, baseDex, baseCon, baseInt, baseWis, baseCha)
            },
            onDismiss = { showNewCharModal = false }
        )
    }

    if (showDeleteConfirmModal) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmModal = false },
            title = { Text("Excluir Personagem") },
            text = {
                Text("Tem certeza que deseja excluir '${activeCharacter?.name}'? Esta ação não pode ser desfeita.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCurrentCharacter()
                        showDeleteConfirmModal = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = DndCrimson)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmModal = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Arquivo .fichadnd35/.itemdnd35 tocado fora do app (gerenciador de arquivos, anexo, etc.) —
    // ver AndroidManifest/MainActivity. Confirma antes de importar, já que tocar sem querer num
    // arquivo não deveria mudar a ficha ativa sozinho.
    if (incomingFileUri != null) {
        val kind = remember(incomingFileUri) { Dnd35FileTypes.classify(context, incomingFileUri) }
        AlertDialog(
            onDismissRequest = onIncomingFileHandled,
            title = { Text("Importar arquivo") },
            text = {
                Text(
                    when (kind) {
                        Dnd35FileKind.CHARACTER -> "Este arquivo parece ser uma ficha de personagem. Importar agora?"
                        Dnd35FileKind.ITEM -> "Este arquivo parece ser um item de inventário. Escolha em qual ficha deseja adicioná-lo."
                        null -> "Não foi possível reconhecer este arquivo como ficha ou item do app."
                    }
                )
            },
            confirmButton = {
                if (kind != null) {
                    Button(onClick = {
                        when (kind) {
                            Dnd35FileKind.CHARACTER -> viewModel.importBackup(incomingFileUri)
                            Dnd35FileKind.ITEM -> {
                                incomingItemImportUri = incomingFileUri
                                selectedIncomingItemTargetId = activeCharacter?.id ?: characters.firstOrNull()?.id
                                showIncomingItemTargetDialog = true
                            }
                        }
                        onIncomingFileHandled()
                    }) {
                        Text("Importar")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onIncomingFileHandled) {
                    Text(if (kind == null) "Fechar" else "Cancelar")
                }
            }
        )
    }

    if (showIncomingItemTargetDialog) {
        AlertDialog(
            onDismissRequest = {
                showIncomingItemTargetDialog = false
                incomingItemImportUri = null
            },
            title = { Text("Adicionar item a qual ficha?") },
            text = {
                if (characters.isEmpty()) {
                    Text("Não há fichas salvas para receber o item.")
                } else {
                    LazyColumn(modifier = Modifier.height(280.dp)) {
                        items(characters, key = { it.id }) { target ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedIncomingItemTargetId = target.id }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedIncomingItemTargetId == target.id,
                                    onClick = { selectedIncomingItemTargetId = target.id }
                                )
                                Column(modifier = Modifier.padding(start = 8.dp)) {
                                    Text(target.name, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${target.characterClass} • Nv ${target.level}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = selectedIncomingItemTargetId != null &&
                        characters.any { it.id == selectedIncomingItemTargetId },
                    onClick = {
                        val uri = incomingItemImportUri
                        val targetId = selectedIncomingItemTargetId
                        if (uri != null && targetId != null) {
                            viewModel.importInventoryItems(uri, targetId)
                        }
                        incomingItemImportUri = null
                        showIncomingItemTargetDialog = false
                    }
                ) { Text("Importar") }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showIncomingItemTargetDialog = false
                    incomingItemImportUri = null
                }) { Text("Cancelar") }
            }
        )
    }
}

/** Nome sugerido para o arquivo de backup: sem acentos e caracteres problemáticos. */
internal fun backupFileName(characterName: String?): String {
    val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ROOT).format(java.util.Date())
    val ext = Dnd35FileTypes.CHARACTER_EXTENSION
    if (characterName.isNullOrBlank()) return "fichas-dnd35-$date$ext"
    val safe = sanitizeFileNamePart(characterName, fallback = "ficha")
    return "ficha-$safe-$date$ext"
}

/** Nome de arquivo seguro para um item avulso de inventário. */
internal fun itemFileName(itemName: String): String {
    val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ROOT).format(java.util.Date())
    val safe = sanitizeFileNamePart(itemName, fallback = "item")
    return "item-$safe-$date${Dnd35FileTypes.ITEM_EXTENSION}"
}

private fun sanitizeFileNamePart(raw: String, fallback: String): String {
    return java.text.Normalizer.normalize(raw.trim(), java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .replace(Regex("[^A-Za-z0-9_-]+"), "_")
        .trim('_')
        .take(40)
        .ifEmpty { fallback }
}
