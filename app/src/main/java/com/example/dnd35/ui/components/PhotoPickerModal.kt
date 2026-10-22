package com.example.dnd35.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.DndGold

@Composable
fun PhotoPickerModal(
    currentPhotoUri: String?,
    onPhotoSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Android zero-permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            // Conceder permissão persistente de leitura
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            onPhotoSelected(uri.toString())
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Foto do Personagem", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Visualização atual
                CharacterAvatar(
                    photoUri = currentPhotoUri,
                    modifier = Modifier
                        .size(110.dp)
                        .border(3.dp, DndGold, CircleShape)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botão para selecionar da galeria
                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Escolher da Galeria")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Ou escolha um retrato clássico:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Avatar 1: Guerreiro Paladino
                    PresetAvatarCard(
                        resName = "hero_portrait_default_1792461602152",
                        label = "Guerreiro",
                        onSelect = {
                            onPhotoSelected("preset:hero_portrait_default_1792461602152")
                            onDismiss()
                        }
                    )

                    // Avatar 2: Maga Elfa
                    PresetAvatarCard(
                        resName = "hero_portrait_mage_1792461617980",
                        label = "Maga Elfa",
                        onSelect = {
                            onPhotoSelected("preset:hero_portrait_mage_1792461617980")
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

@Composable
fun PresetAvatarCard(
    resName: String,
    label: String,
    onSelect: () -> Unit
) {
    val context = LocalContext.current
    val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)

    Card(
        modifier = Modifier
            .clickable(onClick = onSelect)
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            if (resId != 0) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = label,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, DndGold, CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun CharacterAvatar(
    photoUri: String?,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape
) {
    val context = LocalContext.current

    if (photoUri.isNullOrBlank()) {
        val defaultRes = context.resources.getIdentifier(
            "hero_portrait_default_1792461602152",
            "drawable",
            context.packageName
        )
        if (defaultRes != 0) {
            Image(
                painter = painterResource(id = defaultRes),
                contentDescription = "Avatar do Personagem",
                modifier = modifier.clip(shape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                Icons.Default.PhotoCamera,
                contentDescription = null,
                modifier = modifier
                    .clip(CircleShape)
                    .padding(16.dp),
                tint = DndGold
            )
        }
    } else if (photoUri.startsWith("preset:")) {
        val resName = photoUri.removePrefix("preset:")
        val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
        if (resId != 0) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = "Avatar",
                modifier = modifier.clip(shape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = modifier)
        }
    } else {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(Uri.parse(photoUri))
                .crossfade(true)
                .build(),
            contentDescription = "Foto Personalizada",
            modifier = modifier.clip(shape),
            contentScale = ContentScale.Crop
        )
    }
}
