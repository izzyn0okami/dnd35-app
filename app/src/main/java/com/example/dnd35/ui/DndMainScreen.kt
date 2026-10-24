package com.example.dnd35.ui

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
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dnd35.pdf.CharacterPdfExporter
import com.example.dnd35.ui.dialogs.NewCharacterDialog
import com.example.dnd35.ui.tabs.FeatsTab
import com.example.dnd35.ui.tabs.InventoryTab
import com.example.dnd35.ui.tabs.OverviewCombatTab
import com.example.dnd35.ui.tabs.SkillsTab
import com.example.dnd35.ui.tabs.SpellsTab
import com.example.dnd35.viewmodel.CharacterViewModel
import com.example.ui.theme.DndCrimson
import com.example.ui.theme.DndGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DndMainScreen(viewModel: CharacterViewModel) {
    val context = LocalContext.current
    val characters by viewModel.allCharacters.collectAsStateWithLifecycle()
    val activeCharacter by viewModel.activeCharacter.collectAsStateWithLifecycle()
    val computedStats by viewModel.computedStats.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showCharacterMenu by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showNewCharModal by remember { mutableStateOf(false) }
    var showDeleteConfirmModal by remember { mutableStateOf(false) }
    var exportAllPending by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.backupMessages.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
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
                                        color = DndGold
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
                                        Icon(Icons.Default.Add, contentDescription = null, tint = DndGold, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Criar Novo Personagem", color = DndGold, fontWeight = FontWeight.Bold)
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
                            tint = DndGold
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
                                importLauncher.launch(
                                    arrayOf("application/json", "application/octet-stream", "text/plain")
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = DndCrimson, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Excluir Personagem", color = DndCrimson)
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
                val items = listOf(
                    Triple("Combate", Icons.Default.Shield, 0),
                    Triple("Perícias", Icons.Default.Star, 1),
                    Triple("Carga & Itens", Icons.Default.FitnessCenter, 2),
                    Triple("Talentos", Icons.Default.MilitaryTech, 3),
                    Triple("Grimório", Icons.Default.AutoStories, 4)
                )

                items.forEach { (label, icon, index) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
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

            if (char == null && characters.isEmpty()) {
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
                        tint = DndGold,
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
                    CircularProgressIndicator(color = DndGold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Carregando Grimório D&D 3.5...")
                }
            } else {
                when (selectedTab) {
                    0 -> OverviewCombatTab(viewModel = viewModel, stats = calc, character = char)
                    1 -> SkillsTab(viewModel = viewModel, stats = calc, character = char)
                    2 -> InventoryTab(viewModel = viewModel, stats = calc, character = char)
                    3 -> FeatsTab(viewModel = viewModel, stats = calc, character = char)
                    4 -> SpellsTab(viewModel = viewModel, stats = calc, character = char)
                }
            }
        }
    }

    if (showNewCharModal) {
        NewCharacterDialog(
            onConfirm = { name, charClass, race ->
                viewModel.createCharacter(name, charClass, race)
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
}

/** Nome sugerido para o arquivo de backup: sem acentos e caracteres problemáticos. */
internal fun backupFileName(characterName: String?): String {
    val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ROOT).format(java.util.Date())
    if (characterName.isNullOrBlank()) return "fichas-dnd35-$date.json"
    val safe = java.text.Normalizer.normalize(characterName.trim(), java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .replace(Regex("[^A-Za-z0-9_-]+"), "_")
        .trim('_')
        .take(40)
        .ifEmpty { "ficha" }
    return "ficha-$safe-$date.json"
}
