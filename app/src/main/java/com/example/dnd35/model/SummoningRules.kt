package com.example.dnd35.model

enum class SummoningFamily { SUMMON_CREATURES, SUMMON_NATURE_ALLY }

data class SummonCreatureOption(
    val creature: CreatureDefinition,
    val family: SummoningFamily,
    /** Lista de origem da criatura, em círculo de magia. */
    val sourceCircle: Int,
    /** Modelo celestial/abissal indicado pela tabela do Livro do Jogador, quando aplicável. */
    val templateLabel: String = ""
)

/**
 * Tabelas de Invocar Criaturas e Invocar Aliado da Natureza (Livro do Jogador 3.5, cap. 11).
 * A opção é liberada no círculo indicado ou em qualquer círculo superior, pois as magias
 * permitem escolher criaturas de listas inferiores.
 */
object SummoningRules {
    // Listas conferidas com as tabelas do Livro do Jogador (pp. 210–211).
    // Entram aqui somente criaturas que já possuem ficha em CreatureCatalog.
    // As entradas sem ficha oficial permanecem registradas como pendência no log.
    private val natureLists = mapOf(
        1 to "rato-atroz aguia macaco polvo coruja golfinho vibora-pequena lobo",
        2 to "urso-negro crocodilo texugo-atroz morcego-atroz hipogrifo tubarao-medio vibora-media lula carcaju",
        3 to "gorila arminho-atroz lobo-atroz aguia-gigante leao coruja-gigante satiro tubarao-grande cobra-constritora vibora-grande thoqqua",
        4 to "urso-marrom crocodilo-gigante dinonico gorila-atroz javali-atroz carcaju-atroz leao-marinho tubarao-enorme vibora-enorme tigre",
        5 to "urso-polar leao-atroz elasmossauro grifo janni rinoceronte satiro cobra-constritora-gigante nixie baleia-orca",
        6 to "urso-atroz girallon megaraptor polvo-gigante pixie baleia-azul",
        7 to "tigre-atroz djinni cacador-invisivel pixie lula-gigante triceratops tiranossauro baleia-cachalote",
        8 to "tubarao-atroz passaro-roca",
        9 to "grig pixie"
    )

    private val monsterLists = mapOf(
        1 to "cachorro coruja vaga-lume-gigante golfinho texugo macaco rato-atroz-abissal corvo centopeia-media falcao aranha-pequena polvo vibora-pequena",
        2 to "cachorro-de-montaria aguia lula lobo centopeia-grande escorpiao-medio tubarao-medio aranha-media vibora-media",
        3 to "urso-negro bisao texugo-atroz hipogrifo gorila arminho-atroz cao-infernal cobra-constritora javali morcego-atroz centopeia-enorme crocodilo dretch vibora-grande carcaju",
        4 to "arconte-luminar coruja-gigante aguia-gigante leao-celestial mephit-da-agua mephit-do-ar mephit-do-fogo mephit-do-gelo mephit-do-limo mephit-do-magma mephit-da-poeira mephit-do-sal mephit-do-vapor lobo-atroz vespa-gigante tubarao-grande aranha-grande vibora-enorme uivante",
        5 to "urso-marrom leao-marinho grifo achaierai diabo-barbado-barbazu dinonico gorila-atroz javali-atroz tubarao-enorme escorpiao-grande mastim-das-sombras carcaju-atroz crocodilo-gigante tigre",
        6 to "urso-atroz baleia-orca leao-atroz cobra-constritora-gigante diabo-das-correntes-kyton centopeia-imensa rinoceronte elasmossauro aranha-enorme bralani janni fera-do-caos",
        7 to "elefante avoral djinni cacador-invisivel slaad-vermelho diabo-dos-ossos-osyluth megaraptor escorpiao-enorme babau polvo-gigante girallon",
        8 to "baleia-cachalote triceratops lillend slaad-azul lula-gigante gato-infernal-bezekira centopeia-colossal tigre-atroz aranha-imensa tiranossauro vrock",
        9 to "couatl passaro-roca slaad-verde diabo-barbado-barbazu tubarao-atroz escorpiao-imenso bebilith aranha-colossal hezrou"
    )

