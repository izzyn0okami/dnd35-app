package com.example

import com.example.dnd35.model.Dnd35Rules
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PresetEquipmentCatalogTest {
    @Test
    fun catalogContainsCoreWeaponArmorShieldAndAdventuringEquipment() {
        val names = Dnd35Rules.PRESET_ITEMS.map { it.name }.toSet()
        assertTrue(names.contains("Besta Pesada"))
        assertTrue(names.contains("Espada de Duas Lâminas"))
        assertTrue(names.contains("Armadura de Batalha"))
        assertTrue(names.contains("Escudo Pequeno de Metal"))
        assertTrue(names.contains("Virotes de Besta (10)"))
        assertTrue(names.contains("Pé de Cabra"))
        assertTrue(names.contains("Fogo Alquímico"))
    }

    @Test
    fun everyPresetHasAValidCategoryAndEquipmentSlot() {
        val categories = setOf("Armas", "Armaduras", "Escudos", "Itens Mágicos", "Itens de Aventura", "Consumíveis")
        val slots = Dnd35Rules.EquipmentSlots.ALL_SLOTS.toSet()
        assertTrue(Dnd35Rules.PRESET_ITEMS.isNotEmpty())
        Dnd35Rules.PRESET_ITEMS.forEach { item ->
            assertTrue("Categoria inválida para ${item.name}", item.category in categories)
            assertTrue("Espaço inválido para ${item.name}", item.defaultSlot in slots)
        }
    }

    @Test
    fun presetCatalogDoesNotRepeatExactNames() {
        val names = Dnd35Rules.PRESET_ITEMS.map { it.name }
        assertEquals(names.size, names.toSet().size)
    }

    @Test
    fun catalogContainsDungeonMasterGuidePotionsAndOils() {
        val byName = Dnd35Rules.PRESET_ITEMS.associateBy { it.name }
        assertEquals(750f, byName.getValue("Poção de Curar Ferimentos Graves").costGp, 0f)
        assertEquals(3000f, byName.getValue("Óleo de Arma Mágica Maior +5").costGp, 0f)
        assertEquals(1100f, byName.getValue("Poção de Resistência a Elementos 30 (tipo)").costGp, 0f)
        assertTrue(byName.containsKey("Poção de Levitação"))
        assertTrue(byName.containsKey("Óleo de Levitação"))
        assertTrue(byName.containsKey("Óleo de Roupa Encantada +1"))
    }

    @Test
    fun catalogContainsDungeonMasterGuideRings() {
        val byName = Dnd35Rules.PRESET_ITEMS.associateBy { it.name }
        assertEquals(50000f, byName.getValue("Anel de Proteção +5").costGp, 0f)
        assertEquals("+3 Deflexão", byName.getValue("Anel de Proteção +3").damageOrAc)
        assertEquals(200000f, byName.getValue("Anel de Comandar Elemental (fogo)").costGp, 0f)
        assertEquals(97950f, byName.getValue("Anel de Três Desejos").costGp, 0f)
        assertTrue(byName.containsKey("Anel Arcano (IV)"))
        assertTrue(byName.values.filter { it.name.startsWith("Anel ") }.all { it.defaultSlot.startsWith("Dedo") })
    }

    @Test
    fun catalogContainsDungeonMasterGuideRods() {
        val byName = Dnd35Rules.PRESET_ITEMS.associateBy { it.name }
        assertEquals(170000f, byName.getValue("Bastão Metamágico, Acelerar (maior)").costGp, 0f)
        assertEquals(12000f, byName.getValue("Bastão de Maravilhas").costGp, 0f)
        assertEquals(60000f, byName.getValue("Bastão de Obediência").costGp, 0f)
        assertTrue(byName.containsKey("Bastão Imóvel"))
        assertTrue(byName.containsKey("Bastão Mangual"))
    }
}
