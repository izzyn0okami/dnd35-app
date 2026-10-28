package com.example.dnd35.model

/**
 * Tabelas de magias por dia e magias conhecidas do Livro do Jogador D&D 3.5 (SRD), usadas para
 * substituir a configuração manual de espaços de magia por um cálculo totalmente automático.
 *
 * Cada tabela é indexada por [nível de conjurador] -> array de 10 posições (círculos 0 a 9).
 * `-1` significa "círculo ainda não acessível nesse nível" (nenhum espaço, nem mesmo por bônus
 * de atributo); um valor >= 0 é a base de espaços daquele círculo (pode ser 0: já acessível,
 * mas só com bônus de atributo alto é que rende um espaço de fato).
 *
 * Paladino e Ranger são conjuradores tardios: o nível de conjurador já vem calculado como
 * (nível de classe - 3) por quem chama estas tabelas, então aqui a tabela começa em 1.
 *
 * Fonte: tabelas padrão de "Magias por Dia" e "Magias Conhecidas" do SRD 3.5. Se alguma mesa
 * usar uma variante em algum nível alto específico, o ajuste fica concentrado só aqui.
 */
object SpellcastingRules {

    private val NA = -1

    val WIZARD_SPELLS_PER_DAY: Map<Int, IntArray> = mapOf(
        1 to intArrayOf(3, 1, NA, NA, NA, NA, NA, NA, NA, NA),
        2 to intArrayOf(4, 2, NA, NA, NA, NA, NA, NA, NA, NA),
        3 to intArrayOf(4, 2, 1, NA, NA, NA, NA, NA, NA, NA),
        4 to intArrayOf(4, 3, 2, NA, NA, NA, NA, NA, NA, NA),
        5 to intArrayOf(4, 3, 2, 1, NA, NA, NA, NA, NA, NA),
        6 to intArrayOf(4, 3, 3, 2, NA, NA, NA, NA, NA, NA),
        7 to intArrayOf(4, 4, 3, 2, 1, NA, NA, NA, NA, NA),
        8 to intArrayOf(4, 4, 3, 3, 2, NA, NA, NA, NA, NA),
        9 to intArrayOf(4, 4, 4, 3, 2, 1, NA, NA, NA, NA),
        10 to intArrayOf(4, 4, 4, 3, 3, 2, NA, NA, NA, NA),
        11 to intArrayOf(4, 4, 4, 4, 3, 2, 1, NA, NA, NA),
        12 to intArrayOf(4, 4, 4, 4, 3, 3, 2, NA, NA, NA),
        13 to intArrayOf(4, 4, 4, 4, 4, 3, 2, 1, NA, NA),
        14 to intArrayOf(4, 4, 4, 4, 4, 3, 3, 2, NA, NA),
        15 to intArrayOf(4, 4, 4, 4, 4, 4, 3, 2, 1, NA),
        16 to intArrayOf(4, 4, 4, 4, 4, 4, 3, 3, 2, NA),
        17 to intArrayOf(4, 4, 4, 4, 4, 4, 4, 3, 2, 1),
        18 to intArrayOf(4, 4, 4, 4, 4, 4, 4, 3, 3, 2),
        19 to intArrayOf(4, 4, 4, 4, 4, 4, 4, 4, 3, 3),
        20 to intArrayOf(4, 4, 4, 4, 4, 4, 4, 4, 4, 4)
    )

    val SORCERER_SPELLS_PER_DAY: Map<Int, IntArray> = mapOf(
        1 to intArrayOf(5, 3, NA, NA, NA, NA, NA, NA, NA, NA),
        2 to intArrayOf(6, 4, NA, NA, NA, NA, NA, NA, NA, NA),
        3 to intArrayOf(6, 5, NA, NA, NA, NA, NA, NA, NA, NA),
        4 to intArrayOf(6, 6, 3, NA, NA, NA, NA, NA, NA, NA),
        5 to intArrayOf(6, 6, 4, NA, NA, NA, NA, NA, NA, NA),
        6 to intArrayOf(6, 6, 5, 3, NA, NA, NA, NA, NA, NA),
        7 to intArrayOf(6, 6, 6, 4, NA, NA, NA, NA, NA, NA),
        8 to intArrayOf(6, 6, 6, 5, 3, NA, NA, NA, NA, NA),
        9 to intArrayOf(6, 6, 6, 6, 4, NA, NA, NA, NA, NA),
        10 to intArrayOf(6, 6, 6, 6, 5, 3, NA, NA, NA, NA),
        11 to intArrayOf(6, 6, 6, 6, 6, 4, NA, NA, NA, NA),
        12 to intArrayOf(6, 6, 6, 6, 6, 5, 3, NA, NA, NA),
        13 to intArrayOf(6, 6, 6, 6, 6, 6, 4, NA, NA, NA),
        14 to intArrayOf(6, 6, 6, 6, 6, 6, 5, 3, NA, NA),
        15 to intArrayOf(6, 6, 6, 6, 6, 6, 6, 4, NA, NA),
        16 to intArrayOf(6, 6, 6, 6, 6, 6, 6, 5, 3, NA),
        17 to intArrayOf(6, 6, 6, 6, 6, 6, 6, 6, 4, NA),
        18 to intArrayOf(6, 6, 6, 6, 6, 6, 6, 6, 5, 3),
        19 to intArrayOf(6, 6, 6, 6, 6, 6, 6, 6, 6, 4),
        20 to intArrayOf(6, 6, 6, 6, 6, 6, 6, 6, 6, 6)
    )

