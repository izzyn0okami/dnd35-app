package com.example.dnd35.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dnd35.model.CharacterInventoryItem
import com.example.dnd35.model.InventoryIconKind
import com.example.dnd35.model.inventoryIconKindFor
import com.example.ui.theme.DndGold

private fun InventoryIconKind.toImageVector(): ImageVector = when (this) {
    InventoryIconKind.WEAPON -> Icons.Default.Gavel
    InventoryIconKind.ARMOR -> Icons.Default.Checkroom
    InventoryIconKind.SHIELD -> Icons.Default.Shield
    InventoryIconKind.MAGIC -> Icons.Default.AutoAwesome
    InventoryIconKind.CONSUMABLE -> Icons.Default.Science
    InventoryIconKind.ADVENTURE -> Icons.Default.Backpack
    InventoryIconKind.GENERIC -> Icons.Default.Inventory2
}

/**
 * Ícone padrão de um item do inventário, escolhido pela categoria. Decorativo: o nome do item já
 * aparece ao lado, então não há descrição de acessibilidade própria. Itens com foto
 * personalizada continuam mostrando a foto no lugar deste ícone.
 */
@Composable
fun InventoryItemIcon(
    item: CharacterInventoryItem,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = inventoryIconKindFor(item).toImageVector(),
            contentDescription = null,
            tint = DndGold,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}
