package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
