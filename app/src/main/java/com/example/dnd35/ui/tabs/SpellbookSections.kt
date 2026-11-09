package com.example.dnd35.ui.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dnd35.model.CalculatedCharacterStats
import com.example.dnd35.model.CasterClassInfo
import com.example.dnd35.model.CharacterJsonAdapters
import com.example.dnd35.model.CharacterSpell
import com.example.dnd35.model.ClericDomainRules
import com.example.dnd35.model.Dnd35Rules
import com.example.dnd35.model.SpellFilters
import com.example.dnd35.model.SpellbookRules
import com.example.dnd35.model.WizardSpecialtyRules
import com.example.dnd35.viewmodel.CharacterViewModel
import com.example.ui.theme.DndArcaneBlue
import com.example.ui.theme.DndCrimson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Seções da aba Magias, na ordem em que aparecem. */
internal val SPELL_SECTIONS = listOf("Preparadas", "Conhecidas", "Catálogo", "Histórico")

private fun styleDescription(style: SpellbookRules.Style?): String = when (style) {
    SpellbookRules.Style.SPELLBOOK -> "grimório e preparação diária"
    SpellbookRules.Style.PREPARED_LIST -> "preparação diária"
    SpellbookRules.Style.SPONTANEOUS -> "magias conhecidas, sem preparação"
    null -> "sem regras de conjuração"
}