    val SORCERER_SPELLS_KNOWN: Map<Int, IntArray> = mapOf(
        1 to intArrayOf(4, 2, NA, NA, NA, NA, NA, NA, NA, NA),
        2 to intArrayOf(5, 2, NA, NA, NA, NA, NA, NA, NA, NA),
        3 to intArrayOf(5, 3, NA, NA, NA, NA, NA, NA, NA, NA),
        4 to intArrayOf(6, 3, 1, NA, NA, NA, NA, NA, NA, NA),
        5 to intArrayOf(6, 4, 2, NA, NA, NA, NA, NA, NA, NA),
        6 to intArrayOf(7, 4, 2, 1, NA, NA, NA, NA, NA, NA),
        7 to intArrayOf(7, 5, 3, 2, NA, NA, NA, NA, NA, NA),
        8 to intArrayOf(8, 5, 3, 2, 1, NA, NA, NA, NA, NA),
        9 to intArrayOf(8, 5, 4, 3, 2, NA, NA, NA, NA, NA),
        10 to intArrayOf(9, 5, 4, 3, 2, 1, NA, NA, NA, NA),
        11 to intArrayOf(9, 5, 5, 4, 3, 2, NA, NA, NA, NA),
        12 to intArrayOf(9, 5, 5, 4, 3, 2, 1, NA, NA, NA),
        13 to intArrayOf(9, 5, 5, 4, 4, 3, 2, NA, NA, NA),
        14 to intArrayOf(9, 5, 5, 4, 4, 3, 2, 1, NA, NA),
        15 to intArrayOf(9, 5, 5, 4, 4, 4, 3, 2, NA, NA),
        16 to intArrayOf(9, 5, 5, 5, 4, 4, 3, 2, 1, NA),
        17 to intArrayOf(9, 5, 5, 5, 4, 4, 4, 3, 2, NA),
        18 to intArrayOf(9, 5, 5, 5, 5, 4, 4, 3, 2, 1),
        19 to intArrayOf(9, 5, 5, 5, 5, 5, 4, 4, 3, 2),
        20 to intArrayOf(9, 5, 5, 5, 5, 5, 5, 4, 4, 3)
    )

    val CLERIC_DRUID_SPELLS_PER_DAY: Map<Int, IntArray> = mapOf(
        1 to intArrayOf(3, 1, NA, NA, NA, NA, NA, NA, NA, NA),
        2 to intArrayOf(4, 2, NA, NA, NA, NA, NA, NA, NA, NA),
        3 to intArrayOf(4, 2, 1, NA, NA, NA, NA, NA, NA, NA),
        4 to intArrayOf(5, 3, 2, NA, NA, NA, NA, NA, NA, NA),
        5 to intArrayOf(5, 3, 2, 1, NA, NA, NA, NA, NA, NA),
        6 to intArrayOf(5, 3, 3, 2, NA, NA, NA, NA, NA, NA),
        7 to intArrayOf(6, 4, 3, 2, 1, NA, NA, NA, NA, NA),
        8 to intArrayOf(6, 4, 3, 3, 2, NA, NA, NA, NA, NA),
        9 to intArrayOf(6, 4, 4, 3, 2, 1, NA, NA, NA, NA),
        10 to intArrayOf(6, 4, 4, 3, 3, 2, NA, NA, NA, NA),
        11 to intArrayOf(6, 5, 4, 4, 3, 2, 1, NA, NA, NA),
        12 to intArrayOf(6, 5, 4, 4, 3, 3, 2, NA, NA, NA),
        13 to intArrayOf(6, 5, 5, 4, 4, 3, 2, 1, NA, NA),
        14 to intArrayOf(6, 5, 5, 4, 4, 3, 3, 2, NA, NA),
        15 to intArrayOf(6, 5, 5, 5, 4, 4, 3, 2, 1, NA),
        16 to intArrayOf(6, 5, 5, 5, 4, 4, 3, 3, 2, NA),
        17 to intArrayOf(6, 5, 5, 5, 5, 4, 4, 3, 2, 1),
        18 to intArrayOf(6, 5, 5, 5, 5, 4, 4, 3, 3, 2),
        19 to intArrayOf(6, 5, 5, 5, 5, 5, 4, 4, 3, 3),
        20 to intArrayOf(6, 5, 5, 5, 5, 5, 4, 4, 4, 4)
    )

