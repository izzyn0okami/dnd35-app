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
    @Test
    fun additionalCombatAndItemCreationFeatReferencesExplainSpecificExceptions() {
        val greaterWeaponFocus = FeatGlossarySearch.search("Foco em Arma Maior").first()
        assertTrue(greaterWeaponFocus.reference?.special?.contains("8º nível de guerreiro") == true)
        assertTrue(greaterWeaponFocus.reference?.description?.contains("acumula") == true)

        val extraTurning = FeatGlossarySearch.search("Expulsão Adicional").first()
        assertTrue(extraTurning.reference?.special?.contains("cada modalidade") == true)

        val grapple = FeatGlossarySearch.search("Imobilização Aprimorada").first()
        assertTrue(grapple.reference?.description?.contains("ataque") == true)
        assertTrue(grapple.reference?.special?.contains("6º nível") == true)

        val quickDraw = FeatGlossarySearch.search("Saque Rápido").first()
        assertTrue(quickDraw.reference?.description?.contains("ação livre") == true)
        assertTrue(quickDraw.reference?.special?.contains("bônus base de ataque +1") == true)

        val manyshot = FeatGlossarySearch.search("Tiro Múltiplo").first()
        assertTrue(manyshot.reference?.description?.contains("9 m") == true)
        assertTrue(manyshot.reference?.special?.contains("dano adicional baseado em precisão") == true)

        val improvedPreciseShot = FeatGlossarySearch.search("Tiro Preciso Aprimorado").first()
        assertTrue(improvedPreciseShot.reference?.special?.contains("camuflagem total") == true)

        val greaterCleave = FeatGlossarySearch.search("Trespassar Maior").first()
        assertTrue(greaterCleave.reference?.description?.contains("não existe limite") == true)
    }


    @Test
    fun mountedAndCombatManeuverFeatReferencesExplainTheirSpecificRules() {
        val catch = FeatGlossarySearch.search("Apanhar Objetos").first()
        assertTrue(catch.reference?.description?.contains("fora do turno") == true)
        assertTrue(catch.reference?.special?.contains("mão livre") == true)

        val unarmed = FeatGlossarySearch.search("Ataque Desarmado Aprimorado").first()
        assertTrue(unarmed.reference?.normal?.contains("ataques de oportunidade") == true)
        assertTrue(unarmed.reference?.special?.contains("1º nível") == true)

        val stunning = FeatGlossarySearch.search("Ataque Atordoante").first()
        assertTrue(stunning.reference?.description?.contains("CD 10") == true)
        assertTrue(stunning.reference?.special?.contains("uma vez por rodada") == true)

        val mountedArchery = FeatGlossarySearch.search("Arquearia Montada").first()
        assertTrue(mountedArchery.reference?.description?.contains("–8 para –4") == true)

        val mountedCombat = FeatGlossarySearch.search("Combate Montado").first()
        assertTrue(mountedCombat.reference?.description?.contains("teste de Cavalgar") == true)

        val overrun = FeatGlossarySearch.search("Atropelar Aprimorado").first()
        assertTrue(overrun.reference?.normal?.contains("evitar ou bloquear") == true)

        val bullRush = FeatGlossarySearch.search("Encontrão Aprimorado").first()
        assertTrue(bullRush.reference?.description?.contains("+4") == true)

        val trample = FeatGlossarySearch.search("Pisotear").first()
        assertTrue(trample.reference?.description?.contains("cascos") == true)

        val sunder = FeatGlossarySearch.search("Separar Aprimorado").first()
        assertTrue(sunder.reference?.description?.contains("objeto empunhado") == true)
    }

    
    @Test
    fun itemCreationAndReloadFeatReferencesExplainCostsAndActionRules() {
        val scroll = FeatGlossarySearch.search("Escrever Pergaminho").first()
        assertTrue(scroll.reference?.description?.contains("1/25") == true)
        assertTrue(scroll.reference?.sourcePage == 80)

        val wand = FeatGlossarySearch.search("Criar Varinha").first()
        assertTrue(wand.reference?.description?.contains("50 cargas") == true)
        assertTrue(wand.reference?.special?.contains("multiplicados por 50") == true)

        val potion = FeatGlossarySearch.search("Preparar Poção").first()
        assertTrue(potion.reference?.description?.contains("3º nível ou inferior") == true)
        assertTrue(potion.reference?.sourcePage == 83)

        val ring = FeatGlossarySearch.search("Forjar Anel").first()
        assertTrue(ring.reference?.special?.contains("15.000 XP") == true)

        val twoWeaponDefense = FeatGlossarySearch.search("Bloqueio Ambidestro").first()
        assertTrue(twoWeaponDefense.reference?.description?.contains("bônus de escudo") == true)
        assertTrue(twoWeaponDefense.reference?.special?.contains("Destreza 15") == true)

        val reload = FeatGlossarySearch.search("Rapidez de Recarga").first()
        assertTrue(reload.reference?.description?.contains("ação livre") == true)
        assertTrue(reload.reference?.normal?.contains("ação de rodada completa") == true)
    }

    @Test
    fun newlyAuditedMetamagicAndSpecialFeatReferencesMatchTheBrazilianHandbook() {
        val counterspell = FeatGlossarySearch.search("Contramágica Aprimorada").first()
        assertTrue(counterspell.reference?.description?.contains("nível superior") == true)

        val spellMastery = FeatGlossarySearch.search("Dominar Magia").first()
        assertTrue(spellMastery.benefit.contains("modificador de Inteligência"))
        assertTrue(spellMastery.reference?.description?.contains("modificador de Inteligência") == true)

        val greaterWeaponSpecialization = FeatGlossarySearch.search("Especialização em Arma Maior").first()
        assertTrue(greaterWeaponSpecialization.reference?.description?.contains("+2 adicional") == true)
        assertTrue(greaterWeaponSpecialization.reference?.special?.contains("12º nível de guerreiro") == true)

        val augmentedSummoning = FeatGlossarySearch.search("Potencializar Invocação").first()
        assertTrue(augmentedSummoning.reference?.description?.contains("bônus de aprimoramento") == true)

        val widen = FeatGlossarySearch.search("Aumentar Magia").first()
        assertTrue(widen.reference?.description?.contains("15 m + 1,5 m/nível") == true)

        val heighten = FeatGlossarySearch.search("Elevar Magia").first()
        assertTrue(heighten.benefit.contains("qualquer nível de magia superior"))
        assertTrue(heighten.reference?.description?.contains("até o limite do 9º nível") == true)
    }


    @Test
    fun diehardAndLeadershipReferencesMatchTheBrazilianHandbook() {
        val diehard = FeatGlossarySearch.search("Duro de Matar").first()
        assertTrue(diehard.benefit.contains("uma única ação de movimento"))
        assertTrue(diehard.benefit.contains("-10 PV"))
        assertTrue(diehard.reference?.description?.contains("ação padrão") == true)
        assertTrue(diehard.reference?.special?.contains("Tolerância") == true)

        val leadership = FeatGlossarySearch.search("Liderança").first()
        assertTrue(leadership.benefit.contains("companheiros leais"))
        assertTrue(leadership.benefit.contains("seguidores devotados"))
        assertFalse(leadership.benefit.contains("cofre leal"))
        assertTrue(leadership.reference?.sourcePage == 82)
    }


    @Test
    fun skillAndSpellUtilityFeatReferencesMatchTheBrazilianHandbook() {
        val nimbleFingers = FeatGlossarySearch.search("Dedos Lépidos").first()
        assertTrue(nimbleFingers.reference?.description?.contains("Operar Mecanismo") == true)
        assertTrue(nimbleFingers.reference?.description?.contains("Abrir Fechaduras") == true)

        val eschewMaterials = FeatGlossarySearch.search("Ignorar Componentes Materiais").first()
        assertTrue(eschewMaterials.reference?.description?.contains("1 PO ou inferior") == true)
        assertTrue(eschewMaterials.reference?.special?.contains("mais de 1 PO") == true)

        val deftHands = FeatGlossarySearch.search("Mãos Leves").first()
        assertTrue(deftHands.reference?.description?.contains("Prestidigitação") == true)
        assertTrue(deftHands.reference?.description?.contains("Usar Cordas") == true)

        val track = FeatGlossarySearch.search("Rastrear").first()
        assertTrue(track.reference?.description?.contains("1,5 km") == true)
        assertTrue(track.reference?.description?.contains("metade do deslocamento") == true)
    }


    @Test
    fun weaponArmorAndShieldProficiencyReferencesMatchTheBrazilianHandbook() {
        val common = FeatGlossarySearch.search("Usar Arma Comum").first()
        assertTrue(common.reference?.special?.contains("domínio da Guerra") == true)

        val exotic = FeatGlossarySearch.search("Usar Arma Exótica").first()
        assertTrue(exotic.reference?.special?.contains("Força 13") == true)
        assertTrue(exotic.reference?.special?.contains("arma exótica diferente") == true)

        val simple = FeatGlossarySearch.search("Usar Armas Simples").first()
        assertTrue(simple.reference?.special?.contains("druidas, monges, ladinos e magos") == true)

        val light = FeatGlossarySearch.search("Usar Armadura (leve)").first()
        assertTrue(light.reference?.description?.contains("Arte da Fuga") == true)
        val medium = FeatGlossarySearch.search("Usar Armadura (média)").first()
        assertTrue(medium.reference?.special?.contains("Usar Armadura (leve)") == true)
        val heavy = FeatGlossarySearch.search("Usar Armadura (pesada)").first()
        assertTrue(heavy.reference?.special?.contains("Usar Armadura (média)") == true)

        val shield = FeatGlossarySearch.search("Usar Escudo").first()
        assertTrue(shield.reference?.description?.contains("broquéis") == true)
        val shieldBash = FeatGlossarySearch.search("Ataque com Escudo Aprimorado").first()
        assertTrue(shieldBash.reference?.description?.contains("Classe de Armadura") == true)
        val towerShield = FeatGlossarySearch.search("Usar Escudo de Corpo").first()
        assertTrue(towerShield.reference?.special?.contains("Guerreiros") == true)
    }

}
