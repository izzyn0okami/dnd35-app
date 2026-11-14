package com.example

import com.example.dnd35.session.Dnd35LootGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Dnd35LootGeneratorTest {
    @Test
    fun returnsAverageTreasureValueForEncounterLevels() {
        assertEquals(300, Dnd35LootGenerator.averageTreasureForEncounterLevel(1) ?: -1)
        assertEquals(7_500, Dnd35LootGenerator.averageTreasureForEncounterLevel(11) ?: -1)
        assertEquals(80_000, Dnd35LootGenerator.averageTreasureForEncounterLevel(20) ?: -1)
    }

    @Test
    fun rejectsLevelsOutsideThePublishedTable() {
        assertNull(Dnd35LootGenerator.averageTreasureForEncounterLevel(0))
        assertNull(Dnd35LootGenerator.averageTreasureForEncounterLevel(21))
    }
    @Test
    fun randomTreasureUsesThreeIndependentD100RollsAndReturnsGeneratedEntries() {
        val result = Dnd35LootGenerator.generateRandomTreasure(10, kotlin.random.Random(1234))!!

        assertTrue(result.coinsRoll in 1..100)
        assertTrue(result.goodsRoll in 1..100)
        assertTrue(result.itemsRoll in 1..100)
        assertTrue(result.summary.contains("moedas:"))
        assertTrue(result.summary.contains("bens:"))
        assertTrue(result.summary.contains("itens:"))
    }

    @Test
    fun randomTreasureRejectsEncounterLevelsOutsideTheTable() {
        assertNull(Dnd35LootGenerator.generateRandomTreasure(0))
        assertNull(Dnd35LootGenerator.generateRandomTreasure(21))
    }


    @Test
    fun armorAndShieldSizeRollUsesSmallAndMediumRangesExactly() {
        assertEquals("Pequena", Dnd35LootGenerator.armorSizeForRoll(1))
        assertEquals("Pequena", Dnd35LootGenerator.armorSizeForRoll(10))
        assertEquals("Média", Dnd35LootGenerator.armorSizeForRoll(11))
        assertEquals("Média", Dnd35LootGenerator.armorSizeForRoll(100))
        assertNull(Dnd35LootGenerator.armorSizeForRoll(0))
        assertNull(Dnd35LootGenerator.armorSizeForRoll(101))
    }

    @Test
    fun generatedCatalogItemsReceiveRealWeightsAndSmallArmorIsHalfWeight() {
        val armor = Dnd35LootGenerator.GeneratedItem(
            name = "Camisão de cota de malha (Média)",
            category = "Armaduras",
            valueGp = 100f
        )
        val smallArmor = armor.copy(name = "Camisão de cota de malha (Pequena)")
        val acid = Dnd35LootGenerator.GeneratedItem(
            name = "Ácido",
            category = "Consumíveis",
            valueGp = 10f
        )
        assertEquals(27.5f, Dnd35LootGenerator.weightForInventory(armor) ?: -1f, 0.0001f)
        assertEquals(13.75f, Dnd35LootGenerator.weightForInventory(smallArmor) ?: -1f, 0.0001f)
        assertEquals(1.1f, Dnd35LootGenerator.weightForInventory(acid) ?: -1f, 0.0001f)
    }

    @Test
    fun unresolvedGenericLootDoesNotPretendToHaveAKnownWeight() {
        val genericWeapon = Dnd35LootGenerator.GeneratedItem(
            name = "Arma corpo a corpo comum obra-prima",
            category = "Armas",
            valueGp = 350f
        )
        assertNull(Dnd35LootGenerator.weightForInventory(genericWeapon))
    }

    @Test
    fun seededRandomGenerationIsReproducible() {
        val first = Dnd35LootGenerator.generateRandomTreasure(12, kotlin.random.Random(9876))
        val second = Dnd35LootGenerator.generateRandomTreasure(12, kotlin.random.Random(9876))
        assertEquals(first, second)
    }

    @Test
    fun averageTreasureValuesRemainAvailableAlongsideRandomMode() {
        assertEquals(300, Dnd35LootGenerator.averageTreasureForEncounterLevel(1) ?: -1)
        assertEquals(80_000, Dnd35LootGenerator.averageTreasureForEncounterLevel(20) ?: -1)
    }

}
