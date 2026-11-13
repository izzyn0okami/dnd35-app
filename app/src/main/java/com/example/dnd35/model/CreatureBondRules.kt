package com.example.dnd35.model

/**
 * Vínculos permanentes de um personagem com criaturas do Livro dos Monstros.
 * A ficha da criatura permanece no CreatureCatalog; este registro guarda apenas a referência.
 */
enum class CreatureBondKind { FAMILIAR, ANIMAL_COMPANION }

data class CharacterCreatureBond(
    val id: String = java.util.UUID.randomUUID().toString(),
    val kind: CreatureBondKind,
    val creatureId: String
)

data class CreatureSelectionOption(
    val creature: CreatureDefinition,
    val requiredEffectiveLevel: Int,
    val effectiveLevelPenalty: Int = 0,
    val companionEffectiveLevel: Int = 1,
    val restriction: String = ""
)

/**
 * Regras de elegibilidade de familiares e companheiros animais (Livro do Jogador 3.5).
 * As flags do catálogo indicam a lista de origem; os requisitos de nível são aplicados aqui.
 */
object CreatureBondRules {
    private data class CompanionRequirement(
        val minimumLevel: Int,
        val penalty: Int = 0,
        val aquaticOnly: Boolean = false
    )

    private val companionRequirements = mapOf(
        // Lista básica terrestre
        "texugo" to CompanionRequirement(1),
        "camelo" to CompanionRequirement(1),
        "rato-atroz" to CompanionRequirement(1),
        "cachorro" to CompanionRequirement(1),
        "cachorro-de-montaria" to CompanionRequirement(1),
        "aguia" to CompanionRequirement(1),
        "falcao" to CompanionRequirement(1),
        "cavalo-leve" to CompanionRequirement(1),
        "cavalo-pesado" to CompanionRequirement(1),
        "coruja" to CompanionRequirement(1),
        "ponei" to CompanionRequirement(1),
        "vibora-pequena" to CompanionRequirement(1),
        "vibora-media" to CompanionRequirement(1),
        "lobo" to CompanionRequirement(1),

        // Lista básica aquática
        "golfinho" to CompanionRequirement(1, aquaticOnly = true),
        "lula" to CompanionRequirement(1, aquaticOnly = true),
        "tubarao-medio" to CompanionRequirement(1, aquaticOnly = true),
        "crocodilo" to CompanionRequirement(4, 3, aquaticOnly = true),

        // Alternativas a partir do 4º nível efetivo: penalidade -3
        "bisao" to CompanionRequirement(4, 3),
        "carcaju" to CompanionRequirement(4, 3),
        "cobra-constritora" to CompanionRequirement(4, 3),
        "vibora-grande" to CompanionRequirement(4, 3),
        "arminho-atroz" to CompanionRequirement(4, 3),
        "texugo-atroz" to CompanionRequirement(4, 3),
        "guepardo" to CompanionRequirement(4, 3),
        "javali" to CompanionRequirement(4, 3),
        "gorila" to CompanionRequirement(4, 3),
        "lagarto" to CompanionRequirement(4, 3),
        "leopardo" to CompanionRequirement(4, 3),
        "morcego-atroz" to CompanionRequirement(4, 3),
        "tubarao-grande" to CompanionRequirement(4, 3, aquaticOnly = true),
        "urso-negro" to CompanionRequirement(4, 3),

        // Alternativas a partir do 7º nível efetivo: penalidade -6
        "carcaju-atroz" to CompanionRequirement(7, 6),
        "vibora-enorme" to CompanionRequirement(7, 6),
        "crocodilo-gigante" to CompanionRequirement(7, 6),
        "dinonico" to CompanionRequirement(7, 6),
        "gorila-atroz" to CompanionRequirement(7, 6),
        "javali-atroz" to CompanionRequirement(7, 6),
        "rinoceronte" to CompanionRequirement(7, 6),
        "lobo-atroz" to CompanionRequirement(7, 6),
        "leao" to CompanionRequirement(7, 6),
        "elasmossauro" to CompanionRequirement(7, 6, aquaticOnly = true),
        "tigre" to CompanionRequirement(7, 6),
        "urso-marrom" to CompanionRequirement(7, 6),

        // Alternativas a partir do 10º nível efetivo: penalidade -9
        "baleia-orca" to CompanionRequirement(10, 9, aquaticOnly = true),
        "leao-atroz" to CompanionRequirement(10, 9),
        "tubarao-enorme" to CompanionRequirement(10, 9, aquaticOnly = true),
        "megaraptor" to CompanionRequirement(10, 9),
        "cobra-constritora-gigante" to CompanionRequirement(10, 9),
        "urso-polar" to CompanionRequirement(10, 9),

        // Alternativas a partir do 13º nível efetivo: penalidade -12
        "elefante" to CompanionRequirement(13, 12),
        "urso-atroz" to CompanionRequirement(13, 12),
        "polvo-gigante" to CompanionRequirement(13, 12, aquaticOnly = true),

        // Alternativas a partir do 16º nível efetivo: penalidade -15
        "lula-gigante" to CompanionRequirement(16, 15, aquaticOnly = true),
        "triceratops" to CompanionRequirement(16, 15),
        "tigre-atroz" to CompanionRequirement(16, 15),
        "tubarao-atroz" to CompanionRequirement(16, 15, aquaticOnly = true),
        "tiranossauro" to CompanionRequirement(16, 15)
    )

