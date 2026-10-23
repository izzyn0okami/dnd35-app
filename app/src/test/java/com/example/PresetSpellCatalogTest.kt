package com.example

import com.example.dnd35.model.Dnd35Rules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PresetSpellCatalogTest {
    @Test
    fun catalogIncludesAdditionalPlayerHandbookCantrips() {
        val spells = Dnd35Rules.PRESET_SPELLS
        val names = spells.map { it.name }
        listOf(
            "Criar Água",
            "Detectar Veneno",
            "Consertar",
            "Mensagem",
            "Abrir/Fechar",
            "Prestidigitação",
            "Purificar Alimentos e Bebidas",
            "Atordoar",
            "Espirro Ácido",
            "Marca Arcana",
            "Raio de Gelo",
            "Desestabilizar Mortos-Vivos",
            "Toque de Fadiga",
            "Lampejo",
            "Conhecer Direção",
            "Canção de Ninar",
            "Invocar Instrumento",
            "Curar Ferimentos Mínimos",
            "Infligir Ferimentos Mínimos",
            "Virtude"
        ).forEach { name ->
            assertTrue("Magia ausente: $name", name in names)
        }
        assertEquals("O catálogo não deve conter magias com nomes duplicados", names.size, names.toSet().size)
    }

    @Test
    fun newCantripsHaveExpectedClassAvailability() {
        val spells = Dnd35Rules.PRESET_SPELLS.associateBy { it.name }
        assertEquals(setOf("Mago", "Feiticeiro"), spells.getValue("Espirro Ácido").classes.toSet())
        assertEquals(setOf("Bardo"), spells.getValue("Canção de Ninar").classes.toSet())
        assertEquals(setOf("Clérigo", "Druida"), spells.getValue("Curar Ferimentos Mínimos").classes.toSet())
        assertEquals(0, spells.getValue("Virtude").classLevels.getValue("Druida"))
        assertEquals("Permanente", spells.getValue("Marca Arcana").duration)
    }

    @Test
    fun classSpellLevelsAreValidAndMatchListedClasses() {
        Dnd35Rules.PRESET_SPELLS.forEach { spell ->
            assertTrue("Nível geral inválido: ${spell.name}", spell.level in 0..9)
            assertEquals(
                "As classes e os níveis devem corresponder: ${spell.name}",
                spell.classes.toSet(),
                spell.classLevels.keys
            )
            spell.classLevels.forEach { (className, level) ->
                assertTrue("Nível inválido para $className: ${spell.name}", level in 0..9)
            }
        }
    }

    @Test
    fun detectPoisonUsesDifferentLevelsForPaladinAndRanger() {
        val spell = Dnd35Rules.PRESET_SPELLS.single { it.name == "Detectar Veneno" }
        assertEquals(0, spell.classLevels["Clérigo"])
        assertEquals(0, spell.classLevels["Druida"])
        assertEquals(0, spell.classLevels["Mago"])
        assertEquals(0, spell.classLevels["Feiticeiro"])
        assertEquals(1, spell.classLevels["Paladino"])
        assertEquals(1, spell.classLevels["Ranger"])
        assertEquals("Curto (7,5m)", spell.range)
        assertTrue(spell.description.contains("Sabedoria CD 20"))
        val purify = Dnd35Rules.PRESET_SPELLS.single { it.name == "Purificar Alimentos e Bebidas" }
        assertEquals("3m", purify.range)
    }
}
