package com.example.dnd35.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.dnd35.model.CreatureDefinition
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
    var showCurrent by remember { mutableStateOf(false) }
    var showPicker by remember { mutableStateOf(false) }
    var includeAquatic by remember { mutableStateOf(false) }

    val bondsForActiveKind = when (activeKind) {
        CreatureBondKind.FAMILIAR -> familiarBonds
        CreatureBondKind.ANIMAL_COMPANION -> companionBonds
        null -> emptyList()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Familiares e companheiros", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Criaturas vinculadas ao personagem. A seleção respeita as classes e os níveis atuais.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (familiarOptions.isNotEmpty() || familiarBonds.isNotEmpty()) {
                BondActionButton(
                    title = "Familiar",
                    current = familiarBonds,
                    onClick = {
                        activeKind = CreatureBondKind.FAMILIAR
                        if (familiarBonds.isEmpty()) showPicker = true else showCurrent = true
                    }
                )
            }
            if (companionOptions.isNotEmpty() || companionBonds.isNotEmpty()) {
                BondActionButton(
                    title = "Companheiro animal",
                    current = companionBonds,
                    onClick = {
                        activeKind = CreatureBondKind.ANIMAL_COMPANION
                        if (companionBonds.isEmpty()) showPicker = true else showCurrent = true
                    }
                )
            }
            if (familiarOptions.isEmpty() && companionOptions.isEmpty() &&
                familiarBonds.isEmpty() && companionBonds.isEmpty()
            ) {
                Text(
                    "Nenhuma classe atual concede familiar ou companheiro animal.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showCurrent && activeKind != null) {
        AlertDialog(
            onDismissRequest = { showCurrent = false },
            title = { Text(if (activeKind == CreatureBondKind.FAMILIAR) "Familiares atuais" else "Companheiros animais atuais") },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (bondsForActiveKind.isEmpty()) {
                        item { Text("Nenhuma criatura vinculada.") }
                    } else {
                        items(bondsForActiveKind, key = { it.id }) { bond ->
                            val creature = CreatureCatalog.byId(bond.creatureId)
                            if (creature != null) CreatureSummary(creature)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showCurrent = false
                    showPicker = true
                }) { Text("Adicionar outra") }
            },
            dismissButton = {
                TextButton(onClick = { showCurrent = false }) { Text("Fechar") }
            }
        )
    }

    if (showPicker && activeKind != null) {
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
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text(if (activeKind == CreatureBondKind.FAMILIAR) "Escolher familiar" else "Escolher companheiro animal") },
            text = {
                Column {
                    if (activeKind == CreatureBondKind.ANIMAL_COMPANION) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Incluir opções aquáticas", style = MaterialTheme.typography.bodySmall)
                            Checkbox(checked = includeAquatic, onCheckedChange = { includeAquatic = it })
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    LazyColumn(modifier = Modifier.height(360.dp)) {
                        items(options, key = { it.first.id }) { (creature, restriction) ->
                            Column(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    val kind = activeKind ?: return@clickable
                                    val updated = savedBonds + CharacterCreatureBond(kind = kind, creatureId = creature.id)
                                    viewModel.updateCreatureBonds(updated)
                                    showPicker = false
                                }.padding(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                Text(creature.name, fontWeight = FontWeight.Bold)
                                Text(
                                    "${creature.type} • ${creature.size} • ND ${creature.challengeRating}" +
                                        if (restriction.isNotBlank()) " • $restriction" else "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (options.isEmpty()) {
                            item { Text("Nenhuma criatura disponível para a progressão atual.") }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPicker = false }) { Text("Cancelar") } }
        )
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

@Composable
private fun CreatureSummary(creature: CreatureDefinition) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(creature.name, fontWeight = FontWeight.Bold)
            Text(
                "${creature.type} • ${creature.size} • ND ${creature.challengeRating}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "PV ${creature.hitPoints} • CA ${creature.armorClassText} • Iniciativa ${creature.initiative}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
