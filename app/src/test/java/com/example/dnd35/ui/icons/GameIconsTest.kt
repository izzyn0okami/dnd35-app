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
