package com.example.dnd35.model

/** Especialização do Mago: escola de especialidade (nula = generalista) e escolas proibidas. */
data class WizardSpecialty(
    val school: String? = null,
    val prohibited: List<String> = emptyList()
) {
    val isSpecialist: Boolean get() = school != null
}

/**
 * Regras de especialização do Mago (Livro do Jogador 3.5): o especialista escolhe uma escola,
 * ganha 1 magia extra por círculo (de 0 em diante) que só pode ser da escola de especialidade e
 * abre mão de 2 escolas proibidas (só 1 se a especialidade for Adivinhação, que nunca pode ser
 * proibida). Mago generalista não tem escolas proibidas nem espaço extra.
 */
object WizardSpecialtyRules {
    const val WIZARD = "Mago"
    const val DIVINATION = "Adivinhação"

    val SCHOOLS: List<String> = listOf(
        "Abjuração", "Adivinhação", "Conjuração", "Encantamento",
        "Evocação", "Ilusão", "Necromancia", "Transmutação"
    )

    fun maxProhibited(school: String?): Int = when (school) {
        null -> 0
        DIVINATION -> 1
        else -> 2
    }

    /** Mantém só escolas válidas, sem repetir, sem proibir a especialidade nem Adivinhação. */
    fun normalize(school: String?, prohibited: List<String>): WizardSpecialty {
        val canonicalSchool = SCHOOLS.firstOrNull { it.equals(school?.trim(), ignoreCase = true) }
        val banned = prohibited
            .mapNotNull { p -> SCHOOLS.firstOrNull { it.equals(p.trim(), ignoreCase = true) } }
            .filter { it != canonicalSchool && it != DIVINATION }
            .distinct()
            .take(maxProhibited(canonicalSchool))
        return WizardSpecialty(canonicalSchool, banned)
    }

    fun specialtyOf(character: CharacterEntity): WizardSpecialty =
        CharacterJsonAdapters.deserializeSpecialty(character.wizardSpecialtyJson)

    private fun baseSchool(schoolText: String): String = MagicSchoolParser.parse(schoolText).baseSchool

    fun isSpecialtySchool(schoolText: String, specialty: WizardSpecialty): Boolean =
        specialty.school != null && baseSchool(schoolText).equals(specialty.school, ignoreCase = true)

    fun isProhibited(schoolText: String, specialty: WizardSpecialty): Boolean =
        specialty.prohibited.any { it.equals(baseSchool(schoolText), ignoreCase = true) }

    /**
     * Avisos do Mago: magia de escola proibida preparada e preparações comuns que invadem o
     * espaço extra da especialidade (esse espaço só serve para magia da escola de especialidade).
     */
    fun warnings(spells: List<CharacterSpell>, specialty: WizardSpecialty, wizard: CasterClassInfo?): List<String> {
        if (wizard == null) return emptyList()
        val warnings = mutableListOf<String>()
        val wizardSpells = spells.filter { it.casterClass.equals(WIZARD, ignoreCase = true) || it.casterClass.isBlank() }
        wizardSpells.filter { it.prepared && isProhibited(it.school, specialty) }.forEach {
            warnings += "${it.name}: escola ${baseSchool(it.school)} é proibida para este Mago"
        }
        if (specialty.isSpecialist) {
            for (circle in 0..9) {
                val slots = wizard.slotsByLevel[circle] ?: 0
                if (slots == 0) continue
                val common = wizardSpells
                    .filter { it.prepared && it.level == circle && !isSpecialtySchool(it.school, specialty) }
                    .sumOf { it.preparedCount.coerceAtLeast(1) }
                if (common > slots - 1) {
                    warnings += "Mago: $common magias comuns preparadas de ${SpellbookRules.circleLabel(circle)}; 1 espaço é só da especialidade (${specialty.school})"
                }
            }
        }
        return warnings
    }
}
