package com.example

import com.example.dnd35.model.CharacterCalculator
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.CharacterInventoryItem
import com.example.dnd35.model.CharacterJsonAdapters
import com.example.dnd35.model.CharacterSkill
import com.example.dnd35.viewmodel.isValidEditedSkillRanks
import com.example.dnd35.viewmodel.normalizeEditedSkillRanks
import com.example.dnd35.model.SkillPointAllocation
import com.example.dnd35.model.SkillPointLevelRecord
import com.example.dnd35.model.parseSkillPointAllocations
import com.example.dnd35.model.parseWholeSkillPointBudget
import com.example.dnd35.model.Dnd35Rules
import com.example.dnd35.model.Dnd35Rules.SaveProgression
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleUnitTest {

  @Test
  fun skillPointAllocationsRequireWholeNonNegativePointsAndUniqueNames() {
    assertEquals(
      listOf(SkillPointAllocation("Atuação", 2f), SkillPointAllocation("Ouvir", 1f)),
      parseSkillPointAllocations("Atuação=2\nOuvir=1")
    )
    assertEquals(emptyList<SkillPointAllocation>(), parseSkillPointAllocations(""))
    assertEquals(null, parseSkillPointAllocations("Escalar=0,5"))
    assertEquals(null, parseSkillPointAllocations("Ouvir=-1"))
    assertEquals(null, parseSkillPointAllocations("Ouvir=1\nouvir=2"))
    assertEquals(null, parseSkillPointAllocations("linha sem valor"))
  }

  @Test
  fun skillPointBudgetInputRequiresWholeNonNegativePoints() {
    assertEquals(8, parseWholeSkillPointBudget("8"))
    assertEquals(0, parseWholeSkillPointBudget("0"))
    assertEquals(null, parseWholeSkillPointBudget("2,5"))
    assertEquals(null, parseWholeSkillPointBudget("-1"))
    assertEquals(null, parseWholeSkillPointBudget(""))
  }

  @Test
  fun testAbilityModifiers() {
    assertEquals(0, Dnd35Rules.calculateModifier(10))
    assertEquals(0, Dnd35Rules.calculateModifier(11))
    assertEquals(1, Dnd35Rules.calculateModifier(12))
    assertEquals(2, Dnd35Rules.calculateModifier(14))
    assertEquals(3, Dnd35Rules.calculateModifier(16))
    assertEquals(4, Dnd35Rules.calculateModifier(18))
    assertEquals(-1, Dnd35Rules.calculateModifier(8))
    assertEquals(-2, Dnd35Rules.calculateModifier(6))
  }

  @Test
  fun testCarryingCapacityOfficialTable() {
    val capStr10 = Dnd35Rules.getCarryingCapacity(10)
    assertEquals(33, capStr10.lightMaxLb)
    assertEquals(66, capStr10.mediumMaxLb)
    assertEquals(100, capStr10.heavyMaxLb)

    val capStr16 = Dnd35Rules.getCarryingCapacity(16)
    assertEquals(76, capStr16.lightMaxLb)
    assertEquals(153, capStr16.mediumMaxLb)
    assertEquals(230, capStr16.heavyMaxLb)

    // Efeitos da carga
    val lightLoad = Dnd35Rules.calculateEncumbrance(50f, 16, "Médio")
    assertEquals(Dnd35Rules.EncumbranceLevel.LIGHT, lightLoad)

    val mediumLoad = Dnd35Rules.calculateEncumbrance(100f, 16, "Médio")
    assertEquals(Dnd35Rules.EncumbranceLevel.MEDIUM, mediumLoad)
    assertEquals(-3, mediumLoad.armorCheckPenalty)

    val heavyLoad = Dnd35Rules.calculateEncumbrance(200f, 16, "Médio")
    assertEquals(Dnd35Rules.EncumbranceLevel.HEAVY, heavyLoad)
    assertEquals(-6, heavyLoad.armorCheckPenalty)
  }

  @Test
  fun skillPointAllocationsCanonicalizeLegacyAliasesAndRejectDuplicateSkills() {
    assertTrue(com.example.dnd35.model.parseSkillPointAllocations("Ofício=2\nOfícios=1") == null)
    assertTrue(com.example.dnd35.model.parseSkillPointAllocations("Salto=1\nSaltar=2") == null)

    assertEquals(
      listOf(
        com.example.dnd35.model.SkillPointAllocation("Ofícios", 2f),
        com.example.dnd35.model.SkillPointAllocation("Saltar", 1f)
      ),
      com.example.dnd35.model.parseSkillPointAllocations("Ofício=2\nSalto=1")
    )
  }

  @Test
  fun skillPointBudgetEstimateUsesClassIntelligenceAndLevelOneMultiplier() {
    assertEquals(24, com.example.dnd35.model.estimateSkillPointsForLevel("Bardo", 1, 0, "Elfo"))
    assertEquals(28, com.example.dnd35.model.estimateSkillPointsForLevel("Bardo", 1, 0, "Humano"))
    assertEquals(9, com.example.dnd35.model.estimateSkillPointsForLevel("Bardo", 2, 2, "Humano"))
  }

  @Test
  fun skillPointBudgetEstimateAppliesMinimumAndRejectsUnknownInputs() {
    assertEquals(8, com.example.dnd35.model.estimateSkillPointsForLevel("Guerreiro", 1, -5, "Humano"))
    assertEquals(1, com.example.dnd35.model.estimateSkillPointsForLevel("Guerreiro", 2, -5, "Elfo"))
    assertEquals(null, com.example.dnd35.model.estimateSkillPointsForLevel("Classe inexistente", 1, 0, "Humano"))
    assertEquals(null, com.example.dnd35.model.estimateSkillPointsForLevel("Bardo", 0, 0, "Humano"))
  }

  @Test
  fun racialSkillBonusesAreIncludedExactlyOnceInComputedSkills() {
    val halflingSkills = listOf(
      CharacterSkill(name = "Escalar", keyAbility = "FOR"),
      CharacterSkill(name = "Saltar", keyAbility = "FOR"),
      CharacterSkill(name = "Ouvir", keyAbility = "SAB"),
      CharacterSkill(name = "Esconder-se", keyAbility = "DES"),
      CharacterSkill(name = "Furtividade", keyAbility = "DES")
    )
    val halfling = CharacterCalculator.compute(
      CharacterEntity(
        race = "Halfling",
        baseStr = 10,
        baseDex = 10,
        baseWis = 10,
        baseInt = 10,
        baseCha = 10,
        skillsJson = CharacterJsonAdapters.serializeSkills(halflingSkills)
      )
    )
    halfling.computedSkills.forEach { skill ->
      val expectedRacialBonus = if (skill.skill.name == "Esconder-se") 0 else 2
      assertEquals(expectedRacialBonus, skill.racialBonus)
      assertEquals(
        skill.ranks.toInt() + skill.keyAbilityMod + skill.racialBonus +
          skill.sizeBonus + skill.miscBonus + skill.armorPenaltyApplied,
        skill.totalBonus
      )
    }

    val elf = CharacterCalculator.compute(
      CharacterEntity(
        race = "Elfo",
        baseStr = 10,
        baseDex = 10,
        baseWis = 10,
        baseInt = 10,
        baseCha = 10,
        skillsJson = CharacterJsonAdapters.serializeSkills(
          listOf(CharacterSkill(name = "Ouvir", keyAbility = "SAB"))
        )
      )
    ).computedSkills.single()
    assertEquals(2, elf.racialBonus)
    assertEquals(2, elf.totalBonus)

    val halfElf = CharacterCalculator.compute(
      CharacterEntity(
        race = "Meio-Elfo",
        baseStr = 10,
        baseDex = 10,
        baseWis = 10,
        baseInt = 10,
        baseCha = 10,
        skillsJson = CharacterJsonAdapters.serializeSkills(
          listOf(
            CharacterSkill(name = "Ouvir", keyAbility = "SAB"),
            CharacterSkill(name = "Procurar", keyAbility = "INT"),
            CharacterSkill(name = "Observar", keyAbility = "SAB"),
            CharacterSkill(name = "Diplomacia", keyAbility = "CAR"),
            CharacterSkill(name = "Obter Informação", keyAbility = "CAR")
          )
        )
      )
    ).computedSkills.associateBy { it.skill.name }
    val expectedHalfElfBonuses = mapOf(
      "Ouvir" to 1,
      "Procurar" to 1,
      "Observar" to 1,
      "Diplomacia" to 2,
      "Obter Informação" to 2
    )
    expectedHalfElfBonuses.forEach { (skillName, expectedBonus) ->
      val skill = halfElf.getValue(skillName)
      assertEquals(expectedBonus, skill.racialBonus)
      assertEquals(expectedBonus, skill.totalBonus)
    }

    val gnome = CharacterCalculator.compute(
      CharacterEntity(
        race = "Gnomo",
        baseStr = 10,
        baseDex = 10,
        baseWis = 10,
        baseInt = 10,
        baseCha = 10,
        skillsJson = CharacterJsonAdapters.serializeSkills(
          listOf(CharacterSkill(name = "Ofícios (Alquimia)", keyAbility = "INT"))
        )
      )
    ).computedSkills.single()
    assertEquals(2, gnome.racialBonus)
    assertEquals(2, gnome.totalBonus)
  }

  @Test
  fun stoutHalflingDescriptionMatchesStrongheartTraits() {
    val race = Dnd35Rules.RACES.associateBy { it.name }.getValue("Halfling Robusto")
    assertEquals(0, race.conMod)
    assertTrue(race.traits.contains("talento adicional no 1º nível"))
    assertTrue(race.traits.contains("não recebe o +1 racial geral nos testes de resistência"))
    assertTrue(race.traits.contains("+2 em Escalar, Saltar, Ouvir e Furtividade"))
    assertTrue(!race.traits.contains("afinidade com pedras"))
  }

  @Test
  fun stoutHalflingReceivesCoreHalflingSkillBonuses() {
    val character = CharacterCalculator.compute(
      CharacterEntity(
        race = "Halfling Robusto",
        baseStr = 10,
        baseDex = 10,
        baseWis = 10,
        baseInt = 10,
        baseCha = 10,
        skillsJson = CharacterJsonAdapters.serializeSkills(
          listOf(
            CharacterSkill(name = "Escalar", keyAbility = "FOR"),
            CharacterSkill(name = "Saltar", keyAbility = "FOR"),
            CharacterSkill(name = "Ouvir", keyAbility = "SAB"),
            CharacterSkill(name = "Furtividade", keyAbility = "DES")
          )
        )
      )
    )
    val bonuses = character.computedSkills.associate { it.skill.name to it.racialBonus }
    assertEquals(2, bonuses.getValue("Escalar"))
    assertEquals(2, bonuses.getValue("Saltar"))
    assertEquals(2, bonuses.getValue("Ouvir"))
    assertEquals(2, bonuses.getValue("Furtividade"))
  }

  @Test
  fun stoutHalflingDoesNotReceiveGeneralHalflingSavingThrowBonus() {
    val character = CharacterCalculator.compute(
      CharacterEntity(
        race = "Halfling Robusto",
        baseStr = 10,
        baseDex = 10,
        baseCon = 10,
        baseWis = 10,
        baseInt = 10,
        baseCha = 10
      )
    )
    assertEquals(character.baseFort + character.conMod, character.totalFort)
    assertEquals(character.baseRef + character.dexMod, character.totalRef)
    assertEquals(character.baseWill + character.wisMod, character.totalWill)
  }

  @Test
  fun hideSkillUsesSizeModifierWithoutAffectingOtherSkills() {
    val smallCharacter = CharacterCalculator.compute(
      CharacterEntity(
        race = "Halfling",
        baseStr = 10,
        baseDex = 10,
        baseWis = 10,
        baseInt = 10,
        baseCha = 10,
        skillsJson = CharacterJsonAdapters.serializeSkills(
          listOf(
            CharacterSkill(name = "Esconder-se", keyAbility = "DES"),
            CharacterSkill(name = "Furtividade", keyAbility = "DES")
          )
        )
      )
    )
    val hide = smallCharacter.computedSkills.first { it.skill.name == "Esconder-se" }
    val stealth = smallCharacter.computedSkills.first { it.skill.name == "Furtividade" }
    assertEquals(4, hide.sizeBonus)
    assertEquals(5, hide.totalBonus) // +4 tamanho e +1 de Destreza racial; sem bônus racial em Esconder-se
    assertEquals(0, stealth.sizeBonus)
    assertEquals(3, stealth.totalBonus) // +2 racial e +1 de Destreza racial, sem bônus de tamanho
  }

  @Test
  fun testCharacterCalculator() {
    val char = CharacterEntity(
      name = "Guerreiro Teste",
      characterClass = "Guerreiro",
      race = "Humano",
      level = 1,
      baseStr = 16,
      baseDex = 14,
      baseCon = 14,
      baseInt = 10,
      baseWis = 10,
      baseCha = 10
    )
    val stats = CharacterCalculator.compute(char)
    assertEquals(3, stats.strMod)
    assertEquals(2, stats.dexMod)
    assertEquals(1, stats.bab)
    assertEquals(4, stats.grappleBonus) // 1 BAB + 3 Str
    assertEquals(12, stats.totalAc) // 10 + 2 Dex
    assertTrue(stats.computedSkills.isNotEmpty())
  }
  @Test
  fun equippedItemAppliesAbilityBonusEvenWhenDescriptionContinues() {
    val item = CharacterInventoryItem(
      name = "Cinto da Força",
      category = "Itens Mágicos",
      equipped = true,
      equippedSlot = Dnd35Rules.EquipmentSlots.WAIST,
      damageOrAc = "—",
      notes = "+6 FOR; item mágico de teste"
    )
    val character = CharacterEntity(
      baseStr = 14,
      inventoryJson = CharacterJsonAdapters.serializeInventory(listOf(item))
    )

    val stats = CharacterCalculator.compute(character)
    assertEquals("Bônus +6 FOR deve ser reconhecido em qualquer posição da descrição", 20, stats.totalStr)
  }

  @Test
  fun unequippedItemDoesNotApplyAbilityBonusEvenIfSlotIsSet() {
    val item = CharacterInventoryItem(
      name = "Cinto da Força",
      category = "Itens Mágicos",
      equipped = false,
      equippedSlot = Dnd35Rules.EquipmentSlots.WAIST,
      notes = "+6 FOR"
    )
    val character = CharacterEntity(
      baseStr = 14,
      inventoryJson = CharacterJsonAdapters.serializeInventory(listOf(item))
    )

    val stats = CharacterCalculator.compute(character)
    assertEquals("Item não equipado não deve alterar Força", 14, stats.totalStr)
  }

  @Test
  fun barbarianDoesNotGetFastMovementInMediumArmor() {
    val armor = CharacterInventoryItem(
      name = "Gibão de Peles",
      category = "Armaduras",
      equipped = true,
      equippedSlot = Dnd35Rules.EquipmentSlots.ARMOR_BODY,
      weightLb = 25f,
      damageOrAc = "+3 CA",
      notes = "Des Max +4, Pen -3"
    )
    val stats = CharacterCalculator.compute(
      CharacterEntity(
        characterClass = "Bárbaro",
        level = 1,
        baseStr = 16,
        inventoryJson = CharacterJsonAdapters.serializeInventory(listOf(armor))
      )
    )
    assertEquals(9.0f, stats.speedMetersEffective, 0.01f)
  }

  @Test
  fun mediumArmorReducesNormalMovementSpeed() {
    val armor = CharacterInventoryItem(
      name = "Gibão de Peles",
      category = "Armaduras",
      equipped = true,
      equippedSlot = Dnd35Rules.EquipmentSlots.ARMOR_BODY,
      weightLb = 25f,
      damageOrAc = "+3 CA",
      notes = "Des Max +4, Pen -3"
    )
    val stats = CharacterCalculator.compute(
      CharacterEntity(
        characterClass = "Guerreiro",
        level = 1,
        baseStr = 16,
        inventoryJson = CharacterJsonAdapters.serializeInventory(listOf(armor))
      )
    )
    assertEquals(6.0f, stats.speedMetersEffective, 0.01f)
  }

  @Test
  fun dwarfMovementIsNotReducedByMediumLoad() {
    val stats = CharacterCalculator.compute(
      CharacterEntity(
        race = "Anão",
        baseStr = 10,
        inventoryJson = CharacterJsonAdapters.serializeInventory(
          listOf(CharacterInventoryItem(
            name = "Carga de teste",
            category = "Itens de Aventura",
            weightLb = 40f,
            equipped = false
          ))
        )
      )
    )
    assertEquals(Dnd35Rules.EncumbranceLevel.MEDIUM, stats.encumbrance)
    assertEquals(6.0f, stats.speedMetersEffective, 0.01f)
  }

  @Test
  fun mediumLoadReducesSmallCreatureMovementToFourPointFiveMeters() {
    val stats = CharacterCalculator.compute(
      CharacterEntity(
        race = "Halfling",
        baseStr = 10,
        inventoryJson = CharacterJsonAdapters.serializeInventory(
          listOf(CharacterInventoryItem(
            name = "Carga de teste",
            category = "Itens de Aventura",
            weightLb = 30f,
            equipped = false
          ))
        )
      )
    )
    assertEquals(Dnd35Rules.EncumbranceLevel.MEDIUM, stats.encumbrance)
    assertEquals(4.5f, stats.speedMetersEffective, 0.01f)
  }

  @Test
  fun overloadedCharacterCanOnlyMoveOnePointFiveMeters() {
    val stats = CharacterCalculator.compute(
      CharacterEntity(
        baseStr = 10,
        inventoryJson = CharacterJsonAdapters.serializeInventory(
          listOf(CharacterInventoryItem(
            name = "Carga excessiva",
            category = "Itens de Aventura",
            weightLb = 120f,
            equipped = false
          ))
        )
      )
    )
    assertEquals(Dnd35Rules.EncumbranceLevel.OVERLOADED, stats.encumbrance)
    assertEquals(1.5f, stats.speedMetersEffective, 0.01f)
  }

  @Test
  fun barbarianFastMovementWorksWithMediumLoad() {
    val stats = CharacterCalculator.compute(
      CharacterEntity(
        characterClass = "Bárbaro",
        level = 1,
        baseStr = 16,
        inventoryJson = CharacterJsonAdapters.serializeInventory(
          listOf(CharacterInventoryItem(
            name = "Carga de teste",
            category = "Itens de Aventura",
            weightLb = 100f,
            equipped = false
          ))
        )
      )
    )
    assertEquals(Dnd35Rules.EncumbranceLevel.MEDIUM, stats.encumbrance)
    assertEquals(9.0f, stats.speedMetersEffective, 0.01f)
  }

  @Test
  fun barbarianGetsFastMovementWithLightArmorAndLightLoad() {
    val armor = CharacterInventoryItem(
      name = "Armadura de Couro",
      category = "Armaduras",
      equipped = true,
      equippedSlot = Dnd35Rules.EquipmentSlots.ARMOR_BODY,
      weightLb = 15f,
      damageOrAc = "+2 CA",
      notes = "Des Max +6, Pen 0"
    )
    val stats = CharacterCalculator.compute(
      CharacterEntity(
        characterClass = "Bárbaro",
        level = 1,
        baseStr = 16,
        inventoryJson = CharacterJsonAdapters.serializeInventory(listOf(armor))
      )
    )
    assertEquals(12.0f, stats.speedMetersEffective, 0.01f)
  }

  @Test
  fun swimmingAppliesDoubleArmorCheckPenalty() {
    val armor = CharacterInventoryItem(
      name = "Armadura de teste",
      category = "Armaduras",
      equipped = true,
      equippedSlot = Dnd35Rules.EquipmentSlots.ARMOR_BODY,
      damageOrAc = "+3 CA",
      notes = "Pen -3"
    )
    val skills = listOf(
      CharacterSkill(name = "Natação", keyAbility = "FOR", ranks = 1f),
      CharacterSkill(name = "Escalar", keyAbility = "FOR", ranks = 1f)
    )
    val stats = CharacterCalculator.compute(
      CharacterEntity(
        characterClass = "Guerreiro",
        level = 1,
        baseStr = 10,
        skillsJson = CharacterJsonAdapters.serializeSkills(skills),
        inventoryJson = CharacterJsonAdapters.serializeInventory(listOf(armor))
      )
    )
    val swim = stats.computedSkills.first { it.skill.name == "Natação" }
    val climb = stats.computedSkills.first { it.skill.name == "Escalar" }

    assertEquals(-6, swim.armorPenaltyApplied)
    assertEquals(-3, climb.armorPenaltyApplied)
  }

  @Test
  fun monkLosesWisdomArmorClassBonusWithMediumLoad() {
    val unencumbered = CharacterCalculator.compute(
      CharacterEntity(
        characterClass = "Monge",
        level = 5,
        baseStr = 10,
        baseDex = 10,
        baseWis = 14,
        goldGp = 0f
      )
    )
    val encumbered = CharacterCalculator.compute(
      CharacterEntity(
        characterClass = "Monge",
        level = 5,
        baseStr = 10,
        baseDex = 10,
        baseWis = 14,
        goldGp = 0f,
        inventoryJson = CharacterJsonAdapters.serializeInventory(
          listOf(CharacterInventoryItem(
            name = "Carga de teste",
            category = "Itens de Aventura",
            weightLb = 40f,
            equipped = false
          ))
        )
      )
    )

    assertEquals(Dnd35Rules.EncumbranceLevel.LIGHT, unencumbered.encumbrance)
    assertEquals(Dnd35Rules.EncumbranceLevel.MEDIUM, encumbered.encumbrance)
    assertEquals(13, unencumbered.totalAc)
    assertEquals(10, encumbered.totalAc)
  }

  @Test
  fun monkFastMovementIsSuppressedByMediumLoad() {
    val stats = CharacterCalculator.compute(
      CharacterEntity(
        characterClass = "Monge",
        level = 6,
        baseStr = 10,
        goldGp = 0f,
        inventoryJson = CharacterJsonAdapters.serializeInventory(
          listOf(CharacterInventoryItem(
            name = "Carga de teste",
            category = "Itens de Aventura",
            weightLb = 40f,
            equipped = false
          ))
        )
      )
    )
    assertEquals(0.67f, stats.encumbrance.speedFactor, 0.001f)
    assertEquals(6.0f, stats.speedMetersEffective, 0.01f)
  }

  @Test
  fun knowledgeSkillNamesUseFullPortugueseLabels() {
    val names = Dnd35Rules.SKILL_DEFINITIONS.map { it.name }
    assertTrue(names.contains("Conhecimento (Arquitetura e Engenharia)"))
    assertTrue(names.contains("Conhecimento (Nobreza e Realeza)"))
    assertTrue(names.none { it == "Conhecimento (Arquitetura)" })
    assertTrue(names.none { it == "Conhecimento (Nobreza)" })
  }

  @Test
  fun bardClassSkillsMatchCoreSkillList() {
    val bard = Dnd35Rules.CLASSES.first { it.name == "Bardo" }
    val expected = listOf(
      "Acrobacia", "Arte da Fuga", "Atuação", "Avaliação", "Blefar",
      "Concentração", "Conhecimento (Arcano)", "Conhecimento (Arquitetura e Engenharia)",
      "Conhecimento (Geografia)", "Conhecimento (História)", "Conhecimento (Local)",
      "Conhecimento (Masmorras)", "Conhecimento (Natureza)", "Conhecimento (Nobreza e Realeza)",
      "Conhecimento (Planos)", "Conhecimento (Religião)", "Decifrar Escrita", "Diplomacia",
      "Disfarces", "Equilíbrio", "Escalar", "Esconder-se", "Furtividade",
      "Identificar Magia", "Natação", "Obter Informação", "Ofícios", "Ouvir",
      "Prestidigitação", "Profissão", "Saltar", "Sentir Motivação", "Usar Instrumento Mágico"
    )
    assertEquals(expected.toSet(), bard.classSkills.toSet())
    assertTrue(bard.classSkills.none { it == "Observar" })
  }

  @Test
  fun barbarianClassSkillsIncludeCraftProfessionSkill() {
    val barbarian = Dnd35Rules.CLASSES.first { it.name == "Bárbaro" }
    assertTrue(barbarian.classSkills.contains("Ofícios"))
  }

  @Test
  fun clericClassSkillsIncludeCraftProfessionSkill() {
    val cleric = Dnd35Rules.CLASSES.first { it.name == "Clérigo" }
    assertTrue(cleric.classSkills.contains("Ofícios"))
  }

  @Test
  fun druidClassSkillsIncludeCraftProfessionSkill() {
    val druid = Dnd35Rules.CLASSES.first { it.name == "Druida" }
    assertTrue(druid.classSkills.contains("Ofícios"))
  }

  @Test
  fun remainingCoreClassesIncludeCraftSkill() {
    val classesExpectedToHaveCraft = listOf("Guerreiro", "Monge", "Paladino", "Ranger", "Ladino")
    classesExpectedToHaveCraft.forEach { className ->
      val characterClass = Dnd35Rules.CLASSES.first { it.name == className }
      assertTrue("$className deve ter Ofícios como perícia de classe", characterClass.classSkills.contains("Ofícios"))
    }
  }

  @Test
  fun openLockSkillUsesDexterityRequiresTrainingAndIsRogueClassSkill() {
    val definition = Dnd35Rules.SKILL_DEFINITIONS.first { it.name == "Abrir Fechaduras" }
    assertEquals("DES", definition.keyAbility)
    assertTrue(definition.trainedOnly)
    assertTrue(!definition.armorCheckPenaltyApplies)

    val rogue = Dnd35Rules.CLASSES.first { it.name == "Ladino" }
    val fighter = Dnd35Rules.CLASSES.first { it.name == "Guerreiro" }
    assertTrue(rogue.classSkills.contains("Abrir Fechaduras"))
    assertTrue(!fighter.classSkills.contains("Abrir Fechaduras"))
  }

  @Test
  fun skillCatalogIncludesPortugueseAcrobaticsSkill() {
    val acrobatics = Dnd35Rules.SKILL_DEFINITIONS.first { it.name == "Acrobacia" }
    assertTrue(!acrobatics.trainedOnly)
    assertTrue(acrobatics.armorCheckPenaltyApplies)
    assertTrue(Dnd35Rules.SKILL_DEFINITIONS.any { it.name == "Equilíbrio" })
    assertTrue(Dnd35Rules.SKILL_DEFINITIONS.any { it.name == "Arte da Fuga" })
  }

  @Test
  fun legacySkillNamesKeepRanksAndUseCanonicalLabels() {
    val legacySkills = listOf(
      CharacterSkill(name = "Salto", keyAbility = "FOR", ranks = 1f),
      CharacterSkill(name = "Ofício", keyAbility = "INT", ranks = 1f)
    )
    val stats = CharacterCalculator.compute(
      CharacterEntity(
        characterClass = "Bárbaro",
        level = 1,
        skillsJson = CharacterJsonAdapters.serializeSkills(legacySkills)
      )
    )
    val jump = stats.computedSkills.first { it.skill.name == "Saltar" }
    val craft = stats.computedSkills.first { it.skill.name == "Ofícios" }

    assertEquals(1f, jump.ranks)
    assertEquals(1f, craft.ranks)
    assertTrue(jump.isClassSkill)
    assertTrue(craft.isClassSkill)
    assertEquals("Saltar", Dnd35Rules.canonicalSkillName("Salto"))
    assertEquals("Ofícios", Dnd35Rules.canonicalSkillName("Ofício"))
  }

  @Test
  fun skillRanksRespectClassAndCrossClassCapsAndHalfRanks() {
    val skills = listOf(
      CharacterSkill(name = "Escalar", keyAbility = "FOR", ranks = 20f),
      CharacterSkill(name = "Acrobacia", keyAbility = "DES", ranks = 1.25f),
      CharacterSkill(name = "Conhecimento (Arcano)", keyAbility = "INT", ranks = 0f)
    )
    val stats = CharacterCalculator.compute(
      CharacterEntity(
        characterClass = "Guerreiro",
        level = 1,
        skillsJson = CharacterJsonAdapters.serializeSkills(skills)
      )
    )
    val climb = stats.computedSkills.first { it.skill.name == "Escalar" }
    val acrobatics = stats.computedSkills.first { it.skill.name == "Acrobacia" }
    val knowledge = stats.computedSkills.first { it.skill.name == "Conhecimento (Arcano)" }

    assertEquals(4f, climb.maxRanks)
    assertEquals(4f, climb.ranks)
    assertEquals(2f, acrobatics.maxRanks)
    assertEquals(1.5f, acrobatics.ranks)
    assertTrue(knowledge.trainedOnly)
    assertTrue(!knowledge.canRoll)
  }

  @Test
  fun editingSkillRanksPreservesHalfRanksAndRespectsCap() {
    assertEquals(1.5f, normalizeEditedSkillRanks(1.5f, 7f))
    assertEquals(1.5f, normalizeEditedSkillRanks(1.5f, 2f))
    assertEquals(2f, normalizeEditedSkillRanks(2.24f, 7f))
    assertEquals(2f, normalizeEditedSkillRanks(2.25f, 7f))
    assertEquals(2f, normalizeEditedSkillRanks(2.26f, 7f))
    assertTrue(isValidEditedSkillRanks(0f))
    assertTrue(isValidEditedSkillRanks(1.5f))
    assertTrue(!isValidEditedSkillRanks(2.24f))
    assertTrue(!isValidEditedSkillRanks(2.25f))
    assertTrue(!isValidEditedSkillRanks(2.26f))
    assertTrue(!isValidEditedSkillRanks(-0.5f))
    assertTrue(!isValidEditedSkillRanks(Float.NaN))
    assertTrue(!isValidEditedSkillRanks(Float.POSITIVE_INFINITY))
    assertEquals(0f, normalizeEditedSkillRanks(-1f, 7f))
    assertEquals(2f, normalizeEditedSkillRanks(2.5f, 2f))
  }

  @Test
  fun multiclassPreservesHalfRanksWhenSkillBecomesClassSkill() {
    val skills = listOf(
      CharacterSkill(name = "Acrobacia", keyAbility = "DES", ranks = 1.5f)
    )
    val stats = CharacterCalculator.compute(
      CharacterEntity(
        characterClass = "Guerreiro/Bardo",
        level = 2,
        multiclassJson = CharacterJsonAdapters.serializeClassEntries(
          listOf(
            com.example.dnd35.model.CharacterClassEntry("Guerreiro", 1),
            com.example.dnd35.model.CharacterClassEntry("Bardo", 1)
          )
        ),
        skillsJson = CharacterJsonAdapters.serializeSkills(skills)
      )
    )
    val acrobatics = stats.computedSkills.first { it.skill.name == "Acrobacia" }

    assertTrue(acrobatics.isClassSkill)
    assertEquals(1.5f, acrobatics.ranks)
    // As meias graduações são preservadas, mas só a parte inteira melhora o teste.
    assertEquals(
      1 + acrobatics.keyAbilityMod + acrobatics.miscBonus + acrobatics.armorPenaltyApplied,
      acrobatics.totalBonus
    )
  }

  @Test
  fun multiclassRankCapUsesTotalCharacterLevel() {
    val stats = CharacterCalculator.compute(
      CharacterEntity(
        characterClass = "Guerreiro/Ladino",
        level = 4,
        multiclassJson = CharacterJsonAdapters.serializeClassEntries(
          listOf(
            com.example.dnd35.model.CharacterClassEntry("Guerreiro", 2),
            com.example.dnd35.model.CharacterClassEntry("Ladino", 2)
          )
        )
      )
    )
    assertEquals(4, stats.totalCharacterLevel)
    assertEquals(7f, stats.computedSkills.first { it.skill.name == "Escalar" }.maxRanks)
  }

  @Test
  fun testSizeModifiersAndCarryingCapacity() {
    assertEquals(1, Dnd35Rules.getArmorClassSizeModifier("Pequeno"))
    assertEquals(-2, Dnd35Rules.getArmorClassSizeModifier("Enorme"))
    assertEquals(-16, Dnd35Rules.getGrappleSizeModifier("Minúsculo"))
    assertEquals(12, Dnd35Rules.getGrappleSizeModifier("Imenso"))
    assertEquals(4, Dnd35Rules.getCarryingCapacity(10, "Minúsculo").lightMaxLb)
    assertEquals(8, Dnd35Rules.getCarryingCapacity(10, "Diminuto").lightMaxLb)
    assertEquals(16, Dnd35Rules.getCarryingCapacity(10, "Miúdo").lightMaxLb)
    assertEquals(24, Dnd35Rules.getCarryingCapacity(10, "Pequeno").lightMaxLb)
  }

  @Test
  fun paladinHasGoodWillSaveProgression() {
    val paladin = Dnd35Rules.CLASSES.first { it.name == "Paladino" }
    assertEquals(Dnd35Rules.SaveProgression.GOOD, paladin.willType)
    val levelOne = CharacterCalculator.compute(
      CharacterEntity(characterClass = "Paladino", level = 1)
    )
    val levelTwo = CharacterCalculator.compute(
      CharacterEntity(characterClass = "Paladino", level = 2)
    )
    assertEquals(2, levelOne.baseWill)
    assertEquals(3, levelTwo.baseWill)
  }

  @Test
  fun barbarianHasSixRageUsesAtLevelTwenty() {
    val stats = CharacterCalculator.compute(
      CharacterEntity(characterClass = "Bárbaro", level = 20)
    )
    assertEquals(6, stats.rageUsesPerDay)
  }

  @Test
  fun sorcererClassSkillsMatchCoreSkillList() {
    val sorcerer = Dnd35Rules.CLASSES.first { it.name == "Feiticeiro" }
    val expected = setOf(
      "Concentração", "Conhecimento (Arcano)", "Identificar Magia", "Ofícios", "Profissão"
    )
    assertEquals(expected, sorcerer.classSkills.toSet())
    assertTrue(sorcerer.classSkills.none { it == "Blefar" })
  }

  @Test
  fun monkClassSkillsMatchCoreSkillList() {
    val monk = Dnd35Rules.CLASSES.first { it.name == "Monge" }
    val expected = setOf(
      "Acrobacia", "Arte da Fuga", "Atuação", "Concentração",
      "Conhecimento (Arcano)", "Conhecimento (Religião)", "Diplomacia",
      "Equilíbrio", "Escalar", "Esconder-se", "Furtividade", "Natação",
      "Observar", "Ofícios", "Ouvir", "Profissão", "Saltar", "Sentir Motivação"
    )
    assertEquals(expected, monk.classSkills.toSet())
  }

  @Test
  fun wizardAndSorcererFeaturesMatchCoreClassRules() {
    val sorcerer = Dnd35Rules.CLASSES.first { it.name == "Feiticeiro" }
    val wizard = Dnd35Rules.CLASSES.first { it.name == "Mago" }

    assertEquals("CAR", sorcerer.spellCastingAbility)
    assertTrue(sorcerer.features.first { it.name == "Conjuração Espontânea" }.description.contains("repertório limitado"))
    assertTrue(sorcerer.features.first { it.name == "Invocar Familiar" }.description.contains("não é a habilidade Companheiro Animal"))

    assertEquals("INT", wizard.spellCastingAbility)
    assertTrue(wizard.features.first { it.name == "Escrever Pergaminho" }.description.contains("talento bônus"))
    assertTrue(wizard.features.first { it.name == "Especialização em Escola (Opcional)" }.description.contains("Adivinhação"))
    assertEquals(listOf(5, 10, 15, 20), wizard.features.filter { it.name.startsWith("Talento Bônus de Mago") }.map { it.level })
    assertTrue(wizard.features.filter { it.name.startsWith("Talento Bônus de Mago") }.all { it.description.contains("Maestria em Magia") })
  }

  @Test
  fun clericFeaturesDescribeDomainsAndNeutralAlignmentChoices() {
    val features = Dnd35Rules.CLASSES.first { it.name == "Clérigo" }.features
    val turning = features.first { it.name == "Expulsar ou Fascinar Mortos-Vivos" }
    val spontaneous = features.first { it.name == "Conversão Espontânea" }
    val domains = features.first { it.name == "Domínios da Divindade" }

    assertTrue(turning.description.contains("3 + modificador de Carisma"))
    assertTrue(turning.description.contains("clérigos neutros"))
    assertTrue(spontaneous.description.contains("Clérigos neutros"))
    assertTrue(domains.description.contains("dois domínios"))
    assertTrue(domains.description.contains("espaço de magia por nível"))
  }

  @Test
  fun rangerProgressionIncludesFavoredEnemyAndHighLevelFeatures() {
    val features = Dnd35Rules.CLASSES.first { it.name == "Ranger" }.features
    val expectedLevels = listOf(1, 5, 10, 15, 20)

    expectedLevels.forEach { level ->
      assertTrue("Inimigo Predileto ausente no nível $level",
        features.any { it.level == level && it.name.contains("Inimigo Predileto") })
    }

    assertTrue(features.any { it.level == 4 && it.name == "Conjuração de Ranger" })
    assertTrue(features.any { it.level == 17 && it.name == "Camuflagem" })
    assertTrue(features.any { it.level == 18 && it.name == "Esconder-se à Plena Vista" })
    assertTrue(features.none { it.level == 14 && it.name.contains("Inimigo Predileto") })
  }

  @Test
  fun fighterBonusFeatsAreListedAtCorrectLevelsWithoutGrantingSpecificFeats() {
    val features = Dnd35Rules.CLASSES.first { it.name == "Guerreiro" }.features
    val expectedLevels = listOf(1, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20)

    expectedLevels.forEach { level ->
      assertTrue("Talento bônus de Guerreiro ausente no nível $level",
        features.any { it.level == level && it.name == "Talento Bônus de Guerreiro (${level}º nível)" })
    }

    assertTrue(features.none { it.name.contains("Especialização em Arma") && it.name.contains("&") })
    assertTrue(features.none { it.name.contains("Foco Aprimorado") })
  }

  @Test
  fun bardicMusicProgressionFollowsDnd35Milestones() {
    val features = Dnd35Rules.CLASSES.first { it.name == "Bardo" }.features
    val expected = mapOf(
      1 to listOf("Música de Bardo", "Conhecimento de Bardo", "Contramúsica", "Fascinar", "Inspirar Coragem +1"),
      3 to listOf("Inspirar Competência"),
      6 to listOf("Sugestão"),
      8 to listOf("Inspirar Coragem +2"),
      9 to listOf("Inspirar Grandeza"),
      12 to listOf("Canção da Liberdade"),
      14 to listOf("Inspirar Coragem +3"),
      15 to listOf("Inspirar Heroísmo"),
      20 to listOf("Inspirar Coragem +4")
    )

    expected.forEach { (level, names) ->
      names.forEach { name ->
        assertTrue("Habilidade de Bardo ausente no nível $level: $name",
          features.any { it.level == level && it.name == name })
      }
    }
  }

  @Test
  fun paladinCoreClassFeaturesFollowDnd35Progression() {
    val features = Dnd35Rules.CLASSES.first { it.name == "Paladino" }.features
    val expected = mapOf(
      1 to listOf("Aura do Bem", "Detectar o Mal", "Destruir o Mal 1/dia"),
      2 to listOf("Graça Divina", "Imposição de Mãos"),
      3 to listOf("Aura de Coragem", "Saúde Divina"),
      4 to listOf("Expulsar Mortos-Vivos", "Conjuração Divina"),
      5 to listOf("Destruir o Mal 2/dia", "Montaria Especial"),
      6 to listOf("Remover Doença 1/semana"),
      9 to listOf("Remover Doença 2/semana"),
      10 to listOf("Destruir o Mal 3/dia"),
      12 to listOf("Remover Doença 3/semana"),
      15 to listOf("Destruir o Mal 4/dia", "Remover Doença 4/semana"),
      18 to listOf("Remover Doença 5/semana"),
      20 to listOf("Destruir o Mal 5/dia")
    )

    expected.forEach { (level, names) ->
      names.forEach { name ->
        assertTrue("Habilidade de Paladino ausente no nível $level: $name",
          features.any { it.level == level && it.name == name })
      }
    }
  }

  @Test
  fun monkCoreClassFeaturesAppearAtCorrectLevels() {
    val features = Dnd35Rules.CLASSES.first { it.name == "Monge" }.features
    val expected = mapOf(
      1 to "Bônus de CA sem Armadura",
      2 to "Evasão",
      3 to "Mente Tranquila",
      4 to "Queda Lenta 6 m",
      5 to "Pureza Corporal",
      6 to "Queda Lenta 9 m",
      7 to "Plenitude Corporal",
      8 to "Queda Lenta 12 m",
      9 to "Evasão Aprimorada",
      10 to "Queda Lenta 15 m",
      11 to "Corpo de Diamante",
      12 to "Queda Lenta 18 m",
      13 to "Alma de Diamante",
      14 to "Queda Lenta 21 m",
      15 to "Palma Trêmula",
      16 to "Queda Lenta 24 m",
      17 to "Corpo Atemporal",
      18 to "Queda Lenta 27 m",
      19 to "Corpo Vazio",
      20 to "Autoperfeição"
    )

    expected.forEach { (level, name) ->
      assertTrue("Habilidade de Monge ausente no nível $level: $name",
        features.any { it.level == level && it.name == name })
    }
    assertTrue(features.any { it.level == 17 && it.name == "Língua do Sol e da Lua" })
    assertTrue(features.any { it.level == 20 && it.name == "Queda Lenta Qualquer Distância" })
  }

  @Test
  fun rogueSneakAttackProgressionIncreasesEveryOddLevel() {
    val features = Dnd35Rules.CLASSES.first { it.name == "Ladino" }.features
    val expected = (1..19 step 2).associateWith { level ->
      "Ataque Furtivo +${(level + 1) / 2}d6"
    }

    expected.forEach { (level, name) ->
      assertTrue("Ataque furtivo ausente no nível $level: $name",
        features.any { it.level == level && it.name == name })
    }
  }

  @Test
  fun rogueSpecialAbilitiesAreGrantedAtCorrectLevels() {
    val features = Dnd35Rules.CLASSES.first { it.name == "Ladino" }.features
    listOf(10, 13, 16, 19).forEach { level ->
      assertTrue("Habilidade especial de ladino ausente no nível $level",
        features.any { it.level == level && it.name == "Habilidade Especial (${level}º Nv)" })
    }
  }

  @Test
  fun barbarianRageAndDamageReductionProgressionIncludesCoreMilestones() {
    val features = Dnd35Rules.CLASSES.first { it.name == "Bárbaro" }.features
    val expected = mapOf(
      1 to "Fúria 1/dia",
      4 to "Fúria 2/dia",
      7 to "Redução de Dano 1/-",
      8 to "Fúria 3/dia",
      10 to "Redução de Dano 2/-",
      11 to "Fúria Maior",
      12 to "Fúria 4/dia",
      13 to "Redução de Dano 3/-",
      16 to "Fúria 5/dia e Redução de Dano 4/-",
      17 to "Fúria Incansável",
      19 to "Redução de Dano 5/-",
      20 to "Fúria Poderosa e Fúria 6/dia"
    )

    expected.forEach { (level, name) ->
      assertTrue("Habilidade de Bárbaro ausente no nível $level: $name",
        features.any { it.level == level && it.name == name })
    }
  }

  @Test
  fun druidWildShapeProgressionIncludesCoreMilestones() {
    val features = Dnd35Rules.CLASSES.first { it.name == "Druida" }.features
    val expected = mapOf(
      5 to "Forma Selvagem 1/dia",
      6 to "Forma Selvagem 2/dia",
      7 to "Forma Selvagem 3/dia",
      8 to "Forma Selvagem (Grande)",
      9 to "Imunidade a Venenos",
      10 to "Forma Selvagem 4/dia",
      11 to "Forma Selvagem (Miúdo)",
      12 to "Forma Selvagem (Planta)",
      13 to "Mil Faces",
      14 to "Forma Selvagem 5/dia",
      15 to "Corpo Atemporal e Forma Selvagem (Enorme)",
      16 to "Forma Selvagem Elemental 1/dia",
      18 to "Forma Selvagem 6/dia e Elemental 2/dia",
      20 to "Forma Selvagem Elemental 3/dia e Elemental Enorme"
    )

    expected.forEach { (level, name) ->
      assertTrue("Habilidade de Druida ausente no nível $level: $name",
        features.any { it.level == level && it.name == name })
    }
  }

  @Test
  fun monkDiamondBodyIsGrantedAtLevelEleven() {
    val monk = Dnd35Rules.CLASSES.first { it.name == "Monge" }
    assertTrue(monk.features.any { it.level == 11 && it.name == "Corpo de Diamante" })
    assertTrue(monk.features.none { it.name == "Corpo Trêmulo" })
  }

  @Test
  fun remainingCoreClassSkillsMatchExpectedLists() {
    val expectedByClass = mapOf(
      "Bárbaro" to setOf(
        "Adestrar Animais", "Cavalgar", "Escalar", "Intimidação", "Natação",
        "Ofícios", "Ouvir", "Saltar", "Sobrevivência"
      ),
      "Clérigo" to setOf(
        "Concentração", "Conhecimento (Arcano)", "Conhecimento (História)",
        "Conhecimento (Planos)", "Conhecimento (Religião)", "Cura", "Diplomacia",
        "Identificar Magia", "Ofícios", "Profissão"
      ),
      "Druida" to setOf(
        "Adestrar Animais", "Cavalgar", "Concentração", "Conhecimento (Natureza)",
        "Cura", "Diplomacia", "Identificar Magia", "Natação", "Observar", "Ofícios",
        "Ouvir", "Profissão", "Sobrevivência"
      ),
      "Guerreiro" to setOf(
        "Adestrar Animais", "Cavalgar", "Escalar", "Intimidação", "Natação",
        "Ofícios", "Saltar"
      ),
      "Paladino" to setOf(
        "Adestrar Animais", "Cavalgar", "Concentração",
        "Conhecimento (Nobreza e Realeza)", "Conhecimento (Religião)", "Cura",
        "Diplomacia", "Ofícios", "Profissão", "Sentir Motivação"
      ),
      "Ranger" to setOf(
        "Adestrar Animais", "Cavalgar", "Concentração", "Conhecimento (Geografia)",
        "Conhecimento (Masmorras)", "Conhecimento (Natureza)", "Cura", "Escalar",
        "Esconder-se", "Furtividade", "Natação", "Observar", "Ofícios", "Ouvir",
        "Procurar", "Profissão", "Saltar", "Sobrevivência", "Usar Cordas"
      ),
      "Ladino" to setOf(
        "Abrir Fechaduras", "Acrobacia", "Arte da Fuga", "Atuação", "Avaliação",
        "Blefar", "Decifrar Escrita", "Diplomacia", "Disfarces", "Equilíbrio",
        "Escalar", "Esconder-se", "Falsificação", "Furtividade", "Intimidação",
        "Natação", "Observar", "Obter Informação", "Ofícios", "Operar Mecanismo",
        "Ouvir", "Prestidigitação", "Procurar", "Saltar", "Sentir Motivação",
        "Usar Cordas", "Usar Instrumento Mágico"
      ),
      "Mago" to setOf(
        "Concentração", "Conhecimento (Arcano)",
        "Conhecimento (Arquitetura e Engenharia)", "Conhecimento (Geografia)",
        "Conhecimento (História)", "Conhecimento (Local)", "Conhecimento (Masmorras)",
        "Conhecimento (Natureza)", "Conhecimento (Nobreza e Realeza)",
        "Conhecimento (Planos)", "Conhecimento (Religião)", "Decifrar Escrita",
        "Identificar Magia", "Ofícios", "Profissão"
      )
    )

    expectedByClass.forEach { (className, expected) ->
      val actual = Dnd35Rules.CLASSES.first { it.name == className }.classSkills.toSet()
      assertEquals("Lista de perícias de classe incorreta: $className", expected, actual)
    }
  }

  @Test
  fun skillTrainingRequirementsMatchCoreRules() {
    val definitions = Dnd35Rules.SKILL_DEFINITIONS.associateBy { it.name }
    val trainedOnly = setOf(
      "Abrir Fechaduras", "Adestrar Animais",
      "Conhecimento (Arcano)", "Conhecimento (Arquitetura e Engenharia)",
      "Conhecimento (Geografia)", "Conhecimento (História)", "Conhecimento (Local)",
      "Conhecimento (Masmorras)", "Conhecimento (Natureza)",
      "Conhecimento (Nobreza e Realeza)", "Conhecimento (Planos)",
      "Conhecimento (Religião)", "Decifrar Escrita", "Identificar Magia",
      "Operar Mecanismo", "Prestidigitação", "Profissão", "Usar Instrumento Mágico"
    )
    assertEquals(trainedOnly, definitions.values.filter { it.trainedOnly }.map { it.name }.toSet())
    assertTrue(definitions["Acrobacia"]?.trainedOnly == false)
  }

  @Test
  fun armorCheckPenaltyAppliesToCorePhysicalSkills() {
    val definitions = Dnd35Rules.SKILL_DEFINITIONS.associateBy { it.name }
    val expected = setOf(
      "Acrobacia", "Arte da Fuga", "Equilíbrio", "Escalar", "Esconder-se",
      "Furtividade", "Natação", "Prestidigitação", "Saltar"
    )
    assertEquals(expected, definitions.values.filter { it.armorCheckPenaltyApplies }.map { it.name }.toSet())
  }


  @Test
  fun languageListSerializationTrimsDeduplicatesAndRoundTrips() {
    val encoded = CharacterJsonAdapters.serializeLanguages(
      listOf(" Comum ", "Élfico", "comum", "", "Anão")
    )

    assertEquals(listOf("Comum", "Élfico", "Anão"), CharacterJsonAdapters.deserializeLanguages(encoded))
  }

  @Test
  fun missingLanguageDataRemainsEmptyForLegacyCharacters() {
    assertEquals(emptyList<String>(), CharacterJsonAdapters.deserializeLanguages(""))
    assertEquals(emptyList<String>(), CharacterJsonAdapters.deserializeLanguages(null))
  }


  @Test
  fun skillPointHistorySerializationRoundTripsByLevelAndSkill() {
    val history = listOf(
      SkillPointLevelRecord(
        characterLevel = 2,
        className = "Bardo",
        availablePoints = 7f,
        allocations = listOf(
          SkillPointAllocation("Atuação", 2f),
          SkillPointAllocation("Ouvir", 1f)
        )
      ),
      SkillPointLevelRecord(characterLevel = 1, className = "Bardo", availablePoints = 28f)
    )

    val decoded = CharacterJsonAdapters.deserializeSkillPointHistory(
      CharacterJsonAdapters.serializeSkillPointHistory(history)
    )

    assertEquals(1, decoded.first().characterLevel)
    assertEquals("Bardo", decoded.first().className)
    assertEquals(7f, decoded.last().availablePoints)
    assertEquals(listOf("Atuação", "Ouvir"), decoded.last().allocations.map { it.skillName })
    assertEquals(listOf(2f, 1f), decoded.last().allocations.map { it.pointsSpent })
  }

  @Test
  fun missingSkillPointHistoryRemainsEmptyForLegacyCharacters() {
    assertEquals(emptyList<SkillPointLevelRecord>(), CharacterJsonAdapters.deserializeSkillPointHistory(null))
    assertEquals(emptyList<SkillPointLevelRecord>(), CharacterJsonAdapters.deserializeSkillPointHistory(""))
    assertEquals(emptyList<SkillPointLevelRecord>(), CharacterJsonAdapters.deserializeSkillPointHistory("not-json"))
  }

  @Test
  fun skillPointHistoryRejectsInvalidRecordsAndAllocations() {
    val json = """[
      {"characterLevel":0,"className":"Bardo","availablePoints":5},
      {"characterLevel":1,"className":"Bardo","availablePoints":6,"allocations":[
        {"skillName":"Atuação","pointsSpent":2},
        {"skillName":"","pointsSpent":3},
        {"skillName":"Ouvir","pointsSpent":-1}
      ]}
    ]"""
    val decoded = CharacterJsonAdapters.deserializeSkillPointHistory(json)
    assertEquals(1, decoded.size)
    assertEquals(listOf(SkillPointAllocation("Atuação", 2f)), decoded.single().allocations)
  }


  @Test
  fun woodElfDescriptionIncludesInheritedElfTraits() {
    val traits = Dnd35Rules.RACES.first { it.name == "Elfo da Floresta" }.traits
    assertTrue(traits.contains("imunidade a sono mágico"))
    assertTrue(traits.contains("+2 contra encantamentos"))
    assertTrue(traits.contains("+2 em Ouvir, Procurar e Observar"))
    assertTrue(traits.contains("portas secretas automaticamente a 1,5m"))
    assertTrue(traits.contains("espada longa, rapieira"))
  }

  @Test
  fun racialTraitDescriptionsMatchCalculatedSkillBonuses() {
    val races = Dnd35Rules.RACES.associateBy { it.name }
    assertTrue(races.getValue("Elfo").traits.contains("incluindo versões compostas"))

    assertTrue(races.getValue("Meio-Elfo").traits.contains("+2 em Obter Informação e Diplomacia"))
    assertTrue(races.getValue("Meio-Elfo").traits.contains("Visão na penumbra"))
    assertTrue(races.getValue("Meio-Elfo").traits.contains("+1 em Ouvir, Procurar e Observar"))
    assertTrue(races.getValue("Meio-Elfo").traits.contains("Imunidade a sono mágico"))
    assertTrue(races.getValue("Meio-Elfo").traits.contains("+2 nos testes de resistência contra encantamentos"))
    assertTrue(races.getValue("Gnomo").traits.contains("+4 Esconder-se"))
    assertTrue(races.getValue("Halfling").traits.contains("+4 Esconder-se"))
    val drowTraits = races.getValue("Drow (Elfo Negro)").traits
    assertTrue(drowTraits.contains("Imunidade a sono mágico"))
    assertTrue(drowTraits.contains("visão no escuro 36m"))
    assertTrue(drowTraits.contains("+2 nos testes de resistência contra magias e habilidades similares a magia"))
    assertTrue(drowTraits.contains("+2 em Ouvir, Procurar e Observar"))
    assertTrue(drowTraits.contains("portas secretas automaticamente a 1,5m"))
    assertTrue(drowTraits.contains("besta de mão, rapieira e espada curta"))
    assertTrue(drowTraits.contains("resistência à magia (11 + nível de personagem)"))
    assertTrue(drowTraits.contains("habilidades similares a magia 1/dia"))
    assertTrue(drowTraits.contains("cegueira à luz intensa"))
    assertTrue(!drowTraits.contains("visão na penumbra"))
    assertTrue(!drowTraits.contains("arco longo"))
  }
  @Test
  fun dwarfRacialDescriptionsIncludeCoreTraitsForBothSubraces() {
    val races = Dnd35Rules.RACES.associateBy { it.name }
    val hillTraits = races.getValue("Anão").traits
    val mountain = races.getValue("Anão da Montanha")
    val mountainTraits = mountain.traits

    assertTrue(hillTraits.contains("Visão no escuro 18m"))
    assertTrue(hillTraits.contains("+2 em testes de resistência contra veneno"))
    assertTrue(hillTraits.contains("magias e habilidades similares a magia"))
    assertTrue(hillTraits.contains("+2 em Procurar para construções incomuns de pedra"))
    assertTrue(hillTraits.contains("+4 contra encontrão e derrubar"))
    assertTrue(hillTraits.contains("+1 nas jogadas de ataque contra orcs e goblinoides"))
    assertTrue(hillTraits.contains("+4 de esquiva na CA contra gigantes"))
    assertTrue(hillTraits.contains("machado de guerra anão e urgrosh anão"))
    assertTrue(mountainTraits.contains("Mesmos traços raciais do Anão da Colina"))
    assertEquals(0, mountain.strMod)
    assertEquals(2, mountain.conMod)
    assertEquals(-2, mountain.chaMod)
  }
  @Test
  fun halfOrcRacialDescriptionMatchesCoreTraits() {
    val halfOrc = Dnd35Rules.RACES.first { it.name == "Meio-Orc" }
    assertTrue(halfOrc.traits.contains("Visão no escuro 18m"))
    assertTrue(halfOrc.traits.contains("sangue orc"))
    assertTrue(!halfOrc.traits.contains("força física devastadora"))
    assertEquals(2, halfOrc.strMod)
    assertEquals(-2, halfOrc.intMod)
    assertEquals(-2, halfOrc.chaMod)
  }

  @Test
  fun gnomeRacialDescriptionIncludesCoreTraits() {
    val traits = Dnd35Rules.RACES.first { it.name == "Gnomo" }.traits
    assertTrue(traits.contains("visão na penumbra"))
    assertTrue(traits.contains("+2 nos testes de resistência contra ilusões"))
    assertTrue(traits.contains("+1 na CD das ilusões conjuradas"))
    assertTrue(traits.contains("kobolds e goblinoides"))
    assertTrue(traits.contains("+4 de bônus de esquiva na CA contra gigantes"))
    assertTrue(traits.contains("+2 em Ouvir e Ofícios (Alquimia)"))
    assertTrue(traits.contains("martelo-gancho gnômico como arma marcial"))
    assertTrue(traits.contains("Prestidigitação"))
    assertTrue(traits.contains("mamíferos escavadores"))
  }


  @Test
  fun monkUnarmedDamageRespectsCreatureSize() {
    fun damage(level: Int, size: String) = CharacterCalculator.compute(
      CharacterEntity(
        characterClass = "Monge",
        level = level,
        size = size,
        goldGp = 0f
      )
    ).monkUnarmedDamage

    assertEquals("1d4", damage(1, "Pequeno"))
    assertEquals("1d6", damage(1, "Médio"))
    assertEquals("1d8", damage(1, "Grande"))
    assertEquals("2d8", damage(20, "Pequeno"))
    assertEquals("2d10", damage(20, "Médio"))
    assertEquals("4d8", damage(20, "Grande"))
  }
}
