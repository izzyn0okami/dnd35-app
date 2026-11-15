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
    fun rastHasBookBasedDescriptionAndCombatAbilities() {
        val rast = CreatureCatalog.byId("rast")!!
        assertTrue(rast.description.contains("dez a quinze garras"))
        assertTrue(rast.description.contains("100 kg"))
        assertTrue(rast.combat.contains("Olhar Paralisante"))
        assertTrue(rast.combat.contains("Fortitude CD 13"))
        assertTrue(rast.combat.contains("1d6 rodadas"))
        assertTrue(rast.combat.contains("1 ponto de dano temporário de Constituição"))
        assertTrue(rast.combat.contains("vulnerável a frio"))
        assertEquals("10,5 m (7 quadrados), voo 18 m (bom)", rast.speed)
        assertFalse(rast.specialQualities.contains("Rakshasa"))
        assertEquals("5", rast.challengeRating)
    }



    @Test
    fun ravidHasPositiveEnergyBurstAndAnimatesNearbyObjects() {
        val ravid = CreatureCatalog.byId("ravid")!!
        assertTrue(ravid.description.contains("Plano da Energia Positiva"))
        assertTrue(ravid.description.contains("2 m"))
        assertTrue(ravid.combat.contains("2d10 pontos de dano"))
        assertTrue(ravid.combat.contains("inclusive incorpóreos"))
        assertTrue(ravid.combat.contains("a até 6 m"))
        assertTrue(ravid.combat.contains("nível de conjurador 20º"))
        assertTrue(ravid.combat.contains("Ataques Múltiplos"))
        assertEquals("5", ravid.challengeRating)
    }
    @Test
    fun sahuaginEtherealRaidersSatyrAndSkumHaveBookBasedDetails() {
        val sahuagin = CreatureCatalog.byId("sahuagin")!!
        assertTrue(sahuagin.description.contains("demônios do mar"))
        assertTrue(sahuagin.combat.contains("Frenesi de Sangue"))
        assertTrue(sahuagin.combat.contains("50 m"))
        assertFalse(sahuagin.organization.contains("Robgoblin"))
        
        val salteador = CreatureCatalog.byId("salteador-etereo")!!
        assertTrue(salteador.description.contains("quatro braços"))
        assertTrue(salteador.combat.contains("Detectar Magia"))
        assertTrue(salteador.combat.contains("Prestidigitação"))
        
        val saqueador = CreatureCatalog.byId("saqueador-etereo")!!
        assertEquals("Besta Mágica", saqueador.type)
        assertEquals("2d10", saqueador.hitDice)
        assertTrue(saqueador.description.contains("três mandíbulas"))
        assertTrue(saqueador.combat.contains("nível de conjurador 15º"))
        
        val satiro = CreatureCatalog.byId("satiro")!!
        assertTrue(satiro.description.contains("fauno"))
        assertEquals("Flautas", satiro.specialAttacks)
        assertTrue(satiro.combat.contains("Vontade CD 13"))
        assertTrue(satiro.combat.contains("Nível de Desafio é 2 sem flautas e 4 com flautas"))
        
        val skum = CreatureCatalog.byId("skum")!!
        assertTrue(skum.description.contains("criadas pelos aboletes"))
        assertTrue(skum.combat.contains("1d6+2"))
        assertFalse(skum.fullAttack.contains("Sátiro"))
    }

    
    @Test
    fun allSlaadVariantsHaveBookBasedNarrativeAndCombatDetails() {
        val ids = listOf(
            "slaad-azul", "slaad-cinza", "slaad-da-morte",
            "slaad-verde", "slaad-vermelho"
        )
        ids.forEach { id ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem resumo de combate", creature.combat.isNotBlank())
            assertTrue("$id sem origem planar", creature.environment.contains("Limbo"))
            assertTrue("$id sem invocação", creature.combat.contains("Invocar Slaad"))
        }
        val azul = CreatureCatalog.byId("slaad-azul")!!
        assertTrue(azul.combat.contains("Febre Slaad"))
        assertTrue(azul.combat.contains("CD 18"))
        assertEquals("8", azul.challengeRating)
        assertEquals("Caos Eternamente Mutável do Limbo", azul.environment)

        val cinza = CreatureCatalog.byId("slaad-cinza")!!
        assertTrue(cinza.combat.contains("Mudar Forma"))
        assertTrue(cinza.combat.contains("palavra de poder: atordoar"))
        assertEquals("10", cinza.challengeRating)

        val morte = CreatureCatalog.byId("slaad-da-morte")!!
        assertTrue(morte.combat.contains("Atordoar"))
        assertTrue(morte.combat.contains("implosão"))
        assertTrue(morte.combat.contains("telepatia 30 m"))
        assertEquals("13", morte.challengeRating)

        val verde = CreatureCatalog.byId("slaad-verde")!!
        assertTrue(verde.combat.contains("detectar pensamentos"))
        assertTrue(verde.combat.contains("Mudar Forma"))
        assertEquals("9", verde.challengeRating)

        val vermelho = CreatureCatalog.byId("slaad-vermelho")!!
        assertTrue(vermelho.combat.contains("Implante"))
        assertTrue(vermelho.combat.contains("Fortitude CD 16"))
        assertTrue(vermelho.combat.contains("Coaxar Atordoante"))
        assertEquals("7", vermelho.challengeRating)
    }


    @Test
    fun eightCreatureSheetsHaveNarrativeAndCombatDetails() {
        val expected = mapOf(
            "stirge" to listOf("Adesão", "Drenar Sangue", "1d4"),
            "tarrasque" to listOf("Presença Aterradora", "carapaça", "868 PV"),
            "tartarugadragao" to listOf("Sopro", "Naufragar", "12d6"),
            "tendriculo" to listOf("Agarrar Aprimorado", "Regeneração", "Fortitude CD 20"),
            "thoqqua" to listOf("Calor", "Incendiar", "2d6"),
            "tita" to listOf("Arma Gigante", "chuva de meteoros", "20º"),
            "tritao" to listOf("invocar aliado da natureza IV", "elementais da água"),
            "troglodita" to listOf("Mau-Cheiro", "Fortitude CD 13", "9 m")
        )
        expected.forEach { (id, fragments) ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem combate", creature.combat.isNotBlank())
            fragments.forEach { fragment ->
                assertTrue("$id não contém $fragment", creature.combat.contains(fragment))
            }
        }
        assertEquals("20", CreatureCatalog.byId("tarrasque")!!.challengeRating)
        assertEquals("6", CreatureCatalog.byId("tendriculo")!!.challengeRating)
        assertEquals("9", CreatureCatalog.byId("tartarugadragao")!!.challengeRating)
        assertEquals("1/2", CreatureCatalog.byId("stirge")!!.challengeRating)
    }


    @Test
    fun threeMoreCreatureSheetsHaveBookBasedNarrativeAndCombat() {
        val expected = mapOf(
            "uivante" to listOf("Espinhos", "Reflexos CD 16", "Vontade CD 12", "Adestrar Animais CD 25"),
            "unicornio" to listOf("Círculo Mágico Contra o Mal", "teletransporte maior", "neutralizar veneno (CD 21,", "+6 de bônus racial"),
            "ursocoruja" to listOf("Agarrar Aprimorado", "ação livre", "sempre lutam até a morte")
        )
        expected.forEach { (id, fragments) ->
            val creature = CreatureCatalog.byId(id)!!
            assertTrue("$id sem descrição", creature.description.isNotBlank())
            assertTrue("$id sem combate", creature.combat.isNotBlank())
            fragments.forEach { fragment ->
                assertTrue("$id não contém $fragment", creature.combat.contains(fragment))
            }
        }
        assertTrue(CreatureCatalog.byId("uivante")!!.description.contains("Pandemônio"))
        assertTrue(CreatureCatalog.byId("unicornio")!!.description.contains("2.000 PO"))
        assertTrue(CreatureCatalog.byId("ursocoruja")!!.description.contains("1d6 filhotes"))
        assertTrue(CreatureCatalog.byId("ursocoruja")!!.description.contains("CD 30"))
    }

    
    @Test
    fun fiveVMonsterSheetsHaveBookBasedNarrativeAndCombat() {
        val vargouille = CreatureCatalog.byId("vargouille")!!
        assertTrue(vargouille.description.contains("Carceri"))
        assertTrue(vargouille.combat.contains("Silvo (Sob)"))
        assertTrue(vargouille.combat.contains("Fortitude CD 15"))

        val verme = CreatureCatalog.byId("verme-purpura")!!
        assertTrue(verme.description.contains("20 toneladas"))
        assertTrue(verme.combat.contains("Engolir (Ext)"))
        assertTrue(verme.combat.contains("Fortitude CD 25"))

        val vinha = CreatureCatalog.byId("vinha-assassina")!!
        assertTrue(vinha.description.contains("vinho tóxico"))
        assertTrue(vinha.combat.contains("Percepção às Cegas"))
        assertTrue(vinha.combat.contains("Observar CD 20"))

        val rastejante = CreatureCatalog.byId("rastejante-noturno")!!
        assertTrue(rastejante.description.contains("30 m de comprimento"))
        assertTrue(rastejante.combat.contains("Drenar Energia"))
        assertTrue(rastejante.combat.contains("Fortitude CD 22"))

        val vulto = CreatureCatalog.byId("vulto-alado")!!
        assertTrue(vulto.description.contains("12 m"))
        assertTrue(vulto.combat.contains("Drenar Magia"))
        assertTrue(vulto.combat.contains("nível de conjurador 17º"))
    }
    
    @Test
    fun iceWormHasBookBasedDescriptionAndCombatAbilities() {
        val worm = CreatureCatalog.byId("verme-do-gelo")!!
        assertTrue(worm.description.contains("branco-azulada"))
        assertTrue(worm.description.contains("4 toneladas"))
        assertTrue(worm.combat.contains("Trinado (Sob)"))
        assertTrue(worm.combat.contains("Vontade CD 17"))
        assertTrue(worm.combat.contains("15d6"))
        assertTrue(worm.combat.contains("Espasmos de Morte"))
        assertTrue(worm.combat.contains("8d6"))
    }
    
    @Test
    fun worgWyvernAndYrthakHaveCompleteBookBasedNarrativeAndCombat() {
        val worg = CreatureCatalog.byId("worg")!!
        assertTrue(worg.description.contains("150 kg"))
        assertTrue(worg.combat.contains("Imobilização (Ext)"))
        assertTrue(worg.combat.contains("+4 racial em Sobrevivência"))

        val wyvern = CreatureCatalog.byId("wyvern")!!
        assertTrue(wyvern.description.contains("6 m"))
        assertTrue(wyvern.combat.contains("Fortitude CD 17"))
        assertTrue(wyvern.combat.contains("2d6 pontos de dano temporário de Constituição"))

        val yrthak = CreatureCatalog.byId("yrthak")!!
        assertTrue(yrthak.description.contains("12 m de envergadura"))
        assertTrue(yrthak.combat.contains("Rajada Sônica (Sob)"))
        assertTrue(yrthak.combat.contains("Percepção às Cegas (Ext)"))
        assertTrue(yrthak.combat.contains("36 m"))
    }

    
    @Test
    fun elevenAnimalSheetsHaveBookBasedDescriptionAndCombat() {
        val eagle = CreatureCatalog.byId("aguia")!!
        assertTrue(eagle.description.contains("2,1 m"))
        assertTrue(eagle.combat.contains("+8 de bônus racial em Observar"))

        val ermine = CreatureCatalog.byId("arminho")!!
        assertTrue(ermine.combat.contains("Adesão (Ext)"))
        assertTrue(ermine.combat.contains("será reduzida para CA 12"))

        val ray = CreatureCatalog.byId("arraia")!!
        assertTrue(ray.description.contains("plâncton"))
        assertTrue(ray.combat.contains("+8 de bônus racial em Natação"))

        val baboon = CreatureCatalog.byId("babuino")!!
        assertTrue(baboon.description.contains("45 kg"))
        assertTrue(baboon.combat.contains("+8 de bônus racial em Escalar"))

        val blueWhale = CreatureCatalog.byId("baleia-azul")!!
        assertTrue(blueWhale.combat.contains("Percepção às Cegas"))
        assertTrue(blueWhale.combat.contains("36 m"))

        val spermWhale = CreatureCatalog.byId("baleia-cachalote")!!
        assertTrue(spermWhale.description.contains("13 m"))
        assertTrue(spermWhale.combat.contains("Prender a Respiração"))

        val orca = CreatureCatalog.byId("baleia-orca")!!
        assertTrue(orca.description.contains("outras baleias"))
        assertTrue(orca.combat.contains("36 m"))

        val bison = CreatureCatalog.byId("bisao")!!
        assertTrue(bison.description.contains("1,2 tonelada"))
        assertTrue(bison.combat.contains("1d12"))

        val dog = CreatureCatalog.byId("cachorro")!!
        assertTrue(dog.description.contains("10 e 25 kg"))
        assertTrue(dog.combat.contains("Sobrevivência"))

        val ridingDog = CreatureCatalog.byId("cachorro-de-montaria")!!
        assertTrue(ridingDog.combat.contains("750 kg"))
        assertTrue(ridingDog.combat.contains("Imobilização"))

        val camel = CreatureCatalog.byId("camelo")!!
        assertTrue(camel.description.contains("dromedário"))
        assertTrue(camel.combat.contains("2.250 kg"))
    }

    
    @Test
    fun tenAnimalSheetsHaveBookBasedDescriptionAndCombat() {
        val carcaju = CreatureCatalog.byId("carcaju")!!
        assertTrue(carcaju.description.contains("florestas frias"))
        assertTrue(carcaju.combat.contains("Fúria (Ext)"))
        assertTrue(carcaju.combat.contains("+4 de Força"))

        val heavyHorse = CreatureCatalog.byId("cavalo-pesado")!!
        assertTrue(heavyHorse.description.contains("Clydesdales"))
        assertTrue(heavyHorse.combat.contains("1.500 kg"))

        val lightHorse = CreatureCatalog.byId("cavalo-leve")!!
        assertTrue(lightHorse.description.contains("pangarés"))
        assertTrue(lightHorse.combat.contains("1.125 kg"))

        val heavyWarHorse = CreatureCatalog.byId("cavalo-de-guerra-pesado")!!
        assertTrue(heavyWarHorse.description.contains("treinado"))
        assertTrue(heavyWarHorse.combat.contains("teste de Cavalgar"))
        assertTrue(heavyWarHorse.combat.contains("2.250 kg"))

        val lightWarHorse = CreatureCatalog.byId("cavalo-de-guerra-leve")!!
        assertTrue(lightWarHorse.description.contains("agressividade"))
        assertTrue(lightWarHorse.combat.contains("1.725 kg"))

        val constrictor = CreatureCatalog.byId("cobra-constritora")!!
        assertTrue(constrictor.description.contains("jiboia"))
        assertTrue(constrictor.combat.contains("Constrição (Ext)"))
        assertTrue(constrictor.combat.contains("1d3+4"))

        val giantConstrictor = CreatureCatalog.byId("cobra-constritora-gigante")!!
        assertTrue(giantConstrictor.combat.contains("1d8+10"))
        assertTrue(giantConstrictor.combat.contains("Agarrar Aprimorado (Ext)"))

        val crocodile = CreatureCatalog.byId("crocodilo")!!
        assertTrue(crocodile.description.contains("3 m a 3,5 m"))
        assertTrue(crocodile.combat.contains("4 vezes seu valor de Constituição"))
        assertTrue(crocodile.combat.contains("+10 de bônus de cobertura"))

        val giantCrocodile = CreatureCatalog.byId("crocodilo-gigante")!!
        assertTrue(giantCrocodile.description.contains("6 m"))
        assertTrue(giantCrocodile.combat.contains("como os crocodilos comuns"))

        val elephant = CreatureCatalog.byId("elefante")!!
        assertTrue(elephant.description.contains("mamutes e mastodontes"))
        assertTrue(elephant.combat.contains("Reflexos CD 25"))
        assertTrue(elephant.combat.contains("2d8+15"))
    }

    
    @Test
    fun tenMoreAnimalSheetsHaveBookBasedDescriptionAndCombat() {
        val owl = CreatureCatalog.byId("coruja")!!
        assertTrue(owl.description.contains("30 cm a 60 cm"))
        assertTrue(owl.combat.contains("+14 em Furtividade"))

        val raven = CreatureCatalog.byId("corvo")!!
        assertTrue(raven.description.contains("1,2 m"))
        assertTrue(raven.combat.contains("duas garras"))

        val falcon = CreatureCatalog.byId("falcao")!!
        assertTrue(falcon.description.contains("40 cm e 60 cm"))
        assertTrue(falcon.combat.contains("+8 de bônus racial em Observar"))

        val cat = CreatureCatalog.byId("gato")!!
        assertTrue(cat.combat.contains("surpreender suas presas"))
        assertTrue(cat.combat.contains("+8 em Saltar e Equilíbrio"))

        val dolphin = CreatureCatalog.byId("golfinho")!!
        assertTrue(dolphin.description.contains("55 kg e 80 kg"))
        assertTrue(dolphin.combat.contains("6 vezes seu valor de Constituição"))
        assertTrue(dolphin.combat.contains("36 m"))

        val gorilla = CreatureCatalog.byId("gorila")!!
        assertTrue(gorilla.description.contains("150 kg e 200 kg"))
        assertTrue(gorilla.combat.contains("+8 de bônus racial em Escalar"))

        val cheetah = CreatureCatalog.byId("guepardo")!!
        assertTrue(cheetah.combat.contains("Impulso (Ext)"))
        assertTrue(cheetah.combat.contains("150 m"))

        val hyena = CreatureCatalog.byId("hiena")!!
        assertTrue(hyena.description.contains("hiena-listrada"))
        assertTrue(hyena.combat.contains("Imobilização (Ext)"))
        assertTrue(hyena.combat.contains("+2 no teste"))

        val boar = CreatureCatalog.byId("javali")!!
        assertTrue(boar.description.contains("pelos espessos e acinzentados"))
        assertTrue(boar.combat.contains("Ferocidade (Ext)"))

        val donkey = CreatureCatalog.byId("jumento")!!
        assertTrue(donkey.description.contains("orelhas longas"))
        assertTrue(donkey.combat.contains("375 kg"))
    }


    @Test
    fun tenMonsterSheetsHaveExternalSrdBasedDescriptionAndCombat() {
        val pureblood = CreatureCatalog.byId("puro-sangue")!!
        assertTrue(pureblood.description.contains("espiões"))
        assertTrue(pureblood.combat.contains("Alterar Forma"))
        assertTrue(pureblood.combat.contains("Resistência à Magia 14"))

        val lizard = CreatureCatalog.byId("lagarto")!!
        assertTrue(lizard.description.contains("iguanas"))
        assertTrue(lizard.combat.contains("+8 de bônus racial em Equilíbrio"))

        val lion = CreatureCatalog.byId("leao")!!
        assertTrue(lion.combat.contains("Bote (Ext)"))
        assertTrue(lion.combat.contains("rasgar"))

        val leopard = CreatureCatalog.byId("leopardo")!!
        assertTrue(leopard.description.contains("onças"))
        assertTrue(leopard.combat.contains("Agarrar Aprimorado (Ext)"))

        val wolf = CreatureCatalog.byId("lobo")!!
        assertTrue(wolf.combat.contains("flancos"))
        assertTrue(wolf.combat.contains("Imobilização (Ext)"))

        val squid = CreatureCatalog.byId("lula")!!
        assertTrue(squid.combat.contains("Nuvem de Tinta (Ext)"))
        assertTrue(squid.combat.contains("72 m"))

        val giantSquid = CreatureCatalog.byId("lula-gigante")!!
        assertTrue(giantSquid.combat.contains("1d6+8"))
        assertTrue(giantSquid.combat.contains("96 m"))

        val monkey = CreatureCatalog.byId("macaco")!!
        assertTrue(monkey.combat.contains("+8 racial em Equilíbrio"))

        val bat = CreatureCatalog.byId("morcego")!!
        assertTrue(bat.combat.contains("Sentido Cego (Ext)"))
        assertTrue(bat.combat.contains("6 m"))

        val mule = CreatureCatalog.byId("mula")!!
        assertTrue(mule.description.contains("cruzamento estéril"))
        assertTrue(mule.combat.contains("1.575 kg"))
    }


    @Test
    fun tenMoreAnimalSheetsHaveExternalSrdBasedDescriptionAndCombat() {
        val octopus = CreatureCatalog.byId("polvo")!!
        assertTrue(octopus.description.contains("fundo"))
        assertTrue(octopus.combat.contains("Nuvem de Tinta (Ext)"))
        assertTrue(octopus.combat.contains("60 m"))

        val giantOctopus = CreatureCatalog.byId("polvo-gigante")!!
        assertTrue(giantOctopus.description.contains("ventosas"))
        assertTrue(giantOctopus.combat.contains("2d8+6"))
        assertTrue(giantOctopus.combat.contains("quatro tentáculos"))

        val pony = CreatureCatalog.byId("ponei")!!
        assertTrue(pony.description.contains("cavalo pequeno"))
        assertTrue(pony.combat.contains("510 kg"))

        val warPony = CreatureCatalog.byId("ponei-de-guerra")!!
        assertTrue(warPony.description.contains("agressividade"))
        assertTrue(warPony.combat.contains("teste de Cavalgar"))

        val rat = CreatureCatalog.byId("rato")!!
        assertTrue(rat.description.contains("onívoros"))
        assertTrue(rat.combat.contains("+8 em Equilíbrio"))

        val rhino = CreatureCatalog.byId("rinoceronte")!!
        assertTrue(rhino.combat.contains("Investida Poderosa (Ext)"))
        assertTrue(rhino.combat.contains("4d6+24"))

        val toad = CreatureCatalog.byId("sapo")!!
        assertTrue(toad.description.contains("inofensivos"))
        assertTrue(toad.combat.contains("+4 de bônus racial em Esconder-se"))

        val badger = CreatureCatalog.byId("texugo")!!
        assertTrue(badger.combat.contains("Fúria (Ext)"))
        assertTrue(badger.combat.contains("+4 em Força e Constituição"))

        val tiger = CreatureCatalog.byId("tigre")!!
        assertTrue(tiger.combat.contains("Bote (Ext)"))
        assertTrue(tiger.combat.contains("1d8+3"))

        val brownBear = CreatureCatalog.byId("urso-marrom")!!
        assertTrue(brownBear.description.contains("urso-cinzento"))
        assertTrue(brownBear.combat.contains("Agarrar Aprimorado (Ext)"))
    }


    @Test
    fun tenMoreMonsterSheetsHaveExternalSrdDetails() {
        val blackBear = CreatureCatalog.byId("urso-negro")!!
        assertTrue(blackBear.description.contains("onívoro"))
        assertTrue(blackBear.combat.contains("+4 de bônus racial em testes de Natação"))

        val polarBear = CreatureCatalog.byId("urso-polar")!!
        assertTrue(polarBear.combat.contains("+12 racial em Esconder-se"))
        assertTrue(polarBear.combat.contains("Agarrar Aprimorado (Ext)"))

        val fireBeetle = CreatureCatalog.byId("vaga-lume-gigante")!!
        assertTrue(fireBeetle.description.contains("glândulas"))
        assertTrue(fireBeetle.description.contains("1d6 dias"))

        val wasp = CreatureCatalog.byId("vespa-gigante")!!
        assertTrue(wasp.combat.contains("Fortitude CD 14"))
        assertTrue(wasp.combat.contains("1d6 pontos de dano temporário de Destreza"))

        val monitor = CreatureCatalog.byId("lagarto-monitor")!!
        assertTrue(monitor.description.contains("carnívoras"))
        assertTrue(monitor.combat.contains("+8 racial em Natação"))

        for (id in listOf("tubarao-medio", "tubarao-grande", "tubarao-enorme")) {
            val shark = CreatureCatalog.byId(id)!!
            assertTrue(shark.combat.contains("Sentido Cego (Ext)"))
            assertTrue(shark.combat.contains("1,6 km"))
        }

        for (id in listOf("vibora-miuda", "vibora-pequena")) {
            val viper = CreatureCatalog.byId(id)!!
            assertTrue(viper.combat.contains("Fortitude CD 10"))
            assertTrue(viper.combat.contains("1d6 pontos de dano temporário de Constituição"))
        }
    }


    @Test
    fun tenViperAndScorpionSheetsHaveExternalSrdDetails() {
        val viperDcs = mapOf("vibora-media" to "Fortitude CD 11", "vibora-grande" to "Fortitude CD 11", "vibora-enorme" to "Fortitude CD 14")
        for ((id, dc) in viperDcs) {
            val viper = CreatureCatalog.byId(id)!!
            assertTrue(viper.description.contains("víboras"))
            assertTrue(viper.combat.contains(dc))
            assertTrue(viper.combat.contains("1d6 pontos de dano temporário de Constituição"))
        }

        val scorpions = mapOf(
            "escorpiao-miudo" to ("1d2–4" to "Fortitude CD 12"),
            "escorpiao-pequeno" to ("1d3–1" to "Fortitude CD 12"),
            "escorpiao-medio" to ("1d4+1" to "Fortitude CD 13"),
            "escorpiao-grande" to ("1d6+4" to "Fortitude CD 14"),
            "escorpiao-enorme" to ("1d8+6" to "Fortitude CD 18"),
            "escorpiao-imenso" to ("2d6+10" to "Fortitude CD 23"),
            "escorpiao-colossal" to ("2d8+12" to "Fortitude CD 33")
        )
        for ((id, values) in scorpions) {
            val scorpion = CreatureCatalog.byId(id)!!
            assertTrue(scorpion.description.contains("desérticas"))
            assertTrue(scorpion.combat.contains("Constrição (Ext)"))
            assertTrue(scorpion.combat.contains(values.first))
            assertTrue(scorpion.combat.contains(values.second))
            assertTrue(scorpion.combat.contains("Sentido Sísmico de 18 m"))
        }
    }


    @Test
    fun fourteenSpiderAndCentipedeSheetsHaveExternalSrdDetails() {
        val spiders = mapOf(
            "aranha-miuda" to ("Fortitude CD 10" to "1d2 pontos de dano temporário de Força"),
            "aranha-pequena" to ("Fortitude CD 10" to "1d3 pontos de dano temporário de Força"),
            "aranha-media" to ("Fortitude CD 12" to "1d4 pontos de dano temporário de Força"),
            "aranha-grande" to ("Fortitude CD 13" to "1d6 pontos de dano temporário de Força"),
            "aranha-enorme" to ("Fortitude CD 16" to "1d8 pontos de dano temporário de Força"),
            "aranha-imensa" to ("Fortitude CD 20" to "2d6 pontos de dano temporário de Força"),
            "aranha-colossal" to ("Fortitude CD 28" to "2d8 pontos de dano temporário de Força")
        )
        for ((id, values) in spiders) {
            val spider = CreatureCatalog.byId(id)!!
            assertTrue(spider.description.contains("predadoras agressivas"))
            assertTrue(spider.combat.contains(values.first))
            assertTrue(spider.combat.contains(values.second))
            assertTrue(spider.combat.contains("Sentido Sísmico (Ext)"))
            assertTrue(spider.combat.contains("Arte da Fuga"))
        }

        val centipedes = mapOf(
            "centopeia-miuda" to ("Fortitude CD 10" to "1 ponto de dano temporário de Destreza"),
            "centopeia-pequena" to ("Fortitude CD 10" to "1d2 pontos de dano temporário de Destreza"),
            "centopeia-media" to ("Fortitude CD 10" to "1d3 pontos de dano temporário de Destreza"),
            "centopeia-grande" to ("Fortitude CD 11" to "1d4 pontos de dano temporário de Destreza"),
            "centopeia-enorme" to ("Fortitude CD 14" to "1d6 pontos de dano temporário de Destreza"),
            "centopeia-imensa" to ("Fortitude CD 17" to "1d8 pontos de dano temporário de Destreza"),
            "centopeia-colossal" to ("Fortitude CD 23" to "2d6 pontos de dano temporário de Destreza")
        )
        for ((id, values) in centipedes) {
            val centipede = CreatureCatalog.byId(id)!!
            assertTrue(centipede.description.contains("subterrâneo"))
            assertTrue(centipede.combat.contains(values.first))
            assertTrue(centipede.combat.contains(values.second))
            assertTrue(centipede.combat.contains("+8 em Escalar e Esconder-se"))
        }
    }

}
