package com.example.dnd35.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt
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
    var pendingCropUri by remember { mutableStateOf<Uri?>(null) }

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
            pendingCropUri = uri
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

    if (pendingCropUri != null) {
        SquarePhotoCropDialog(sourceUri = pendingCropUri!!, onCancel = { pendingCropUri = null }, onConfirm = { croppedUri ->
            onPhotoSelected(croppedUri)
            pendingCropUri = null
            onDismiss()
        })
    }
}

@Composable
fun SquarePhotoCropDialog(sourceUri: Uri, onCancel: () -> Unit, onConfirm: (String) -> Unit) {
    val context = LocalContext.current
    var bitmap by remember(sourceUri) { mutableStateOf<Bitmap?>(null) }
    var loadFailed by remember(sourceUri) { mutableStateOf(false) }
    var zoom by remember(sourceUri) { mutableFloatStateOf(1f) }
    var pan by remember(sourceUri) { mutableStateOf(Offset.Zero) }
    var viewportSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    LaunchedEffect(sourceUri) {
        bitmap = try { context.contentResolver.openInputStream(sourceUri)?.use { BitmapFactory.decodeStream(it) } } catch (_: Exception) { null }
        loadFailed = bitmap == null
    }
    AlertDialog(onDismissRequest = onCancel, title = { Text("Recortar foto") }, text = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Arraste para ajustar o enquadramento. Use pinça para aproximar ou afastar.", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            val source = bitmap
            if (source != null) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                    val density = androidx.compose.ui.platform.LocalDensity.current
                    val widthPx = with(density) { maxWidth.toPx() }
                    val heightPx = with(density) { maxHeight.toPx() }
                    Canvas(modifier = Modifier.fillMaxSize().pointerInput(source, widthPx, heightPx) {
                        detectTransformGestures { _, panChange, zoomChange, _ ->
                            zoom = (zoom * zoomChange).coerceIn(1f, 5f)
                            val fit = max(widthPx / source.width, heightPx / source.height)
                            val limitX = max(0f, (source.width * fit * zoom - widthPx) / 2f)
                            val limitY = max(0f, (source.height * fit * zoom - heightPx) / 2f)
                            pan = Offset((pan.x + panChange.x).coerceIn(-limitX, limitX), (pan.y + panChange.y).coerceIn(-limitY, limitY))
                        }
                    }) {
                        viewportSize = androidx.compose.ui.unit.IntSize(size.width.roundToInt(), size.height.roundToInt())
                        val fit = max(size.width / source.width, size.height / source.height)
                        val sw = (source.width * fit * zoom).roundToInt()
                        val sh = (source.height * fit * zoom).roundToInt()
                        val left = ((size.width - sw) / 2f + pan.x).roundToInt()
                        val top = ((size.height - sh) / 2f + pan.y).roundToInt()
                        drawImage(source.asImageBitmap(), dstOffset = androidx.compose.ui.unit.IntOffset(left, top), dstSize = androidx.compose.ui.unit.IntSize(sw, sh))
                        drawRect(color = Color.White.copy(alpha = 0.9f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
                    }
                }
            } else if (loadFailed) Text("Não foi possível abrir essa imagem. Escolha outra foto.", color = MaterialTheme.colorScheme.error)
            else Text("Carregando imagem…")
        }
    }, confirmButton = {
        Button(enabled = bitmap != null && viewportSize.width > 0 && viewportSize.height > 0, onClick = {
            val source = bitmap ?: return@Button
            try {
                val fit = max(viewportSize.width.toFloat() / source.width, viewportSize.height.toFloat() / source.height)
                val scale = fit * zoom
                val side = minOf(viewportSize.width, viewportSize.height) / scale
                val left = (source.width / 2f - pan.x / scale - side / 2f).roundToInt().coerceIn(0, (source.width - side).roundToInt().coerceAtLeast(0))
                val top = (source.height / 2f - pan.y / scale - side / 2f).roundToInt().coerceIn(0, (source.height - side).roundToInt().coerceAtLeast(0))
                val cropSide = side.roundToInt().coerceAtLeast(1).coerceAtMost(minOf(source.width - left, source.height - top))
                val cropped = Bitmap.createBitmap(source, left, top, cropSide, cropSide)
                val directory = File(context.filesDir, "character-portraits").apply { mkdirs() }
                val output = File(directory, "portrait-" + System.currentTimeMillis() + ".jpg")
                FileOutputStream(output).use { cropped.compress(Bitmap.CompressFormat.JPEG, 92, it) }
                if (cropped !== source) cropped.recycle()
                onConfirm(Uri.fromFile(output).toString())
            } catch (_: Exception) { loadFailed = true }
        }) { Text("Usar foto") }
    }, dismissButton = { OutlinedButton(onClick = onCancel) { Text("Cancelar") } })
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