    // Paladino/Ranger: conjuradores tardios (nível de conjurador = nível de classe - 3,
    // já calculado por quem chama). Círculo 0 não existe (NA) e o máximo é o 4º círculo.
    val HALF_CASTER_DIVINE_SPELLS_PER_DAY: Map<Int, IntArray> = mapOf(
        1 to intArrayOf(NA, 0, NA, NA, NA, NA, NA, NA, NA, NA),
        2 to intArrayOf(NA, 0, NA, NA, NA, NA, NA, NA, NA, NA),
        3 to intArrayOf(NA, 1, NA, NA, NA, NA, NA, NA, NA, NA),
        4 to intArrayOf(NA, 1, NA, NA, NA, NA, NA, NA, NA, NA),
        5 to intArrayOf(NA, 1, 0, NA, NA, NA, NA, NA, NA, NA),
        6 to intArrayOf(NA, 1, 1, NA, NA, NA, NA, NA, NA, NA),
        7 to intArrayOf(NA, 1, 1, NA, NA, NA, NA, NA, NA, NA),
        8 to intArrayOf(NA, 1, 1, 0, NA, NA, NA, NA, NA, NA),
        9 to intArrayOf(NA, 1, 1, 1, NA, NA, NA, NA, NA, NA),
        10 to intArrayOf(NA, 2, 1, 1, NA, NA, NA, NA, NA, NA),
        11 to intArrayOf(NA, 2, 2, 1, 0, NA, NA, NA, NA, NA),
        12 to intArrayOf(NA, 2, 2, 1, 1, NA, NA, NA, NA, NA),
        13 to intArrayOf(NA, 2, 2, 2, 1, NA, NA, NA, NA, NA),
        14 to intArrayOf(NA, 3, 2, 2, 1, NA, NA, NA, NA, NA),
        15 to intArrayOf(NA, 3, 3, 2, 2, NA, NA, NA, NA, NA),
        16 to intArrayOf(NA, 3, 3, 3, 2, NA, NA, NA, NA, NA),
        17 to intArrayOf(NA, 3, 3, 3, 3, NA, NA, NA, NA, NA)
    )

    // Bardo: conjurador espontâneo tardio (começa no 2º nível de personagem), máximo 6º círculo.
    val BARD_SPELLS_PER_DAY: Map<Int, IntArray> = mapOf(
        1 to intArrayOf(2, NA, NA, NA, NA, NA, NA, NA, NA, NA),
        2 to intArrayOf(3, 0, NA, NA, NA, NA, NA, NA, NA, NA),
        3 to intArrayOf(3, 1, NA, NA, NA, NA, NA, NA, NA, NA),
        4 to intArrayOf(3, 2, 0, NA, NA, NA, NA, NA, NA, NA),
        5 to intArrayOf(3, 3, 1, NA, NA, NA, NA, NA, NA, NA),
        6 to intArrayOf(3, 3, 2, NA, NA, NA, NA, NA, NA, NA),
        7 to intArrayOf(3, 3, 2, 0, NA, NA, NA, NA, NA, NA),
        8 to intArrayOf(3, 3, 3, 1, NA, NA, NA, NA, NA, NA),
        9 to intArrayOf(3, 3, 3, 2, NA, NA, NA, NA, NA, NA),
        10 to intArrayOf(3, 3, 3, 2, 0, NA, NA, NA, NA, NA),
        11 to intArrayOf(3, 3, 3, 3, 1, NA, NA, NA, NA, NA),
        12 to intArrayOf(3, 3, 3, 3, 2, NA, NA, NA, NA, NA),
        13 to intArrayOf(3, 3, 3, 3, 2, 0, NA, NA, NA, NA),
        14 to intArrayOf(4, 4, 4, 3, 3, 1, NA, NA, NA, NA),
        15 to intArrayOf(4, 4, 4, 4, 3, 2, NA, NA, NA, NA),
        16 to intArrayOf(4, 4, 4, 4, 3, 2, 0, NA, NA, NA),
        17 to intArrayOf(4, 4, 4, 4, 4, 3, 1, NA, NA, NA),
        18 to intArrayOf(4, 4, 4, 4, 4, 3, 2, NA, NA, NA),
        19 to intArrayOf(4, 4, 4, 4, 4, 4, 3, NA, NA, NA),
        20 to intArrayOf(4, 4, 4, 4, 4, 4, 4, NA, NA, NA)
    )

