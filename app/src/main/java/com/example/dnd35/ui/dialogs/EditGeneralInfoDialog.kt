package com.example.dnd35.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.Dnd35Rules

internal fun normalizeEditedCharacterName(name: String): String? =
    name.trim().takeIf { it.isNotEmpty() }

@Composable
fun EditGeneralInfoDialog(
    character: CharacterEntity,
    onSave: (
        name: String,
        race: String,
        alignment: String,
        deity: String,
        size: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(character.name) }
    var nameTouched by remember { mutableStateOf(character.name.isBlank()) }
    val normalizedName = normalizeEditedCharacterName(name)
    val showNameError = nameTouched && normalizedName == null
    var selectedRace by remember { mutableStateOf(character.race) }
    var alignment by remember { mutableStateOf(character.alignment) }
    var deity by remember { mutableStateOf(character.deity) }
    var size by remember { mutableStateOf(character.size) }

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
                    onValueChange = {
                        name = it
                        nameTouched = true
                    },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = showNameError,
                    supportingText = if (showNameError) {
                        { Text("Informe um nome para o personagem.") }
                    } else null
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = size,
                    onValueChange = { size = it },
                    label = { Text("Tamanho") },
                    placeholder = { Text("Médio") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

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
                    val validName = normalizeEditedCharacterName(name)
                    if (validName == null) {
                        nameTouched = true
                        return@Button
                    }
                    onSave(validName, selectedRace, alignment, deity, size)
                    onDismiss()
                },
                enabled = normalizedName != null
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
