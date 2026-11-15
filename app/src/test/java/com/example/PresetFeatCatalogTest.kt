package com.example

import com.example.dnd35.model.Dnd35Rules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PresetFeatCatalogTest {
    @Test
    fun catalogIncludesAdditionalPlayerHandbookCombatAndMetamagicFeats() {
        val names = Dnd35Rules.PRESET_FEATS.map { it.name }
        assertTrue("O catálogo deve conter os talentos do Livro do Jogador sem duplicatas", names.size >= 109)
        listOf(
            "Acuidade com Arma",
            "Agarrar Aprimorado",
            "Imobilização Aprimorada",
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
            "Pisotear"
        ).forEach { name ->
            assertTrue("Talento ausente: $name", name in names)
        }
    }

    @Test
    fun presetFeatNamesAreUnique() {
        val names = Dnd35Rules.PRESET_FEATS.map { it.name }
        assertEquals(names.size, names.toSet().size)
        assertTrue("Derrubar Aprimorado não é um talento separado na edição brasileira", "Derrubar Aprimorado" !in names)
    }
}