    private val rangerBasicCompanionIds = setOf(
        "cavalo-leve", "cavalo-pesado", "coruja", "falcao", "lobo",
        "ponei", "rato-atroz", "texugo", "vibora-pequena", "vibora-media"
    )
    private val aquaticBasicCompanionIds = setOf("crocodilo", "golfinho", "lula", "tubarao-medio")

    private fun levelOf(classes: List<CharacterClassEntry>, className: String): Int =
        classes.filter { it.className.equals(className, ignoreCase = true) }.sumOf { it.level }

    /** Mago e Feiticeiro acumulam seus níveis para determinar o poder do familiar. */
    fun familiarMasterLevel(classes: List<CharacterClassEntry>): Int =
        levelOf(classes, "Mago") + levelOf(classes, "Feiticeiro")

    fun familiarOptions(classes: List<CharacterClassEntry>): List<CreatureDefinition> {
        if (familiarMasterLevel(classes) < 1) return emptyList()
        return CreatureCatalog.ALL.filter { it.familiar }.sortedBy { CreatureCatalog.normalizeForSearch(it.name) }
    }

    /**
     * O nível de druida e metade dos níveis de ranger (arredondado para baixo) se acumulam.
     * Um ranger de 4º nível, portanto, possui nível efetivo de druida 2 para essa habilidade.
     */
    fun animalCompanionEffectiveLevel(classes: List<CharacterClassEntry>): Int {
        val druidLevel = levelOf(classes, "Druida")
        val rangerLevel = levelOf(classes, "Ranger")
        // O Ranger só recebe Companheiro Animal no 4º nível; níveis anteriores não contribuem.
        val rangerContribution = if (rangerLevel >= 4) rangerLevel / 2 else 0
        return druidLevel + rangerContribution
    }

    fun animalCompanionOptions(
        classes: List<CharacterClassEntry>,
        aquaticCampaign: Boolean = false
    ): List<CreatureSelectionOption> {
        val effectiveLevel = animalCompanionEffectiveLevel(classes)
        if (effectiveLevel < 1) return emptyList()
        val hasDruidLevels = levelOf(classes, "Druida") > 0
        val rangerLevel = levelOf(classes, "Ranger")
        val rangerOnly = !hasDruidLevels && rangerLevel >= 4
        val hasCompanionFeature = hasDruidLevels || rangerLevel >= 4

        return CreatureCatalog.ALL.mapNotNull { creature ->
            if (!creature.animalCompanion) return@mapNotNull null
            var requirement = companionRequirements[creature.id] ?: return@mapNotNull null
            if (rangerOnly && creature.id !in rangerBasicCompanionIds &&
                !(aquaticCampaign && creature.id in aquaticBasicCompanionIds) &&
                requirement.minimumLevel == 1
            ) return@mapNotNull null
            if (aquaticCampaign && hasCompanionFeature && creature.id in aquaticBasicCompanionIds) {
                requirement = CompanionRequirement(1, aquaticOnly = true)
            }
            if (requirement.aquaticOnly && (!aquaticCampaign || !hasCompanionFeature)) return@mapNotNull null
            if (effectiveLevel < requirement.minimumLevel) return@mapNotNull null
            val adjustedLevel = (effectiveLevel - requirement.penalty).coerceAtLeast(1)
            CreatureSelectionOption(
                creature = creature,
                requiredEffectiveLevel = requirement.minimumLevel,
                effectiveLevelPenalty = requirement.penalty,
                companionEffectiveLevel = adjustedLevel,
                restriction = listOfNotNull(
                    if (requirement.aquaticOnly) "Campanha/ambiente aquático" else null,
                    if (requirement.penalty > 0) "Nível efetivo de druida -${requirement.penalty}" else null
                ).joinToString(" • ")
            )
        }.sortedBy { CreatureCatalog.normalizeForSearch(it.creature.name) }
    }

    fun isValidBond(bond: CharacterCreatureBond): Boolean {
        val creature = CreatureCatalog.byId(bond.creatureId) ?: return false
        return when (bond.kind) {
            CreatureBondKind.FAMILIAR -> creature.familiar
            CreatureBondKind.ANIMAL_COMPANION -> creature.animalCompanion
        }
    }
}
