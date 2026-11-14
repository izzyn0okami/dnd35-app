package com.example.dnd35.ui.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dnd35.model.CreatureDefinition
import com.example.dnd35.model.CreatureCatalog
import com.example.dnd35.ui.components.CreatureImage
import com.example.dnd35.ui.components.CreatureImageTradingCard
import com.example.dnd35.ui.components.CreatureCardStack

/**
 * Bestiário consultivo. As ilustrações são resolvidas automaticamente em assets/monsters
 * pelo nome normalizado da criatura.
 */
@Composable
fun CreatureCatalogTab(modifier: Modifier = Modifier) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedType by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedSize by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedCreatureId by rememberSaveable { mutableStateOf<String?>(null) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var sizeMenuExpanded by remember { mutableStateOf(false) }

    val selectedCreature = selectedCreatureId?.let(CreatureCatalog::byId)
    val filtered = remember(query, selectedType, selectedSize) {
        CreatureCatalog.filter(query = query, type = selectedType, size = selectedSize)
    }

    if (selectedCreature != null) {
        CreatureDetail(
            creature = selectedCreature,
            onBack = { selectedCreatureId = null },
            modifier = modifier
        )
        return
    }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Text(
            text = "Livro dos Monstros",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
        )
        Text(
            text = "${filtered.size} criaturas no catálogo",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            label = { Text("Buscar criatura") },
            singleLine = true
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box {
                FilterChip(
                    selected = selectedType != null,
                    onClick = { typeMenuExpanded = true },
                    label = { Text(selectedType ?: "Todos os tipos") }
                )
                DropdownMenu(
                    expanded = typeMenuExpanded,
                    onDismissRequest = { typeMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todos os tipos") },
                        onClick = { selectedType = null; typeMenuExpanded = false }
                    )
                    CreatureCatalog.TYPES.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = { selectedType = type; typeMenuExpanded = false }
                        )
                    }
                }
            }
            Box {
                FilterChip(
                    selected = selectedSize != null,
                    onClick = { sizeMenuExpanded = true },
                    label = { Text(selectedSize ?: "Todos os tamanhos") }
                )
                DropdownMenu(
                    expanded = sizeMenuExpanded,
                    onDismissRequest = { sizeMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todos os tamanhos") },
                        onClick = { selectedSize = null; sizeMenuExpanded = false }
                    )
                    CreatureCatalog.SIZES.forEach { size ->
                        DropdownMenuItem(
                            text = { Text(size) },
                            onClick = { selectedSize = size; sizeMenuExpanded = false }
                        )
                    }
                }
            }
        }
        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhuma criatura corresponde aos filtros.")
            }
        } else {
            CreatureCardStack(
                items = filtered,
                key = { it.id },
                modifier = Modifier.fillMaxWidth()
            ) { creature ->
                CreatureImageTradingCard(
                    creature = creature,
                    details = "${creature.size} • ${creature.typeDetail.ifBlank { creature.type }}",
                    stats = "ND ${creature.challengeRating} • PV ${creature.hitPoints} • CA ${creature.armorClassText}",
                    selectLabel = "Abrir ficha completa",
                    onSelect = { selectedCreatureId = creature.id }
                )
            } {
                items(filtered, key = { it.id }) { creature ->
                    CreatureListItem(creature = creature) {
                        selectedCreatureId = creature.id
                    }
                }
            }
        }
    }
}

