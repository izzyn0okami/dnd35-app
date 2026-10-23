package com.example.dnd35.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.CharacterJsonAdapters
import com.example.dnd35.model.Dnd35Rules

@Composable
fun EditGeneralInfoDialog(
    character: CharacterEntity,
    onSave: (
        name: String,
        characterClass: String,
        race: String,
        level: Int,
        alignment: String,
        deity: String,
        size: String
    ) -> Unit,
    onOpenMulticlass: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(character.name) }
    var selectedClass by remember { mutableStateOf(character.characterClass) }
    var selectedRace by remember { mutableStateOf(character.race) }
    var levelText by remember { mutableStateOf("${character.level}") }
    val parsedLevel = levelText.toIntOrNull()
    val isMulticlass = remember(character.multiclassJson, character.characterClass, character.level) {
        CharacterJsonAdapters.deserializeClassEntries(character.multiclassJson, character.characterClass, character.level).size > 1
    }
    var alignment by remember { mutableStateOf(character.alignment) }
    var deity by remember { mutableStateOf(character.deity) }
    var size by remember { mutableStateOf(character.size) }

    var classExpanded by remember { mutableStateOf(false) }
    var raceExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Dados do Personagem", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = levelText,
                        onValueChange = { levelText = it },
                        label = { Text("Nível") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = !isMulticlass,
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    OutlinedTextField(
                        value = size,
                        onValueChange = { size = it },
                        label = { Text("Tamanho") },
                        placeholder = { Text("Médio") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Classe
                Text("Classe:", style = MaterialTheme.typography.labelSmall)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedClass,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isMulticlass) { classExpanded = true },
                        enabled = !isMulticlass
                    )
                    DropdownMenu(
                        expanded = classExpanded,
                        onDismissRequest = { classExpanded = false }
                    ) {
                        for (c in Dnd35Rules.CLASSES) {
                            DropdownMenuItem(
                                text = { Text(c.name) },
                                onClick = {
                                    selectedClass = c.name
                                    classExpanded = false
                                }
                            )
                        }
                    }
                }

                if (isMulticlass) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Este personagem tem mais de uma classe. Para alterar classes ou níveis, use “Gerenciar multiclasse e níveis”.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (onOpenMulticlass != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenMulticlass()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.padding(horizontal = 3.dp))
                        Text("Gerenciar multiclasse e níveis")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Raça
                Text("Raça:", style = MaterialTheme.typography.labelSmall)
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

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = alignment,
                    onValueChange = { alignment = it },
                    label = { Text("Tendência") },
                    placeholder = { Text("ex: Leal e Bom") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = deity,
                    onValueChange = { deity = it },
                    label = { Text("Divindade") },
                    placeholder = { Text("ex: Pelor, Moradin, Kord") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lvl = parsedLevel?.takeIf { it >= 1 } ?: return@Button
                    onSave(name, selectedClass, selectedRace, lvl, alignment, deity, size)
                    onDismiss()
                },
                enabled = parsedLevel != null && parsedLevel >= 1
            ) {
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
