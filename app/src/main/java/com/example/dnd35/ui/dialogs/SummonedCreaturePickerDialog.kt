package com.example.dnd35.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dnd35.model.SummonCreatureOption
import com.example.dnd35.model.SpellbookRules
import com.example.dnd35.ui.components.CreatureDeckOverlay
import com.example.dnd35.ui.components.CreatureImageTradingCard
import com.example.dnd35.ui.components.CreatureCardStack

@Composable
fun SummonedCreaturePickerDialog(
    spellName: String,
    options: List<SummonCreatureOption>,
    onSelect: (SummonCreatureOption) -> Unit,
    onDismiss: () -> Unit
) {
    CreatureDeckOverlay(
        title = "Escolher criatura",
        subtitle = spellName,
        onDismiss = onDismiss
    ) {
        Text(
            "As listas inferiores também são válidas para este círculo. Selecione a carta que será invocada.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Quando houver modelo Celestial ou Abissal, a variante será registrada; os ajustes completos do modelo ainda não são aplicados automaticamente à ficha de combate.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.tertiary
        )
        Text(
            "${options.size} opção(ões) disponíveis",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (options.isEmpty()) {
            Text("Nenhuma criatura disponível para esta magia.")
        } else {
            CreatureCardStack(
                items = options,
                key = { "${it.creature.id}-${it.sourceCircle}-${it.templateLabel}" },
                modifier = Modifier.fillMaxWidth()
            ) { option ->
                val variant = option.templateLabel.takeIf { it.isNotBlank() }
                    ?.let { "Modelo/variante: $it" }
                CreatureImageTradingCard(
                    creature = option.creature,
                    details = "Lista de ${SpellbookRules.circleLabel(option.sourceCircle)}",
                    stats = "PV ${option.creature.hitPoints}  •  CA ${option.creature.armorClass}  •  Iniciativa ${option.creature.initiative}",
                    footer = variant,
                    selectLabel = "Invocar",
                    onSelect = { onSelect(option) }
                )
            } {
                items(options, key = { "${it.creature.id}-${it.sourceCircle}-${it.templateLabel}" }) { option ->
                    val variant = option.templateLabel.takeIf { it.isNotBlank() }
                        ?.let { "Modelo/variante: $it" }
                    CreatureImageTradingCard(
                        creature = option.creature,
                        details = "Lista de ${SpellbookRules.circleLabel(option.sourceCircle)}",
                        stats = "PV ${option.creature.hitPoints} • CA ${option.creature.armorClassText} • Iniciativa ${option.creature.initiative}",
                        footer = variant,
                        selectLabel = "Invocar criatura",
                        onSelect = { onSelect(option) }
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End) {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    }
}
