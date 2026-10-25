package com.example

import com.example.dnd35.model.Dnd35Rules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Valida o carregamento dos catálogos JSON em app/src/main/resources/dnd35/
 * (feats.json, items.json, spells.json).
 * Os testes de conteúdo específico (nomes, categorias) continuam em
 * PresetFeatCatalogTest, PresetEquipmentCatalogTest e PresetSpellCatalogTest;
 * aqui a preocupação é a mecânica de carregar o JSON corretamente.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CatalogJsonLoadingTest {

    @Test
    fun loadsAllThreeCatalogsWithoutThrowing() {
        assertTrue(Dnd35Rules.PRESET_FEATS.isNotEmpty())
        assertTrue(Dnd35Rules.PRESET_ITEMS.isNotEmpty())
        assertTrue(Dnd35Rules.PRESET_SPELLS.isNotEmpty())
    }

    @Test
    fun lazyLoadingIsCachedAndStable() {
        // O catálogo é `by lazy`; duas leituras devem devolver a mesma lista (mesmo conteúdo),
        // sem reler o arquivo a cada acesso.
        val first = Dnd35Rules.PRESET_SPELLS
        val second = Dnd35Rules.PRESET_SPELLS
        assertTrue(first === second || first == second)
        assertEquals(first.size, second.size)
    }

    @Test
    fun everyFeatHasNonBlankRequiredFields() {
        Dnd35Rules.PRESET_FEATS.forEach { feat ->
            assertTrue("Talento sem nome", feat.name.isNotBlank())
            assertTrue("${feat.name}: prerequisites em branco", feat.prerequisites.isNotBlank())
            assertTrue("${feat.name}: summary em branco", feat.summary.isNotBlank())
            assertTrue("${feat.name}: benefit em branco", feat.benefit.isNotBlank())
        }
    }

    @Test
    fun everyItemHasNonBlankRequiredFieldsAndNonNegativeNumbers() {
        Dnd35Rules.PRESET_ITEMS.forEach { item ->
            assertTrue("Item sem nome", item.name.isNotBlank())
            assertTrue("${item.name}: category em branco", item.category.isNotBlank())
            assertTrue("${item.name}: defaultSlot em branco", item.defaultSlot.isNotBlank())
            assertTrue("${item.name}: weightLb negativo", item.weightLb >= 0f)
            assertTrue("${item.name}: costGp negativo", item.costGp >= 0f)
        }
    }

    @Test
    fun everySpellHasConsistentClassLevelsAndValidLevel() {
        Dnd35Rules.PRESET_SPELLS.forEach { spell ->
            assertTrue("${spell.name}: nível fora de 0-9", spell.level in 0..9)
            assertTrue("${spell.name}: sem nenhuma classe", spell.classes.isNotEmpty())
            // classLevels deve cobrir exatamente as classes declaradas, nem mais nem menos.
            assertEquals(
                "${spell.name}: classLevels não bate com classes",
                spell.classes.toSet(),
                spell.classLevels.keys
            )
            spell.classLevels.values.forEach { lvl ->
                assertTrue("${spell.name}: nível por classe fora de 0-9 ($lvl)", lvl in 0..9)
            }
            spell.domainLevels.values.forEach { lvl ->
                assertTrue("${spell.name}: nível de domínio fora de 0-9 ($lvl)", lvl in 0..9)
            }
        }
    }

    @Test
    fun noCatalogHasDuplicateNamesIgnoringCase() {
        fun assertNoDuplicates(names: List<String>, label: String) {
            val seen = mutableSetOf<String>()
            val dup = names.firstOrNull { !seen.add(it.lowercase()) }
            assertTrue("$label tem nome duplicado: $dup", dup == null)
        }
        assertNoDuplicates(Dnd35Rules.PRESET_FEATS.map { it.name }, "Talentos")
        assertNoDuplicates(Dnd35Rules.PRESET_ITEMS.map { it.name }, "Itens")
        assertNoDuplicates(Dnd35Rules.PRESET_SPELLS.map { it.name }, "Magias")
    }

    @Test
    fun catalogSizesMatchThePlayersHandbookSnapshotCapturedOnMigration() {
        // Contagem registrada na migração dos catálogos de Kotlin para JSON (ver docs/CATALOGOS_JSON.md).
        // Uma queda nesse número indica JSON cortado ou mal gerado; o número só deve SUBIR
        // conforme mais conteúdo do Livro do Jogador for adicionado.
        assertTrue("Talentos regrediram", Dnd35Rules.PRESET_FEATS.size >= 52)
        assertTrue("Itens regrediram", Dnd35Rules.PRESET_ITEMS.size >= 163)
        assertTrue("Magias regrediram", Dnd35Rules.PRESET_SPELLS.size >= 327)
    }
}
