package com.example.dnd35.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.dnd35.model.AbilityScoreMethod
import com.example.dnd35.model.Dnd35Rules
import com.example.dnd35.model.PointBuyBudget
import com.example.dnd35.model.pointBuyCost
import com.example.dnd35.model.pointBuyTotalCost
import com.example.dnd35.model.rollAbilityScore
import com.example.ui.theme.DndCrimson
import com.example.ui.theme.DndGold

private val STEP_TITLES = listOf("Nome", "Raça", "Classe", "Atributos", "Revisão")

/**
 * Criação guiada de personagem: nome → raça → classe → atributos (rolagem/manual ou compra
 * por pontos, com os modificadores raciais já aplicados na prévia) → revisão. O app nunca
 * obriga a rolar dados no próprio aplicativo — cada atributo pode ser digitado diretamente,
 * como quando o jogador rola dados físicos à mesa.
 */
@Composable
fun NewCharacterDialog(
    onConfirm: (
        name: String,
        characterClass: String,
        race: String,
        baseStr: Int,
        baseDex: Int,
        baseCon: Int,
        baseInt: Int,
        baseWis: Int,
        baseCha: Int
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }

    var name by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf(Dnd35Rules.CLASSES.first().name) }
    var selectedRace by remember { mutableStateOf(Dnd35Rules.RACES.first().name) }

    var classExpanded by remember { mutableStateOf(false) }
    var raceExpanded by remember { mutableStateOf(false) }

    var method by remember { mutableStateOf(AbilityScoreMethod.ROLL_OR_MANUAL) }
    var pointBuyBudget by remember { mutableStateOf(PointBuyBudget.PADRAO) }
    var str by remember { mutableIntStateOf(10) }
    var dex by remember { mutableIntStateOf(10) }
    var con by remember { mutableIntStateOf(10) }
    var intl by remember { mutableIntStateOf(10) }
    var wis by remember { mutableIntStateOf(10) }
    var cha by remember { mutableIntStateOf(10) }

    fun resetScoresForMethod(newMethod: AbilityScoreMethod) {
        val base = if (newMethod == AbilityScoreMethod.POINT_BUY) 8 else 10
        str = base; dex = base; con = base; intl = base; wis = base; cha = base
    }

    val currentClassInfo = Dnd35Rules.CLASSES.find { it.name == selectedClass } ?: Dnd35Rules.CLASSES.first()
    val currentRaceInfo = Dnd35Rules.RACES.find { it.name == selectedRace } ?: Dnd35Rules.RACES.first()

    val currentScores = listOf(str, dex, con, intl, wis, cha)
    val pointBuySpent = pointBuyTotalCost(currentScores)
    val pointBuyRemaining = pointBuySpent?.let { pointBuyBudget.totalPoints - it }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Criar Novo Personagem — ${STEP_TITLES[step]}", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (step + 1) / STEP_TITLES.size.toFloat() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp, max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (step) {
                    0 -> NameStep(name = name, onNameChange = { name = it })
                    1 -> RaceStep(
                        selectedRace = selectedRace,
                        currentRaceInfo = currentRaceInfo,
                        raceExpanded = raceExpanded,
                        onExpandedChange = { raceExpanded = it },
                        onRaceSelected = { selectedRace = it }
                    )
                    2 -> ClassStep(
                        selectedClass = selectedClass,
                        currentClassInfo = currentClassInfo,
                        classExpanded = classExpanded,
                        onExpandedChange = { classExpanded = it },
                        onClassSelected = { selectedClass = it }
                    )
                    3 -> AbilityScoreStep(
                        method = method,
                        onMethodChange = {
                            method = it
                            resetScoresForMethod(it)
                        },
                        pointBuyBudget = pointBuyBudget,
                        onPointBuyBudgetChange = {
                            pointBuyBudget = it
                            resetScoresForMethod(AbilityScoreMethod.POINT_BUY)
                        },
                        raceInfo = currentRaceInfo,
                        pointBuySpent = pointBuySpent ?: 0,
                        pointBuyRemaining = pointBuyRemaining ?: 0,
                        str = str, dex = dex, con = con, intl = intl, wis = wis, cha = cha,
                        onStrChange = { str = it },
                        onDexChange = { dex = it },
                        onConChange = { con = it },
                        onIntChange = { intl = it },
                        onWisChange = { wis = it },
                        onChaChange = { cha = it }
                    )
                    4 -> ReviewStep(
                        name = name.ifBlank { "Aventureiro" },
                        raceInfo = currentRaceInfo,
                        classInfo = currentClassInfo,
                        str = str, dex = dex, con = con, intl = intl, wis = wis, cha = cha
                    )
                }
            }
        },
        confirmButton = {
            if (step < STEP_TITLES.lastIndex) {
                val canAdvance = step != 3 || method != AbilityScoreMethod.POINT_BUY || (pointBuyRemaining ?: -1) >= 0
                Button(onClick = { step++ }, enabled = canAdvance) { Text("Avançar") }
            } else {
                Button(
                    onClick = {
                        onConfirm(
                            if (name.isBlank()) "Aventureiro" else name.trim(),
                            selectedClass,
                            selectedRace,
                            str, dex, con, intl, wis, cha
                        )
                        onDismiss()
                    }
                ) { Text("Criar Ficha") }
            }
        },
        dismissButton = {
            if (step > 0) {
                OutlinedButton(onClick = { step-- }) { Text("Voltar") }
            } else {
                OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
            }
        }
    )
}

