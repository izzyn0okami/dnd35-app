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
 * Cobre o respaldo por extensão de [Dnd35FileTypes.classify] — o caminho usado quando o provedor
 * de documentos (ou um `file://` direto) não devolve o MIME próprio que a exportação gravou.
 * O caminho pelo MIME em si depende de um ContentProvider real por trás do Uri, fora do escopo
 * razoável de um teste de unidade.
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
    fun `arquivo com extensao desconhecida nao e reconhecido`() {
        val uri = Uri.parse("file:///storage/emulated/0/Download/foto-ferias.json")
        assertNull(Dnd35FileTypes.classify(context, uri))
    }
}
