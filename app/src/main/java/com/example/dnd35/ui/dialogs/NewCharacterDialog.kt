package com.example.dnd35.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dnd35.model.Dnd35Rules

@Composable
fun NewCharacterDialog(
    onConfirm: (name: String, characterClass: String, race: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf(Dnd35Rules.CLASSES.first().name) }
    var selectedRace by remember { mutableStateOf(Dnd35Rules.RACES.first().name) }

    var classExpanded by remember { mutableStateOf(false) }
    var raceExpanded by remember { mutableStateOf(false) }

    val currentClassInfo = Dnd35Rules.CLASSES.find { it.name == selectedClass } ?: Dnd35Rules.CLASSES.first()
    val currentRaceInfo = Dnd35Rules.RACES.find { it.name == selectedRace } ?: Dnd35Rules.RACES.first()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Criar Novo Personagem", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do Aventureiro") },
                    placeholder = { Text("ex: Tordek, Lidda, Mialee") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Seletor de Classe
                Text("Classe do Livro do Jogador:", style = MaterialTheme.typography.labelMedium)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedClass,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { classExpanded = true }
                    )
                    DropdownMenu(
                        expanded = classExpanded,
                        onDismissRequest = { classExpanded = false }
                    ) {
                        for (c in Dnd35Rules.CLASSES) {
                            DropdownMenuItem(
                                text = { Text("${c.name} (DV: d${c.hitDie})") },
                                onClick = {
                                    selectedClass = c.name
                                    classExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "${currentClassInfo.description} Dado de Vida: d${currentClassInfo.hitDie}, BBA: ${currentClassInfo.babType.name}.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Seletor de Raça
                Text("Raça do Livro do Jogador:", style = MaterialTheme.typography.labelMedium)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedRace,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { raceExpanded = true }
                    )
                    DropdownMenu(
                        expanded = raceExpanded,
                        onDismissRequest = { raceExpanded = false }
                    ) {
                        for (r in Dnd35Rules.RACES) {
                            DropdownMenuItem(
                                text = { Text(r.name) },
                                onClick = {
                                    selectedRace = r.name
                                    raceExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        currentRaceInfo.traits,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        if (name.isBlank()) "Aventureiro" else name.trim(),
                        selectedClass,
                        selectedRace
                    )
                    onDismiss()
                }
            ) {
                Text("Criar Ficha")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
