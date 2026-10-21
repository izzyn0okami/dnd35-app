package com.example

import com.example.dnd35.model.CharacterCalculator
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.CharacterInventoryItem
import com.example.dnd35.model.CharacterJsonAdapters
import com.example.dnd35.model.CharacterSkill
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
  fun monkDiamondBodyIsGrantedAtLevelEleven() {
    val monk = Dnd35Rules.CLASSES.first { it.name == "Monge" }
    assertTrue(monk.features.any { it.level == 11 && it.name == "Corpo de Diamante" })
    assertTrue(monk.features.none { it.name == "Corpo Trêmulo" })
  }

}
