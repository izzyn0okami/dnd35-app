package com.example.dnd35.model

/**
 * Regras do espaço de domínio do Clérigo (Livro do Jogador 3.5): o Clérigo escolhe 2 domínios
 * e, em cada círculo de 1º em diante, ganha 1 espaço extra que só pode ser usado para preparar
 * a magia de um desses domínios naquele círculo.
 *
 * O espaço extra já está somado ao total de espaços do Clérigo (SpellcastingRules). Aqui fica a
 * escolha dos domínios, a lista de magias de domínio por círculo e o aviso de excesso.
 */
object ClericDomainRules {
    const val CLERIC = "Clérigo"
    const val MAX_DOMAINS = 2

    /** Os 22 domínios do Livro do Jogador. */
    val ALL_DOMAINS: List<String> = listOf(
        "Água", "Animais", "Ar", "Bem", "Caos", "Conhecimento", "Cura", "Destruição",
        "Enganação", "Fogo", "Força", "Guerra", "Magia", "Mal", "Morte", "Ordem",
        "Planta", "Proteção", "Sol", "Sorte", "Terra", "Viagem"
    )

    /** Mantém só domínios válidos (grafia canônica), sem repetir, até [MAX_DOMAINS]. */
    fun normalize(domains: List<String>): List<String> =
        domains.mapNotNull { d -> ALL_DOMAINS.firstOrNull { it.equals(d.trim(), ignoreCase = true) } }
            .distinct()
            .take(MAX_DOMAINS)

    fun domainsOf(character: CharacterEntity): List<String> =
        normalize(CharacterJsonAdapters.deserializeDomains(character.clericDomainsJson))

    /** O Clérigo ganha o espaço de domínio em cada círculo de 1º em diante. */
    fun hasDomainSlotAt(circle: Int): Boolean = circle >= 1

    /** Magias de domínio disponíveis num círculo: (domínio, magia), na ordem dos domínios escolhidos. */
    fun domainSpellsAt(
        domains: List<String>,
        circle: Int,
        catalog: List<Dnd35Rules.PresetSpell> = Dnd35Rules.PRESET_SPELLS
    ): List<Pair<String, Dnd35Rules.PresetSpell>> =
        domains.flatMap { domain ->
            catalog.filter { it.domainLevels[domain] == circle }.map { domain to it }
        }

    /** Magia de domínio preparada em cada círculo (a que ocupa o espaço de domínio). */
    fun preparedDomainSpells(spells: List<CharacterSpell>): List<CharacterSpell> =
        spells.filter { it.domain.isNotBlank() && it.prepared }

    /** Avisos: mais de uma magia de domínio por círculo ou magia de um domínio não escolhido. */
    fun warnings(spells: List<CharacterSpell>, domains: List<String>): List<String> {
        val warnings = mutableListOf<String>()
        val prepared = preparedDomainSpells(spells)
        prepared.groupBy { it.level }.forEach { (level, list) ->
            if (list.size > 1) {
                warnings += "Clérigo: ${list.size} magias de domínio preparadas de ${SpellbookRules.circleLabel(level)} (só 1 por círculo)"
            }
        }
        prepared.filter { s -> domains.none { it.equals(s.domain, ignoreCase = true) } }.forEach {
            warnings += "${it.name}: domínio ${it.domain} não está entre os domínios escolhidos"
        }
        return warnings
    }
}
