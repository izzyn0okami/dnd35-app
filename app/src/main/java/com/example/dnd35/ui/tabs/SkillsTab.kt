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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.dnd35.model.CalculatedCharacterStats
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.CharacterJsonAdapters
import com.example.dnd35.model.ComputedSkill
import com.example.dnd35.model.estimateSkillPointsForLevel
import com.example.dnd35.ui.dialogs.DiceRollDialog
import com.example.dnd35.viewmodel.CharacterViewModel
import com.example.dnd35.viewmodel.isValidEditedSkillRanks
import com.example.ui.theme.DndCrimson
import com.example.ui.theme.DndGold
import com.example.ui.theme.DndHpGreen

@Composable
fun SkillsTab(
    viewModel: CharacterViewModel,
    stats: CalculatedCharacterStats,
    character: CharacterEntity
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableIntStateOf(0) } // 0: Todas, 1: De Classe, 2: Com Graduação
    var editingSkill by remember { mutableStateOf<ComputedSkill?>(null) }
    var diceRollTarget by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var craftSpecialtyInput by remember { mutableStateOf("") }
    var craftSpecialtyMessage by remember { mutableStateOf<String?>(null) }

    val maxClassRank = stats.totalCharacterLevel + 3
    val maxCrossRank = maxClassRank / 2.0f
    val totalSkillPointsSpent = stats.computedSkills.sumOf {
        it.ranks.toDouble() * if (it.isClassSkill) 1.0 else 2.0
    }

    val filteredSkills = stats.computedSkills.filter {
        val matchesQuery = it.skill.name.contains(searchQuery, ignoreCase = true) ||
                it.skill.keyAbility.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (filterType) {
            1 -> it.isClassSkill
            2 -> it.ranks > 0f
            else -> true
        }
        matchesQuery && matchesFilter
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("skills_tab")
    ) {
        item {
            var languageInput by remember(character.id) { mutableStateOf("") }
            val languages = CharacterJsonAdapters.deserializeLanguages(character.languagesJson)
            Card(
                modifier = Modifier.fillMaxWidth().testTag("languages_section"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Idiomas conhecidos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Falar Idioma é uma habilidade especial: idiomas não recebem graduações nem testes de perícia.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = languageInput,
                            onValueChange = { languageInput = it },
                            label = { Text("Adicionar idioma") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val cleaned = languageInput.trim()
                                if (cleaned.isNotEmpty() && languages.none { it.equals(cleaned, ignoreCase = true) }) {
                                    viewModel.updateLanguages(languages + cleaned)
                                }
                                languageInput = ""
                            },
                            enabled = languageInput.isNotBlank()
                        ) { Text("Adicionar") }
                    }
                    if (languages.isEmpty()) {
                        Text("Nenhum idioma cadastrado.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        languages.forEach { language ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(language, modifier = Modifier.weight(1f))
                                IconButton(onClick = { viewModel.updateLanguages(languages.filterNot { it == language }) }) {
                                    Icon(Icons.Default.Remove, contentDescription = "Remover idioma $language")
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Ofícios é uma categoria: cada especialidade possui graduações independentes.
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("craft_specialties_section"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Especialidades de Ofícios", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Cada especialidade é uma perícia independente. Digite o nome da especialidade para adicioná-la à ficha.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = craftSpecialtyInput,
                            onValueChange = {
                                craftSpecialtyInput = it
                                craftSpecialtyMessage = null
                            },
                            label = { Text("Nova especialidade (ex.: Armas)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val entered = craftSpecialtyInput.trim()
                                val name = if (entered.startsWith("Ofícios (", ignoreCase = true) && entered.endsWith(")")) {
                                    com.example.dnd35.model.Dnd35Rules.canonicalSkillName(entered)
                                } else {
                                    com.example.dnd35.model.Dnd35Rules.canonicalSkillName("Ofícios ($entered)")
                                }
                                when {
                                    entered.isBlank() -> {
                                        craftSpecialtyMessage = "Digite o nome da especialidade."
                                    }
                                    name.substringAfter("(", "").removeSuffix(")").isBlank() -> {
                                        craftSpecialtyMessage = "O nome da especialidade não pode ficar vazio."
                                    }
                                    stats.computedSkills.any { it.skill.name.equals(name, ignoreCase = true) } -> {
                                        craftSpecialtyMessage = "$name já existe nesta ficha."
                                    }
                                    else -> {
                                        viewModel.updateSkill(name, 0f, 0, false)
                                        craftSpecialtyMessage = "$name adicionado à ficha."
                                        craftSpecialtyInput = ""
                                    }
                                }
                            },
                            enabled = craftSpecialtyInput.isNotBlank()
                        ) { Text("Salvar ofício") }
                    }
                    craftSpecialtyMessage?.let { message ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Histórico explícito de orçamento de pontos de perícia
        item {
            var levelInput by remember(character.id) { mutableStateOf("") }
            var classInput by remember(character.id) { mutableStateOf("") }
            var availableInput by remember(character.id) { mutableStateOf("") }
            var allocationsInput by remember(character.id) { mutableStateOf("") }
            val history = CharacterJsonAdapters.deserializeSkillPointHistory(character.skillPointHistoryJson)
            val parsedAllocations = remember(allocationsInput) {
                val lines = allocationsInput.lines().map { it.trim() }.filter { it.isNotEmpty() }
                val parsed = lines.mapNotNull { line ->
                    val parts = line.split("=", limit = 2)
                    if (parts.size != 2) null
                    else {
                        val name = parts[0].trim()
                        val points = parts[1].trim().replace(',', '.').toFloatOrNull()
                        if (name.isEmpty() || points == null || !points.isFinite() || points < 0f) null
                        else com.example.dnd35.model.SkillPointAllocation(name, points)
                    }
                }
                if (parsed.size == lines.size) parsed else emptyList()
            }
            val linesCount = allocationsInput.lines().count { it.isNotBlank() }
            val level = levelInput.toIntOrNull()
            val available = availableInput.trim().replace(',', '.').toFloatOrNull()
            val estimatedBudget = estimateSkillPointsForLevel(
                className = classInput.trim(),
                characterLevel = level ?: 0,
                intelligenceModifier = stats.intMod,
                raceName = character.race
            )
            val duplicateLevel = level != null && history.any { it.characterLevel == level }
            val allocationsValid = linesCount == parsedAllocations.size
            val canSave = level != null && level >= 1 && classInput.isNotBlank() &&
                available != null && available.isFinite() && available >= 0f &&
                allocationsValid && !duplicateLevel

            Card(
                modifier = Modifier.fillMaxWidth().testTag("skill_point_history_section"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Orçamento de pontos de perícia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Registre cada nível separadamente. A estimativa usa a INT atual e serve como referência; se o modificador mudou, informe o orçamento histórico confirmado. O aplicativo não deduz gastos pelas graduações atuais.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = levelInput,
                            onValueChange = { levelInput = it.filter(Char::isDigit).take(2) },
                            label = { Text("Nível") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.7f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = classInput,
                            onValueChange = { classInput = it },
                            label = { Text("Classe do nível") },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = availableInput,
                        onValueChange = { availableInput = it },
                        label = { Text("Pontos disponíveis") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (estimatedBudget != null) {
                        Text(
                            "Estimativa pelas regras: $estimatedBudget ponto(s), usando a INT atual (${if (stats.intMod >= 0) "+" else ""}${stats.intMod}) e a raça cadastrada. Se a INT mudou, informe o orçamento histórico confirmado.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = { availableInput = estimatedBudget.toString() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Usar estimativa") }
                    }
                    OutlinedTextField(
                        value = allocationsInput,
                        onValueChange = { allocationsInput = it },
                        label = { Text("Gastos por perícia") },
                        placeholder = { Text("Atuação=2\nOuvir=1\nEscalar=0,5") },
                        supportingText = { Text("Uma linha por perícia: nome= pontos gastos. Use vírgula ou ponto nos decimais.") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 5
                    )
                    if (duplicateLevel) {
                        Text("Já existe um registro para esse nível. Remova-o antes de cadastrá-lo novamente.", color = DndCrimson, style = MaterialTheme.typography.bodySmall)
                    }
                    if (!allocationsValid) {
                        Text("Confira os gastos: cada linha deve seguir o formato nome=valor.", color = DndCrimson, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val record = com.example.dnd35.model.SkillPointLevelRecord(
                                characterLevel = level!!,
                                className = classInput.trim(),
                                availablePoints = available!!,
                                allocations = parsedAllocations
                            )
                            viewModel.updateSkillPointHistory(history + record)
                            levelInput = ""
                            classInput = ""
                            availableInput = ""
                            allocationsInput = ""
                        },
                        enabled = canSave,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Registrar nível") }

                    if (history.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Histórico registrado", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        history.sortedBy { it.characterLevel }.forEach { record ->
                            val spent = record.allocations.sumOf { it.pointsSpent.toDouble() }.toFloat()
                            val difference = record.availablePoints - spent
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Nível ${record.characterLevel} — ${record.className}", fontWeight = FontWeight.SemiBold)
                                    val status = when {
                                        difference < -0.001f -> "Excede o orçamento em ${String.format("%.1f", -difference)} ponto(s)"
                                        difference > 0.001f -> "Restam ${String.format("%.1f", difference)} ponto(s) sem alocação"
                                        else -> "Gastos correspondem ao orçamento"
                                    }
                                    Text(
                                        "${String.format("%.1f", spent)} / ${String.format("%.1f", record.availablePoints)} pontos — $status",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (difference < -0.001f) DndCrimson else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = {
                                    viewModel.updateSkillPointHistory(history.filterNot { it.characterLevel == record.characterLevel })
                                }) {
                                    Icon(Icons.Default.Remove, contentDescription = "Remover histórico do nível ${record.characterLevel}")
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Banner de Graduação Máxima e Gastos
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Graduação Máxima: $maxClassRank (Classe) / $maxCrossRank (Entre-classe)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Graduações totais investidas: ${String.format("%.1f", totalSkillPointsSpent)} pts",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    if (stats.armorCheckPenalty < 0) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DndCrimson),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Pen: ${stats.armorCheckPenalty}",
                                color = androidx.compose.ui.graphics.Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Barra de Busca
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar perícia (ex: Observar, Furtividade)...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Filtros Chips
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filterType == 0,
                    onClick = { filterType = 0 },
                    label = { Text("Todas (${stats.computedSkills.size})") }
                )
                FilterChip(
                    selected = filterType == 1,
                    onClick = { filterType = 1 },
                    label = { Text("★ De Classe") }
                )
                FilterChip(
                    selected = filterType == 2,
                    onClick = { filterType = 2 },
                    label = { Text("Treinadas (>0)") }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Lista de Perícias
        items(filteredSkills) { item ->
            SkillItemRow(
                computed = item,
                onAddRank = {
                    val step = if (item.isClassSkill) 1.0f else 0.5f
                    viewModel.updateSkill(item.skill.name, minOf(item.maxRanks, item.ranks + step), item.miscBonus, item.isClassSkill)
                },
                onSubRank = {
                    val step = if (item.isClassSkill) 1.0f else 0.5f
                    viewModel.updateSkill(item.skill.name, (item.ranks - step).coerceAtLeast(0f), item.miscBonus, item.isClassSkill)
                },
                onEdit = { editingSkill = item },
                onRoll = {
                    if (item.canRoll) diceRollTarget = Pair(item.skill.name, item.totalBonus)
                }
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal de Edição Detalhada da Perícia
    editingSkill?.let { comp ->
        var rankInput by remember { mutableStateOf("${comp.ranks}") }
        var miscInput by remember { mutableStateOf("${comp.miscBonus}") }
        var rankInputError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { editingSkill = null },
            title = { Text("Ajustar Perícia: ${comp.skill.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Atributo Chave: ${comp.skill.keyAbility} (Mod: ${if (comp.keyAbilityMod >= 0) "+${comp.keyAbilityMod}" else "${comp.keyAbilityMod}"})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (comp.armorPenaltyApplied != 0) {
                        Text(
                            "Penalidade de Armadura aplicada: ${comp.armorPenaltyApplied}",
                            style = MaterialTheme.typography.bodySmall,
                            color = DndCrimson
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = rankInput,
                        onValueChange = {
                            rankInput = it
                            rankInputError = false
                        },
                        label = { Text("Pontos de Graduação") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = rankInputError,
                        supportingText = if (rankInputError) {
                            { Text("Use apenas incrementos de 0,5 (ex.: 1; 1,5; 2).") }
                        } else null
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = miscInput,
                        onValueChange = { miscInput = it },
                        label = { Text("Bônus Diversos / Talentos (ex: +3 Foco em Perícia)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        if (comp.isClassSkill) "Perícia de classe (limite: ${comp.maxRanks} graduações)"
                        else "Perícia entre classes (limite: ${comp.maxRanks} graduações)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val r = rankInput.trim().replace(',', '.').toFloatOrNull()
                    if (r == null || !isValidEditedSkillRanks(r)) {
                        rankInputError = true
                    } else {
                        val m = miscInput.toIntOrNull() ?: comp.miscBonus
                        viewModel.updateSkill(comp.skill.name, minOf(comp.maxRanks, r), m, comp.isClassSkill)
                        editingSkill = null
                    }
                }) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { editingSkill = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal de Rolagem de d20 da Perícia
    diceRollTarget?.let { (name, bonus) ->
        DiceRollDialog(
            actionTitle = "Teste de $name",
            bonus = bonus,
            damageNotation = "",
            onDismiss = { diceRollTarget = null }
        )
    }
}

@Composable
fun SkillItemRow(
    computed: ComputedSkill,
    onAddRank: () -> Unit,
    onSubRank: () -> Unit,
    onEdit: () -> Unit,
    onRoll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (computed.ranks > 0) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Estrela de Classe
            if (computed.isClassSkill) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Perícia de Classe",
                    tint = DndGold,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 4.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(16.dp))
            }

            // Nome e Detalhes
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onEdit)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = computed.skill.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (computed.ranks > 0) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "[${computed.skill.keyAbility}]",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                val formula = "Grad: ${computed.ranks} + Mod: ${computed.keyAbilityMod}" +
                        (if (computed.miscBonus != 0) " + Div: ${computed.miscBonus}" else "") +
                        (if (computed.armorPenaltyApplied != 0) " Pen: ${computed.armorPenaltyApplied}" else "")
                Text(
                    text = formula,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Bônus Total em destaque
            val totalStr = if (computed.totalBonus >= 0) "+${computed.totalBonus}" else "${computed.totalBonus}"
            Text(
                text = totalStr,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (computed.totalBonus >= 0) DndHpGreen else DndCrimson,
                modifier = Modifier
                    .clickable(onClick = onRoll)
                    .padding(horizontal = 8.dp)
            )

            // Botão de Rolar Dado d20
            IconButton(onClick = onRoll, enabled = computed.canRoll, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Casino,
                    contentDescription = if (computed.canRoll) "Rolar" else "Exige treinamento",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Botões de Graduação (+ e -)
            IconButton(
                onClick = onSubRank,
                enabled = computed.ranks > 0,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Diminuir Graduação", modifier = Modifier.size(16.dp))
            }

            IconButton(
                onClick = onAddRank,
                enabled = computed.ranks < computed.maxRanks,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Aumentar Graduação", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
        }
    }
}
