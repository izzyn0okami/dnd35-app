package com.example.dnd35.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/** Um conjunto de ícones (ou autor, quando a licença exige crédito por autor) usado no app. */
data class IconCredit(
    val title: String,
    val author: String,
    val license: String,
    val url: String
)

/**
 * Créditos dos ícones exibidos no app. Qualquer conjunto novo de ícones só pode ser usado
 * depois de entrar aqui com autor, licença e link.
 */
object IconCredits {
    val entries: List<IconCredit> = listOf(
        IconCredit(
            title = "Material Icons",
            author = "Google",
            license = "Apache License 2.0",
            url = "https://github.com/google/material-design-icons"
        )
    )
}

@Composable
fun CreditsDialog(
    onDismiss: () -> Unit,
    credits: List<IconCredit> = IconCredits.entries
) {
    val uriHandler = LocalUriHandler.current
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().heightIn(max = 640.dp).testTag("credits_dialog")) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Créditos e licenças",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ícones dos itens do inventário",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    credits.forEach { credit ->
                        Column(modifier = Modifier.fillMaxWidth().clickable { uriHandler.openUri(credit.url) }) {
                            Text(credit.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text("Autor: ${credit.author}", style = MaterialTheme.typography.bodySmall)
                            Text("Licença: ${credit.license}", style = MaterialTheme.typography.bodySmall)
                            Text(
                                credit.url,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Fechar")
                }
            }
        }
    }
}
