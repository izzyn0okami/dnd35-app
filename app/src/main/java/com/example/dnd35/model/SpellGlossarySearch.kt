package com.example.dnd35.model

import java.text.Normalizer
import java.util.Locale

data class SpellGlossaryResult(
    val spell: Dnd35Rules.PresetSpell,
    val reference: Dnd35Rules.SpellReference?
)

object SpellGlossarySearch {
    fun search(query: String): List<SpellGlossaryResult> {
        val normalized = normalize(query)
        if (normalized.isBlank()) return emptyList()
        val references = Dnd35Rules.SPELL_REFERENCES.associateBy { normalize(it.name) }
        return Dnd35Rules.PRESET_SPELLS.asSequence()
            .map { SpellGlossaryResult(it, references[normalize(it.name)]) }
            .filter { result ->
                val spell = result.spell
                val ref = result.reference
                listOf(
                    spell.name, spell.school, spell.description, spell.castTime, spell.components,
                    spell.range, spell.duration, spell.savingThrow,
                    spell.classes.joinToString(" "), spell.classLevels.keys.joinToString(" "),
                    spell.domainLevels.keys.joinToString(" "),
                    ref?.explanation.orEmpty(), ref?.tacticalNotes.orEmpty(), ref?.interactions.orEmpty()
                ).any { normalize(it).contains(normalized) }
            }
            .sortedWith(
                compareBy<SpellGlossaryResult> {
                    if (normalize(it.spell.name) == normalized) 0 else 1
                }.thenBy {
                    if (normalize(it.spell.name).startsWith(normalized)) 0 else 1
                }.thenBy { it.spell.name }
            )
            .toList()
    }

    private fun normalize(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase(Locale.ROOT)
            .trim()
}
