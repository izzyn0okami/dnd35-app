package com.example.dnd35.ui.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dnd35.model.CalculatedCharacterStats
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.CharacterFeat
import com.example.dnd35.model.FeatPrerequisiteChecker
import com.example.dnd35.model.FeatPrerequisiteStatus
import com.example.dnd35.ui.dialogs.AddEditFeatDialog
import com.example.dnd35.viewmodel.CharacterViewModel
import com.example.ui.theme.DndCrimson
import com.example.ui.theme.DndGold

@Composable
fun FeatsTab(
    viewModel: CharacterViewModel,
    stats: CalculatedCharacterStats,
    character: CharacterEntity
) {
    var showAddFeatModal by remember { mutableStateOf(false) }
    var featToEdit by remember { mutableStateOf<CharacterFeat?>(null) }
    var pendingFeatDeleteId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("feats_tab")
    ) {
        // Banner Informativo
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = DndGold, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "Talentos e habilidades especiais",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Catálogo oficial do Livro do Jogador (D&D 3.5)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Barra de Ação "+ Adicionar Talento"
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Talentos Registrados (${stats.feats.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { showAddFeatModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adicionar Talento")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Lista de Talentos
        if (stats.feats.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Nenhum talento registrado.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Toque em '+ Adicionar Talento' para buscar no catálogo do Livro do Jogador (Ataque Poderoso, Foco em Arma, etc.) ou criar um personalizado.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(stats.feats) { feat ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                feat.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row {
                                IconButton(onClick = { featToEdit = feat }, modifier = Modifier.size(48.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar ${feat.name}", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { pendingFeatDeleteId = feat.id }, modifier = Modifier.size(48.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir ${feat.name}", modifier = Modifier.size(18.dp), tint = DndCrimson)
                                }
                            }
                        }

                        if (feat.prerequisites.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Pré-requisitos: ${feat.prerequisites}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            val assessment = FeatPrerequisiteChecker.assess(feat.prerequisites, stats)
                            Text(
                                assessment.message,
                                style = MaterialTheme.typography.labelSmall,
                                color = when (assessment.status) {
                                    FeatPrerequisiteStatus.MET -> MaterialTheme.colorScheme.primary
                                    FeatPrerequisiteStatus.NOT_MET -> MaterialTheme.colorScheme.error
                                    FeatPrerequisiteStatus.PARTIAL -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }

                        if (feat.summary.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                feat.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (feat.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                feat.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAddFeatModal) {
        AddEditFeatDialog(
            featToEdit = null,
            stats = stats,
            onConfirm = { newFeat -> viewModel.addFeat(newFeat) },
            onDismiss = { showAddFeatModal = false }
        )
    }

    pendingFeatDeleteId?.let { featId ->
        AlertDialog(
            onDismissRequest = { pendingFeatDeleteId = null },
            title = { Text("Excluir talento?") },
            text = { Text("Esta ação não pode ser desfeita.") },
            confirmButton = { Button(onClick = { viewModel.removeFeat(featId); pendingFeatDeleteId = null }) { Text("Excluir") } },
            dismissButton = { androidx.compose.material3.OutlinedButton(onClick = { pendingFeatDeleteId = null }) { Text("Cancelar") } }
        )
    }

    featToEdit?.let { feat ->
        AddEditFeatDialog(
            featToEdit = feat,
            stats = stats,
            onConfirm = { updated ->
                viewModel.removeFeat(feat.id)
                viewModel.addFeat(updated)
                featToEdit = null
            },
            onDismiss = { featToEdit = null }
        )
    }
}
