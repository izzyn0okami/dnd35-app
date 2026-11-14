package com.example

import androidx.room.Room
import com.example.dnd35.data.AppDatabase
import com.example.dnd35.data.CharacterRepository
import com.example.dnd35.model.CharacterJsonAdapters
import com.example.dnd35.model.MetricUnits
import com.example.dnd35.model.StartingEquipmentKits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlinx.coroutines.runBlocking

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StartingEquipmentKitsTest {
    @Test
    fun createsEquipmentKitsForAllPlayerHandbookBaseClasses() {
        val classes = listOf(
            "Bárbaro", "Bardo", "Clérigo", "Druida", "Feiticeiro",
            "Guerreiro", "Ladino", "Mago", "Monge", "Paladino", "Patrulheiro", "Ranger"
        )
        classes.forEach { name ->
            assertNotNull("Conjunto inicial ausente para $name", StartingEquipmentKits.forClass(name, "Humano"))
        }
    }

    @Test
    fun barbarianKitIncludesTheBookWeaponsAndArmor() {
        val kit = StartingEquipmentKits.forClass("Bárbaro", "Meio-orc")!!
        val names = kit.items.map { it.name }

        assertTrue(names.any { it.contains("Machado Grande", ignoreCase = true) })
        assertTrue(names.any { it.contains("Arco Curto", ignoreCase = true) })
        assertTrue(names.any { it.contains("Adaga", ignoreCase = true) })
        assertTrue(names.any { it.contains("Couro Batido", ignoreCase = true) })
        assertTrue(kit.rollStartingGold() in 2f..8f)
    }

    @Test
    fun dwarfFighterUsesDwarfKitAndHumanFighterUsesHumanKit() {
        val dwarf = StartingEquipmentKits.forClass("Guerreiro", "Anão")!!
        val human = StartingEquipmentKits.forClass("Guerreiro", "Humano")!!

        assertTrue(dwarf.items.any { it.name.contains("Machado de Guerra Anão", ignoreCase = true) })
        assertTrue(human.items.any { it.name.contains("Espada Larga", ignoreCase = true) })
        assertFalse(human.items.any { it.name.contains("Machado de Guerra Anão", ignoreCase = true) })
    }

    @Test
    fun smallCharactersReceiveSizeAdjustedWeaponAndArmorWeights() {
        val gnome = StartingEquipmentKits.forClass("Bardo", "Gnomo")!!
        val human = StartingEquipmentKits.forClass("Bardo", "Humano")!!
        val gnomeArmor = gnome.items.first { it.name.contains("Couro Batido", ignoreCase = true) }
        val humanArmor = human.items.first { it.name.contains("Couro Batido", ignoreCase = true) }

        assertEquals(humanArmor.weightLb * 0.5f, gnomeArmor.weightLb, 0.001f)
        assertEquals("Armaduras", gnomeArmor.category)
        assertEquals("+3 CA", gnomeArmor.damageOrAc)
        assertEquals("Corpo (Armadura / Vestimenta)", gnomeArmor.equippedSlot)
    }

    @Test
    fun creatingHumanFighterPersistsStartingEquipmentInCharacterInventory() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        try {
            val repository = CharacterRepository(database.characterDao())
            val characterId = repository.createNewCharacter(
                name = "Guerreiro de Teste",
                characterClass = "Guerreiro",
                race = "Humano"
            )
            val savedCharacter = database.characterDao()
                .getAllCharactersOnce()
                .single { it.id == characterId }
            val inventory = CharacterJsonAdapters.deserializeInventory(savedCharacter.inventoryJson)

            assertTrue(inventory.any { it.name.equals("Brunea", ignoreCase = true) })
            assertTrue(inventory.any { it.name.equals("Espada Larga", ignoreCase = true) })
        } finally {
            database.close()
        }
    }

    @Test
    fun rangerReceivesTheEquipmentListedInThePlayerHandbookClassSection() {
        val ranger = StartingEquipmentKits.forClass("Patrulheiro", "Elfo")!!
        val names = ranger.items.map { it.name }

        assertTrue(names.any { it == "Mochila" })
        assertTrue(names.any { it == "Cantil" })
        assertTrue(names.any { it == "Rações de Viagem (por dia)" })
        assertTrue(names.any { it == "Saco de Dormir" })
        assertTrue(names.any { it == "Pederneira e Isqueiro" })
        val rangerArmor = ranger.items.first { it.name == "Couro Batido" }
        assertEquals("Armaduras", rangerArmor.category)
        assertEquals("+3 CA", rangerArmor.damageOrAc)
        assertEquals("Corpo (Armadura / Vestimenta)", rangerArmor.equippedSlot)
        assertTrue(names.any { it == "Espada Longa" })
        assertTrue(names.any { it == "Espada Curta" })
        assertTrue(names.any { it == "Arco Longo" })
        assertEquals(MetricUnits.kilogramsToPounds(10f), rangerArmor.weightLb, 0.001f)
        assertEquals(MetricUnits.kilogramsToPounds(1.5f), ranger.items.first { it.name == "Arco Longo" }.weightLb, 0.001f)
        assertTrue(names.any { it == "Aljava" })
        assertTrue(names.any { it == "Flechas (20)" })
        assertEquals(3, ranger.items.first { it.name == "Tocha" }.quantity)
        assertFalse(names.contains("Saco"))
        assertTrue(ranger.rollStartingGold() in 2f..8f)
    }
}
