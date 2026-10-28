package com.example.dnd35.ui.dialogs

import android.net.Uri
import android.content.Intent
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.dnd35.model.CharacterInventoryItem
import com.example.dnd35.model.Dnd35Rules

@Composable
fun AddEditItemDialog(
    itemToEdit: CharacterInventoryItem? = null,
    onConfirm: (CharacterInventoryItem) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(if (itemToEdit == null) 0 else 1) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var photoError by remember { mutableStateOf<String?>(null) }
    var isSavingPhoto by remember { mutableStateOf(false) }

    // Copia a foto para o armazenamento privado do app. Assim, a imagem não depende
    // das permissões temporárias do seletor nem da disponibilidade do provedor original.
    var imageUri by remember { mutableStateOf(itemToEdit?.imageUri) }
    var ammoUnitsPerPackageText by remember {
        mutableStateOf(
            (itemToEdit?.ammunitionUnitsPerPackage
                ?: Regex("\\\\((\\\\d+)\\\\)$").find(itemToEdit?.name.orEmpty())?.groupValues?.getOrNull(1)?.toIntOrNull()
                ?: 1).toString()
        )
    }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            photoError = null
            // OpenDocument normalmente concede acesso persistente; a cópia privada continua sendo a fonte final.
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            isSavingPhoto = true
            coroutineScope.launch {
                val savedUri = withContext(Dispatchers.IO) {
                    runCatching {
                        val photoDir = File(context.filesDir, "item_photos").apply {
                            check(isDirectory || mkdirs()) { "Não foi possível criar a pasta privada das fotos." }
                        }
                        val mimeType = context.contentResolver.getType(uri)
                        val extension = android.webkit.MimeTypeMap.getSingleton()
                            .getExtensionFromMimeType(mimeType)
                            ?.takeIf { it.matches(Regex("[A-Za-z0-9]{2,5}")) }
                            ?: "jpg"
                        val photoFile = File(photoDir, "${UUID.randomUUID()}.$extension")
                        try {
                            val input = context.contentResolver.openInputStream(uri)
                                ?: error("Não foi possível abrir a imagem selecionada.")
                            input.use { source ->
                                photoFile.outputStream().use { output -> source.copyTo(output) }
                            }
                            check(photoFile.length() > 0L) { "A imagem selecionada está vazia." }
                            Uri.fromFile(photoFile).toString()
                        } catch (error: Throwable) {
                            photoFile.delete()
                            throw error
                        }
                    }
                }
                savedUri.onSuccess { imageUri = it }
                    .onFailure { photoError = "Não foi possível salvar a foto. Tente escolher outra imagem." }
                isSavingPhoto = false
            }
        }
    }

    // Campos do item customizado
    var name by remember { mutableStateOf(itemToEdit?.name ?: "") }
    var category by remember { mutableStateOf(itemToEdit?.category ?: "Itens de Aventura") }
    var quantityText by remember { mutableStateOf("${itemToEdit?.quantity ?: 1}") }
    var weightText by remember { mutableStateOf("${itemToEdit?.weightLb ?: 1.0f}") }
    var costText by remember { mutableStateOf("${itemToEdit?.costGp ?: 0.0f}") }
    var damageOrAc by remember { mutableStateOf(itemToEdit?.damageOrAc ?: "") }
    var notes by remember { mutableStateOf(itemToEdit?.notes ?: "") }
    var equipped by remember { mutableStateOf(itemToEdit?.equipped ?: false) }
    val parsedQuantity = quantityText.toIntOrNull()
    val parsedWeight = weightText.toFloatOrNull()
    val parsedCost = costText.toFloatOrNull()
    var equippedSlot by remember { mutableStateOf(itemToEdit?.equippedSlot ?: Dnd35Rules.EquipmentSlots.NONE) }

    // Busca no catálogo
    var catalogQuery by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var slotExpanded by remember { mutableStateOf(false) }

    val categories = listOf("Armas", "Armaduras", "Escudos", "Itens Mágicos", "Itens de Aventura", "Consumíveis", "Outros")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (itemToEdit == null) "Adicionar Item ao Inventário" else "Editar Item",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (itemToEdit == null) {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Catálogo do Livro do Jogador") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Item personalizado") }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (selectedTab == 0 && itemToEdit == null) {
                    // Catálogo do Livro do Jogador
                    OutlinedTextField(
                        value = catalogQuery,
                        onValueChange = { catalogQuery = it },
                        placeholder = { Text("Buscar armas, armaduras, anéis...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val filtered = Dnd35Rules.PRESET_ITEMS.filter {
                        it.name.contains(catalogQuery, ignoreCase = true) ||
                        it.category.contains(catalogQuery, ignoreCase = true) ||
                        it.defaultSlot.contains(catalogQuery, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        items(filtered) { preset ->
                            val isDefaultEquipped = preset.defaultSlot != Dnd35Rules.EquipmentSlots.NONE
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        val newItem = CharacterInventoryItem(
                                            name = preset.name,
                                            category = preset.category,
                                            quantity = 1,
                                            weightLb = preset.weightLb,
                                            costGp = preset.costGp,
                                            damageOrAc = preset.damageOrAc,
                                            equipped = isDefaultEquipped,
                                            equippedSlot = preset.defaultSlot,
                                            notes = preset.properties
                                        )
                                        onConfirm(newItem)
                                        onDismiss()
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                                        Text(preset.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            "${preset.category} • Espaço: ${preset.defaultSlot}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            "${preset.weightLb} lb • ${preset.costGp} PO" + (if (preset.damageOrAc != "-") " • ${preset.damageOrAc}" else ""),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (preset.properties.isNotBlank()) {
                                            Text(
                                                preset.properties,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Icon(Icons.Default.Add, contentDescription = "Adicionar", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                } else {
                    // Formulário Customizado
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                val inferred = Regex("\\\\((\\\\d+)\\\\)$").find(it.trim())?.groupValues?.getOrNull(1)
                                if (ammoUnitsPerPackageText == "1" && inferred != null) ammoUnitsPerPackageText = inferred
                            },
                            label = { Text("Nome do Item") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Foto do item:", style = MaterialTheme.typography.labelSmall)
                        photoError?.let { errorMessage ->
                            Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (!imageUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = imageUri,
                                    contentDescription = "Foto de $name",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { imagePicker.launch(arrayOf("image/*")) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(if (imageUri.isNullOrBlank()) "Escolher foto" else "Trocar foto")
                                }
                                if (!imageUri.isNullOrBlank()) {
                                    OutlinedButton(
                                        onClick = { imageUri = null },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Remover foto")
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Categoria:", style = MaterialTheme.typography.labelSmall)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { categoryExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(category)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Selecionar categoria")
                                }
                            }
                            DropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            category = cat
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Espaço de equipamento:", style = MaterialTheme.typography.labelSmall)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { slotExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(equippedSlot)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Selecionar espaço de equipamento")
                                }
                            }
                            DropdownMenu(
                                expanded = slotExpanded,
                                onDismissRequest = { slotExpanded = false }
                            ) {
                                Dnd35Rules.EquipmentSlots.ALL_SLOTS.forEach { slot ->
                                    DropdownMenuItem(
                                        text = { Text(slot) },
                                        onClick = {
                                            equippedSlot = slot
                                            equipped = slot != Dnd35Rules.EquipmentSlots.NONE
                                            slotExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = ammoUnitsPerPackageText,
                            onValueChange = { ammoUnitsPerPackageText = it.filter(Char::isDigit).take(4) },
                            label = { Text("Unidades por pacote (1 = avulso)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = quantityText,
                                onValueChange = { quantityText = it },
                                label = { Text("Qtd") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            OutlinedTextField(
                                value = weightText,
                                onValueChange = { weightText = it },
                                label = { Text("Peso unit. (lb)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = costText,
                                onValueChange = { costText = it },
                                label = { Text("Valor (PO)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            OutlinedTextField(
                                value = damageOrAc,
                                onValueChange = { damageOrAc = it },
                                label = { Text("Dano ou CA") },
                                placeholder = { Text("ex: 1d8 / +4 CA") },
                                modifier = Modifier.weight(1.2f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Propriedades / Detalhes") },
                            placeholder = { Text("ex: Des Max +3, Pen -2") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = equipped,
                                onCheckedChange = {
                                    equipped = it
                                    if (!it) equippedSlot = Dnd35Rules.EquipmentSlots.NONE
                                }
                            )
                            Text("Equipado atualmente", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTab == 1 || itemToEdit != null) {
                Button(
                    onClick = {
                        val finalItem = (itemToEdit ?: CharacterInventoryItem(name = name, category = category)).copy(
                            name = if (name.isBlank()) "Novo Item" else name.trim(),
                            category = category,
                            quantity = parsedQuantity ?: return@Button,
                            weightLb = parsedWeight ?: return@Button,
                            costGp = parsedCost ?: return@Button,
                            damageOrAc = damageOrAc,
                            equipped = equipped,
                            equippedSlot = if (equipped && equippedSlot == Dnd35Rules.EquipmentSlots.NONE) Dnd35Rules.EquipmentSlots.MAIN_HAND else equippedSlot,
                            notes = notes,
                            imageUri = imageUri,
                            ammunitionUnitsPerPackage = ammoUnitsPerPackageText.toIntOrNull()?.takeIf { it in 1..9999 },
                            ammunitionUnitsRemaining = if (itemToEdit != null && parsedQuantity == itemToEdit.quantity) itemToEdit.ammunitionUnitsRemaining else null
                        )
                        onConfirm(finalItem)
                        onDismiss()
                    },
                    enabled = !isSavingPhoto && name.isNotBlank() && ammoUnitsPerPackageText.toIntOrNull() in 1..9999 && parsedQuantity != null && parsedQuantity >= 1 && parsedWeight != null && parsedWeight >= 0f && parsedCost != null && parsedCost >= 0f
                ) {
                    Text("Salvar")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
