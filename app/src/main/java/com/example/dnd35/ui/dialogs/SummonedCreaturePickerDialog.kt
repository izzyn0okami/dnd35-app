package com.example.dnd35.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dnd35.model.SummonCreatureOption
import com.example.dnd35.model.SpellbookRules

@Composable
fun SummonedCreaturePickerDialog(
    spellName: String,
    options: List<SummonCreatureOption>,
    onSelect: (SummonCreatureOption) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Escolher criatura — $spellName") },
        text = {
            Column {
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
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 440.dp)
                ) {
                    items(options, key = { "${it.creature.id}-${it.sourceCircle}-${it.templateLabel}" }) { option ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { onSelect(option) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(option.creature.name, fontWeight = FontWeight.Bold)
                                val variant = option.templateLabel.takeIf { it.isNotBlank() }?.let { " • Modelo/variante: $it" }.orEmpty()
                                Text(
                                    "Lista de ${SpellbookRules.circleLabel(option.sourceCircle)}$variant",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "${option.creature.type} • ${option.creature.size} • ND ${option.creature.challengeRating} • PV ${option.creature.hitPoints} • CA ${option.creature.armorClass}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
