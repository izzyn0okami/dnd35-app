package com.example.dnd35.backup

import android.util.Base64
import com.example.dnd35.model.CharacterEntity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Formato de arquivo para exportar e importar fichas.
 *
 * O arquivo é um JSON com um envelope versionado. As colunas que já guardam JSON
 * (perícias, inventário, talentos, magias etc.) são gravadas como texto, exatamente
 * como estão no banco, para não perder nenhum dado nem depender do esquema interno.
 *
 * Regras de compatibilidade:
 * - campos desconhecidos são ignorados e campos ausentes usam o valor padrão da ficha;
 * - arquivos com [FORMAT_VERSION] maior que o suportado são recusados com mensagem clara;
 * - a importação nunca sobrescreve fichas existentes: cada ficha vira um registro novo.
 */
object CharacterBackup {
    const val APP_ID = "ficha-dnd35"
    const val FORMAT_VERSION = 1
    const val MAX_CHARACTERS = 500

    /** Colunas que guardam JSON como texto e são copiadas sem reinterpretar. */
    private val JSON_COLUMNS = listOf(
        "skillsJson", "languagesJson", "skillPointHistoryJson", "languageAcquisitionsJson",
        "inventoryJson", "featsJson", "spellsJson", "spellSlotsJson", "multiclassJson"
    )

    data class ImportedCharacter(
        val entity: CharacterEntity,
        /** Bytes do retrato (JPEG) embutido no arquivo, se houver. */
        val photoBytes: ByteArray? = null
    )

    sealed class ParseResult {
        data class Success(
            val characters: List<ImportedCharacter>,
            val warnings: List<String>
        ) : ParseResult()

        data class Failure(val message: String) : ParseResult()
    }

    /**
     * Gera o JSON de backup.
     *
     * @param photoReader recebe o `photoUri` da ficha e devolve os bytes do retrato quando ele
     * é um arquivo local do app; devolve null para retratos predefinidos ou indisponíveis.
     */
    fun toJson(
        characters: List<CharacterEntity>,
        exportedAt: Long = System.currentTimeMillis(),
        photoReader: (String) -> ByteArray? = { null }
    ): String {
        val root = JSONObject()
        root.put("app", APP_ID)
        root.put("formatVersion", FORMAT_VERSION)
        root.put("exportedAt", exportedAt)
        val array = JSONArray()
        characters.forEach { array.put(characterToJson(it, photoReader)) }
        root.put("characters", array)
        return root.toString(2)
    }

    private fun characterToJson(c: CharacterEntity, photoReader: (String) -> ByteArray?): JSONObject {
        val o = JSONObject()
        o.put("name", c.name)
        o.put("characterClass", c.characterClass)
        o.put("race", c.race)
        o.put("level", c.level)
        o.put("alignment", c.alignment)
        o.put("deity", c.deity)
        o.put("size", c.size)
        o.put("currentHp", c.currentHp)
        o.put("maxHp", c.maxHp)
        o.put("tempHp", c.tempHp)
        o.put("nonLethalDamage", c.nonLethalDamage)
        o.put("baseStr", c.baseStr)
        o.put("baseDex", c.baseDex)
        o.put("baseCon", c.baseCon)
        o.put("baseInt", c.baseInt)
        o.put("baseWis", c.baseWis)
        o.put("baseCha", c.baseCha)
        o.put("modStrBonus", c.modStrBonus)
        o.put("modDexBonus", c.modDexBonus)
        o.put("modConBonus", c.modConBonus)
        o.put("modIntBonus", c.modIntBonus)
        o.put("modWisBonus", c.modWisBonus)
        o.put("modChaBonus", c.modChaBonus)
        o.put("armorBonus", c.armorBonus)
        o.put("shieldBonus", c.shieldBonus)
        o.put("naturalArmor", c.naturalArmor)
        o.put("deflectionBonus", c.deflectionBonus)
        o.put("miscAcBonus", c.miscAcBonus)
        o.put("miscFortitude", c.miscFortitude)
        o.put("miscReflex", c.miscReflex)
        o.put("miscWill", c.miscWill)
        o.put("miscInitiative", c.miscInitiative)
        putFloat(o, "baseSpeedMeters", c.baseSpeedMeters)
        putFloat(o, "goldGp", c.goldGp)
        putFloat(o, "silverSp", c.silverSp)
        putFloat(o, "copperCp", c.copperCp)
        putFloat(o, "platinumPp", c.platinumPp)
        o.put("skillsJson", c.skillsJson)
        o.put("languagesJson", c.languagesJson)
        o.put("skillPointHistoryJson", c.skillPointHistoryJson)
        o.put("languageAcquisitionsJson", c.languageAcquisitionsJson)
        o.put("inventoryJson", c.inventoryJson)
        o.put("featsJson", c.featsJson)
        o.put("spellsJson", c.spellsJson)
        o.put("spellSlotsJson", c.spellSlotsJson)
        o.put("multiclassJson", c.multiclassJson)
        o.put("notes", c.notes)
        o.put("updatedAt", c.updatedAt)

        val photoUri = c.photoUri
        if (!photoUri.isNullOrBlank()) {
            if (photoUri.startsWith(PRESET_PREFIX)) {
                o.put("photoPreset", photoUri)
            } else {
                val bytes = photoReader(photoUri)
                if (bytes != null && bytes.isNotEmpty()) {
                    o.put("photoJpegBase64", Base64.encodeToString(bytes, Base64.NO_WRAP))
                }
            }
        }
        return o
    }