@Composable
private fun CreatureListItem(creature: CreatureDefinition, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CreatureImage(creature, Modifier.size(76.dp), ContentScale.Crop, maxDimensionPx = 700)
            Spacer(modifier = Modifier.size(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(creature.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "${creature.size} • ${creature.typeDetail.ifBlank { creature.type }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val usageLabels = buildList {
                    if (creature.animalCompanion) add("Companheiro animal")
                    if (creature.summonedCreature) add("Invocável")
                    if (creature.familiar) add("Familiar")
                }
                if (usageLabels.isNotEmpty()) {
                    Text(
                        usageLabels.joinToString(" • "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    "ND ${creature.challengeRating}",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun CreatureDetail(
    creature: CreatureDefinition,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar ao bestiário")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(creature.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "${creature.size} • ${creature.typeDetail.ifBlank { creature.type }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item {
            CreatureImage(creature, Modifier.fillMaxWidth().height(300.dp),
                contentScale = ContentScale.Fit, maxDimensionPx = 1800)
        }
        item {
            CreatureSection("Descrição") {
                Text(
                    creature.description.ifBlank { "Texto descritivo pendente de extração e conferência no Livro dos Monstros." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (creature.description.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
            }
        }
        item {
            CreatureSection("Usos previstos nas regras") {
                val uses = listOfNotNull(
                    "Companheiro animal".takeIf { creature.animalCompanion },
                    "Criatura invocável".takeIf { creature.summonedCreature },
                    "Familiar".takeIf { creature.familiar }
                )
                if (uses.isEmpty()) {
                    Text("Não consta nas listas básicas de companheiro animal, invocação ou familiar do Livro do Jogador.")
                } else {
                    uses.forEach { use ->
                        Text("• $use", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        item {
            CreatureSection("Bloco de Estatísticas") {
                CreatureField("Dados de Vida", "${creature.hitDice} (${creature.hitPoints} PV)")
                CreatureField("Iniciativa", signed(creature.initiative))
                CreatureField("Deslocamento", creature.speed)
                CreatureField("Classe de Armadura", creature.armorClassText)
                CreatureField("CA de Toque", creature.touchAc.toString())
                CreatureField("CA de Surpresa", creature.flatFootedAc.toString())
                CreatureField("Ataque Base/Agarrar", "${signed(creature.baseAttack)}/${signed(creature.grapple)}")
                CreatureField("Ataque", creature.attack)
                CreatureField("Ataque Total", creature.fullAttack)
                CreatureField("Espaço/Alcance", creature.spaceReach)
                CreatureField("Ataques Especiais", creature.specialAttacks)
                CreatureField("Qualidades Especiais", creature.specialQualities)
                CreatureField(
                    "Testes de Resistência",
                    "Fort ${signed(creature.saves.fort)} • Ref ${signed(creature.saves.ref)} • Von ${signed(creature.saves.will)}"
                )
            }
        }
        item {
            CreatureSection("Habilidades") {
                val a = creature.abilities
                CreatureField("Força", score(a.str))
                CreatureField("Destreza", score(a.dex))
                CreatureField("Constituição", score(a.con))
                CreatureField("Inteligência", score(a.int))
                CreatureField("Sabedoria", score(a.wis))
                CreatureField("Carisma", score(a.cha))
            }
        }
        item {
            CreatureSection("Perícias e Talentos") {
                CreatureField("Perícias", creature.skills)
                CreatureField("Talentos", creature.feats)
            }
        }
        item {
            CreatureSection("Combate") {
                Text(
                    creature.combat.ifBlank { "Texto da seção Combate pendente de extração e conferência no Livro dos Monstros." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (creature.combat.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
            }
        }
        item {
            CreatureSection("Informações Gerais") {
                CreatureField("Ambiente", creature.environment)
                CreatureField("Organização", creature.organization)
                CreatureField("Nível de Desafio", creature.challengeRating)
                CreatureField("Tesouro", creature.treasure)
                CreatureField("Tendência", creature.alignment)
                CreatureField("Progressão", creature.advancement)
                CreatureField("Ajuste de Nível", creature.levelAdjustment)
                CreatureField("Fonte dos dados", creature.source)
            }
        }
    }
}

@Composable
private fun CreatureSection(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            HorizontalDivider()
            content()
        }
    }
}

@Composable
private fun CreatureField(label: String, value: String) {
    if (value.isBlank()) return
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun signed(value: Int): String = if (value >= 0) "+$value" else value.toString()
private fun score(value: Int?): String = value?.toString() ?: "—"
