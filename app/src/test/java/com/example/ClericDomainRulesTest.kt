package com.example

import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.CharacterJsonAdapters
import com.example.dnd35.model.CharacterSpell
import com.example.dnd35.model.ClericDomainRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Robolectric: os adaptadores JSON usam org.json e android.util.Log.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ClericDomainRulesTest {

    @Test
    fun normalizeKeepsOnlyValidDistinctDomainsUpToTwo() {
        assertEquals(listOf("Guerra", "Sol"), ClericDomainRules.normalize(listOf("guerra", " Sol ", "Guerra", "Fogo")))
        assertEquals(listOf("Fogo"), ClericDomainRules.normalize(listOf("Inexistente", "fogo")))
        assertEquals(emptyList<String>(), ClericDomainRules.normalize(emptyList()))
    }

    @Test
    fun domainsRoundTripThroughCharacterJson() {
        val json = CharacterJsonAdapters.serializeDomains(listOf("Guerra", "Sol"))
        val character = CharacterEntity(clericDomainsJson = json)
        assertEquals(listOf("Guerra", "Sol"), ClericDomainRules.domainsOf(character))
        assertEquals("", CharacterJsonAdapters.serializeDomains(emptyList()))
        assertEquals(emptyList<String>(), ClericDomainRules.domainsOf(CharacterEntity()))
        assertEquals(emptyList<String>(), CharacterJsonAdapters.deserializeDomains("não é json"))
    }

    @Test
    fun everyDomainHasOneSpellAtEachCircleFromFirstToNinth() {
        ClericDomainRules.ALL_DOMAINS.forEach { domain ->
            (1..9).forEach { circle ->
                val spells = ClericDomainRules.domainSpellsAt(listOf(domain), circle)
                assertEquals("$domain, círculo $circle", 1, spells.size)
            }
        }
    }

    @Test
    fun domainSpellsAtListsOneOptionPerChosenDomain() {
        val options = ClericDomainRules.domainSpellsAt(listOf("Fogo", "Sol"), 2)
        assertEquals(listOf("Fogo" to "Criar Chamas", "Sol" to "Esquentar Metal"), options.map { it.first to it.second.name })
    }

    @Test
    fun spellDomainFieldRoundTripsAndOldSpellsHaveNone() {
        val json = CharacterJsonAdapters.serializeSpells(
            listOf(
                CharacterSpell(name = "Criar Chamas", level = 2, casterClass = "Clérigo", domain = "Fogo"),
                CharacterSpell(name = "Luz", level = 0)
            )
        )
        val back = CharacterJsonAdapters.deserializeSpells(json)
        assertEquals("Fogo", back[0].domain)
        assertEquals("", back[1].domain)
        assertEquals("", CharacterJsonAdapters.deserializeSpells("[{\"name\":\"Luz\"}]").single().domain)
    }

    @Test
    fun warningsFlagTwoDomainSpellsInOneCircleAndUnchosenDomains() {
        val spells = listOf(
            CharacterSpell(name = "Criar Chamas", level = 2, domain = "Fogo", prepared = true),
            CharacterSpell(name = "Esquentar Metal", level = 2, domain = "Sol", prepared = true),
            CharacterSpell(name = "Profanar", level = 2, domain = "Mal", prepared = false)
        )
        val warnings = ClericDomainRules.warnings(spells, listOf("Fogo", "Sol"))
        assertEquals(1, warnings.size)
        assertTrue(warnings.single().contains("2 magias de domínio"))
        val unchosen = ClericDomainRules.warnings(spells.take(1), listOf("Sol"))
        assertTrue(unchosen.single().contains("Criar Chamas") && unchosen.single().contains("Fogo"))
        assertTrue(ClericDomainRules.warnings(spells.take(1), listOf("Fogo")).isEmpty())
    }
}
