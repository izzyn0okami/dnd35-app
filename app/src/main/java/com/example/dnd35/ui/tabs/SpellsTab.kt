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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dnd35.model.CalculatedCharacterStats
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.CharacterSpell
import com.example.dnd35.model.Dnd35Rules
import com.example.dnd35.model.SpellFilters
import com.example.dnd35.model.SpellbookRules
import com.example.dnd35.model.SpellSlotInfo
import com.example.dnd35.ui.dialogs.AddEditSpellDialog
import com.example.dnd35.ui.dialogs.SpellDetailDialog
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

    // Seção ativa (Preparadas, Conhecidas, Catálogo, Histórico) e filtros de cada uma
    var selectedSection by rememberSaveable { mutableStateOf(0) }
    var knownQuery by remember { mutableStateOf("") }
    var onlyFavorites by remember { mutableStateOf(false) }
    var catalogCriteria by remember { mutableStateOf(SpellFilters.Criteria()) }
    var showMoreFilters by remember { mutableStateOf(false) }
    var detailSpell by remember { mutableStateOf<Dnd35Rules.PresetSpell?>(null) }
    var showClearHistory by remember { mutableStateOf(false) }

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
                    Column(modifier = Modifier.fillMaxWidth()) {
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
                            modifier = Modifier.align(Alignment.End).height(34.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
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
                                        Text(
                                            knownSpellsSummary(c),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        stats.casterClasses.firstOrNull()?.let { c ->
                            Text(
                                knownSpellsSummary(c),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
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
                    Text(
                        "Espaços de magia por dia (cálculo automático; toque para gastar ou recuperar):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Um cartão por classe e círculo; o nome da classe só aparece na multiclasse.
                        items(stats.spellSlots.filter { it.totalSlots > 0 || it.usedSlots > 0 }) { slot ->
                            SpellSlotCard(
                                slot = slot,
                                classLabel = slot.className.takeIf { stats.casterClasses.size > 1 },
                                onUseSlot = { viewModel.adjustSpellSlotUsed(slot, 1, stats.spellSlots) },
                                onRecoverSlot = { viewModel.adjustSpellSlotUsed(slot, -1, stats.spellSlots) }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Domínios do Clérigo (só aparece para quem tem a classe Clérigo)
        clericDomainSection(viewModel, stats)
        // Especialização do Mago (só aparece para quem tem a classe Mago)
        wizardSpecialtySection(viewModel, stats)

        // 2. Seções: Preparadas, Conhecidas, Catálogo e Histórico
        item {
            TabRow(selectedTabIndex = selectedSection) {
                SPELL_SECTIONS.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedSection == index,
                        onClick = { selectedSection = index },
                        text = { Text(title, maxLines = 1, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        when (selectedSection) {
            0 -> preparedSection(
                viewModel = viewModel,
                stats = stats,
                onOpenSpell = { detailSpell = SpellbookRules.presetFor(it) }
            )
            1 -> knownSection(
                viewModel = viewModel,
                stats = stats,
                query = knownQuery,
                onQueryChange = { knownQuery = it },
                onlyFavorites = onlyFavorites,
                onOnlyFavoritesChange = { onlyFavorites = it },
                onAddManual = { showAddSpellModal = true },
                onOpenSpell = { detailSpell = SpellbookRules.presetFor(it) },
                onEditSpell = { spellToEdit = it },
                onDeleteSpell = { pendingSpellDeleteId = it }
            )
            2 -> catalogSection(
                stats = stats,
                criteria = catalogCriteria,
                onCriteriaChange = { catalogCriteria = it },
                showMoreFilters = showMoreFilters,
                onShowMoreFiltersChange = { showMoreFilters = it },
                onOpenSpell = { detailSpell = it }
            )
            else -> historySection(
                stats = stats,
                onClear = { showClearHistory = true }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    detailSpell?.let { preset ->
        SpellDetailDialog(
            spell = preset,
            stats = stats,
            viewModel = viewModel,
            onDismiss = { detailSpell = null }
        )
    }

    if (showClearHistory) {
        AlertDialog(
            onDismissRequest = { showClearHistory = false },
            title = { Text("Limpar histórico?") },
            text = { Text("Os registros de magias conjuradas serão apagados. Os espaços de magia não mudam.") },
            confirmButton = { Button(onClick = { viewModel.clearSpellHistory(); showClearHistory = false }) { Text("Limpar") } },
            dismissButton = { OutlinedButton(onClick = { showClearHistory = false }) { Text("Cancelar") } }
        )
    }

    // Modal para criar magia personalizada
    if (showAddSpellModal) {
        AddEditSpellDialog(
            spellToEdit = null,
            manualOnly = true,
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

}

/**
 * Resumo de magias conhecidas de uma classe conjuradora, para exibição no banner.
 * Conjuradores espontâneos (Bardo/Feiticeiro) têm um repertório limitado por círculo;
 * conjuradores preparados conhecem toda a lista de magias da classe que já podem conjurar
 * (o grimório da ficha já reflete isso — cada magia adicionada é conhecida).
 */
private fun knownSpellsSummary(c: com.example.dnd35.model.CasterClassInfo): String {
    val known = c.knownSpellsByLevel
    return if (known != null) {
        val parts = (0..9).mapNotNull { lvl ->
            val n = known[lvl] ?: 0
            if (n > 0) "${if (lvl == 0) "Trq" else "${lvl}º"}:$n" else null
        }
        if (parts.isEmpty()) "Magias conhecidas: nenhuma ainda" else "Magias conhecidas por círculo: ${parts.joinToString(" ")}"
    } else {
        "Conhece toda a lista de magias de ${c.className} que já pode conjurar (sujeito à preparação diária)"
    }
}

@Composable
private fun SpellSlotCard(
    slot: SpellSlotInfo,
    classLabel: String? = null,
    onUseSlot: () -> Unit,
    onRecoverSlot: () -> Unit
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
            if (!classLabel.isNullOrBlank()) {
                Text(
                    text = classLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Text(
                text = if (slot.level == 0) "Nível 0" else "Nível ${slot.level}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = DndArcaneBlue
            )
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
