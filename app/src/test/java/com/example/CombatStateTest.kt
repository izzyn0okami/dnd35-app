package com.example

import com.example.dnd35.session.Combatant
import com.example.dnd35.session.CombatState
import org.junit.Assert.assertEquals
import org.junit.Test

class CombatStateTest {

    private fun combatant(
        id: String = "p1",
        currentHp: Int = 20,
        maxHp: Int = 20,
        tempHp: Int = 0
    ) = Combatant(
        id = id,
        displayName = "Teste",
        isNpc = false,
        initiative = 10,
        currentHp = currentHp,
        maxHp = maxHp,
        tempHp = tempHp
    )

    @Test
    fun `dano consome PV temporario antes do PV real`() {
        val c = combatant(currentHp = 20, maxHp = 20, tempHp = 5)
        val result = c.withDamage(8)
        // 5 de PV temp absorvem primeiro, sobra 3 de dano real.
        assertEquals(0, result.tempHp)
        assertEquals(17, result.currentHp)
    }

    @Test
    fun `dano menor que o PV temporario nao afeta o PV real`() {
        val c = combatant(currentHp = 20, maxHp = 20, tempHp = 10)
        val result = c.withDamage(4)
        assertEquals(6, result.tempHp)
        assertEquals(20, result.currentHp)
    }

    @Test
    fun `dano sem PV temporario reduz o PV real diretamente`() {
        val c = combatant(currentHp = 15, maxHp = 20, tempHp = 0)
        val result = c.withDamage(6)
        assertEquals(0, result.tempHp)
        assertEquals(9, result.currentHp)
    }

    @Test
    fun `PV real nao passa de -999 mesmo com dano enorme`() {
        val c = combatant(currentHp = 5, maxHp = 20, tempHp = 0)
        val result = c.withDamage(5000)
        assertEquals(-999, result.currentHp)
    }

    @Test
    fun `cura (valor negativo) aumenta o PV real sem ultrapassar o maximo`() {
        val c = combatant(currentHp = 5, maxHp = 20, tempHp = 0)
        val result = c.withDamage(-100)
        assertEquals(20, result.currentHp)
    }

    @Test
    fun `cura nunca restaura PV temporario`() {
        val c = combatant(currentHp = 5, maxHp = 20, tempHp = 3)
        val result = c.withDamage(-10)
        assertEquals(3, result.tempHp)
        assertEquals(15, result.currentHp)
    }

    @Test
    fun `amount zero nao altera o combatente`() {
        val c = combatant(currentHp = 12, maxHp = 20, tempHp = 2)
        val result = c.withDamage(0)
        assertEquals(c, result)
    }

    @Test
    fun `start ordena por iniciativa decrescente e comeca na rodada 1, turno 0`() {
        val combatants = listOf(
            combatant(id = "a").copy(initiative = 8),
            combatant(id = "b").copy(initiative = 20),
            combatant(id = "c").copy(initiative = 14)
        )
        val state = CombatState.start(combatants)
        assertEquals(listOf("b", "c", "a"), state.combatants.map { it.id })
        assertEquals(0, state.currentTurnIndex)
        assertEquals(1, state.round)
        assertEquals("b", state.currentCombatantId)
    }

    @Test
    fun `npcCombatantId usa o prefixo esperado`() {
        assertEquals("npc:42", CombatState.npcCombatantId(42L))
    }
}
