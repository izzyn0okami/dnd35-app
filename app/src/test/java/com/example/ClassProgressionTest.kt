package com.example

import com.example.dnd35.model.Dnd35Rules
import com.example.dnd35.model.SpellcastingRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Confere as progressões gerais da Tabela 3-1 do Livro do Jogador 3.5
 * em todos os níveis de classe, inclusive nos pontos de transição.
 */
class ClassProgressionTest {

    @Test
    fun baseAttackProgressionLabelsAreLocalizedAndReadable() {
        assertEquals(
            "Integral (BBA igual ao nível)",
            Dnd35Rules.babProgressionLabel(Dnd35Rules.BabProgression.FULL)
        )
        assertEquals(
            "Três quartos (BBA de 3/4 do nível)",
            Dnd35Rules.babProgressionLabel(Dnd35Rules.BabProgression.THREE_QUARTERS)
        )
        assertEquals(
            "Metade (BBA de 1/2 do nível)",
            Dnd35Rules.babProgressionLabel(Dnd35Rules.BabProgression.HALF)
        )
    }

    @Test
    fun baseAttackBonusMatchesAllThreeProgressionsFromLevelOneToTwenty() {
        val full = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
        val threeQuarters = listOf(0, 1, 2, 3, 3, 4, 5, 6, 6, 7, 8, 9, 9, 10, 11, 12, 12, 13, 14, 15)
        val half = listOf(0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10)

        for (level in 1..20) {
            assertEquals("BBA completo no nível $level", full[level - 1],
                Dnd35Rules.calculateBaseAttackBonus(Dnd35Rules.BabProgression.FULL, level))
            assertEquals("BBA médio no nível $level", threeQuarters[level - 1],
                Dnd35Rules.calculateBaseAttackBonus(Dnd35Rules.BabProgression.THREE_QUARTERS, level))
            assertEquals("BBA ruim no nível $level", half[level - 1],
                Dnd35Rules.calculateBaseAttackBonus(Dnd35Rules.BabProgression.HALF, level))
        }
    }

    @Test
    fun goodAndPoorSavingThrowsMatchEveryLevelFromOneToTwenty() {
        val good = (1..20).map { 2 + it / 2 }
        val poor = (1..20).map { it / 3 }

        for (level in 1..20) {
            assertEquals("Resistência boa no nível $level", good[level - 1],
                Dnd35Rules.calculateBaseSave(Dnd35Rules.SaveProgression.GOOD, level))
            assertEquals("Resistência ruim no nível $level", poor[level - 1],
                Dnd35Rules.calculateBaseSave(Dnd35Rules.SaveProgression.POOR, level))
        }
    }

    @Test
    fun barbarianTrapSenseIncreasesEveryThreeLevels() {
        val barbarian = Dnd35Rules.CLASSES.single { it.name == "Bárbaro" }
        val trapSense = barbarian.features.filter { it.name.startsWith("Sentir Armadilhas") }
            .associate { it.level to it.name }

        assertEquals(mapOf(3 to "Sentir Armadilhas +1", 6 to "Sentir Armadilhas +2",
            9 to "Sentir Armadilhas +3", 12 to "Sentir Armadilhas +4",
            15 to "Sentir Armadilhas +5", 18 to "Sentir Armadilhas +6"), trapSense)
    }

