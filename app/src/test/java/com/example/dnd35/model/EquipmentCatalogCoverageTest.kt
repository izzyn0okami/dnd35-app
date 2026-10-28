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
            "Besta de Mão", "Besta de Repetição Leve", "Besta de Repetição Pesada", "Shuriken (5)"
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
