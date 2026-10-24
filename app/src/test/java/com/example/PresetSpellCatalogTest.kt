package com.example

import com.example.dnd35.model.Dnd35Rules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PresetSpellCatalogTest {
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
            "Detectar Veneno",
            "Consertar",
            "Mensagem",
            "Abrir/Fechar",
            "Prestidigitação",
            "Purificar Alimentos e Bebidas",
            "Atordoar",
            "Espirro Ácido",
            "Marca Arcana",
            "Raio de Gelo",
            "Desestabilizar Mortos-Vivos",
            "Toque de Fadiga",
            "Lampejo",
            "Conhecer Direção",
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
            "Emaranhar",
            "Bom Fruto",
            "Passos sem Pegadas",
            "Falar com Animais",
            "Produzir Chama",
            "Raio Colorido",
            "Favor Divino",
            "Comando",
            "Santuário",
            "Escudo da Fé",
            "Presas Mágicas",
            "Passos Longos",
            "Fogo das Fadas",
            "Remover Medo",
            "Perdição",
            "Abençoar Água",
            "Desespero",
            "Suportar Elementos",
            "Pedra Mágica",
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
        assertEquals(setOf("Mago", "Feiticeiro"), spells.getValue("Espirro Ácido").classes.toSet())
        assertFalse(spells.getValue("Espirro Ácido").spellResistance)
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
        assertEquals(setOf("Clérigo", "Paladino"), spells.getValue("Favor Divino").classes.toSet())
        assertEquals(setOf("Clérigo"), spells.getValue("Comando").classes.toSet())
        assertTrue(spells.getValue("Comando").spellResistance)
        assertEquals(setOf("Clérigo"), spells.getValue("Santuário").classes.toSet())
        assertEquals(setOf("Clérigo", "Paladino"), spells.getValue("Escudo da Fé").classes.toSet())
        assertEquals(setOf("Druida", "Ranger"), spells.getValue("Presas Mágicas").classes.toSet())
        assertEquals(setOf("Druida", "Ranger"), spells.getValue("Passos Longos").classes.toSet())
        assertEquals(setOf("Druida"), spells.getValue("Fogo das Fadas").classes.toSet())
        assertEquals(setOf("Bardo", "Clérigo"), spells.getValue("Remover Medo").classes.toSet())
        assertEquals("Toque", spells.getValue("Santuário").range)
        assertEquals("1 minuto/nível", spells.getValue("Escudo da Fé").duration)
    }

    @Test
    fun firstLevelSpellBatchFiveHasExpectedClassesAndMechanics() {
        val spells = Dnd35Rules.PRESET_SPELLS.associateBy { it.name }
        assertEquals(setOf("Clérigo"), spells.getValue("Perdição").classes.toSet())
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
        assertEquals(setOf("Clérigo", "Paladino"), spells.getValue("Amaldiçoar Água").classes.toSet())
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

        val detectTraps = spells.getValue("Detectar Laços e Covas")
        assertEquals(setOf("Druida", "Ranger"), detectTraps.classes.toSet())
        assertEquals("18m/nível", detectTraps.range)
        assertFalse(detectTraps.spellResistance)

        val summonNature = spells.getValue("Convocar Aliado da Natureza I")
        assertEquals(setOf("Druida", "Ranger"), summonNature.classes.toSet())
        assertEquals("1 rodada", summonNature.castTime)
        assertEquals("1 rodada/nível", summonNature.duration)
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
    fun detectPoisonUsesDifferentLevelsForPaladinAndRanger() {
        val spell = Dnd35Rules.PRESET_SPELLS.single { it.name == "Detectar Veneno" }
        assertEquals(0, spell.classLevels["Clérigo"])
        assertEquals(0, spell.classLevels["Druida"])
        assertEquals(0, spell.classLevels["Mago"])
        assertEquals(0, spell.classLevels["Feiticeiro"])
        assertEquals(1, spell.classLevels["Paladino"])
        assertEquals(1, spell.classLevels["Ranger"])
        assertEquals("Curto (7,5m)", spell.range)
        assertTrue(spell.description.contains("Sabedoria CD 20"))
        val purify = Dnd35Rules.PRESET_SPELLS.single { it.name == "Purificar Alimentos e Bebidas" }
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
        assertEquals(setOf("Druida", "Ranger"), spells.getValue("Emaranhar").classes.toSet())
        assertFalse(spells.getValue("Emaranhar").spellResistance)
        assertEquals(setOf("Druida", "Ranger"), spells.getValue("Passos sem Pegadas").classes.toSet())
        assertEquals(setOf("Druida", "Ranger"), spells.getValue("Falar com Animais").classes.toSet())
        assertEquals(setOf("Druida"), spells.getValue("Produzir Chama").classes.toSet())
        assertEquals(setOf("Mago", "Feiticeiro"), spells.getValue("Raio Colorido").classes.toSet())
        assertTrue(spells.getValue("Raio Colorido").spellResistance)
    }


    @Test
    fun firstLevelAlignmentProtectionSpellsHaveExpectedClassesAndMechanics() {
        val spells = Dnd35Rules.PRESET_SPELLS.associateBy { it.name }
        listOf("Proteção contra o Caos", "Proteção contra o Bem").forEach { name ->
            val spell = spells.getValue(name)
            assertEquals(setOf("Clérigo", "Paladino", "Mago", "Feiticeiro"), spell.classes.toSet())
            assertEquals("Toque", spell.range)
            assertEquals("1 minuto/nível", spell.duration)
            assertTrue(spell.spellResistance)
        }
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
        assertEquals("2 horas/nível", spells.getValue("Montaria").duration)
        assertEquals(setOf("Bardo", "Mago", "Feiticeiro"), spells.getValue("Imagem Silenciosa").classes.toSet())
        assertEquals("Concentração", spells.getValue("Imagem Silenciosa").duration)
        assertFalse(spells.getValue("Imagem Silenciosa").spellResistance)
    }



    @Test
    fun firstLevelArcaneSpellBatchTenHasExpectedClassesAndMechanics() {
        val spells = Dnd35Rules.PRESET_SPELLS.associateBy { it.name }
        val floatingDisk = spells.getValue("Disco Flutuante")
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
        assertEquals("Médio (30m + 3m/nível)", holdPortal.range)
        assertEquals("1 minuto/nível", holdPortal.duration)
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
    fun catalogIncludesAdditionalFirstLevelPlayerHandbookSpells() {
        val spells = Dnd35Rules.PRESET_SPELLS
        val byName = spells.associateBy { it.name }
        val additions = listOf("Cerrar Portas", "Disco Flutuante de Tenser", "Leque Cromático", "Montaria Arcana")
        additions.forEach { name ->
            assertEquals("Deve existir uma única entrada para $name", 1, spells.count { it.name == name })
            assertEquals(1, byName.getValue(name).level)
            assertEquals(setOf("Mago", "Feiticeiro"), byName.getValue(name).classes.toSet())
            assertEquals(1, byName.getValue(name).classLevels.getValue("Mago"))
            assertEquals(1, byName.getValue(name).classLevels.getValue("Feiticeiro"))
        }
        val closePortal = byName.getValue("Cerrar Portas")
        assertEquals("V", closePortal.components)
        assertEquals("1 minuto/nível (D)", closePortal.duration)
        assertFalse(closePortal.spellResistance)
        val disk = byName.getValue("Disco Flutuante de Tenser")
        assertEquals("V, G, M", disk.components)
        assertEquals("1 hora/nível", disk.duration)
        assertFalse(disk.spellResistance)
        val colorSpray = byName.getValue("Leque Cromático")
        assertEquals("4,5 m", colorSpray.range)
        assertEquals("Vontade anula", colorSpray.savingThrow)
        assertTrue(colorSpray.spellResistance)
        val mount = byName.getValue("Montaria Arcana")
        assertEquals("1 rodada", mount.castTime)
        assertEquals("2 horas/nível (D)", mount.duration)
        assertFalse(mount.spellResistance)
    }

}
