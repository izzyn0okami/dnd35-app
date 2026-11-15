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
    fun blankSearchReturnsNoResults() {
        assertTrue(FeatGlossarySearch.search("   ").isEmpty())
    }

    @Test
    fun resultsIncludePlayerHandbookReference() {
        val result = FeatGlossarySearch.search("Foco em Arma").first()
        assertTrue(result.source.contains("Livro do Jogador"))
        assertFalse(result.benefit.isBlank())
    }
}
