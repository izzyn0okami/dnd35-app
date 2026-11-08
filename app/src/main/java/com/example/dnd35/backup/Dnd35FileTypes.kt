package com.example.dnd35.backup

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

/** O que um arquivo tocado pelo usuário (ACTION_VIEW) representa, pra saber como importar. */
enum class Dnd35FileKind { CHARACTER, ITEM }

/**
 * Extensões e tipos MIME próprios dos arquivos que o app exporta (ficha e item de inventário
 * avulso), para que o Android ofereça este app ao tocar um desses arquivos num gerenciador de
 * arquivos ou anexo — ver intent-filters no AndroidManifest.xml. O conteúdo continua sendo o
 * mesmo JSON de sempre; só a extensão/MIME muda.
 */
object Dnd35FileTypes {
    const val CHARACTER_MIME = "application/vnd.dnd35.ficha+json"
    const val CHARACTER_EXTENSION = ".fichadnd35"

    const val ITEM_MIME = "application/vnd.dnd35.item+json"
    const val ITEM_EXTENSION = ".itemdnd35"

    /**
     * Tipos aceitos ao importar fichas: o próprio tipo novo mais os genéricos usados antes dessa
     * mudança, pra não quebrar backups já salvos.
     */
    val CHARACTER_IMPORT_MIME_TYPES = arrayOf(
        CHARACTER_MIME, "application/json", "application/octet-stream", "text/plain"
    )

    /** Tipos aceitos ao importar um arquivo de item avulso. */
    val ITEM_IMPORT_MIME_TYPES = arrayOf(
        ITEM_MIME, "application/json", "application/octet-stream", "text/plain"
    )

    /**
     * Identifica ficha ou item pelo MIME e, como respaldo, pelo nome real do arquivo.
     *
     * Em URIs content:// o caminho normalmente contém um ID interno do provedor, não o nome
     * do arquivo. Por isso consultamos OpenableColumns.DISPLAY_NAME em vez de depender apenas
     * de uri.toString(). O MIME tem prioridade quando o provedor o preserva.
     */
    fun classify(context: Context, uri: Uri): Dnd35FileKind? {
        val mimeType = runCatching { context.contentResolver.getType(uri) }.getOrNull()
        if (mimeType == CHARACTER_MIME) return Dnd35FileKind.CHARACTER
        if (mimeType == ITEM_MIME) return Dnd35FileKind.ITEM

        val displayName = runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                val nameColumn = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameColumn >= 0 && cursor.moveToFirst()) cursor.getString(nameColumn) else null
            }
        }.getOrNull()

        return classifyFromMetadata(mimeType, displayName, uri.toString())
    }

    /** Mantida separada para testar os metadados típicos de provedores content://. */
    internal fun classifyFromMetadata(
        mimeType: String?,
        displayName: String?,
        uriString: String
    ): Dnd35FileKind? {
        if (mimeType == CHARACTER_MIME) return Dnd35FileKind.CHARACTER
        if (mimeType == ITEM_MIME) return Dnd35FileKind.ITEM

        val candidate = displayName?.takeIf { it.isNotBlank() } ?: uriString
        return when {
            candidate.endsWith(CHARACTER_EXTENSION, ignoreCase = true) -> Dnd35FileKind.CHARACTER
            candidate.endsWith(ITEM_EXTENSION, ignoreCase = true) -> Dnd35FileKind.ITEM
            else -> null
        }
    }
}