    @Test
    fun monkBonusFeatsAndGreaterFlurryAreRepresentedAtCorrectLevels() {
        val monk = Dnd35Rules.CLASSES.single { it.name == "Monge" }
        assertEquals(1, monk.features.count { it.level == 1 && it.name == "Talento Bônus de Monge (1º nível)" })
        assertEquals(1, monk.features.count { it.level == 2 && it.name == "Talento Bônus de Monge (2º nível)" })
        assertEquals(1, monk.features.count { it.level == 3 && it.name == "Movimento Rápido +3m" })
        assertEquals(1, monk.features.count { it.level == 6 && it.name == "Movimento Rápido +6m" })
        assertEquals(1, monk.features.count { it.level == 9 && it.name == "Movimento Rápido +9m" })
        assertEquals(1, monk.features.count { it.level == 12 && it.name == "Movimento Rápido +12m" })
        assertEquals(1, monk.features.count { it.level == 15 && it.name == "Movimento Rápido +15m" })
        assertEquals(1, monk.features.count { it.level == 18 && it.name == "Movimento Rápido +18m" })
        assertEquals(1, monk.features.count { it.level == 6 && it.name == "Talento Bônus de Monge (6º nível)" })
        assertTrue(monk.features.single { it.level == 2 && it.name == "Talento Bônus de Monge (2º nível)" }
            .description.contains("Reflexos em Combate ou Desviar Objetos"))
        assertTrue(monk.features.single { it.level == 6 && it.name == "Talento Bônus de Monge (6º nível)" }
            .description.contains("Desarme Aprimorado ou Imobilização Aprimorada"))
        assertTrue(monk.features.single { it.level == 2 && it.name == "Talento Bônus de Monge (2º nível)" }
            .description.contains("sem precisar cumprir os pré-requisitos"))
        assertEquals(1, monk.features.count { it.level == 7 && it.name == "Integridade Corporal" })
        assertEquals(1, monk.features.count { it.level == 10 && it.name == "Ataque Chi (Ordem)" })
        assertTrue(monk.features.none { it.name == "Ataque Chi (+2)" })
        assertEquals(1, monk.features.count { it.level == 12 && it.name == "Passo Etéreo" })
        assertTrue(monk.features.single { it.level == 12 && it.name == "Passo Etéreo" }
            .description.contains("metade do nível de monge"))
        assertEquals(1, monk.features.count { it.level == 16 && it.name == "Ataque Chi (Adamante)" })
        assertEquals("Recebe um segundo ataque adicional na rajada de golpes, também com o bônus base de ataque máximo.",
            monk.features.single { it.level == 11 && it.name == "Rajada Maior" }.description)
        assertTrue(monk.features.single { it.level == 3 && it.name == "Movimento Rápido +3m" }
            .description.contains("carga média ou pesada"))
        assertTrue(monk.features.single { it.level == 1 && it.name == "Bônus de CA sem Armadura" }
            .description.contains("apenas carga leve"))
        assertTrue(monk.features.single { it.level == 2 && it.name == "Evasão" }
            .description.contains("armadura leve"))
    }

    @Test
    fun paladinRemoveDiseaseProgressesEveryThreeLevelsStartingAtThree() {
        val paladin = Dnd35Rules.CLASSES.single { it.name == "Paladino" }
        val removeDisease = paladin.features.filter { it.name.startsWith("Remover Doença") }
            .associate { it.level to it.name }
        assertEquals(
            mapOf(
                3 to "Remover Doença 1/semana",
                6 to "Remover Doença 2/semana",
                9 to "Remover Doença 3/semana",
                12 to "Remover Doença 4/semana",
                15 to "Remover Doença 5/semana",
                18 to "Remover Doença 6/semana"
            ),
            removeDisease
        )
    }

    @Test
    fun rogueTrapfindingSeparatesFindingFromDisablingMagicTraps() {
        val rogue = Dnd35Rules.CLASSES.single { it.name == "Ladino" }
        val trapfinding = rogue.features.single { it.level == 1 && it.name == "Encontrar Armadilhas" }
        assertTrue(trapfinding.description.contains("Procurar para localizar armadilhas com CD acima de 20"))
        assertTrue(trapfinding.description.contains("Operar Mecanismo para desarmar armadilhas mágicas"))
    }

    @Test
    fun rogueTrapSenseIncreasesEveryThreeLevels() {
        val rogue = Dnd35Rules.CLASSES.single { it.name == "Ladino" }
        val trapSense = rogue.features.filter { it.name.startsWith("Sentir Armadilhas") }
            .associate { it.level to it.name }
        assertEquals(
            mapOf(
                3 to "Sentir Armadilhas +1",
                6 to "Sentir Armadilhas +2",
                9 to "Sentir Armadilhas +3",
                12 to "Sentir Armadilhas +4",
                15 to "Sentir Armadilhas +5",
                18 to "Sentir Armadilhas +6"
            ),
            trapSense
        )
    }

    @Test
    fun rogueEvasionDocumentsItsArmorRestriction() {
        val rogue = Dnd35Rules.CLASSES.single { it.name == "Ladino" }
        val evasion = rogue.features.single { it.level == 2 && it.name == "Evasão" }
        assertTrue(evasion.description.contains("sem armadura"))
        assertTrue(evasion.description.contains("armadura leve"))
    }

    @Test
    fun rangerEvasionDocumentsItsArmorRestriction() {
        val ranger = Dnd35Rules.CLASSES.single { it.name == "Ranger" }
        val evasion = ranger.features.single { it.level == 9 && it.name == "Evasão" }
        assertTrue(evasion.description.contains("sem armadura"))
        assertTrue(evasion.description.contains("armadura leve"))
    }

