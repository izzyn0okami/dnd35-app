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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dnd35.model.FeatGlossaryResult
import com.example.dnd35.model.FeatGlossarySearch

@Composable
fun FeatGlossaryDialog(
    initialQuery: String = "",
    onDismiss: () -> Unit
) {
    var query by remember(initialQuery) { mutableStateOf(initialQuery) }
    var selected by remember { mutableStateOf<FeatGlossaryResult?>(null) }
    val results = remember(query) { FeatGlossarySearch.search(query) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Glossário de talentos", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it; selected = null },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Pesquisar talento, pré-requisito ou benefício") }
                )
                Spacer(Modifier.height(8.dp))
                if (selected != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(onClick = { selected = null }) { Text("← Voltar aos resultados") }
                        Text(selected!!.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Pré-requisitos", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(selected!!.prerequisites.ifBlank { "Nenhum pré-requisito listado." })
                        Text("Resumo", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(selected!!.summary)
                        Text("Benefício", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(selected!!.benefit)
                        Text("Referência", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(selected!!.source, style = MaterialTheme.typography.bodySmall)
                    }
                } else if (query.isBlank()) {
                    Text(
                        "Pesquise no catálogo de talentos do Livro do Jogador. A busca considera nomes, pré-requisitos e benefícios.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (results.isEmpty()) {
                    Text("Nenhum talento encontrado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("${results.size} resultado(s)", style = MaterialTheme.typography.labelMedium)
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                        items(results, key = { it.name }) { result ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable { selected = result },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(result.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(
                                        result.prerequisites.ifBlank { "Sem pré-requisitos listados" },
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(result.summary, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}
