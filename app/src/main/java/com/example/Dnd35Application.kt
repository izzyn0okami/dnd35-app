package com.example

import android.app.Application
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
        }
    }
}
