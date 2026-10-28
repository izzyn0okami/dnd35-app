package com.example.dnd35.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmmunitionCompatibilityTest {
    private val ammoSlot = Dnd35Rules.EquipmentSlots.AMMUNITION

    @Test
    fun consumesOneUnitFromSelectedAmmunitionSlot() {
        val selected = CharacterInventoryItem(id = "ammo-1", name = "Flechas", category = "Consumíveis", quantity = 20, equippedSlot = ammoSlot)
        val other = CharacterInventoryItem(id = "ammo-2", name = "Virotes", category = "Consumíveis", quantity = 12)
        val updated = consumeAmmunitionStack(listOf(selected, other), "ammo-1")!!
        assertEquals(19, updated.first { it.id == "ammo-1" }.quantity)
        assertEquals(12, updated.first { it.id == "ammo-2" }.quantity)
    }

    @Test
    fun packageOfTenAmmunitionProvidesTenShots() {
        val pack = CharacterInventoryItem(
            id = "ammo-pack",
            name = "Virotes de Besta (10)",
            category = "Itens de Aventura",
            quantity = 1,
            equippedSlot = ammoSlot
        )

        assertEquals(10, remainingAmmunitionUnits(pack))
        val afterOne = consumeAmmunitionStack(listOf(pack), "ammo-pack")!!.single()
        assertEquals(1, afterOne.quantity)
        assertEquals(9, remainingAmmunitionUnits(afterOne))

        val afterSecond = consumeAmmunitionStack(listOf(afterOne), "ammo-pack")!!.single()
        assertEquals(8, remainingAmmunitionUnits(afterSecond))
    }

    @Test
    fun twoPackagesOfTenKeepTwentyShotsAndReducePackageCountAtBoundary() {
        val pack = CharacterInventoryItem(
            id = "ammo-pack",
            name = "Flechas (10)",
            category = "Itens de Aventura",
            quantity = 2,
            equippedSlot = ammoSlot
        )

        var current = pack
        repeat(10) {
            current = consumeAmmunitionStack(listOf(current), "ammo-pack")!!.single()
        }
        assertEquals(10, remainingAmmunitionUnits(current))
        assertEquals(1, current.quantity)
    }

    @Test
    fun removesLastUnitAndRejectsEmptyStack() {
        val selected = CharacterInventoryItem(id = "ammo-1", name = "Munição escolhida", category = "Consumíveis", quantity = 1, equippedSlot = ammoSlot)
        assertEquals(emptyList<CharacterInventoryItem>(), consumeAmmunitionStack(listOf(selected), "ammo-1"))
        assertNull(consumeAmmunitionStack(listOf(selected.copy(quantity = 0)), "ammo-1"))
    }

    @Test
    fun cannotConsumeItemNotEquippedInAmmunitionSlot() {
        val item = CharacterInventoryItem(id = "item-1", name = "Flechas", category = "Consumíveis", quantity = 10)
        assertNull(consumeAmmunitionStack(listOf(item), "item-1"))
    }
}