    // Float.toString dá a menor representação decimal; evita 1.1f virar 1.100000023841858.
    private fun putFloat(o: JSONObject, key: String, value: Float) {
        val safe = if (value.isFinite()) value else 0f
        o.put(key, safe.toString().toDouble())
    }

    fun parse(json: String): ParseResult {
        val root = try {
            JSONObject(json)
        } catch (_: JSONException) {
            return ParseResult.Failure("O arquivo não é um backup válido de fichas (JSON inválido).")
        }

        if (root.optString("app") != APP_ID) {
            return ParseResult.Failure("Este arquivo não foi gerado pelo app Ficha D&D 3.5.")
        }
        val version = root.optInt("formatVersion", 0)
        if (version < 1) {
            return ParseResult.Failure("Versão do arquivo de backup inválida.")
        }
        if (version > FORMAT_VERSION) {
            return ParseResult.Failure(
                "Este backup foi criado por uma versão mais nova do app. Atualize o app para importá-lo."
            )
        }
        val array = root.optJSONArray("characters")
            ?: return ParseResult.Failure("O arquivo não contém a lista de fichas.")
        if (array.length() > MAX_CHARACTERS) {
            return ParseResult.Failure("O arquivo contém fichas demais (máximo de $MAX_CHARACTERS).")
        }

        val warnings = mutableListOf<String>()
        val result = mutableListOf<ImportedCharacter>()
        for (index in 0 until array.length()) {
            val obj = array.optJSONObject(index)
            if (obj == null) {
                warnings += "Item ${index + 1} ignorado: não é uma ficha."
                continue
            }
            result += parseCharacter(obj, index + 1, warnings)
        }
        if (result.isEmpty()) {
            return ParseResult.Failure("O arquivo não contém nenhuma ficha utilizável.")
        }
        return ParseResult.Success(result, warnings)
    }

