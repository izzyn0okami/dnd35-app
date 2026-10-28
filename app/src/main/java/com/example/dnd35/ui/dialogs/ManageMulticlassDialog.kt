package com.example.dnd35.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dnd35.model.CharacterClassEntry
import com.example.dnd35.model.Dnd35Rules
import com.example.dnd35.model.calculateMulticlassXpPenalty
import com.example.ui.theme.DndCrimson
import com.example.ui.theme.DndGold
import com.example.ui.theme.DndHpGreen

@Composable
fun ManageMulticlassDialog(
    initialClasses: List<CharacterClassEntry>,
    raceName: String,
    onConfirm: (List<CharacterClassEntry>) -> Unit,
    onDismiss: () -> Unit
) {
    var classesList by remember {
        mutableStateOf(if (initialClasses.isEmpty()) listOf(CharacterClassEntry("Guerreiro", 1)) else initialClasses)
    }

    var showAddMenu by remember { mutableStateOf(false) }

    val race = Dnd35Rules.RACES.find { it.name.equals(raceName, ignoreCase = true) } ?: Dnd35Rules.RACES.first()

    // Humanos e meio-elfos consideram qualquer classe como favorecida.
    val anyClassFavored = race.favoredClass.equals("Qualquer", ignoreCase = true)
    val effectiveFavored = if (anyClassFavored) {
        classesList.maxByOrNull { it.level }?.className ?: "Qualquer classe"
    } else {
        race.favoredClass
    }

    // Cálculos preliminares ao vivo
    val totalLevel = classesList.sumOf { it.level }

    var totalBab = 0
    var totalFort = 0
    var totalRef = 0
    var totalWill = 0

    for (entry in classesList) {
        val info = Dnd35Rules.CLASSES.find { it.name.equals(entry.className, ignoreCase = true) } ?: continue
        totalBab += Dnd35Rules.calculateBaseAttackBonus(info.babType, entry.level)
        totalFort += Dnd35Rules.calculateBaseSave(info.fortType, entry.level)
        totalRef += Dnd35Rules.calculateBaseSave(info.refType, entry.level)
        totalWill += Dnd35Rules.calculateBaseSave(info.willType, entry.level)
    }

    // Verificação de penalidade de XP do Livro do Jogador
    val xpPenalty = calculateMulticlassXpPenalty(
        classes = classesList,
        favoredClass = race.favoredClass,
        anyClassFavored = anyClassFavored
    )
    val offendingClasses = xpPenalty.offendingClasses
    val xpPenaltyPercent = xpPenalty.percentage
    val hasXpPenalty = xpPenaltyPercent > 0
    val exemptClassName = xpPenalty.exemptClassName ?: effectiveFavored

    val availableClassesToAdd = Dnd35Rules.CLASSES.filter { c ->
        classesList.none { it.className.equals(c.name, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Gerenciar Multiclasse & Níveis", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text(
                    "Regras oficiais D&D 3.5: BBA e Testes de Resistência acumulativos",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(classesList) { entry ->
                        val info = Dnd35Rules.CLASSES.find { it.name.equals(entry.className, ignoreCase = true) }
                            ?: Dnd35Rules.CLASSES.first()
                        val isFavored = entry.className.equals(effectiveFavored, ignoreCase = true)

                        val babPart = Dnd35Rules.calculateBaseAttackBonus(info.babType, entry.level)
                        val fortPart = Dnd35Rules.calculateBaseSave(info.fortType, entry.level)
                        val refPart = Dnd35Rules.calculateBaseSave(info.refType, entry.level)
                        val willPart = Dnd35Rules.calculateBaseSave(info.willType, entry.level)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(entry.className, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        if (entry == classesList.first()) {
                                            Text(
                                                "Inicial",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "d${info.hitDie}",
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        if (isFavored) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = DndGold, modifier = Modifier.size(14.dp))
                                                Text(if (anyClassFavored) "Isenta de XP" else "Favorecida", style = MaterialTheme.typography.labelSmall, color = DndGold, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    // Controles de Nível
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                if (entry.level > 1) {
                                                    classesList = classesList.map {
                                                        if (it.className == entry.className) it.copy(level = it.level - 1) else it
                                                    }
                                                }
                                            },
                                            enabled = entry.level > 1,
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Diminuir Nível", modifier = Modifier.size(16.dp))
                                        }

                                        Text(
                                            "Nv ${entry.level}",
                                            fontWeight = FontWeight.ExtraBold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp)
                                        )

                                        IconButton(
                                            onClick = {
                                                classesList = classesList.map {
                                                    if (it.className == entry.className) it.copy(level = it.level + 1) else it
                                                }
                                            },
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Aumentar Nível", modifier = Modifier.size(16.dp))
                                        }

                                        // A primeira classe registrada é a classe inicial e determina o multiplicador x4.
                                        // Não permitir removê-la evita transferir esse benefício para uma classe posterior.
                                        if (classesList.size > 1 && entry != classesList.first()) {
                                            IconButton(
                                                onClick = {
                                                    classesList = classesList.filterNot { it.className == entry.className }
                                                },
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Remover Classe", tint = DndCrimson, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Contribuições desta classe
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("BBA: +$babPart", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                                    Text("Fort: +$fortPart", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Ref: +$refPart", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Vont: +$willPart", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Botão para Adicionar Nova Classe à Multiclasse
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showAddMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = availableClassesToAdd.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (availableClassesToAdd.isNotEmpty()) "Adicionar Nova Classe à Ficha" else "Todas as 11 Classes Adicionadas")
                    }

                    DropdownMenu(
                        expanded = showAddMenu,
                        onDismissRequest = { showAddMenu = false }
                    ) {
                        for (cls in availableClassesToAdd) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(cls.name, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("(d${cls.hitDie})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = {
                                    classesList = classesList + CharacterClassEntry(cls.name, 1)
                                    showAddMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cartão Resumo de Progressão Acumulada
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Nível Total: Nv $totalLevel", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("BBA Combinado: +$totalBab", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Bases: Fort +$totalFort • Ref +$totalRef • Vont +$totalWill", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Cartão da Regra de XP de Multiclasse (D&D 3.5)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasXpPenalty) DndCrimson.copy(alpha = 0.15f) else DndHpGreen.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = if (hasXpPenalty) DndCrimson else DndHpGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                if (hasXpPenalty) "Penalidade de XP: -${xpPenaltyPercent}%" else "Penalidade de XP: Nenhuma",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (hasXpPenalty) DndCrimson else DndHpGreen
                            )
                            val explanation = if (hasXpPenalty) {
                                "Classes não favorecidas diferem em 2 ou mais níveis (${offendingClasses.joinToString { it.className }}). Favorecida: $effectiveFavored."
                            } else {
                                if (anyClassFavored) {
                                    "A classe de nível mais alto ($exemptClassName) é desconsiderada no cálculo; as demais classes ainda podem gerar penalidade."
                                } else {
                                    "Suas classes estão equilibradas ou protegidas pela classe favorecida ($effectiveFavored)."
                                }
                            }
                            Text(explanation, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(classesList) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Salvar Progressão")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
