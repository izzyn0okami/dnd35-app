package com.example.dnd35.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dnd35.model.CharacterClassEntry
import com.example.dnd35.model.CharacterCreatureBond
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.CharacterJsonAdapters
import com.example.dnd35.model.CreatureBondKind
import com.example.dnd35.model.CreatureBondRules
import com.example.dnd35.model.CreatureCatalog
import com.example.dnd35.ui.components.CreatureImageTradingCard
import com.example.dnd35.ui.components.CreatureCardStack
import com.example.dnd35.viewmodel.CharacterViewModel

@Composable
fun CreatureBondSection(
    character: CharacterEntity,
    classEntries: List<CharacterClassEntry>,
    viewModel: CharacterViewModel
) {
    val savedBonds = CharacterJsonAdapters.deserializeCreatureBonds(character.creatureBondsJson)
        .filter { CreatureBondRules.isValidBond(it) }
    val familiarBonds = savedBonds.filter { it.kind == CreatureBondKind.FAMILIAR }
    val companionBonds = savedBonds.filter { it.kind == CreatureBondKind.ANIMAL_COMPANION }
    val familiarOptions = remember(classEntries) { CreatureBondRules.familiarOptions(classEntries) }
    val companionOptions = remember(classEntries) { CreatureBondRules.animalCompanionOptions(classEntries) }

    var activeKind by remember { mutableStateOf<CreatureBondKind?>(null) }
    var showPicker by remember { mutableStateOf(false) }
    var includeAquatic by remember { mutableStateOf(false) }
    var bondPendingRemoval by remember { mutableStateOf<CharacterCreatureBond?>(null) }

    val bondsForActiveKind = when (activeKind) {
        CreatureBondKind.FAMILIAR -> familiarBonds
        CreatureBondKind.ANIMAL_COMPANION -> companionBonds
        null -> emptyList()
    }
    val options = when (activeKind) {
        CreatureBondKind.FAMILIAR -> familiarOptions.map { it to "" }
        CreatureBondKind.ANIMAL_COMPANION -> CreatureBondRules.animalCompanionOptions(
            classEntries,
            aquaticCampaign = includeAquatic
        ).map { option ->
            option.creature to option.restriction.ifBlank {
                if (option.effectiveLevelPenalty > 0) "Nível efetivo -${option.effectiveLevelPenalty}" else ""
            }
        }
        null -> emptyList()
    }

    if (savedBonds.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Familiares e companheiros", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Criaturas vinculadas ao personagem. Arraste a carta da frente para percorrer a pilha.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (familiarOptions.isNotEmpty() || familiarBonds.isNotEmpty()) {
                    OutlinedButton(onClick = { activeKind = CreatureBondKind.FAMILIAR; showPicker = true },
                        modifier = Modifier.fillMaxWidth()) {
                        Text(if (familiarBonds.isEmpty()) "Selecionar familiar" else "Adicionar outro familiar")
                    }
                }
                if (companionOptions.isNotEmpty() || companionBonds.isNotEmpty()) {
                    OutlinedButton(onClick = { activeKind = CreatureBondKind.ANIMAL_COMPANION; showPicker = true },
                        modifier = Modifier.fillMaxWidth()) {
                        Text(if (companionBonds.isEmpty()) "Selecionar companheiro animal" else "Adicionar outro companheiro")
                    }
                }
            }
        }
    } else if (familiarOptions.isNotEmpty() || companionOptions.isNotEmpty()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (familiarOptions.isNotEmpty()) {
                OutlinedButton(onClick = { activeKind = CreatureBondKind.FAMILIAR; showPicker = true }) {
                    Text("Vincular familiar")
                }
            }
            if (companionOptions.isNotEmpty()) {
                OutlinedButton(onClick = { activeKind = CreatureBondKind.ANIMAL_COMPANION; showPicker = true }) {
                    Text("Vincular companheiro")
                }
            }
        }
    }

    if (familiarBonds.isNotEmpty() || companionBonds.isNotEmpty()) {
        val linkedCards = familiarBonds.map { it to "Familiar" } +
            companionBonds.map { it to "Companheiro animal" }
        CreatureCardStack(
            items = linkedCards,
            key = { it.first.id },
            modifier = Modifier.fillMaxWidth()
        ) { (bond, label) ->
            CreatureCatalog.byId(bond.creatureId)?.let { creature ->
                CreatureImageTradingCard(
                    creature,
                    details = "$label vinculado ao personagem",
                    stats = "PV ${creature.hitPoints} • CA ${creature.armorClassText} • Iniciativa ${creature.initiative}",
                    selectLabel = "Remover vínculo",
                    onSelect = { bondPendingRemoval = bond }
                )
            }
        }
    }

    bondPendingRemoval?.let { bond ->
        val creatureName = CreatureCatalog.byId(bond.creatureId)?.name ?: "esta criatura"
        AlertDialog(
            onDismissRequest = { bondPendingRemoval = null },
            title = { Text("Remover vínculo?") },
            text = { Text("Deseja remover $creatureName deste personagem? A criatura será desvinculada, mas sua carta continuará no bestiário.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateCreatureBonds(savedBonds.filterNot { it.id == bond.id })
                    bondPendingRemoval = null
                }) { Text("Remover", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { bondPendingRemoval = null }) { Text("Cancelar") }
            }
        )
    }
    if (showPicker && activeKind != null) {
        CreatureDeckOverlay(
            title = if (activeKind == CreatureBondKind.FAMILIAR) "Escolher familiar" else "Escolher companheiro animal",
            subtitle = "Selecione uma carta disponível para a progressão atual",
            onDismiss = { showPicker = false }
        ) {
            if (activeKind == CreatureBondKind.ANIMAL_COMPANION) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Incluir opções aquáticas", style = MaterialTheme.typography.bodyMedium)
                    Checkbox(checked = includeAquatic, onCheckedChange = { includeAquatic = it })
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text("${options.size} criatura(s) elegível(is)", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (options.isEmpty()) {
                Text(
                    "Nenhuma criatura disponível para a progressão atual.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(options, key = { it.first.id }) { (creature, restriction) ->
                        CreatureImageTradingCard(
                            creature = creature,
                            details = "Disponível como ${if (activeKind == CreatureBondKind.FAMILIAR) "familiar" else "companheiro animal"}.",
                            stats = "PV ${creature.hitPoints} • CA ${creature.armorClassText} • Iniciativa ${creature.initiative}",
                            footer = restriction.ifBlank { null },
                            selectLabel = "Vincular criatura",
                            onSelect = {
                                val kind = activeKind ?: return@CreatureImageTradingCard
                                viewModel.updateCreatureBonds(savedBonds + CharacterCreatureBond(kind = kind, creatureId = creature.id))
                                showPicker = false
                            }
                        )
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { showPicker = false }) { Text("Cancelar") }
            }
        }
    }
}

@Composable
private fun BondActionButton(
    title: String,
    current: List<CharacterCreatureBond>,
    onClick: () -> Unit
) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(if (current.isEmpty()) "Selecionar $title" else "$title atual (${current.size})")
    }
}
