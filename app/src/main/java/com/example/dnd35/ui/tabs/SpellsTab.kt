package com.example.dnd35.ui.tabs

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.dnd35.model.CalculatedCharacterStats
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.CharacterSpell
import com.example.dnd35.model.MagicSchoolParser
import com.example.dnd35.model.SpellSlotInfo
import com.example.dnd35.ui.dialogs.AddEditSpellDialog
import com.example.dnd35.viewmodel.CharacterViewModel
import com.example.ui.theme.DndArcaneBlue
import com.example.ui.theme.DndCrimson
import com.example.ui.theme.DndGold
import com.example.ui.theme.DndHpGreen

@Composable
fun SpellsTab(
    viewModel: CharacterViewModel,
    stats: CalculatedCharacterStats,
    character: CharacterEntity
) {
    var showAddSpellModal by remember { mutableStateOf(false) }
    var spellToEdit by remember { mutableStateOf<CharacterSpell?>(null) }
    var pendingSpellDeleteId by remember { mutableStateOf<String?>(null) }
    var configuringSlotsLevel by remember { mutableStateOf<SpellSlotInfo?>(null) }

    // Filtros de busca no grimório do personagem
    var searchQuery by remember { mutableStateOf("") }
    var selectedLevelFilter by remember { mutableStateOf<Int?>(null) }
    var selectedSchoolFilter by remember { mutableStateOf("Todas") }
    var filterOnlyPrepared by remember { mutableStateOf(false) }

    val schools = listOf("Todas", "Abjuração", "Adivinhação", "Conjuração", "Encantamento", "Evocação", "Ilusão", "Necromancia", "Transmutação", "Universal")

    val filteredSpells = stats.spells.filter { spell ->
        val matchesText = spell.name.contains(searchQuery, ignoreCase = true) ||
                spell.description.contains(searchQuery, ignoreCase = true)
        val matchesLevel = selectedLevelFilter == null || spell.level == selectedLevelFilter
        val matchesSchool = MagicSchoolParser.matchesBaseSchool(spell.school, selectedSchoolFilter)
        val matchesPrepared = !filterOnlyPrepared || spell.prepared
        matchesText && matchesLevel && matchesSchool && matchesPrepared
    }.sortedBy { it.level }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("spells_tab")
    ) {
        // 1. Banner Conjurador & Atributos Místicos
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoStories, contentDescription = null, tint = DndArcaneBlue, modifier = Modifier.size(26.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Grimório de magias (D&D 3.5)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                val abilityLabel = stats.castingAbilityName?.let {
                                    "Atributo Chave: $it (+${stats.castingAbilityMod})"
                                } ?: "Sem atributo conjurador base"
                                Text(abilityLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Button(
                            onClick = { viewModel.longRest() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.height(34.dp),
                            contentPadding = ButtonDefaults.TextButtonContentPadding
                        ) {
                            Icon(Icons.Default.Bedtime, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Recuperar espaços de magia", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (stats.casterClasses.size > 1) {
                        Text(
                            "Conjurador Multiclasse (Classes Místicas):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(stats.casterClasses) { c ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                        Text(
                                            "${c.className} (Nv ${c.casterLevel})",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "CD Base: 10 + Nv + ${c.keyAbility} (+${c.keyAbilityMod})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Tabela de Classe de Dificuldade (CD) das Magias por Círculo
                    Text(
                        "Classe de Dificuldade (CD = 10 + Nível + Mod ${stats.castingAbilityName ?: "Atributo"}):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items((0..9).toList()) { lvl ->
                            val dc = stats.spellSaveDcByLevel[lvl] ?: (10 + lvl)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(if (lvl == 0) "Nível 0" else "Nível $lvl", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("CD $dc", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = DndArcaneBlue)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Linha Horizontal de Espaços de Magia (Círculos 0 ao 9)
                    Text("Espaços de magia por dia (toque para gastar ou recuperar):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(stats.spellSlots) { slot ->
                            SpellSlotCard(
                                slot = slot,
                                onUseSlot = { viewModel.adjustSpellSlotUsed(slot.level, 1) },
                                onRecoverSlot = { viewModel.adjustSpellSlotUsed(slot.level, -1) },
                                onConfigure = { configuringSlotsLevel = slot }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 2. Barra de Busca e Filtros Avançados
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar magia no seu grimório...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Filtro por Círculo
            Text("Filtrar por nível da magia:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedLevelFilter == null,
                        onClick = { selectedLevelFilter = null },
                        label = { Text("Todos") }
                    )
                }
                items((0..9).toList()) { lvl ->
                    FilterChip(
                        selected = selectedLevelFilter == lvl,
                        onClick = { selectedLevelFilter = if (selectedLevelFilter == lvl) null else lvl },
                        label = { Text(if (lvl == 0) "Nível 0" else "Nível $lvl") }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            // Filtro por Escola e Preparadas
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(schools) { sch ->
                        FilterChip(
                            selected = selectedSchoolFilter == sch,
                            onClick = { selectedSchoolFilter = sch },
                            label = { Text(sch) }
                        )
                    }
                }
                FilterChip(
                    selected = filterOnlyPrepared,
                    onClick = { filterOnlyPrepared = !filterOnlyPrepared },
                    label = { Text("★ Preparadas") },
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 3. Barra de Ação "+ Adicionar Magia"
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Magias na Ficha (${filteredSpells.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { showAddSpellModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = DndArcaneBlue)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adicionar Magia")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 4. Lista Detalhada de Magias
        if (filteredSpells.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Nenhuma magia encontrada.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Toque em '+ Adicionar Magia' para pesquisar magias do Livro do Jogador por nível, escola e classe conjuradora.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredSpells) { spell ->
                val dc = stats.spellSaveDcByLevel[spell.level] ?: (10 + spell.level)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (spell.prepared) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DndArcaneBlue),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (spell.level == 0) "Truque" else "${spell.level}º Círculo",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    spell.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { spellToEdit = spell }, modifier = Modifier.size(48.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar ${spell.name}", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { pendingSpellDeleteId = spell.id }, modifier = Modifier.size(48.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir ${spell.name}", modifier = Modifier.size(18.dp), tint = DndCrimson)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Linha de Propriedades da Magia
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                "${spell.school} • Comp: ${spell.components} • CD $dc",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = DndArcaneBlue
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = spell.prepared,
                                    onCheckedChange = { viewModel.toggleSpellPrepared(spell.id) },
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Preparada", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Text(
                            "Tempo: ${spell.castTime} • Alcance: ${spell.range} • Duração: ${spell.duration}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (spell.savingThrow != "Nenhum" && spell.savingThrow.isNotBlank()) {
                            Text(
                                "Resistência: ${spell.savingThrow} • RM: ${if (spell.spellResistance) "Sim" else "Não"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (spell.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                spell.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal para Adicionar Magia
    if (showAddSpellModal) {
        AddEditSpellDialog(
            spellToEdit = null,
            onConfirm = { newSpell -> viewModel.addSpell(newSpell) },
            onDismiss = { showAddSpellModal = false }
        )
    }

    // Modal para Editar Magia
    pendingSpellDeleteId?.let { spellId ->
        AlertDialog(
            onDismissRequest = { pendingSpellDeleteId = null },
            title = { Text("Excluir magia?") },
            text = { Text("Esta ação não pode ser desfeita.") },
            confirmButton = { Button(onClick = { viewModel.removeSpell(spellId); pendingSpellDeleteId = null }) { Text("Excluir") } },
            dismissButton = { OutlinedButton(onClick = { pendingSpellDeleteId = null }) { Text("Cancelar") } }
        )
    }

    spellToEdit?.let { spell ->
        AddEditSpellDialog(
            spellToEdit = spell,
            onConfirm = { updated ->
                viewModel.removeSpell(spell.id)
                viewModel.addSpell(updated)
                spellToEdit = null
            },
            onDismiss = { spellToEdit = null }
        )
    }

    // Modal para Configurar Espaços Totais deste Círculo
    configuringSlotsLevel?.let { slot ->
        var totalText by remember { mutableStateOf("${slot.totalSlots}") }
        AlertDialog(
            onDismissRequest = { configuringSlotsLevel = null },
            title = { Text("Espaços Diários: Círculo ${slot.level}") },
            text = {
                OutlinedTextField(
                    value = totalText,
                    onValueChange = { totalText = it },
                    label = { Text("Total de Espaços por Dia") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    val tot = totalText.toIntOrNull()?.coerceAtLeast(0) ?: slot.totalSlots
                    viewModel.setSpellSlotTotal(slot.level, tot)
                    configuringSlotsLevel = null
                }) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { configuringSlotsLevel = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun SpellSlotCard(
    slot: SpellSlotInfo,
    onUseSlot: () -> Unit,
    onRecoverSlot: () -> Unit,
    onConfigure: () -> Unit
) {
    val remaining = (slot.totalSlots - slot.usedSlots).coerceAtLeast(0)
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (remaining > 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.width(96.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (slot.level == 0) "Nível 0" else "Nível ${slot.level}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = DndArcaneBlue
                )
                IconButton(onClick = onConfigure, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Configurar espaços de magia do nível ${slot.level}", modifier = Modifier.size(12.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$remaining / ${slot.totalSlots}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (remaining > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Text(
                text = "disponíveis",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = onUseSlot,
                    enabled = remaining > 0,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Gastar espaço de magia", modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = onRecoverSlot,
                    enabled = slot.usedSlots > 0,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Recuperar espaço de magia", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
