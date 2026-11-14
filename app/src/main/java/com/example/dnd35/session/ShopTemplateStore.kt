package com.example.dnd35.session

import android.content.Context
import com.example.dnd35.model.CharacterInventoryItem
import com.example.dnd35.model.CharacterJsonAdapters

/**
 * Inventários de loja salvos localmente pelo Mestre para reutilização rápida entre sessões.
 * Os modelos guardam o estoque original; compras alteram apenas a cópia da sessão ativa.
 */
object ShopTemplateStore {
    private const val PREFS_NAME = "dnd35_shop_templates"
    private const val KEY_PREFIX = "shop:"

    fun names(context: Context): List<String> =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .all.keys
            .filter { it.startsWith(KEY_PREFIX) }
            .map { it.removePrefix(KEY_PREFIX) }
            .filter { it.isNotBlank() }
            .sortedBy { it.lowercase() }

    fun save(context: Context, name: String, items: List<CharacterInventoryItem>): Boolean {
        val normalized = name.trim()
        if (normalized.isBlank() || items.isEmpty() || items.any { it.quantity <= 0 || it.name.isBlank() }) return false
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PREFIX + normalized, CharacterJsonAdapters.serializeInventory(items))
            .commit()
    }

    fun load(context: Context, name: String): List<CharacterInventoryItem>? {
        val raw = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PREFIX + name.trim(), null) ?: return null
        return runCatching { CharacterJsonAdapters.deserializeInventory(raw) }
            .getOrNull()
            ?.takeIf { it.isNotEmpty() }
    }

    fun delete(context: Context, name: String): Boolean =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_PREFIX + name.trim())
            .commit()
}