    @Test
    fun bardGainsMassSuggestionAtLevelEighteen() {
        val bard = Dnd35Rules.CLASSES.single { it.name == "Bardo" }
        assertEquals(1, bard.features.count { it.level == 18 && it.name == "Sugestão em Massa" })
    }

    @Test
    fun bardFeatureNamesAndTargetProgressionsMatchTheBrazilianHandbook() {
        val bard = Dnd35Rules.CLASSES.single { it.name == "Bardo" }
        val fascinateTargets = bard.features.filter { it.name.startsWith("Fascinar (") }
            .associate { it.level to it.name }
        assertEquals(
            mapOf(
                1 to "Fascinar (1 criatura)",
                4 to "Fascinar (2 criaturas)",
                7 to "Fascinar (3 criaturas)",
                10 to "Fascinar (4 criaturas)",
                13 to "Fascinar (5 criaturas)",
                16 to "Fascinar (6 criaturas)",
                19 to "Fascinar (7 criaturas)"
            ),
            fascinateTargets
        )
        assertTrue(bard.features.any { it.level == 1 && it.name == "Música de Proteção" })
        assertTrue(bard.features.none { it.name == "Contramúsica" })
        assertTrue(bard.features.any { it.level == 12 && it.name == "Melodia da Libertação" })
        assertTrue(bard.features.single { it.level == 9 && it.name == "Inspirar Grandeza" }
            .description.contains("até quatro no 18º nível"))
        assertTrue(bard.features.single { it.level == 15 && it.name == "Inspirar Heroísmo" }
            .description.contains("alvo adicional no 18º nível"))
    }

    @Test
    fun rangerHideInPlainSightDocumentsTheNaturalTerrainDistance() {
        val ranger = Dnd35Rules.CLASSES.single { it.name == "Ranger" }
        val ability = ranger.features.single { it.level == 18 && it.name == "Esconder-se à Plena Vista" }
        assertTrue(ability.description.contains("mesmo enquanto observado"))
        assertTrue(ability.description.contains("3 metros"))
        assertTrue(ability.description.contains("sem cobertura ou camuflagem"))
    }

    @Test
    fun representativeClassProgressionsMatchThePlayerHandbook() {
        val classes = Dnd35Rules.CLASSES.associateBy { it.name }

        assertEquals(Dnd35Rules.BabProgression.FULL, classes.getValue("Guerreiro").babType)
        assertEquals(Dnd35Rules.SaveProgression.GOOD, classes.getValue("Guerreiro").fortType)
        assertEquals(Dnd35Rules.SaveProgression.POOR, classes.getValue("Guerreiro").refType)
        assertEquals(Dnd35Rules.SaveProgression.POOR, classes.getValue("Guerreiro").willType)

        assertEquals(Dnd35Rules.BabProgression.THREE_QUARTERS, classes.getValue("Ladino").babType)
        assertEquals(Dnd35Rules.SaveProgression.POOR, classes.getValue("Ladino").fortType)
        assertEquals(Dnd35Rules.SaveProgression.GOOD, classes.getValue("Ladino").refType)
        assertEquals(Dnd35Rules.SaveProgression.POOR, classes.getValue("Ladino").willType)

        assertEquals(Dnd35Rules.BabProgression.HALF, classes.getValue("Mago").babType)
        assertEquals(Dnd35Rules.SaveProgression.POOR, classes.getValue("Mago").fortType)
        assertEquals(Dnd35Rules.SaveProgression.POOR, classes.getValue("Mago").refType)
        assertEquals(Dnd35Rules.SaveProgression.GOOD, classes.getValue("Mago").willType)

        assertEquals(Dnd35Rules.SaveProgression.GOOD, classes.getValue("Monge").fortType)
        assertEquals(Dnd35Rules.SaveProgression.GOOD, classes.getValue("Monge").refType)
        assertEquals(Dnd35Rules.SaveProgression.GOOD, classes.getValue("Monge").willType)
    }
    
    @Test
    fun uncannyDodgeDescriptionsIncludeInvisibleAttackerAndImmobilizationConditions() {
        val rogueFeature = Dnd35Rules.CLASSES.single { it.name == "Ladino" }
            .features.single { it.name == "Esquiva Sobrenatural" && it.level == 4 }
        val barbarianFeature = Dnd35Rules.CLASSES.single { it.name == "Bárbaro" }
            .features.single { it.name == "Esquiva Sobrenatural" && it.level == 2 }

        listOf(rogueFeature, barbarianFeature).forEach { feature ->
            assertTrue(feature.description.contains("surpreendido"))
            assertTrue(feature.description.contains("oponente invisível"))
            assertTrue(feature.description.contains("imobilizado"))
        }
    }


