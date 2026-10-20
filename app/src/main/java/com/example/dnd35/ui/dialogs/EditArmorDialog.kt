package com.example.dnd35.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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

@Composable
fun EditArmorDialog(
    character: CharacterEntity,
    onSave: (
        armor: Int,
        shield: Int,
        natural: Int,
        deflection: Int,
        miscAc: Int,
        miscFort: Int,
        miscRef: Int,
        miscWill: Int,
        miscInit: Int
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var naturalText by remember { mutableStateOf("${character.naturalArmor}") }
    var deflectionText by remember { mutableStateOf("${character.deflectionBonus}") }
    var miscAcText by remember { mutableStateOf("${character.miscAcBonus}") }

    var miscFortText by remember { mutableStateOf("${character.miscFortitude}") }
    var miscRefText by remember { mutableStateOf("${character.miscReflex}") }
    var miscWillText by remember { mutableStateOf("${character.miscWill}") }
    var miscInitText by remember { mutableStateOf("${character.miscInitiative}") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustar Defesas & Bônus Mágicos", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Bônus Especiais de CA:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = naturalText,
                        onValueChange = { naturalText = it },
                        label = { Text("Armadura Natural") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    OutlinedTextField(
                        value = deflectionText,
                        onValueChange = { deflectionText = it },
                        label = { Text("Deflexão (Anéis)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = miscAcText,
                    onValueChange = { miscAcText = it },
                    label = { Text("Outros Bônus de CA (Esquiva, etc.)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Bônus Diversos de Resistências & Iniciativa:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = miscFortText,
                        onValueChange = { miscFortText = it },
                        label = { Text("Fortitude Extra") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    OutlinedTextField(
                        value = miscRefText,
                        onValueChange = { miscRefText = it },
                        label = { Text("Reflexos Extra") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = miscWillText,
                        onValueChange = { miscWillText = it },
                        label = { Text("Vontade Extra") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    OutlinedTextField(
                        value = miscInitText,
                        onValueChange = { miscInitText = it },
                        label = { Text("Iniciativa Extra") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        character.armorBonus,
                        character.shieldBonus,
                        naturalText.toIntOrNull() ?: 0,
                        deflectionText.toIntOrNull() ?: 0,
                        miscAcText.toIntOrNull() ?: 0,
                        miscFortText.toIntOrNull() ?: 0,
                        miscRefText.toIntOrNull() ?: 0,
                        miscWillText.toIntOrNull() ?: 0,
                        miscInitText.toIntOrNull() ?: 0
                    )
                    onDismiss()
                }
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
