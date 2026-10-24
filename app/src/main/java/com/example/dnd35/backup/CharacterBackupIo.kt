package com.example.dnd35.backup

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.dnd35.model.CharacterEntity
import java.io.ByteArrayOutputStream
import java.io.File

/** Leitura e gravação do arquivo de backup e dos retratos. Executar fora da thread principal. */
object CharacterBackupIo {
    private const val PORTRAIT_DIR = "character-portraits"
    private const val MAX_FILE_BYTES = 30L * 1024 * 1024
    private const val MAX_PORTRAIT_BYTES = 5L * 1024 * 1024

    fun writeBackup(context: Context, target: Uri, characters: List<CharacterEntity>) {
        val json = CharacterBackup.toJson(characters) { photoUri -> readPortrait(context, photoUri) }
        val output = context.contentResolver.openOutputStream(target, "wt")
            ?: throw java.io.IOException("Não foi possível abrir o arquivo de destino.")
        output.use { it.write(json.toByteArray(Charsets.UTF_8)) }
    }

    /** Lê o arquivo escolhido pelo usuário. Falha com [BackupFileException] se for grande demais. */
    fun readBackupText(context: Context, source: Uri): String {
        val input = context.contentResolver.openInputStream(source)
            ?: throw java.io.IOException("Não foi possível abrir o arquivo selecionado.")
        return input.use { stream ->
            val buffer = ByteArrayOutputStream()
            val chunk = ByteArray(64 * 1024)
            var total = 0L
            while (true) {
                val read = stream.read(chunk)
                if (read < 0) break
                total += read
                if (total > MAX_FILE_BYTES) {
                    throw BackupFileException("O arquivo é grande demais para ser um backup de fichas.")
                }
                buffer.write(chunk, 0, read)
            }
            buffer.toString(Charsets.UTF_8.name())
        }
    }

    /** Salva os bytes do retrato na pasta do app e devolve o novo `photoUri`, ou null se não for imagem. */
    fun savePortrait(context: Context, bytes: ByteArray, sequence: Int): String? {
        if (bytes.size > MAX_PORTRAIT_BYTES) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val dir = File(context.filesDir, PORTRAIT_DIR).apply { mkdirs() }
        val file = File(dir, "portrait-${System.currentTimeMillis()}-$sequence.jpg")
        file.writeBytes(bytes)
        return Uri.fromFile(file).toString()
    }

    // Só lê retratos que estão na pasta de retratos do próprio app.
    private fun readPortrait(context: Context, photoUri: String): ByteArray? {
        return try {
            val uri = Uri.parse(photoUri)
            if (uri.scheme != "file") return null
            val file = File(uri.path ?: return null).canonicalFile
            val dir = File(context.filesDir, PORTRAIT_DIR).canonicalFile
            if (file.parentFile != dir || !file.isFile || file.length() > MAX_PORTRAIT_BYTES) return null
            file.readBytes()
        } catch (_: Exception) {
            null
        }
    }
}

class BackupFileException(message: String) : Exception(message)
