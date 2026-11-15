package com.example.dnd35.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FeatGlossarySearchTest {
    @Test
    fun searchFindsFeatByNameWithoutAccentSensitivity() {
        val results = FeatGlossarySearch.search("ataque poderoso")
        assertTrue(results.any { it.name.equals("Ataque Poderoso", ignoreCase = true) })
    }

    @Test
    fun searchFindsTermsInsideBenefitsAndPrerequisites() {
        val results = FeatGlossarySearch.search("BBA")
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.prerequisites.contains("BBA", ignoreCase = true) })
    }


    @Test
    fun auditedFeatHasExpandedReferenceSeparateFromSheetSummary() {
        val result = FeatGlossarySearch.search("Ataque Poderoso").first()
        assertTrue(result.summary.isNotBlank())
        assertTrue((result.reference?.description?.length ?: 0) > result.summary.length)
        assertTrue(result.reference?.source?.contains("p. 78") == true)
    }

    @Test
    fun barbarianRageHasDetailedClassReference() {
        val result = ClassFeatureGlossarySearch.search("Fúria")
            .first { it.className == "Bárbaro" }
        assertTrue(result.reference?.restrictions?.contains("Concentração") == true)
        assertTrue(result.reference?.sourcePages == "21–22")
        val improved = ClassFeatureGlossarySearch.search("Esquiva Sobrenatural Aprimorada")
            .first { it.className == "Bárbaro" }
        assertTrue(improved.reference?.term == "Esquiva Sobrenatural Aprimorada")
    }


    @Test
    fun bardicMusicReferenceExplainsUsesAndPerformanceRequirements() {
        val result = ClassFeatureGlossarySearch.search("Música de Bardo")
            .first { it.className == "Bardo" }
        assertTrue(result.reference?.duration?.contains("nível de bardo") == true)
        assertTrue(result.reference?.progression?.contains("graduações em Atuação") == true)
        assertTrue(result.reference?.sourcePages == "25–26")
    }


    @Test
    fun druidWildShapeReferenceExplainsFormLimitsAndProgression() {
        val result = ClassFeatureGlossarySearch.search("Forma Selvagem")
            .first { it.className == "Druida" }
        assertTrue(result.reference?.restrictions?.contains("Dados de Vida") == true)
        assertTrue(result.reference?.progression?.contains("elementais Enormes") == true)
        assertTrue(result.reference?.sourcePages == "31–32")
    }


    @Test
    fun rangerReferencesExplainFavoredEnemyAndCombatStyleMastery() {
        val enemy = ClassFeatureGlossarySearch.search("Inimigo Predileto")
            .first { it.className == "Ranger" }
        assertTrue(enemy.reference?.restrictions?.contains("não se acumulam") == true)
        assertTrue(enemy.reference?.sourcePages == "48")

        val mastery = ClassFeatureGlossarySearch.search("Maestria do Estilo de Combate")
            .first { it.className == "Ranger" }
        assertTrue(mastery.reference?.progression?.contains("Tiro Preciso") == true)
        assertTrue(mastery.reference?.sourcePages == "49")
    }


    @Test
    fun paladinReferencesExplainDailyAndWeeklyUses() {
        val smite = ClassFeatureGlossarySearch.search("Destruir o Mal")
            .first { it.className == "Paladino" }
        assertTrue(smite.reference?.progression?.contains("5 usos/dia") == true)

        val disease = ClassFeatureGlossarySearch.search("Remover Doença")
            .first { it.className == "Paladino" }
        assertTrue(disease.reference?.progression?.contains("5 usos por semana") == true)
    }


    @Test
    fun monkReferencesExplainFlurryAndVibratingPalmLimits() {
        val flurry = ClassFeatureGlossarySearch.search("Rajada de Golpes")
            .first { it.className == "Monge" }
        assertTrue(flurry.reference?.progression?.contains("desaparece no 9º") == true)

        val palm = ClassFeatureGlossarySearch.search("Mão Vibrante")
            .first { it.className == "Monge" }
        assertTrue(palm.reference?.restrictions?.contains("imunes a sucessos decisivos") == true)
        assertTrue(palm.reference?.sourcePages == "44")

        val fall = ClassFeatureGlossarySearch.search("Queda Suave 6 m")
            .first { it.className == "Monge" }
        assertTrue(fall.reference?.term == "Queda Suave")
    }


    @Test
    fun rogueSneakAttackReferenceExplainsImmunitiesAndRange() {
        val sneak = ClassFeatureGlossarySearch.search("Ataque Furtivo")
            .first { it.className == "Ladino" }
        assertTrue(sneak.reference?.restrictions?.contains("9 m") == true)
        assertTrue(sneak.reference?.restrictions?.contains("mortos-vivos") == true)

        val traps = ClassFeatureGlossarySearch.search("Encontrar Armadilhas")
            .first { it.className == "Ladino" }
        assertTrue(traps.reference?.progression?.contains("margem de 10") == true)
    }


    @Test
    fun clericReferencesExplainDomainSlotsAndUndeadChanneling() {
        val domains = ClassFeatureGlossarySearch.search("Domínios da Divindade")
            .first { it.className == "Clérigo" }
        assertTrue(domains.reference?.restrictions?.contains("tendência") == true)

        val turning = ClassFeatureGlossarySearch.search("Expulsar ou Fascinar Mortos-Vivos")
            .first { it.className == "Clérigo" }
        assertTrue(turning.reference?.duration?.contains("3 + modificador de Carisma") == true)
    }


    @Test
    fun arcaneCasterReferencesSeparateSpontaneousCastingFromSpellbookPreparation() {
        val sorcerer = ClassFeatureGlossarySearch.search("Conjuração Espontânea")
            .first { it.className == "Feiticeiro" }
        assertTrue(sorcerer.reference?.restrictions?.contains("Carisma") == true)
        assertTrue(sorcerer.reference?.progression?.contains("a cada dois níveis") == true)

        val wizard = ClassFeatureGlossarySearch.search("Preparação com Grimório")
            .first { it.className == "Mago" }
        assertTrue(wizard.reference?.progression?.contains("duas magias") == true)
        assertTrue(wizard.reference?.sourcePages == "40–41")
    }


    @Test
    fun fighterBonusFeatReferenceExplainsRestrictedListAndPrerequisites() {
        val result = ClassFeatureGlossarySearch.search("Talento Bônus de Guerreiro")
            .first { it.className == "Guerreiro" }
        assertTrue(result.reference?.restrictions?.contains("pré-requisitos") == true)
        assertTrue(result.reference?.sourcePages == "37")
    }


    @Test
    fun featReferencesUseOfficialBrazilianNamesAndDetailedCombatRules() {
        val reflexes = FeatGlossarySearch.search("Reflexos de Combate").first()
        assertTrue(reflexes.name == "Reflexos de Combate")
        assertTrue(reflexes.reference?.special?.contains("surpreso") == true)

        val expertise = FeatGlossarySearch.search("Especialização em Combate").first()
        assertTrue(expertise.reference?.normal?.contains("–4") == true)
        assertTrue(expertise.reference?.sourcePage == 80)
    }


    @Test
    fun searchesCanFindTermsOnlyPresentInExpandedReferenceText() {
        val feat = FeatGlossarySearch.search("armas leves")
        assertTrue(feat.any { it.name == "Ataque Poderoso" })

        val feature = ClassFeatureGlossarySearch.search("Concentração")
        assertTrue(feature.any { it.className == "Bárbaro" && it.reference?.term == "Fúria" })
    }


    @Test
    fun twoWeaponAndCombatReflexesReferencesDescribeExactAttackRules() {
        val twoWeapons = FeatGlossarySearch.search("Combater com Duas Armas").first()
        assertTrue(twoWeapons.reference?.normal?.contains("Tabela 8–10") == true)

        val reflexes = FeatGlossarySearch.search("Reflexos de Combate").first()
        assertTrue(reflexes.reference?.description?.contains("três ataques de oportunidade") == true)
    }


    @Test
    fun skillAndSavingThrowFeatsHaveDistinctReferenceRules() {
        val skill = FeatGlossarySearch.search("Foco em Perícia").first()
        assertTrue(skill.reference?.description?.contains("+3") == true)

        val endurance = FeatGlossarySearch.search("Tolerância").first()
        assertTrue(endurance.reference?.special?.contains("Rangers") == true)
        assertTrue(endurance.reference?.normal?.contains("fatigado") == true)
    }


    @Test
    fun metamagicReferencesExplainLevelCostAndExceptions() {
        val quicken = FeatGlossarySearch.search("Acelerar Magia").first()
        assertTrue(quicken.reference?.special?.contains("quatro níveis") == true)

        val silent = FeatGlossarySearch.search("Magia Silenciosa").first()
        assertTrue(silent.reference?.special?.contains("bardo") == true)

        val greaterPenetration = FeatGlossarySearch.search("Magia Penetrante Maior").first()
        assertTrue(greaterPenetration.reference?.special?.contains("totalizando +4") == true)
    }

    @Test
    fun blankSearchReturnsNoResults() {
        assertTrue(FeatGlossarySearch.search("   ").isEmpty())
    }

    @Test
    fun resultsIncludePlayerHandbookReference() {
        val result = FeatGlossarySearch.search("Foco em Arma").first()
        assertTrue(result.source.contains("Livro do Jogador"))
        assertFalse(result.benefit.isBlank())
    }
}
