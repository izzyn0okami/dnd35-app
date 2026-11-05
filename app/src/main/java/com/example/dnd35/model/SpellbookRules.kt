package com.example.dnd35.model

/**
 * Regras do Grimório e Magias (Livro do Jogador 3.5) que ligam o catálogo de magias, a ficha
 * e as classes conjuradoras. Só funções puras: a interface decide como exibir o resultado.
 *
 * Limites de preparação e de magias conhecidas são apenas sinalizados (a ficha avisa, não
 * bloqueia), porque itens, talentos ou regras da mesa podem conceder espaços extras. O que as
 * regras de classe controlam de verdade é quais ações aparecem para cada magia (por exemplo,
 * só o Mago precisa ter a magia no grimório para prepará-la).
 */
object SpellbookRules {

    /** Como a classe adquire e usa magias. */
    enum class Style {
        /** Mago: aprende magias para o grimório e prepara todo dia a partir dele. */
        SPELLBOOK,

        /** Clérigo, Druida, Paladino e Ranger: preparam todo dia a partir da lista da classe. */
        PREPARED_LIST,

        /** Feiticeiro e Bardo: repertório fixo de magias conhecidas, sem preparação diária. */
        SPONTANEOUS
    }

    fun styleFor(className: String): Style? = when (className.trim().lowercase()) {
        "mago" -> Style.SPELLBOOK
        "clérigo", "clerigo", "druida", "paladino", "ranger" -> Style.PREPARED_LIST
        "feiticeiro", "bardo" -> Style.SPONTANEOUS
        else -> null
    }

    /** CD do teste de resistência de uma magia: 10 + círculo + modificador do atributo-chave. */
    fun spellDc(spellLevel: Int, abilityMod: Int): Int = 10 + spellLevel + abilityMod

    /** Valor mínimo do atributo-chave para conjurar uma magia do círculo (10 + círculo). */
    fun minAbilityScore(spellLevel: Int): Int = 10 + spellLevel

    fun abilityScoreFor(info: CasterClassInfo, stats: CalculatedCharacterStats): Int = when (info.keyAbility) {
        "INT" -> stats.totalInt
        "SAB" -> stats.totalWis
        "CAR" -> stats.totalCha
        else -> 0
    }

    /** Maior círculo que a classe já acessa neste nível (-1 se nenhum). */
    fun highestAccessibleCircle(info: CasterClassInfo): Int =
        SpellcastingRules.highestAccessibleCircle(info.className, info.casterLevel)

    /** Maior círculo que o personagem pode conjurar de fato: limitado pelo nível e pelo atributo. */
    fun maxCastableCircle(info: CasterClassInfo, abilityScore: Int): Int =
        minOf(highestAccessibleCircle(info), abilityScore - 10, 9)

    /**
     * Páginas que a magia ocupa no grimório (Livro do Jogador 3.5): 1 página por círculo, com
     * mínimo de 1 (truques também ocupam uma página).
     */
    fun spellbookPages(spellLevel: Int): Int = spellLevel.coerceAtLeast(1)

    /** Custo, em peças de ouro, de copiar uma magia para o grimório: 100 po por página. */
    fun copyCostGp(spellLevel: Int): Int = 100 * spellbookPages(spellLevel)

    /** Tempo de cópia em horas: 1 hora por página. */
    fun copyHours(spellLevel: Int): Int = spellbookPages(spellLevel)

    enum class Eligibility { OK, CIRCLE_NOT_REACHED, ABILITY_TOO_LOW, NOT_ON_CLASS_LIST, PROHIBITED_SCHOOL }

    /** Se a classe pode aprender/preparar uma magia do catálogo e, se não, por quê. */
    fun eligibility(
        preset: Dnd35Rules.PresetSpell,
        info: CasterClassInfo,
        abilityScore: Int,
        wizardSpecialty: WizardSpecialty = WizardSpecialty()
    ): Eligibility {
        val level = classLevelOf(preset, info.className) ?: return Eligibility.NOT_ON_CLASS_LIST
        if (info.className.equals(WizardSpecialtyRules.WIZARD, ignoreCase = true) &&
            WizardSpecialtyRules.isProhibited(preset.school, wizardSpecialty)
        ) return Eligibility.PROHIBITED_SCHOOL
        if (level > highestAccessibleCircle(info)) return Eligibility.CIRCLE_NOT_REACHED
        if (abilityScore < minAbilityScore(level)) return Eligibility.ABILITY_TOO_LOW
        return Eligibility.OK
    }

