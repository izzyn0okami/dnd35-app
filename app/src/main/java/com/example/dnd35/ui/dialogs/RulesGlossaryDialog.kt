package com.example.dnd35.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class RulesGlossaryType {
    BESTIARY,
    SPELLBOOK
}

private data class GlossaryEntry(val term: String, val explanation: String)

@Composable
fun RulesGlossaryDialog(type: RulesGlossaryType, onDismiss: () -> Unit) {
    val title: String
    val introduction: String
    val entries: List<GlossaryEntry>

    when (type) {
        RulesGlossaryType.BESTIARY -> {
            title = "Ajuda do Bestiário"
            introduction = "Consulte as abreviações e os termos usados nas fichas de criaturas de D&D 3.5."
            entries = listOf(
                GlossaryEntry("Ext — Extraordinária", "Habilidade não mágica, normalmente ligada à anatomia, ao treinamento ou às capacidades naturais da criatura. Não é afetada por efeitos que impedem magia."),
                GlossaryEntry("Sob — Sobrenatural", "Habilidade mágica que não funciona como uma magia comum: em geral não exige componentes, não provoca ataques de oportunidade e não pode ser dissipada. Ainda pode ser suprimida em um campo antimagia."),
                GlossaryEntry("SM — Similar à Magia", "Habilidade que reproduz o efeito de uma magia. Não exige os componentes normais da magia, mas costuma ter usos limitados e pode ser afetada por resistência à magia e dissipar magia."),
                GlossaryEntry("ND — Nível de Desafio", "Estimativa da dificuldade que a criatura representa para um grupo de personagens. É uma referência para o Mestre, não o nível de personagem da criatura."),
                GlossaryEntry("DV — Dados de Vida", "Quantidade e tipo de dados usados para determinar os pontos de vida da criatura. Exemplo: 4d8."),
                GlossaryEntry("PV — Pontos de Vida", "Quantidade de dano que a criatura pode sofrer antes de morrer ou ser destruída, conforme suas regras."),
                GlossaryEntry("CA — Classe de Armadura", "Número que um atacante precisa igualar ou superar para acertar a criatura com um ataque. A ficha também informa modalidades específicas, como CA de toque e CA de surpresa."),
                GlossaryEntry("CA de toque", "Classe de Armadura contra ataques de toque. Não considera armadura, escudo ou armadura natural, mas outros modificadores aplicáveis continuam valendo."),
                GlossaryEntry("CA de surpresa", "Classe de Armadura quando a criatura está desprevenida. Nessa situação, normalmente não aplica seu bônus de Destreza à CA."),
                GlossaryEntry("BBA — Bônus Base de Ataque", "Parcela do bônus de ataque determinada pelos Dados de Vida e pela progressão da criatura."),
                GlossaryEntry("Agarrar", "Modificador usado nas jogadas de ataque de agarrar, incluindo o BBA, o modificador de Força e o modificador de tamanho."),
                GlossaryEntry("RD — Redução de Dano", "Reduz o dano de cada ataque sofrido. A notação indica quanto dano é ignorado e que tipo de arma ou material pode superar a redução; por exemplo, 5/mágica."),
                GlossaryEntry("RM — Resistência à Magia", "Valor que um conjurador precisa superar em um teste de nível de conjurador para afetar a criatura com uma magia ou habilidade aplicável."),
                GlossaryEntry("Fort, Ref e Von", "Abreviações de Fortitude, Reflexos e Vontade: os três tipos de testes de resistência."),
                GlossaryEntry("Espaço e alcance", "Espaço representa a área ocupada pela criatura em combate; alcance indica até onde ela pode realizar ataques corpo a corpo.")
            )
        }
        RulesGlossaryType.SPELLBOOK -> {
            title = "Ajuda do Grimório"
            introduction = "Os componentes indicam o que é necessário para conjurar uma magia. Uma magia pode exigir um ou vários componentes."
            entries = listOf(
                GlossaryEntry("V — Verbal", "A conjuração exige palavras pronunciadas em voz firme. Em geral, não é possível usar o componente verbal quando não se pode falar ou produzir os sons necessários."),
                GlossaryEntry("G — Gestual", "A conjuração exige gestos precisos com pelo menos uma mão livre. Uma mão ocupada ou presa pode impedir esse componente."),
                GlossaryEntry("M — Material", "A conjuração exige uma substância ou objeto físico indicado pela magia. O componente é consumido durante a conjuração; componentes com custo indicado precisam ter o valor especificado."),
                GlossaryEntry("F — Foco", "A magia exige um objeto específico como foco. Diferentemente de um componente material, o foco não é consumido pela conjuração, salvo indicação expressa da magia."),
                GlossaryEntry("FD — Foco Divino", "É um foco ligado à fé do conjurador, normalmente um símbolo sagrado ou profano. Não é consumido pela conjuração, salvo indicação expressa."),
                GlossaryEntry("XP — Custo em experiência", "A conjuração exige que o personagem gaste a quantidade de pontos de experiência indicada. Esse custo é pago além de quaisquer outros componentes.")
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(introduction, style = MaterialTheme.typography.bodyMedium)
                entries.forEach { entry ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(entry.term, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(entry.explanation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Entendi") } }
    )
}
