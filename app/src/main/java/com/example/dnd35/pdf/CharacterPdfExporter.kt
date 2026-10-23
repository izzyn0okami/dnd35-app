package com.example.dnd35.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.dnd35.model.CalculatedCharacterStats
import com.example.dnd35.model.CharacterCalculator
import com.example.dnd35.model.CharacterEntity
import com.example.dnd35.model.Dnd35Rules
import java.io.File
import java.util.Locale
import java.io.FileOutputStream

object CharacterPdfExporter {

    fun exportAndSharePdf(context: Context, character: CharacterEntity) {
        try {
            val stats = CharacterCalculator.compute(character)
            val pdfDocument = PdfDocument()

            val pageWidth = 595 // A4 standard width (pt)
            val pageHeight = 842 // A4 standard height (pt)

            // Paint styles
            val titlePaint = Paint().apply {
                color = Color.rgb(40, 20, 10)
                textSize = 18f
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.rgb(100, 60, 20)
                textSize = 9f
                typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
                isAntiAlias = true
            }

            val headerPaint = Paint().apply {
                color = Color.rgb(120, 25, 20)
                textSize = 11f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }

            val bodyPaint = Paint().apply {
                color = Color.rgb(30, 30, 30)
                textSize = 8.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                isAntiAlias = true
            }

            val boldBodyPaint = Paint().apply {
                color = Color.rgb(20, 20, 20)
                textSize = 8.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }

            val borderPaint = Paint().apply {
                color = Color.rgb(180, 140, 90)
                style = Paint.Style.STROKE
                strokeWidth = 1f
                isAntiAlias = true
            }

            val fillBoxPaint = Paint().apply {
                color = Color.rgb(250, 246, 238) // Parchment paper tone
                style = Paint.Style.FILL
            }

            val goldBannerPaint = Paint().apply {
                color = Color.rgb(238, 225, 200)
                style = Paint.Style.FILL
            }

            // ==================== PÁGINA 1: ATRIBUTOS, COMBATE & CARGA ====================
            val pageInfo1 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page1 = pdfDocument.startPage(pageInfo1)
            val canvas1: Canvas = page1.canvas

            // Fundo de pergaminho sutil
            canvas1.drawColor(Color.rgb(253, 251, 247))

            // Moldura externa decorativa
            val outerMargin = 20f
            canvas1.drawRect(outerMargin, outerMargin, pageWidth - outerMargin, pageHeight - outerMargin, borderPaint)
            canvas1.drawRect(outerMargin + 3, outerMargin + 3, pageWidth - outerMargin - 3, pageHeight - outerMargin - 3, Paint().apply {
                color = Color.rgb(220, 195, 160)
                style = Paint.Style.STROKE
                strokeWidth = 0.5f
            })

            // Título Principal
            canvas1.drawText("FICHA DE PERSONAGEM — D&D 3.5", 30f, 44f, titlePaint)
            canvas1.drawText("Sistema d20 • Edição 3.5 das Regras Oficiais", 30f, 56f, subtitlePaint)

            // Carregar Foto do Personagem se houver
            val photoBitmap = loadCharacterBitmap(context, character.photoUri)
            val photoRect = RectF(pageWidth - 95f, 30f, pageWidth - 30f, 95f)
            if (photoBitmap != null) {
                canvas1.drawBitmap(photoBitmap, null, photoRect, Paint(Paint.FILTER_BITMAP_FLAG))
                canvas1.drawRect(photoRect, borderPaint)
            } else {
                canvas1.drawRect(photoRect, fillBoxPaint)
                canvas1.drawRect(photoRect, borderPaint)
                val photoTextPaint = Paint().apply {
                    color = Color.GRAY
                    textSize = 7f
                    textAlign = Paint.Align.CENTER
                }
                canvas1.drawText("Sem Foto", photoRect.centerX(), photoRect.centerY() + 2f, photoTextPaint)
            }

            // Bloco de Identificação
            val infoTop = 68f
            canvas1.drawRoundRect(RectF(30f, infoTop, pageWidth - 105f, infoTop + 48f), 4f, 4f, fillBoxPaint)
            canvas1.drawRoundRect(RectF(30f, infoTop, pageWidth - 105f, infoTop + 48f), 4f, 4f, borderPaint)

            canvas1.drawText("Nome: ${character.name}", 36f, infoTop + 14f, boldBodyPaint)
            val penaltyStr = if (stats.multiclassPenalty.hasPenalty) " [-${stats.multiclassPenalty.penaltyPercent}% XP]" else ""
            canvas1.drawText("Classe: ${stats.classesDisplay} (Nv ${stats.totalCharacterLevel})$penaltyStr", 36f, infoTop + 28f, bodyPaint)
            canvas1.drawText("Raça: ${character.race}  |  Tamanho: ${character.size}", 36f, infoTop + 42f, bodyPaint)

            val col2X = 220f
            canvas1.drawText("Tendência: ${character.alignment}", col2X, infoTop + 14f, bodyPaint)
            canvas1.drawText("Divindade: ${character.deity}", col2X, infoTop + 28f, bodyPaint)
            canvas1.drawText("Deslocamento: ${stats.speedMetersEffective} m (${(stats.speedMetersEffective / 1.5f).toInt()} quadrados)", col2X, infoTop + 42f, bodyPaint)

            // Bloco de Atributos (Coluna Esquerda)
            val attrLeft = 30f
            val attrWidth = 110f
            var curY = 130f

            canvas1.drawRoundRect(RectF(attrLeft, curY, attrLeft + attrWidth, curY + 220f), 4f, 4f, fillBoxPaint)
            canvas1.drawRoundRect(RectF(attrLeft, curY, attrLeft + attrWidth, curY + 220f), 4f, 4f, borderPaint)

            canvas1.drawRect(RectF(attrLeft, curY, attrLeft + attrWidth, curY + 22f), goldBannerPaint)
            canvas1.drawText("HABILIDADES", attrLeft + 12f, curY + 15f, headerPaint)

            val abilities = listOf(
                Triple("FOR", stats.totalStr, stats.strMod),
                Triple("DES", stats.totalDex, stats.dexMod),
                Triple("CON", stats.totalCon, stats.conMod),
                Triple("INT", stats.totalInt, stats.intMod),
                Triple("SAB", stats.totalWis, stats.wisMod),
                Triple("CAR", stats.totalCha, stats.chaMod)
            )

            var abY = curY + 40f
            for ((name, score, mod) in abilities) {
                canvas1.drawText(name, attrLeft + 10f, abY, boldBodyPaint)
                canvas1.drawText("$score", attrLeft + 48f, abY, bodyPaint)
                val sign = if (mod >= 0) "+$mod" else "$mod"
                canvas1.drawText(sign, attrLeft + 76f, abY, boldBodyPaint)
                canvas1.drawLine(attrLeft + 5f, abY + 6f, attrLeft + attrWidth - 5f, abY + 6f, borderPaint)
                abY += 30f
            }

            // Bloco de Combate (Coluna Direita)
            val combatLeft = 150f
            val combatWidth = pageWidth - 30f - combatLeft

            // PV e Vida
            canvas1.drawRoundRect(RectF(combatLeft, curY, combatLeft + combatWidth, curY + 65f), 4f, 4f, fillBoxPaint)
            canvas1.drawRoundRect(RectF(combatLeft, curY, combatLeft + combatWidth, curY + 65f), 4f, 4f, borderPaint)
            canvas1.drawRect(RectF(combatLeft, curY, combatLeft + combatWidth, curY + 20f), goldBannerPaint)
            canvas1.drawText("PONTOS DE VIDA & COMBATE", combatLeft + 10f, curY + 14f, headerPaint)

            canvas1.drawText("PV Máx: ${character.maxHp}    |    PV Atual: ${character.currentHp}", combatLeft + 12f, curY + 36f, boldBodyPaint)
            canvas1.drawText("PV Temporários: ${character.tempHp}    |    Dano Não-Letal: ${character.nonLethalDamage}", combatLeft + 12f, curY + 52f, bodyPaint)

            // Classe de Armadura (CA)
            val acY = curY + 75f
            canvas1.drawRoundRect(RectF(combatLeft, acY, combatLeft + combatWidth, acY + 65f), 4f, 4f, fillBoxPaint)
            canvas1.drawRoundRect(RectF(combatLeft, acY, combatLeft + combatWidth, acY + 65f), 4f, 4f, borderPaint)
            canvas1.drawRect(RectF(combatLeft, acY, combatLeft + combatWidth, acY + 20f), goldBannerPaint)
            canvas1.drawText("CLASSE DE ARMADURA (CA)", combatLeft + 10f, acY + 14f, headerPaint)

            canvas1.drawText("CA Total: ${stats.totalAc}", combatLeft + 12f, acY + 36f, boldBodyPaint)
            canvas1.drawText("Toque: ${stats.touchAc}    |    Surpreso: ${stats.flatFootedAc}", combatLeft + 110f, acY + 36f, bodyPaint)
            canvas1.drawText("Armadura: +${stats.armorBonusTotal}  |  Escudo: +${stats.shieldBonusTotal}  |  Des Ef.: +${stats.effectiveDexForAc}  |  Pen. Armadura: ${stats.armorCheckPenalty}", combatLeft + 12f, acY + 52f, bodyPaint)

            // Testes de Resistência & BBA
            val saveY = acY + 75f
            canvas1.drawRoundRect(RectF(combatLeft, saveY, combatLeft + combatWidth, saveY + 70f), 4f, 4f, fillBoxPaint)
            canvas1.drawRoundRect(RectF(combatLeft, saveY, combatLeft + combatWidth, saveY + 70f), 4f, 4f, borderPaint)
            canvas1.drawRect(RectF(combatLeft, saveY, combatLeft + combatWidth, saveY + 20f), goldBannerPaint)
            canvas1.drawText("TESTES DE RESISTÊNCIA & ATAQUE", combatLeft + 10f, saveY + 14f, headerPaint)

            val fortStr = if (stats.totalFort >= 0) "+${stats.totalFort}" else "${stats.totalFort}"
            val refStr = if (stats.totalRef >= 0) "+${stats.totalRef}" else "${stats.totalRef}"
            val willStr = if (stats.totalWill >= 0) "+${stats.totalWill}" else "${stats.totalWill}"

            canvas1.drawText("Fortitude: $fortStr  (Base ${stats.baseFort} + Con ${stats.conMod})", combatLeft + 12f, saveY + 36f, boldBodyPaint)
            canvas1.drawText("Reflexos: $refStr  (Base ${stats.baseRef} + Des ${stats.dexMod})", combatLeft + 12f, saveY + 50f, boldBodyPaint)
            canvas1.drawText("Vontade: $willStr  (Base ${stats.baseWill} + Sab ${stats.wisMod})", combatLeft + 12f, saveY + 64f, boldBodyPaint)

            val initStr = if (stats.initiativeTotal >= 0) "+${stats.initiativeTotal}" else "${stats.initiativeTotal}"
            canvas1.drawText("Iniciativa: $initStr", combatLeft + 220f, saveY + 36f, boldBodyPaint)
            canvas1.drawText("BBA: ${stats.babDisplay}", combatLeft + 220f, saveY + 50f, boldBodyPaint)
            canvas1.drawText("Agarrar: +${stats.grappleBonus}", combatLeft + 220f, saveY + 64f, bodyPaint)

            // Bloco de Armas e Ataques Equipados
            val weaponY = 360f
            canvas1.drawRoundRect(RectF(30f, weaponY, pageWidth - 30f, weaponY + 120f), 4f, 4f, fillBoxPaint)
            canvas1.drawRoundRect(RectF(30f, weaponY, pageWidth - 30f, weaponY + 120f), 4f, 4f, borderPaint)
            canvas1.drawRect(RectF(30f, weaponY, pageWidth - 30f, weaponY + 20f), goldBannerPaint)
            val equippedWeapons = stats.inventory.filter { it.equipped && it.category.startsWith("Arma", ignoreCase = true) }
            canvas1.drawText("ATAQUES & ARMAS (${equippedWeapons.size} cadastradas; máx. 4 exibidas)", 40f, weaponY + 14f, headerPaint)
            var wRowY = weaponY + 36f
            if (equippedWeapons.isEmpty()) {
                canvas1.drawText("Ataque Desarmado  •  Bônus: +${stats.bab + stats.strMod}  •  Dano: 1d3+${stats.strMod} Concussão", 42f, wRowY, bodyPaint)
            } else {
                for (w in equippedWeapons.take(4)) {
                    val atkBonus = stats.bab + stats.strMod
                    val atkStr = if (atkBonus >= 0) "+$atkBonus" else "$atkBonus"
                    canvas1.drawText("${w.name}  •  Ataque: $atkStr  •  Dano: ${w.damageOrAc}  •  ${w.notes}", 42f, wRowY, boldBodyPaint)
                    wRowY += 20f
                }
            }

            // Bloco de Capacidade de Carga e Gestão de Peso (Tabela 9-1 Oficial)
            val loadY = 490f
            canvas1.drawRoundRect(RectF(30f, loadY, pageWidth - 30f, loadY + 130f), 4f, 4f, fillBoxPaint)
            canvas1.drawRoundRect(RectF(30f, loadY, pageWidth - 30f, loadY + 130f), 4f, 4f, borderPaint)
            canvas1.drawRect(RectF(30f, loadY, pageWidth - 30f, loadY + 20f), goldBannerPaint)
            canvas1.drawText("GESTÃO DE CARGA & PESO (LIVRO DO JOGADOR 3.5)", 40f, loadY + 14f, headerPaint)

            canvas1.drawText("Peso Carregado Total: ${String.format("%.1f", stats.totalWeightLb)} lb   •   Status Atual: ${stats.encumbrance.label}", 42f, loadY + 38f, boldBodyPaint)
            canvas1.drawText("Carga Leve: até ${stats.carryingCapacity.lightMaxLb} lb   •   Carga Média: ${stats.carryingCapacity.lightMaxLb + 1}–${stats.carryingCapacity.mediumMaxLb} lb   •   Carga Pesada: ${stats.carryingCapacity.mediumMaxLb + 1}–${stats.carryingCapacity.heavyMaxLb} lb", 42f, loadY + 54f, bodyPaint)
            canvas1.drawText("Erguer sobre a cabeça: ${stats.carryingCapacity.heavyMaxLb} lb   •   Erguer do chão: ${stats.carryingCapacity.heavyMaxLb * 2} lb   •   Arrastar: ${stats.carryingCapacity.heavyMaxLb * 5} lb", 42f, loadY + 68f, bodyPaint)

            // Efeitos da carga
            val maxDexNote = stats.encumbrance.maxDexBonus?.let { "+$it" } ?: "Sem limite"
            canvas1.drawText("Efeitos da Carga: Des Máxima: $maxDexNote  |  Penalidade na CA: ${stats.encumbrance.armorCheckPenalty}  |  Fator Velocidade: ${(stats.encumbrance.speedFactor * 100).toInt()}%", 42f, loadY + 84f, bodyPaint)

            // Tesouro / Moedas
            canvas1.drawLine(35f, loadY + 94f, pageWidth - 35f, loadY + 94f, borderPaint)
            canvas1.drawText("Riqueza & Moedas:  PO: ${character.goldGp.toInt()}   |   PP: ${character.silverSp.toInt()}   |   PC: ${character.copperCp.toInt()}   |   PL: ${character.platinumPp.toInt()}", 42f, loadY + 114f, boldBodyPaint)

            // Talentos e Habilidades Notáveis (Prévia Pág 1)
            val featsY = 630f
            canvas1.drawRoundRect(RectF(30f, featsY, pageWidth - 30f, pageHeight - 35f), 4f, 4f, fillBoxPaint)
            canvas1.drawRoundRect(RectF(30f, featsY, pageWidth - 30f, pageHeight - 35f), 4f, 4f, borderPaint)
            canvas1.drawRect(RectF(30f, featsY, pageWidth - 30f, featsY + 20f), goldBannerPaint)
            canvas1.drawText("TALENTOS & HABILIDADES (${stats.feats.size}; máx. 6 exibidos)", 40f, featsY + 14f, headerPaint)

            var featRowY = featsY + 36f
            for (f in stats.feats.take(6)) {
                canvas1.drawText("• ${f.name}: ${f.summary.ifBlank { f.description.take(60) }}", 42f, featRowY, boldBodyPaint)
                featRowY += 18f
            }
            if (stats.feats.isEmpty()) {
                canvas1.drawText("Nenhum talento cadastrado.", 42f, featRowY, bodyPaint)
            }

            pdfDocument.finishPage(page1)

            // ==================== PÁGINA 2: PERÍCIAS, MAGIAS & INVENTÁRIO ====================
            val pageInfo2 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
            val page2 = pdfDocument.startPage(pageInfo2)
            val canvas2: Canvas = page2.canvas

            canvas2.drawColor(Color.rgb(253, 251, 247))
            canvas2.drawRect(outerMargin, outerMargin, pageWidth - outerMargin, pageHeight - outerMargin, borderPaint)

            canvas2.drawText("PERÍCIAS & GRIMÓRIO — ${character.name.uppercase()}", 30f, 44f, titlePaint)
            canvas2.drawText("Página 2 • Perícias com Graduações, Grimório de Magias e Inventário", 30f, 56f, subtitlePaint)

            // Bloco de Perícias em 2 Colunas
            val skillsTop = 70f
            val skillBoxWidth = 260f

            // Coluna 1
            canvas2.drawRoundRect(RectF(30f, skillsTop, 30f + skillBoxWidth, 540f), 4f, 4f, fillBoxPaint)
            canvas2.drawRoundRect(RectF(30f, skillsTop, 30f + skillBoxWidth, 540f), 4f, 4f, borderPaint)
            canvas2.drawRect(RectF(30f, skillsTop, 30f + skillBoxWidth, skillsTop + 20f), goldBannerPaint)
            canvas2.drawText("PERÍCIAS (A - M)", 40f, skillsTop + 14f, headerPaint)

            // Coluna 2
            val col2Left = 300f
            canvas2.drawRoundRect(RectF(col2Left, skillsTop, col2Left + skillBoxWidth, 540f), 4f, 4f, fillBoxPaint)
            canvas2.drawRoundRect(RectF(col2Left, skillsTop, col2Left + skillBoxWidth, 540f), 4f, 4f, borderPaint)
            canvas2.drawRect(RectF(col2Left, skillsTop, col2Left + skillBoxWidth, skillsTop + 20f), goldBannerPaint)
            canvas2.drawText("PERÍCIAS (N - U)", col2Left + 10f, skillsTop + 14f, headerPaint)

            val half = stats.computedSkills.size / 2
            val skillsCol1 = stats.computedSkills.take(half)
            val skillsCol2 = stats.computedSkills.drop(half)

            var sk1Y = skillsTop + 34f
            for (sk in skillsCol1) {
                val totalStr = if (sk.totalBonus >= 0) "+${sk.totalBonus}" else "${sk.totalBonus}"
                val classMarker = if (sk.isClassSkill) "★" else " "
                canvas2.drawText("$classMarker ${sk.skill.name.take(18)}", 36f, sk1Y, if (sk.ranks > 0) boldBodyPaint else bodyPaint)
                canvas2.drawText("${sk.skill.keyAbility}  G:${formatRanks(sk.ranks)}  Tot: $totalStr", 195f, sk1Y, boldBodyPaint)
                sk1Y += 23f
            }

            var sk2Y = skillsTop + 34f
            for (sk in skillsCol2) {
                val totalStr = if (sk.totalBonus >= 0) "+${sk.totalBonus}" else "${sk.totalBonus}"
                val classMarker = if (sk.isClassSkill) "★" else " "
                canvas2.drawText("$classMarker ${sk.skill.name.take(18)}", col2Left + 6f, sk2Y, if (sk.ranks > 0) boldBodyPaint else bodyPaint)
                canvas2.drawText("${sk.skill.keyAbility}  G:${sk.ranks.toInt()}  Tot: $totalStr", col2Left + 165f, sk2Y, boldBodyPaint)
                sk2Y += 23f
            }

            // Bloco de Magias & Espaços (Inferior Esquerdo)
            val spellBoxTop = 550f
            val spellBoxWidth = 260f
            canvas2.drawRoundRect(RectF(30f, spellBoxTop, 30f + spellBoxWidth, pageHeight - 35f), 4f, 4f, fillBoxPaint)
            canvas2.drawRoundRect(RectF(30f, spellBoxTop, 30f + spellBoxWidth, pageHeight - 35f), 4f, 4f, borderPaint)
            canvas2.drawRect(RectF(30f, spellBoxTop, 30f + spellBoxWidth, spellBoxTop + 20f), goldBannerPaint)
            canvas2.drawText("MAGIAS (${stats.spells.size}; máx. 9 exibidas)", 40f, spellBoxTop + 14f, headerPaint)

            var spY = spellBoxTop + 36f
            val activeSlots = stats.spellSlots.filter { it.totalSlots > 0 }
            if (activeSlots.isNotEmpty()) {
                val slotSummary = activeSlots.joinToString("  |  ") { "Nv ${it.level}: ${it.usedSlots}/${it.totalSlots}" }
                canvas2.drawText("Espaços: $slotSummary", 36f, spY, boldBodyPaint)
                spY += 18f
            }

            for (sp in stats.spells.take(9)) {
                canvas2.drawText("• [Nv ${sp.level}] ${sp.name} (${sp.school})", 36f, spY, boldBodyPaint)
                canvas2.drawText("  Alcance: ${sp.range}  •  ${sp.description.take(40)}", 36f, spY + 11f, subtitlePaint)
                spY += 22f
            }
            if (stats.spells.isEmpty()) {
                canvas2.drawText("Nenhuma magia registrada no grimório.", 36f, spY, bodyPaint)
            }

            // Bloco de Inventário Detalhado (Inferior Direito)
            canvas2.drawRoundRect(RectF(col2Left, spellBoxTop, col2Left + skillBoxWidth, pageHeight - 35f), 4f, 4f, fillBoxPaint)
            canvas2.drawRoundRect(RectF(col2Left, spellBoxTop, col2Left + skillBoxWidth, pageHeight - 35f), 4f, 4f, borderPaint)
            canvas2.drawRect(RectF(col2Left, spellBoxTop, col2Left + skillBoxWidth, spellBoxTop + 20f), goldBannerPaint)
            canvas2.drawText("INVENTÁRIO (${stats.inventory.size}; máx. 10 exibidos)", col2Left + 10f, spellBoxTop + 14f, headerPaint)

            var invY = spellBoxTop + 36f
            for (item in stats.inventory.take(10)) {
                val eq = if (item.equipped || item.equippedSlot != Dnd35Rules.EquipmentSlots.NONE) "[${item.equippedSlot.take(7)}] " else ""
                val itemStr = "$eq${item.name} (x${item.quantity}) - ${item.weightLb * item.quantity} lb"
                canvas2.drawText(itemStr, col2Left + 10f, invY, boldBodyPaint)
                if (item.damageOrAc.isNotBlank() || item.notes.isNotBlank()) {
                    canvas2.drawText("${item.damageOrAc} ${item.notes}".take(40), col2Left + 15f, invY + 11f, subtitlePaint)
                }
                invY += 22f
            }
            if (stats.inventory.isEmpty()) {
                canvas2.drawText("Mochila vazia.", col2Left + 10f, invY, bodyPaint)
            }

            pdfDocument.finishPage(page2)

            // Salvar no arquivo temporário e compartilhar
            val safeName = character.name.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val pdfFile = File(context.cacheDir, "Ficha_${safeName}_DND35.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Ficha de D&D 3.5 - ${character.name}")
                putExtra(Intent.EXTRA_TEXT, "Ficha gerada para ${character.name} (${character.characterClass} Nível ${character.level} - D&D 3.5)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Compartilhar Ficha PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Toast.makeText(context, "PDF gerado com sucesso!", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erro ao gerar PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /** Exibe graduações fracionárias no PDF sem alterar o bônus dos testes. */
    private fun formatRanks(ranks: Float): String {
        return if (ranks % 1f == 0f) {
            ranks.toInt().toString()
        } else {
            String.format(Locale.ROOT, "%.1f", ranks).replace('.', ',')
        }
    }

    private fun loadCharacterBitmap(context: Context, photoUriStr: String?): Bitmap? {
        if (photoUriStr.isNullOrBlank()) return null
        return try {
            if (photoUriStr.startsWith("preset:")) {
                val resName = photoUriStr.removePrefix("preset:")
                val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                if (resId != 0) {
                    BitmapFactory.decodeResource(context.resources, resId)
                } else null
            } else {
                val uri = Uri.parse(photoUriStr)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }
        } catch (_: Exception) {
            null
        }
    }
}
