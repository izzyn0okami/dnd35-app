package com.example.dnd35.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.CharacterJsonAdapters
import com.example.dnd35.model.CreatureCatalog
import com.example.dnd35.model.CreatureDefinition
import com.example.dnd35.model.SpellCastRecord
import com.example.dnd35.viewmodel.CharacterViewModel

/**
 * Invocações persistentes da ficha. A duração em rodadas é controlada pela mesa;
 * o app não usa tempo real para expirar efeitos que dependem de turnos.
 */
@Composable
fun ActiveSummonsSection(
    character: CharacterEntity,
    viewModel: CharacterViewModel
) {
    val activeSummons = remember(character.spellHistoryJson) {
        CharacterJsonAdapters.deserializeSpellHistory(character.spellHistoryJson)
            .filter { it.isActiveSummon }
            .sortedBy { it.timestamp }
    }
    if (activeSummons.isEmpty()) return

    var selectedCreature by remember { mutableStateOf<CreatureDefinition?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("active_summons_section"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                if (activeSummons.size == 1) "Invocação ativa" else "Invocações ativas (${activeSummons.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                "As criaturas permanecem aqui até serem dispensadas. Confira a duração da magia em rodadas na mesa.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            activeSummons.forEach { record ->
                ActiveSummonCard(
                    record = record,
                    onOpen = { selectedCreature = CreatureCatalog.byId(record.summonedCreatureId) },
                    onDismiss = { viewModel.dismissSummon(record.id) }
                )
            }
        }
    }

    selectedCreature?.let { creature ->
        CreatureDetailsOverlay(creature = creature, onDismiss = { selectedCreature = null })
    }
}

@Composable
private fun ActiveSummonCard(
    record: SpellCastRecord,
    onOpen: () -> Unit,
    onDismiss: () -> Unit
) {
    val creature = CreatureCatalog.byId(record.summonedCreatureId)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (creature != null) {
                    CreatureImage(
                        creature = creature,
                        modifier = Modifier.size(88.dp).clip(RoundedCornerShape(10.dp))
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        creature?.name ?: record.summonedCreatureId,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    val subtitle = buildList {
                        add(record.spellName)
                        if (record.casterClass.isNotBlank()) add(record.casterClass)
                        if (record.summonedCreatureVariant.isNotBlank()) add(record.summonedCreatureVariant)
                    }.joinToString(" • ")
                    Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    if (creature != null) {
                        val initiative = if (creature.initiative >= 0) "+${creature.initiative}" else creature.initiative.toString()
                        Text(
                            "PV ${creature.hitPoints} • CA ${creature.armorClassText} • Iniciativa $initiative • Deslocamento ${creature.speed}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text("Ficha da criatura não encontrada no bestiário.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = onDismiss) { Text("Dispensar") }
            }
        }
    }
}
