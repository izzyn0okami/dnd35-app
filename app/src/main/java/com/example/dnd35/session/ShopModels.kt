package com.example.dnd35.session

import com.example.dnd35.model.CharacterInventoryItem

/**
 * Loja aberta pelo Mestre a partir do inventário de um NPC seu (ver [SessionServer.openShop]):
 * [items] é uma cópia do inventário daquele NPC no momento em que a loja foi aberta ou
 * atualizada, com o preço de cada item vindo do próprio `costGp`. Comprar não mexe na ficha real
 * do NPC — é só uma vitrine da sessão, como o baú de loot.
 */
data class ShopState(
    val npcCharacterId: Long,
    val npcName: String,
    val items: List<CharacterInventoryItem>
)

/** O que o Jogador vê da loja aberta — sem o id interno do NPC, que não importa para ele. */
data class ShopSnapshot(
    val npcName: String,
    val items: List<CharacterInventoryItem>
)
