package com.example

import com.example.dnd35.model.Dnd35Rules
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Confere as progressões gerais da Tabela 3-1 do Livro do Jogador 3.5
 * em todos os níveis de classe, inclusive nos pontos de transição.
 */
class ClassProgressionTest {

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
        assertEquals(1, monk.features.count { it.level == 13 && it.name == "Ataque Chi (+2)" })
        assertEquals(1, monk.features.count { it.level == 12 && it.name == "Passo Etéreo" })
        assertEquals(1, monk.features.count { it.level == 16 && it.name == "Ataque Chi (Adamante)" })
        assertEquals("Recebe um segundo ataque adicional na rajada de golpes, também com o bônus base de ataque máximo.",
            monk.features.single { it.level == 11 && it.name == "Rajada Maior" }.description)
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
}
