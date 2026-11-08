package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.os.Build
import android.view.FrameMetrics
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.dnd35.diagnostics.CrashLogStore
import com.example.dnd35.diagnostics.isSlowFrameDuration
import com.example.dnd35.ui.DndMainScreen
import com.example.dnd35.viewmodel.CharacterViewModel
import com.example.dnd35.viewmodel.SessionViewModel
import com.example.ui.theme.MyApplicationTheme

/**
 * `singleTop` no Manifest evita empilhar uma nova Activity a cada toque num arquivo .fichadnd35/
 * .itemdnd35 (ex: abrindo vários anexos em sequência) — por isso o [onNewIntent] também precisa
 * tratar o Uri recebido, não só o [onCreate].
 */
class MainActivity : ComponentActivity() {
    private var pendingFileUri = mutableStateOf<Uri?>(null)
    private var frameMetricsThread: HandlerThread? = null
    private var frameMetricsListener: Window.OnFrameMetricsAvailableListener? = null
    private var lastSlowFrameLogAt = 0L
    private var resumeStartedAt = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingFileUri.value = viewIntentUri(intent)
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val characterViewModel: CharacterViewModel = viewModel()
                    val sessionViewModel: SessionViewModel = viewModel()
                    val incomingFileUri by pendingFileUri
                    DndMainScreen(
                        viewModel = characterViewModel,
                        sessionViewModel = sessionViewModel,
                        incomingFileUri = incomingFileUri,
                        onIncomingFileHandled = { pendingFileUri.value = null }
                    )
                }
            }
        }
        startFrameMetricsMonitoring()
    }

    override fun onResume() {
        super.onResume()
        resumeStartedAt = SystemClock.elapsedRealtime()
        recordLifecycleEvent("LIFECYCLE_RESUME", "Activity retomada")
        window.decorView.postOnAnimation {
            val elapsed = SystemClock.elapsedRealtime() - resumeStartedAt
            if (elapsed >= 250L) {
                recordLifecycleEvent(
                    "SLOW_RESUME_FRAME_CALLBACK",
                    "Tempo até o próximo callback de animação: ${elapsed}ms"
                )
            }
        }
    }

    override fun onPause() {
        recordLifecycleEvent("LIFECYCLE_PAUSE", "Activity pausada")
        super.onPause()
    }

    override fun onDestroy() {
        frameMetricsListener?.let { listener ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                window.removeOnFrameMetricsAvailableListener(listener)
            }
        }
        frameMetricsListener = null
        frameMetricsThread?.quitSafely()
        frameMetricsThread = null
        super.onDestroy()
    }

    private fun recordLifecycleEvent(category: String, message: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            CrashLogStore.record(category, message)
        }
    }

    private fun startFrameMetricsMonitoring() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
        val thread = HandlerThread("Dnd35FrameMetrics").apply { start() }
        frameMetricsThread = thread
        val listener = Window.OnFrameMetricsAvailableListener { _, metrics, droppedReports ->
            val durationNanos = metrics.getMetric(FrameMetrics.TOTAL_DURATION)
            if (isSlowFrameDuration(durationNanos)) {
                val now = SystemClock.elapsedRealtime()
                if (now - lastSlowFrameLogAt >= 5_000L) {
                    lastSlowFrameLogAt = now
                    CrashLogStore.record(
                        "SLOW_FRAME",
                        "Duração total=${durationNanos / 1_000_000.0}ms; relatórios descartados=$droppedReports"
                    )
                }
            }
        }
        frameMetricsListener = listener
        window.addOnFrameMetricsAvailableListener(listener, Handler(thread.looper))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingFileUri.value = viewIntentUri(intent)
    }

    private fun viewIntentUri(intent: Intent?): Uri? {
        return if (intent?.action == Intent.ACTION_VIEW) intent.data else null
    }
}
