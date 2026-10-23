package com.example.dnd35.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.dnd35.model.CharacterSpell
import com.example.dnd35.model.Dnd35Rules
import com.example.ui.theme.DndArcaneBlue

@Composable
fun AddEditSpellDialog(
    spellToEdit: CharacterSpell? = null,
    onConfirm: (CharacterSpell) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(if (spellToEdit == null) 0 else 1) }

    // Campos da magia
    var name by remember { mutableStateOf(spellToEdit?.name ?: "") }
    var levelText by remember { mutableStateOf("${spellToEdit?.level ?: 0}") }
    val parsedLevel = levelText.toIntOrNull()
    var school by remember { mutableStateOf(spellToEdit?.school ?: "Evocação") }
    var castTime by remember { mutableStateOf(spellToEdit?.castTime ?: "1 ação padrão") }
    var components by remember { mutableStateOf(spellToEdit?.components ?: "V, S") }
    var range by remember { mutableStateOf(spellToEdit?.range ?: "Médio (30m + 3m/nível)") }
    var duration by remember { mutableStateOf(spellToEdit?.duration ?: "Instantânea") }
    var savingThrow by remember { mutableStateOf(spellToEdit?.savingThrow ?: "Nenhum") }
    var spellResistance by remember { mutableStateOf(spellToEdit?.spellResistance ?: true) }
    var description by remember { mutableStateOf(spellToEdit?.description ?: "") }

    // Filtros de busca no catálogo PHB
    var catalogQuery by remember { mutableStateOf("") }
    var selectedClassFilter by remember { mutableStateOf("Todas") }
    var selectedSchoolFilter by remember { mutableStateOf("Todas") }
    var selectedLevelFilter by remember { mutableStateOf<Int?>(null) } // null = Todos

    val classesOptions = listOf("Todas", "Mago", "Feiticeiro", "Clérigo", "Druida", "Bardo", "Paladino", "Ranger")
    val schoolsOptions = listOf("Todas", "Abjuração", "Adivinhação", "Conjuração", "Encantamento", "Evocação", "Ilusão", "Necromancia", "Transmutação")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (spellToEdit == null) "Adicionar Magia ao Grimório" else "Editar Magia",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (spellToEdit == null) {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Catálogo do Livro do Jogador (3.5)") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Personalizada") }
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (selectedTab == 0 && spellToEdit == null) {
                    // Campo de pesquisa por texto
                    OutlinedTextField(
                        value = catalogQuery,
                        onValueChange = { catalogQuery = it },
                        placeholder = { Text("Buscar magia por nome ou efeito...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Filtros por Círculo (Nível)
                    Text("Filtrar por Círculo:", style = MaterialTheme.typography.labelSmall)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedLevelFilter == null,
                                onClick = { selectedLevelFilter = null },
                                label = { Text("Todos") }
                            )
                        }
                        items((0..9).toList()) { lvl ->
                            FilterChip(
                                selected = selectedLevelFilter == lvl,
                                onClick = { selectedLevelFilter = if (selectedLevelFilter == lvl) null else lvl },
                                label = { Text(if (lvl == 0) "Nv 0" else "$lvl") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Filtro por Classe Conjuradora
                    Text("Classe Conjuradora:", style = MaterialTheme.typography.labelSmall)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(classesOptions) { cls ->
                            FilterChip(
                                selected = selectedClassFilter == cls,
                                onClick = { selectedClassFilter = cls },
                                label = { Text(cls) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Filtro por Escola de Magia
                    Text("Escola:", style = MaterialTheme.typography.labelSmall)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(schoolsOptions) { sch ->
                            FilterChip(
                                selected = selectedSchoolFilter == sch,
                                onClick = { selectedSchoolFilter = sch },
                                label = { Text(sch) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Lista Filtrada do Catálogo PHB
                    val filtered = Dnd35Rules.PRESET_SPELLS.filter { spell ->
                        val matchesQuery = spell.name.contains(catalogQuery, ignoreCase = true) ||
                                spell.description.contains(catalogQuery, ignoreCase = true)
                        val matchesLevel = selectedLevelFilter == null || (if (selectedClassFilter == "Todas") spell.level else spell.classLevels[selectedClassFilter] ?: spell.level) == selectedLevelFilter
                        val matchesClass = selectedClassFilter == "Todas" || spell.classLevels.keys.any { it.equals(selectedClassFilter, ignoreCase = true) }
                        val matchesSchool = selectedSchoolFilter == "Todas" || spell.school.equals(selectedSchoolFilter, ignoreCase = true)
                        matchesQuery && matchesLevel && matchesClass && matchesSchool
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    ) {
                        items(filtered) { preset ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        val newSpell = CharacterSpell(
                                            name = preset.name,
                                            level = if (selectedClassFilter == "Todas") preset.level else preset.classLevels[selectedClassFilter] ?: preset.level,
                                            school = preset.school,
                                            castTime = preset.castTime,
                                            components = preset.components,
                                            range = preset.range,
                                            duration = preset.duration,
                                            savingThrow = preset.savingThrow,
                                            spellResistance = preset.spellResistance,
                                            prepared = true,
                                            description = preset.description
                                        )
                                        onConfirm(newSpell)
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
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = DndArcaneBlue),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = if (preset.level == 0) "Nv 0" else "Nv ${preset.level}",
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                preset.name,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }

                                        Text(
                                            "${preset.school} • Classes: ${preset.classes.joinToString(", ") { cls -> "$cls (Nv ${preset.classLevels[cls] ?: preset.level})" }}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "Alcance: ${preset.range} • Duração: ${preset.duration}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            preset.description,
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
                    // Formulário Customizado
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nome da Magia") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = levelText,
                                onValueChange = { levelText = it },
                                label = { Text("Círculo (0 a 9)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            OutlinedTextField(
                                value = school,
                                onValueChange = { school = it },
                                label = { Text("Escola") },
                                placeholder = { Text("Evocação, Ilusão") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = castTime,
                                onValueChange = { castTime = it },
                                label = { Text("Tempo de Conjuração") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            OutlinedTextField(
                                value = components,
                                onValueChange = { components = it },
                                label = { Text("Componentes") },
                                placeholder = { Text("V, S, M, DF") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = range,
                                onValueChange = { range = it },
                                label = { Text("Alcance") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            OutlinedTextField(
                                value = savingThrow,
                                onValueChange = { savingThrow = it },
                                label = { Text("Resistência") },
                                placeholder = { Text("Vontade anula") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = spellResistance, onCheckedChange = { spellResistance = it })
                            Text("Resistência a Magia (RM) se aplica", style = MaterialTheme.typography.bodyMedium)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Efeitos e Descrição da Magia") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTab == 1 || spellToEdit != null) {
                Button(
                    onClick = {
                        val lvl = parsedLevel?.takeIf { it in 0..9 } ?: return@Button
                        val finalSpell = (spellToEdit ?: CharacterSpell(name = name)).copy(
                            name = if (name.isBlank()) "Nova Magia" else name.trim(),
                            level = lvl,
                            school = school,
                            castTime = castTime,
                            components = components,
                            range = range,
                            duration = duration,
                            savingThrow = savingThrow,
                            spellResistance = spellResistance,
                            description = description
                        )
                        onConfirm(finalSpell)
                        onDismiss()
                    },
                    enabled = name.isNotBlank() && parsedLevel != null && parsedLevel in 0..9
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