    /** Círculo da magia na lista de uma classe (ignora maiúsculas), ou null se não consta. */
    fun classLevelOf(preset: Dnd35Rules.PresetSpell, className: String): Int? =
        preset.classLevels.entries.firstOrNull { it.key.equals(className, ignoreCase = true) }?.value

    /** Dados do catálogo para uma magia da ficha; magias manuais viram um registro sem classes. */
    fun presetFor(spell: CharacterSpell): Dnd35Rules.PresetSpell =
        Dnd35Rules.PRESET_SPELLS.firstOrNull { it.name.equals(spell.name, ignoreCase = true) }
            ?: Dnd35Rules.PresetSpell(
                name = spell.name,
                level = spell.level,
                school = spell.school,
                classes = emptyList(),
                castTime = spell.castTime,
                components = spell.components,
                range = spell.range,
                duration = spell.duration,
                savingThrow = spell.savingThrow,
                spellResistance = spell.spellResistance,
                description = spell.description
            )

    /** Mago: magias antigas sem classe registrada contam como já presentes no grimório. */
    fun isInSpellbook(spell: CharacterSpell): Boolean = spell.inSpellbook || spell.casterClass.isBlank()

    /**
     * Classe conjuradora a que uma magia da ficha pertence. Magias novas guardam a classe;
     * as antigas (sem classe) vão para a primeira classe que acessa aquele círculo.
     */
    fun classFor(spell: CharacterSpell, casters: List<CasterClassInfo>): CasterClassInfo? {
        if (casters.isEmpty()) return null
        if (spell.casterClass.isNotBlank()) {
            casters.firstOrNull { it.className.equals(spell.casterClass, ignoreCase = true) }?.let { return it }
        }
        return casters.firstOrNull { highestAccessibleCircle(it) >= spell.level } ?: casters.first()
    }

    /** Total de preparações usadas por classe e círculo (cada preparação ocupa um espaço). */
    fun preparedUsage(spells: List<CharacterSpell>, casters: List<CasterClassInfo>): Map<Pair<String, Int>, Int> {
        val usage = mutableMapOf<Pair<String, Int>, Int>()
        for (spell in spells) {
            if (!spell.prepared) continue
            val info = classFor(spell, casters) ?: continue
            if (styleFor(info.className) == Style.SPONTANEOUS) continue
            val key = info.className to spell.level
            usage[key] = (usage[key] ?: 0) + spell.preparedCount.coerceAtLeast(1)
        }
        return usage
    }

    /** Quantas magias conhecidas de uma classe espontânea existem em cada círculo. */
    fun knownCount(spells: List<CharacterSpell>, casters: List<CasterClassInfo>, className: String, level: Int): Int =
        spells.count { s ->
            s.level == level && classFor(s, casters)?.className.equals(className, ignoreCase = true)
        }

    /** Textos de aviso para limites estourados (vazio quando está tudo dentro do limite). */
    fun overLimitWarnings(spells: List<CharacterSpell>, casters: List<CasterClassInfo>): List<String> {
        val warnings = mutableListOf<String>()
        val usage = preparedUsage(spells, casters)
        for (info in casters) {
            when (styleFor(info.className)) {
                Style.SPONTANEOUS -> {
                    val limits = info.knownSpellsByLevel ?: continue
                    for (level in 0..9) {
                        val known = knownCount(spells, casters, info.className, level)
                        val limit = limits[level] ?: 0
                        if (known > limit) {
                            warnings += "${info.className}: $known magias conhecidas de ${circleLabel(level)} (limite $limit)"
                        }
                    }
                }
                Style.SPELLBOOK, Style.PREPARED_LIST -> {
                    for (level in 0..9) {
                        val used = usage[info.className to level] ?: 0
                        val slots = info.slotsByLevel[level] ?: 0
                        if (used > slots) {
                            warnings += "${info.className}: $used preparadas de ${circleLabel(level)} (espaços $slots)"
                        }
                    }
                }
                null -> Unit
            }
        }
        return warnings
    }

    fun circleLabel(level: Int): String = if (level == 0) "nível 0" else "${level}º círculo"
}
