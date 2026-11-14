package com.example.dnd35.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.dnd35.model.CreatureDefinition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Normalizer

@Composable
fun CreatureImage(
    creature: CreatureDefinition,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    maxDimensionPx: Int = 1100
) {
    val context = LocalContext.current
    val assetFiles = remember(context) { context.assets.list("monsters")?.toList().orEmpty() }
    val bitmap by produceState<ImageBitmap?>(initialValue = null, creature.id, creature.name, maxDimensionPx) {
        value = withContext(Dispatchers.IO) {
            val keys = setOf(normalizeCreatureImageKey(creature.name), normalizeCreatureImageKey(creature.id))
            val filename = assetFiles.firstOrNull { file ->
                normalizeCreatureImageKey(file.substringBeforeLast('.', file)) in keys
            }
            if (filename == null) {
                null
            } else {
                val path = "monsters/$filename"
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.assets.open(path).use { BitmapFactory.decodeStream(it, null, bounds) }
                var sampleSize = 1
                while (bounds.outWidth / sampleSize > maxDimensionPx ||
                    bounds.outHeight / sampleSize > maxDimensionPx
                ) sampleSize *= 2
                val options = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = android.graphics.Bitmap.Config.RGB_565
                }
                context.assets.open(path).use { input ->
                    BitmapFactory.decodeStream(input, null, options)?.asImageBitmap()
                }
            }
        }
    }

    Box(modifier = modifier.clip(RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!,
                contentDescription = "Ilustração de ${creature.name}",
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Imagem não disponível", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun normalizeCreatureImageKey(value: String): String =
    Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .lowercase()
        .filter { it.isLetterOrDigit() }

/**
 * Apresenta uma pilha navegável de cartas. A carta da frente pode ser arrastada
 * horizontalmente para revelar a próxima, sem perder o estado de busca/filtro da lista.
 */
@Composable
fun <T> CreatureCardStack(
    items: List<T>,
    key: (T) -> String,
    modifier: Modifier = Modifier,
    card: @Composable (T) -> Unit
) {
    if (items.isEmpty()) return
    val identity = items.map(key)
    var activeIndex by remember(identity) { mutableStateOf(0) }
    val safeIndex = activeIndex.coerceIn(0, items.lastIndex)
    val visibleCount = minOf(items.size, 4)
    val visible = (0 until visibleCount).map { offset ->
        items[(safeIndex + offset) % items.size]
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(370.dp)
                .pointerInput(identity, safeIndex) {
                    var dragDistance = 0f
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, amount ->
                            dragDistance += amount
                            change.consume()
                        },
                        onDragEnd = {
                            if (dragDistance < -48f) activeIndex = (safeIndex + 1) % items.size
                            else if (dragDistance > 48f) activeIndex = (safeIndex - 1 + items.size) % items.size
                            dragDistance = 0f
                        },
                        onDragCancel = { dragDistance = 0f }
                    )
                },
            contentAlignment = Alignment.TopStart
        ) {
            // As cartas seguintes ficam atrás; a carta ativa é desenhada por último.
            visible.drop(1).forEachIndexed { index, item ->
                Box(
                    modifier = Modifier
                        .offset(x = ((index + 1) * 18).dp, y = 5.dp)
                        .width(250.dp)
                        .height(350.dp)
                        .zIndex(index.toFloat())
                ) { card(item) }
            }
            Box(
                modifier = Modifier
                    .width(250.dp)
                    .height(350.dp)
                    .zIndex(10f)
            ) { card(visible.first()) }
        }
        Text(
            "${safeIndex + 1} / ${items.size} • arraste a carta para revelar a próxima",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CreatureImageTradingCard(
    creature: CreatureDefinition,
    details: String,
    stats: String,
    modifier: Modifier = Modifier,
    footer: String? = null,
    selectLabel: String = "Selecionar criatura",
    onSelect: (() -> Unit)? = null
) {
    var showBack by remember(creature.id) { mutableStateOf(false) }
    var showDetails by remember(creature.id) { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth().height(350.dp).combinedClickable(
            onClick = { showBack = !showBack },
            onLongClick = { showDetails = true }
        ),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(creature.name, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        "${creature.type} • ${creature.size} • ND ${creature.challengeRating}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(if (showBack) "VERSO" else "FRENTE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            if (!showBack) {
                CreatureImage(
                    creature = creature,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentScale = ContentScale.Crop,
                    maxDimensionPx = 1000
                )
                Text(details, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(9.dp)) {
                    Text(stats, modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                if (!footer.isNullOrBlank()) {
                    Text(footer, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                }
                Text("Toque para virar • segure para ver a ficha completa",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("ATRIBUTOS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
                    val a = creature.abilities
                    Text("FOR ${a.str ?: "—"} • DES ${a.dex ?: "—"} • CON ${a.con ?: "—"}",
                        style = MaterialTheme.typography.bodySmall)
                    Text("INT ${a.int ?: "—"} • SAB ${a.wis ?: "—"} • CAR ${a.cha ?: "—"}",
                        style = MaterialTheme.typography.bodySmall)
                    Text("Ataques especiais: ${creature.specialAttacks.ifBlank { "—" }}",
                        style = MaterialTheme.typography.labelSmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    Text("Qualidades: ${creature.specialQualities.ifBlank { "—" }}",
                        style = MaterialTheme.typography.labelSmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    Text("Talentos: ${creature.feats.ifBlank { "—" }}",
                        style = MaterialTheme.typography.labelSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (!footer.isNullOrBlank()) {
                        Text(footer, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                    }
                }
                if (onSelect != null) {
                    Button(onClick = onSelect, modifier = Modifier.fillMaxWidth()) { Text(selectLabel) }
                }
                Text("Toque para voltar à frente • segure para ver a ficha completa",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    if (showDetails) CreatureDetailsOverlay(creature, onDismiss = { showDetails = false })
}

@Composable
fun CreatureDetailsOverlay(creature: CreatureDefinition, onDismiss: () -> Unit) {
    CreatureDeckOverlay(
        title = creature.name,
        subtitle = "${creature.typeDetail.ifBlank { creature.type }} • ${creature.size} • ND ${creature.challengeRating}",
        onDismiss = onDismiss
    ) {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                CreatureImage(creature, Modifier.fillMaxWidth().height(300.dp),
                    contentScale = ContentScale.Fit, maxDimensionPx = 1800)
            }
            item { CreatureInfoSection("Descrição") {
                Text(creature.description.ifBlank { "Texto descritivo pendente de conferência no Livro dos Monstros." })
            } }
            item { CreatureInfoSection("Bloco de estatísticas") {
                CreatureInfoField("Dados de Vida", "${creature.hitDice} (${creature.hitPoints} PV)")
                CreatureInfoField("Iniciativa", creature.initiative.creatureSigned())
                CreatureInfoField("Deslocamento", creature.speed)
                CreatureInfoField("Classe de Armadura", creature.armorClassText)
                CreatureInfoField("CA de Toque / Surpresa", "${creature.touchAc} / ${creature.flatFootedAc}")
                CreatureInfoField("Ataque Base/Agarrar", "${creature.baseAttack.creatureSigned()}/${creature.grapple.creatureSigned()}")
                CreatureInfoField("Ataque", creature.attack)
                CreatureInfoField("Ataque Total", creature.fullAttack)
                CreatureInfoField("Espaço/Alcance", creature.spaceReach)
                CreatureInfoField("Ataques Especiais", creature.specialAttacks)
                CreatureInfoField("Qualidades Especiais", creature.specialQualities)
                CreatureInfoField("Testes de Resistência",
                    "Fort ${creature.saves.fort.creatureSigned()} • Ref ${creature.saves.ref.creatureSigned()} • Von ${creature.saves.will.creatureSigned()}")
            } }
            item { CreatureInfoSection("Habilidades") {
                val a = creature.abilities
                CreatureInfoField("Força", a.str?.toString() ?: "—")
                CreatureInfoField("Destreza", a.dex?.toString() ?: "—")
                CreatureInfoField("Constituição", a.con?.toString() ?: "—")
                CreatureInfoField("Inteligência", a.int?.toString() ?: "—")
                CreatureInfoField("Sabedoria", a.wis?.toString() ?: "—")
                CreatureInfoField("Carisma", a.cha?.toString() ?: "—")
            } }
            item { CreatureInfoSection("Perícias, Talentos e Combate") {
                CreatureInfoField("Perícias", creature.skills)
                CreatureInfoField("Talentos", creature.feats)
                CreatureInfoField("Combate", creature.combat)
            } }
            item { CreatureInfoSection("Informações gerais") {
                CreatureInfoField("Ambiente", creature.environment)
                CreatureInfoField("Organização", creature.organization)
                CreatureInfoField("Tesouro", creature.treasure)
                CreatureInfoField("Tendência", creature.alignment)
                CreatureInfoField("Progressão", creature.advancement)
                CreatureInfoField("Ajuste de Nível", creature.levelAdjustment)
                CreatureInfoField("Fonte", creature.source)
            } }
        }
    }
}

@Composable
private fun CreatureInfoSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun CreatureInfoField(label: String, value: String) {
    if (value.isBlank()) return
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun Int.creatureSigned(): String = if (this > 0) "+$this" else toString()