    @Test
    fun spellcastingCoreUsesPlayerHandbookAbilityAndBonusSlotRules() {
        assertTrue(SpellcastingRules.meetsAbilityRequirement(11, 1))
        assertFalse(SpellcastingRules.meetsAbilityRequirement(11, 2))
        assertTrue(SpellcastingRules.meetsAbilityRequirement(10, 0))
        assertFalse(SpellcastingRules.meetsAbilityRequirement(9, 0))
        assertFalse(SpellcastingRules.meetsAbilityRequirement(30, 10))
        assertEquals(1, SpellcastingRules.bonusSlots(18, 1))
        assertEquals(1, SpellcastingRules.bonusSlots(18, 4))
        assertEquals(0, SpellcastingRules.bonusSlots(18, 5))
        assertEquals(2, SpellcastingRules.bonusSlots(26, 1))
        assertEquals(1, SpellcastingRules.bonusSlots(26, 5))
        assertEquals(0, SpellcastingRules.bonusSlots(10, 1))
    }

    @Test
    fun spellcastingCoreKeepsClassMethodsAndCasterLevelsDistinct() {
        assertEquals(SpellcastingRules.Method.SPONTANEOUS_KNOWN_SPELLS, SpellcastingRules.methodForClass("Bardo"))
        assertEquals(SpellcastingRules.Method.SPONTANEOUS_KNOWN_SPELLS, SpellcastingRules.methodForClass("Feiticeiro"))
        assertEquals(SpellcastingRules.Method.SPELLBOOK_PREPARATION, SpellcastingRules.methodForClass("Mago"))
        assertEquals(SpellcastingRules.Method.DIVINE_PREPARATION, SpellcastingRules.methodForClass("Clérigo"))
        assertEquals(SpellcastingRules.Method.DIVINE_PREPARATION, SpellcastingRules.methodForClass("Druida"))
        assertEquals(SpellcastingRules.Method.DIVINE_PREPARATION, SpellcastingRules.methodForClass("Paladino"))
        assertEquals(SpellcastingRules.Method.DIVINE_PREPARATION, SpellcastingRules.methodForClass("Ranger"))
        assertEquals(SpellcastingRules.Method.NONE, SpellcastingRules.methodForClass("Guerreiro"))
        assertEquals(0, SpellcastingRules.casterLevel("Ranger", 3))
        assertEquals(2, SpellcastingRules.casterLevel("Ranger", 4))
        assertEquals(2, SpellcastingRules.casterLevel("Ranger", 5))
        assertEquals(10, SpellcastingRules.casterLevel("Ranger", 20))
        assertEquals(1, SpellcastingRules.casterLevel("Paladino", 4))
        assertEquals(17, SpellcastingRules.casterLevel("Paladino", 20))
        assertEquals(7, SpellcastingRules.casterLevel("Mago", 7))
    }


    @Test
    fun bardKnownSpellProgressionMatchesPlayerHandbookTable() {
        assertEquals(4, SpellcastingRules.knownSpells("Bardo", 1, 0))
        assertEquals(0, SpellcastingRules.knownSpells("Bardo", 1, 1))
        assertEquals(2, SpellcastingRules.knownSpells("Bardo", 2, 1))
        assertEquals(2, SpellcastingRules.knownSpells("Bardo", 4, 2))
        assertEquals(2, SpellcastingRules.knownSpells("Bardo", 7, 3))
        assertEquals(4, SpellcastingRules.knownSpells("Bardo", 20, 6))
        assertEquals(null, SpellcastingRules.knownSpells("Bardo", 21, 0))
    }

    @Test
    fun bardDailySpellSlotProgressionMatchesPlayerHandbookTable() {
        assertEquals(2, SpellcastingRules.baseDailySlots("Bardo", 1, 0))
        assertEquals(0, SpellcastingRules.baseDailySlots("Bardo", 1, 1))
        assertEquals(1, SpellcastingRules.baseDailySlots("Bardo", 3, 1))
        assertEquals(2, SpellcastingRules.baseDailySlots("Bardo", 6, 2))
        assertEquals(1, SpellcastingRules.baseDailySlots("Bardo", 8, 3))
        assertEquals(4, SpellcastingRules.baseDailySlots("Bardo", 20, 6))
        assertEquals(null, SpellcastingRules.baseDailySlots("Bardo", 21, 0))
    }

