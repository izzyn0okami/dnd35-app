package com.example.dnd35.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DndCrimson
import com.example.ui.theme.DndGold
import com.example.ui.theme.DndHpGreen
import kotlin.random.Random

@Composable
fun DiceRollDialog(
    actionTitle: String,
    bonus: Int,
    damageNotation: String,
    onDismiss: () -> Unit
) {
    var d20Result by remember { mutableIntStateOf(Random.nextInt(1, 21)) }
    val totalAttack = d20Result + bonus

    val isNat20 = d20Result == 20
    val isNat1 = d20Result == 1

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Casino, contentDescription = null, tint = DndGold, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(actionTitle, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Grande Círculo do Dado d20
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isNat20 -> DndHpGreen.copy(alpha = 0.2f)
                                isNat1 -> DndCrimson.copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.primaryContainer
                            }
                        )
                        .border(
                            3.dp,
                            when {
                                isNat20 -> DndHpGreen
                                isNat1 -> DndCrimson
                                else -> DndGold
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$d20Result",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                isNat20 -> DndHpGreen
                                isNat1 -> DndCrimson
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                        Text("d20", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isNat20) {
                    Text(
                        "★ 20 NATURAL! ACERTO CRÍTICO! ★",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DndHpGreen
                    )
                } else if (isNat1) {
                    Text(
                        "⚠ 1 NATURAL! FALHA CRÍTICA! ⚠",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DndCrimson
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Cartão com a fórmula detalhada
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        val sign = if (bonus >= 0) "+$bonus" else "$bonus"
                        Text(
                            "Jogada de Ataque: $d20Result $sign = $totalAttack",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (damageNotation.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Dano da Arma: $damageNotation",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                d20Result = Random.nextInt(1, 21)
            }) {
                Text("Rolar Novamente")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}