    private fun circleMap(lists: Map<Int, String>): Map<String, Int> =
        lists.flatMap { (circle, ids) -> ids.split(Regex("\\s+")).filter { it.isNotBlank() }.map { it to circle } }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, circles) -> circles.minOrNull() ?: 9 }

    private val natureCircleById = circleMap(natureLists)
    private val monsterCircleById = circleMap(monsterLists)

    private val natureVariants = mapOf(
        "satiro" to listOf(3 to "Sem flauta", 5 to "Com flauta"),
        "pixie" to listOf(
            6 to "Sem flechas especiais",
            7 to "Com flechas soníferas",
            9 to "Com flechas soníferas e de perda de memória"
        )
    )

    private val monsterTemplates = mapOf(
        "cachorro" to "Celestial", "coruja" to "Celestial", "vaga-lume-gigante" to "Celestial",
        "golfinho" to "Celestial", "texugo" to "Celestial", "macaco" to "Celestial",
        "cachorro-de-montaria" to "Celestial", "aguia" to "Celestial", "urso-negro" to "Celestial",
        "bisao" to "Celestial", "texugo-atroz" to "Celestial", "hipogrifo" to "Celestial",
        "arconte-luminar" to "Celestial", "coruja-gigante" to "Celestial", "aguia-gigante" to "Celestial",
        "leao-celestial" to "Celestial", "urso-marrom" to "Celestial", "leao-marinho" to "Celestial",
        "grifo" to "Celestial", "urso-atroz" to "Celestial", "baleia-orca" to "Celestial",
        "leao-atroz" to "Celestial", "elefante" to "Celestial", "baleia-cachalote" to "Celestial",
        "triceratops" to "Celestial", "passaro-roca" to "Celestial",

        "rato-atroz-abissal" to "Abissal", "corvo" to "Abissal", "centopeia-media" to "Abissal",
        "falcao" to "Abissal", "aranha-pequena" to "Abissal", "polvo" to "Abissal",
        "vibora-pequena" to "Abissal", "lula" to "Abissal", "lobo" to "Abissal",
        "centopeia-grande" to "Abissal", "escorpiao-medio" to "Abissal", "tubarao-medio" to "Abissal",
        "aranha-media" to "Abissal", "vibora-media" to "Abissal", "gorila" to "Abissal",
        "arminho-atroz" to "Abissal", "cao-infernal" to "Abissal", "cobra-constritora" to "Abissal",
        "javali" to "Abissal", "morcego-atroz" to "Abissal", "centopeia-enorme" to "Abissal",
        "crocodilo" to "Abissal", "dretch" to "Abissal", "vibora-grande" to "Abissal",
        "carcaju" to "Abissal", "lobo-atroz" to "Abissal", "vespa-gigante" to "Abissal",
        "tubarao-grande" to "Abissal", "aranha-grande" to "Abissal", "vibora-enorme" to "Abissal",
        "dinonico" to "Abissal", "gorila-atroz" to "Abissal", "javali-atroz" to "Abissal",
        "tubarao-enorme" to "Abissal", "escorpiao-grande" to "Abissal", "carcaju-atroz" to "Abissal",
        "crocodilo-gigante" to "Abissal", "tigre" to "Abissal", "cobra-constritora-gigante" to "Abissal",
        "centopeia-imensa" to "Abissal", "elasmossauro" to "Abissal", "aranha-enorme" to "Abissal",
        "megaraptor" to "Abissal", "escorpiao-enorme" to "Abissal", "babau" to "Abissal",
        "polvo-gigante" to "Abissal", "girallon" to "Abissal", "lula-gigante" to "Abissal",
        "gato-infernal-bezekira" to "Abissal", "centopeia-colossal" to "Abissal",
        "tigre-atroz" to "Abissal", "aranha-imensa" to "Abissal", "tiranossauro" to "Abissal",
        "tubarao-atroz" to "Abissal", "escorpiao-imenso" to "Abissal",
        "aranha-colossal" to "Abissal"
    )

    fun isSummoningSpell(spellName: String): Boolean =
        spellName.trim().startsWith("Invocar Criaturas", ignoreCase = true) ||
            spellName.trim().startsWith("Invocar Aliado da Natureza", ignoreCase = true)

    fun familyForSpell(spellName: String): SummoningFamily? = when {
        spellName.trim().startsWith("Invocar Criaturas", ignoreCase = true) -> SummoningFamily.SUMMON_CREATURES
        spellName.trim().startsWith("Invocar Aliado da Natureza", ignoreCase = true) -> SummoningFamily.SUMMON_NATURE_ALLY
        else -> null
    }

    fun isClassAllowed(family: SummoningFamily, className: String): Boolean {
        val name = className.trim()
        return when (family) {
            SummoningFamily.SUMMON_CREATURES ->
                listOf("Bardo", "Clérigo", "Feiticeiro", "Mago").any { it.equals(name, ignoreCase = true) }
            SummoningFamily.SUMMON_NATURE_ALLY ->
                listOf("Druida", "Ranger").any { it.equals(name, ignoreCase = true) }
        }
    }

    fun hasListEntry(creatureId: String): Boolean =
        creatureId in natureCircleById || creatureId in monsterCircleById

    /** Retorna todas as cartas válidas até o círculo da magia, incluindo as listas inferiores. */
    fun creaturesForCircle(family: SummoningFamily, circle: Int): List<SummonCreatureOption> {
        if (circle !in 1..9) return emptyList()
        return CreatureCatalog.ALL.flatMap { creature ->
            if (!creature.summonedCreature) {
                emptyList()
            } else {
                val candidates: List<Pair<Int, String>> = when (family) {
                    SummoningFamily.SUMMON_NATURE_ALLY ->
                        natureVariants[creature.id]
                            ?: listOfNotNull(natureCircleById[creature.id]?.let { it to "" })
                    SummoningFamily.SUMMON_CREATURES ->
                        listOfNotNull(monsterCircleById[creature.id]?.let { it to monsterTemplates[creature.id].orEmpty() })
                }
                candidates.filter { it.first <= circle }.map { (sourceCircle, variant) ->
                    SummonCreatureOption(
                        creature = creature,
                        family = family,
                        sourceCircle = sourceCircle,
                        templateLabel = variant
                    )
                }
            }
        }.sortedWith(compareBy({ it.sourceCircle }, { CreatureCatalog.normalizeForSearch(it.creature.name) }, { it.templateLabel }))
    }

    /**
     * Resolve a seleção para uma magia concreta da ficha. Exige que a magia conste na lista
     * daquela classe e que o personagem já alcance o círculo, considerando atributo-chave.
     * Retorna null quando não é uma magia de invocação; lista vazia quando é, mas não é elegível.
     */
    fun optionsForSpell(
        spellName: String,
        spellCircle: Int,
        casterClass: String,
        stats: CalculatedCharacterStats,
        character: CharacterEntity,
        spellDomain: String = ""
    ): List<SummonCreatureOption>? {
        val family = familyForSpell(spellName) ?: return null
        val preset = Dnd35Rules.PRESET_SPELLS.firstOrNull { it.name.equals(spellName, ignoreCase = true) }
            ?: return emptyList()
        val clericDomainSpell = casterClass.equals("Clérigo", ignoreCase = true) &&
            spellDomain.isNotBlank() &&
            ClericDomainRules.domainsOf(character).any { it.equals(spellDomain, ignoreCase = true) } &&
            preset.domainLevels.keys.any { it.equals(spellDomain, ignoreCase = true) }
        if (!isClassAllowed(family, casterClass) && !clericDomainSpell) return emptyList()
        val listedCircle = if (clericDomainSpell) {
            preset.domainLevels.entries.firstOrNull { it.key.equals(spellDomain, ignoreCase = true) }?.value
        } else {
            SpellbookRules.classLevelOf(preset, casterClass)
        } ?: return emptyList()
        if (listedCircle != spellCircle) return emptyList()
        val caster = stats.casterClasses.firstOrNull { it.className.equals(casterClass, ignoreCase = true) }
            ?: return emptyList()
        val abilityScore = SpellbookRules.abilityScoreFor(caster, stats)
        if (SpellbookRules.maxCastableCircle(caster, abilityScore) < listedCircle) return emptyList()
        return creaturesForCircle(family, listedCircle)
    }

    /** IDs sinalizados como invocáveis no catálogo, mas que não constam nas listas do livro. */
    fun unclassifiedCatalogCreatureIds(): List<String> =
        CreatureCatalog.ALL.filter { it.summonedCreature && !hasListEntry(it.id) }.map { it.id }
}
