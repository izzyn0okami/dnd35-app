package com.example

import com.example.dnd35.model.Dnd35Rules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PresetSpellCatalogTest {
    @Test
    fun catalogIncludesMissingFirstLevelBardSpells() {
        val spells = Dnd35Rules.PRESET_SPELLS
        val expectedClasses = setOf("Bardo", "Mago", "Feiticeiro")
        listOf("Apagar", "Aura Mágica de Nystul", "Imagem Silenciosa", "Ventriloquismo").forEach { name ->
            val spell = spells.single { it.name == name }
            assertEquals(1, spell.level)
            assertEquals(expectedClasses, spell.classes.toSet())
            assertEquals(1, spell.classLevels["Bardo"])
            assertEquals(1, spell.classLevels["Mago"])
            assertEquals(1, spell.classLevels["Feiticeiro"])
        }
        assertEquals("V, G", spells.single { it.name == "Apagar" }.components)
        assertTrue(spells.single { it.name == "Imagem Silenciosa" }.components.startsWith("V, G, F"))
        assertTrue(spells.single { it.name == "Ventriloquismo" }.components.startsWith("V, F"))
        assertFalse(spells.single { it.name == "Ventriloquismo" }.spellResistance)
    }

    @Test
    fun cantripCatalogUsesCanonicalPlayerHandbookNames() {
        val spells = Dnd35Rules.PRESET_SPELLS
        listOf("Atordoar", "Espirro Ácido", "Desestabilizar Mortos-Vivos", "Lampejo",
            "Toque de Fadiga", "Purificar Alimentos e Bebidas").forEach { alias ->
            assertFalse("Nome alternativo não deve duplicar a magia: $alias", spells.any { it.name == alias })
        }
        listOf("Pasmar", "Raio de Ácido", "Romper Morto-Vivo", "Brilho",
            "Toque da Fadiga", "Purificar Alimentos").forEach { canonical ->
            assertEquals("Deve haver uma única entrada para $canonical", 1, spells.count { it.name == canonical })
        }
        assertEquals("V, G, M", spells.single { it.name == "Toque da Fadiga" }.components)
        assertEquals("V, G", spells.single { it.name == "Purificar Alimentos" }.components)
    }

    @Test
    fun directionSpellUsesPlayerHandbookName() {
        val spells = Dnd35Rules.PRESET_SPELLS
        val spell = spells.single { it.name == "Intuir Direção" }
        assertFalse(spells.any { it.name == "Conhecer Direção" })
        assertEquals(setOf("Bardo", "Druida"), spell.classes.toSet())
        assertEquals("V, G", spell.components)
        assertEquals("Pessoal", spell.range)
    }

    @Test
    fun guidanceUsesPlayerHandbookNameAndDetails() {
        val spells = Dnd35Rules.PRESET_SPELLS
        val guidance = spells.single { it.name == "Orientação" }
        assertFalse(spells.any { it.name == "Orientação Divina" })
        assertEquals(0, guidance.level)
        assertEquals(setOf("Clérigo", "Druida"), guidance.classes.toSet())
        assertEquals("V, G", guidance.components)
        assertEquals("Toque", guidance.range)
        assertEquals("1 min ou até descarregar", guidance.duration)
        assertEquals("Vontade anula (inofensiva)", guidance.savingThrow)
        assertTrue(guidance.spellResistance)
    }

    @Test
    fun catalogIncludesAdditionalPlayerHandbookCantrips() {
        val spells = Dnd35Rules.PRESET_SPELLS
        val names = spells.map { it.name }
        listOf("Brilho", "Pasmar", "Raio de Ácido", "Romper Morto-Vivo").forEach { name ->
            assertEquals("Deve existir uma única entrada para $name", 1, spells.count { it.name == name })
            assertEquals(0, spells.first { it.name == name }.level)
        }
        assertEquals(setOf("Bardo", "Druida", "Mago", "Feiticeiro"), spells.first { it.name == "Brilho" }.classes.toSet())
        assertEquals("V", spells.first { it.name == "Brilho" }.components)
        assertEquals(setOf("Bardo", "Mago", "Feiticeiro"), spells.first { it.name == "Pasmar" }.classes.toSet())
        assertEquals("V, G, M", spells.first { it.name == "Pasmar" }.components)
        assertEquals("Nenhum", spells.first { it.name == "Raio de Ácido" }.savingThrow)
        assertFalse(spells.first { it.name == "Raio de Ácido" }.spellResistance)
        assertTrue(spells.first { it.name == "Romper Morto-Vivo" }.spellResistance)
        listOf(
            "Criar Água",
            "Detectar Venenos",
            "Consertar",
            "Mensagem",
            "Abrir/Fechar",
            "Prestidigitação",
            "Marca Arcana",
            "Raio de Gelo",
            "Intuir Direção",
            "Canção de Ninar",
            "Invocar Instrumento",
            "Curar Ferimentos Mínimos",
            "Infligir Ferimentos Mínimos",
            "Virtude",
            "Alarme",
            "Causar Medo",
            "Compreender Idiomas",
            "Enfeitiçar Pessoa",
            "Área Escorregadia",
            "Detectar Portas Secretas",
            "Proteção contra o Mal",
            "Detectar Mortos-Vivos",
            "Disfarçar-se",
            "Servo Invisível",
            "Invocar Criaturas I",
            "Identificação",
            "Raio do Enfraquecimento",
            "Aumentar Pessoa",
            "Arma Mágica",
            "Salto",
            "Mãos Flamejantes",
            "Enfeitiçar Animal",
            "Constrição",
            "Bom Fruto",
            "Passos sem Pegadas",
            "Falar com Animais",
            "Produzir Chama",
            "Raio Colorido",
            "Comando",
            "Santuário",
            "Escudo da Fé",
            "Presa Mágica",
            "Passos Longos",
            "Fogo das Fadas",
            "Remover Medo",
            "Maldição Menor",
            "Abençoar Água",
            "Desespero",
            "Suportar Elementos",
            "Porrete Mágico",
            "Detectar Animais ou Plantas",
            "Ocultar-se de Animais",
            "Amaldiçoar Água",
            "Escudo Entrópico",
            "Ocultar-se de Mortos-Vivos",
            "Vigília da Morte",
            "Detectar Bem",
            "Detectar Mal",
            "Detectar Caos",
            "Detectar Ordem"
        ).forEach { name ->
            assertTrue("Magia ausente: $name", name in names)
        }
        assertEquals("O catálogo não deve conter magias com nomes duplicados", names.size, names.toSet().size)
    }

    @Test
    fun newCantripsHaveExpectedClassAvailability() {
        val spells = Dnd35Rules.PRESET_SPELLS.associateBy { it.name }
        assertEquals(setOf("Mago", "Feiticeiro"), spells.getValue("Raio de Ácido").classes.toSet())
        assertFalse(spells.getValue("Raio de Ácido").spellResistance)
        assertEquals(setOf("Bardo"), spells.getValue("Canção de Ninar").classes.toSet())
        assertEquals(setOf("Clérigo", "Druida"), spells.getValue("Curar Ferimentos Mínimos").classes.toSet())
        assertEquals(0, spells.getValue("Virtude").classLevels.getValue("Druida"))
        assertEquals("Permanente", spells.getValue("Marca Arcana").duration)
        assertEquals(setOf("Bardo", "Ranger", "Mago", "Feiticeiro"), spells.getValue("Alarme").classes.toSet())
        assertEquals(setOf("Bardo", "Clérigo", "Mago", "Feiticeiro"), spells.getValue("Causar Medo").classes.toSet())
        assertEquals(setOf("Clérigo", "Paladino", "Mago", "Feiticeiro"), spells.getValue("Proteção contra o Mal").classes.toSet())
        assertEquals(1, spells.getValue("Proteção contra o Mal").classLevels.getValue("Paladino"))
        assertTrue(spells.getValue("Proteção contra o Mal").spellResistance)
    }

    @Test
    fun firstLevelSpellBatchFourHasExpectedClassesAndMechanics() {
        val spells = Dnd35Rules.PRESET_SPELLS.associateBy { it.name }
        assertEquals(setOf("Clérigo", "Paladino"), spells.getValue("Auxílio Divino").classes.toSet())
        assertEquals(1, spells.getValue("Auxílio Divino").classLevels.getValue("Paladino"))
        assertTrue(spells.getValue("Auxílio Divino").description.contains("máximo +3"))
        assertEquals(setOf("Clérigo"), spells.getValue("Comando").classes.toSet())
        assertTrue(spells.getValue("Comando").spellResistance)
        assertEquals(setOf("Clérigo"), spells.getValue("Santuário").classes.toSet())
        assertEquals(setOf("Clérigo"), spells.getValue("Escudo da Fé").classes.toSet())
        assertEquals(setOf("Druida", "Ranger"), spells.getValue("Presa Mágica").classes.toSet())
        assertEquals(setOf("Druida", "Ranger"), spells.getValue("Passos Longos").classes.toSet())
        assertEquals(setOf("Druida"), spells.getValue("Fogo das Fadas").classes.toSet())
        assertEquals(setOf("Bardo", "Clérigo"), spells.getValue("Remover Medo").classes.toSet())
        assertEquals("Toque", spells.getValue("Santuário").range)
        assertEquals("1 minuto/nível", spells.getValue("Escudo da Fé").duration)
    }

    @Test
    fun firstLevelSpellBatchFiveHasExpectedClassesAndMechanics() {
        val spells = Dnd35Rules.PRESET_SPELLS.associateBy { it.name }
        assertEquals(setOf("Clérigo"), spells.getValue("Maldição Menor").classes.toSet())
        assertEquals(setOf("Clérigo", "Paladino"), spells.getValue("Abençoar Água").classes.toSet())
        assertEquals("1 minuto", spells.getValue("Abençoar Água").castTime)
        assertEquals(setOf("Bardo", "Clérigo", "Druida", "Paladino", "Ranger", "Mago", "Feiticeiro"), spells.getValue("Suportar Elementos").classes.toSet())
        assertEquals(setOf("Druida", "Ranger"), spells.getValue("Detectar Animais ou Plantas").classes.toSet())
        assertFalse(spells.getValue("Detectar Animais ou Plantas").spellResistance)
        assertEquals(setOf("Druida", "Ranger"), spells.getValue("Ocultar-se de Animais").classes.toSet())
        assertEquals("24 horas", spells.getValue("Suportar Elementos").duration)
    }

    @Test
    fun firstLevelSpellBatchSixHasExpectedClassesAndMechanics() {
        val spells = Dnd35Rules.PRESET_SPELLS.associateBy { it.name }
        assertEquals(setOf("Clérigo"), spells.getValue("Amaldiçoar Água").classes.toSet())
        assertEquals("1 minuto", spells.getValue("Amaldiçoar Água").castTime)
        assertEquals(setOf("Clérigo", "Mago", "Feiticeiro"), spells.getValue("Escudo Entrópico").classes.toSet())
        assertFalse(spells.getValue("Escudo Entrópico").spellResistance)
        assertEquals(setOf("Clérigo"), spells.getValue("Ocultar-se de Mortos-Vivos").classes.toSet())
        assertEquals(setOf("Clérigo"), spells.getValue("Vigília da Morte").classes.toSet())
        assertEquals("18m", spells.getValue("Detectar Bem").range)
        assertFalse(spells.getValue("Detectar Caos").spellResistance)
    }

    @Test
    fun firstLevelNatureSpellBatchEightHasExpectedClassesAndMechanics() {
        val spells = Dnd35Rules.PRESET_SPELLS.associateBy { it.name }
        val calmAnimals = spells.getValue("Acalmar Animais")
        assertEquals(setOf("Druida", "Ranger"), calmAnimals.classes.toSet())
        assertEquals("1 minuto/nível", calmAnimals.duration)
        assertTrue(calmAnimals.spellResistance)

        val detectTraps = spells.getValue("Detectar Armadilhas")
        assertEquals(setOf("Druida", "Ranger"), detectTraps.classes.toSet())
        assertEquals("18 m", detectTraps.range)
        assertFalse(detectTraps.spellResistance)
        assertEquals(1, spells.values.count { it.name == "Detectar Armadilhas" })
        assertFalse(spells.containsKey("Detectar Laços e Covas"))

        val summonNature = spells.getValue("Invocar Aliado da Natureza I")
        assertEquals(setOf("Druida", "Ranger"), summonNature.classes.toSet())
        assertEquals("1 rodada", summonNature.castTime)
        assertEquals("1 rodada/nível (D)", summonNature.duration)
        assertFalse(summonNature.spellResistance)
    }

    @Test
    fun classSpellLevelsAreValidAndMatchListedClasses() {
        Dnd35Rules.PRESET_SPELLS.forEach { spell ->
            assertTrue("Nível geral inválido: ${spell.name}", spell.level in 0..9)
            assertEquals(
                "As classes e os níveis devem corresponder: ${spell.name}",
                spell.classes.toSet(),
                spell.classLevels.keys
            )
            spell.classLevels.forEach { (className, level) ->
                assertTrue("Nível inválido para $className: ${spell.name}", level in 0..9)
            }
        }
    }

    @Test
    fun detectPoisonsUsesPlayerHandbookClassesAndLevels() {
        val spell = Dnd35Rules.PRESET_SPELLS.single { it.name == "Detectar Venenos" }
        assertEquals(0, spell.classLevels["Clérigo"])
        assertEquals(0, spell.classLevels["Druida"])
        assertEquals(0, spell.classLevels["Mago"])
        assertEquals(0, spell.classLevels["Feiticeiro"])
        assertEquals(1, spell.classLevels["Paladino"])
        assertTrue(spell.classes.contains("Ranger"))
        assertEquals(1, spell.classLevels["Ranger"])
        assertEquals("V, G", spell.components)
        assertEquals("Curto (7,5m)", spell.range)
        assertTrue(spell.description.contains("Sabedoria CD 20"))
        val purify = Dnd35Rules.PRESET_SPELLS.single { it.name == "Purificar Alimentos" }
        assertEquals("3m", purify.range)
    }

    @Test
    fun firstLevelSpellBatchTwoHasExpectedClassesAndMechanics() {
        val spells = Dnd35Rules.PRESET_SPELLS.associateBy { it.name }
        assertEquals(setOf("Bardo", "Mago", "Feiticeiro"), spells.getValue("Disfarçar-se").classes.toSet())
        assertEquals("18m", spells.getValue("Identificação").range)
        assertEquals(setOf("Bardo", "Clérigo", "Mago", "Feiticeiro"), spells.getValue("Invocar Criaturas I").classes.toSet())
        assertEquals(setOf("Mago", "Feiticeiro"), spells.getValue("Raio do Enfraquecimento").classes.toSet())
        assertTrue(spells.getValue("Raio do Enfraquecimento").spellResistance)
        assertEquals(setOf("Clérigo", "Paladino", "Mago", "Feiticeiro"), spells.getValue("Arma Mágica").classes.toSet())
        assertEquals(setOf("Bardo", "Druida", "Ranger", "Mago", "Feiticeiro"), spells.getValue("Salto").classes.toSet())
        assertEquals(1, spells.getValue("Arma Mágica").classLevels.getValue("Paladino"))
        assertEquals(1, spells.getValue("Salto").classLevels.getValue("Ranger"))
    }


    @Test
    fun firstLevelSpellBatchThreeHasExpectedClassAvailabilityAndMechanics() {
        val spells = Dnd35Rules.PRESET_SPELLS.associateBy { it.name }
        assertEquals(setOf("Mago", "Feiticeiro"), spells.getValue("Mãos Flamejantes").classes.toSet())
        assertEquals("4,5m", spells.getValue("Mãos Flamejantes").range)
        assertEquals(setOf("Druida", "Ranger"), spells.getValue("Enfeitiçar Animal").classes.toSet())
        assertEquals(setOf("Druida", "Ranger"), spells.getValue("Constrição").classes.toSet())
        assertFalse(spells.getValue("Constrição").spellResistance)
        assertEquals(setOf("Druida", "Ranger"), spells.getValue("Passos sem Pegadas").classes.toSet())
        assertEquals(setOf("Druida", "Ranger"), spells.getValue("Falar com Animais").classes.toSet())
        assertEquals(setOf("Druida"), spells.getValue("Produzir Chama").classes.toSet())
        assertEquals(setOf("Mago", "Feiticeiro"), spells.getValue("Raio Colorido").classes.toSet())
        assertTrue(spells.getValue("Raio Colorido").spellResistance)
    }


    @Test
    fun firstLevelAlignmentProtectionSpellsHaveExpectedClassesAndMechanics() {
        val spells = Dnd35Rules.PRESET_SPELLS.associateBy { it.name }
        listOf("Proteção contra o Caos", "Proteção contra o Mal").forEach { name ->
            val spell = spells.getValue(name)
            assertEquals(setOf("Clérigo", "Paladino", "Mago", "Feiticeiro"), spell.classes.toSet())
            assertEquals(1, spell.classLevels.getValue("Paladino"))
            assertEquals("Toque", spell.range)
            assertEquals("1 minuto/nível", spell.duration)
            assertTrue(spell.spellResistance)
        }
        val goodProtection = spells.getValue("Proteção contra o Bem")
        assertEquals(setOf("Clérigo", "Mago", "Feiticeiro"), goodProtection.classes.toSet())
        assertEquals("Toque", goodProtection.range)
        assertEquals("1 minuto/nível", goodProtection.duration)
        assertTrue(goodProtection.spellResistance)
        val lawProtection = spells.getValue("Proteção contra a Lei")
        assertEquals(setOf("Clérigo", "Mago", "Feiticeiro"), lawProtection.classes.toSet())
        assertEquals("Toque", lawProtection.range)
        assertEquals("1 minuto/nível", lawProtection.duration)
        assertTrue(lawProtection.spellResistance)
    }


    @Test
    fun firstLevelArcaneSpellBatchNineHasExpectedClassesAndMechanics() {
        val spells = Dnd35Rules.PRESET_SPELLS.associateBy { it.name }
        assertEquals(setOf("Bardo", "Mago", "Feiticeiro"), spells.getValue("Animar Cordas").classes.toSet())
        assertEquals("Médio (30m + 3m/nível)", spells.getValue("Animar Cordas").range)
        assertFalse(spells.getValue("Animar Cordas").spellResistance)
        assertEquals(setOf("Mago", "Feiticeiro"), spells.getValue("Ataque Certeiro").classes.toSet())
        assertEquals("Pessoal", spells.getValue("Ataque Certeiro").range)
        assertFalse(spells.getValue("Ataque Certeiro").spellResistance)
        assertEquals(setOf("Bardo", "Mago", "Feiticeiro"), spells.getValue("Hipnotismo").classes.toSet())
        assertEquals("1 rodada", spells.getValue("Hipnotismo").castTime)
        assertEquals("2d4 rodadas", spells.getValue("Hipnotismo").duration)
        assertTrue(spells.getValue("Hipnotismo").spellResistance)
        assertEquals(setOf("Bardo", "Mago", "Feiticeiro"), spells.getValue("Montaria").classes.toSet())
        assertEquals("2 horas/nível (D)", spells.getValue("Montaria").duration)
        assertEquals(setOf("Bardo", "Mago", "Feiticeiro"), spells.getValue("Imagem Silenciosa").classes.toSet())
        assertEquals("Concentração", spells.getValue("Imagem Silenciosa").duration)
        assertFalse(spells.getValue("Imagem Silenciosa").spellResistance)
    }



    @Test
    fun firstLevelArcaneSpellBatchTenHasExpectedClassesAndMechanics() {
        val spells = Dnd35Rules.PRESET_SPELLS.associateBy { it.name }
        val floatingDisk = spells.getValue("Disco Flutuante de Tenser")
        assertEquals(setOf("Mago", "Feiticeiro"), floatingDisk.classes.toSet())
        assertEquals("1 hora/nível", floatingDisk.duration)
        assertFalse(floatingDisk.spellResistance)

        val reducePerson = spells.getValue("Reduzir Pessoa")
        assertEquals(setOf("Bardo", "Mago", "Feiticeiro"), reducePerson.classes.toSet())
        assertEquals("1 minuto/nível", reducePerson.duration)
        assertTrue(reducePerson.spellResistance)

        val shockingGrasp = spells.getValue("Toque Chocante")
        assertEquals(setOf("Mago", "Feiticeiro"), shockingGrasp.classes.toSet())
        assertEquals("Toque", shockingGrasp.range)
        assertTrue(shockingGrasp.spellResistance)
    }


    @Test
    fun firstLevelArcaneSpellBatchElevenHasExpectedClassesAndMechanics() {
        val spells = Dnd35Rules.PRESET_SPELLS.associateBy { it.name }
        val holdPortal = spells.getValue("Trancar Portal")
        assertEquals(setOf("Mago", "Feiticeiro"), holdPortal.classes.toSet())
        assertEquals("Médio (30 m + 3 m/nível)", holdPortal.range)
        assertEquals("1 minuto/nível (D)", holdPortal.duration)
        assertFalse(holdPortal.spellResistance)

        val ventriloquism = spells.getValue("Ventriloquismo")
        assertEquals(setOf("Bardo", "Mago", "Feiticeiro"), ventriloquism.classes.toSet())
        assertEquals("1 minuto/nível", ventriloquism.duration)
        assertFalse(ventriloquism.spellResistance)

        val magicAura = spells.getValue("Aura Mágica de Nystul")
        assertEquals(setOf("Bardo", "Mago", "Feiticeiro"), magicAura.classes.toSet())
        assertEquals("Toque", magicAura.range)
        assertEquals("1 dia/nível", magicAura.duration)
        assertFalse(magicAura.spellResistance)
    }


    @Test
    fun firstLevelArcaneSpellBatchTwelveHasExpectedClassesAndMechanics() {
        val spells = Dnd35Rules.PRESET_SPELLS.associateBy { it.name }
        val erase = spells.getValue("Apagar")
        assertEquals(setOf("Bardo", "Mago", "Feiticeiro"), erase.classes.toSet())
        assertEquals("Instantânea", erase.duration)
        assertFalse(erase.spellResistance)

        val chillTouch = spells.getValue("Toque Gélido")
        assertEquals(setOf("Mago", "Feiticeiro"), chillTouch.classes.toSet())
        assertEquals("Toque", chillTouch.range)
        assertTrue(chillTouch.spellResistance)
    }

    @Test
    fun shieldDescriptionDoesNotConfuseItWithFloatingDisk() {
        val spell = Dnd35Rules.PRESET_SPELLS.single { it.name == "Escudo Arcano" }
        assertTrue(spell.description.contains("disco invisível de força"))
        assertTrue(spell.description.contains("+4 de bônus de escudo na CA"))
        assertTrue(spell.description.contains("anulando Mísseis Mágicos"))
        assertFalse(spell.description.startsWith("Disco flutuante"))
    }

    @Test
    fun featherFallDescriptionUsesCorrectApproximateFallRate() {
        val spell = Dnd35Rules.PRESET_SPELLS.single { it.name == "Queda Suave" }
        assertTrue(spell.description.contains("18m por rodada (60 pés)"))
        assertTrue(spell.description.contains("anulando o dano de queda"))
        assertEquals("Vontade anula", spell.savingThrow)
        assertTrue(spell.spellResistance)
    }

    @Test
    fun endureElementsDoesNotAllowSpellResistance() {
        val spell = Dnd35Rules.PRESET_SPELLS.first { it.name == "Suportar Elementos" }
        assertEquals("24 horas", spell.duration)
        assertFalse(spell.spellResistance)
    }

    @Test
    fun cureLightWoundsUsesClassSpecificSpellLevel() {
        val spell = Dnd35Rules.PRESET_SPELLS.first { it.name == "Curar Ferimentos Leves" }
        assertEquals(1, spell.classLevels.getValue("Clérigo"))
        assertEquals(1, spell.classLevels.getValue("Druida"))
        assertEquals(1, spell.classLevels.getValue("Bardo"))
        assertEquals(1, spell.classLevels.getValue("Paladino"))
        assertEquals(2, spell.classLevels.getValue("Ranger"))
    }


    @Test
    fun bardFirstLevelSpellAuditIncludesPreviouslyMissingPhbEntries() {
        val spells = Dnd35Rules.PRESET_SPELLS.associateBy { it.name }
        val expected = setOf(
            "Área Escorregadia",
            "Boca Encantada",
            "Confusão Menor",
            "Dissimular Tendência",
            "Obscurecer Objeto",
            "Recuo Acelerado",
            "Riso Histérico de Tasha, O",
            "Transformação Momentânea"
        )
        assertTrue(spells.keys.containsAll(expected))
        assertFalse(spells.containsKey("Retirada Acelerada"))
        assertTrue(spells.containsKey("Recuo Acelerado"))
        val slipperyArea = spells.getValue("Área Escorregadia")
        assertEquals(setOf("Bardo", "Mago", "Feiticeiro"), slipperyArea.classes.toSet())
        assertEquals("V, G, M", slipperyArea.components)
        assertEquals("1 rodada/nível (D)", slipperyArea.duration)
        assertEquals("Reflexos anula (veja texto)", slipperyArea.savingThrow)
        assertFalse(slipperyArea.spellResistance)
        assertTrue(slipperyArea.description.contains("quadrado de 3 m"))
        assertEquals(1, slipperyArea.classLevels.getValue("Bardo"))
        assertEquals(1, spells.getValue("Boca Encantada").classLevels.getValue("Bardo"))
        assertEquals(2, spells.getValue("Boca Encantada").classLevels.getValue("Mago"))
        val lesserConfusion = spells.getValue("Confusão Menor")
        assertEquals(setOf("Bardo"), lesserConfusion.classes.toSet())
        assertEquals("V, G, FD", lesserConfusion.components)
        assertEquals("Nenhum", lesserConfusion.savingThrow)
        assertFalse(lesserConfusion.spellResistance)
        assertEquals("1 rodada", lesserConfusion.duration)
        assertTrue(lesserConfusion.description.contains("única criatura viva"))
        assertEquals(1, lesserConfusion.classLevels.getValue("Bardo"))
        val concealAlignment = spells.getValue("Dissimular Tendência")
        assertEquals(setOf("Bardo", "Clérigo", "Paladino"), concealAlignment.classes.toSet())
        assertEquals(1, concealAlignment.classLevels.getValue("Bardo"))
        assertEquals(2, concealAlignment.classLevels.getValue("Clérigo"))
        assertEquals(2, concealAlignment.classLevels.getValue("Paladino"))
        assertEquals("Curto (7,5m + 1,5m/2 níveis)", concealAlignment.range)
        assertEquals("V, G", concealAlignment.components)
        val obscureObject = spells.getValue("Obscurecer Objeto")
        assertEquals(setOf("Bardo", "Clérigo", "Mago", "Feiticeiro"), obscureObject.classes.toSet())
        assertEquals(1, obscureObject.classLevels.getValue("Bardo"))
        assertEquals(3, obscureObject.classLevels.getValue("Clérigo"))
        assertEquals(2, obscureObject.classLevels.getValue("Mago"))
        assertEquals(2, obscureObject.classLevels.getValue("Feiticeiro"))
        assertEquals("Toque", obscureObject.range)
        assertEquals("8 horas (D)", obscureObject.duration)
        assertEquals("Vontade anula (objeto)", obscureObject.savingThrow)
        assertTrue(obscureObject.spellResistance)
        assertTrue(obscureObject.description.contains("50 kg por nível"))
        assertTrue(obscureObject.description.contains("falha automaticamente"))
        assertTrue(obscureObject.description.contains("não consegue sentir o objeto"))
        assertEquals(2, spells.getValue("Riso Histérico de Tasha, O").classLevels.getValue("Mago"))
        assertEquals(1, spells.getValue("Transformação Momentânea").classLevels.getValue("Feiticeiro"))
        assertEquals(1, spells.values.count { it.name == "Área Escorregadia" })
    }

    @Test
    fun catalogIncludesMissingFirstLevelClericSpellsFromPlayerHandbook() {
        val spells = Dnd35Rules.PRESET_SPELLS
        val expected = listOf(
            "Auxílio Divino",
            "Invisibilidade Contra Mortos-Vivos",
            "Pedra Encantada",
            "Visão da Morte"
        )
        expected.forEach { name ->
            assertEquals(1, spells.single { it.name == name }.level)
        }

        val divineFavor = spells.single { it.name == "Auxílio Divino" }
        assertEquals(setOf("Clérigo", "Paladino"), divineFavor.classes.toSet())
        assertEquals(1, divineFavor.classLevels["Clérigo"])
        assertEquals(1, divineFavor.classLevels["Paladino"])
        assertEquals("Pessoal", divineFavor.range)
        assertFalse(divineFavor.spellResistance)

        val hideFromUndead = spells.single { it.name == "Invisibilidade Contra Mortos-Vivos" }
        assertEquals(setOf("Clérigo"), hideFromUndead.classes.toSet())
        assertEquals("Toque", hideFromUndead.range)
        assertEquals("10 minutos/nível (D)", hideFromUndead.duration)
        assertTrue(hideFromUndead.spellResistance)

        val magicStone = spells.single { it.name == "Pedra Encantada" }
        assertEquals(setOf("Clérigo", "Druida"), magicStone.classes.toSet())
        assertEquals(1, magicStone.classLevels["Clérigo"])
        assertEquals(1, magicStone.classLevels["Druida"])
        assertTrue(magicStone.description.contains("2d6+2"))

        val deathwatch = spells.single { it.name == "Visão da Morte" }
        assertEquals("9 m", deathwatch.range)
        assertEquals("10 minutos/nível", deathwatch.duration)
        assertFalse(deathwatch.spellResistance)
    }


    @Test
    fun minorDoomMatchesPortuguesePlayersHandbookEntry() {
        val spell = Dnd35Rules.PRESET_SPELLS.single { it.name == "Maldição Menor" }
        assertEquals(1, spell.level)
        assertEquals(setOf("Clérigo"), spell.classes.toSet())
        assertEquals("15 m", spell.range)
        assertEquals("1 minuto/nível", spell.duration)
        assertEquals("V, G, FD", spell.components)
        assertEquals("Vontade anula", spell.savingThrow)
        assertTrue(spell.spellResistance)
        assertTrue(spell.description.contains("dissipa Bênção"))
        assertEquals(1, Dnd35Rules.PRESET_SPELLS.count { it.name == "Maldição Menor" })
        assertFalse(Dnd35Rules.PRESET_SPELLS.any { it.name == "Perdição" })
    }
    @Test
    fun druidFirstLevelSpellNamesMatchPortuguesePlayersHandbook() {
        val names = Dnd35Rules.PRESET_SPELLS.map { it.name }
        assertEquals(1, names.count { it == "Pedra Encantada" })
        assertFalse(names.contains("Pedra Mágica"))
        assertFalse(names.contains("Emaranhar"))
        assertFalse(names.contains("Presas Mágicas"))
        assertFalse(names.contains("Convocar Aliado da Natureza I"))
        assertTrue(names.contains("Constrição"))
        assertTrue(names.contains("Presa Mágica"))
        assertTrue(names.contains("Invocar Aliado da Natureza I"))
    }



    @Test
    fun catalogIncludesAdditionalFirstLevelPlayerHandbookSpells() {
        val spells = Dnd35Rules.PRESET_SPELLS
        val byName = spells.associateBy { it.name }
        val additions = listOf("Trancar Portal", "Disco Flutuante de Tenser", "Leque Cromático")
        additions.forEach { name ->
            assertEquals("Deve existir uma única entrada para $name", 1, spells.count { it.name == name })
            assertEquals(1, byName.getValue(name).level)
            assertEquals(setOf("Mago", "Feiticeiro"), byName.getValue(name).classes.toSet())
            assertEquals(1, byName.getValue(name).classLevels.getValue("Mago"))
            assertEquals(1, byName.getValue(name).classLevels.getValue("Feiticeiro"))
        }
        val closePortal = byName.getValue("Trancar Portal")
        assertEquals("V", closePortal.components)
        assertEquals("1 minuto/nível (D)", closePortal.duration)
        assertFalse(closePortal.spellResistance)
        assertEquals(1, spells.count { it.name == "Disco Flutuante de Tenser" })
        assertEquals(0, spells.count { it.name == "Disco Flutuante" })
        val disk = byName.getValue("Disco Flutuante de Tenser")
        assertEquals("V, G, M (uma gota de mercúrio)", disk.components)
        assertEquals("1 hora/nível", disk.duration)
        assertFalse(disk.spellResistance)
        val colorSpray = byName.getValue("Leque Cromático")
        assertEquals("4,5 m", colorSpray.range)
        assertEquals("Vontade anula", colorSpray.savingThrow)
        assertTrue(colorSpray.spellResistance)
        assertTrue(colorSpray.description.contains("2d4 rodadas"))
        assertTrue(colorSpray.description.contains("1d4 rodadas"))
        assertEquals(0, spells.count { it.name == "Montaria Arcana" })
        val mount = byName.getValue("Montaria")
        assertEquals(setOf("Bardo", "Mago", "Feiticeiro"), mount.classes.toSet())
        assertEquals("1 rodada", mount.castTime)
        assertEquals("V, G, M (um pouco de pelo de cavalo)", mount.components)
        assertEquals("2 horas/nível (D)", mount.duration)
        assertFalse(mount.spellResistance)
    }



    @Test
    fun paladinAndRangerSpellListsHaveCorrectClassSpecificLevels() {
        val spells = Dnd35Rules.PRESET_SPELLS.associateBy { it.name }

        val blessWeapon = spells.getValue("Abençoar Arma")
        assertEquals(1, blessWeapon.level)
        assertEquals(setOf("Paladino"), blessWeapon.classes.toSet())
        assertEquals(mapOf("Paladino" to 1), blessWeapon.classLevels)

        val readMagic = spells.getValue("Ler Magias")
        assertEquals(setOf("Bardo", "Clérigo", "Druida", "Paladino", "Ranger", "Mago", "Feiticeiro"), readMagic.classes.toSet())
        assertEquals(0, readMagic.classLevels.getValue("Bardo"))
        assertEquals(0, readMagic.classLevels.getValue("Clérigo"))
        assertEquals(0, readMagic.classLevels.getValue("Druida"))
        assertEquals(1, readMagic.classLevels.getValue("Paladino"))
        assertEquals(1, readMagic.classLevels.getValue("Ranger"))
        assertEquals(0, readMagic.classLevels.getValue("Mago"))
        assertEquals(0, readMagic.classLevels.getValue("Feiticeiro"))

        val createWater = spells.getValue("Criar Água")
        assertEquals(mapOf("Clérigo" to 0, "Druida" to 0, "Paladino" to 1), createWater.classLevels)
        assertEquals("V, G, FD", createWater.components)

        val detectUndead = spells.getValue("Detectar Mortos-Vivos")
        assertEquals(setOf("Clérigo", "Mago", "Feiticeiro", "Paladino"), detectUndead.classes.toSet())
        assertEquals(mapOf("Clérigo" to 1, "Mago" to 1, "Feiticeiro" to 1, "Paladino" to 1), detectUndead.classLevels)

        val resistance = spells.getValue("Resistência")
        assertEquals(1, resistance.classLevels.getValue("Paladino"))
        assertEquals(0, resistance.classLevels.getValue("Bardo"))
        assertEquals("V, G, M/FD", resistance.components)

        val virtue = spells.getValue("Virtude")
        assertEquals(mapOf("Clérigo" to 0, "Druida" to 0, "Paladino" to 1), virtue.classLevels)
        assertEquals("V, G, FD", virtue.components)
        assertEquals("Fortitude anula (inofensivo)", virtue.savingThrow)

        val cureLight = spells.getValue("Curar Ferimentos Leves")
        assertEquals(1, cureLight.classLevels.getValue("Paladino"))
        assertEquals(2, cureLight.classLevels.getValue("Ranger"))

        val lesserRestoration = spells.getValue("Restauração Menor")
        assertEquals(setOf("Clérigo", "Druida", "Paladino", "Ranger"), lesserRestoration.classes.toSet())
        assertEquals(mapOf("Clérigo" to 2, "Druida" to 2, "Paladino" to 1, "Ranger" to 1), lesserRestoration.classLevels)
    }


    @Test
    fun rangerFirstLevelSpellListIncludesEnergyResistanceAndDelayPoison() {
        val spells = Dnd35Rules.PRESET_SPELLS.associateBy { it.name }

        val resistElements = spells.getValue("Resistência à Elementos")
        assertEquals(2, resistElements.level)
        assertEquals(setOf("Clérigo", "Druida", "Paladino", "Ranger", "Mago", "Feiticeiro"), resistElements.classes.toSet())
        assertEquals(mapOf("Clérigo" to 2, "Druida" to 2, "Paladino" to 2, "Ranger" to 1, "Mago" to 2, "Feiticeiro" to 2), resistElements.classLevels)
        assertEquals("V, G, FD", resistElements.components)
        assertEquals("Fortitude anula (inofensiva)", resistElements.savingThrow)
        assertEquals("10 minutos/nível", resistElements.duration)
        assertTrue(resistElements.spellResistance)
        assertFalse(spells.values.any { it.name == "Suportar Energia" })

        val delayPoison = spells.getValue("Retardar Envenenamento")
        assertEquals(setOf("Clérigo", "Druida", "Paladino", "Ranger"), delayPoison.classes.toSet())
        assertEquals(mapOf("Clérigo" to 2, "Druida" to 2, "Paladino" to 2, "Ranger" to 1), delayPoison.classLevels)
        assertEquals("1 hora/nível", delayPoison.duration)
        assertTrue(delayPoison.spellResistance)
    }


    @Test
    fun rangerFirstLevelCatalogIncludesTrapDetectionAnimalInvisibilityAndMessenger() {
        val spells = Dnd35Rules.PRESET_SPELLS.associateBy { it.name }

        val traps = spells.getValue("Detectar Armadilhas")
        assertEquals(mapOf("Druida" to 1, "Ranger" to 1), traps.classLevels)
        assertEquals("18 m", traps.range)
        assertEquals("Nenhum", traps.savingThrow)
        assertFalse(traps.spellResistance)

        val invisibility = spells.getValue("Invisibilidade Contra Animais")
        assertEquals(mapOf("Druida" to 1, "Ranger" to 1), invisibility.classLevels)
        assertEquals("G, FD", invisibility.components)
        assertEquals("Vontade anula (objeto)", invisibility.savingThrow)
        assertTrue(invisibility.spellResistance)

        val messenger = spells.getValue("Mensageiro Animal")
        assertEquals(mapOf("Bardo" to 2, "Druida" to 2, "Ranger" to 1), messenger.classLevels)
        assertEquals("V, G, M", messenger.components)
        assertEquals("1 dia/nível", messenger.duration)
        assertTrue(messenger.spellResistance)
    }



    @Test
    fun rangerSecondLevelCatalogIncludesSixVerifiedSpells() {
        val spells = Dnd35Rules.PRESET_SPELLS.associateBy { it.name }

        val speakWithPlants = spells.getValue("Falar com Plantas")
        assertEquals(mapOf("Bardo" to 4, "Druida" to 3, "Ranger" to 2), speakWithPlants.classLevels)
        assertEquals("Pessoal", speakWithPlants.range)

        val holdAnimal = spells.getValue("Imobilizar Animal")
        assertEquals(mapOf("Druida" to 2, "Ranger" to 2), holdAnimal.classLevels)
        assertEquals("Vontade anula", holdAnimal.savingThrow)
        assertTrue(holdAnimal.spellResistance)

        val windWall = spells.getValue("Muralha de Vento")
        assertEquals(mapOf("Clérigo" to 3, "Druida" to 3, "Ranger" to 2, "Mago" to 3, "Feiticeiro" to 3), windWall.classLevels)
        assertEquals("V, G, M/FD", windWall.components)
        assertEquals("Nenhum; veja texto", windWall.savingThrow)

        val barkskin = spells.getValue("Pele de Árvore")
        assertEquals(mapOf("Druida" to 2, "Ranger" to 2), barkskin.classLevels)
        assertEquals("V, G, FD", barkskin.components)

        val protection = spells.getValue("Proteção Contra Elementos")
        assertEquals(mapOf("Clérigo" to 3, "Druida" to 3, "Ranger" to 2, "Mago" to 3, "Feiticeiro" to 3), protection.classLevels)
        assertEquals("Fortitude anula (inofensiva)", protection.savingThrow)

        val owlWisdom = spells.getValue("Sabedoria da Coruja")
        assertEquals(mapOf("Clérigo" to 2, "Druida" to 2, "Paladino" to 2, "Ranger" to 2, "Mago" to 2, "Feiticeiro" to 2), owlWisdom.classLevels)
        assertEquals("V, G, M/FD", owlWisdom.components)
    }



    @Test
    fun rangerSecondLevelCatalogIncludesSummonNatureAllyII() {
        val spells = Dnd35Rules.PRESET_SPELLS
        val byName = spells.associateBy { it.name }

        val summonI = byName.getValue("Invocar Aliado da Natureza I")
        assertEquals(mapOf("Druida" to 1, "Ranger" to 1), summonI.classLevels)
        assertEquals("Conjuração (Invocação)", summonI.school)
        assertEquals("1 rodada", summonI.castTime)
        assertEquals("V, G, FD", summonI.components)
        assertEquals("Curto (7,5 m + 1,5 m/2 níveis)", summonI.range)
        assertEquals("1 rodada/nível (D)", summonI.duration)
        assertEquals("Nenhum", summonI.savingThrow)
        assertFalse(summonI.spellResistance)

        val summonII = byName.getValue("Invocar Aliado da Natureza II")
        assertEquals(1, spells.count { it.name == "Invocar Aliado da Natureza II" })
        assertEquals(2, summonII.level)
        assertEquals(mapOf("Druida" to 2, "Ranger" to 2), summonII.classLevels)
        assertEquals("Conjuração (Invocação)", summonII.school)
        assertEquals("1 rodada", summonII.castTime)
        assertEquals("V, G, FD", summonII.components)
        assertEquals(summonI.range, summonII.range)
        assertEquals(summonI.duration, summonII.duration)
        assertEquals("Nenhum", summonII.savingThrow)
        assertFalse(summonII.spellResistance)
        assertTrue(summonII.description.contains("1d3"))
        assertTrue(summonII.description.contains("mesmo tipo"))
    }


    @Test
    fun rangerSecondLevelCatalogUsesCanonicalCatAgilityAndIncludesThornGrowth() {
        val spells = Dnd35Rules.PRESET_SPELLS
        val byName = spells.associateBy { it.name }

        assertFalse(byName.containsKey("Graça Felina"))
        val catAgility = byName.getValue("Agilidade do Gato")
        assertEquals(1, spells.count { it.name == "Agilidade do Gato" })
        assertEquals(2, catAgility.level)
        assertEquals(mapOf("Bardo" to 2, "Druida" to 2, "Ranger" to 2, "Feiticeiro" to 2, "Mago" to 2), catAgility.classLevels)
        assertEquals("V, G, M", catAgility.components)
        assertEquals("Toque", catAgility.range)
        assertEquals("1 min/nível", catAgility.duration)
        assertEquals("Vontade anula (inofensiva)", catAgility.savingThrow)
        assertTrue(catAgility.spellResistance)

        val thornGrowth = byName.getValue("Crescer Espinhos")
        assertEquals(2, thornGrowth.level)
        assertEquals(mapOf("Druida" to 3, "Ranger" to 2), thornGrowth.classLevels)
        assertEquals("V, G, FD", thornGrowth.components)
        assertEquals("Médio (30 m + 3 m/nível)", thornGrowth.range)
        assertEquals("1 hora/nível (D)", thornGrowth.duration)
        assertEquals("Reflexos parcial", thornGrowth.savingThrow)
        assertTrue(thornGrowth.spellResistance)
        assertTrue(thornGrowth.description.contains("1d4"))
        assertTrue(thornGrowth.description.contains("Operar Mecanismo"))
    }


    @Test
    fun rangerSecondLevelCatalogIncludesTrapSpellWithPlayerHandbookMechanics() {
        val spells = Dnd35Rules.PRESET_SPELLS
        val trap = spells.single { it.name == "Armadilha" }

        assertEquals(2, trap.level)
        assertEquals("Transmutação", trap.school)
        assertEquals(mapOf("Ranger" to 2, "Druida" to 3), trap.classLevels)
        assertEquals("3 rodadas", trap.castTime)
        assertEquals("V, G, FD", trap.components)
        assertEquals("Toque", trap.range)
        assertEquals("Até ser disparada ou destruída", trap.duration)
        assertEquals("Nenhum", trap.savingThrow)
        assertFalse(trap.spellResistance)
        assertTrue(trap.description.contains("CD 23"))
        assertTrue(trap.description.contains("1d6 pontos de dano"))
        assertTrue(trap.description.contains("CA 7 e 5 PV"))
        assertEquals(1, spells.count { it.name == "Armadilha" })
    }

    @Test
    fun rangerSecondLevelCatalogIncludesBearEnduranceFromPlayerHandbook() {
        val spells = Dnd35Rules.PRESET_SPELLS
        val bearEndurance = spells.single { it.name == "Vigor do Urso" }

        assertEquals(2, bearEndurance.level)
        assertEquals("Transmutação", bearEndurance.school)
        assertEquals(
            mapOf("Clérigo" to 2, "Druida" to 2, "Ranger" to 2, "Feiticeiro" to 2, "Mago" to 2),
            bearEndurance.classLevels
        )
        assertEquals("1 ação padrão", bearEndurance.castTime)
        assertEquals("V, G, FD", bearEndurance.components)
        assertEquals("Toque", bearEndurance.range)
        assertEquals("1 minuto/nível", bearEndurance.duration)
        assertEquals("Vontade anula (inofensiva)", bearEndurance.savingThrow)
        assertTrue(bearEndurance.spellResistance)
        assertTrue(bearEndurance.description.contains("+4 de bônus de melhoria na Constituição"))
        assertEquals(1, spells.count { it.name == "Vigor do Urso" })
    }

    @Test
    fun rangerThirdLevelCatalogIncludesHealingWaterWalkingAndPoisonNeutralization() {
        val spells = Dnd35Rules.PRESET_SPELLS

        val waterWalking = spells.single { it.name == "Caminhar na Água" }
        assertEquals(3, waterWalking.level)
        assertEquals("Transmutação [Água]", waterWalking.school)
        assertEquals(mapOf("Clérigo" to 3, "Ranger" to 3), waterWalking.classLevels)
        assertEquals("V, G, FD", waterWalking.components)
        assertEquals("Toque", waterWalking.range)
        assertEquals("10 minutos/nível (D)", waterWalking.duration)
        assertEquals("Vontade anula (inofensiva)", waterWalking.savingThrow)
        assertTrue(waterWalking.spellResistance)

        val healing = spells.single { it.name == "Curar Ferimentos Moderados" }
        assertEquals(3, healing.level)
        assertEquals("Conjuração (Cura)", healing.school)
        assertEquals(
            mapOf("Bardo" to 2, "Clérigo" to 2, "Druida" to 3, "Paladino" to 3, "Ranger" to 3),
            healing.classLevels
        )
        assertEquals("Instantânea", healing.duration)
        assertTrue(healing.description.contains("2d8"))
        assertTrue(healing.description.contains("máximo de +10"))

        val poison = spells.single { it.name == "Neutralizar Venenos" }
        assertEquals(3, poison.level)
        assertEquals("Conjuração (Cura)", poison.school)
        assertEquals(
            mapOf("Bardo" to 4, "Clérigo" to 4, "Druida" to 3, "Paladino" to 4, "Ranger" to 3),
            poison.classLevels
        )
        assertEquals("V, G, M/FD", poison.components)
        assertEquals("10 minutos/nível", poison.duration)
        assertTrue(poison.description.contains("imunidade a venenos"))
        assertEquals(1, spells.count { it.name == "Caminhar na Água" })
        assertEquals(1, spells.count { it.name == "Curar Ferimentos Moderados" })
        assertEquals(1, spells.count { it.name == "Neutralizar Venenos" })
    }


    @Test
    fun rangerThirdLevelCatalogIncludesPlantTreeAndNatureSummoningSpells() {
        val spells = Dnd35Rules.PRESET_SPELLS
        val expected = mapOf(
            "Ampliar Plantas" to mapOf("Druida" to 3, "Ranger" to 3),
            "Comandar Plantas" to mapOf("Druida" to 4, "Ranger" to 3),
            "Enfraquecer Plantas" to mapOf("Druida" to 3, "Ranger" to 3),
            "Forma de Árvore" to mapOf("Druida" to 2, "Ranger" to 3),
            "Invocar Aliado da Natureza III" to mapOf("Druida" to 3, "Ranger" to 3)
        )
        expected.forEach { (name, levels) ->
            val spell = spells.single { it.name == name }
            assertEquals(3, spell.level)
            assertEquals(levels, spell.classLevels)
            assertEquals(1, spells.count { it.name == name })
        }
        val amplify = spells.single { it.name == "Ampliar Plantas" }
        assertEquals("V, G, FD", amplify.components)
        assertEquals("Instantânea", amplify.duration)
        assertFalse(amplify.spellResistance)
        assertTrue(amplify.description.contains("Enfraquecer Plantas"))
        val command = spells.single { it.name == "Comandar Plantas" }
        assertEquals("V", command.components)
        assertEquals("1 dia/nível", command.duration)
        assertEquals("Vontade anula", command.savingThrow)
        assertTrue(command.spellResistance)
        val weaken = spells.single { it.name == "Enfraquecer Plantas" }
        assertEquals("Nenhum", weaken.savingThrow)
        assertFalse(weaken.spellResistance)
        assertTrue(weaken.description.contains("Ampliar Plantas"))
        val tree = spells.single { it.name == "Forma de Árvore" }
        assertEquals("Pessoal", tree.range)
        assertEquals("1 hora/nível (D)", tree.duration)
        assertTrue(tree.description.contains("+10 de armadura natural"))
        val summon = spells.single { it.name == "Invocar Aliado da Natureza III" }
        assertEquals("1 rodada", summon.castTime)
        assertEquals("1 rodada/nível (D)", summon.duration)
        assertEquals("V, G, FD", summon.components)
        assertTrue(summon.description.contains("1d4+1"))
    }


    @Test
    fun rangerThirdLevelCatalogIncludesGreaterMagicFangReduceAnimalRemoveDiseaseAndRepelInsects() {
        val spells = Dnd35Rules.PRESET_SPELLS
        val expected = mapOf(
            "Presa Mágica Maior" to mapOf("Druida" to 3, "Ranger" to 3),
            "Reduzir Animal" to mapOf("Druida" to 2, "Ranger" to 3),
            "Remover Doenças" to mapOf("Clérigo" to 3, "Druida" to 3, "Ranger" to 3),
            "Repelir Insetos" to mapOf("Bardo" to 4, "Clérigo" to 4, "Druida" to 4, "Ranger" to 3)
        )
        expected.forEach { (name, levels) ->
            val spell = spells.single { it.name == name }
            assertEquals(3, spell.level)
            assertEquals(levels, spell.classLevels)
            assertEquals(1, spells.count { it.name == name })
        }
        val fang = spells.single { it.name == "Presa Mágica Maior" }
        assertEquals("1 hora/nível", fang.duration)
        assertTrue(fang.description.contains("quatro níveis"))
        val reduce = spells.single { it.name == "Reduzir Animal" }
        assertEquals("1 hora/nível (D)", reduce.duration)
        assertEquals("Nenhum", reduce.savingThrow)
        assertFalse(reduce.spellResistance)
        val remove = spells.single { it.name == "Remover Doenças" }
        assertEquals("Instantânea", remove.duration)
        assertEquals("Fortitude anula (inofensiva)", remove.savingThrow)
        assertTrue(remove.spellResistance)
        val repel = spells.single { it.name == "Repelir Insetos" }
        assertEquals("3 m", repel.range)
        assertEquals("10 minutos/nível (D)", repel.duration)
        assertTrue(repel.description.contains("um terço"))
    }


    @Test
    fun darkvisionIsCatalogedAtSecondLevelForRangerAndArcaneCasters() {
        val spells = Dnd35Rules.PRESET_SPELLS
        val darkvision = spells.single { it.name == "Visão no Escuro" }
        assertEquals(2, darkvision.level)
        assertEquals(
            mapOf("Ranger" to 2, "Feiticeiro" to 2, "Mago" to 2),
            darkvision.classLevels
        )
        assertEquals("Transmutação", darkvision.school)
        assertEquals("V, G, M", darkvision.components)
        assertEquals("Toque", darkvision.range)
        assertEquals("1 hora/nível", darkvision.duration)
        assertEquals("Vontade anula (inofensiva)", darkvision.savingThrow)
        assertTrue(darkvision.spellResistance)
        assertTrue(darkvision.description.contains("18 m"))
        assertTrue(darkvision.description.contains("escuridão mágica"))
        assertEquals(1, spells.count { it.name == "Visão no Escuro" })
    }


    @Test
    fun rangerFourthLevelCatalogIncludesAllSevenPlayerHandbookSpells() {
        val spells = Dnd35Rules.PRESET_SPELLS
        val expected = mapOf(
            "Ampliar Animais" to mapOf("Druida" to 5, "Ranger" to 4, "Feiticeiro" to 5, "Mago" to 5),
            "Caminhar em Árvores" to mapOf("Druida" to 5, "Ranger" to 4),
            "Comunhão Com a Natureza" to mapOf("Druida" to 5, "Ranger" to 4),
            "Curar Ferimentos Graves" to mapOf("Bardo" to 3, "Clérigo" to 3, "Druida" to 4, "Paladino" to 4, "Ranger" to 4),
            "Dificultar Detecção" to mapOf("Ranger" to 4, "Feiticeiro" to 3, "Mago" to 3),
            "Invocar Aliado da Natureza IV" to mapOf("Druida" to 4, "Ranger" to 4),
            "Movimentação Livre" to mapOf("Bardo" to 4, "Druida" to 4, "Ranger" to 4)
        )
        expected.forEach { (name, levels) ->
            val spell = spells.single { it.name == name }
            assertEquals(4, spell.level)
            assertEquals(levels, spell.classLevels)
        }
        assertEquals("Transmutação", spells.single { it.name == "Ampliar Animais" }.school)
        assertEquals("Conjuração (Teletransporte)", spells.single { it.name == "Caminhar em Árvores" }.school)
        assertEquals("Adivinhação", spells.single { it.name == "Comunhão Com a Natureza" }.school)
        assertEquals("Conjuração (Cura)", spells.single { it.name == "Curar Ferimentos Graves" }.school)
        assertEquals("Abjuração", spells.single { it.name == "Dificultar Detecção" }.school)
        assertEquals("Conjuração (Invocação)", spells.single { it.name == "Invocar Aliado da Natureza IV" }.school)
        assertEquals("Abjuração", spells.single { it.name == "Movimentação Livre" }.school)
        assertEquals(expected.size, expected.keys.sumOf { name -> spells.count { it.name == name } })
    }


    @Test
    fun paladinFirstLevelSpellListMatchesPlayerHandbookAndClassLevels() {
        val spells = Dnd35Rules.PRESET_SPELLS
        val expected = setOf(
            "Abençoar Água", "Abençoar Arma", "Arma Mágica", "Auxílio Divino",
            "Bênção", "Criar Água", "Curar Ferimentos Leves", "Detectar Mortos-Vivos",
            "Detectar Venenos", "Ler Magias", "Proteção contra o Caos",
            "Proteção contra o Mal", "Resistência", "Restauração Menor",
            "Suportar Elementos", "Virtude"
        )
        val actual = spells.filter { it.classLevels["Paladino"] == 1 }.map { it.name }.toSet()
        assertEquals(expected, actual)
        expected.forEach { name ->
            val matches = spells.filter { it.name == name }
            assertEquals("Deve existir uma única entrada para $name", 1, matches.size)
            assertTrue("$name deve incluir Paladino na lista de classes", "Paladino" in matches.single().classes)
            assertEquals(1, matches.single().classLevels["Paladino"])
        }
        assertFalse("Favor Divino é uma tradução duplicada de Auxílio Divino", spells.any { it.name == "Favor Divino" })
        assertFalse("Proteção contra o Bem não pertence à lista de Paladino", spells.single { it.name == "Proteção contra o Bem" }.classes.contains("Paladino"))
        assertFalse("Escudo da Fé não pertence à lista de Paladino", spells.single { it.name == "Escudo da Fé" }.classes.contains("Paladino"))
        assertFalse("Amaldiçoar Água não pertence à lista de Paladino", spells.single { it.name == "Amaldiçoar Água" }.classes.contains("Paladino"))
        assertFalse("Detectar Bem não pertence à lista de Paladino", spells.single { it.name == "Detectar Bem" }.classes.contains("Paladino"))
        assertFalse("Detectar Mal não pertence à lista de Paladino", spells.single { it.name == "Detectar Mal" }.classes.contains("Paladino"))
    }

    @Test
    fun paladinSpellListIncludesMissingPlayerHandbookSpellsFromLevelsTwoToFour() {
        val spells = Dnd35Rules.PRESET_SPELLS
        val expected = mapOf(
            "Esplendor da Águia" to 2,
            "Proteger Outro" to 2,
            "Remover Paralisia" to 2,
            "Zona da Verdade" to 2,
            "Arma Mágica Maior" to 3,
            "Círculo Mágico Contra o Caos" to 3,
            "Círculo Mágico Contra o Mal" to 3,
            "Curar Montaria" to 3,
            "Discernir Mentiras" to 3,
            "Luz do Dia" to 3,
            "Remover Cegueira/Surdez" to 3,
            "Remover Maldição" to 3,
            "Cancelar Encantamento" to 4,
            "Dissipar o Caos" to 4,
            "Dissipar o Mal" to 4,
            "Espada Sagrada" to 4,
            "Marca da Justiça" to 4,
            "Proteção Contra a Morte" to 4,
            "Restauração" to 4
        )
        expected.forEach { (name, paladinLevel) ->
            val matches = spells.filter { it.name == name }
            assertEquals("Deve existir uma única entrada para $name", 1, matches.size)
            val spell = matches.single()
            assertEquals("Nível geral esperado para $name", paladinLevel, spell.level)
            assertEquals("Nível de Paladino esperado para $name", paladinLevel, spell.classLevels["Paladino"])
            assertTrue("$name deve estar disponível para Paladino", "Paladino" in spell.classes)
        }
        assertEquals("O catálogo não deve conter nomes duplicados",
            spells.size, spells.map { it.name }.toSet().size)
    }


    @Test
    fun rangerFirstLevelCatalogMatchesPlayerHandbook() {
        val spells = Dnd35Rules.PRESET_SPELLS
        val expected = setOf(
            "Acalmar Animais", "Alarme", "Constrição", "Detectar Animais ou Plantas",
            "Detectar Armadilhas", "Detectar Venenos", "Enfeitiçar Animal",
            "Falar com Animais", "Invisibilidade Contra Animais",
            "Invocar Aliado da Natureza I", "Ler Magias", "Mensageiro Animal",
            "Passos Longos", "Passos sem Pegadas"
        )
        val actual = spells.filter { it.classLevels["Ranger"] == 1 }.map { it.name }.toSet()
        assertEquals(expected, actual)
        expected.forEach { name ->
            val matches = spells.filter { it.name == name }
            assertEquals("Deve existir uma única entrada para $name", 1, matches.size)
            assertTrue("$name deve incluir Ranger na lista de classes", "Ranger" in matches.single().classes)
            assertEquals(1, matches.single().classLevels["Ranger"])
        }
    }
}
