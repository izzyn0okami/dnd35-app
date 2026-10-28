package com.example.dnd35.model

import android.util.Log

import org.json.JSONArray
import org.json.JSONObject

object CharacterJsonAdapters {

    fun serializeLanguages(languages: List<String>): String {
        val array = JSONArray()
        languages.map { it.trim() }.filter { it.isNotEmpty() }.distinctBy { it.lowercase() }
            .forEach { array.put(it) }
        return array.toString()
    }

    fun deserializeLanguages(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).mapNotNull { index ->
                array.optString(index).trim().takeIf { it.isNotEmpty() }
            }.distinctBy { it.lowercase() }
        } catch (e: Exception) {
            Log.w("CharacterJsonAdapters", "JSON de idiomas inválido", e)
            emptyList()
        }
    }


    fun serializeLanguageAcquisitions(records: List<LanguageAcquisition>): String {
        val array = JSONArray()
        records.filter { it.isValid() }
            .distinctBy { it.language.trim().lowercase() }
            .forEach { record ->
                val obj = JSONObject()
                obj.put("language", record.language.trim())
                obj.put("source", record.source.name)
                if (record.characterLevel != null) obj.put("characterLevel", record.characterLevel)
                obj.put("pointsSpent", record.pointsSpent)
                array.put(obj)
            }
        return array.toString()
    }

    fun deserializeLanguageAcquisitions(json: String?): List<LanguageAcquisition> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).mapNotNull { index ->
                val obj = array.optJSONObject(index) ?: return@mapNotNull null
                val language = obj.optString("language", "").trim()
                val source = runCatching {
                    LanguageAcquisitionSource.valueOf(obj.optString("source", "OTHER"))
                }.getOrDefault(LanguageAcquisitionSource.OTHER)
                val level = if (obj.has("characterLevel") && !obj.isNull("characterLevel")) {
                    obj.optInt("characterLevel", 0)
                } else null
                val record = LanguageAcquisition(
                    language = language,
                    source = source,
                    characterLevel = level,
                    pointsSpent = obj.optInt("pointsSpent", 0)
                )
                record.takeIf { it.isValid() }
            }.distinctBy { it.language.lowercase() }
        } catch (e: Exception) {
            Log.w("CharacterJsonAdapters", "JSON de aquisição de idiomas inválido", e)
            emptyList()
        }
    }

    fun serializeSkillPointHistory(records: List<SkillPointLevelRecord>): String {
        val array = JSONArray()
        records.sortedWith(compareBy<SkillPointLevelRecord> { it.characterLevel }.thenBy { it.className.lowercase() })
            .forEach { record ->
                val obj = JSONObject()
                obj.put("characterLevel", record.characterLevel)
                obj.put("className", record.className.trim())
                obj.put("availablePoints", record.availablePoints.toDouble())
                val allocations = JSONArray()
                record.allocations.forEach { allocation ->
                    val entry = JSONObject()
                    entry.put("skillName", allocation.skillName.trim())
                    entry.put("pointsSpent", allocation.pointsSpent.toDouble())
                    allocations.put(entry)
                }
                obj.put("allocations", allocations)
                array.put(obj)
            }
        return array.toString()
    }

    fun deserializeSkillPointHistory(json: String?): List<SkillPointLevelRecord> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).mapNotNull recordLoop@{ index ->
                val obj = array.optJSONObject(index) ?: return@recordLoop null
                val level = obj.optInt("characterLevel", 0)
                val className = obj.optString("className", "").trim()
                if (level < 1 || className.isEmpty()) return@recordLoop null
                val allocationsJson = obj.optJSONArray("allocations") ?: JSONArray()
                val allocations = (0 until allocationsJson.length()).mapNotNull allocationLoop@{ allocationIndex ->
                    val allocation = allocationsJson.optJSONObject(allocationIndex) ?: return@allocationLoop null
                    val skillName = allocation.optString("skillName", "").trim()
                    val points = allocation.optDouble("pointsSpent", 0.0).toFloat()
                    if (skillName.isEmpty() || !points.isFinite() || points < 0f) null
                    else SkillPointAllocation(skillName, points)
                }
                val available = obj.optDouble("availablePoints", Double.NaN).toFloat()
                if (!available.isFinite() || available < 0f) null
                else SkillPointLevelRecord(level, className, available, allocations)
            }
        } catch (e: Exception) {
            Log.w("CharacterJsonAdapters", "JSON de histórico de pontos de perícia inválido", e)
            emptyList()
        }
    }

    fun serializeSkills(skills: List<CharacterSkill>): String {
        val array = JSONArray()
        for (s in skills) {
            val obj = JSONObject()
            obj.put("name", s.name)
            obj.put("keyAbility", s.keyAbility)
            obj.put("ranks", s.ranks.toDouble())
            obj.put("miscBonus", s.miscBonus)
            obj.put("isClassSkill", s.isClassSkill)
            obj.put("notes", s.notes)
            array.put(obj)
        }
        return array.toString()
    }

    fun deserializeSkills(json: String?): List<CharacterSkill> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<CharacterSkill>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CharacterSkill(
                        name = obj.optString("name"),
                        keyAbility = obj.optString("keyAbility", "FOR"),
                        ranks = obj.optDouble("ranks", 0.0).toFloat(),
                        miscBonus = obj.optInt("miscBonus", 0),
                        isClassSkill = obj.optBoolean("isClassSkill", false),
                        notes = obj.optString("notes", "")
                    )
                )
            }
        } catch (e: Exception) {
            Log.w("CharacterJsonAdapters", "JSON de perícias inválido; dados anteriores ao erro foram preservados", e)
        }
        return list
    }

    fun serializeInventory(items: List<CharacterInventoryItem>): String {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("name", item.name)
            obj.put("category", item.category)
            obj.put("quantity", item.quantity)
            obj.put("weightLb", item.weightLb.toDouble())
            obj.put("costGp", item.costGp.toDouble())
            obj.put("damageOrAc", item.damageOrAc)
            obj.put("equipped", item.equipped)
            obj.put("equippedSlot", item.equippedSlot)
            obj.put("notes", item.notes)
            array.put(obj)
        }
        return array.toString()
    }

    fun deserializeInventory(json: String?): List<CharacterInventoryItem> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<CharacterInventoryItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val isEq = obj.optBoolean("equipped", false)
                val defaultSlot = if (isEq) {
                    when (obj.optString("category", "Equipamento").lowercase()) {
                        "armas", "arma" -> Dnd35Rules.EquipmentSlots.MAIN_HAND
                        "armaduras", "armadura" -> Dnd35Rules.EquipmentSlots.ARMOR_BODY
                        "escudos", "escudo" -> Dnd35Rules.EquipmentSlots.OFF_HAND
                        else -> Dnd35Rules.EquipmentSlots.MAIN_HAND
                    }
                } else {
                    Dnd35Rules.EquipmentSlots.NONE
                }

                list.add(
                    CharacterInventoryItem(
                        id = obj.optString("id"),
                        name = obj.optString("name"),
                        category = obj.optString("category", "Itens de Aventura"),
                        quantity = obj.optInt("quantity", 1),
                        weightLb = obj.optDouble("weightLb", 0.0).toFloat(),
                        costGp = obj.optDouble("costGp", 0.0).toFloat(),
                        damageOrAc = obj.optString("damageOrAc", ""),
                        equipped = isEq,
                        equippedSlot = obj.optString("equippedSlot", defaultSlot),
                        notes = obj.optString("notes", "")
                    )
                )
            }
        } catch (e: Exception) {
            Log.w("CharacterJsonAdapters", "JSON de inventário inválido; dados anteriores ao erro foram preservados", e)
        }
        return list
    }

    fun serializeFeats(feats: List<CharacterFeat>): String {
        val array = JSONArray()
        for (f in feats) {
            val obj = JSONObject()
            obj.put("id", f.id)
            obj.put("name", f.name)
            obj.put("prerequisites", f.prerequisites)
            obj.put("summary", f.summary)
            obj.put("description", f.description)
            array.put(obj)
        }
        return array.toString()
    }

    fun deserializeFeats(json: String?): List<CharacterFeat> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<CharacterFeat>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CharacterFeat(
                        id = obj.optString("id"),
                        name = obj.optString("name"),
                        prerequisites = obj.optString("prerequisites", ""),
                        summary = obj.optString("summary", ""),
                        description = obj.optString("description", "")
                    )
                )
            }
        } catch (e: Exception) {
            Log.w("CharacterJsonAdapters", "JSON de talentos inválido; dados anteriores ao erro foram preservados", e)
        }
        return list
    }

    fun serializeSpells(spells: List<CharacterSpell>): String {
        val array = JSONArray()
        for (s in spells) {
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("name", s.name)
            obj.put("level", s.level)
            obj.put("school", s.school)
            obj.put("castTime", s.castTime)
            obj.put("components", s.components)
            obj.put("range", s.range)
            obj.put("duration", s.duration)
            obj.put("savingThrow", s.savingThrow)
            obj.put("spellResistance", s.spellResistance)
            obj.put("prepared", s.prepared)
            obj.put("preparedCount", s.preparedCount)
            obj.put("description", s.description)
            array.put(obj)
        }
        return array.toString()
    }

    fun deserializeSpells(json: String?): List<CharacterSpell> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<CharacterSpell>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CharacterSpell(
                        id = obj.optString("id"),
                        name = obj.optString("name"),
                        level = obj.optInt("level", 0),
                        school = obj.optString("school", ""),
                        castTime = obj.optString("castTime", "1 ação padrão"),
                        components = obj.optString("components", "V, S"),
                        range = obj.optString("range", "Toque"),
                        duration = obj.optString("duration", "Instantânea"),
                        savingThrow = obj.optString("savingThrow", "Nenhum"),
                        spellResistance = obj.optBoolean("spellResistance", true),
                        prepared = obj.optBoolean("prepared", true),
                        preparedCount = obj.optInt("preparedCount", 1),
                        description = obj.optString("description", "")
                    )
                )
            }
        } catch (e: Exception) {
            Log.w("CharacterJsonAdapters", "JSON de magias inválido; dados anteriores ao erro foram preservados", e)
        }
        return list
    }

    fun serializeSpellSlots(slots: List<SpellSlotInfo>): String {
        val array = JSONArray()
        for (slot in slots) {
            val obj = JSONObject()
            obj.put("level", slot.level)
            obj.put("totalSlots", slot.totalSlots)
            obj.put("usedSlots", slot.usedSlots)
            array.put(obj)
        }
        return array.toString()
    }

    fun deserializeSpellSlots(json: String?): List<SpellSlotInfo> {
        // totalSlots aqui só importa para fichas muito antigas; o cálculo atual
        // (CharacterCalculator.compute) sempre recalcula o total automaticamente e só lê
        // usedSlots desta lista.
        if (json.isNullOrBlank()) {
            return (0..9).map { SpellSlotInfo(level = it, totalSlots = 0, usedSlots = 0) }
        }
        val list = mutableListOf<SpellSlotInfo>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    SpellSlotInfo(
                        level = obj.optInt("level", i),
                        totalSlots = obj.optInt("totalSlots", 0),
                        usedSlots = obj.optInt("usedSlots", 0)
                    )
                )
            }
        } catch (e: Exception) {
            Log.w("CharacterJsonAdapters", "JSON de espaços de magia inválido; dados anteriores ao erro foram preservados", e)
        }
        if (list.isEmpty()) {
            return (0..9).map { SpellSlotInfo(level = it, totalSlots = 0, usedSlots = 0) }
        }
        return list
    }

    fun serializeClassEntries(classes: List<CharacterClassEntry>): String {
        val array = JSONArray()
        for (c in classes) {
            val obj = JSONObject()
            obj.put("className", c.className)
            obj.put("level", c.level)
            array.put(obj)
        }
        return array.toString()
    }

    fun deserializeClassEntries(json: String?, fallbackClass: String = "Guerreiro", fallbackLevel: Int = 1): List<CharacterClassEntry> {
        val cleanFallbackClass = fallbackClass.split("/").firstOrNull()?.trim()?.split(" ")?.firstOrNull()?.trim()
            ?.ifBlank { "Guerreiro" } ?: "Guerreiro"
        val cleanFallbackLevel = fallbackLevel.coerceAtLeast(1)

        if (json.isNullOrBlank()) {
            return listOf(CharacterClassEntry(cleanFallbackClass, cleanFallbackLevel))
        }
        val list = mutableListOf<CharacterClassEntry>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val name = obj.optString("className", cleanFallbackClass)
                val lvl = obj.optInt("level", 1).coerceAtLeast(1)
                list.add(CharacterClassEntry(name, lvl))
            }
        } catch (e: Exception) {
            Log.w("CharacterJsonAdapters", "JSON de classes inválido; dados anteriores ao erro foram preservados", e)
        }
        return if (list.isEmpty()) listOf(CharacterClassEntry(cleanFallbackClass, cleanFallbackLevel)) else list
    }
}