    @Test
    fun druidDailySpellSlotProgressionMatchesPlayerHandbookTable() {
        assertEquals(3, SpellcastingRules.baseDailySlots("Druida", 1, 0))
        assertEquals(1, SpellcastingRules.baseDailySlots("Druida", 1, 1))
        assertEquals(1, SpellcastingRules.baseDailySlots("Druida", 3, 2))
        assertEquals(1, SpellcastingRules.baseDailySlots("Druida", 7, 4))
        assertEquals(1, SpellcastingRules.baseDailySlots("Druida", 13, 7))
        assertEquals(4, SpellcastingRules.baseDailySlots("Druida", 20, 9))
        assertEquals(null, SpellcastingRules.baseDailySlots("Druida", 21, 0))
    }

    @Test
    fun sorcererDailySpellSlotProgressionMatchesPlayerHandbookTable() {
        assertEquals(5, SpellcastingRules.baseDailySlots("Feiticeiro", 1, 0))
        assertEquals(3, SpellcastingRules.baseDailySlots("Feiticeiro", 1, 1))
        assertEquals(3, SpellcastingRules.baseDailySlots("Feiticeiro", 4, 2))
        assertEquals(3, SpellcastingRules.baseDailySlots("Feiticeiro", 6, 3))
        assertEquals(3, SpellcastingRules.baseDailySlots("Feiticeiro", 14, 7))
        assertEquals(6, SpellcastingRules.baseDailySlots("Feiticeiro", 20, 9))
        assertEquals(null, SpellcastingRules.baseDailySlots("Mago", 20, 9))
        assertEquals(null, SpellcastingRules.baseDailySlots("Feiticeiro", 0, 0))
    }

    @Test
    fun sorcererKnownSpellProgressionMatchesPlayerHandbookTable() {
        assertEquals(4, SpellcastingRules.knownSpells("Feiticeiro", 1, 0))
        assertEquals(2, SpellcastingRules.knownSpells("Feiticeiro", 1, 1))
        assertEquals(1, SpellcastingRules.knownSpells("Feiticeiro", 4, 2))
        assertEquals(1, SpellcastingRules.knownSpells("Feiticeiro", 6, 3))
        assertEquals(1, SpellcastingRules.knownSpells("Feiticeiro", 12, 6))
        assertEquals(1, SpellcastingRules.knownSpells("Feiticeiro", 18, 9))
        assertEquals(3, SpellcastingRules.knownSpells("Feiticeiro", 20, 9))
        assertEquals(null, SpellcastingRules.knownSpells("Mago", 20, 9))
        assertEquals(null, SpellcastingRules.knownSpells("Feiticeiro", 0, 0))
    }

    @Test
    fun clericDailySpellSlotsAndDomainSlotsStaySeparate() {
        assertEquals(3, SpellcastingRules.baseDailySlots("Clérigo", 1, 0))
        assertEquals(1, SpellcastingRules.baseDailySlots("Clérigo", 1, 1))
        assertEquals(1, SpellcastingRules.baseDailySlots("Clérigo", 3, 2))
        assertEquals(1, SpellcastingRules.baseDailySlots("Clérigo", 13, 7))
        assertEquals(4, SpellcastingRules.baseDailySlots("Clérigo", 20, 9))
        assertEquals(0, SpellcastingRules.domainSlots("Clérigo", 1, 0))
        assertEquals(1, SpellcastingRules.domainSlots("Clérigo", 1, 1))
        assertEquals(0, SpellcastingRules.domainSlots("Clérigo", 1, 2))
        assertEquals(1, SpellcastingRules.domainSlots("Clérigo", 20, 9))
        assertEquals(null, SpellcastingRules.domainSlots("Druida", 20, 9))
        assertEquals(null, SpellcastingRules.domainSlots("Clérigo", 21, 1))
    }

    @Test
    fun wizardDailySpellSlotProgressionMatchesPlayerHandbookTable() {
        assertEquals(3, SpellcastingRules.baseDailySlots("Mago", 1, 0))
        assertEquals(1, SpellcastingRules.baseDailySlots("Mago", 1, 1))
        assertEquals(1, SpellcastingRules.baseDailySlots("Mago", 3, 2))
        assertEquals(1, SpellcastingRules.baseDailySlots("Mago", 7, 4))
        assertEquals(1, SpellcastingRules.baseDailySlots("Mago", 13, 7))
        assertEquals(1, SpellcastingRules.baseDailySlots("Mago", 17, 9))
        assertEquals(4, SpellcastingRules.baseDailySlots("Mago", 20, 9))
        assertEquals(null, SpellcastingRules.baseDailySlots("Mago", 21, 9))
    }

}
