package com.example.dnd35.ui.preview

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dnd35.model.CharacterInventoryItem
import com.example.dnd35.model.CharacterSkill
import com.example.dnd35.model.ComputedSkill
import com.example.dnd35.ui.components.QuickHpButton
import com.example.dnd35.ui.session.ChatMessageBubble
import com.example.dnd35.ui.tabs.AbilityCard
import com.example.dnd35.ui.tabs.AbilityData
import com.example.dnd35.ui.tabs.AttackRow
import com.example.dnd35.ui.tabs.CombatSummaryStat
import com.example.dnd35.ui.tabs.InventoryItemRowRefined
import com.example.dnd35.ui.tabs.SaveBox
import com.example.dnd35.ui.tabs.SkillItemRow
import com.example.ui.theme.DndHpGreen
import com.example.ui.theme.DndHpRed
import com.example.ui.theme.DndHpYellow
import com.example.ui.theme.MyApplicationTheme

/**
 * Previews dos componentes visuais que não dependem de ViewModel.
 *
 * Cada função anotada com [ComponentPreviews] aparece em quatro variantes no Android Studio:
 * tema claro, tema escuro, fonte ampliada (1,5x) e largura estreita (320dp) — as condições em que
 * a auditoria de UI encontrou mais problemas (contraste do dourado/vermelho, textos cortados,
 * alvos de toque apertados). Os dados são fictícios e servem só para visualização.
 */
@Preview(name = "Claro", group = "Variantes", showBackground = true)
@Preview(
    name = "Escuro",
    group = "Variantes",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Fonte 1,5x", group = "Variantes", showBackground = true, fontScale = 1.5f)
@Preview(name = "Estreito 320dp", group = "Variantes", showBackground = true, widthDp = 320)
annotation class ComponentPreviews

@Composable
private fun PreviewFrame(content: @Composable () -> Unit) {
    MyApplicationTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                content()
            }
        }
    }
}

@ComponentPreviews
@Composable
private fun AbilityCardsPreview() = PreviewFrame {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        AbilityCard(
            data = AbilityData("FOR", total = 16, mod = 3, base = 14, magic = 0, racial = 2),
            modifier = Modifier.weight(1f),
            onClick = {}
        )
        AbilityCard(
            data = AbilityData("DES", total = 8, mod = -1, base = 8, magic = 0, racial = 0),
            modifier = Modifier.weight(1f),
            onClick = {}
        )
        AbilityCard(
            data = AbilityData("CON", total = 14, mod = 2, base = 14, magic = 0, racial = 0),
            modifier = Modifier.weight(1f),
            onClick = {}
        )
    }
}

@ComponentPreviews
@Composable
private fun SaveBoxesPreview() = PreviewFrame {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        SaveBox(name = "Fortitude", total = 7, base = 5, mod = 2, onClick = {})
        SaveBox(name = "Reflexos", total = 1, base = 2, mod = -1, onClick = {})
        SaveBox(name = "Vontade", total = 4, base = 1, mod = 3, onClick = {})
    }
}

@ComponentPreviews
@Composable
private fun CombatSummaryPreview() = PreviewFrame {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        CombatSummaryStat(
            label = "INICIATIVA",
            value = "+2",
            modifier = Modifier.weight(1f),
            onClick = {}
        )
        CombatSummaryStat(
            label = "CA",
            value = "18",
            modifier = Modifier.weight(1f),
            subtitle = "Toque 11 • Surpr. 17",
            highlight = true,
            onClick = {}
        )
        CombatSummaryStat(
            label = "BBA",
            value = "+11",
            modifier = Modifier.weight(1f),
            subtitle = "+11/+6/+1",
            onClick = {}
        )
    }
}

@ComponentPreviews
@Composable
private fun AttackRowPreview() = PreviewFrame {
    AttackRow(
        weaponName = "Espada Longa +1",
        attackBonus = 12,
        attackProgression = "+12/+7/+2",
        damageNotation = "1d8+4",
        notes = "Crítico 19-20/x2",
        onRoll = {}
    )
    AttackRow(
        weaponName = "Adaga",
        attackBonus = 3,
        damageNotation = "1d4+1",
        notes = "",
        onRoll = {}
    )
}

@ComponentPreviews
@Composable
private fun QuickHpButtonsPreview() = PreviewFrame {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        QuickHpButton(label = "-5", color = DndHpRed, onClick = {})
        QuickHpButton(label = "-1", color = DndHpYellow, onClick = {})
        QuickHpButton(label = "+1", color = DndHpGreen, onClick = {})
        QuickHpButton(label = "+5", color = DndHpGreen, onClick = {})
    }
}

@ComponentPreviews
@Composable
private fun SkillRowsPreview() = PreviewFrame {
    SkillItemRow(
        computed = sampleSkill(name = "Furtividade", ranks = 4f, classSkill = true, total = 7),
        onAddRank = {},
        onSubRank = {},
        onEdit = {},
        onRoll = {}
    )
    SkillItemRow(
        computed = sampleSkill(name = "Arte da Fuga", ranks = 0f, classSkill = false, total = -1),
        onAddRank = {},
        onSubRank = {},
        onEdit = {},
        onRoll = {}
    )
}

@ComponentPreviews
@Composable
private fun InventoryRowsPreview() = PreviewFrame {
    InventoryItemRowRefined(
        item = CharacterInventoryItem(
            name = "Espada Longa",
            category = "Armas",
            weightLb = 4f,
            costGp = 15f,
            damage = "1d8",
            equipped = true
        ),
        onOpenSlotPicker = {},
        onEdit = {},
        onDelete = {},
        onExport = {}
    )
    InventoryItemRowRefined(
        item = CharacterInventoryItem(
            name = "Poção de Curar Ferimentos Leves",
            category = "Itens Mágicos",
            quantity = 3,
            weightLb = 0.1f,
            costGp = 50f,
            notes = "Cura 1d8+1 PV"
        ),
        onOpenSlotPicker = {},
        onEdit = {},
        onDelete = {},
        onExport = {}
    )
}

@ComponentPreviews
@Composable
private fun ChatBubblesPreview() = PreviewFrame {
    ChatMessageBubble(sender = "Mestre", text = "Você ouve passos no corredor.", fromMe = false)
    ChatMessageBubble(sender = "Você", text = "Vou me esconder atrás da porta.", fromMe = true)
}

private fun sampleSkill(
    name: String,
    ranks: Float,
    classSkill: Boolean,
    total: Int
) = ComputedSkill(
    skill = CharacterSkill(name = name, keyAbility = "DES", ranks = ranks, isClassSkill = classSkill),
    keyAbilityMod = 3,
    ranks = ranks,
    racialBonus = 0,
    sizeBonus = 0,
    miscBonus = 0,
    armorPenaltyApplied = 0,
    totalBonus = total,
    isClassSkill = classSkill,
    maxRanks = 4f,
    trainedOnly = false,
    canRoll = true
)
