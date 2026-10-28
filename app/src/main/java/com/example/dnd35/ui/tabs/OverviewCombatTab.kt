package com.example.dnd35.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dnd35.model.CalculatedCharacterStats
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.Dnd35Rules
import com.example.dnd35.ui.components.CharacterAvatar
import com.example.dnd35.ui.components.HpBarSection
import com.example.dnd35.ui.components.PhotoPickerModal
import com.example.dnd35.ui.dialogs.DiceRollDialog
import com.example.dnd35.ui.dialogs.EditAbilityDialog
import com.example.dnd35.ui.dialogs.EditArmorDialog
import com.example.dnd35.ui.dialogs.EditGeneralInfoDialog
import com.example.dnd35.ui.dialogs.ManageMulticlassDialog
import com.example.dnd35.viewmodel.CharacterViewModel
import com.example.ui.theme.DndCrimson
import com.example.ui.theme.DndGold
import com.example.ui.theme.DndHpGreen

@Composable
fun OverviewCombatTab(
    viewModel: CharacterViewModel,
    stats: CalculatedCharacterStats,
    character: CharacterEntity
) {
    var showPhotoModal by remember { mutableStateOf(false) }
    var showGeneralInfoModal by remember { mutableStateOf(false) }
    var showArmorModal by remember { mutableStateOf(false) }
    var showMulticlassModal by remember { mutableStateOf(false) }
    var editingAbility by remember { mutableStateOf<Pair<String, Triple<Int, Int, Int>>?>(null) } // Name to (Base, Magic, Racial)
    var diceRollTarget by remember { mutableStateOf<Triple<String, Int, String>?>(null) } // Title, Bonus, Damage
    var showAmmunitionPicker by remember { mutableStateOf(false) }

    val equippedAmmunition = stats.inventory.firstOrNull {
        it.equippedSlot == Dnd35Rules.EquipmentSlots.AMMUNITION && it.quantity > 0
    }
    val ammunitionOptions = stats.inventory.filter {
        it.quantity > 0 && it.inBackpack && !it.equipped &&
            (it.equippedSlot == Dnd35Rules.EquipmentSlots.NONE || it.equippedSlot == Dnd35Rules.EquipmentSlots.AMMUNITION)
    }
    val race = Dnd35Rules.RACES.find { it.name.equals(character.race, ignoreCase = true) } ?: Dnd35Rules.RACES.first()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("overview_tab")
    ) {
        // 1. Cabeçalho do Herói: retrato quadrado com informações logo abaixo.
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RectangleShape)
                    .testTag("character_hero_header")
            ) {
                CharacterAvatar(
                    photoUri = character.photoUri,
                    shape = RectangleShape,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { showPhotoModal = true }
                )

                // Botão de edição com alvo de toque confortável.
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.58f))
                        .clickable { showPhotoModal = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Trocar Foto",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showGeneralInfoModal = true }
                    .testTag("character_hero_info"),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = character.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar Informações",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "${stats.classesDisplay} • Nível ${stats.totalCharacterLevel}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${character.race} • ${character.alignment}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (character.deity.isNotBlank() && character.deity != "Nenhuma") {
                        Text(
                            text = "Divindade: ${character.deity}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Slot único de munição, escolhido manualmente pelo jogador e independente da arma.
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Munição equipada", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        equippedAmmunition?.let { "${it.name} — ${it.quantity} unidade(s)" } ?: "Nenhuma pilha selecionada",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { showAmmunitionPicker = true }, modifier = Modifier.weight(1f)) {
                            Text(if (equippedAmmunition == null) "Equipar munição" else "Trocar munição")
                        }
                        Button(
                            onClick = { viewModel.consumeEquippedAmmunition() },
                            enabled = equippedAmmunition != null,
                            modifier = Modifier.weight(1f)
                        ) { Text("Gastar 1") }
                    }
                }
            }
        }

        // 2. Barra de Pontos de Vida (HP) com Ações
        item {
            HpBarSection(
                currentHp = character.currentHp,
                maxHp = character.maxHp,
                tempHp = character.tempHp,
                nonLethal = character.nonLethalDamage,
                onAdjustHp = { viewModel.adjustHp(it) },
                onSetMaxHp = { viewModel.setMaxHp(it) },
                onSetTempHp = { viewModel.setTempHp(it) },
                onSetNonLethal = { viewModel.setNonLethal(it) },
                onLongRest = { viewModel.longRest() }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 2.1 Resumo de Combate: o que mais importa de olhar rápido durante a partida —
        // Iniciativa, BBA corpo a corpo e à distância (prontos para somar ao d20) e Deslocamento.
        // Fica logo após a barra de PV, antes de qualquer seção administrativa/de referência.
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("combat_summary_section"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Resumo de Combate",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CombatSummaryStat(
                            label = "Iniciativa",
                            value = if (stats.initiativeTotal >= 0) "+${stats.initiativeTotal}" else "${stats.initiativeTotal}",
                            modifier = Modifier.weight(1f),
                            onClick = { diceRollTarget = Triple("Iniciativa", stats.initiativeTotal, "") }
                        )
                        CombatSummaryStat(
                            label = "CA",
                            value = "${stats.totalAc}",
                            subtitle = "Toque ${stats.touchAc} • Surp. ${stats.flatFootedAc}",
                            modifier = Modifier.weight(1f),
                            onClick = { showArmorModal = true }
                        )
                        CombatSummaryStat(
                            label = "Deslocamento",
                            value = "${stats.speedMetersEffective} m",
                            modifier = Modifier.weight(1f),
                            onClick = {}
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CombatSummaryStat(
                            label = "BBA Corpo a Corpo",
                            value = stats.meleeAttackDisplay,
                            modifier = Modifier.weight(1f),
                            highlight = true,
                            onClick = {
                                val firstAttack = parseFirstAttackBonus(stats.meleeAttackDisplay)
                                diceRollTarget = Triple("Ataque Corpo a Corpo", firstAttack, "")
                            }
                        )
                        CombatSummaryStat(
                            label = "BBA à Distância",
                            value = stats.rangedAttackDisplay,
                            modifier = Modifier.weight(1f),
                            highlight = true,
                            onClick = {
                                val firstAttack = parseFirstAttackBonus(stats.rangedAttackDisplay)
                                diceRollTarget = Triple("Ataque à Distância", firstAttack, "")
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "BBA base (sem FOR/DES/tamanho): ${stats.babDisplay}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 2.5 Classes & Multiclasse (D&D 3.5)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Classes & Multiclasse (D&D 3.5)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                if (stats.isMulticlass) "Personagem Multiclasse (Nível ${stats.totalCharacterLevel})" else "Classe Única (Nível ${stats.totalCharacterLevel})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = { showMulticlassModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.height(48.dp),
                            contentPadding = ButtonDefaults.TextButtonContentPadding
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gerenciar", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    for (detail in stats.multiclassDetails) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            detail.entry.className,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "d${detail.hitDie}",
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        if (detail.isFavored) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(Icons.Default.Star, contentDescription = "Favorecida", tint = DndGold, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Text(
                                        "BBA: +${detail.babContribution} • Fort +${detail.baseFort} • Ref +${detail.baseRef} • Vont +${detail.baseWill}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    "Nv ${detail.entry.level}",
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val penalty = stats.multiclassPenalty
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (penalty.hasPenalty) DndCrimson.copy(alpha = 0.15f) else DndHpGreen.copy(alpha = 0.15f)
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
                                tint = if (penalty.hasPenalty) DndCrimson else DndHpGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                penalty.explanation,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 3. Grid dos 6 Atributos D&D 3.5 com Cálculos Automáticos
        item {
            Text(
                "Atributos (Toque para ajustar base/itens):",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            val abilityList = listOf(
                AbilityData("FOR", stats.totalStr, stats.strMod, character.baseStr, character.modStrBonus, race.strMod),
                AbilityData("DES", stats.totalDex, stats.dexMod, character.baseDex, character.modDexBonus, race.dexMod),
                AbilityData("CON", stats.totalCon, stats.conMod, character.baseCon, character.modConBonus, race.conMod),
                AbilityData("INT", stats.totalInt, stats.intMod, character.baseInt, character.modIntBonus, race.intMod),
                AbilityData("SAB", stats.totalWis, stats.wisMod, character.baseWis, character.modWisBonus, race.wisMod),
                AbilityData("CAR", stats.totalCha, stats.chaMod, character.baseCha, character.modChaBonus, race.chaMod)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (ab in abilityList.take(3)) {
                    AbilityCard(
                        data = ab,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            editingAbility = Pair(ab.name, Triple(ab.base, ab.magic, ab.racial))
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (ab in abilityList.drop(3)) {
                    AbilityCard(
                        data = ab,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            editingAbility = Pair(ab.name, Triple(ab.base, ab.magic, ab.racial))
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 4. Classe de Armadura (CA) & Defesas
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showArmorModal = true },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = DndGold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Classe de Armadura (CA)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Icon(Icons.Default.Edit, contentDescription = "Editar Defesas", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TOTAL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${stats.totalAc}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TOQUE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${stats.touchAc}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SURPRESO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${stats.flatFootedAc}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "10 + Armadura (+${stats.armorBonusTotal}) + Escudo (+${stats.shieldBonusTotal}) + Des Ef. (+${stats.effectiveDexForAc}) + Nat (+${character.naturalArmor}) + Def (+${character.deflectionBonus})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (stats.armorCheckPenalty < 0) {
                        Text(
                            "Penalidade de Armadura total: ${stats.armorCheckPenalty} (aplicada às perícias físicas)",
                            style = MaterialTheme.typography.labelSmall,
                            color = DndCrimson,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 5. Testes de Resistência (Fortitude, Reflexos, Vontade)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Testes de Resistência", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SaveBox(
                            name = "Fortitude (CON)",
                            total = stats.totalFort,
                            base = stats.baseFort,
                            mod = stats.conMod,
                            onClick = {
                                diceRollTarget = Triple("Teste de Fortitude", stats.totalFort, "")
                            }
                        )
                        SaveBox(
                            name = "Reflexos (DES)",
                            total = stats.totalRef,
                            base = stats.baseRef,
                            mod = stats.dexMod,
                            onClick = {
                                diceRollTarget = Triple("Teste de Reflexos", stats.totalRef, "")
                            }
                        )
                        SaveBox(
                            name = "Vontade (SAB)",
                            total = stats.totalWill,
                            base = stats.baseWill,
                            mod = stats.wisMod,
                            onClick = {
                                diceRollTarget = Triple("Teste de Vontade", stats.totalWill, "")
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 6. Nível de Carga (detalhe que não cabia no Resumo de Combate acima)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nível de Carga", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        stats.encumbrance.label,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (stats.encumbrance.armorCheckPenalty < 0) DndCrimson else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 7. Armas e Ataques Rápidos
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ataques & Armas", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Toque para rolar d20", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val weapons = stats.inventory.filter { it.category.startsWith("Arma", ignoreCase = true) }
                    if (weapons.isEmpty()) {
                        val unarmedBonus = parseFirstAttackBonus(stats.meleeAttackDisplay)
                        val unarmedDamage = stats.monkUnarmedDamage?.let { "$it+${stats.strMod}" } ?: "1d3+${stats.strMod} (Concussão)"
                        AttackRow(
                            weaponName = if (stats.classInfo.name.equals("Monge", ignoreCase = true)) "Golpe Desarmado de Monge" else "Ataque Desarmado",
                            attackBonus = unarmedBonus,
                            attackProgression = stats.meleeAttackDisplay,
                            damageNotation = unarmedDamage,
                            notes = if (stats.classInfo.name.equals("Monge", ignoreCase = true)) "Dano letal desarmado e Rajada de Golpes" else "Ataque básico sem armas",
                            onRoll = {
                                diceRollTarget = Triple("Ataque Desarmado", unarmedBonus, unarmedDamage)
                            }
                        )
                    } else {
                        for (w in weapons) {
                            val isRanged = w.notes.contains("alcance", ignoreCase = true) || w.name.contains("Arco", ignoreCase = true) || w.name.contains("Besta", ignoreCase = true) || w.name.contains("Funda", ignoreCase = true)
                            val atkBonus = if (isRanged) parseFirstAttackBonus(stats.rangedAttackDisplay) else parseFirstAttackBonus(stats.meleeAttackDisplay)
                            AttackRow(
                                weaponName = w.name,
                                attackBonus = atkBonus,
                                attackProgression = if (isRanged) stats.rangedAttackDisplay else stats.meleeAttackDisplay,
                                damageNotation = w.damageOrAc,
                                notes = w.notes,
                                onRoll = {
                                    diceRollTarget = Triple("Ataque com ${w.name}", atkBonus, w.damageOrAc)
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 8. Habilidades Especiais da Classe & Raça (D&D 3.5)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Habilidades Especiais (${character.characterClass} Nv ${character.level} • ${character.race})",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Badges de Destaque da Classe
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        stats.sneakAttackDice?.let { dice ->
                            Card(colors = CardDefaults.cardColors(containerColor = DndCrimson), shape = RoundedCornerShape(8.dp)) {
                                Text("Ataque Furtivo: $dice", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                        stats.rageUsesPerDay?.let { uses ->
                            Card(colors = CardDefaults.cardColors(containerColor = DndCrimson), shape = RoundedCornerShape(8.dp)) {
                                Text("Fúria: ${if (uses == 99) "Ilimitada" else "${uses}x/dia"}", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                        stats.monkUnarmedDamage?.let { dmg ->
                            Card(colors = CardDefaults.cardColors(containerColor = DndGold), shape = RoundedCornerShape(8.dp)) {
                                Text("Dano Desarmado: $dmg", color = Color.Black, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                        stats.turningUsesPerDay?.let { uses ->
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(8.dp)) {
                                Text("Expulsão: ${uses}x/dia", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Traços Raciais
                    Text("Herança Racial (${character.race}):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text(stats.raceInfo.traits, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(8.dp))

                    // Lista de Habilidades de Classe Desbloqueadas até o Nível Atual
                    Text("Habilidades de Classe Desbloqueadas:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    for (feat in stats.activeClassFeatures) {
                        Column(modifier = Modifier.padding(vertical = 3.dp)) {
                            Text("• [Nv ${feat.level}] ${feat.name}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            Text(feat.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modais
    if (showPhotoModal) {
        PhotoPickerModal(
            currentPhotoUri = character.photoUri,
            onPhotoSelected = { uri -> viewModel.setPhotoUri(uri) },
            onDismiss = { showPhotoModal = false }
        )
    }

    if (showGeneralInfoModal) {
        EditGeneralInfoDialog(
            character = character,
            onSave = { name, charClass, r, lvl, align, deity, size ->
                viewModel.updateGeneralInfo(name, charClass, r, lvl, align, deity, size)
            },
            onOpenMulticlass = { showMulticlassModal = true },
            onDismiss = { showGeneralInfoModal = false }
        )
    }

    if (showMulticlassModal) {
        ManageMulticlassDialog(
            initialClasses = stats.classEntries,
            raceName = character.race,
            onConfirm = { updatedClasses ->
                viewModel.updateClasses(updatedClasses)
                showMulticlassModal = false
            },
            onDismiss = { showMulticlassModal = false }
        )
    }

    if (showArmorModal) {
        EditArmorDialog(
            character = character,
            onSave = { arm, shield, nat, defl, miscAc, miscFort, miscRef, miscWill, miscInit ->
                viewModel.updateArmorValues(arm, shield, nat, defl, miscAc, miscFort, miscRef, miscWill, miscInit)
            },
            onDismiss = { showArmorModal = false }
        )
    }

    editingAbility?.let { (name, tuple) ->
        val (base, magic, racial) = tuple
        EditAbilityDialog(
            abilityName = name,
            currentBase = base,
            currentMagicBonus = magic,
            racialBonus = racial,
            onSave = { newBase, newMagic ->
                viewModel.updateAbility(name, newBase, newMagic)
            },
            onDismiss = { editingAbility = null }
        )
    }

    diceRollTarget?.let { (title, bonus, damage) ->
        DiceRollDialog(
            actionTitle = title,
            bonus = bonus,
            damageNotation = damage,
            onDismiss = { diceRollTarget = null }
        )
    }

    if (showAmmunitionPicker) {
        AlertDialog(
            onDismissRequest = { showAmmunitionPicker = false },
            title = { Text("Escolher munição") },
            text = {
                LazyColumn(modifier = Modifier.height(280.dp)) {
                    items(ammunitionOptions) { item ->
                        Text(
                            text = "${item.name} (${item.quantity})",
                            modifier = Modifier.fillMaxWidth().clickable {
                                viewModel.equipAmmunition(item.id)
                                showAmmunitionPicker = false
                            }.padding(vertical = 12.dp, horizontal = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (ammunitionOptions.isEmpty()) {
                        item { Text("Não há itens disponíveis para selecionar.") }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { OutlinedButton(onClick = { showAmmunitionPicker = false }) { Text("Fechar") } }
        )
    }
}

data class AbilityData(
    val name: String,
    val total: Int,
    val mod: Int,
    val base: Int,
    val magic: Int,
    val racial: Int
)

@Composable
fun AbilityCard(data: AbilityData, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(data.name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            val modStr = if (data.mod >= 0) "+${data.mod}" else "${data.mod}"
            Text(
                text = modStr,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (data.mod >= 0) DndHpGreen else DndCrimson
            )
            Text(
                text = "${data.total}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SaveBox(name: String, total: Int, base: Int, mod: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(name, style = MaterialTheme.typography.labelSmall)
            val totalStr = if (total >= 0) "+$total" else "$total"
            Text(totalStr, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Text("B:$base M:$mod", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Extrai o primeiro (maior) bônus de uma string de progressão de BBA como "+11/+6/+1". */
private fun parseFirstAttackBonus(progression: String): Int {
    val first = progression.substringBefore('/')
    return first.toIntOrNull() ?: 0
}

@Composable
fun CombatSummaryStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    highlight: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (highlight) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (highlight) Color.White else MaterialTheme.colorScheme.primary
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (highlight) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AttackRow(
    weaponName: String,
    attackBonus: Int,
    attackProgression: String? = null,
    damageNotation: String,
    notes: String,
    onRoll: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                Text(weaponName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                if (attackProgression != null) {
                    Text(
                        "Ataques: $attackProgression",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text("Dano: $damageNotation", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                if (notes.isNotBlank()) {
                    Text(notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            val sign = if (attackBonus >= 0) "+$attackBonus" else "$attackBonus"
            Button(
                onClick = onRoll,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(sign, fontWeight = FontWeight.Bold)
            }
        }
    }
}
