package com.example.dnd35.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dnd35.model.CharacterFeat
import com.example.dnd35.model.Dnd35Rules

@Composable
fun AddEditFeatDialog(
    featToEdit: CharacterFeat? = null,
    onConfirm: (CharacterFeat) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(if (featToEdit == null) 0 else 1) }

    var name by remember { mutableStateOf(featToEdit?.name ?: "") }
    var prerequisites by remember { mutableStateOf(featToEdit?.prerequisites ?: "") }
    var summary by remember { mutableStateOf(featToEdit?.summary ?: "") }
    var description by remember { mutableStateOf(featToEdit?.description ?: "") }

    var catalogQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (featToEdit == null) "Adicionar Talento" else "Editar Talento",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (featToEdit == null) {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Catálogo PHB") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Personalizado") }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (selectedTab == 0 && featToEdit == null) {
                    OutlinedTextField(
                        value = catalogQuery,
                        onValueChange = { catalogQuery = it },
                        placeholder = { Text("Buscar talentos do Livro do Jogador...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val filtered = Dnd35Rules.PRESET_FEATS.filter {
                        it.name.contains(catalogQuery, ignoreCase = true) ||
                        it.summary.contains(catalogQuery, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        items(filtered) { preset ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        val newFeat = CharacterFeat(
                                            name = preset.name,
                                            prerequisites = preset.prerequisites,
                                            summary = preset.summary,
                                            description = preset.benefit
                                        )
                                        onConfirm(newFeat)
                                        onDismiss()
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(preset.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        if (preset.prerequisites.isNotBlank()) {
                                            Text(
                                                "Pré-requisito: ${preset.prerequisites}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(
                                            preset.summary,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(Icons.Default.Add, contentDescription = "Adicionar", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nome do Talento") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = prerequisites,
                            onValueChange = { prerequisites = it },
                            label = { Text("Pré-requisitos") },
                            placeholder = { Text("ex: FOR 13, BBA +1") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = summary,
                            onValueChange = { summary = it },
                            label = { Text("Resumo Rápido") },
                            placeholder = { Text("ex: Subtrai do ataque para somar no dano") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Benefício Completo / Regras") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTab == 1 || featToEdit != null) {
                Button(
                    onClick = {
                        val finalFeat = (featToEdit ?: CharacterFeat(name = name)).copy(
                            name = if (name.isBlank()) "Novo Talento" else name.trim(),
                            prerequisites = prerequisites,
                            summary = summary,
                            description = description
                        )
                        onConfirm(finalFeat)
                        onDismiss()
                    }
                ) {
                    Text("Salvar")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
