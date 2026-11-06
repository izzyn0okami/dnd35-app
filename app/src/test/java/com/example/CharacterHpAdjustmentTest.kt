package com.example

import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.viewmodel.HpAdjustment
import com.example.dnd35.viewmodel.calculateHpAdjustment
import com.example.dnd35.viewmodel.withSessionCombatHp
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterHpAdjustmentTest {

    @Test
    fun damageConsumesTemporaryHpBeforeReducingRealHpAndAllowsNegativeValues() {
        assertEquals(
            HpAdjustment(currentHp = -3, tempHp = 0),
            calculateHpAdjustment(currentHp = 3, maxHp = 10, tempHp = 2, delta = -8)
        )
    }

    @Test
    fun healingCanRecoverCharacterFromNegativeHp() {
        assertEquals(
            HpAdjustment(currentHp = 3, tempHp = 0),
            calculateHpAdjustment(currentHp = -5, maxHp = 10, tempHp = 0, delta = 8)
        )
    }

    @Test
    fun damageCanContinueBelowZeroWhenCharacterIsAlreadyDying() {
        assertEquals(
            HpAdjustment(currentHp = -12, tempHp = 0),
            calculateHpAdjustment(currentHp = -5, maxHp = 10, tempHp = 0, delta = -7)
        )
    }

    @Test
    fun healingNeverExceedsMaximumAndDoesNotRestoreTemporaryHp() {
        assertEquals(
            HpAdjustment(currentHp = 10, tempHp = 4),
            calculateHpAdjustment(currentHp = 8, maxHp = 10, tempHp = 4, delta = 9)
        )
    }

    @Test
    fun combatSnapshotChangesOnlyCurrentAndTemporaryHp() {
        val original = CharacterEntity(
            id = 42L,
            name = "Personagem vinculado",
            currentHp = 15,
            maxHp = 20,
            tempHp = 6,
            goldGp = 123f,
            inventoryJson = "[{\\\"name\\\":\\\"Espada\\\"}]"
        )

        val updated = original.withSessionCombatHp(currentHp = -2, tempHp = 0)

        assertEquals(original.copy(currentHp = -2, tempHp = 0), updated)
        assertEquals(20, updated.maxHp)
        assertEquals(123f, updated.goldGp)
        assertEquals(original.inventoryJson, updated.inventoryJson)
    }
}
