package com.example

import com.example.dnd35.model.Dnd35Rules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PresetFeatCatalogTest {
    @Test
    fun catalogIncludesAdditionalPlayerHandbookCombatAndMetamagicFeats() {
        val names = Dnd35Rules.PRESET_FEATS.map { it.name }
        assertTrue("O catálogo deve conter mais talentos que a lista inicial", names.size >= 53)
        listOf(
            "Acuidade com Arma",
            "Agarrar Aprimorado",
            "Derrubar Aprimorado",
            "Desarme Aprimorado",
            "Reflexos de Combate",
            "Combater com Duas Armas",
            "Combater com Duas Armas Aprimorado",
            "Combater com Duas Armas Maior",
            "Saque Rápido",
            "Magia em Combate",
            "Foco em Magia",
            "Foco em Magia Maior",
            "Sucesso Decisivo Aprimorado",
            "Tiro Longo",
            "Ataque Desarmado Aprimorado",
            "Lutar às Cegas",
            "Tiro Rápido",
            "Tiro Múltiplo",
            "Tiro Preciso Aprimorado",
            "Tiro em Movimento",
            "Ataque Giratório",
            "Fintar Aprimorado",
            "Combate Montado",
            "Investida Montada",
            "Atropelar"
        ).forEach { name ->
            assertTrue("Talento ausente: $name", name in names)
        }
    }

    @Test
    fun presetFeatNamesAreUnique() {
        val names = Dnd35Rules.PRESET_FEATS.map { it.name }
        assertEquals(names.size, names.toSet().size)
    }
}
