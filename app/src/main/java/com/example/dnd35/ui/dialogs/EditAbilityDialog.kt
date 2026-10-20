package com.example.dnd35.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.dnd35.model.Dnd35Rules

@Composable
fun EditAbilityDialog(
    abilityName: String,
    currentBase: Int,
    currentMagicBonus: Int,
    racialBonus: Int,
    onSave: (base: Int, magic: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var baseText by remember { mutableStateOf("$currentBase") }
    var magicText by remember { mutableStateOf("$currentMagicBonus") }

    val baseVal = baseText.toIntOrNull() ?: currentBase
    val magicVal = magicText.toIntOrNull() ?: currentMagicBonus
    val totalScore = baseVal + racialBonus + magicVal
    val modifier = Dnd35Rules.calculateModifier(totalScore)
    val modStr = if (modifier >= 0) "+$modifier" else "$modifier"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustar $abilityName", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Cartão com o resumo do cálculo
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Total Final: ", style = MaterialTheme.typography.titleMedium)
                            Text("$totalScore", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Modificador: ", style = MaterialTheme.typography.titleMedium)
                            Text(modStr, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                        Text(
                            "Base ($baseVal) + Raça ($racialBonus) + Mágico/Item ($magicVal)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = baseText,
                    onValueChange = { baseText = it },
                    label = { Text("Valor Base (ex: 10 a 18)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = magicText,
                    onValueChange = { magicText = it },
                    label = { Text("Bônus Mágico / Itens / Outros") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(baseVal, magicVal)
                onDismiss()
            }) {
                Text("Salvar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
