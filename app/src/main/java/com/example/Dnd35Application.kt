package com.example

import android.app.Application
import com.example.dnd35.diagnostics.CrashLogStore

/** Inicializa o registro persistente de falhas antes da criação das telas. */
class Dnd35Application : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashLogStore.initialize(this)
    }
}
