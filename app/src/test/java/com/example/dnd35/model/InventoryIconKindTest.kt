package com.example.dnd35.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class InventoryIconKindTest {

    private fun kindOf(category: String) =
        inventoryIconKindFor(CharacterInventoryItem(name = "Item", category = category))

    @Test
    fun `cada categoria do dialogo de item tem um tipo de icone`() {
        assertEquals(InventoryIconKind.WEAPON, kindOf("Armas"))
        assertEquals(InventoryIconKind.ARMOR, kindOf("Armaduras"))
        assertEquals(InventoryIconKind.SHIELD, kindOf("Escudos"))
        assertEquals(InventoryIconKind.MAGIC, kindOf("Itens Mágicos"))
        assertEquals(InventoryIconKind.ADVENTURE, kindOf("Itens de Aventura"))
        assertEquals(InventoryIconKind.CONSUMABLE, kindOf("Consumíveis"))
        assertEquals(InventoryIconKind.GENERIC, kindOf("Outros"))
    }

    @Test
    fun `armadura e escudo nao sao confundidos com arma`() {
        assertEquals(InventoryIconKind.ARMOR, kindOf("Armadura"))
        assertEquals(InventoryIconKind.SHIELD, kindOf("Escudo"))
        assertEquals(InventoryIconKind.WEAPON, kindOf("Arma"))
    }

    @Test
    fun `categoria ignora acentos, caixa e espacos nas pontas`() {
        assertEquals(InventoryIconKind.MAGIC, kindOf("  itens magicos "))
        assertEquals(InventoryIconKind.CONSUMABLE, kindOf("CONSUMIVEIS"))
        assertEquals(InventoryIconKind.ADVENTURE, kindOf("itens de aventura"))
    }

    @Test
    fun `categoria desconhecida ou vazia cai no icone generico`() {
        assertEquals(InventoryIconKind.GENERIC, kindOf(""))
        assertEquals(InventoryIconKind.GENERIC, kindOf("Categoria inventada pelo usuario"))
    }

    @Test
    fun `todo item do catalogo embutido resulta em tipo especifico`() {
        val semIcone = Dnd35Rules.PRESET_ITEMS
            .filter { kindOf(it.category) == InventoryIconKind.GENERIC }
            .map { "${it.name} (${it.category})" }
        assertTrue("Itens do catálogo sem ícone específico: $semIcone", semIcone.isEmpty())
    }
}