    val BARD_SPELLS_KNOWN: Map<Int, IntArray> = mapOf(
        1 to intArrayOf(4, 2, NA, NA, NA, NA, NA, NA, NA, NA),
        2 to intArrayOf(5, 3, NA, NA, NA, NA, NA, NA, NA, NA),
        3 to intArrayOf(6, 4, NA, NA, NA, NA, NA, NA, NA, NA),
        4 to intArrayOf(6, 4, 2, NA, NA, NA, NA, NA, NA, NA),
        5 to intArrayOf(6, 4, 3, NA, NA, NA, NA, NA, NA, NA),
        6 to intArrayOf(6, 4, 3, NA, NA, NA, NA, NA, NA, NA),
        7 to intArrayOf(6, 4, 3, 2, NA, NA, NA, NA, NA, NA),
        8 to intArrayOf(6, 4, 4, 3, NA, NA, NA, NA, NA, NA),
        9 to intArrayOf(6, 4, 4, 3, NA, NA, NA, NA, NA, NA),
        10 to intArrayOf(6, 4, 4, 3, 2, NA, NA, NA, NA, NA),
        11 to intArrayOf(6, 4, 4, 4, 3, NA, NA, NA, NA, NA),
        12 to intArrayOf(6, 4, 4, 4, 3, NA, NA, NA, NA, NA),
        13 to intArrayOf(6, 4, 4, 4, 3, 2, NA, NA, NA, NA),
        14 to intArrayOf(6, 4, 4, 4, 4, 3, NA, NA, NA, NA),
        15 to intArrayOf(6, 4, 4, 4, 4, 3, NA, NA, NA, NA),
        16 to intArrayOf(6, 4, 4, 4, 4, 3, 2, NA, NA, NA),
        17 to intArrayOf(6, 4, 4, 4, 4, 4, 3, NA, NA, NA),
        18 to intArrayOf(6, 4, 4, 4, 4, 4, 3, NA, NA, NA),
        19 to intArrayOf(6, 4, 4, 4, 4, 4, 4, NA, NA, NA),
        20 to intArrayOf(6, 4, 4, 4, 4, 4, 4, NA, NA, NA)
    )

    private fun tableFor(className: String): Map<Int, IntArray>? = when (className.trim().lowercase()) {
        "mago" -> WIZARD_SPELLS_PER_DAY
        "feiticeiro" -> SORCERER_SPELLS_PER_DAY
        "clérigo", "druida" -> CLERIC_DRUID_SPELLS_PER_DAY
        "paladino", "ranger" -> HALF_CASTER_DIVINE_SPELLS_PER_DAY
        "bardo" -> BARD_SPELLS_PER_DAY
        else -> null
    }

    private fun knownTableFor(className: String): Map<Int, IntArray>? = when (className.trim().lowercase()) {
        "feiticeiro" -> SORCERER_SPELLS_KNOWN
        "bardo" -> BARD_SPELLS_KNOWN
        else -> null
    }

    /**
     * Bônus de magias por circulo concedido por um modificador de atributo alto (regra padrão
     * 3.5): 0 se o modificador for menor que o círculo; caso contrário, 1 espaço extra, mais
     * mais 1 a cada 9 pontos adicionais de modificador acima do mínimo necessário. Círculo 0
     * (truques/orações) nunca recebe bônus.
     */
    fun bonusSpellsForAbilityMod(spellLevel: Int, abilityMod: Int): Int {
        if (spellLevel < 1 || abilityMod < spellLevel) return 0
        return 1 + (abilityMod - spellLevel) / 9
    }

    /**
     * Espaços de magia por dia de UMA classe conjuradora, já somando a base da tabela com o
     * bônus de atributo (só aplicado a círculos que a classe já acessa nesse nível). Retorna
     * null se a classe não tiver tabela de conjuração própria.
     */
    fun spellsPerDayForClassLevel(className: String, casterLevel: Int, abilityMod: Int, hasDomainSlot: Boolean): IntArray? {
        val table = tableFor(className) ?: return null
        val base = table[casterLevel.coerceAtLeast(1)] ?: return (0..9).map { 0 }.toIntArray()
        return IntArray(10) { circle ->
            val baseValue = base[circle]
            if (baseValue < 0) 0
            else {
                val bonus = bonusSpellsForAbilityMod(circle, abilityMod)
                val domainBonus = if (hasDomainSlot && circle >= 1) 1 else 0
                baseValue + bonus + domainBonus
            }
        }
    }

    /** Magias conhecidas por círculo para classes conjuradoras espontâneas (Bardo, Feiticeiro). */
    fun knownSpellsForClassLevel(className: String, casterLevel: Int): IntArray? {
        val table = knownTableFor(className) ?: return null
        val base = table[casterLevel.coerceAtLeast(1)] ?: return IntArray(10) { 0 }
        return IntArray(10) { circle -> if (base[circle] < 0) 0 else base[circle] }
    }
}
