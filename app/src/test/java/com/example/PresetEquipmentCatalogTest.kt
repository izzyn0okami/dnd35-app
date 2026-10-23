package com.example

import com.example.dnd35.model.Dnd35Rules
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class PresetEquipmentCatalogTest {
    @Test
    fun catalogContainsCoreWeaponArmorShieldAndAdventuringEquipment() {
        val names = Dnd35Rules.PRESET_ITEMS.map { it.name }.toSet()
        assertTrue(names.contains("Besta Pesada"))
        assertTrue(names.contains("Espada de Duas Lâminas"))
        assertTrue(names.contains("Armadura Completa (Full Plate)"))
        assertTrue(names.contains("Escudo Leve de Aço"))
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
}
