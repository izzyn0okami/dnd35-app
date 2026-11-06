package com.example.dnd35.session

/**
 * Estado de combate da sessão: quem está na iniciativa, de quem é a vez e o PV/condições de
 * cada combatente. É estado global — o Mestre é a autoridade (igual ao resto da sessão) e toda
 * mudança é sincronizada para todos os aparelhos conectados.
 *
 * `id` de um combatente é o id do participante (jogador conectado) ou, para um NPC do Mestre
 * (que não tem aparelho conectado), `"npc:<characterId>"`.
 */
data class Combatant(
    val id: String,
    val displayName: String,
    val isNpc: Boolean,
    val initiative: Int,
    val currentHp: Int,
    val maxHp: Int,
    val tempHp: Int = 0,
    val conditions: List<String> = emptyList()
) {
    fun withDamage(amount: Int): Combatant {
        if (amount == 0) return this
        if (amount > 0) {
            // Dano: primeiro consome o PV temporário, só depois o PV real.
            val remaining = (amount - tempHp).coerceAtLeast(0)
            val newTemp = (tempHp - amount).coerceAtLeast(0)
            val newHp = (currentHp - remaining).coerceAtLeast(-999)
            return copy(currentHp = newHp, tempHp = newTemp)
        }
        // Cura: nunca passa do máximo, nunca repõe PV temporário.
        val healed = (currentHp - amount).coerceAtMost(maxHp)
        return copy(currentHp = healed)
    }
}

data class CombatState(
    /** Sempre ordenado da maior para a menor iniciativa. */
    val combatants: List<Combatant>,
    val currentTurnIndex: Int = 0,
    val round: Int = 1
) {
    val currentCombatantId: String?
        get() = combatants.getOrNull(currentTurnIndex)?.takeIf { it.currentHp > DEAD_HP_THRESHOLD }?.id

    /**
     * Avança para o próximo combatente vivo. Pelo Livro do Jogador, personagens com 0 PV
     * estão incapacitados e ainda podem agir; entre -1 e -9 PV estão morrendo, mas ainda vivos.
     * Apenas personagens com -10 PV ou menos são considerados mortos e ignorados na iniciativa.
     */
    fun nextTurn(): CombatState {
        if (combatants.isEmpty()) return copy(currentTurnIndex = -1)
        val livingIndices = combatants.indices.filter { combatants[it].currentHp > DEAD_HP_THRESHOLD }
        if (livingIndices.isEmpty()) return copy(currentTurnIndex = -1)

        val nextIndex = livingIndices.firstOrNull { it > currentTurnIndex } ?: livingIndices.first()
        val wrapped = currentTurnIndex >= 0 && nextIndex <= currentTurnIndex
        return copy(currentTurnIndex = nextIndex, round = if (wrapped) round + 1 else round)
    }

    companion object {
        const val NPC_ID_PREFIX = "npc:"
        const val DEAD_HP_THRESHOLD = -10

        fun npcCombatantId(characterId: Long): String = "$NPC_ID_PREFIX$characterId"

        /** Constrói o estado inicial já ordenado por iniciativa (maior primeiro). */
        fun start(combatants: List<Combatant>): CombatState =
            CombatState(combatants = combatants.sortedByDescending { it.initiative }, currentTurnIndex = 0, round = 1)
    }
}
