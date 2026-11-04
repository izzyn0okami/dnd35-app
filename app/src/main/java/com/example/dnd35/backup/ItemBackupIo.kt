package com.example.dnd35.backup

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.dnd35.model.CharacterInventoryItem
import com.example.dnd35.model.CharacterJsonAdapters
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * Backup portátil de item. A imagem é incorporada ao JSON como Base64 para que
 * o arquivo não dependa do caminho privado do aparelho que o exportou.
 */
object ItemBackupIo {
    private const val APP_ID = "ficha-dnd35-item"
    private const val FORMAT_VERSION = 1
    private const val PHOTO_DIR = "item_photos"
    private const val MAX_IMAGE_BYTES = 5 * 1024 * 1024

    data class ImportedItems(
        val items: List<CharacterInventoryItem>,
        val warnings: List<String> = emptyList()
    )

    fun writeItemBackup(context: Context, target: Uri, item: CharacterInventoryItem) {
        val serialized = JSONArray(CharacterJsonAdapters.serializeInventory(listOf(item)))
        val itemJson = serialized.optJSONObject(0)
            ?: throw BackupFileException("Não foi possível preparar o item para exportação.")
        val imageUri = item.imageUri
        if (!imageUri.isNullOrBlank()) {
            readPrivateItemPhoto(context, imageUri)?.let { bytes ->
                itemJson.put("imageBase64", Base64.encodeToString(bytes, Base64.NO_WRAP))
            }
        }
        // O caminho local nunca é compartilhado: o receptor cria seu próprio arquivo.
        itemJson.remove("imageUri")

        val root = JSONObject()
            .put("app", APP_ID)
            .put("formatVersion", FORMAT_VERSION)
            .put("items", JSONArray().put(itemJson))
        CharacterBackupIo.writeText(context, target, root.toString(2))
    }

    fun readItemBackup(context: Context, source: Uri): ImportedItems {
        val text = CharacterBackupIo.readBackupText(context, source)
        val warnings = mutableListOf<String>()
        val itemsArray = try {
            if (text.trimStart().startsWith("[")) {
                // Compatibilidade com arquivos de item da versão anterior (JSON array puro).
                JSONArray(text)
            } else {
                val root = JSONObject(text)
                if (root.optString("app") != APP_ID) {
                    throw BackupFileException("Este arquivo não é um backup de item do D&D 3.5.")
                }
                val version = root.optInt("formatVersion", 0)
                if (version < 1 || version > FORMAT_VERSION) {
                    throw BackupFileException("Versão do arquivo de item não suportada.")
                }
                root.optJSONArray("items")
                    ?: throw BackupFileException("O arquivo não contém uma lista de itens.")
            }
        } catch (e: JSONException) {
            throw BackupFileException("O arquivo de item está corrompido ou não é um JSON válido.")
        }

        val normalized = JSONArray()
        for (index in 0 until itemsArray.length()) {
            val item = itemsArray.optJSONObject(index) ?: continue
            val encoded = item.optString("imageBase64", "")
            item.remove("imageBase64")
            // Nunca reutilizar imageUri do dispositivo de origem.
            item.remove("imageUri")
            if (encoded.isNotEmpty()) {
                val bytes = decodeImage(encoded)
                val savedUri = bytes?.let { savePrivateItemPhoto(context, it) }
                if (savedUri != null) {
                    item.put("imageUri", savedUri)
                } else {
                    warnings += "A imagem do item ${index + 1} estava inválida ou excedia o limite e foi ignorada."
                }
            }
            normalized.put(item)
        }

        val items = try {
            CharacterJsonAdapters.deserializeInventory(normalized.toString())
        } catch (_: Exception) {
            emptyList()
        }
        return ImportedItems(items, warnings)
    }

    private fun readPrivateItemPhoto(context: Context, photoUri: String): ByteArray? {
        return try {
            val uri = Uri.parse(photoUri)
            if (uri.scheme != "file") return null
            val file = File(uri.path ?: return null).canonicalFile
            val directory = File(context.filesDir, PHOTO_DIR).canonicalFile
            if (file.parentFile != directory || !file.isFile || file.length() !in 1..MAX_IMAGE_BYTES.toLong()) {
                return null
            }
            val bytes = file.readBytes()
            if (isValidImage(bytes)) bytes else null
        } catch (_: Exception) {
            null
        }
    }

    private fun decodeImage(encoded: String): ByteArray? {
        if (encoded.length > (MAX_IMAGE_BYTES * 4 / 3) + 16) return null
        if (!encoded.all {
                it in 'A'..'Z' || it in 'a'..'z' || it in '0'..'9' ||
                    it == '+' || it == '/' || it == '=' || it == '\n' || it == '\r'
            }) return null
        return try {
            Base64.decode(encoded, Base64.DEFAULT).takeIf {
                it.size in 1..MAX_IMAGE_BYTES && isValidImage(it)
            }
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun isValidImage(bytes: ByteArray): Boolean {
        if (bytes.isEmpty() || bytes.size > MAX_IMAGE_BYTES) return false
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        return bounds.outWidth > 0 && bounds.outHeight > 0
    }

    private fun savePrivateItemPhoto(context: Context, bytes: ByteArray): String? {
        if (!isValidImage(bytes)) return null
        return try {
            val directory = File(context.filesDir, PHOTO_DIR).apply {
                if (!isDirectory && !mkdirs()) return null
            }
            val file = File(directory, "${UUID.randomUUID()}.jpg")
            try {
                file.writeBytes(bytes)
                Uri.fromFile(file).toString()
            } catch (_: Exception) {
                file.delete()
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
