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
                GlossaryEntry("Ext — Extraordinária", "Habilidade não mágica, embora possa desafiar as leis da física. Não é afetada por dissipar magia, resistência à magia ou campo antimagia. Em geral, não pode ser interrompida como uma magia e não provoca ataques de oportunidade."),
                GlossaryEntry("Sob — Sobrenatural", "Habilidade mágica, mas não funciona como uma magia comum. Não é afetada por resistência à magia, não pode ser dissipada nem usada para contra-atacar magias. É suprimida em campo antimagia. Normalmente exige ação padrão, não provoca ataques de oportunidade e não exige Concentração."),
                GlossaryEntry("SM — Similar à Magia", "Reproduz o efeito de uma magia. Não exige componentes verbais, gestuais ou materiais, foco nem custo de XP. É afetada por resistência à magia e pode ser dissipada; usá-la sob ameaça normalmente provoca ataque de oportunidade. Não funciona em campo antimagia."),
                GlossaryEntry("Habilidade natural", "Capacidade decorrente da natureza física da criatura que não foi identificada como Ext, Sob ou SM. Não se deve presumir que seja mágica apenas por ser incomum."),
                GlossaryEntry("Ataques especiais e qualidades especiais", "Ataques especiais são capacidades usadas para atacar ou prejudicar oponentes. Qualidades especiais abrangem defesas, sentidos, formas de movimento, resistências, vulnerabilidades e outras capacidades que não são propriamente ataques."),
                GlossaryEntry("Usos e frequência", "Indicações como 1/dia, 3/dia ou à vontade informam quantas vezes a habilidade pode ser usada. À vontade significa que não há limite diário, mas o tempo de ativação e outras condições continuam valendo."),
                GlossaryEntry("CD — Classe de Dificuldade", "Número que o alvo precisa alcançar no teste de resistência para evitar ou reduzir um efeito. Use a CD indicada na ficha. Como regra geral, a CD de habilidades de monstros costuma ser 10 + metade dos Dados de Vida raciais + o modificador do atributo relevante."),
                GlossaryEntry("Nível de conjurador", "Determina variáveis de uma habilidade similar à magia que reproduz uma magia, como alcance, duração e dificuldade para dissipar o efeito. Se não for indicado, normalmente é igual aos Dados de Vida da criatura."),
                GlossaryEntry("Dano e dreno de atributo", "Dano de atributo reduz temporariamente um valor; dreno reduz o valor permanentemente até ser restaurado por meios apropriados. Constituição 0 normalmente significa morte; os demais atributos em 0 produzem incapacidades específicas."),
                GlossaryEntry("Dreno de energia", "Ataque que concede níveis negativos. Cada nível negativo impõe penalidades e pode se tornar perda permanente de nível se a vítima falhar no teste posterior indicado pela regra. A ficha informa quantidade, CD e frequência."),
                GlossaryEntry("Veneno", "A ficha informa forma de inoculação, CD de Fortitude e dano inicial e secundário. O dano secundário ocorre depois do intervalo indicado. Criaturas imunes a veneno não são afetadas."),
                GlossaryEntry("Doença", "Pode ser transmitida por ataque, contato, inalação ou outra forma indicada. A descrição informa incubação, CD e dano inicial e secundário."),
                GlossaryEntry("Paralisia", "Impede a vítima de agir e a deixa indefesa, conforme as regras da condição. A ficha informa teste de resistência, duração e eventuais imunidades."),
                GlossaryEntry("Olhar", "Ataque visual que afeta criaturas que olham para o usuário, conforme alcance, direção e condições descritas. Desviar os olhos ou fechá-los pode evitar alguns ataques, mas traz as penalidades correspondentes."),
                GlossaryEntry("Sopro", "Ataque especial que normalmente afeta uma área, como cone ou linha. A ficha informa formato, alcance, dano, tipo de energia, frequência e teste de resistência."),
                GlossaryEntry("Agarrar aprimorado", "Quando a criatura acerta o ataque indicado, pode iniciar um agarrão sem provocar ataque de oportunidade e sem realizar o ataque de toque inicial. A ficha informa quais ataques permitem iniciar o agarrão."),
                GlossaryEntry("Agarrar", "Manobra para imobilizar ou restringir um oponente. Envolve testes resistidos; tamanho, Força e modificadores específicos influenciam o resultado. Estar agarrado limita as ações da vítima."),
                GlossaryEntry("Constrição", "Depois de agarrar e obter sucesso no teste indicado, a criatura esmaga o alvo e causa o dano adicional informado. Algumas criaturas causam esse dano automaticamente a cada rodada em que mantêm o agarrão."),
                GlossaryEntry("Bote", "Permite à criatura realizar um ataque completo ao fim de uma investida, em vez de ficar limitada ao ataque normalmente permitido por uma investida."),
                GlossaryEntry("Rasgar e dilacerar", "Ataques adicionais que causam dano quando a criatura acerta o alvo com os ataques exigidos pela habilidade. A ficha informa a quantidade de ataques necessários e o dano."),
                GlossaryEntry("Atropelar", "A criatura pode passar por cima de oponentes durante seu deslocamento. Os alvos podem realizar ataque de oportunidade com penalidade ou tentar Reflexos para reduzir o dano à metade."),
                GlossaryEntry("Investida poderosa", "Ataque especial realizado ao investir, normalmente causando dano adicional quando a criatura atinge o alvo. O dano e eventuais condições aparecem na descrição da criatura."),
                GlossaryEntry("Engolfar", "A criatura envolve o oponente com o próprio corpo. O alvo pode ficar preso, sofrer dano ou ser submetido a outros efeitos descritos na habilidade."),
                GlossaryEntry("Engolir inteiro", "A criatura pode engolir o alvo após cumprir as condições da habilidade. A vítima sofre os efeitos descritos para o interior do corpo, normalmente dano contínuo, e pode tentar escapar."),
                GlossaryEntry("Enredar e teia", "Uma teia ou substância semelhante pode prender e restringir o movimento. A ficha informa alcance, área, resistência para evitar, CD para escapar e/ou força necessária para romper."),
                GlossaryEntry("Drenar sangue", "A criatura suga sangue de uma vítima agarrada ou indefesa, causando dano ou perda indicada. Algumas habilidades também restauram pontos de vida."),
                GlossaryEntry("Medo e desespero", "Efeitos de medo podem deixar a vítima abalada, assustada ou em pânico. Desespero e habilidades semelhantes podem impor condições diferentes, inclusive paralisia. Consulte CD e duração na ficha."),
                GlossaryEntry("RD — Redução de Dano", "Subtrai uma quantidade fixa do dano de cada ataque físico que não supere a condição indicada após a barra. Exemplo: RD 5/mágica ignora 5 pontos de dano de armas não mágicas. Não reduz dano de energia."),
                GlossaryEntry("Resistência à energia", "Reduz o dano recebido de um tipo de energia pelo valor indicado, por aplicação de dano. Não concede imunidade: qualquer dano acima da resistência ainda é sofrido."),
                GlossaryEntry("Imunidade e vulnerabilidade à energia", "Imunidade impede todo dano do tipo indicado. Vulnerabilidade normalmente aumenta em 50% o dano daquele tipo. A descrição específica pode estabelecer exceções."),
                GlossaryEntry("Cura acelerada", "A criatura recupera automaticamente a quantidade indicada de pontos de vida a cada rodada. Não restaura perdas que não sejam dano, como certas formas de perda de atributo."),
                GlossaryEntry("Regeneração", "O dano sofrido é convertido em dano não letal e a criatura recupera esse dano à taxa indicada. Certos tipos de ataque, especificados na ficha, causam dano letal normalmente. Pode permitir recuperar ou recolocar partes do corpo."),
                GlossaryEntry("Evasão", "Quando obtém sucesso em Reflexos contra um efeito que normalmente causa metade do dano, a criatura não sofre dano algum. Só funciona quando pode aplicar seu bônus de Destreza à CA."),
                GlossaryEntry("Evasão aprimorada", "Funciona como evasão, mas a criatura ainda sofre apenas metade do dano quando falha no teste de Reflexos. Se obtiver sucesso, não sofre dano."),
                GlossaryEntry("RM — Resistência à Magia", "Valor que o conjurador precisa igualar ou superar em um teste de nível de conjurador (1d20 + nível de conjurador). Aplica-se a magias e habilidades similares à magia sujeitas à RM, não a habilidades Ext ou Sob."),
                GlossaryEntry("Resistência à expulsão", "Bônus que aumenta os Dados de Vida efetivos da criatura para resistir a tentativas de expulsar, fascinar, comandar ou fortalecer mortos-vivos, conforme a habilidade."),
                GlossaryEntry("Visão no escuro", "Permite enxergar sem luz dentro do alcance indicado, normalmente em preto e branco. Não permite ver através de escuridão mágica que bloqueie esse sentido, salvo regra específica."),
                GlossaryEntry("Visão na penumbra", "Permite enxergar melhor sob iluminação fraca, como luar ou luz das estrelas. Não concede visão na escuridão total."),
                GlossaryEntry("Sentido cego", "Permite perceber criaturas e objetos sem depender da visão dentro do alcance indicado, usando sentidos não visuais. Normalmente não identifica cores nem detalhes visuais."),
                GlossaryEntry("Percepção às cegas", "Permite localizar criaturas sem visão dentro do alcance indicado, usando sentidos como audição, olfato ou vibrações. Condições que bloqueiem o sentido podem impedir a percepção."),
                GlossaryEntry("Faro", "Permite detectar criaturas e rastros pelo olfato. O alcance varia com vento e intensidade do odor. Aproximar-se a até 1,5 m pode permitir localizar a fonte; rastrear pelo cheiro segue as regras da habilidade."),
                GlossaryEntry("Sentido sísmico", "Permite detectar e localizar criaturas em contato com o mesmo solo ou superfície dentro do alcance indicado. A criatura que usa esse sentido também precisa estar em contato com a superfície."),
                GlossaryEntry("Incorpóreo", "A criatura não possui corpo físico. Em geral, só pode ser ferida por outras criaturas incorpóreas, armas mágicas ou efeitos específicos; atravessa objetos sólidos, mas não efeitos que bloqueiem incorpóreos."),
                GlossaryEntry("Forma alternativa", "Permite assumir uma ou mais formas específicas. A descrição determina quais características físicas, ataques, deslocamentos e capacidades são alterados ou mantidos."),
                GlossaryEntry("Invisibilidade e camuflagem", "Invisibilidade impede a visão normal, mas não torna a criatura automaticamente indetectável: som, cheiro, rastros e sentidos especiais ainda podem revelar sua presença. Camuflagem dificulta localizar ou atingir a criatura."),
                GlossaryEntry("Telepatia", "Permite comunicar-se mentalmente com outras criaturas dentro do alcance e nas condições indicadas. Não significa, por si só, ler pensamentos ou controlar a mente."),
                GlossaryEntry("Psiquismo", "Termo usado em algumas fichas para capacidades mentais ou psíquicas. Efeitos, limites, CDs e interações são os descritos no verbete da criatura; não se deve presumir que todas sejam idênticas."),
                GlossaryEntry("Invocar criaturas", "Chama outras criaturas para auxiliar o usuário. A ficha informa quais podem ser invocadas, chance de sucesso, duração e frequência. Não é necessariamente equivalente a uma magia de invocação."),
                GlossaryEntry("Imunidade a efeitos", "A imunidade indicada impede que a criatura seja afetada por uma categoria específica, como veneno, sono, paralisia, doença, acertos críticos ou efeitos de ação mental. Não se estende automaticamente a categorias não mencionadas."),
                GlossaryEntry("Características de morto-vivo", "Mortos-vivos são imunes a diversos efeitos que afetam criaturas vivas, não possuem Constituição e usam regras próprias para Dados de Vida, cura, energia positiva/negativa e testes de resistência."),
                GlossaryEntry("ND — Nível de Desafio", "Estimativa da dificuldade que a criatura representa para um grupo de personagens. É uma referência para o Mestre, não o nível de personagem da criatura."),
                GlossaryEntry("DV — Dados de Vida", "Quantidade e tipo de dados usados para determinar os pontos de vida da criatura. Exemplo: 4d8."),
                GlossaryEntry("PV — Pontos de Vida", "Quantidade de dano que a criatura pode sofrer antes de morrer ou ser destruída, conforme suas regras."),
                GlossaryEntry("CA — Classe de Armadura", "Número que um atacante precisa igualar ou superar para acertar a criatura. A ficha também informa CA de toque e CA de surpresa."),
                GlossaryEntry("CA de toque", "Não considera armadura, escudo ou armadura natural, mas outros modificadores aplicáveis continuam valendo."),
                GlossaryEntry("CA de surpresa", "CA quando a criatura está desprevenida. Nessa situação, normalmente não aplica seu bônus de Destreza à CA."),
                GlossaryEntry("BBA — Bônus Base de Ataque", "Parcela do bônus de ataque determinada pelos Dados de Vida e pela progressão da criatura."),
                GlossaryEntry("Agarrar (modificador)", "Modificador usado nas jogadas de ataque de agarrar, incluindo BBA, modificador de Força e modificador de tamanho."),
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