    private fun parseCharacter(o: JSONObject, position: Int, warnings: MutableList<String>): ImportedCharacter {
        val d = CharacterEntity()

        val rawName = str(o, "name", "").trim()
        val name = if (rawName.isEmpty()) {
            warnings += "Ficha $position sem nome: importada como \"Personagem importado\"."
            "Personagem importado"
        } else rawName

        val maxHp = int(o, "maxHp", d.maxHp).coerceIn(1, 100_000)
        val label = name

        fun jsonColumn(key: String): String {
            val text = str(o, key, "")
            if (text.isBlank()) return ""
            return try {
                JSONArray(text)
                text
            } catch (_: JSONException) {
                warnings += "$label: campo \"$key\" inválido foi ignorado."
                ""
            }
        }

        val photoPreset = str(o, "photoPreset", "")
        val photoBase64 = str(o, "photoJpegBase64", "")
        val photoBytes = if (photoBase64.isNotEmpty()) {
            val decoded = try {
                Base64.decode(photoBase64, Base64.DEFAULT)
            } catch (_: IllegalArgumentException) {
                null
            }
            if (decoded == null || decoded.isEmpty()) {
                warnings += "$label: retrato corrompido foi ignorado."
                null
            } else decoded
        } else null

        val entity = CharacterEntity(
            id = 0,
            name = name,
            characterClass = str(o, "characterClass", d.characterClass),
            race = str(o, "race", d.race),
            level = int(o, "level", d.level).coerceIn(1, 100),
            alignment = str(o, "alignment", d.alignment),
            deity = str(o, "deity", d.deity),
            size = str(o, "size", d.size),
            photoUri = photoPreset.takeIf { it.startsWith(PRESET_PREFIX) },
            currentHp = int(o, "currentHp", maxHp).coerceIn(0, maxHp),
            maxHp = maxHp,
            tempHp = int(o, "tempHp", 0).coerceIn(0, 100_000),
            nonLethalDamage = int(o, "nonLethalDamage", 0).coerceIn(0, 100_000),
            baseStr = score(o, "baseStr", d.baseStr),
            baseDex = score(o, "baseDex", d.baseDex),
            baseCon = score(o, "baseCon", d.baseCon),
            baseInt = score(o, "baseInt", d.baseInt),
            baseWis = score(o, "baseWis", d.baseWis),
            baseCha = score(o, "baseCha", d.baseCha),
            modStrBonus = bonus(o, "modStrBonus"),
            modDexBonus = bonus(o, "modDexBonus"),
            modConBonus = bonus(o, "modConBonus"),
            modIntBonus = bonus(o, "modIntBonus"),
            modWisBonus = bonus(o, "modWisBonus"),
            modChaBonus = bonus(o, "modChaBonus"),
            armorBonus = bonus(o, "armorBonus"),
            shieldBonus = bonus(o, "shieldBonus"),
            naturalArmor = bonus(o, "naturalArmor"),
            deflectionBonus = bonus(o, "deflectionBonus"),
            miscAcBonus = bonus(o, "miscAcBonus"),
            miscFortitude = bonus(o, "miscFortitude"),
            miscReflex = bonus(o, "miscReflex"),
            miscWill = bonus(o, "miscWill"),
            miscInitiative = bonus(o, "miscInitiative"),
            baseSpeedMeters = float(o, "baseSpeedMeters", d.baseSpeedMeters).coerceIn(0f, 1000f),
            goldGp = coins(o, "goldGp", d.goldGp),
            silverSp = coins(o, "silverSp", d.silverSp),
            copperCp = coins(o, "copperCp", d.copperCp),
            platinumPp = coins(o, "platinumPp", d.platinumPp),
            skillsJson = jsonColumn("skillsJson"),
            languagesJson = jsonColumn("languagesJson"),
            skillPointHistoryJson = jsonColumn("skillPointHistoryJson"),
            languageAcquisitionsJson = jsonColumn("languageAcquisitionsJson"),
            inventoryJson = jsonColumn("inventoryJson"),
            featsJson = jsonColumn("featsJson"),
            spellsJson = jsonColumn("spellsJson"),
            spellSlotsJson = jsonColumn("spellSlotsJson"),
            multiclassJson = jsonColumn("multiclassJson"),
            notes = str(o, "notes", ""),
            updatedAt = longValue(o, "updatedAt", System.currentTimeMillis()).takeIf { it > 0 }
                ?: System.currentTimeMillis()
        )
        return ImportedCharacter(entity, photoBytes)
    }

    // org.json devolve a string "null" para valores JSON null; aqui eles viram o padrão.
    private fun str(o: JSONObject, key: String, default: String): String =
        if (!o.has(key) || o.isNull(key)) default else o.optString(key, default)

    private fun int(o: JSONObject, key: String, default: Int): Int =
        if (!o.has(key) || o.isNull(key)) default else o.optInt(key, default)

    private fun longValue(o: JSONObject, key: String, default: Long): Long =
        if (!o.has(key) || o.isNull(key)) default else o.optLong(key, default)

    private fun float(o: JSONObject, key: String, default: Float): Float {
        if (!o.has(key) || o.isNull(key)) return default
        val v = o.optDouble(key, Double.NaN)
        return if (v.isNaN() || v.isInfinite()) default else v.toFloat()
    }

    private fun score(o: JSONObject, key: String, default: Int): Int = int(o, key, default).coerceIn(1, 99)

    private fun bonus(o: JSONObject, key: String): Int = int(o, key, 0).coerceIn(-100, 100)

    private fun coins(o: JSONObject, key: String, default: Float): Float =
        float(o, key, default).coerceAtLeast(0f)

    const val PRESET_PREFIX = "preset:"
}
