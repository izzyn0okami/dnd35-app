package com.example.dnd35.ui.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dnd35.model.CalculatedCharacterStats
import com.example.dnd35.model.ClericDomainRules
import com.example.dnd35.model.SpellbookRules
import com.example.dnd35.viewmodel.CharacterViewModel
import com.example.ui.theme.DndArcaneBlue

/**
 * Cartão "Domínios do Clérigo" da aba Magias: escolha de até 2 domínios e, para cada círculo
 * que o Clérigo já acessa, a magia de domínio que vai no espaço extra de domínio. Só aparece
 * para fichas com a classe Clérigo.
 */
internal fun LazyListScope.clericDomainSection(
    viewModel: CharacterViewModel,
    stats: CalculatedCharacterStats
) {
    val cleric = stats.casterClasses.firstOrNull { it.className.equals(ClericDomainRules.CLERIC, ignoreCase = true) }
        ?: return
    val domains = ClericDomainRules.domainsOf(stats.character)
    val topCircle = SpellbookRules.highestAccessibleCircle(cleric)

    item {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Domínios do Clérigo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Escolha 2 domínios. Em cada círculo (1º em diante) há 1 espaço extra, só para a magia de domínio daquele círculo.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                ClericDomainRules.ALL_DOMAINS.chunked(4).forEach { rowDomains ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        rowDomains.forEach { domain ->
                            val selected = domains.any { it.equals(domain, ignoreCase = true) }
                            FilterChip(
                                selected = selected,
                                enabled = selected || domains.size < ClericDomainRules.MAX_DOMAINS,
                                onClick = {
                                    val next = if (selected) domains - domain else domains + domain
                                    viewModel.setClericDomains(next)
                                },
                                label = { Text(domain, style = MaterialTheme.typography.labelSmall, maxLines = 1) }
                            )
                        }
                    }
                }
                Text(
                    "${domains.size}/${ClericDomainRules.MAX_DOMAINS} domínios escolhidos",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                if (domains.isNotEmpty() && topCircle >= 1) {
                    Spacer(Modifier.height(10.dp))
                    Text("Magia de domínio por círculo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    (1..topCircle).forEach { circle ->
                        val options = ClericDomainRules.domainSpellsAt(domains, circle)
                        if (options.isEmpty()) return@forEach
                        Text(
                            SpellbookRules.circleLabel(circle),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        options.forEach { (domain, preset) ->
                            val inSlot = stats.spells.any {
                                it.name.equals(preset.name, ignoreCase = true) &&
                                    it.domain.equals(domain, ignoreCase = true) && it.prepared
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(preset.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text("Domínio $domain", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (inSlot) {
                                    val spellId = stats.spells.first {
                                        it.name.equals(preset.name, ignoreCase = true) && it.domain.equals(domain, ignoreCase = true)
                                    }.id
                                    OutlinedButton(
                                        onClick = { viewModel.clearDomainSpell(spellId) },
                                        contentPadding = ButtonDefaults.TextButtonContentPadding
                                    ) { Text("No espaço de domínio ✓", style = MaterialTheme.typography.labelSmall) }
                                } else {
                                    Button(
                                        onClick = { viewModel.prepareDomainSpell(preset, domain) },
                                        colors = ButtonDefaults.buttonColors(containerColor = DndArcaneBlue),
                                        contentPadding = ButtonDefaults.TextButtonContentPadding
                                    ) { Text("Preparar", style = MaterialTheme.typography.labelSmall) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
