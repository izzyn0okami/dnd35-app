package com.example.dnd35.backup

import android.content.Context
import android.net.Uri

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
     * Identifica se um arquivo tocado pelo usuário (ex: via ACTION_VIEW, abrindo no gerenciador
     * de arquivos) é uma ficha ou um item avulso — primeiro pelo MIME que o provedor de
     * documentos guardou na exportação, com a extensão do nome como respaldo pra provedores que
     * não preservam o MIME original. Retorna `null` se não reconhecer nenhum dos dois.
     */
    fun classify(context: Context, uri: Uri): Dnd35FileKind? {
        when (context.contentResolver.getType(uri)) {
            CHARACTER_MIME -> return Dnd35FileKind.CHARACTER
            ITEM_MIME -> return Dnd35FileKind.ITEM
        }
        val path = uri.toString()
        return when {
            path.endsWith(CHARACTER_EXTENSION, ignoreCase = true) -> Dnd35FileKind.CHARACTER
            path.endsWith(ITEM_EXTENSION, ignoreCase = true) -> Dnd35FileKind.ITEM
            else -> null
        }
    }
}
