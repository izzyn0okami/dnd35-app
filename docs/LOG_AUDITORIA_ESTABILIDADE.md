# Auditoria de estabilidade — D&D 3.5 Android

## Escopo e premissas

Esta auditoria investiga o congelamento relatado ao retornar ao aplicativo enquanto uma ficha estava apenas sendo visualizada. A exceção `SocketException: Software caused connection abort` registrada em `SessionClient` pertence ao fluxo de multiplayer; como o usuário não estava usando multiplayer, ela não é tratada como causa do congelamento.

Não há evidência suficiente para afirmar que a causa do congelamento já foi identificada. A meta desta etapa é acrescentar observabilidade e remover um risco conhecido de bloqueio da interface.

## Diagnóstico inicial — 2026-11-07/08

- `CharacterViewModel` chamava `CharacterCalculator.compute()` diretamente nos caminhos de carregamento, seleção e persistência/atualização.
- O cálculo percorre progressão de classes, perícias, inventário, talentos, magias e espaços de magia. É trabalho de CPU e pode crescer com a ficha.
- O cálculo síncrono é um risco real para a thread principal, mas voltar à tela inicial sem recriar o ViewModel não implica, por si só, que ele seja executado novamente.
- `MainActivity` não registrava pausa/retomada nem tempos de quadros.
- `CrashLogStore` registrava exceções não tratadas, mas não consultava encerramentos anteriores do processo nem ANRs históricos.
- A exceção de socket do multiplayer permanece classificada como não relacionada ao cenário relatado.

## Implementação nesta branch

### Cálculo de estatísticas
- As chamadas de cálculo foram encaminhadas para `Dispatchers.Default`.
- Cada solicitação recebe um token monotônico. Uma conclusão só atualiza `computedStats` se ainda for a solicitação mais recente e se o personagem ativo continuar igual ao personagem calculado.
- A troca para lista vazia invalida cálculos pendentes.
- O estado calculado é limpo durante a atualização para não exibir estatísticas antigas junto de uma ficha nova.

### Instrumentação de lifecycle e quadros
- `MainActivity` registra eventos de pausa e retomada em I/O.
- Registra demora igual ou superior a 250 ms até o próximo callback de animação após retomada.
- `FrameMetrics` é coletado em uma thread dedicada; quadros com duração total de pelo menos 32 ms são registrados, com limitação de frequência para evitar excesso de logs.
- A instrumentação de quadros usa a API disponível a partir do Android 7.0 (API 24), compatível com o minSdk do aplicativo.

### Histórico de encerramentos
- Em Android 11 (API 30) ou superior, consulta até dez encerramentos históricos do processo.
- Registra ANR, crash Java/nativo, falta de memória, falha de inicialização, uso excessivo de recursos e encerramento por sinal.
- Para ANR, tenta anexar até 12.000 caracteres do trace fornecido pelo sistema.
- Usa timestamp persistido em preferências privadas para não repetir eventos já registrados.
- A consulta e a persistência são executadas em `Dispatchers.IO`.

## Testes acrescentados

- Token de cálculo mais recente invalida requisições anteriores.
- Token atual permanece válido até uma nova solicitação.
- Política de quadros lentos nos limites de 32 ms.

## Limitações e validação pendente

- Testes unitários e compilação Android precisam passar na CI do head final.
- É necessário testar em aparelho real: abrir ficha, alternar para outro app, retornar, observar fluidez e exportar o relatório de diagnóstico.
- Se houver novo congelamento, o relatório deverá permitir correlacionar `LIFECYCLE_RESUME`, `SLOW_RESUME_FRAME_CALLBACK`, `SLOW_FRAME` e `HISTORICAL_PROCESS_EXIT`.
- `FrameMetrics` mede duração de quadros e não identifica sozinho a função responsável.
- O callback após retomada é uma aproximação do tempo até a próxima oportunidade de animação, não uma medição exata do primeiro frame apresentado.
- A coleta histórica depende do Android disponibilizar os dados; ausência de registro não prova ausência de ANR.
- Nenhuma regra de D&D 3.5, estrutura do banco ou formato de ficha foi alterado.
- Não fazer merge antes da CI verde e da revisão do diff. Não fazer merge sem autorização explícita do usuário.

## PRs e isolamento

- A PR #334, referente à abertura de fichas/itens exportados, permanece separada e não deve ser absorvida por esta alteração.

## Instrumentação complementar — 2026-11-08

A PR #337 foi ampliada sem abrir uma PR paralela, após conferir que ela continuava sendo a PR de estabilidade aberta e que a PR #334 trata de outro assunto.

### Novos eventos operacionais
- `SLOW_CHARACTER_CALCULATION`: registra cálculos com duração igual ou superior a 500 ms, incluindo duração, nível do personagem e se a solicitação ainda era a mais recente.
- `CHARACTER_CALCULATION_FAILED`: registra o tipo da exceção e a validade do token; a exceção continua sendo propagada para preservar o comportamento de falha já existente e também ser capturada pelo handler global.
- Exportação: registra início, ausência de fichas, conclusão ou falha, duração e quantidade de fichas. Não registra URI, nome ou conteúdo da ficha.
- Importação: registra início, leitura concluída (somente tamanho em caracteres), validação, quantidade de fichas/avisos e conclusão/falha com duração. Avisos sobre retratos usam índice, não nome de personagem.
- Os registros são feitos no fluxo `Dispatchers.IO`; cálculo permanece em `Dispatchers.Default`.

