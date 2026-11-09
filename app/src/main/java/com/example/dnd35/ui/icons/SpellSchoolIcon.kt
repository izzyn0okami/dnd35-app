package com.example.dnd35.ui.icons

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/**
 * Associação visual das escolas de magia com símbolos já licenciados e empacotados no app.
 * A escola é extraída antes de descritores entre parênteses ou componentes entre colchetes.
 */
internal fun spellSchoolIcon(school: String): String? {
    val normalized = school.trim().substringBefore(" (").substringBefore(" [")
    return when (normalized.lowercase()) {
        "abjuração" -> "magic-shield"
        "adivinhação" -> "crystal-ball"
        "conjuração" -> "magic-lamp"
        "encantamento" -> "domino-mask"
        "evocação" -> "glowing-artifact"
        "ilusão" -> "mirror-mirror"
        "necromancia" -> "skull-crossed-bones"
        "transmutação" -> "fairy-wand"
        "universal" -> "spell-book"
        else -> null
    }
}

@Composable
fun SpellSchoolIcon(school: String, modifier: Modifier = Modifier) {
    val icon = GameIcons.find(spellSchoolIcon(school))
    if (icon != null) {
        Icon(
            painter = painterResource(id = icon.resId),
            contentDescription = "Escola de magia: $school",
            modifier = modifier,
            tint = Color.Unspecified
        )
    }
}