@Composable
private fun NameStep(name: String, onNameChange: (String) -> Unit) {
    Text(
        "Como se chama seu aventureiro?",
        style = MaterialTheme.typography.bodyMedium
    )
    Spacer(modifier = Modifier.height(10.dp))
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Nome do Aventureiro") },
        placeholder = { Text("ex: Tordek, Lidda, Mialee") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
private fun RaceStep(
    selectedRace: String,
    currentRaceInfo: Dnd35Rules.RaceInfo,
    raceExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onRaceSelected: (String) -> Unit
) {
    Text("Raça do Livro do Jogador:", style = MaterialTheme.typography.labelMedium)
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedRace,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Selecionar raça",
                    modifier = Modifier.clickable { onExpandedChange(true) }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(true) }
        )
        DropdownMenu(
            expanded = raceExpanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.heightIn(max = 320.dp)
        ) {
            for (r in Dnd35Rules.RACES) {
                DropdownMenuItem(
                    text = { Text(r.name) },
                    onClick = {
                        onRaceSelected(r.name)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(6.dp))
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(currentRaceInfo.traits, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Modificadores: ${formatRaceAbilityMods(currentRaceInfo)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ClassStep(
    selectedClass: String,
    currentClassInfo: Dnd35Rules.ClassInfo,
    classExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onClassSelected: (String) -> Unit
) {
    Text("Classe do Livro do Jogador:", style = MaterialTheme.typography.labelMedium)
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedClass,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Selecionar classe",
                    modifier = Modifier.clickable { onExpandedChange(true) }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(true) }
        )
        DropdownMenu(
            expanded = classExpanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.heightIn(max = 320.dp)
        ) {
            for (c in Dnd35Rules.CLASSES) {
                DropdownMenuItem(
                    text = { Text("${c.name} (DV: d${c.hitDie})") },
                    onClick = {
                        onClassSelected(c.name)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(6.dp))
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "${currentClassInfo.description}\nDado de Vida: d${currentClassInfo.hitDie}.\nBBA (Bônus Base de Ataque): ${Dnd35Rules.babProgressionLabel(currentClassInfo.babType)}.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
private fun AbilityScoreStep(
    method: AbilityScoreMethod,
    onMethodChange: (AbilityScoreMethod) -> Unit,
    pointBuyBudget: PointBuyBudget,
    onPointBuyBudgetChange: (PointBuyBudget) -> Unit,
    raceInfo: Dnd35Rules.RaceInfo,
    pointBuySpent: Int,
    pointBuyRemaining: Int,
    str: Int, dex: Int, con: Int, intl: Int, wis: Int, cha: Int,
    onStrChange: (Int) -> Unit,
    onDexChange: (Int) -> Unit,
    onConChange: (Int) -> Unit,
    onIntChange: (Int) -> Unit,
    onWisChange: (Int) -> Unit,
    onChaChange: (Int) -> Unit
) {
    var methodExpanded by remember { mutableStateOf(false) }
    var budgetExpanded by remember { mutableStateOf(false) }

    Text(
        "Escolha como gerar os atributos. Role os dados no app ou digite o resultado dos seus próprios dados — o app nunca exige rolar aqui dentro.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { methodExpanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(
                when (method) {
                    AbilityScoreMethod.ROLL_OR_MANUAL -> "Rolagem ou entrada manual"
                    AbilityScoreMethod.POINT_BUY -> "Compra por pontos"
                }
            )
        }
        DropdownMenu(expanded = methodExpanded, onDismissRequest = { methodExpanded = false }) {
            DropdownMenuItem(
                text = { Text("Rolagem ou entrada manual") },
                onClick = { onMethodChange(AbilityScoreMethod.ROLL_OR_MANUAL); methodExpanded = false }
            )
            DropdownMenuItem(
                text = { Text("Compra por pontos") },
                onClick = { onMethodChange(AbilityScoreMethod.POINT_BUY); methodExpanded = false }
            )
        }
    }

    if (method == AbilityScoreMethod.POINT_BUY) {
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { budgetExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(pointBuyBudget.label)
            }
            DropdownMenu(expanded = budgetExpanded, onDismissRequest = { budgetExpanded = false }) {
                for (b in PointBuyBudget.values()) {
                    DropdownMenuItem(
                        text = { Text(b.label) },
                        onClick = { onPointBuyBudgetChange(b); budgetExpanded = false }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Pontos gastos: $pointBuySpent / ${pointBuyBudget.totalPoints} — saldo: $pointBuyRemaining",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (pointBuyRemaining < 0) DndCrimson else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Spacer(modifier = Modifier.height(10.dp))

    val rows = listOf(
        Triple("FOR", str, raceInfo.strMod) to onStrChange,
        Triple("DES", dex, raceInfo.dexMod) to onDexChange,
        Triple("CON", con, raceInfo.conMod) to onConChange,
        Triple("INT", intl, raceInfo.intMod) to onIntChange,
        Triple("SAB", wis, raceInfo.wisMod) to onWisChange,
        Triple("CAR", cha, raceInfo.chaMod) to onChaChange
    )
    rows.forEach { (data, onChange) ->
        val (label, base, racialMod) = data
        AbilityScoreRow(
            label = label,
            base = base,
            racialMod = racialMod,
            method = method,
            canIncrease = method != AbilityScoreMethod.POINT_BUY ||
                (pointBuyCost(base + 1) != null && pointBuyCost(base + 1)!! - pointBuyCost(base)!! <= pointBuyRemaining),
            canDecrease = base > (if (method == AbilityScoreMethod.POINT_BUY) 8 else 1),
            onIncrease = { onChange(base + 1) },
            onDecrease = { onChange(base - 1) },
            onManualEntry = { onChange(it) }
        )
        Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
private fun AbilityScoreRow(
    label: String,
    base: Int,
    racialMod: Int,
    method: AbilityScoreMethod,
    canIncrease: Boolean,
    canDecrease: Boolean,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onManualEntry: (Int) -> Unit
) {
    val total = base + racialMod
    Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp))
            when (method) {
                AbilityScoreMethod.POINT_BUY -> {
                    IconButton(onClick = onDecrease, enabled = canDecrease) { Text("−") }
                    Text("$base", modifier = Modifier.width(32.dp), style = MaterialTheme.typography.bodyMedium)
                    IconButton(onClick = onIncrease, enabled = canIncrease) { Text("+") }
                }
                AbilityScoreMethod.ROLL_OR_MANUAL -> {
                    var text by remember(base) { mutableStateOf(base.toString()) }
                    OutlinedTextField(
                        value = text,
                        onValueChange = { input ->
                            text = input.filter(Char::isDigit).take(2)
                            text.toIntOrNull()?.let { if (it in 1..30) onManualEntry(it) }
                        },
                        modifier = Modifier.width(72.dp),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = { onManualEntry(rollAbilityScore()) }) {
                        Icon(Icons.Default.Casino, contentDescription = "Rolar $label (4d6, descarta o menor)", tint = DndGold)
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Racial: ${if (racialMod >= 0) "+" else ""}$racialMod",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            Text(
                "Total: $total",
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ReviewStep(
    name: String,
    raceInfo: Dnd35Rules.RaceInfo,
    classInfo: Dnd35Rules.ClassInfo,
    str: Int, dex: Int, con: Int, intl: Int, wis: Int, cha: Int
) {
    Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Text("${raceInfo.name} — ${classInfo.name}", style = MaterialTheme.typography.bodyMedium)
    Spacer(modifier = Modifier.height(10.dp))

    val estimatedHp = classInfo.hitDie + Dnd35Rules.calculateModifier(con + raceInfo.conMod)
    Text(
        "PV iniciais estimados: ${maxOf(1, estimatedHp)} (dado de vida máximo no 1º nível + modificador de Constituição)",
        style = MaterialTheme.typography.bodySmall
    )
    Spacer(modifier = Modifier.height(10.dp))

    val rows = listOf(
        Triple("Força", str, raceInfo.strMod),
        Triple("Destreza", dex, raceInfo.dexMod),
        Triple("Constituição", con, raceInfo.conMod),
        Triple("Inteligência", intl, raceInfo.intMod),
        Triple("Sabedoria", wis, raceInfo.wisMod),
        Triple("Carisma", cha, raceInfo.chaMod)
    )
    rows.forEach { (label, base, racialMod) ->
        val total = base + racialMod
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                "$base ${if (racialMod != 0) "(${if (racialMod >= 0) "+" else ""}$racialMod racial) " else ""}= $total (mod. ${Dnd35Rules.calculateModifier(total).let { if (it >= 0) "+$it" else "$it" }})",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
    Text(
        "Ao confirmar, a ficha é criada com perícias de classe já marcadas; talentos, magias e equipamento são adicionados nas abas seguintes.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private fun formatRaceAbilityMods(raceInfo: Dnd35Rules.RaceInfo): String {
    val parts = listOfNotNull(
        "FOR".takeIf { raceInfo.strMod != 0 }?.let { "FOR ${signed(raceInfo.strMod)}" },
        "DES".takeIf { raceInfo.dexMod != 0 }?.let { "DES ${signed(raceInfo.dexMod)}" },
        "CON".takeIf { raceInfo.conMod != 0 }?.let { "CON ${signed(raceInfo.conMod)}" },
        "INT".takeIf { raceInfo.intMod != 0 }?.let { "INT ${signed(raceInfo.intMod)}" },
        "SAB".takeIf { raceInfo.wisMod != 0 }?.let { "SAB ${signed(raceInfo.wisMod)}" },
        "CAR".takeIf { raceInfo.chaMod != 0 }?.let { "CAR ${signed(raceInfo.chaMod)}" }
    )
    return if (parts.isEmpty()) "nenhum" else parts.joinToString(", ")
}

private fun signed(value: Int): String = if (value >= 0) "+$value" else "$value"