@Composable
private fun LevelBadge(level: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DndArcaneBlue),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = if (level == 0) "Nv 0" else "Nv $level",
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun SectionNote(text: String, error: Boolean = false) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
private fun EmptyCard(title: String, hint: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Preparadas
// ---------------------------------------------------------------------------------------------

/**
 * Magias prontas para conjurar. Conjuradores que preparam (Mago, Clérigo, Druida, Paladino,
 * Ranger) listam as preparadas com o uso dos espaços por círculo; Feiticeiro e Bardo listam
 * todas as conhecidas, já que não preparam.
 */
internal fun LazyListScope.preparedSection(
    viewModel: CharacterViewModel,
    stats: CalculatedCharacterStats,
    onOpenSpell: (CharacterSpell) -> Unit
) {
    val casters = stats.casterClasses
    if (casters.isEmpty()) {
        item { EmptyCard("Nenhuma classe conjuradora.", "Classes que conjuram magias (Mago, Clérigo, Bardo...) liberam esta seção.") }
        return
    }

    val warnings = SpellbookRules.overLimitWarnings(stats.spells, casters) +
        ClericDomainRules.warnings(stats.spells, ClericDomainRules.domainsOf(stats.character)) +
        WizardSpecialtyRules.warnings(
            stats.spells,
            WizardSpecialtyRules.specialtyOf(stats.character),
            casters.firstOrNull { it.className.equals(WizardSpecialtyRules.WIZARD, ignoreCase = true) }
        )
    if (warnings.isNotEmpty()) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Acima do limite da classe", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                    warnings.forEach {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        }
    }

    val usage = SpellbookRules.preparedUsage(stats.spells, casters)
    for (info in casters) {
        val style = SpellbookRules.styleFor(info.className)
        val spellsOfClass = stats.spells
            .filter { SpellbookRules.classFor(it, casters)?.className == info.className }
            .filter { if (style == SpellbookRules.Style.SPONTANEOUS) true else it.prepared }
            .sortedWith(compareBy({ it.level }, { it.name }))

        item {
            Text(
                "${info.className} • ${styleDescription(style)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 6.dp)
            )
            if (style != SpellbookRules.Style.SPONTANEOUS) {
                val levels = (0..9).filter { (info.slotsByLevel[it] ?: 0) > 0 || (usage[info.className to it] ?: 0) > 0 }
                if (levels.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(levels) { lvl ->
                            val used = usage[info.className to lvl] ?: 0
                            val slots = info.slotsByLevel[lvl] ?: 0
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (used > slots) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(if (lvl == 0) "Nível 0" else "Nível $lvl", style = MaterialTheme.typography.labelSmall)
                                    Text("$used / $slots", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = DndArcaneBlue)
                                }
                            }
                        }
                    }
                    SectionNote("Preparadas / espaços por círculo")
                }
            }
        }

        if (spellsOfClass.isEmpty()) {
            item {
                SectionNote(
                    if (style == SpellbookRules.Style.SPONTANEOUS) "Nenhuma magia conhecida. Escolha no Catálogo."
                    else "Nenhuma magia preparada. Prepare pelo Catálogo ou pela seção Conhecidas."
                )
            }
        } else {
            items(spellsOfClass) { spell ->
                // Espaço da PRÓPRIA classe desta seção: espaços de outra classe não conjuram esta magia.
                val slot = stats.spellSlots.firstOrNull {
                    it.level == spell.level && it.className.equals(info.className, ignoreCase = true)
                }
                val remaining = slot?.let { (it.totalSlots - it.usedSlots).coerceAtLeast(0) } ?: 0
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable { onOpenSpell(spell) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LevelBadge(spell.level)
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                (if (spell.favorite) "★ " else "") + spell.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "${spell.school} • CD ${SpellbookRules.spellDc(spell.level, info.keyAbilityMod)}" +
                                    if (spell.domain.isNotBlank()) " • Domínio ${spell.domain}" else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (style != SpellbookRules.Style.SPONTANEOUS && spell.domain.isBlank()) {
                            IconButton(
                                onClick = { viewModel.setSpellPreparedCount(spell.id, spell.preparedCount - 1) },
                                modifier = Modifier.size(48.dp)
                            ) { Icon(Icons.Default.Remove, contentDescription = "Preparar uma vez a menos: ${spell.name}", modifier = Modifier.size(16.dp)) }
                            Text("x${spell.preparedCount}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            IconButton(
                                onClick = { viewModel.setSpellPreparedCount(spell.id, spell.preparedCount + 1) },
                                modifier = Modifier.size(48.dp)
                            ) { Icon(Icons.Default.Add, contentDescription = "Preparar mais uma vez: ${spell.name}", modifier = Modifier.size(16.dp)) }
                        }
                        OutlinedButton(
                            onClick = { if (slot != null) viewModel.castSpell(spell.id, slot, stats.spellSlots) },
                            enabled = slot != null && remaining > 0,
                            contentPadding = ButtonDefaults.TextButtonContentPadding
                        ) { Text("Conjurar", style = MaterialTheme.typography.labelSmall) }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Conhecidas
// ---------------------------------------------------------------------------------------------

/** Tudo que a ficha conhece, agrupado por classe conjuradora, com o uso dos limites. */
internal fun LazyListScope.knownSection(
    viewModel: CharacterViewModel,
    stats: CalculatedCharacterStats,
    query: String,
    onQueryChange: (String) -> Unit,
    onlyFavorites: Boolean,
    onOnlyFavoritesChange: (Boolean) -> Unit,
    onAddManual: () -> Unit,
    onOpenSpell: (CharacterSpell) -> Unit,
    onEditSpell: (CharacterSpell) -> Unit,
    onDeleteSpell: (String) -> Unit
) {
    val casters = stats.casterClasses
    item {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Buscar nas magias da ficha...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = onlyFavorites,
                onClick = { onOnlyFavoritesChange(!onlyFavorites) },
                label = { Text("★ Favoritas") }
            )
            Button(onClick = onAddManual, colors = ButtonDefaults.buttonColors(containerColor = DndArcaneBlue)) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Criar magia")
            }
        }
    }

    val visible = stats.spells.filter { s ->
        (query.isBlank() || s.name.contains(query, ignoreCase = true) || s.description.contains(query, ignoreCase = true)) &&
            (!onlyFavorites || s.favorite)
    }

    if (stats.spells.isEmpty()) {
        item { EmptyCard("Nenhuma magia na ficha.", "Abra o Catálogo para aprender magias do Livro do Jogador conforme a sua classe.") }
        return
    }
    if (visible.isEmpty()) {
        item { EmptyCard("Nenhuma magia encontrada.", "Ajuste a busca ou o filtro de favoritas.") }
        return
    }

    // Magias sem classe conjuradora reconhecida (ficha sem conjurador) ficam em "Outras".
    val groups = buildList<Pair<CasterClassInfo?, List<CharacterSpell>>> {
        for (c in casters) {
            add(c to visible.filter { SpellbookRules.classFor(it, casters)?.className == c.className })
        }
        add(null to visible.filter { SpellbookRules.classFor(it, casters) == null })
    }

    for ((info, group) in groups) {
        if (group.isEmpty()) continue
        val style = info?.let { SpellbookRules.styleFor(it.className) }
        item {
            Text(
                if (info != null) "${info.className} • ${styleDescription(style)}" else "Outras magias",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (info != null && style == SpellbookRules.Style.SPONTANEOUS) {
                val limits = info.knownSpellsByLevel
                val parts = (0..9).mapNotNull { lvl ->
                    val limit = limits?.get(lvl) ?: 0
                    val known = SpellbookRules.knownCount(stats.spells, casters, info.className, lvl)
                    if (limit > 0 || known > 0) "${if (lvl == 0) "Nv0" else "${lvl}º"}: $known/$limit" else null
                }
                SectionNote("Conhecidas / limite: ${parts.joinToString("  ")}")
            } else if (info != null && style == SpellbookRules.Style.SPELLBOOK) {
                val inBook = stats.spells.count { SpellbookRules.classFor(it, casters)?.className == info.className && SpellbookRules.isInSpellbook(it) }
                val free = SpellbookRules.freeSpellsAllowance(info.casterLevel, info.keyAbilityMod)
                val freeUsed = SpellbookRules.freeSpellsUsed(stats.spells)
                SectionNote("$inBook magia(s) no grimório • magias grátis (1º círculo ou mais): $freeUsed/$free")
            }
        }
        items(group.sortedWith(compareBy({ it.level }, { it.name }))) { spell ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clickable { onOpenSpell(spell) },
                colors = CardDefaults.cardColors(
                    containerColor = if (spell.prepared && style != SpellbookRules.Style.SPONTANEOUS)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LevelBadge(spell.level)
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(spell.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        val tags = buildList {
                            add(spell.school)
                            if (style == SpellbookRules.Style.SPELLBOOK && SpellbookRules.isInSpellbook(spell)) add("no grimório")
                            if (style != SpellbookRules.Style.SPONTANEOUS && info != null && spell.prepared) add("preparada x${spell.preparedCount}")
                        }
                        Text(tags.joinToString(" • "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { viewModel.toggleSpellFavorite(spell.id) }, modifier = Modifier.size(48.dp)) {
                        Icon(
                            if (spell.favorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (spell.favorite) "Desfavoritar ${spell.name}" else "Favoritar ${spell.name}",
                            modifier = Modifier.size(18.dp),
                            tint = DndArcaneBlue
                        )
                    }
                    IconButton(onClick = { onEditSpell(spell) }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar ${spell.name}", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { onDeleteSpell(spell.id) }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir ${spell.name}", modifier = Modifier.size(18.dp), tint = DndCrimson)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Catálogo
// ---------------------------------------------------------------------------------------------

@Composable
private fun ChipRow(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        items(options) { opt ->
            FilterChip(selected = selected == opt, onClick = { onSelect(opt) }, label = { Text(opt) })
        }
    }
}

/** Todas as magias do Livro do Jogador, independentes da ficha, com filtros. */
internal fun LazyListScope.catalogSection(
    stats: CalculatedCharacterStats,
    criteria: SpellFilters.Criteria,
    onCriteriaChange: (SpellFilters.Criteria) -> Unit,
    showMoreFilters: Boolean,
    onShowMoreFiltersChange: (Boolean) -> Unit,
    onOpenSpell: (Dnd35Rules.PresetSpell) -> Unit
) {
    item {
        OutlinedTextField(
            value = criteria.query,
            onValueChange = { onCriteriaChange(criteria.copy(query = it)) },
            placeholder = { Text("Buscar por nome ou texto da magia...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(6.dp))
        ChipRow("Classe", SpellFilters.classOptions, criteria.className) { onCriteriaChange(criteria.copy(className = it)) }

        Text("Círculo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            item {
                FilterChip(selected = criteria.level == null, onClick = { onCriteriaChange(criteria.copy(level = null)) }, label = { Text("Todos") })
            }
            items((0..9).toList()) { lvl ->
                FilterChip(
                    selected = criteria.level == lvl,
                    onClick = { onCriteriaChange(criteria.copy(level = if (criteria.level == lvl) null else lvl)) },
                    label = { Text(if (lvl == 0) "Nv 0" else "Nv $lvl") }
                )
            }
        }
        ChipRow("Escola", SpellFilters.schoolOptions, criteria.school) { onCriteriaChange(criteria.copy(school = it)) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = { onShowMoreFiltersChange(!showMoreFilters) }) {
                Text(
                    (if (showMoreFilters) "Menos filtros" else "Mais filtros") +
                        (if (criteria.activeCount > 0) " (${criteria.activeCount} ativos)" else ""),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            if (criteria.activeCount > 0 || criteria.query.isNotBlank()) {
                OutlinedButton(onClick = { onCriteriaChange(SpellFilters.Criteria()) }) {
                    Text("Limpar", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        if (showMoreFilters) {
            ChipRow("Tempo de conjuração", SpellFilters.castTimeOptions, criteria.castTime) { onCriteriaChange(criteria.copy(castTime = it)) }
            ChipRow("Alcance", SpellFilters.rangeOptions, criteria.range) { onCriteriaChange(criteria.copy(range = it)) }
            ChipRow("Duração", SpellFilters.durationOptions, criteria.duration) { onCriteriaChange(criteria.copy(duration = it)) }
            ChipRow("Componentes (V, G, M, F, FD, XP)", SpellFilters.componentOptions, criteria.component) { onCriteriaChange(criteria.copy(component = it)) }
            ChipRow("Teste de resistência", SpellFilters.saveOptions, criteria.save) { onCriteriaChange(criteria.copy(save = it)) }
            Text("Resistência à Magia", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                item { FilterChip(selected = criteria.spellResistance == null, onClick = { onCriteriaChange(criteria.copy(spellResistance = null)) }, label = { Text("Todas") }) }
                item { FilterChip(selected = criteria.spellResistance == true, onClick = { onCriteriaChange(criteria.copy(spellResistance = true)) }, label = { Text("Com RM") }) }
                item { FilterChip(selected = criteria.spellResistance == false, onClick = { onCriteriaChange(criteria.copy(spellResistance = false)) }, label = { Text("Sem RM") }) }
            }
        }
        Spacer(Modifier.height(8.dp))
    }

    val results = Dnd35Rules.PRESET_SPELLS
        .filter { SpellFilters.matches(it, criteria) }
        .sortedWith(compareBy({ it.level }, { it.name }))

    item {
        Text(
            "Livro do Jogador: ${results.size} de ${Dnd35Rules.PRESET_SPELLS.size} magias",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
    }

    if (results.isEmpty()) {
        item { EmptyCard("Nenhuma magia encontrada.", "Afrouxe algum filtro ou limpe a busca.") }
        return
    }

    items(results) { preset ->
        val inSheet = stats.spells.any { it.name.equals(preset.name, ignoreCase = true) }
        val levelShown = if (criteria.className != SpellFilters.ALL) {
            SpellbookRules.classLevelOf(preset, criteria.className) ?: preset.level
        } else preset.level
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
                .clickable { onOpenSpell(preset) },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LevelBadge(levelShown)
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        preset.name + if (inSheet) "  ✓ na ficha" else "",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "${preset.school} • " + preset.classLevels.entries.joinToString(", ") { "${it.key} ${it.value}" },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Alcance: ${preset.range} • Duração: ${preset.duration}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Histórico
// ---------------------------------------------------------------------------------------------

/** Registro das magias conjuradas, da mais recente para a mais antiga. */
internal fun LazyListScope.historySection(
    stats: CalculatedCharacterStats,
    onClear: () -> Unit
) {
    val records = CharacterJsonAdapters.deserializeSpellHistory(stats.character.spellHistoryJson).asReversed()
    if (records.isEmpty()) {
        item { EmptyCard("Nenhuma magia conjurada ainda.", "Use \"Conjurar\" nas magias preparadas ou no detalhe da magia para registrar aqui.") }
        return
    }
    item {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${records.size} conjuração(ões) registrada(s)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = onClear) { Text("Limpar histórico", style = MaterialTheme.typography.labelSmall) }
        }
    }
    val format = SimpleDateFormat("dd/MM HH:mm", Locale("pt", "BR"))
    items(records) { r ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LevelBadge(r.spellLevel)
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(r.spellName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    val detail = buildList {
                        if (r.casterClass.isNotBlank()) add(r.casterClass)
                        if (r.slotLevel >= 0) add("espaço de ${SpellbookRules.circleLabel(r.slotLevel)}")
                    }
                    if (detail.isNotEmpty()) {
                        Text(detail.joinToString(" • "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(format.format(Date(r.timestamp)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
