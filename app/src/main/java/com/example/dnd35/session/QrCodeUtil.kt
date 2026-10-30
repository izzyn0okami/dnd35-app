package com.example.dnd35.session

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

/** Geração do QR Code que o Mestre mostra para os Jogadores escanearem ao entrar na sessão. */
object QrCodeUtil {
    fun generate(content: String, sizePx: Int = 720): Bitmap {
        return BarcodeEncoder().encodeBitmap(content, BarcodeFormat.QR_CODE, sizePx, sizePx)
    }
}
