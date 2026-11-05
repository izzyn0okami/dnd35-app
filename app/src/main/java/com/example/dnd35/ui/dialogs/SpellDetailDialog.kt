package com.example.dnd35.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.dnd35.model.CalculatedCharacterStats
import com.example.dnd35.model.CharacterSpell
import com.example.dnd35.model.ClericDomainRules
import com.example.dnd35.model.Dnd35Rules
import com.example.dnd35.model.SpellbookRules
import com.example.dnd35.viewmodel.CharacterViewModel
import com.example.ui.theme.DndArcaneBlue

/**
 * Página de detalhes de uma magia. As ações mostradas dependem das regras da classe
 * conjuradora escolhida: o Mago adiciona ao grimório e depois prepara; Clérigo, Druida,
 * Paladino e Ranger preparam direto da lista da classe; Feiticeiro e Bardo aprendem a magia
 * como conhecida e a conjuram sem preparar.
 */
@Composable
fun SpellDetailDialog(
    spell: Dnd35Rules.PresetSpell,
    stats: CalculatedCharacterStats,
    viewModel: CharacterViewModel,
    onDismiss: () -> Unit
) {
    val casters = stats.casterClasses
    val options = casters.filter { SpellbookRules.classLevelOf(spell, it.className) != null }
    var selectedName by remember { mutableStateOf(options.firstOrNull()?.className) }
    val info = options.firstOrNull { it.className == selectedName } ?: options.firstOrNull()
    val level = info?.let { SpellbookRules.classLevelOf(spell, it.className) } ?: spell.level
    val style = info?.let { SpellbookRules.styleFor(it.className) }
    val abilityScore = info?.let { SpellbookRules.abilityScoreFor(it, stats) } ?: 0
    val eligibility = info?.let { SpellbookRules.eligibility(spell, it, abilityScore) }

    // Entrada da magia na ficha para a classe escolhida (se já foi aprendida/preparada).
    val entry: CharacterSpell? = stats.spells.firstOrNull { s ->
        s.name.equals(spell.name, ignoreCase = true) &&
            (info == null || SpellbookRules.classFor(s, casters)?.className.equals(info.className, ignoreCase = true))
    }

    // Espaço da classe que conjura esta magia (espaços de outras classes não servem).
    val slot = entry?.let { e ->
        val slotClass = info?.className ?: SpellbookRules.classFor(e, casters)?.className
        stats.spellSlots.firstOrNull { it.level == e.level && it.className.equals(slotClass, ignoreCase = true) }
    }
    val remainingSlots = slot?.let { (it.totalSlots - it.usedSlots).coerceAtLeast(0) } ?: 0

    fun newCharacterSpell(): CharacterSpell = CharacterSpell(
        name = spell.name,
        level = level,
        school = spell.school,
        castTime = spell.castTime,
        components = spell.components,
        range = spell.range,
        duration = spell.duration,
        savingThrow = spell.savingThrow,
        spellResistance = spell.spellResistance,
        prepared = false,
        description = spell.description,
        casterClass = info?.className ?: ""
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(spell.name, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(spell.school, style = MaterialTheme.typography.labelMedium, color = DndArcaneBlue, fontWeight = FontWeight.Bold)
                val levels = buildList {
                    spell.classLevels.forEach { (cls, lvl) -> add("$cls $lvl") }
                    spell.domainLevels.forEach { (dom, lvl) -> add("Domínio $dom $lvl") }
                }
                if (levels.isNotEmpty()) {
                    Text("Nível: ${levels.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                }
                Text("Tempo de conjuração: ${spell.castTime}", style = MaterialTheme.typography.bodySmall)
                Text("Alcance: ${spell.range}", style = MaterialTheme.typography.bodySmall)
                Text("Componentes: ${spell.components}", style = MaterialTheme.typography.bodySmall)
                Text("Duração: ${spell.duration}", style = MaterialTheme.typography.bodySmall)
                Text("Teste de resistência: ${spell.savingThrow}", style = MaterialTheme.typography.bodySmall)
                Text("Resistência à Magia: ${if (spell.spellResistance) "Sim" else "Não"}", style = MaterialTheme.typography.bodySmall)

                if (spell.description.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(spell.description, style = MaterialTheme.typography.bodySmall)
                }

                // Clérigo: magia de um dos domínios escolhidos pode ocupar o espaço de domínio do círculo.
                val clericInfo = casters.firstOrNull { it.className.equals(ClericDomainRules.CLERIC, ignoreCase = true) }
                if (clericInfo != null) {
                    val topCircle = SpellbookRules.highestAccessibleCircle(clericInfo)
                    ClericDomainRules.domainsOf(stats.character)
                        .filter { spell.domainLevels.containsKey(it) }
                        .forEach { domain ->
                            val circle = spell.domainLevels.getValue(domain)
                            val domainEntry = stats.spells.firstOrNull {
                                it.name.equals(spell.name, ignoreCase = true) &&
                                    it.domain.equals(domain, ignoreCase = true) && it.prepared
                            }
                            Spacer(Modifier.height(6.dp))
                            if (circle > topCircle) {
                                Text(
                                    "Domínio $domain: o Clérigo ainda não acessa magias de ${SpellbookRules.circleLabel(circle)}.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else if (domainEntry != null) {
                                OutlinedButton(
                                    onClick = { viewModel.clearDomainSpell(domainEntry.id) },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Tirar do espaço de domínio ($domain)") }
                            } else {
                                Button(
                                    onClick = { viewModel.prepareDomainSpell(spell, domain) },
                                    colors = ButtonDefaults.buttonColors(containerColor = DndArcaneBlue),
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Preparar no espaço de domínio ($domain, ${SpellbookRules.circleLabel(circle)})") }
                            }
                        }
                }

                if (info != null) {
                    Spacer(Modifier.height(8.dp))
                    if (options.size > 1) {
                        Text("Classe conjuradora:", style = MaterialTheme.typography.labelSmall)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(options) { opt ->
                                FilterChip(
                                    selected = opt.className == info.className,
                                    onClick = { selectedName = opt.className },
                                    label = { Text(opt.className) }
                                )
                            }
                        }
                    }
                    Text(
                        "${info.className}: CD ${SpellbookRules.spellDc(level, info.keyAbilityMod)} " +
                            "(10 + $level + ${info.keyAbilityMod}) • atributo mínimo para conjurar: " +
                            "${SpellbookRules.minAbilityScore(level)} (atual $abilityScore)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    when (eligibility) {
                        SpellbookRules.Eligibility.CIRCLE_NOT_REACHED ->
                            Text("${info.className} ainda não acessa magias de ${SpellbookRules.circleLabel(level)}.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        SpellbookRules.Eligibility.ABILITY_TOO_LOW ->
                            Text("Atributo-chave baixo demais para conjurar magias de ${SpellbookRules.circleLabel(level)}.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        else -> Unit
                    }

                    Spacer(Modifier.height(6.dp))
                    if (eligibility == SpellbookRules.Eligibility.OK) {
                        when (style) {
                            SpellbookRules.Style.SPELLBOOK -> {
                                if (entry == null || !SpellbookRules.isInSpellbook(entry)) {
                                    Button(
                                        onClick = { viewModel.learnSpell(newCharacterSpell(), addToSpellbook = true, prepare = false) },
                                        colors = ButtonDefaults.buttonColors(containerColor = DndArcaneBlue),
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Adicionar ao grimório") }
                                } else if (!entry.prepared) {
                                    Button(
                                        onClick = { viewModel.setSpellPrepared(entry.id, true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = DndArcaneBlue),
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Preparar magia") }
                                } else {
                                    OutlinedButton(
                                        onClick = { viewModel.setSpellPrepared(entry.id, false) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Remover da preparação") }
                                }
                            }
                            SpellbookRules.Style.PREPARED_LIST -> {
                                if (entry == null) {
                                    Button(
                                        onClick = { viewModel.learnSpell(newCharacterSpell(), addToSpellbook = false, prepare = true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = DndArcaneBlue),
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Preparar magia") }
                                } else if (!entry.prepared) {
                                    Button(
                                        onClick = { viewModel.setSpellPrepared(entry.id, true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = DndArcaneBlue),
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Preparar magia") }
                                } else {
                                    OutlinedButton(
                                        onClick = { viewModel.setSpellPrepared(entry.id, false) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Remover da preparação") }
                                }
                            }
                            SpellbookRules.Style.SPONTANEOUS -> {
                                if (entry == null) {
                                    Button(
                                        onClick = { viewModel.learnSpell(newCharacterSpell(), addToSpellbook = false, prepare = false) },
                                        colors = ButtonDefaults.buttonColors(containerColor = DndArcaneBlue),
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Adicionar às conhecidas") }
                                } else {
                                    OutlinedButton(
                                        onClick = { viewModel.removeSpell(entry.id) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Remover das conhecidas") }
                                }
                            }
                            null -> Unit
                        }
                    }

                    if (entry != null) {
                        val canCast = style == SpellbookRules.Style.SPONTANEOUS || entry.prepared
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { if (slot != null) viewModel.castSpell(entry.id, slot, stats.spellSlots) },
                                enabled = canCast && remainingSlots > 0,
                                modifier = Modifier.weight(1f)
                            ) { Text("Marcar como conjurada") }
                            OutlinedButton(
                                onClick = { viewModel.toggleSpellFavorite(entry.id) },
                                modifier = Modifier.weight(1f)
                            ) { Text(if (entry.favorite) "Desfavoritar" else "Favoritar") }
                        }
                        if (canCast && remainingSlots == 0) {
                            Text(
                                "Sem espaços de ${SpellbookRules.circleLabel(entry.level)} disponíveis.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Nenhuma classe conjuradora da ficha usa esta magia.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}
