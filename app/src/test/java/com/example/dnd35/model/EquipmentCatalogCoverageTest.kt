package com.example.dnd35.model

import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EquipmentCatalogCoverageTest {
    private val names by lazy { Dnd35Rules.PRESET_ITEMS.map { normalize(it.name) }.toSet() }

    @Test
    fun includesCorePlayerHandbookWeaponsAndAmmunition() {
        listOf(
            "Azagaia", "Adaga de Soco", "Foice Curta", "Maça-Estrela", "Lança Longa",
            "Besta Pesada", "Virotes de Besta (10)", "Balas de Funda (10)", "Machado de Batalha",
            "Espada Larga", "Foice Longa", "Lança Montada", "Rede", "Boleadeira",
            "Besta de Mão", "Besta Leve de Repetição", "Besta Pesada de Repetição", "Shuriken (5)"
        ).forEach { assertTrue("Arma/munição ausente: $it", normalize(it) in names) }
    }

    @Test
    fun includesPlayerHandbookArmorShieldAndAccessories() {
        listOf(
            "Camisão de Cota de Malha", "Loriga Segmentada", "Meia Armadura",
            "Armadura de Batalha", "Escudo Pequeno de Madeira", "Escudo Pequeno de Metal",
            "Escudo Grande de Madeira", "Escudo Grande de Metal", "Escudo de Corpo",
            "Cravos para Armadura", "Manopla de Segurança", "Cravos para Escudos"
        ).forEach { assertTrue("Armadura/escudo/acessório ausente: $it", normalize(it) in names) }
    }

    @Test
    fun includesAdventuringGearToolsAndSpecialSubstances() {
        listOf(
            "Agulha de Costura", "Algemas (Obra-Prima)", "Aríete", "Baú (Vazio)",
            "Escada (3 m)", "Estrepes", "Fechadura (Incrível)", "Luneta", "Sinete",
            "Tocha", "Bolsa de Componentes de Magia", "Clepsidra",
            "Instrumentos de Ladrão (Obra-Prima)", "Kit de Disfarces", "Laboratório Alquímico",
            "Ácido (frasco)", "Água Benta (frasco)", "Bolsa de Cola", "Fósforo",
            "Tocha da Chama Eterna"
        ).forEach { assertTrue("Equipamento/consumível ausente: $it", normalize(it) in names) }
    }

    @Test
    fun convertsCanonicalWeaponAndShieldWeightsFromTheHandbookToPounds() {
        val byName = Dnd35Rules.PRESET_ITEMS.associateBy { normalize(it.name) }
        assertEquals(1.1f, byName[normalize("Virotes de Besta (10)")]!!.weightLb, 0.01f)
        assertEquals(5.5f, byName[normalize("Balas de Funda (10)")]!!.weightLb, 0.01f)
        assertEquals(49.6f, byName[normalize("Escudo de Corpo")]!!.weightLb, 0.1f)
        assertEquals(55.0f, byName[normalize("Armadura de Batalha")]!!.weightLb, 0.01f)
        assertEquals(5.5f, byName[normalize("Broquel")]!!.weightLb, 0.01f)
        assertEquals(10.0f, byName[normalize("Escudo Pequeno de Metal")]!!.costGp, 0.01f)
        assertEquals(8.3f, byName[normalize("Escudo Pequeno de Metal")]!!.weightLb, 0.1f)
        assertEquals(22.0f, byName[normalize("Tenda")]!!.weightLb, 0.01f)
        assertEquals(11.0f, byName[normalize("Corda de Cânhamo (15 m)")]!!.weightLb, 0.01f)
    }

    @Test
    fun catalogNamesAreUniqueIgnoringAccentsAndCase() {
        val normalized = Dnd35Rules.PRESET_ITEMS.map { normalize(it.name) }
        assertEquals(normalized.size, normalized.toSet().size)
    }

    private fun normalize(value: String): String =
        java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase()
            .replace(Regex("\\s+"), " ")
            .trim()
}
