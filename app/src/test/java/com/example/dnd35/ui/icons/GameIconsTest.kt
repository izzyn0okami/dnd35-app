package com.example.dnd35.ui.icons

import com.example.dnd35.model.CharacterInventoryItem
import com.example.dnd35.model.CharacterJsonAdapters
import com.example.dnd35.model.Dnd35Rules
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class GameIconsTest {

    @Test
    fun everyCatalogItemHasAnExistingIcon() {
        val missing = Dnd35Rules.PRESET_ITEMS.filter { GameIcons.find(it.iconKey) == null }.map { it.name }
        assertTrue("Itens sem ícone válido: $missing", missing.isEmpty())
    }

    @Test
    fun everySpellSchoolHasARegisteredIcon() {
        val schools = listOf(
            "Abjuração", "Adivinhação", "Conjuração", "Encantamento",
            "Evocação", "Ilusão", "Necromancia", "Transmutação", "Universal"
        )
        schools.forEach { school ->
            assertNotNull("$school deve ter ícone", spellSchoolIcon(school))
            assertNotNull("$school deve apontar para ícone registrado", GameIcons.find(spellSchoolIcon(school)))
        }
    }

    @Test
    fun specificSpellIconsAreRegisteredAndFallbackUsesSchoolIcon() {
        val expected = mapOf(
            "Mãos Flamejantes" to "fire-bottle",
            "Bola de Fogo" to "fire-bottle",
            "Raio de Ácido" to "acid",
            "Sono" to "person-in-bed",
            "Teia" to "fishing-net",
            "Curar Ferimentos Leves" to "health-potion",
            "Dedo da Morte" to "skull-crossed-bones",
            "Relâmpago" to "thunderball",
            "Teletransporte" to "wingfoot",
            "Detectar Magia" to "crystal-ball",
            "Grito Maior" to "lyre",
            "Nuvem Incendiária" to "fire-bottle",
            "Invocar Criaturas VII" to "magic-lamp",
            "Labirinto" to "crossed-chains",
            "Mãos Mágicas" to "orb-wand",
            "Mansão Magnífica de Mordenkainen" to "camping-tent",
            "Punho Cerrado de Bigby, O" to "mailed-fist"

            "Prestidigitação" to "fairy-wand",
            "Marca Arcana" to "wax-seal",
            "Virtude" to "heart-bottle",
            "Queda Suave" to "wingfoot",
            "Pedra Encantada" to "marbles",
            "Área Escorregadia" to "oil-can",
            "Remover Medo" to "magic-shield",
            "Aura Mágica de Nystul" to "mirror-mirror",
            "Recuo Acelerado" to "wingfoot",
            "Invisibilidade Contra Animais" to "mirror-mirror",
            "Invisibilidade Contra Mortos-Vivos" to "mirror-mirror",
            "Reduzir Animal" to "fairy-wand",
            "Ampliar Animais" to "hound",
            "Caminhar em Árvores" to "herbs-bundle",
            "Comunhão Com a Natureza" to "herbs-bundle",
            "Dificultar Detecção" to "magic-shield",
            "Movimentação Livre" to "wingfoot",
            "Raio Polar" to "crystal-wand",
            "Aliado Extra-Planar Maior" to "magic-lamp",
            "Refugiar Itens" to "mirror-mirror",
            "Visão" to "crystal-ball"
        )
        expected.forEach { (spell, key) ->
            assertEquals(key, spellIconKey(spell))
            assertNotNull("Ícone $key de $spell deve estar registrado", GameIcons.find(key))
        }
        assertNull(spellIconKey("Magia sem associação específica"))
        val catalogNames = Dnd35Rules.PRESET_SPELLS.map { it.name.trim().lowercase(java.util.Locale.ROOT) }.toSet()
        val invalidNames = spellIconOverrideNames.filterNot { it in catalogNames }
        assertTrue("Associações sem magia correspondente no catálogo: $invalidNames", invalidNames.isEmpty())
        val invalidIcons = Dnd35Rules.PRESET_SPELLS.mapNotNull { spell ->
            val key = spellIconKey(spell.name)
            if (key != null && GameIcons.find(key) == null) "${spell.name}: $key" else null
        }
        assertTrue("Magias apontando para ícones inexistentes: $invalidIcons", invalidIcons.isEmpty())
        Dnd35Rules.PRESET_SPELLS.forEach { spell ->
            val key = spellIconKey(spell.name) ?: spellSchoolIcon(spell.school)
            assertNotNull("Ícone ausente para ${spell.name}", GameIcons.find(key))
        }
    }

    @Test
    fun spellsWithoutUnambiguousExistingSymbolsKeepSchoolFallback() {
        val fallbackOnly = setOf(
            "Orientação",
            "Intuir Direção",
            "Servo Invisível",
            "Presa Mágica",
            "Disco Flutuante de Tenser",
            "Apagar",
            "Sabedoria da Coruja",
            "Presa Mágica Maior",
            "Repelir Insetos",
            "Destruição Rastejante"
        )
        val catalogFallbacks = Dnd35Rules.PRESET_SPELLS
            .filter { spellIconKey(it.name) == null }
            .map { it.name }
            .toSet()
        assertEquals(fallbackOnly, catalogFallbacks)
        Dnd35Rules.PRESET_SPELLS.filter { it.name in fallbackOnly }.forEach { spell ->
            assertNotNull(GameIcons.find(spellSchoolIcon(spell.school)))
        }
    }

    @Test
    fun iconKeysAreUnique() {
        val keys = GameIcons.all.map { it.key }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun drawableFilesMatchRegistry() {
        val dir = File("src/main/res/drawable")
        val files = dir.listFiles { f -> f.name.startsWith("gi_") && f.extension == "xml" }
            ?.map { it.nameWithoutExtension }?.toSet().orEmpty()
        val expected = GameIcons.all.map { "gi_" + it.key.replace('-', '_') }.toSet()
        assertEquals(expected, files)
    }

    @Test
    fun creditsCoverEveryAuthorInRegistry() {
        val credited = GameIconCredits.authors.map { it.name }.toSet()
        val used = GameIcons.all.map { it.authorName }.toSet()
        assertTrue("Sem crédito: ${used - credited}", credited.containsAll(used))
    }

    @Test
    fun resolverPrefersExplicitKeyThenCatalogNameThenNull() {
        val preset = Dnd35Rules.PRESET_ITEMS.first()
        val other = GameIcons.all.first { it.key != preset.iconKey }
        assertEquals(preset.iconKey, resolveGameIcon(null, preset.name)?.key)
        assertEquals(other.key, resolveGameIcon(other.key, preset.name)?.key)
        assertEquals(preset.iconKey, resolveGameIcon("chave-inexistente", preset.name)?.key)
        assertNull(resolveGameIcon(null, "Item totalmente inventado xyz"))
    }

    @Test
    fun iconKeySurvivesJsonRoundTripAndOldJsonStillLoads() {
        val items = listOf(
            CharacterInventoryItem(name = "Custom", category = "Armas", iconKey = "some-key"),
            CharacterInventoryItem(name = "Sem ícone", category = "Armas")
        )
        val back = CharacterJsonAdapters.deserializeInventory(CharacterJsonAdapters.serializeInventory(items))
        assertEquals("some-key", back[0].iconKey)
        assertNull(back[1].iconKey)

        val old = """[{"id":"a","name":"Velho","category":"Armas","quantity":1}]"""
        val oldItem = CharacterJsonAdapters.deserializeInventory(old).single()
        assertNotNull(oldItem)
        assertNull(oldItem.iconKey)
    }
}
