package com.example.dnd35.ui.dialogs

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.dnd35.model.normalizedForCatalogLookup
import com.example.dnd35.ui.icons.GameIcon
import com.example.dnd35.ui.icons.GameIcons
import com.example.ui.theme.DndGold

/**
 * Seletor do ícone de um item. [selectedKey] é a escolha atual (nulo = automático) e
 * [automaticIcon] é o ícone que o modo automático usaria agora, só para mostrar ao usuário.
 * [onSelect] recebe a chave escolhida ou nulo para voltar ao automático.
 */
@Composable
fun IconPickerDialog(
    selectedKey: String?,
    automaticIcon: GameIcon?,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var group by remember { mutableStateOf<String?>(null) }
    val normalizedQuery = query.normalizedForCatalogLookup()
    val icons = remember(normalizedQuery, group) {
        GameIcons.all.filter { icon ->
            (group == null || icon.group == group) &&
                (normalizedQuery.isEmpty() ||
                    "${icon.label} ${icon.keywords} ${icon.key}".normalizedForCatalogLookup().contains(normalizedQuery))
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(modifier = Modifier.fillMaxWidth(0.94f).heightIn(max = 620.dp).testTag("icon_picker_dialog")) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Escolher ícone", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Buscar (espada, poção, escudo...)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("icon_picker_search")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(selected = group == null, onClick = { group = null }, label = { Text("Todos") })
                    GameIcons.groups.forEach { name ->
                        FilterChip(
                            selected = group == name,
                            onClick = { group = if (group == name) null else name },
                            label = { Text(name) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (icons.isEmpty()) {
                    Text(
                        "Nenhum ícone encontrado.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(76.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(icons, key = { it.key }) { icon ->
                            val selected = icon.key == selectedKey
                            val shape = RoundedCornerShape(8.dp)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp)
                                    .clip(shape)
                                    .border(
                                        width = if (selected) 2.dp else 1.dp,
                                        color = if (selected) DndGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = shape
                                    )
                                    .clickable { onSelect(icon.key) }
                                    .padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(icon.resId),
                                    contentDescription = null,
                                    tint = if (selected) DndGold else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    icon.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { onSelect(null) }, modifier = Modifier.testTag("icon_picker_automatic")) {
                        if (automaticIcon != null) {
                            Icon(
                                painter = painterResource(automaticIcon.resId),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(if (selectedKey == null) "Automático (atual)" else "Voltar ao automático")
                    }
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                }
            }
        }
    }
}
