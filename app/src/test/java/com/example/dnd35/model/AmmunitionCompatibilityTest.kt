package com.example.dnd35.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmmunitionCompatibilityTest {
    @Test
    fun findsArrowsForEquippedBow() {
        val items = listOf(
            CharacterInventoryItem(name = "Flechas", category = "Consumíveis", quantity = 20),
            CharacterInventoryItem(name = "Virotes", category = "Consumíveis", quantity = 12)
        )
        assertEquals("Flechas", findCompatibleAmmunition(items, "Arco longo")?.name)
    }

    @Test
    fun findsBoltsForCrossbow() {
        val items = listOf(CharacterInventoryItem(name = "Virotes de besta", category = "Consumíveis", quantity = 8))
        assertEquals("Virotes de besta", findCompatibleAmmunition(items, "Besta leve")?.name)
    }

    @Test
    fun ignoresEmptyStacksAndUnsupportedWeapons() {
        val items = listOf(CharacterInventoryItem(name = "Flechas", category = "Consumíveis", quantity = 0))
        assertNull(findCompatibleAmmunition(items, "Arco curto"))
        assertNull(findCompatibleAmmunition(items, "Machado de guerra"))
    }
}
