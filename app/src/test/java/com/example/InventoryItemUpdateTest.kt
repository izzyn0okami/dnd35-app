package com.example

import com.example.dnd35.model.CharacterInventoryItem
import com.example.dnd35.model.Dnd35Rules
import com.example.dnd35.viewmodel.replaceInventoryItemInList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class InventoryItemUpdateTest {
    @Test
    fun editingCategoryAndEquipmentSlotPersistsAndReleasesPreviousOccupant() {
        val oldItem = CharacterInventoryItem(
            id = "old",
            name = "Espada antiga",
            category = "Armas",
            equipped = true,
            equippedSlot = Dnd35Rules.EquipmentSlots.MAIN_HAND
        )
        val editedItem = oldItem.copy(
            name = "Espada flamejante",
            category = "Itens Mágicos",
            equipped = true,
            equippedSlot = Dnd35Rules.EquipmentSlots.OFF_HAND
        )
        val occupant = CharacterInventoryItem(
            id = "shield",
            name = "Escudo",
            category = "Escudos",
            equipped = true,
            equippedSlot = Dnd35Rules.EquipmentSlots.OFF_HAND
        )

        val result = replaceInventoryItemInList(listOf(oldItem, occupant), editedItem)

        assertEquals("Itens Mágicos", result.first { it.id == "old" }.category)
        assertEquals(Dnd35Rules.EquipmentSlots.OFF_HAND, result.first { it.id == "old" }.equippedSlot)
        assertTrue(result.first { it.id == "old" }.equipped)
        assertFalse(result.first { it.id == "shield" }.equipped)
        assertEquals(Dnd35Rules.EquipmentSlots.NONE, result.first { it.id == "shield" }.equippedSlot)
    }

    @Test
    fun movingItemToBackpackDoesNotChangeOtherEquipment() {
        val edited = CharacterInventoryItem(
            id = "item",
            name = "Anel",
            category = "Itens Mágicos",
            equipped = false,
            equippedSlot = Dnd35Rules.EquipmentSlots.NONE
        )
        val other = CharacterInventoryItem(
            id = "other",
            name = "Outro anel",
            category = "Itens Mágicos",
            equipped = true,
            equippedSlot = Dnd35Rules.EquipmentSlots.RING_1
        )

        val result = replaceInventoryItemInList(listOf(edited, other), edited.copy(name = "Anel de proteção"))

        assertEquals("Anel de proteção", result.first { it.id == "item" }.name)
        assertTrue(result.first { it.id == "other" }.equipped)
        assertEquals(Dnd35Rules.EquipmentSlots.RING_1, result.first { it.id == "other" }.equippedSlot)
    }

    @Test
    fun unknownItemIdLeavesInventoryUnchanged() {
        val existing = CharacterInventoryItem(id = "existing", name = "Poção", category = "Consumíveis")
        val inventory = listOf(existing)
        val result = replaceInventoryItemInList(
            inventory,
            CharacterInventoryItem(id = "missing", name = "Poção nova", category = "Consumíveis")
        )

        assertSame(inventory, result)
    }
}
