package com.example.dnd35.session

/**
 * Tipos de pedido que um Jogador pode mandar ao Mestre para aprovação. O Mestre é a autoridade
 * da sessão — toda mudança iniciada por um Jogador passa por aqui, nunca muta o estado direto
 * (diferente das ações do próprio Mestre, que mutam na hora, ver [SessionServer]).
 */
enum class RequestKind {
    /** Jogador manda uma versão atualizada da própria ficha (ex.: ganhou um item, gastou ouro). */
    CHARACTER_UPDATE,

    /** Jogador pede uma mudança de PV num combate ativo (ex.: usar uma poção, sofrer dano). */
    COMBAT_HP_CHANGE,

    /** Jogador pede para pegar um item do baú de loot compartilhado da sessão. */
    TAKE_LOOT
}

/**
 * Pedido pendente de aprovação do Mestre. [summary] é o texto mostrado ao Mestre para decidir;
 * os demais campos variam de acordo com [kind] e só são usados se aprovado.
 */
data class ActionRequest(
    val id: String,
    val requesterId: String,
    val requesterName: String,
    val kind: RequestKind,
    val summary: String,
    val characterJson: String? = null,
    val combatantId: String? = null,
    val hpDelta: Int? = null,
    /** Id do item do baú de loot (dentro de [SessionServer.lootChest]) que o jogador quer pegar. */
    val lootItemId: String? = null
)
