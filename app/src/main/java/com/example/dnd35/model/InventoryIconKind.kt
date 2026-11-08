package com.example.dnd35.model

import java.text.Normalizer

/**
 * Tipo de ícone padrão de um item do inventário.
 *
 * É sempre derivado do item em tempo de execução (hoje, só da categoria) e nunca gravado:
 * não altera o modelo, o banco, os backups (.fichadnd35/.itemdnd35) nem as mensagens da sessão
 * de mesa. A foto personalizada (imageUri) continua tendo prioridade sobre qualquer ícone.
 */
enum class InventoryIconKind {
    WEAPON,
    ARMOR,
    SHIELD,
    MAGIC,
    CONSUMABLE,
    ADVENTURE,
    GENERIC
}

private val combiningMarks = "\\p{M}+".toRegex()

/** Minúsculas e sem acentos, para "Itens Mágicos" e "itens magicos" cairem no mesmo ramo. */
private fun String.normalizedForIcon(): String =
    Normalizer.normalize(trim(), Normalizer.Form.NFD).replace(combiningMarks, "").lowercase()

fun inventoryIconKindFor(item: CharacterInventoryItem): InventoryIconKind {
    val category = item.category.normalizedForIcon()
    return when {
        isWeaponInventoryCategory(item.category) -> InventoryIconKind.WEAPON
        isArmorInventoryCategory(item.category) -> InventoryIconKind.ARMOR
        isShieldInventoryCategory(item.category) -> InventoryIconKind.SHIELD
        category.startsWith("itens magicos") || category == "item magico" -> InventoryIconKind.MAGIC
        category.startsWith("consumiv") -> InventoryIconKind.CONSUMABLE
        category.startsWith("itens de aventura") || category == "item de aventura" -> InventoryIconKind.ADVENTURE
        else -> InventoryIconKind.GENERIC
    }
}
