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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dnd35.model.CalculatedCharacterStats
import com.example.dnd35.model.WizardSpecialtyRules
import com.example.dnd35.viewmodel.CharacterViewModel

/**
 * Cartão "Especialização do Mago" da aba Magias: generalista ou especialista em uma escola,
 * com as escolas proibidas. Só aparece para fichas com a classe Mago.
 */
internal fun LazyListScope.wizardSpecialtySection(
    viewModel: CharacterViewModel,
    stats: CalculatedCharacterStats
) {
    if (stats.casterClasses.none { it.className.equals(WizardSpecialtyRules.WIZARD, ignoreCase = true) }) return
    val specialty = WizardSpecialtyRules.specialtyOf(stats.character)
    val maxBanned = WizardSpecialtyRules.maxProhibited(specialty.school)

    item {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Especialização do Mago", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Especialista ganha 1 magia extra por círculo (0 em diante), só da escola de especialidade, e abre mão de " +
                        "2 escolas proibidas (1 se a especialidade for Adivinhação). Generalista não tem espaço extra nem proibições.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Text("Escola de especialidade", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                FilterChip(
                    selected = !specialty.isSpecialist,
                    onClick = { viewModel.setWizardSpecialty(null, emptyList()) },
                    label = { Text("Generalista", style = MaterialTheme.typography.labelSmall) }
                )
                WizardSpecialtyRules.SCHOOLS.chunked(3).forEach { rowSchools ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        rowSchools.forEach { school ->
                            FilterChip(
                                selected = specialty.school == school,
                                onClick = { viewModel.setWizardSpecialty(school, specialty.prohibited) },
                                label = { Text(school, style = MaterialTheme.typography.labelSmall, maxLines = 1) }
                            )
                        }
                    }
                }

                if (specialty.isSpecialist) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Escolas proibidas (${specialty.prohibited.size}/$maxBanned)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                    WizardSpecialtyRules.SCHOOLS
                        .filter { it != specialty.school && it != WizardSpecialtyRules.DIVINATION }
                        .chunked(3)
                        .forEach { rowSchools ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                rowSchools.forEach { school ->
                                    val banned = school in specialty.prohibited
                                    FilterChip(
                                        selected = banned,
                                        enabled = banned || specialty.prohibited.size < maxBanned,
                                        onClick = {
                                            val next = if (banned) specialty.prohibited - school else specialty.prohibited + school
                                            viewModel.setWizardSpecialty(specialty.school, next)
                                        },
                                        label = { Text(school, style = MaterialTheme.typography.labelSmall, maxLines = 1) }
                                    )
                                }
                            }
                        }
                }
            }
        }
    }
}
