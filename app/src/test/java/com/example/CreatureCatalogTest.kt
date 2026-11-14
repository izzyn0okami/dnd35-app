package com.example

import com.example.dnd35.model.CreatureCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CreatureCatalogTest {
    @Test
    fun catalogLoadsWithUniqueIdsAndValidSizes() {
        val all = CreatureCatalog.ALL
        assertTrue("Catálogo deveria ter ao menos 200 criaturas", all.size >= 200)
        assertEquals(all.size, all.map { it.id }.toSet().size)
        all.forEach {
            assertTrue("Tamanho inválido em ${it.name}: ${it.size}", it.size in CreatureCatalog.SIZES)
            assertTrue("PV inválido em ${it.name}", it.hitPoints > 0)
        }
    }

    @Test
    fun wolfMatchesMonsterManual() {
        val wolf = CreatureCatalog.byId("lobo")
        assertNotNull(wolf)
        wolf!!
        assertEquals("2d8+4", wolf.hitDice)
        assertEquals(13, wolf.hitPoints)
        assertEquals(14, wolf.armorClass)
        assertEquals("Médio", wolf.size)
    }

    @Test
    fun owlHasFractionalChallengeRating() {
        val owl = CreatureCatalog.byId("coruja")!!
        assertEquals("1/4", owl.challengeRating)
        assertEquals(0.25, owl.challengeRatingValue, 0.0001)
        assertEquals("Miúdo", owl.size)
    }

    @Test
    fun searchIgnoresAccentsAndCombinesFilters() {
        val byAccent = CreatureCatalog.filter(query = "MEDIO")
        assertTrue(byAccent.isEmpty() || byAccent.all { CreatureCatalog.normalizeForSearch(it.name).contains("medio") })
        assertTrue(CreatureCatalog.filter(query = "lobo").any { it.id == "lobo" })
        val weak = CreatureCatalog.filter(maxChallengeRating = 1.0, size = "Médio")
        assertTrue(weak.isNotEmpty())
        assertTrue(weak.all { it.size == "Médio" && it.challengeRatingValue <= 1.0 })
    }

    @Test
    fun undeadStyleCreaturesHaveNullIntelligence() {
        assertTrue(CreatureCatalog.ALL.any { it.abilities.int == null })
    }

    @Test
    fun srdAnimalsFillTheCompanionAndSummonGaps() {
        val hyena = CreatureCatalog.byId("hiena")!!
        assertEquals(13, hyena.hitPoints)
        assertEquals(14, hyena.armorClass)
        assertEquals("1", hyena.challengeRating)
        assertTrue(hyena.source.startsWith("SRD 3.5"))

        val viper = CreatureCatalog.byId("vibora-media")!!
        assertEquals(9, viper.hitPoints)
        assertEquals(16, viper.armorClass)

        val scorpion = CreatureCatalog.byId("escorpiao-medio")!!
        assertEquals("2d8+4", scorpion.hitDice)
        assertEquals(null, scorpion.abilities.int)

        assertTrue(CreatureCatalog.byId("aranha-colossal") != null)
        assertTrue(CreatureCatalog.byId("centopeia-imensa") != null)
        assertTrue(CreatureCatalog.byId("tubarao-enorme") != null)
    }

    @Test
    fun rulesEligibilityFlagsMatchPlayersHandbookLists() {
        val wolf = CreatureCatalog.byId("lobo")!!
        assertTrue(wolf.animalCompanion)
        assertTrue(CreatureCatalog.byId("arminho-atroz")!!.animalCompanion)
        assertTrue(wolf.summonedCreature)
        assertTrue(!wolf.familiar)
        assertTrue(CreatureCatalog.byId("urso-atroz")!!.summonedCreature)
        assertTrue(CreatureCatalog.byId("elefante")!!.summonedCreature)
        assertTrue(CreatureCatalog.byId("polvo-gigante")!!.summonedCreature)
        assertTrue(CreatureCatalog.byId("lula-gigante")!!.summonedCreature)
        assertTrue(CreatureCatalog.byId("texugo-atroz")!!.animalCompanion)
        assertTrue(CreatureCatalog.byId("texugo-atroz")!!.summonedCreature)
        assertTrue(CreatureCatalog.byId("cobra-constritora-gigante")!!.animalCompanion)
        assertTrue(CreatureCatalog.byId("cobra-constritora-gigante")!!.summonedCreature)

        val owl = CreatureCatalog.byId("coruja")!!
        assertTrue(owl.animalCompanion)
        assertTrue(owl.summonedCreature)
        assertTrue(owl.familiar)

        val cat = CreatureCatalog.byId("gato")!!
        assertTrue(cat.familiar)
        assertTrue(!cat.animalCompanion)

        val aboleth = CreatureCatalog.byId("abolete")!!
        assertTrue(!aboleth.animalCompanion)
        assertTrue(!aboleth.summonedCreature)
        assertTrue(!aboleth.familiar)
    }


    @Test
    fun monstrousArthropodsInSummonMonsterListsAreMarked() {
        listOf(
            "centopeia-media", "centopeia-grande", "centopeia-enorme",
            "centopeia-imensa", "centopeia-colossal",
            "aranha-pequena", "aranha-media", "aranha-grande",
            "aranha-enorme", "aranha-imensa", "aranha-colossal",
            "escorpiao-medio", "escorpiao-grande", "escorpiao-enorme",
            "escorpiao-imenso"
        ).forEach { id ->
            assertTrue("$id deveria constar nas listas de invocação", CreatureCatalog.byId(id)!!.summonedCreature)
        }
        assertTrue("Escorpião Colossal não consta na tabela de Invocar Criaturas", !CreatureCatalog.byId("escorpiao-colossal")!!.summonedCreature)
    }

    @Test
    fun direAnimalsHaveNarrativeAndCombatSummaries() {
        listOf(
            "arminho-atroz", "carcaju-atroz", "gorila-atroz", "javali-atroz",
            "leao-atroz", "lobo-atroz", "morcego-atroz", "rato-atroz",
            "tigre-atroz", "tubarao-atroz", "urso-atroz"
        ).forEach { id ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem resumo de combate", creature.combat.isNotBlank())
        }
    }

    @Test
    fun earlyAlphabeticalCreaturesHaveNarrativeAndCombatSummaries() {
        listOf(
            "aranea", "aranha-interplanar", "arbusto-errante", "arconte-luminar",
            "arconte-mensageiro", "azer", "beholder", "belker", "anis", "bruxa-verde"
        ).forEach { id ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem resumo de combate", creature.combat.isNotBlank())
        }
        val beholder = CreatureCatalog.byId("beholder")!!
        assertTrue(beholder.attack.contains("raios ópticos"))
        assertTrue(beholder.fullAttack.contains("raios ópticos"))
    }

    @Test
    fun bugbearToConstrictorHaveNarrativeAndCombatSummaries() {
        listOf(
            "bugbear", "bulette", "cacador-invisivel", "cao-infernal",
            "cao-de-guerra-nessian", "cao-teleportador", "cao-yeth",
            "chuul", "cocatriz", "constritor"
        ).forEach { id ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem resumo de combate", creature.combat.isNotBlank())
        }
        assertEquals(17, CreatureCatalog.byId("bugbear")!!.armorClass)
        assertEquals("Plano Elemental do Ar", CreatureCatalog.byId("cacador-invisivel")!!.environment)
        assertEquals("Pântanos temperados", CreatureCatalog.byId("chuul")!!.environment)
        assertEquals("Subterrâneo", CreatureCatalog.byId("constritor")!!.environment)
    }

    @Test
    fun seaWitchThroughHezrouHaveNarrativeAndCombatSummaries() {
        listOf(
            "bruxa-do-mar", "coruja-gigante", "couatl", "cria-vampirica",
            "babau", "balor", "bebilith", "dretch", "glabrezu", "hezrou"
        ).forEach { id ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem resumo de combate", creature.combat.isNotBlank())
        }

        assertTrue(!CreatureCatalog.byId("babau")!!.skills.contains("DEMÔNIO"))
        assertTrue(!CreatureCatalog.byId("dretch")!!.specialAttacks.contains("Caçador"))
        assertTrue(!CreatureCatalog.byId("hezrou")!!.spaceReach.contains("GLABREZU"))
        assertTrue(CreatureCatalog.byId("coruja-gigante")!!.summonedCreature)
    }


    @Test
    fun abyssalCelestialDemonsAndUnderdarkCreaturesHaveNarrativeSummaries() {
        listOf(
            "rato-atroz-abissal", "leao-celestial", "marilith", "nalfeshnee",
            "quasit", "vrock", "derro", "destrachan",
            "devorador-de-aranhas", "diabo-barbado-barbazu"
        ).forEach { id ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem resumo de combate", creature.combat.isNotBlank())
        }

        assertEquals(18, CreatureCatalog.byId("quasit")!!.armorClass)
        assertTrue(!CreatureCatalog.byId("marilith")!!.feats.contains("DEMÔNIO"))
        assertTrue(CreatureCatalog.byId("derro")!!.feats.contains("Lutar às Cegas"))
    }


    @Test
    fun infernalFiendsAndPredatorsHaveNarrativeSummaries() {
        listOf(
            "diabo-das-correntes-kyton", "diabo-farpado-hamatula",
            "diabo-dos-ossos-osyluth", "diabrete", "erinyes",
            "gato-infernal-bezekira", "lorde-das-profundezas",
            "digestor", "dilacerador-cinzento", "dinonico"
        ).forEach { id ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem resumo de combate", creature.combat.isNotBlank())
        }
    }


    @Test
    fun dinosaurEntriesHaveNarrativeAndCombatSummaries() {
        listOf("elasmossauro", "megaraptor", "tiranossauro", "triceratops").forEach { id ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem resumo de combate", creature.combat.isNotBlank())
        }
        assertTrue(CreatureCatalog.byId("elasmossauro")!!.combat.contains("Esconder-se"))
        assertTrue(CreatureCatalog.byId("megaraptor")!!.combat.contains("Bote"))
        assertTrue(CreatureCatalog.byId("tiranossauro")!!.combat.contains("Engolir"))
        assertTrue(CreatureCatalog.byId("triceratops")!!.combat.contains("4d8+20"))
    }


    @Test
    fun shapechangersFeyAndEladrinHaveNarrativeAndCombatSummaries() {
        listOf("doppelganger", "dragonado", "driade", "drider", "bralani", "ghaele", "ente").forEach { id ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem resumo de combate", creature.combat.isNotBlank())
        }
        assertTrue(CreatureCatalog.byId("doppelganger")!!.combat.contains("Detectar Pensamentos"))
        assertTrue(CreatureCatalog.byId("dragonado")!!.combat.contains("Rugido"))
        assertTrue(CreatureCatalog.byId("driade")!!.combat.contains("Simbiose"))
        assertTrue(CreatureCatalog.byId("drider")!!.combat.contains("Veneno"))
        assertTrue(CreatureCatalog.byId("bralani")!!.combat.contains("Rajada Turbilhão"))
        assertTrue(CreatureCatalog.byId("ghaele")!!.combat.contains("Ataque Visual"))
        assertTrue(CreatureCatalog.byId("ente")!!.combat.contains("Animar Árvores"))
    }


    @Test
    fun swarmsExcavatorAndSphinxesHaveNarrativeAndCombatSummaries() {
        listOf(
            "enxame-de-centopeias", "enxame-de-gafanhotos", "enxame-de-morcegos",
            "escavador", "androesfinge", "crioesfinge", "ginoesfinge"
        ).forEach { id ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem resumo de combate", creature.combat.isNotBlank())
        }
        assertTrue(CreatureCatalog.byId("enxame-de-centopeias")!!.combat.contains("Veneno"))
        assertTrue(CreatureCatalog.byId("enxame-de-gafanhotos")!!.combat.contains("Distração"))
        assertTrue(CreatureCatalog.byId("enxame-de-morcegos")!!.combat.contains("Sangramento"))
        assertTrue(CreatureCatalog.byId("escavador")!!.combat.contains("Muco Corrosivo"))
        assertTrue(CreatureCatalog.byId("androesfinge")!!.combat.contains("Rugido"))
        assertTrue(CreatureCatalog.byId("crioesfinge")!!.combat.contains("chifres"))
        assertTrue(CreatureCatalog.byId("ginoesfinge")!!.combat.contains("símbolo"))
    }


    @Test
    fun specterEttercapEttinAndFeyHaveNarrativeAndCombatSummaries() {
        listOf("espectro", "ettercap", "ettin", "grig", "nixie").forEach { id ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem resumo de combate", creature.combat.isNotBlank())
        }
        assertTrue(CreatureCatalog.byId("espectro")!!.combat.contains("Drenar Energia"))
        assertTrue(CreatureCatalog.byId("ettercap")!!.combat.contains("Teia"))
        assertTrue(CreatureCatalog.byId("ettin")!!.combat.contains("Duas Armas"))
        assertTrue(CreatureCatalog.byId("grig")!!.combat.contains("Rabeca"))
        assertTrue(CreatureCatalog.byId("nixie")!!.combat.contains("Enfeitiçar Pessoa"))
    }


    @Test
    fun pixieGhostChaosBeastWillOWispGhostFungusAndGargoyleHaveNarrativeSummaries() {
        listOf(
            "pixie", "fantasma-guerreiro-humano-de-5-nivel", "fera-do-caos",
            "fogofatuo", "fungo-fantasma", "gargula"
        ).forEach { id ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem resumo de combate", creature.combat.isNotBlank())
        }
        assertTrue(CreatureCatalog.byId("pixie")!!.combat.contains("flechas especiais"))
        assertTrue(CreatureCatalog.byId("fantasma-guerreiro-humano-de-5-nivel")!!.combat.contains("possessão"))
        assertTrue(CreatureCatalog.byId("fera-do-caos")!!.combat.contains("Instabilidade Corpórea"))
        assertTrue(CreatureCatalog.byId("fogofatuo")!!.combat.contains("2d8"))
        assertTrue(CreatureCatalog.byId("fungo-fantasma")!!.combat.contains("Invisibilidade Maior"))
        assertTrue(CreatureCatalog.byId("gargula")!!.combat.contains("Imobilidade"))
    }


    @Test
    fun djinniEfreetiAndJanniHaveNarrativeAndCombatSummaries() {
        listOf("djinni", "efreeti", "janni").forEach { id ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem resumo de combate", creature.combat.isNotBlank())
        }
        assertTrue(CreatureCatalog.byId("djinni")!!.combat.contains("vendaval"))
        assertTrue(CreatureCatalog.byId("efreeti")!!.combat.contains("1d6 de dano por fogo"))
        assertTrue(CreatureCatalog.byId("janni")!!.combat.contains("Tolerância aos Elementos"))
    }


    @Test
    fun cloudFireAndStoneGiantsHaveNarrativeAndCombatSummaries() {
        listOf("gigante-das-nuvens", "gigante-do-fogo", "gigante-de-pedra").forEach { id ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem resumo de combate", creature.combat.isNotBlank())
        }
        assertTrue(CreatureCatalog.byId("gigante-das-nuvens")!!.combat.contains("42 m"))
        assertTrue(CreatureCatalog.byId("gigante-do-fogo")!!.combat.contains("2d6 de dano por fogo"))
        assertTrue(CreatureCatalog.byId("gigante-de-pedra")!!.combat.contains("36 m"))
    }


    @Test
    fun girallonGnollAndClayGolemHaveNarrativeAndCombatSummaries() {
        listOf("girallon", "gnoll", "golem-de-barro").forEach { id ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem resumo de combate", creature.combat.isNotBlank())
        }
        assertTrue(CreatureCatalog.byId("girallon")!!.combat.contains("2d4+12"))
        assertTrue(CreatureCatalog.byId("gnoll")!!.combat.contains("vantagem numérica"))
        assertTrue(CreatureCatalog.byId("golem-de-barro")!!.combat.contains("Fúria"))
    }


    @Test
    fun ironGolemGrickGriffonGrimlockAvoralAndHippogriffHaveNarrativeSummaries() {
        listOf(
            "golem-de-ferro", "grick", "grifo", "grimlock", "avoral", "hipogrifo"
        ).forEach { id ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem resumo de combate", creature.combat.isNotBlank())
        }
        assertTrue(CreatureCatalog.byId("golem-de-ferro")!!.combat.contains("nuvem de gás venenoso"))
        assertTrue(CreatureCatalog.byId("grick")!!.combat.contains("quatro tentáculos"))
        assertTrue(CreatureCatalog.byId("grifo")!!.combat.contains("Rasgar"))
        assertTrue(CreatureCatalog.byId("grimlock")!!.combat.contains("Sentido Cego"))
        assertTrue(CreatureCatalog.byId("avoral")!!.combat.contains("aura de medo"))
        assertTrue(CreatureCatalog.byId("hipogrifo")!!.combat.contains("sela exótica"))
    }


    @Test
    fun homunculusAndInevitableUndeadHaveNarrativeSummaries() {
        listOf("homunculo", "marut", "kolyarut", "zelekhut", "inumano").forEach { id ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem resumo de combate", creature.combat.isNotBlank())
        }
        assertTrue(CreatureCatalog.byId("homunculo")!!.combat.contains("sono por 1 minuto"))
        assertTrue(CreatureCatalog.byId("marut")!!.combat.contains("3d6 de dano sônico"))
        assertTrue(CreatureCatalog.byId("kolyarut")!!.combat.contains("Toque Vampírico"))
        assertTrue(CreatureCatalog.byId("zelekhut")!!.combat.contains("correntes com cravos"))
        assertTrue(CreatureCatalog.byId("inumano")!!.combat.contains("1d4 rodadas"))
    }


    @Test
    fun koboldKrakenLammasuGoldenProtectorAndSeaLionHaveNarrativeSummaries() {
        listOf(
            "kobold-combatente-de-1-nivel", "kraken", "lammasu",
            "protetor-dourado-lammasu-meiodragao-celestial", "leao-marinho"
        ).forEach { id ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem resumo de combate", creature.combat.isNotBlank())
        }
        assertTrue(CreatureCatalog.byId("kobold-combatente-de-1-nivel")!!.combat.contains("armadilhas"))
        assertTrue(CreatureCatalog.byId("kraken")!!.combat.contains("84 m"))
        assertTrue(CreatureCatalog.byId("lammasu")!!.combat.contains("6d8"))
        assertTrue(CreatureCatalog.byId("protetor-dourado-lammasu-meiodragao-celestial")!!.combat.contains("destruir o mal"))
        assertTrue(CreatureCatalog.byId("leao-marinho")!!.combat.contains("2d6+6"))
    }


    @Test
    fun lichLillendGrayOozeSteppeWolfAndLocathahHaveNarrativeSummaries() {
        listOf("lich", "lillend", "limo-cinzento", "lobo-das-estepes", "locathah").forEach { id ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem resumo de combate", creature.combat.isNotBlank())
        }
        assertTrue(CreatureCatalog.byId("lich")!!.combat.contains("filactéria"))
        assertTrue(CreatureCatalog.byId("lillend")!!.combat.contains("Constrição"))
        assertTrue(CreatureCatalog.byId("limo-cinzento")!!.combat.contains("Transparência"))
        assertTrue(CreatureCatalog.byId("lobo-das-estepes")!!.combat.contains("4d6"))
        assertTrue(CreatureCatalog.byId("locathah")!!.combat.contains("virotes de bestas"))
    }


    @Test
    fun electricLizardMagminDarkmantleShadowMastiffAndMedusaHaveSummaries() {
        listOf(
            "lagarto-eletrico", "magmin", "manto-negro", "mastim-das-sombras", "medusa"
        ).forEach { id ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem resumo de combate", creature.combat.isNotBlank())
        }
        assertTrue(CreatureCatalog.byId("lagarto-eletrico")!!.combat.contains("choque letal"))
        assertTrue(CreatureCatalog.byId("magmin")!!.combat.contains("aura ígnea"))
        assertTrue(CreatureCatalog.byId("manto-negro")!!.combat.contains("Percepção às Cegas"))
        assertTrue(CreatureCatalog.byId("mastim-das-sombras")!!.combat.contains("Camuflagem de Sombras"))
        assertTrue(CreatureCatalog.byId("medusa")!!.combat.contains("Olhar Petrificante"))
    }


    @Test
    fun mephitsHaveNarrativeAndCombatSummaries() {
        listOf(
            "mephit-da-agua", "mephit-do-ar", "mephit-do-fogo", "mephit-do-gelo",
            "mephit-do-limo", "mephit-do-magma", "mephit-da-poeira", "mephit-do-sal",
            "mephit-do-vapor"
        ).forEach { id ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem resumo de combate", creature.combat.isNotBlank())
        }
        assertTrue(CreatureCatalog.byId("mephit-do-vapor")!!.combat.contains("água fervente"))
        assertTrue(CreatureCatalog.byId("mephit-do-gelo")!!.combat.contains("hipotermia"))
        assertTrue(CreatureCatalog.byId("mephit-da-agua")!!.combat.contains("Natação"))
    }


    @Test
    fun mephitEarthAndMimicHaveCompleteNarrativeSummaries() {
        listOf("mephit-da-terra", "mimico").forEach { id ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem resumo de combate", creature.combat.isNotBlank())
        }
        assertTrue(CreatureCatalog.byId("mephit-da-terra")!!.combat.contains("amolecer terra e pedra"))
        assertTrue(CreatureCatalog.byId("mephit-da-terra")!!.combat.contains("enterrado até a cintura"))
        assertTrue(CreatureCatalog.byId("mimico")!!.combat.contains("Agarrar"))
        assertTrue(CreatureCatalog.byId("mephit-do-fogo")!!.combat.contains("raio ardente"))
        assertFalse(CreatureCatalog.byId("mephit-da-agua")!!.combat.contains("bolha de ácido"))
    }


    @Test
    fun mohrgAndRustMonsterHaveNarrativeAndCombatSummaries() {
        listOf("mohrg", "monstro-da-ferrugem").forEach { id ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem resumo de combate", creature.combat.isNotBlank())
        }
        assertTrue(CreatureCatalog.byId("mohrg")!!.combat.contains("1d4 dias"))
        assertTrue(CreatureCatalog.byId("mohrg")!!.combat.contains("CD 17"))
        assertTrue(CreatureCatalog.byId("monstro-da-ferrugem")!!.combat.contains("Reflexos CD 17"))
        assertTrue(CreatureCatalog.byId("monstro-da-ferrugem")!!.combat.contains("27 m"))
    }


    @Test
    fun mummyHasBookAccurateNarrativeAndSpecialAttacks() {
        val mummy = CreatureCatalog.byId("mumia")!!
        assertTrue(mummy.description.contains("deuses sombrios do deserto"))
        assertTrue(mummy.combat.contains("Desespero"))
        assertTrue(mummy.combat.contains("1d6 pontos de dano temporário de Constituição"))
        assertTrue(mummy.combat.contains("CD 20"))
        assertTrue(mummy.specialQualities.contains("vulnerabilidade ao fogo"))
    }


    @Test
    fun allFourNagaEntriesHaveCompleteBookBasedSheets() {
        val ids = listOf("naga-aquatica", "naga-espiritual", "naga-guardia", "naga-negra")
        ids.forEach { id ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem combate", creature.combat.isNotBlank())
            assertEquals("Aberração", creature.type)
            assertEquals("Grande", creature.size)
        }
        assertTrue(CreatureCatalog.byId("naga-aquatica")!!.combat.contains("Natação"))
        assertTrue(CreatureCatalog.byId("naga-espiritual")!!.combat.contains("Olhar Encantador"))
        assertTrue(CreatureCatalog.byId("naga-guardia")!!.combat.contains("Saliva"))
        assertTrue(CreatureCatalog.byId("naga-negra")!!.combat.contains("Pensamentos Protegidos"))
    }


    @Test
    fun ogreMageHasBookBasedDescriptionAndCombat() {
        val ogre = CreatureCatalog.byId("ogro-mago")!!
        assertTrue(ogre.description.contains("3 m de altura"))
        assertTrue(ogre.description.contains("Gigante e Comum"))
        assertTrue(ogre.combat.contains("cone glacial"))
        assertTrue(ogre.combat.contains("forma gasosa"))
        assertTrue(ogre.combat.contains("Regeneração (Ext)"))
        assertTrue(ogre.combat.contains("fogo e ácido"))
    }


    @Test
    fun rocHasBookBasedDescriptionAndHuntingBehavior() {
        val roc = CreatureCatalog.byId("passaro-roca")!!
        assertTrue(roc.description.contains("envergadura próxima de 25 m"))
        assertTrue(roc.description.contains("raio de aproximadamente 15 km"))
        assertTrue(roc.combat.contains("criatura Média ou maior"))
        assertTrue(roc.combat.contains("até a morte"))
        assertTrue(roc.combat.contains("+4 de bônus racial"))
    }


    @Test
    fun pegasusHasBookBasedDescriptionAndCombatRules() {
        val pegasus = CreatureCatalog.byId("pegaso")!!
        assertTrue(pegasus.description.contains("duas grandes asas"))
        assertTrue(pegasus.description.contains("difícil de domesticar"))
        assertTrue(pegasus.combat.contains("detectar o bem e detectar o mal"))
        assertTrue(pegasus.combat.contains("nível de conjurador 5º"))
        assertTrue(pegasus.combat.contains("Adestrar Animais (CD 25)"))
        assertTrue(pegasus.combat.contains("sela exótica"))
        assertTrue(pegasus.combat.contains("Ouvir e Observar"))
        assertEquals("3", pegasus.challengeRating)
    }


    @Test
    fun pseudodragonHasBookBasedDescriptionAndCombatAbilities() {
        val pseudodragon = CreatureCatalog.byId("pseudodragao")!!
        assertTrue(pseudodragon.description.contains("30 cm"))
        assertTrue(pseudodragon.description.contains("personalidade é quase felina"))
        assertTrue(pseudodragon.description.contains("não serve a mestres malignos"))
        assertTrue(pseudodragon.combat.contains("Fortitude CD 14"))
        assertTrue(pseudodragon.combat.contains("1d3 horas"))
        assertTrue(pseudodragon.combat.contains("Sentido Cego"))
        assertTrue(pseudodragon.combat.contains("Comum ou Silvestre"))
        assertTrue(pseudodragon.combat.contains("+8 em florestas"))
        assertEquals("1", pseudodragon.challengeRating)
    }

}
