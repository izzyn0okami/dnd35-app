package com.example

import com.example.dnd35.model.Dnd35Rules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CraftSpecialtyClassificationTest {

    @Test
    fun craftSpecialtyMatchesGenericCraftClassSkill() {
        assertTrue(Dnd35Rules.isCraftSpecialty("Ofícios (Armadilhas)"))
        assertTrue(Dnd35Rules.classSkillMatches("Ofícios (Armadilhas)", "Ofícios"))
        assertFalse(Dnd35Rules.classSkillMatches("Ofícios (Armadilhas)", "Furtividade"))
    }

    @Test
    fun craftSpecialtyUsesIntelligenceEvenWhenFallbackIsDifferent() {
        assertEquals("INT", Dnd35Rules.canonicalSkillKeyAbility("Ofícios (Armadilhas)", "FOR"))
    }

    @Test
    fun nonCraftCustomSkillKeepsItsFallbackKeyAbility() {
        assertEquals("FOR", Dnd35Rules.canonicalSkillKeyAbility("Perícia personalizada", "FOR"))
        assertFalse(Dnd35Rules.isCraftSpecialty("Ofícios ()"))
    }
}
