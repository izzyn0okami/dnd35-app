package com.example.dnd35.ui.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.dnd35.model.CalculatedCharacterStats
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.CharacterInventoryItem
import com.example.dnd35.model.Dnd35Rules
import com.example.dnd35.model.ammunitionBundleSize
import com.example.dnd35.model.remainingAmmunitionUnits
import com.example.dnd35.ui.dialogs.AddEditItemDialog
import com.example.dnd35.viewmodel.CharacterViewModel
import com.example.ui.theme.DndCrimson
import com.example.ui.theme.DndGold
import com.example.ui.theme.DndHpGreen
import com.example.ui.theme.DndHpYellow

internal fun parseNonNegativeCoinAmount(raw: String): Float? {
    val normalized = raw.trim().replace(',', '.')
    val amount = normalized.toFloatOrNull() ?: return null
    return amount.takeIf { it.isFinite() && it >= 0f }
}

@Composable
fun InventoryTab(
    viewModel: CharacterViewModel,
    stats: CalculatedCharacterStats,
    character: CharacterEntity
) {
    var showAddItemModal by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<CharacterInventoryItem?>(null) }
    var showCoinsModal by remember { mutableStateOf(false) }
    var showQuickGoldDialog by remember { mutableStateOf<Boolean?>(null) } // true: adicionar, false: gastar
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    // Fechado por padrão: é uma seção de configuração, não o que a maioria olha primeiro
    // ao abrir o inventário (ver lista de itens e adicionar itens).
    var expandSlotsView by remember { mutableStateOf(false) }
    var itemForSlotAssign by remember { mutableStateOf<CharacterInventoryItem?>(null) }
    var pendingItemDeleteId by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Todas", "Armas", "Armaduras", "Escudos", "Itens Mágicos", "Itens de Aventura", "Consumíveis")

    val cap = stats.carryingCapacity
    val droppedBackpackWeight = stats.inventory.filter {
        !it.inBackpack && !it.equipped && it.equippedSlot == Dnd35Rules.EquipmentSlots.NONE
    }.fold(0f) { total, item -> total + item.weightLb * item.quantity }
    val totalWeight = (stats.totalWeightLb - droppedBackpackWeight).coerceAtLeast(0f)
    val encumbrance = Dnd35Rules.calculateEncumbrance(totalWeight, character.baseStr + character.modStrBonus, character.size)
    val hasDroppedBackpackContents = stats.inventory.any {
        !it.inBackpack && !it.equipped && it.equippedSlot == Dnd35Rules.EquipmentSlots.NONE
    }
    val hasBackpackContents = stats.inventory.any {
        it.inBackpack && !it.equipped && it.equippedSlot == Dnd35Rules.EquipmentSlots.NONE
    }
    val heavyMax = maxOf(1, cap.heavyMaxLb).toFloat()
    val weightFraction = (totalWeight / heavyMax).coerceIn(0f, 1f)
    val animatedWeightFraction by animateFloatAsState(targetValue = weightFraction, label = "weight_progress")

    val encumbranceColor = when (encumbrance) {
        Dnd35Rules.EncumbranceLevel.LIGHT -> DndHpGreen
        Dnd35Rules.EncumbranceLevel.MEDIUM -> DndHpYellow
        Dnd35Rules.EncumbranceLevel.HEAVY -> DndCrimson
        Dnd35Rules.EncumbranceLevel.OVERLOADED -> Color.Red
    }

    val totalEquivalentGp = character.goldGp + (character.silverSp / 10f) + (character.copperCp / 100f) + (character.platinumPp * 10f)
    val totalCoinsCount = character.goldGp.toInt() + character.silverSp.toInt() + character.copperCp.toInt() + character.platinumPp.toInt()
    val coinsWeightLb = totalCoinsCount / 50.0f

    val filteredItems = if (selectedCategoryIndex == 0) {
        stats.inventory
    } else {
        val selectedCat = categories[selectedCategoryIndex]
        stats.inventory.filter { it.category.equals(selectedCat, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("inventory_tab")
    ) {
        // 1. Bloco de Gestão de Carga & Peso (Tabela 9-1 PHB 3.5)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = DndGold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Peso e carga (D&D 3.5)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = encumbranceColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                encumbrance.label,
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text("PESO TOTAL CARREGADO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "${String.format("%.1f", totalWeight)} lb",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = encumbranceColor
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("CAPACIDADE MÁXIMA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "${cap.heavyMaxLb} lb",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.25f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedWeightFraction)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(encumbranceColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        LoadLimitInfo("Leve", "0–${cap.lightMaxLb} lb", DndHpGreen)
                        LoadLimitInfo("Média", "${cap.lightMaxLb + 1}–${cap.mediumMaxLb} lb", DndHpYellow)
                        LoadLimitInfo("Pesada", "${cap.mediumMaxLb + 1}–${cap.heavyMaxLb} lb", DndCrimson)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 2. Painel de Espaços de Equipamento Corporais (Body Slots)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandSlotsView = !expandSlotsView },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = DndGold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Espaços de Equipamento (Corpo)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        IconButton(onClick = { expandSlotsView = !expandSlotsView }) {
                            Icon(
                                if (expandSlotsView) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expandir"
                            )
                        }
                    }

                    AnimatedVisibility(visible = expandSlotsView) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            Text(
                                "Atribua itens aos espaços corporais para ativar seus efeitos na CA e atributos:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val mainSlots = listOf(
                                Dnd35Rules.EquipmentSlots.MAIN_HAND,
                                Dnd35Rules.EquipmentSlots.OFF_HAND,
                                Dnd35Rules.EquipmentSlots.ARMOR_BODY,
                                Dnd35Rules.EquipmentSlots.HEAD,
                                Dnd35Rules.EquipmentSlots.NECK,
                                Dnd35Rules.EquipmentSlots.SHOULDERS,
                                Dnd35Rules.EquipmentSlots.HANDS,
                                Dnd35Rules.EquipmentSlots.RING_1,
                                Dnd35Rules.EquipmentSlots.RING_2,
                                Dnd35Rules.EquipmentSlots.WAIST,
                                Dnd35Rules.EquipmentSlots.FEET
                            )

                            for (slotName in mainSlots) {
                                val equippedInSlot = stats.inventory.find {
                                    (it.equippedSlot == slotName) ||
                                    (it.equipped && it.equippedSlot.isBlank() && defaultSlotMatches(it, slotName))
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        slotName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.width(150.dp)
                                    )

                                    if (equippedInSlot != null) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                equippedInSlot.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(end = 4.dp)
                                            )
                                            IconButton(
                                                onClick = {
                                                    viewModel.assignItemSlot(equippedInSlot.id, Dnd35Rules.EquipmentSlots.NONE)
                                                },
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Desequipar ${equippedInSlot.name}", tint = DndCrimson, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    } else {
                                        Text(
                                            "Vazio",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 3. Bloco Refinado de Moedas e Riqueza com Ações
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = DndGold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Riqueza e moedas", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }

                        IconButton(onClick = { showCoinsModal = true }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar Moedas", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Contadores de Moedas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        CoinBox("PO (Ouro)", character.goldGp)
                        CoinBox("PP (Prata)", character.silverSp)
                        CoinBox("PC (Cobre)", character.copperCp)
                        CoinBox("PL (Platina)", character.platinumPp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        "Total Equivalente: ${String.format("%.1f", totalEquivalentGp)} PO  •  Peso das Moedas: ${String.format("%.2f", coinsWeightLb)} lb (${totalCoinsCount} moedas)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botões rápidos de gestão financeira
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showQuickGoldDialog = true },
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Text("+ Ouro", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = { showQuickGoldDialog = false },
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Text("- Gastar", style = MaterialTheme.typography.labelSmall)
                        }
                        Button(
                            onClick = { viewModel.convertCoinsToGold() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.weight(1.3f).height(36.dp)
                        ) {
                            Icon(Icons.Default.CurrencyExchange, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Converter", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 4. Filtros de Categorias de Itens
        item {
            Text("Categorias de Itens:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(categories.indices.toList()) { index ->
                    FilterChip(
                        selected = selectedCategoryIndex == index,
                        onClick = { selectedCategoryIndex = index },
                        label = { Text(categories[index]) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Ações da mochila: mover todos os itens guardados de uma só vez.
        item {
            OutlinedButton(
                onClick = { viewModel.setBackpackContentsCarried(carried = hasDroppedBackpackContents) },
                enabled = hasDroppedBackpackContents || hasBackpackContents,
                modifier = Modifier.fillMaxWidth().testTag("toggle_backpack_contents")
            ) {
                Text(if (hasDroppedBackpackContents) "Pegar mochila inteira" else "Soltar mochila inteira")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 5. Barra de Ação "+ Adicionar Item"
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Itens no Inventário (${filteredItems.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { showAddItemModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adicionar Item")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 6. Lista de Itens do Inventário
        if (filteredItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Nenhum item nesta categoria.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Toque em '+ Adicionar Item' para explorar o catálogo do Livro do Jogador.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(filteredItems) { item ->
                InventoryItemRowRefined(
                    item = item,
                    onOpenSlotPicker = { itemForSlotAssign = item },
                    onEdit = { itemToEdit = item },
                    onDelete = { pendingItemDeleteId = item.id }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal de Adição de Item
    if (showAddItemModal) {
        AddEditItemDialog(
            itemToEdit = null,
            onConfirm = { newItem -> viewModel.addInventoryItem(newItem) },
            onDismiss = { showAddItemModal = false }
        )
    }

    // Modal de Edição de Item
    itemToEdit?.let { item ->
        AddEditItemDialog(
            itemToEdit = item,
            onConfirm = { updated ->
                viewModel.removeInventoryItem(item.id)
                viewModel.addInventoryItem(
                    updated.copy(
                        id = item.id,
                        equipped = item.equipped,
                        equippedSlot = item.equippedSlot,
                        inBackpack = item.inBackpack
                    )
                )
                itemToEdit = null
            },
            onDismiss = { itemToEdit = null }
        )
    }

    pendingItemDeleteId?.let { itemId ->
        AlertDialog(
            onDismissRequest = { pendingItemDeleteId = null },
            title = { Text("Excluir item?") },
            text = { Text("Esta ação não pode ser desfeita.") },
            confirmButton = { Button(onClick = { viewModel.removeInventoryItem(itemId); pendingItemDeleteId = null }) { Text("Excluir") } },
            dismissButton = { OutlinedButton(onClick = { pendingItemDeleteId = null }) { Text("Cancelar") } }
        )
    }

    // Modal para Atribuir Item a Espaço de Corpo Específico
    itemForSlotAssign?.let { item ->
        AlertDialog(
            onDismissRequest = { itemForSlotAssign = null },
            title = { Text("Equipar: ${item.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Selecione em qual espaço corporal este item será equipado:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(modifier = Modifier.height(260.dp)) {
                        items(Dnd35Rules.EquipmentSlots.ALL_SLOTS) { slotName ->
                            val isSelected = item.equippedSlot == slotName || (item.equipped && defaultSlotMatches(item, slotName))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable {
                                        viewModel.assignItemSlot(item.id, slotName)
                                        itemForSlotAssign = null
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(slotName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = "Selecionado", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = { itemForSlotAssign = null }) {
                    Text("Fechar")
                }
            }
        )
    }

    // Modal para Adicionar / Gastar Ouro Rápido
    showQuickGoldDialog?.let { isAdding ->
        var goldInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showQuickGoldDialog = null },
            title = { Text(if (isAdding) "Adicionar Peças de Ouro (PO)" else "Gastar Peças de Ouro (PO)") },
            text = {
                OutlinedTextField(
                    value = goldInput,
                    onValueChange = { goldInput = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Quantidade em PO") },
                    singleLine = true,
                    placeholder = { Text("ex: 50") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    val delta = goldInput.toFloatOrNull() ?: return@Button
                    if (delta <= 0f) return@Button
                    if (isAdding) viewModel.quickCoinAction(delta)
                    else viewModel.quickCoinAction(-delta)
                    showQuickGoldDialog = null
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showQuickGoldDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal de Edição Detalhada de Moedas
    if (showCoinsModal) {
        var gpText by remember { mutableStateOf("${character.goldGp.toInt()}") }
        var spText by remember { mutableStateOf("${character.silverSp.toInt()}") }
        var cpText by remember { mutableStateOf("${character.copperCp.toInt()}") }
        var ppText by remember { mutableStateOf("${character.platinumPp.toInt()}") }
        var coinValidationError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showCoinsModal = false },
            title = { Text("Ajustar Moedas", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "10 PC = 1 PP • 10 PP = 1 PO • 10 PO = 1 PL. 50 moedas pesam 1 libra (lb).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = gpText,
                        onValueChange = { gpText = it; coinValidationError = null },
                        label = { Text("Peças de Ouro (PO)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = spText,
                        onValueChange = { spText = it; coinValidationError = null },
                        label = { Text("Peças de Prata (PP)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cpText,
                        onValueChange = { cpText = it; coinValidationError = null },
                        label = { Text("Peças de Cobre (PC)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ppText,
                        onValueChange = { ppText = it; coinValidationError = null },
                        label = { Text("Peças de Platina (PL)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    coinValidationError?.let { message ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val gp = parseNonNegativeCoinAmount(gpText)
                    val sp = parseNonNegativeCoinAmount(spText)
                    val cp = parseNonNegativeCoinAmount(cpText)
                    val pp = parseNonNegativeCoinAmount(ppText)
                    if (gp == null || sp == null || cp == null || pp == null) {
                        coinValidationError = "Informe valores numéricos válidos e não negativos para todas as moedas."
                        return@Button
                    }
                    viewModel.updateCoins(gp, sp, cp, pp)
                    showCoinsModal = false
                }) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCoinsModal = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun InventoryItemRowRefined(
    item: CharacterInventoryItem,
    onOpenSlotPicker: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val totalItemWeight = item.weightLb * item.quantity
    val isEquipped = item.equipped || (item.equippedSlot != Dnd35Rules.EquipmentSlots.NONE && item.equippedSlot.isNotBlank())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isEquipped) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!item.imageUri.isNullOrBlank()) {
                AsyncImage(
                    model = item.imageUri,
                    contentDescription = "Foto de ${item.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onEdit)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onEdit)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name + when {
                            ammunitionBundleSize(item) > 1 -> " (${remainingAmmunitionUnits(item)} munições restantes)"
                            item.quantity > 1 -> " (x${item.quantity})"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (isEquipped) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DndGold),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "EQUIPADO",
                                color = Color.Black,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Espaço Corporal Atribuído
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onOpenSlotPicker)
                ) {
                    Text(
                        "Espaço: ${item.equippedSlot}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Alterar espaço de equipamento", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${item.category} • ${String.format("%.1f", totalItemWeight)} lb" + (if (item.costGp > 0) " • ${item.costGp} PO" else ""),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (item.damageOrAc.isNotBlank() && item.damageOrAc != "-") {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            item.damageOrAc,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (!item.inBackpack && !isEquipped) {
                    Text("Fora da mochila", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }

                if (item.notes.isNotBlank()) {
                    Text(
                        item.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onOpenSlotPicker, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Shield, contentDescription = "Atribuir espaço de equipamento", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Editar ${item.name}", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir ${item.name}", modifier = Modifier.size(18.dp), tint = DndCrimson)
            }
        }
    }
}

fun defaultSlotMatches(item: CharacterInventoryItem, slotName: String): Boolean {
    return when (item.category.lowercase()) {
        "armas", "arma" -> slotName == Dnd35Rules.EquipmentSlots.MAIN_HAND
        "armaduras", "armadura" -> slotName == Dnd35Rules.EquipmentSlots.ARMOR_BODY
        "escudos", "escudo" -> slotName == Dnd35Rules.EquipmentSlots.OFF_HAND
        else -> false
    }
}

@Composable
private fun LoadLimitInfo(title: String, range: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
        Text(range, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CoinBox(label: String, value: Float) {
    val displayStr = if (value % 1f == 0f) value.toInt().toString() else String.format("%.1f", value)
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                displayStr,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = DndGold
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
