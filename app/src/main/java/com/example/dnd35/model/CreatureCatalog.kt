package com.example.dnd35.model

import java.text.Normalizer

/** Atributos de uma criatura. `null` = sem valor ("—" no livro, ex.: Inteligência de mortos-vivos). */
data class CreatureAbilities(
    val str: Int?,
    val dex: Int?,
    val con: Int?,
    val int: Int?,
    val wis: Int?,
    val cha: Int?
)

data class CreatureSaves(val fort: Int, val ref: Int, val will: Int)

/**
 * Ficha-base de uma criatura do Livro dos Monstros 3.5 (somente leitura).
 *
 * Os números usados em cálculo (PV, CA, BBA, atributos, testes de resistência) são campos
 * próprios; ataques, qualidades, perícias e talentos ficam como texto do livro até a fase de
 * dados estruturados. Campos que o bloco extraído do livro não traz ficam como `""`, o que
 * é diferente de `"—"` (impresso no livro).
 */
data class CreatureDefinition(
    val id: String,
    val name: String,
    val type: String,
    val size: String,
    /** Texto entre parênteses da linha de tipo, ex.: "Médio — Metamorfo". */
    val typeDetail: String,
    /** Ex.: "2d8+4", "1/2d8". */
    val hitDice: String,
    /** Quantidade de dados de vida; fracionária para criaturas com menos de 1 DV (1/2d8 = 0,5). */
    val hdCount: Double,
    val hdType: Int,
    val hitPoints: Int,
    val initiative: Int,
    val speed: String,
    val armorClass: Int,
    val armorClassText: String,
    val touchAc: Int,
    val flatFootedAc: Int,
    val baseAttack: Int,
    val grapple: Int,
    val attack: String,
    val fullAttack: String,
    val spaceReach: String,
    val specialAttacks: String,
    val specialQualities: String,
    val saves: CreatureSaves,
    val abilities: CreatureAbilities,
    val skills: String,
    val feats: String,
    val environment: String,
    val organization: String,
    /** Texto do ND como no livro: "5", "1/3"... */
    val challengeRating: String,
    val treasure: String,
    val alignment: String,
    val advancement: String,
    val levelAdjustment: String,
    val source: String,
    val description: String = "",
    val combat: String = ""
) {
    /** ND numérico para ordenar e filtrar ("1/3" = 0,333). */
    val challengeRatingValue: Double get() = CreatureCatalog.parseChallengeRating(challengeRating)
}

/** Catálogo do Livro dos Monstros, lido de `dnd35/creatures.json` (ver docs/CATALOGOS_JSON.md). */
object CreatureCatalog {
    val ALL: List<CreatureDefinition> by lazy { load() }

    fun byId(id: String): CreatureDefinition? = ALL.firstOrNull { it.id == id }

    /** Tipos presentes no catálogo, em ordem alfabética, para o filtro por tipo. */
    val TYPES: List<String> by lazy { ALL.map { it.type }.distinct().sorted() }

    val SIZES: List<String> = listOf(
        "Mínimo", "Diminuto", "Miúdo", "Pequeno", "Médio", "Grande", "Enorme", "Imenso", "Colossal"
    )

    fun parseChallengeRating(text: String): Double {
        val t = text.trim()
        val slash = t.indexOf('/')
        if (slash > 0) {
            val num = t.substring(0, slash).trim().toDoubleOrNull()
            val den = t.substring(slash + 1).trim().toDoubleOrNull()
            if (num != null && den != null && den != 0.0) return num / den
        }
        return t.toDoubleOrNull() ?: 0.0
    }

    /** Minúsculas, sem acentos e com travessão tratado como hífen, para busca tolerante. */
    fun normalizeForSearch(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .replace('–', '-')
            .lowercase()
            .trim()

    /**
     * Filtra o catálogo. Todos os critérios são opcionais e se combinam com "e".
     * `query` procura no nome, sem diferenciar maiúsculas nem acentos.
     */
    fun filter(
        creatures: List<CreatureDefinition> = ALL,
        query: String = "",
        type: String? = null,
        size: String? = null,
        minChallengeRating: Double? = null,
        maxChallengeRating: Double? = null
    ): List<CreatureDefinition> {
        val q = normalizeForSearch(query)
        return creatures.filter { c ->
            (q.isEmpty() || normalizeForSearch(c.name).contains(q)) &&
                (type == null || c.type == type) &&
                (size == null || c.size == size) &&
                (minChallengeRating == null || c.challengeRatingValue >= minChallengeRating) &&
                (maxChallengeRating == null || c.challengeRatingValue <= maxChallengeRating)
        }.sortedBy { normalizeForSearch(it.name) }
    }

    private fun load(): List<CreatureDefinition> {
        val stream = CreatureCatalog::class.java.classLoader?.getResourceAsStream("dnd35/creatures.json")
            ?: throw IllegalStateException("Catálogo não encontrado no classpath: dnd35/creatures.json")
        val array = org.json.JSONArray(stream.use { String(it.readBytes(), Charsets.UTF_8) })
        return (0 until array.length()).map { i -> parse(array.getJSONObject(i), i) }
    }

    private fun parse(o: org.json.JSONObject, index: Int): CreatureDefinition {
        val ctx = "criatura #${index + 1}"
        fun str(key: String): String {
            if (!o.has(key) || o.isNull(key)) throw IllegalStateException("$ctx: campo obrigatório ausente: $key")
            return o.getString(key)
        }
        fun optionalStr(key: String): String = if (!o.has(key) || o.isNull(key)) "" else o.getString(key)
        fun int(key: String): Int {
            if (!o.has(key) || o.isNull(key)) throw IllegalStateException("$ctx: campo obrigatório ausente: $key")
            return o.getInt(key)
        }
        val name = str("name")
        val saves = o.getJSONObject("saves")
        val ab = o.getJSONObject("abilities")
        fun abil(key: String): Int? = if (ab.isNull(key)) null else ab.getInt(key)
        return CreatureDefinition(
            id = str("id"),
            name = name,
            type = str("type"),
            size = str("size"),
            typeDetail = str("typeDetail"),
            hitDice = str("hitDice"),
            hdCount = o.getDouble("hdCount"),
            hdType = int("hdType"),
            hitPoints = int("hitPoints"),
            initiative = int("initiative"),
            speed = str("speed"),
            armorClass = int("armorClass"),
            armorClassText = str("armorClassText"),
            touchAc = int("touchAc"),
            flatFootedAc = int("flatFootedAc"),
            baseAttack = int("baseAttack"),
            grapple = int("grapple"),
            attack = str("attack"),
            fullAttack = str("fullAttack"),
            spaceReach = str("spaceReach"),
            specialAttacks = str("specialAttacks"),
            specialQualities = str("specialQualities"),
            saves = CreatureSaves(saves.getInt("fort"), saves.getInt("ref"), saves.getInt("will")),
            abilities = CreatureAbilities(abil("str"), abil("dex"), abil("con"), abil("int"), abil("wis"), abil("cha")),
            skills = str("skills"),
            feats = str("feats"),
            environment = str("environment"),
            organization = str("organization"),
            challengeRating = str("challengeRating"),
            treasure = str("treasure"),
            alignment = str("alignment"),
            advancement = str("advancement"),
            levelAdjustment = str("levelAdjustment"),
            source = str("source"),
            description = optionalStr("description"),
            combat = optionalStr("combat")
        )
    }
}
