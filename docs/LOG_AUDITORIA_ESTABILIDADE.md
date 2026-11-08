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
