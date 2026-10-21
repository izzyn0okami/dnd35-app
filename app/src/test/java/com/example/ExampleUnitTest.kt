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
  fun stoutHalflingReceivesOnlyItsListenRacialSkillBonus() {
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
    assertEquals(0, bonuses.getValue("Escalar"))
    assertEquals(0, bonuses.getValue("Saltar"))
    assertEquals(2, bonuses.getValue("Ouvir"))
    assertEquals(0, bonuses.getValue("Furtividade"))
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
    assertEquals(6.03f, stats.speedMetersEffective, 0.02f)
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
  fun paladinHasPoorWillSaveProgression() {
    val paladin = Dnd35Rules.CLASSES.first { it.name == "Paladino" }
    assertEquals(Dnd35Rules.SaveProgression.POOR, paladin.willType)
    val stats = CharacterCalculator.compute(
      CharacterEntity(characterClass = "Paladino", level = 1)
    )
    assertEquals(0, stats.baseWill)
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
    assertTrue(!races.getValue("Meio-Elfo").traits.contains("Imunidade a sono mágico"))
    assertTrue(!races.getValue("Meio-Elfo").traits.contains("+2 contra encantamentos"))
    assertTrue(races.getValue("Gnomo").traits.contains("+4 Esconder-se"))
    assertTrue(races.getValue("Halfling").traits.contains("+4 Esconder-se"))
    val drowTraits = races.getValue("Drow (Elfo Negro)").traits
    assertTrue(drowTraits.contains("Imunidade a sono mágico"))
    assertTrue(drowTraits.contains("+2 contra encantamentos"))
    assertTrue(drowTraits.contains("visão na penumbra"))
    assertTrue(drowTraits.contains("+2 em Ouvir, Procurar e Observar"))
    assertTrue(drowTraits.contains("portas secretas automaticamente a 1,5m"))
    assertTrue(drowTraits.contains("espada longa, rapieira, arco longo e arco curto"))
    assertTrue(drowTraits.contains("incluindo versões compostas"))
  }
}
