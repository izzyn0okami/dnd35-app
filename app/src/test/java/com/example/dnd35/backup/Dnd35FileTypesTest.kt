package com.example.dnd35.backup

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Cobre a classificação por MIME e extensão, inclusive o caso content:// em que o caminho da
 * URI contém apenas um ID interno e o nome real vem de OpenableColumns.DISPLAY_NAME.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Dnd35FileTypesTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `reconhece ficha pela extensao quando nao ha MIME de provedor`() {
        val uri = Uri.parse("file:///storage/emulated/0/Download/ficha-legolas-2026-11-02.fichadnd35")
        assertEquals(Dnd35FileKind.CHARACTER, Dnd35FileTypes.classify(context, uri))
    }

    @Test
    fun `reconhece item pela extensao quando nao ha MIME de provedor`() {
        val uri = Uri.parse("file:///storage/emulated/0/Download/item-espada-2026-11-02.itemdnd35")
        assertEquals(Dnd35FileKind.ITEM, Dnd35FileTypes.classify(context, uri))
    }

    @Test
    fun `extensao e reconhecida sem diferenciar maiusculas`() {
        val uri = Uri.parse("file:///storage/emulated/0/Download/ficha-legolas.FICHADND35")
        assertEquals(Dnd35FileKind.CHARACTER, Dnd35FileTypes.classify(context, uri))
    }

    @Test
    fun `reconhece ficha content uri pelo nome de exibicao do provedor`() {
        val result = Dnd35FileTypes.classifyFromMetadata(
            mimeType = "application/octet-stream",
            displayName = "ficha-legolas.fichadnd35",
            uriString = "content://downloads/document/4821"
        )
        assertEquals(Dnd35FileKind.CHARACTER, result)
    }

    @Test
    fun `reconhece item content uri pelo nome de exibicao do provedor`() {
        val result = Dnd35FileTypes.classifyFromMetadata(
            mimeType = "application/json",
            displayName = "item-espada.itemdnd35",
            uriString = "content://downloads/document/4822"
        )
        assertEquals(Dnd35FileKind.ITEM, result)
    }

    @Test
    fun `MIME personalizado identifica o arquivo mesmo sem extensao visivel`() {
        val character = Dnd35FileTypes.classifyFromMetadata(
            mimeType = Dnd35FileTypes.CHARACTER_MIME,
            displayName = "backup sem extensao",
            uriString = "content://provider/document/1"
        )
        val item = Dnd35FileTypes.classifyFromMetadata(
            mimeType = Dnd35FileTypes.ITEM_MIME,
            displayName = "export",
            uriString = "content://provider/document/2"
        )
        assertEquals(Dnd35FileKind.CHARACTER, character)
        assertEquals(Dnd35FileKind.ITEM, item)
    }

    @Test
    fun `arquivo com extensao desconhecida nao e reconhecido`() {
        val result = Dnd35FileTypes.classifyFromMetadata(
            mimeType = "application/json",
            displayName = "foto-ferias.json",
            uriString = "content://downloads/document/4823"
        )
        assertNull(result)
    }
}
