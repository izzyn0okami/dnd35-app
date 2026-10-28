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
    fun includesPlayerHandbookFoodLodgingMountsAndTransport() {
        listOf(
            "Acomodação Pobre (por dia)", "Acomodação Padrão (por dia)", "Acomodação Boa (por dia)",
            "Banquete (por pessoa)", "Carne (pedaço)", "Cerveja (caneca)", "Cerveja (jarra)",
            "Pão", "Queijo (pedaço)", "Refeição Pobre (por dia)", "Refeição Padrão (por dia)",
            "Refeição Boa (por dia)", "Vinho Normal (jarro)", "Vinho Bom (garrafa)",
            "Alforje", "Alimentação (por dia)", "Cachorro de Montaria", "Cão de Guarda",
            "Cavalo de Guerra Leve", "Cavalo de Guerra Pesado", "Cavalo Leve", "Cavalo Pesado",
            "Pônei de Guerra", "Pônei", "Estábulo (por dia)", "Freio e Rédeas",
            "Jumento ou Mula", "Sela de Carga", "Sela Militar", "Sela de Montaria",
            "Sela Exótica de Carga", "Sela Exótica Militar", "Sela Exótica de Montaria",
            "Barco a Remo", "Remo", "Barcaça", "Carroça", "Carruagem", "Charrete",
            "Galeão", "Navio de Guerra", "Navio", "Trenó", "Veleiro"
        ).forEach { assertTrue("Equipamento/serviço ausente: $it", normalize(it) in names) }
    }

    @Test
    fun convertsFoodMountAndTransportCostsAndWeightsFromTheHandbook() {
        val byName = Dnd35Rules.PRESET_ITEMS.associateBy { normalize(it.name) }
        assertEquals(0.04f, byName[normalize("Cerveja (caneca)")]!!.costGp, 0.001f)
        assertEquals(0.2f, byName[normalize("Acomodação Pobre (por dia)")]!!.costGp, 0.001f)
        assertEquals(8.8f, byName[normalize("Alforje")]!!.weightLb, 0.01f)
        assertEquals(16.5f, byName[normalize("Sela de Carga")]!!.weightLb, 0.01f)
        assertEquals(110.2f, byName[normalize("Barco a Remo")]!!.weightLb, 0.1f)
        assertEquals(440.9f, byName[normalize("Carroça")]!!.weightLb, 0.1f)
        assertEquals(0.0f, byName[normalize("Barcaça")]!!.weightLb, 0.0f)
        assertEquals(3000.0f, byName[normalize("Barcaça")]!!.costGp, 0.01f)
    }


    @Test
    fun includesPlayerHandbookClothingAndFixedPriceServices() {
        listOf(
            "Traje de Artesão", "Traje de Clima Frio", "Traje de Corte",
            "Traje de Entretenimento", "Traje de Explorador", "Traje de Monge",
            "Traje de Nobre", "Traje de Plebeu", "Traje de Sábio", "Traje de Viajante",
            "Traje Real", "Vestimentas de Clérigo", "Condução de Carroça (1,5 km)",
            "Mensageiro (1,5 km)", "Passagem de Navio (1,5 km)",
            "Trabalhador Treinado (por dia)", "Trabalhador sem Treinamento (por dia)",
            "Pedágio (estrada ou portão)"
        ).forEach { assertTrue("Indumentária/serviço ausente: $it", normalize(it) in names) }
    }

    @Test
    fun convertsPlayerHandbookClothingWeightsAndServicePrices() {
        val byName = Dnd35Rules.PRESET_ITEMS.associateBy { normalize(it.name) }
        assertEquals(4.4f, byName[normalize("Traje de Artesão")]!!.weightLb, 0.01f)
        assertEquals(7.7f, byName[normalize("Traje de Clima Frio")]!!.weightLb, 0.01f)
        assertEquals(16.5f, byName[normalize("Traje Real")]!!.weightLb, 0.01f)
        assertEquals(0.1f, byName[normalize("Traje de Plebeu")]!!.costGp, 0.001f)
        assertEquals(0.03f, byName[normalize("Condução de Carroça (1,5 km)")]!!.costGp, 0.001f)
        assertEquals(0.02f, byName[normalize("Mensageiro (1,5 km)")]!!.costGp, 0.001f)
        assertEquals(0.3f, byName[normalize("Trabalhador Treinado (por dia)")]!!.costGp, 0.001f)
        assertEquals(0.01f, byName[normalize("Pedágio (estrada ou portão)")]!!.costGp, 0.001f)
    }


    @Test
    fun matchesPlayerHandbookLampChalkAndInkEntries() {
        val byName = Dnd35Rules.PRESET_ITEMS.associateBy { normalize(it.name) }
        listOf("Lâmpada", "Giz (1 pedaço)", "Tinta (vidro de 30 ml)")
            .forEach { assertTrue("Item da Tabela 7-8 ausente: $it", normalize(it) in names) }
        assertEquals(1.0f, byName[normalize("Lâmpada")]!!.costGp, 0.001f)
        assertEquals(1.1f, byName[normalize("Lâmpada")]!!.weightLb, 0.01f)
        assertEquals(0.01f, byName[normalize("Giz (1 pedaço)")]!!.costGp, 0.001f)
        assertEquals(0.0f, byName[normalize("Giz (1 pedaço)")]!!.weightLb, 0.0f)
        assertEquals(8.0f, byName[normalize("Tinta (vidro de 30 ml)")]!!.costGp, 0.001f)
        assertEquals(0.0f, byName[normalize("Tinta (vidro de 30 ml)")]!!.weightLb, 0.0f)
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
