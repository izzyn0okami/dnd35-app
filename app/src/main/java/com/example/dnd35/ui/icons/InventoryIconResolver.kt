package com.example.dnd35.ui.icons

import com.example.dnd35.model.CharacterInventoryItem
import com.example.dnd35.model.Dnd35Rules

/**
 * Ícone específico de um item, na ordem:
 *  1. o ícone escolhido para o item ([iconKey]), se a chave existir neste app;
 *  2. o ícone padrão do item de mesmo nome no catálogo (cobre fichas antigas, sem chave gravada);
 *  3. nada: quem desenha usa o ícone genérico da categoria.
 *
 * Uma chave desconhecida (ex.: backup de uma versão mais nova do app) cai no passo 2 em vez de falhar.
 * A foto personalizada do item (imageUri) é tratada antes, por quem desenha o card.
 */
fun resolveGameIcon(iconKey: String?, itemName: String): GameIcon? =
    GameIcons.find(iconKey) ?: GameIcons.find(Dnd35Rules.presetIconKeyFor(itemName))

fun resolveGameIcon(item: CharacterInventoryItem): GameIcon? = resolveGameIcon(item.iconKey, item.name)
