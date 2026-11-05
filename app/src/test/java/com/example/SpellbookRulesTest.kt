package com.example

import com.example.dnd35.model.CharacterCalculator
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.CharacterJsonAdapters
import com.example.dnd35.model.CharacterSpell
import com.example.dnd35.model.Dnd35Rules
import com.example.dnd35.model.SpellCastRecord
import com.example.dnd35.model.SpellFilters
import com.example.dnd35.model.SpellbookRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Robolectric: CharacterCalculator.compute() e os adaptadores JSON usam android.util.Log.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SpellbookRulesTest {

    private fun stats(multiclassJson: String, int: Int = 10, wis: Int = 10, cha: Int = 10) =
        CharacterCalculator.compute(
            CharacterEntity(multiclassJson = multiclassJson, baseInt = int, baseWis = wis, baseCha = cha)
        )

    private fun spell(name: String, level: Int, casterClass: String = "", prepared: Boolean = false, count: Int = 1, inBook: Boolean = false) =
        CharacterSpell(name = name, level = level, casterClass = casterClass, prepared = prepared, preparedCount = count, inSpellbook = inBook)

    private fun preset(name: String) = Dnd35Rules.PRESET_SPELLS.first { it.name == name }

    @Test
    fun castingStyleFollowsTheClassRules() {
        assertEquals(SpellbookRules.Style.SPELLBOOK, SpellbookRules.styleFor("Mago"))
        assertEquals(SpellbookRules.Style.SPONTANEOUS, SpellbookRules.styleFor("Feiticeiro"))
        assertEquals(SpellbookRules.Style.SPONTANEOUS, SpellbookRules.styleFor("Bardo"))
        for (c in listOf("Clérigo", "Druida", "Paladino", "Ranger")) {
            assertEquals(c, SpellbookRules.Style.PREPARED_LIST, SpellbookRules.styleFor(c))
        }
        assertEquals(null, SpellbookRules.styleFor("Guerreiro"))
    }

    @Test
    fun dcAndMinimumAbilityScoreUseTheSpellLevel() {
        assertEquals(10 + 3 + 4, SpellbookRules.spellDc(3, 4))
        assertEquals(10, SpellbookRules.minAbilityScore(0))
        assertEquals(19, SpellbookRules.minAbilityScore(9))
    }

    @Test
    fun accessibleCircleFollowsCasterLevelAndLateCasters() {
        assertEquals(1, SpellcastingRules_highest("Mago", 1))
        assertEquals(2, SpellcastingRules_highest("Mago", 3))
        // Paladino só conjura a partir do 4º nível de classe (nível de conjurador 1 = círculo 1).
        val paladin = stats("[{\"className\":\"Paladino\",\"level\":4}]", wis = 14)
        val info = paladin.casterClasses.single()
        assertEquals(1, SpellbookRules.highestAccessibleCircle(info))
        val lowPaladin = stats("[{\"className\":\"Paladino\",\"level\":3}]", wis = 14)
        assertTrue(lowPaladin.casterClasses.isEmpty())
    }

    private fun SpellcastingRules_highest(className: String, level: Int) =
        com.example.dnd35.model.SpellcastingRules.highestAccessibleCircle(className, level)

    @Test
    fun eligibilityChecksClassListCircleAndAbility() {
        val fireball = preset("Bola de Fogo")
        val wizard1 = stats("[{\"className\":\"Mago\",\"level\":1}]", int = 18).casterClasses.single()
        assertEquals(SpellbookRules.Eligibility.CIRCLE_NOT_REACHED, SpellbookRules.eligibility(fireball, wizard1, 18))

        val wizard5Low = stats("[{\"className\":\"Mago\",\"level\":5}]", int = 12).casterClasses.single()
        assertEquals(SpellbookRules.Eligibility.ABILITY_TOO_LOW, SpellbookRules.eligibility(fireball, wizard5Low, 12))
        assertEquals(SpellbookRules.Eligibility.OK, SpellbookRules.eligibility(fireball, wizard5Low, 13))

        val cleric5 = stats("[{\"className\":\"Clérigo\",\"level\":5}]", wis = 16).casterClasses.single()
        assertEquals(SpellbookRules.Eligibility.NOT_ON_CLASS_LIST, SpellbookRules.eligibility(fireball, cleric5, 16))
    }

    @Test
    fun legacySpellsWithoutClassAreAssignedToTheClassThatReachesTheCircle() {
        // Mago 1 só acessa o 1º círculo; Clérigo 5 acessa o 3º: a magia antiga de 3º círculo é do Clérigo.
        val s = stats("[{\"className\":\"Mago\",\"level\":1},{\"className\":\"Clérigo\",\"level\":5}]", int = 14, wis = 14)
        val owner = SpellbookRules.classFor(spell("Dissipar Magia", 3), s.casterClasses)
        assertEquals("Clérigo", owner?.className)
        // Uma magia que registra a classe respeita o registro.
        val explicit = SpellbookRules.classFor(spell("Sono", 1, casterClass = "Mago"), s.casterClasses)
        assertEquals("Mago", explicit?.className)
    }

    @Test
    fun wizardLegacySpellsCountAsInTheSpellbookButNewOnesNeedToBeAdded() {
        assertTrue(SpellbookRules.isInSpellbook(spell("Sono", 1)))
        assertFalse(SpellbookRules.isInSpellbook(spell("Sono", 1, casterClass = "Mago")))
        assertTrue(SpellbookRules.isInSpellbook(spell("Sono", 1, casterClass = "Mago", inBook = true)))
    }

    @Test
    fun preparedUsageSumsPreparationsPerClassAndCircleAndSkipsSpontaneous() {
        val s = stats("[{\"className\":\"Mago\",\"level\":3},{\"className\":\"Feiticeiro\",\"level\":3}]", int = 14, cha = 14)
        val spells = listOf(
            spell("Sono", 1, "Mago", prepared = true, count = 2, inBook = true),
            spell("Armadura Arcana", 1, "Mago", prepared = true, inBook = true),
            spell("Escudo", 1, "Mago", prepared = false, inBook = true),
            spell("Mísseis Mágicos", 1, "Feiticeiro", prepared = true)
        )
        val usage = SpellbookRules.preparedUsage(spells, s.casterClasses)
        assertEquals(3, usage[("Mago" to 1)])
        assertEquals(null, usage[("Feiticeiro" to 1)])
    }

    @Test
    fun overLimitWarningsFlagTooManyPreparedAndTooManyKnown() {
        val wizard = stats("[{\"className\":\"Mago\",\"level\":1}]", int = 10)
        val slots1 = wizard.casterClasses.single().slotsByLevel[1] ?: 0
        val tooMany = listOf(spell("Sono", 1, "Mago", prepared = true, count = slots1 + 1, inBook = true))
        assertEquals(1, SpellbookRules.overLimitWarnings(tooMany, wizard.casterClasses).size)
        val ok = listOf(spell("Sono", 1, "Mago", prepared = true, count = slots1, inBook = true))
        assertTrue(SpellbookRules.overLimitWarnings(ok, wizard.casterClasses).isEmpty())

        val sorcerer = stats("[{\"className\":\"Feiticeiro\",\"level\":1}]", cha = 10)
        val known1 = sorcerer.casterClasses.single().knownSpellsByLevel?.get(1) ?: 0
        val manyKnown = (0..known1).map { spell("Magia $it", 1, "Feiticeiro") }
        assertEquals(1, SpellbookRules.overLimitWarnings(manyKnown, sorcerer.casterClasses).size)
    }

    @Test
    fun presetForFallsBackToAManualSpellWithoutClasses() {
        val manual = SpellbookRules.presetFor(spell("Magia da Mesa", 2))
        assertEquals("Magia da Mesa", manual.name)
        assertTrue(manual.classLevels.isEmpty())
        assertEquals("Bola de Fogo", SpellbookRules.presetFor(spell("bola de fogo", 3)).name)
    }

    // ---- Filtros do catálogo ----

    @Test
    fun filtersGroupFreeTextFieldsIntoBuckets() {
        assertEquals("Padrão", SpellFilters.castTimeBucket("1 ação padrão"))
        assertEquals("Padrão", SpellFilters.castTimeBucket("1 ação padrão ou veja texto"))
        assertEquals("Rodadas", SpellFilters.castTimeBucket("3 rodadas"))
        assertEquals("Minutos ou mais", SpellFilters.castTimeBucket("10 minutos"))
        assertEquals("Livre/rápida", SpellFilters.castTimeBucket("Ação livre"))
        assertEquals("Curto", SpellFilters.rangeBucket("Curto (7,5 m + 1,5 m/2 níveis)"))
        assertEquals("Toque", SpellFilters.rangeBucket("Toque"))
        assertEquals("Instantânea", SpellFilters.durationBucket("Instantânea"))
        assertEquals("Minutos", SpellFilters.durationBucket("1 min/nível (D)"))
        assertEquals("Rodadas", SpellFilters.durationBucket("1 rodada/nível"))
        assertEquals("Permanente", SpellFilters.durationBucket("Permanente; veja texto"))
        assertEquals(setOf("V", "G", "M", "FD"), SpellFilters.componentTokens("V, G, M/FD"))
        assertEquals(setOf("V", "G", "M", "F"), SpellFilters.componentTokens("V, G, M (F)"))
        assertTrue(SpellFilters.matchesSave("Vontade anula (inofensiva)", "Vontade"))
        assertTrue(SpellFilters.matchesSave("Nenhum (veja texto)", "Nenhum"))
        assertFalse(SpellFilters.matchesSave("Fortitude parcial", "Vontade"))
    }

    @Test
    fun filtersUseTheSpellLevelOfTheChosenClass() {
        val byClass = Dnd35Rules.PRESET_SPELLS.filter {
            SpellFilters.matches(it, SpellFilters.Criteria(className = "Mago", level = 3))
        }
        assertTrue(byClass.isNotEmpty())
        assertTrue(byClass.all { SpellbookRules.classLevelOf(it, "Mago") == 3 })
        assertNotNull(byClass.firstOrNull { it.name == "Bola de Fogo" })

        val clerics = Dnd35Rules.PRESET_SPELLS.filter {
            SpellFilters.matches(it, SpellFilters.Criteria(className = "Clérigo"))
        }
        assertTrue(clerics.none { it.name == "Bola de Fogo" })
    }

    @Test
    fun textSearchIgnoresAccentsAndCase() {
        val results = Dnd35Rules.PRESET_SPELLS.filter {
            SpellFilters.matches(it, SpellFilters.Criteria(query = "misseis magicos"))
        }
        assertTrue(results.any { it.name == "Mísseis Mágicos" })
    }

    @Test
    fun everyCatalogSpellFallsInsideTheFilterOptions() {
        // Cada filtro tem que cobrir todo o catálogo: sem magia "sumida" por categoria faltando.
        for (spell in Dnd35Rules.PRESET_SPELLS) {
            assertTrue(spell.name, SpellFilters.castTimeBucket(spell.castTime) in SpellFilters.castTimeOptions)
            assertTrue(spell.name, SpellFilters.rangeBucket(spell.range) in SpellFilters.rangeOptions)
            assertTrue(spell.name, SpellFilters.durationBucket(spell.duration) in SpellFilters.durationOptions)
        }
    }

    // ---- Persistência ----

    @Test
    fun spellJsonKeepsClassSpellbookAndFavoriteAndReadsOldSheets() {
        val original = spell("Sono", 1, "Mago", prepared = true, count = 2, inBook = true).copy(favorite = true)
        val restored = CharacterJsonAdapters.deserializeSpells(CharacterJsonAdapters.serializeSpells(listOf(original))).single()
        assertEquals("Mago", restored.casterClass)
        assertTrue(restored.inSpellbook)
        assertTrue(restored.favorite)
        assertEquals(2, restored.preparedCount)

        // Ficha antiga: JSON sem os campos novos continua válido, com valores padrão.
        val old = CharacterJsonAdapters.deserializeSpells("[{\"id\":\"a\",\"name\":\"Luz\",\"level\":0}]").single()
        assertEquals("", old.casterClass)
        assertFalse(old.inSpellbook)
        assertFalse(old.favorite)
    }

    @Test
    fun spellHistoryRoundTrips() {
        val records = listOf(
            SpellCastRecord(id = "1", timestamp = 1000L, spellName = "Bola de Fogo", spellLevel = 3, casterClass = "Mago", slotLevel = 3),
            SpellCastRecord(id = "2", timestamp = 2000L, spellName = "Luz", spellLevel = 0)
        )
        val restored = CharacterJsonAdapters.deserializeSpellHistory(CharacterJsonAdapters.serializeSpellHistory(records))
        assertEquals(records, restored)
        assertTrue(CharacterJsonAdapters.deserializeSpellHistory("").isEmpty())
        assertTrue(CharacterJsonAdapters.deserializeSpellHistory("isto não é json").isEmpty())
    }

    @Test
    fun copyingASpellCostsOneHundredGoldAndOneHourPerPage() {
        assertEquals(100, SpellbookRules.copyCostGp(0)) // truque: 1 página
        assertEquals(100, SpellbookRules.copyCostGp(1))
        assertEquals(500, SpellbookRules.copyCostGp(5))
        assertEquals(900, SpellbookRules.copyCostGp(9))
        assertEquals(1, SpellbookRules.copyHours(0))
        assertEquals(3, SpellbookRules.copyHours(3))
    }
}
