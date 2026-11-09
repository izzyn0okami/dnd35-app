package com.example

import android.app.Application
import android.content.ComponentCallbacks2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.example.dnd35.diagnostics.CrashLogStore

/** Inicializa o registro persistente de falhas antes da criação das telas. */
class Dnd35Application : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        CrashLogStore.initialize(this)
        applicationScope.launch {
            CrashLogStore.recordHistoricalProcessExits(this@Dnd35Application)
            CrashLogStore.recordMemorySnapshot(this@Dnd35Application, "APP_START")
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        applicationScope.launch {
            CrashLogStore.recordMemorySnapshot(
                this@Dnd35Application,
                "ON_TRIM_MEMORY",
                "level=$level (${trimMemoryLevelName(level)})"
            )
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        applicationScope.launch {
            CrashLogStore.recordMemorySnapshot(this@Dnd35Application, "ON_LOW_MEMORY")
        }
    }

    private fun trimMemoryLevelName(level: Int): String = when (level) {
        ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> "RUNNING_MODERATE"
        ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> "RUNNING_LOW"
        ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> "RUNNING_CRITICAL"
        ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> "UI_HIDDEN"
        ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> "BACKGROUND"
        ComponentCallbacks2.TRIM_MEMORY_MODERATE -> "MODERATE"
        ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> "COMPLETE"
        else -> "UNKNOWN"
    }
}
