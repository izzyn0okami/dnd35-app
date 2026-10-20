package com.example.dnd35.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DndCrimson
import com.example.ui.theme.DndHpGreen
import com.example.ui.theme.DndHpRed
import com.example.ui.theme.DndHpYellow
import com.example.ui.theme.DndTempPurple

@Composable
fun HpBarSection(
    currentHp: Int,
    maxHp: Int,
    tempHp: Int,
    nonLethal: Int,
    onAdjustHp: (Int) -> Unit,
    onSetMaxHp: (Int) -> Unit,
    onSetTempHp: (Int) -> Unit,
    onSetNonLethal: (Int) -> Unit,
    onLongRest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditMaxHpDialog by remember { mutableStateOf(false) }
    var showCustomHpDialog by remember { mutableStateOf<String?>(null) } // "DANO" ou "CURA" ou "TEMP"
    var showRestConfirmDialog by remember { mutableStateOf(false) }

    val safeMax = maxOf(1, maxHp)
    val fraction = (currentHp.toFloat() / safeMax.toFloat()).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(targetValue = fraction, label = "hp_fill")

    val barColor = when {
        fraction > 0.5f -> DndHpGreen
        fraction > 0.2f -> DndHpYellow
        else -> DndHpRed
    }
    val animatedColor by animateColorAsState(targetValue = barColor, label = "hp_color")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hp_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Título e Botão de Descanso
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Pontos de Vida",
                        tint = animatedColor,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        "Pontos de Vida",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showEditMaxHpDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar PV Máx", tint = MaterialTheme.colorScheme.primary)
                    }
                    Button(
                        onClick = { showRestConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        contentPadding = ButtonDefaults.TextButtonContentPadding,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Bedtime, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Descanso", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Numerais de PV em destaque
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$currentHp",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = animatedColor
                    )
                    Text(
                        text = " / $maxHp PV",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (tempHp > 0) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DndTempPurple.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                "+$tempHp Temp",
                                color = DndTempPurple,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (nonLethal > 0) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DndHpYellow.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "$nonLethal Não-Letal",
                                color = DndHpYellow,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Barra visual de pontos de vida interativa
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.25f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            ) {
                // Preenchimento de HP
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedFraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(animatedColor)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botões rápidos de ajuste
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickHpButton(label = "-5", color = DndCrimson) { onAdjustHp(-5) }
                QuickHpButton(label = "-1", color = DndCrimson) { onAdjustHp(-1) }
                QuickHpButton(label = "+1", color = DndHpGreen) { onAdjustHp(1) }
                QuickHpButton(label = "+5", color = DndHpGreen) { onAdjustHp(5) }

                OutlinedButton(
                    onClick = { showCustomHpDialog = "DANO" },
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Dano", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = { showCustomHpDialog = "CURA" },
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Healing, contentDescription = null, modifier = Modifier.size(14.dp), tint = DndHpGreen)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Cura", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = { showCustomHpDialog = "TEMP" },
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(14.dp), tint = DndTempPurple)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Temp", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }

    // Modal de Edição de PV Máx
    if (showEditMaxHpDialog) {
        var inputVal by remember { mutableStateOf("$maxHp") }
        AlertDialog(
            onDismissRequest = { showEditMaxHpDialog = false },
            title = { Text("Definir PV Máximo") },
            text = {
                OutlinedTextField(
                    value = inputVal,
                    onValueChange = { inputVal = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("PV Máximo") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    inputVal.toIntOrNull()?.let { onSetMaxHp(it) }
                    showEditMaxHpDialog = false
                }) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditMaxHpDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal de Dano / Cura / PV Temporário Personalizado
    showCustomHpDialog?.let { actionType ->
        var inputVal by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCustomHpDialog = null },
            title = { Text(when (actionType) {
                "DANO" -> "Aplicar Dano"
                "CURA" -> "Aplicar Cura"
                else -> "Pontos de Vida Temporários"
            }) },
            text = {
                OutlinedTextField(
                    value = inputVal,
                    onValueChange = { inputVal = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Quantidade") },
                    singleLine = true,
                    placeholder = { Text("ex: 14") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = inputVal.toIntOrNull() ?: 0
                        when (actionType) {
                            "DANO" -> onAdjustHp(-amount)
                            "CURA" -> onAdjustHp(amount)
                            "TEMP" -> onSetTempHp(amount)
                        }
                        showCustomHpDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (actionType == "DANO") DndCrimson else DndHpGreen
                    )
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCustomHpDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Confirmação de Descanso Longo
    if (showRestConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestConfirmDialog = false },
            title = { Text("Descanso Longo") },
            text = {
                Text("O descanso completo restaura todos os seus Pontos de Vida ao máximo ($maxHp), remove PV temporários e dano não-letal, e restaura todos os seus espaços de magia.")
            },
            confirmButton = {
                Button(onClick = {
                    onLongRest()
                    showRestConfirmDialog = false
                }) {
                    Text("Descansar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRestConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun QuickHpButton(label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp),
        contentPadding = ButtonDefaults.TextButtonContentPadding,
        modifier = Modifier
            .height(36.dp)
            .width(42.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
