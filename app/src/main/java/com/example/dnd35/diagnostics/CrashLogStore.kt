package com.example.dnd35.diagnostics

import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Registro local de falhas e diagnóstico. Não envia dados pela rede: os arquivos ficam
 * no armazenamento privado do app e só saem do aparelho quando o usuário escolhe exportá-los.
 */
object CrashLogStore {
    private const val DIRECTORY = "diagnostics"
    private const val LOG_FILE = "diagnostics.log"
    private const val MAX_FILE_BYTES = 512 * 1024L
    private const val ROTATED_FILES = 3

    @Volatile
    private var appContext: Context? = null

    @Volatile
    private var handlerInstalled = false

    fun initialize(context: Context) {
        appContext = context.applicationContext
        if (handlerInstalled) return
        synchronized(this) {
            if (handlerInstalled) return
            val previous = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, error ->
                record("FATAL_CRASH", "Falha não tratada na thread '${thread.name}'", error)
                if (previous != null) {
                    previous.uncaughtException(thread, error)
                } else {
                    Process.killProcess(Process.myPid())
                    kotlin.system.exitProcess(10)
                }
            }
            handlerInstalled = true
        }
    }

    @Synchronized
    fun record(category: String, message: String, error: Throwable? = null) {
        val context = appContext ?: return
        runCatching {
            val directory = File(context.filesDir, DIRECTORY)
            if (!directory.exists() && !directory.mkdirs()) return
            val file = File(directory, LOG_FILE)
            if (file.exists() && file.length() >= MAX_FILE_BYTES) rotate(directory, file)

            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z", Locale.ROOT).format(Date())
            val stack = error?.let {
                StringWriter().also { writer -> it.printStackTrace(PrintWriter(writer)) }.toString()
            }.orEmpty()

            FileOutputStream(file, true).bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.appendLine("[$timestamp] [$category] $message")
                writer.appendLine("App: ${context.packageName}")
                writer.appendLine("Versão: ${runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "desconhecida"}")
                writer.appendLine("Android: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
                writer.appendLine("Dispositivo: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                if (stack.isNotBlank()) writer.appendLine(stack)
                writer.appendLine("----------------------------------------------------------------")
            }
        }
    }

    private fun rotate(directory: File, current: File) {
        File(directory, "$LOG_FILE.$ROTATED_FILES").delete()
        for (index in ROTATED_FILES - 1 downTo 1) {
            val source = File(directory, "$LOG_FILE.$index")
            if (source.exists()) source.renameTo(File(directory, "$LOG_FILE.${index + 1}"))
        }
        current.renameTo(File(directory, "$LOG_FILE.1"))
    }

    fun recordHistoricalProcessExits(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        runCatching {
            val preferences = context.getSharedPreferences("diagnostics_state", Context.MODE_PRIVATE)
            val lastRecordedAt = preferences.getLong("last_process_exit_timestamp", 0L)
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val exits = manager.getHistoricalProcessExitReasons(context.packageName, 0, 10)
            var newestTimestamp = lastRecordedAt
            val abnormalReasons = setOf(
                ApplicationExitInfo.REASON_ANR,
                ApplicationExitInfo.REASON_CRASH,
                ApplicationExitInfo.REASON_CRASH_NATIVE,
                ApplicationExitInfo.REASON_LOW_MEMORY,
                ApplicationExitInfo.REASON_INITIALIZATION_FAILURE,
                ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE,
                ApplicationExitInfo.REASON_SIGNALED
            )
            exits.sortedBy { it.timestamp }.forEach { exit ->
                newestTimestamp = maxOf(newestTimestamp, exit.timestamp)
                if (exit.timestamp <= lastRecordedAt || exit.reason !in abnormalReasons) return@forEach
                val trace = if (exit.reason == ApplicationExitInfo.REASON_ANR) {
                    runCatching {
                        exit.traceInputStream?.bufferedReader()?.use { it.readText().take(12_000) }
                    }.getOrNull()
                } else null
                val details = buildString {
                    append("Processo=${exit.processName}; motivo=${exitReasonName(exit.reason)}")
                    append("; timestamp=${exit.timestamp}; status=${exit.status}; importance=${exit.importance}")
                    if (!exit.description.isNullOrBlank()) append("; descrição=${exit.description}")
                    if (!trace.isNullOrBlank()) appendLine().append("Trace ANR:\n").append(trace)
                }
                record("HISTORICAL_PROCESS_EXIT", details)
            }
            if (newestTimestamp > lastRecordedAt) {
                preferences.edit().putLong("last_process_exit_timestamp", newestTimestamp).commit()
            }
        }
    }

    private fun exitReasonName(reason: Int): String = when (reason) {
        ApplicationExitInfo.REASON_ANR -> "ANR"
        ApplicationExitInfo.REASON_CRASH -> "CRASH"
        ApplicationExitInfo.REASON_CRASH_NATIVE -> "CRASH_NATIVE"
        ApplicationExitInfo.REASON_LOW_MEMORY -> "LOW_MEMORY"
        ApplicationExitInfo.REASON_INITIALIZATION_FAILURE -> "INITIALIZATION_FAILURE"
        ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE -> "EXCESSIVE_RESOURCE_USAGE"
        ApplicationExitInfo.REASON_SIGNALED -> "SIGNALED"
        else -> "OTHER_${reason}"
    }

    /** Abre o compartilhamento do relatório consolidado; o usuário escolhe o destino. */
    fun shareLogs(context: Context) {
        val directory = File(context.filesDir, DIRECTORY)
        val logs = directory.listFiles()
            ?.filter { it.isFile && it.name.startsWith(LOG_FILE) }
            ?.sortedByDescending { it.lastModified() }
            .orEmpty()

        val export = File(context.cacheDir, "dnd35-diagnostic-report.txt")
        export.writeText(
            buildString {
                appendLine("D&D 3.5 — Relatório de diagnóstico")
                appendLine("Gerado em: ${Date()}")
                appendLine("Pacote: ${context.packageName}")
                appendLine("Versão: ${runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "desconhecida"}")
                appendLine("Android: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
                appendLine("Dispositivo: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                appendLine()
                if (logs.isEmpty()) {
                    appendLine("Nenhum erro registrado até o momento.")
                } else {
                    logs.forEach { log ->
                        appendLine("===== ${log.name} =====")
                        appendLine(runCatching { log.readText() }.getOrDefault("Não foi possível ler este arquivo."))
                        appendLine()
                    }
                }
            },
            Charsets.UTF_8
        )

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            export
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Relatório de diagnóstico — D&D 3.5")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Exportar logs de diagnóstico"))
    }
}
