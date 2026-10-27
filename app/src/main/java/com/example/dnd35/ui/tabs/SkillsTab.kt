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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.example.dnd35.model.LanguageRules
import com.example.dnd35.model.LanguageAcquisition
import com.example.dnd35.model.LanguageAcquisitionSource
import com.example.dnd35.model.SkillPointAllocation
import com.example.dnd35.model.ComputedSkill
import com.example.dnd35.model.estimateSkillPointsForLevel
import com.example.dnd35.ui.dialogs.DiceRollDialog
import com.example.dnd35.viewmodel.CharacterViewModel
import com.example.dnd35.viewmodel.isValidEditedSkillRanks
import com.example.dnd35.viewmodel.skillRankAdjustmentStep
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
    val skillPointHistory = CharacterJsonAdapters.deserializeSkillPointHistory(character.skillPointHistoryJson)
    val skillPointHistoryTotal = com.example.dnd35.model.calculateSkillPointHistoryTotal(
        skillPointHistory,
        stats.totalCharacterLevel
    )
    val hasCompleteSkillPointHistory = skillPointHistoryTotal.isComplete
    val currentLevelSkillPointHistory = skillPointHistory.filter {
        it.characterLevel in 1..stats.totalCharacterLevel
    }
    val totalAvailableSkillPoints = if (hasCompleteSkillPointHistory) {
        currentLevelSkillPointHistory.sumOf { it.availablePoints.toDouble() }
    } else {
        null
    }
    val totalSkillPointsSpent = if (hasCompleteSkillPointHistory) {
        skillPointHistoryTotal.pointsSpent.toDouble()
    } else {
        stats.computedSkills.sumOf {
            it.ranks.toDouble() * if (it.isClassSkill) 1.0 else 2.0
        }
    }
    val recordedSkillPointBalance = totalAvailableSkillPoints?.minus(skillPointHistoryTotal.pointsSpent.toDouble())

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
                            "${if (hasCompleteSkillPointHistory) "Pontos gastos (histórico completo)" else "Pontos gastos (estimativa atual)"}: ${String.format("%.1f", totalSkillPointsSpent)} pts",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        if (hasCompleteSkillPointHistory && totalAvailableSkillPoints != null && recordedSkillPointBalance != null) {
                            Text(
                                "Orçamento registrado: ${String.format("%.1f", totalAvailableSkillPoints)} pts • " +
                                    when {
                                        recordedSkillPointBalance < -0.001 -> "excesso de ${String.format("%.1f", -recordedSkillPointBalance)} pts"
                                        recordedSkillPointBalance > 0.001 -> "saldo: ${String.format("%.1f", recordedSkillPointBalance)} pts"
                                        else -> "saldo: 0 pts"
                                    },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (recordedSkillPointBalance < -0.001) DndCrimson else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        } else {
                            Text(
                                "Saldo disponível: indisponível — registre o orçamento e os gastos de todos os níveis atuais.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
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
                    label = { Text("★ De classe") }
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
                    val step = skillRankAdjustmentStep(item.ranks, item.isClassSkill)
                    viewModel.updateSkill(item.skill.name, minOf(item.maxRanks, item.ranks + step), item.miscBonus, item.isClassSkill)
                },
                onSubRank = {
                    val step = skillRankAdjustmentStep(item.ranks, item.isClassSkill)
                    viewModel.updateSkill(item.skill.name, (item.ranks - step).coerceAtLeast(0f), item.miscBonus, item.isClassSkill)
                },
                onEdit = { editingSkill = item },
                onRoll = {
                    if (item.canRoll) diceRollTarget = Pair(item.skill.name, item.totalBonus)
                }
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Idiomas conhecidos
        item {
            var languageInput by remember(character.id) { mutableStateOf("") }
            var languageMenuExpanded by remember(character.id) { mutableStateOf(false) }
            var languageSourceMenuExpanded by remember(character.id) { mutableStateOf(false) }
            var languageSource by remember(character.id) { mutableStateOf(LanguageAcquisitionSource.OTHER) }
            var languageLevelInput by remember(character.id) { mutableStateOf("") }
            var languageAcquisitionError by remember(character.id) { mutableStateOf<String?>(null) }
            val languages = CharacterJsonAdapters.deserializeLanguages(character.languagesJson)
            val acquisitions = CharacterJsonAdapters.deserializeLanguageAcquisitions(character.languageAcquisitionsJson)
            fun addLanguage(language: String) {
                val cleaned = language.trim()
                if (cleaned.isEmpty()) return
                if (languages.any { it.equals(cleaned, ignoreCase = true) }) {
                    languageAcquisitionError = "Esse idioma já está cadastrado."
                    return
                }
                val level = languageLevelInput.toIntOrNull()
                if (languageSource == LanguageAcquisitionSource.PURCHASED && (level == null || level < 1)) {
                    languageAcquisitionError = "Informe o nível em que o ponto foi gasto."
                    return
                }
                val acquisition = LanguageAcquisition(
                    language = cleaned,
                    source = languageSource,
                    characterLevel = if (languageSource == LanguageAcquisitionSource.PURCHASED) level else null,
                    pointsSpent = if (languageSource == LanguageAcquisitionSource.PURCHASED) 1 else 0
                )
                if (languageSource == LanguageAcquisitionSource.PURCHASED) {
                    if (level == null || level !in 1..stats.totalCharacterLevel) {
                        languageAcquisitionError = "Informe um nível de personagem válido, entre 1 e ${stats.totalCharacterLevel}."
                        return
                    }
                    val recordsForLevel = skillPointHistory.filter { it.characterLevel == level }
                    if (recordsForLevel.isEmpty()) {
                        languageAcquisitionError = "Registre primeiro o orçamento de perícias desse nível para contabilizar o ponto."
                        return
                    }
                    if (recordsForLevel.size != 1) {
                        languageAcquisitionError = "Há registros duplicados nesse nível. Corrija o histórico antes de contabilizar o idioma."
                        return
                    }
                    val levelRecord = recordsForLevel.single()
                    val languageAllocationName = "Falar Idioma ($cleaned)"
                    if (levelRecord.allocations.any { it.skillName.equals(languageAllocationName, ignoreCase = true) }) {
                        languageAcquisitionError = "Esse gasto já consta no histórico de perícias."
                        return
                    }
                    val alreadySpent = levelRecord.allocations.sumOf { it.pointsSpent.toDouble() }
                    if (alreadySpent + 1.0 > levelRecord.availablePoints.toDouble()) {
                        languageAcquisitionError = "Não há ponto disponível nesse nível no histórico registrado."
                        return
                    }
                    viewModel.updateSkillPointHistory(
                        skillPointHistory.map { record ->
                            if (record.characterLevel == level) record.copy(
                                allocations = record.allocations + SkillPointAllocation(languageAllocationName, 1f)
                            ) else record
                        }
                    )
                }
                viewModel.updateLanguages(languages + cleaned)
                viewModel.updateLanguageAcquisitions(acquisitions + acquisition)
                languageInput = ""
                languageLevelInput = ""
                languageAcquisitionError = null
            }
            val racialLanguageRules = LanguageRules.forRace(character.race)
            val selectableLanguages = (
                (racialLanguageRules?.automatic.orEmpty() +
                    racialLanguageRules?.bonusOptions.orEmpty() +
                    LanguageRules.coreLanguages)
                ).distinctBy { it.lowercase() }
                .filterNot { option -> languages.any { it.equals(option, ignoreCase = true) } }
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
                    Text(
                        text = if (racialLanguageRules == null) {
                            "Escolha um idioma do catálogo ou digite um idioma permitido pela sua campanha."
                        } else {
                            "Idiomas automáticos da raça: ${racialLanguageRules.automatic.joinToString()}. " +
                                "As opções raciais são sugestões; confirme as escolhas permitidas pelo cenário."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (selectableLanguages.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(onClick = { languageMenuExpanded = true }) {
                                Text("Escolher idioma")
                            }
                            DropdownMenu(
                                expanded = languageMenuExpanded,
                                onDismissRequest = { languageMenuExpanded = false }
                            ) {
                                selectableLanguages.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            addLanguage(option)
                                            languageMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = { languageSourceMenuExpanded = true }) {
                            Text(when (languageSource) {
                                LanguageAcquisitionSource.RACIAL -> "Fonte: raça"
                                LanguageAcquisitionSource.INTELLIGENCE -> "Fonte: Inteligência"
                                LanguageAcquisitionSource.CLASS_FEATURE -> "Fonte: classe"
                                LanguageAcquisitionSource.FEAT -> "Fonte: talento"
                                LanguageAcquisitionSource.PURCHASED -> "Fonte: comprado (1 ponto)"
                                LanguageAcquisitionSource.OTHER -> "Fonte: outra"
                            })
                        }
                        DropdownMenu(
                            expanded = languageSourceMenuExpanded,
                            onDismissRequest = { languageSourceMenuExpanded = false }
                        ) {
                            LanguageAcquisitionSource.values().forEach { source ->
                                DropdownMenuItem(
                                    text = { Text(when (source) {
                                        LanguageAcquisitionSource.RACIAL -> "Raça"
                                        LanguageAcquisitionSource.INTELLIGENCE -> "Inteligência"
                                        LanguageAcquisitionSource.CLASS_FEATURE -> "Habilidade de classe"
                                        LanguageAcquisitionSource.FEAT -> "Talento"
                                        LanguageAcquisitionSource.PURCHASED -> "Comprado (1 ponto)"
                                        LanguageAcquisitionSource.OTHER -> "Outra fonte"
                                    }) },
                                    onClick = { languageSource = source; languageSourceMenuExpanded = false }
                                )
                            }
                        }
                    }
                    if (languageSource == LanguageAcquisitionSource.PURCHASED) {
                        OutlinedTextField(
                            value = languageLevelInput,
                            onValueChange = { languageLevelInput = it.filter(Char::isDigit).take(2) },
                            label = { Text("Nível do personagem em que gastou o ponto") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = languageInput,
                            onValueChange = { languageInput = it },
                            label = { Text("Adicionar idioma") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { addLanguage(languageInput) }, enabled = languageInput.isNotBlank()) {
                            Text("Adicionar")
                        }
                    }
                    languageAcquisitionError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
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
                                val acquisition = acquisitions.firstOrNull { it.language.equals(language, ignoreCase = true) }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(language)
                                    if (acquisition != null) {
                                        Text(
                                            when (acquisition.source) {
                                                LanguageAcquisitionSource.PURCHASED -> "Comprado: 1 ponto no nível ${acquisition.characterLevel}"
                                                LanguageAcquisitionSource.RACIAL -> "Origem: raça"
                                                LanguageAcquisitionSource.INTELLIGENCE -> "Origem: Inteligência"
                                                LanguageAcquisitionSource.CLASS_FEATURE -> "Origem: habilidade de classe"
                                                LanguageAcquisitionSource.FEAT -> "Origem: talento"
                                                LanguageAcquisitionSource.OTHER -> "Origem: outra"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        Text("Origem não registrada (ficha antiga)", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                IconButton(onClick = {
                                    val removedAcquisition = acquisition
                                    if (removedAcquisition?.source == LanguageAcquisitionSource.PURCHASED) {
                                        val allocationName = "Falar Idioma (${removedAcquisition.language})"
                                        viewModel.updateSkillPointHistory(
                                            skillPointHistory.map { record ->
                                                if (record.characterLevel == removedAcquisition.characterLevel) {
                                                    record.copy(allocations = record.allocations.filterNot {
                                                        it.skillName.equals(allocationName, ignoreCase = true)
                                                    })
                                                } else record
                                            }
                                        )
                                    }
                                    viewModel.updateLanguages(languages.filterNot { it == language })
                                    viewModel.updateLanguageAcquisitions(acquisitions.filterNot { it.language.equals(language, ignoreCase = true) })
                                }) {
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
                com.example.dnd35.model.parseSkillPointAllocations(allocationsInput)
            }
            val level = levelInput.toIntOrNull()
            val available = com.example.dnd35.model.parseWholeSkillPointBudget(availableInput)
            val estimatedBudget = estimateSkillPointsForLevel(
                className = classInput.trim(),
                characterLevel = level ?: 0,
                intelligenceModifier = stats.intMod,
                raceName = character.race
            )
            val duplicateLevel = level != null && history.any { it.characterLevel == level }
            val allocationsValid = parsedAllocations != null
            val canSave = level != null && level >= 1 && classInput.isNotBlank() &&
                available != null &&
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
                        label = { Text("Pontos disponíveis (inteiro)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        placeholder = { Text("Atuação=2\nOuvir=1\nEscalar=1") },
                        supportingText = { Text("Uma linha por perícia: nome=pontos inteiros gastos. Não repita nomes; meia graduação entreclasse custa 1 ponto.") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 5
                    )
                    if (duplicateLevel) {
                        Text("Já existe um registro para esse nível. Remova-o antes de cadastrá-lo novamente.", color = DndCrimson, style = MaterialTheme.typography.bodySmall)
                    }
                    if (!allocationsValid) {
                        Text("Confira os gastos: use nome=pontos inteiros não negativos, sem repetir perícias.", color = DndCrimson, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val record = com.example.dnd35.model.SkillPointLevelRecord(
                                characterLevel = level!!,
                                className = classInput.trim(),
                                availablePoints = available!!.toFloat(),
                                allocations = parsedAllocations!!
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
                        history.withIndex().sortedBy { it.value.characterLevel }.forEach { (recordIndex, indexedRecord) ->
                            val record = indexedRecord
                            val spent = record.allocations.sumOf { it.pointsSpent.toDouble() }.toFloat()
                            val difference = record.availablePoints - spent
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Nível ${record.characterLevel} — ${record.className}", fontWeight = FontWeight.SemiBold)
                                    if (record.characterLevel > character.level) {
                                        Text(
                                            "Acima do nível atual (${character.level}); preservado, mas não entra no total de pontos gastos.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
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
                                    viewModel.updateSkillPointHistory(history.filterIndexed { index, _ -> index != recordIndex })
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
            title = { Text("Ajustar perícia: ${comp.skill.name}", fontWeight = FontWeight.Bold) },
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
                        label = { Text("Graduações") },
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
                        label = { Text("Bônus diversos e de talentos (ex.: +3 em Foco em Perícia)") },
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
                        (if (computed.racialBonus != 0) " + Rac: ${computed.racialBonus}" else "") +
                        (if (computed.sizeBonus != 0) " + Tam: ${computed.sizeBonus}" else "") +
                        (if (computed.miscBonus != 0) " + Div: ${computed.miscBonus}" else "") +
                        (if (computed.armorPenaltyApplied != 0) " Pen: ${computed.armorPenaltyApplied}" else "")
                Text(
                    text = formula,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val skillPointCost = computed.ranks * if (computed.isClassSkill) 1f else 2f
                Text(
                    text = "Pontos investidos nesta perícia: ${String.format("%.1f", skillPointCost)} pts (estimativa pelas graduações atuais)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (computed.trainedOnly && computed.ranks < 1f &&
                    com.example.dnd35.model.Dnd35Rules.allowsUntrainedKnowledgeCheck(computed.skill.name)
                ) {
                    Text(
                        "Sem graduação: apenas informações comuns (CD 10 ou inferior).",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
            IconButton(onClick = onRoll, enabled = computed.canRoll, modifier = Modifier.size(48.dp)) {
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
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Diminuir graduação de ${computed.skill.name}", modifier = Modifier.size(16.dp))
            }

            IconButton(
                onClick = onAddRank,
                enabled = computed.ranks < computed.maxRanks,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Aumentar graduação de ${computed.skill.name}", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
        }
    }
}