### Privacidade e limites
- Não são registrados nomes, conteúdo JSON, URI de origem/destino, tokens ou dados de personagem.
- O texto integral do arquivo importado não é gravado; somente seu tamanho.
- A rotação existente do `diagnostics.log` permanece responsável pelo limite de armazenamento.
- O histórico circular geral de eventos e a identificação de tela ativa ainda não foram implementados. Devem ser avaliados separadamente para evitar gravar eventos de navegação em excesso ou introduzir I/O síncrono.

### Validação desta ampliação
- Commit: `eda5b33cf15d8d88cba8d68a31c6e780bf95f8d7`.
- A CI do novo head ainda precisa ser verificada.
- Permanecem pendentes testes manuais de importação/exportação e retorno do app em aparelho real.

## Continuação da investigação após o relatório do usuário — 2026-11-08

### Identificação da instalação analisada
- O relatório identifica o pacote `com.aistudio.dndsheet.qvkztr`, versão `0.0.1-beta`, Android 17/API 37 e Motorola Edge 30 Fusion.
- O `applicationId` atual em `app/build.gradle.kts` é exatamente `com.aistudio.dndsheet.qvkztr`; portanto, o relatório corresponde ao identificador configurado no projeto. O namespace Kotlin `com.example` é distinto do applicationId.
- Os dois eventos históricos `LOW_MEMORY` não demonstram, isoladamente, vazamento de memória. Quadros de aproximadamente 540 ms e 498 ms indicam travamentos perceptíveis durante as retomadas, mas não identificam a função responsável.

### Achados de código e correções desta etapa
- A seleção da ficha era mantida apenas em `MutableStateFlow` no ViewModel. Após recriação do processo, o estado começava nulo e a primeira ficha da lista era selecionada. A seleção agora é persistida em preferências privadas e restaurada quando a lista do Room é carregada; se a ficha não existir mais, a seleção é corrigida para a primeira disponível.
- A aba principal usava `remember`, que não sobrevive à recriação da Activity. Foi alterado para `rememberSaveable`.
- A decodificação e leitura EXIF da fotografia no recortador era executada dentro de `LaunchedEffect`, mas sem dispatcher de I/O explícito. O trabalho agora é executado em `Dispatchers.IO`; o estado Compose continua sendo atualizado após o retorno ao contexto principal.
- A aplicação passa a registrar snapshots agregados de memória na inicialização, em `onTrimMemory` e em `onLowMemory`: heap Java usado/total/máximo, heap nativo, PSS e memória disponível/limite do dispositivo. Não são coletados nomes, imagens, conteúdo ou atributos de fichas.

### Limitações e validação
- Esses snapshots permitirão observar a pressão de memória em ocorrências futuras; não reconstituem retroativamente a memória nos dois encerramentos históricos.
- O callback `onTrimMemory` informa pressão do sistema, não prova vazamento no aplicativo.
- A decodificação fora da thread principal reduz risco de congelamento, mas não reduz automaticamente o tamanho máximo da imagem decodificada.
- Permanecem necessários testes automatizados, CI e validação manual no aparelho: selecionar uma ficha que não seja a primeira, forçar encerramento/recriação do processo, confirmar a ficha e a aba restauradas, selecionar/recortar foto grande e exportar novo relatório.
- Nenhuma alteração foi feita em regras, cálculos de D&D, schema do Room ou formato de backup.

## Preservação da interface durante atualizações da ficha — 2026-11-09

### Problema observado
- Ao usar os botões rápidos de PV, a ficha desaparecia brevemente e a tela principal exibia “Carregando Grimório D&D 3.5...”.
- A causa foi localizada em `CharacterViewModel.recalculateStatsAsync()`: toda solicitação zerava `computedStats` antes de calcular novamente, embora a ficha ativa e as estatísticas anteriores ainda estivessem disponíveis.
- Como `DndMainScreen` usa `computedStats == null` como condição de carregamento, a interface inteira era substituída pelo indicador durante atualizações comuns.

### Correção proposta nesta PR
- Manter as estatísticas calculadas anteriormente enquanto uma atualização do mesmo personagem é processada em `Dispatchers.Default`.
- Continuar usando o token monotônico para impedir que cálculos antigos sobrescrevam o resultado mais recente.
- Limpar as estatísticas somente quando a ficha ativa muda, quando a lista fica vazia ou durante a seleção explícita de outro personagem.
- A primeira abertura continua exibindo o carregamento enquanto não existe cálculo inicial; a troca de personagem não reutiliza estatísticas da ficha anterior.
- Nenhuma regra, fórmula, valor de PV, persistência ou formato de backup é alterado.

### Testes e validação
- Teste unitário da decisão de limpar estatísticas ao trocar de personagem, incluindo inicialização sem personagem anterior.
- Teste de preservação quando o ID do personagem permanece o mesmo.
- CI e compilação Android devem ser verificadas no head final da PR.
- Validação manual pendente: usar repetidamente os botões rápidos de dano e cura, confirmar que a ficha permanece visível e verificar que os valores calculados são atualizados; trocar de personagem e confirmar que não aparecem dados da ficha anterior.
