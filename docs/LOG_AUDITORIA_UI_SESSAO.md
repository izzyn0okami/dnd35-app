# Log de auditoria — navegação da ficha e sessão multiplayer

## 2026-11-05 — Separação entre ficha e mesa

### Decisões aprovadas
- A antiga aba principal **Combate** passa a se chamar **Personagem**, pois apresenta a ficha individual e seu resumo de combate, não o combate coletivo multiplayer.
- A ficha continua sendo a área central do aplicativo, preservando as cinco seções existentes: Personagem, Perícias, Carga & Itens, Talentos e Grimório.
- A Sessão de Mesa passa a ter navegação contextual própria com três áreas: **Sessão**, **Combate** e **Recursos**.
- O Mestre e os jogadores compartilham a mesma organização de áreas, respeitando suas permissões e ações disponíveis.
- O usuário pode sair temporariamente da tela da sessão e consultar qualquer aba da ficha. Isso apenas oculta a interface da sessão; não encerra hospedagem nem desconecta o jogador. O botão de Sessão de Mesa na barra superior permite retornar.
- A tela da sessão deve permitir rolagem vertical completa, sem esconder participantes, controles ou seções abaixo da área visível.

### Implementação nesta alteração
- Renomeada a primeira aba da navegação principal para **Personagem**.
- Ao selecionar qualquer aba da ficha, a tela da Sessão de Mesa é ocultada sem chamar stopHosting() ou leaveSession().
- Adicionadas as áreas contextuais Sessão / Combate / Recursos para Mestre e Jogador.
- Organizados QR Code, participantes, pedidos, chat e administração de NPCs na área Sessão.
- Organizados iniciativa, rodadas e alterações de PV na área Combate.
- Organizados baú de loot e loja na área Recursos.
- Adicionada rolagem vertical à tela da Sessão de Mesa.
- Aumentada a altura calculada da lista de participantes para acomodar cartões com duas linhas de informação.

### Restrições de escopo
- Não alterar regras de D&D 3.5, cálculos de ficha, regras de combate ou protocolo de rede.
- Não encerrar ou reiniciar uma sessão ao alternar entre a ficha e a mesa.
- Não duplicar dados da ficha: as telas existentes continuam sendo a fonte dos dados individuais.

### Validação pendente
- Executar testDebugUnitTest e assembleDebug na CI.
- Testar manualmente Mestre e pelo menos um jogador conectado.
- Confirmar alternância repetida entre ficha e sessão sem desconexão, perda de estado ou perda de sincronização.
- Verificar rolagem em aparelhos com alturas reduzidas e lista grande de participantes.


## 2026-11-05 — Auditoria funcional da área Sessão (entrada e reconexão)

### Escopo verificado
- Revisados `SessionHubScreen.kt`, `SessionViewModel.kt` e `SessionModels.kt` na branch principal.
- Verificados os fluxos de seleção de modo, hospedagem, entrada por QR Code, entrada manual e reconexão.
- Confirmado que não havia PRs abertas no repositório durante esta revisão.

### Constatações confirmadas
1. **QR Code inválido sem feedback:** o retorno do leitor é passado a `JoinPayload.fromJson()`; quando o conteúdo não corresponde ao formato esperado, o método retorna `null` e a UI não informa ao jogador por que a entrada não ocorreu.
2. **Validação insuficiente do payload:** `JoinPayload.fromJson()` extrai os campos obrigatórios, mas não valida conteúdo vazio do endereço/sessão nem o intervalo da porta. A validação deve existir no modelo/fluxo de entrada, não apenas na aparência do campo.
3. **Porta manual sem limite válido:** a tela habilita a ação quando o texto pode ser convertido para inteiro, sem verificar o intervalo TCP válido (1–65535). A validação deve impedir valores fora do intervalo e apresentar mensagem clara.
4. **Destinatário privado desconectado:** o Mestre constrói a lista de destinatários filtrando apenas `role == PLAYER`; participantes com `connected == false` continuam selecionáveis. A lista de envio deve priorizar/exibir apenas jogadores conectados, mantendo os desconectados visíveis no roster.
5. **Retorno à seleção de modo:** voltar de Mestre/Jogador altera somente o estado local `mode`; não encerra a sessão. Isso preserva a conexão, conforme decisão aprovada. Porém, a tela de seleção não indica que já existe uma sessão ativa, podendo induzir a iniciar outro fluxo. A solução deve preservar a sessão e deixar explícito o estado ativo, sem chamar `stopHosting()` ou `leaveSession()`.

### Reconexão — comportamento observado
- O estado desconectado oferece botão **Reconectar** e mantém a opção de entrar novamente manualmente.
- `SessionViewModel.reconnect()` delega ao cliente existente, que mantém os dados de conexão/token da tentativa anterior.
- A implementação documenta que reconexão deve recuperar o mesmo identificador de participante, sem duplicar roster/combate. A confirmação completa desse comportamento ainda depende de teste integrado com Mestre e Jogador; não foi declarada como validada apenas pela leitura da UI.

### Decisões para a próxima implementação consolidada
- Exibir feedback para QR Code inválido ou incompatível.
- Validar endereço e porta tanto no payload QR quanto na entrada manual.
- Não permitir selecionar destinatário desconectado para mensagem privada.
- Ao retornar à seleção de modo, mostrar claramente se há hospedagem/conexão ativa e oferecer retorno à sessão existente, sem recriá-la.
- Preservar participantes desconectados no roster para permitir reconexão.
- Acrescentar testes unitários de parsing/validação do payload e testes de UI para feedback e seleção de destinatários.

### Limites desta etapa
- Nenhum código de produção foi alterado nesta auditoria.
- Nenhuma regra de D&D 3.5 ou protocolo de rede foi modificada.
- A validação integrada de reconexão, alternância repetida entre ficha/sessão e estados de falha continua pendente.
## 2026-11-05 — Implementação consolidada da entrada e retorno à sessão

### Alterações implementadas
- `JoinPayload` agora valida identificador/nome não vazios, endereço IPv4 completo e porta TCP entre 1 e 65535. Payload QR inválido passa a ser rejeitado durante o parsing.
- A validação do endereço e da porta também ocorre no `SessionViewModel`, protegendo a conexão mesmo quando a chamada não parte da tela.
- A entrada manual aceita somente IPv4 válido e porta entre 1 e 65535; o campo de porta aceita apenas dígitos e até cinco caracteres, com mensagens de validação visíveis.
- QR Code inválido ou incompatível agora apresenta feedback ao jogador, sem iniciar uma tentativa de conexão.
- O seletor de destinatário de mensagem privada do Mestre lista apenas participantes com papel de Jogador e conexão ativa. Participantes desconectados continuam no roster.
- Ao voltar à seleção de modo com uma sessão ativa, a tela informa o estado e oferece **Retornar à sessão existente**. A ação apenas retorna à interface já ativa; não chama `stopHosting()` nem `leaveSession()` e não inicia uma nova conexão.

### Testes acrescentados
- Cobertura de payload válido e rejeição de IPv4 incompleto/inválido, porta fora do intervalo e identificador vazio.
- Cobertura da validação do endereço IPv4 usado na entrada manual.
- A execução dos testes e a compilação ainda dependem da CI desta PR.

### Validação ainda pendente
- CI: `testDebugUnitTest` e `assembleDebug`.
- Teste integrado com Mestre e Jogador em aparelhos distintos, incluindo QR inválido, entrada manual inválida, desconexão/reconexão e alternância repetida entre ficha e sessão.
- Confirmar visualmente a tela de seleção de modo em aparelhos com tela reduzida.

### Limites
- Não foram alteradas regras de D&D 3.5 nem o protocolo de rede.
- Esta alteração cobre a entrada/retorno e a seleção de destinatários privados; não encerra a auditoria das áreas Combate, Recursos e navegação geral da sessão.

## 2026-11-06 — Auditoria funcional da área Combate (Mestre e Jogador)

### Constatações confirmadas na revisão da branch principal
1. **Participantes desconectados na seleção:** a lista de candidatos filtrava apenas pelo papel Jogador, permitindo selecionar participantes que já estavam desconectados.
2. **Iniciativa inválida convertida em zero:** campo vazio ou texto não numérico era convertido silenciosamente para iniciativa 0 no momento de iniciar o combate.
3. **PV fictícios para jogador sem ficha:** quando não havia ficha recebida, a interface atribuía 10/10 PV sem informar que era um valor estimado.
4. **Diálogo sem rolagem própria:** a lista de candidatos podia ultrapassar a área disponível em aparelhos menores.
5. **Controles horizontais excessivos:** os quatro botões de dano/cura em uma única linha podiam exceder a largura disponível.
6. **Avanço não ignorava derrotados:** apesar do comentário indicar próximo combatente vivo, o servidor avançava simplesmente para o próximo índice, incluindo PV 0 ou negativos.

### Alterações implementadas nesta branch
- A seleção de combatentes agora inclui somente jogadores conectados e fichas NPC do Mestre.
- Jogadores sem ficha continuam elegíveis, mas não recebem PV inventados: ao selecioná-los, o Mestre deve informar PV atuais e máximos. A confirmação exige valores inteiros, PV máximos positivos e PV atuais não superiores aos máximos.
- A iniciativa precisa ser informada como inteiro válido; o diálogo não substitui campos vazios ou inválidos por zero.
- O conteúdo do diálogo de início de combate recebeu limite de altura e rolagem vertical própria.
- Os controles de dano/cura do Mestre e os pedidos de alteração de PV do Jogador foram reorganizados em duas linhas, reduzindo o risco de estouro horizontal.
- O avanço de turno passou a ignorar somente combatentes mortos (PV iguais ou inferiores a -10). Personagens com 0 PV estão incapacitados, mas ainda podem realizar uma ação de movimento ou padrão; personagens entre -1 e -9 PV estão morrendo, inconscientes e não podem agir, mas ainda não estão mortos. Ambos permanecem na ordem de iniciativa. A rodada aumenta quando a ordem retorna ao primeiro combatente vivo.
- Quando todos estão com PV iguais ou inferiores a -10, o estado fica sem combatente atual (índice de turno -1) e a interface informa que todos estão mortos. A recuperação desses personagens não é simulada pelo tracker; o combate deve ser encerrado ou o estado deve ser corrigido pelo Mestre.
- A lógica de avanço foi centralizada em CombatState.nextTurn() para permitir testes unitários sem depender de socket.

### Testes acrescentados
- Próximo turno ignora somente PV -10 ou inferiores, mantendo na ordem personagens com 0 PV e com PV entre -1 e -9.
- Retorno ao início da ordem incrementa a rodada.
- Todos mortos (PV -10 ou inferiores) deixam o turno sem alvo e não incrementam a rodada.
- Um único combatente vivo, mesmo com 0 PV, continua avançando a rodada corretamente.

### Limites e validação pendente
- Não foram alteradas regras de D&D 3.5, cálculos de PV, protocolo de rede ou regras de dano/cura.
- Os valores manuais de PV são apenas dados iniciais do encontro para participantes sem ficha vinculada; não são gravados na ficha do jogador.
- Ainda é necessário executar a CI (testDebugUnitTest e assembleDebug) e revisar eventuais falhas antes de considerar a alteração pronta.
- O teste integrado em dois aparelhos, com Mestre e Jogador, continua pendente para validar visualmente o diálogo em telas pequenas e a sincronização do combate após a queda/retorno de conexão.

### Referência de regra consultada
- **Livro do Jogador D&D 3.5, edição brasileira, Capítulo 8: Combate, páginas impressas 124–125** (páginas 128–129 do PDF): 0 PV = incapacitado; -1 a -9 PV = morrendo; -10 PV ou menos = morto. A regra de iniciativa deve preservar a distinção entre incapacitação, condição de morrendo e morte. Esta auditoria não implementa a perda automática de 1 PV ao fim de cada rodada nem testes de estabilização.

## 2026-11-06 — Implementação consolidada: transações e entregas da área Recursos

### Alterações já registradas nesta branch
- O HELLO do jogador transmite o ID local da ficha escolhida, separado do JSON de backup; o cliente preserva esse vínculo ao reconectar.
- O Mestre mantém o vínculo entre participante e ficha no roster local. O ID local não é transmitido no roster para os demais jogadores.
- Pedidos de pegar loot e comprar enviam um snapshot atualizado da ficha escolhida; o servidor reconcilia os dados, mas preserva o saldo monetário já autoritativo para impedir que um snapshot antigo restaure moedas gastas. A reconexão também preserva o saldo mantido pelo Mestre.
- O servidor rejeita retirada/compra quando não existe ficha vinculada e recebida.
- Compras passam a validar preço e saldo no servidor. A aritmética usa cobre como unidade exata: 1 PP = 10 PC, 1 PO = 100 PC e 1 PL = 1.000 PC. Preço ou saldo inválido, subcobre ou insuficiente é rejeitado; a conversão preserva o valor total.
- O saldo atualizado e o item comprado são enviados como uma única mensagem de concessão, e a ficha local recebe item e moedas na mesma gravação por ID.
- A concessão usa identificador único como ID do item local. Reenvio não duplica o item se ele já foi persistido.
- O servidor mantém um outbox em memória por participante, reenviando concessões não confirmadas após reconexão. O cliente só confirma depois de salvar a ficha.
- Pedidos pendentes não são descartados quando o jogador desconecta. O Mestre não pode aprová-los enquanto o destinatário estiver desconectado.
- A aba Recursos do Mestre também exibe os pedidos pendentes.
- O formulário do baú agora aceita categoria, quantidade, peso e preço; entradas inválidas não são convertidas silenciosamente para quantidade 1.

### Testes acrescentados/atualizados
- Testes unitários de conversão monetária, saldo insuficiente, preço subcobre, valores negativos e valores não finitos.
- Testes de serialização do ID local no HELLO, ocultação do ID local no roster, ID de concessão, saldo pós-compra e confirmação de entrega.
- A execução dos testes e a compilação ainda dependem da CI da PR; nenhuma aprovação de CI é declarada nesta etapa.

### Limitações ainda abertas
- O outbox é mantido somente em memória no processo do Mestre. Encerrar o processo/hospedagem perde pedidos, estoque e concessões pendentes; persistência após reinício exige armazenamento local durável.
- A entrega ainda precisa ser validada em teste integrado com dois aparelhos, incluindo queda de conexão antes/depois da gravação e confirmação.
- Na loja, cada compra debita o preço registrado e entrega uma unidade; quando o estoque é uma pilha, a quantidade é reduzida em 1 e o restante permanece disponível.
- A CI precisa confirmar compilação, testes unitários e empacotamento do APK antes de abrir solicitação de merge.


### Correções após a primeira execução da CI (2026-11-06)
- A primeira CI executou 448 testes e identificou três timeouts: reconexão rápida, concessão de loot e compra na loja.
- Os testes de loot e loja foram atualizados para entrar com uma ficha local identificada, enviar o snapshot da ficha no pedido e verificar o ID da ficha e os dados do grant recebido. O teste da loja também usa saldo suficiente e verifica o saldo após a compra.
- O cliente passou a numerar as tentativas de conexão. Respostas e atualizações de estado de uma conexão antiga são ignoradas quando uma tentativa mais recente já começou; isso evita que o encerramento tardio da conexão anterior substitua o estado da reconexão.
- Estas correções foram publicadas na branch da PR #321. A nova execução da CI ainda precisa confirmar que os três timeouts foram eliminados; não considerar a etapa validada até testDebugUnitTest e assembleDebug concluírem com sucesso.

- Ajuste adicional no teste de compra: o troco é conferido na denominação canônica de platina (50 PP para saldo inicial de 1.000 PO e preço de 500 PO), sem esperar que o servidor mantenha moedas em ouro quando a conversão as representa em platina.


### Diagnóstico da CI #1811 — falhas nas asserções de concessão (2026-11-06)
- A CI executou 448 testes; 2 falharam: concessão de loot e compra na loja. A compilação Kotlin foi concluída, mas `assembleDebug` e upload do APK foram ignorados devido à falha dos testes.
- A causa foi localizada nos testes: o servidor substitui o ID original do item pelo ID único do grant para permitir deduplicação/idempotência na persistência local. As asserções ainda comparavam o item recebido com o objeto original, incluindo o ID antigo, embora os demais campos fossem preservados.
- Corrigidas as duas expectativas para comparar o item recebido com `item.copy(id = granted.grantId)`. Isso alinha o teste ao contrato de concessão sem alterar o comportamento de produção.
- A correção foi enviada à mesma branch da PR #321. A CI precisa ser executada novamente; até sua conclusão, a etapa Recursos permanece não validada.


### Validação da CI #1814 — PR #321 (2026-11-06)
- Commit validado: `f5adc83cca3571ce20ff2b86ebbd458f352628dd`.
- Workflow Android build nº 1814, run ID `42146247120`: sucesso. `testDebugUnitTest`, `assembleDebug`, upload dos esquemas Room e upload do APK debug concluídos com sucesso.
- As duas falhas de asserção de grant da execução anterior foram eliminadas nesta execução.
- A revisão do fluxo confirmou que o servidor valida saldo/preço antes de debitar, gera ID único para cada concessão, conserva grants não confirmados no outbox e só os remove após ACK do cliente; o cliente grava item e saldo na ficha vinculada pelo ID local. A conversão monetária segue o Livro do Jogador brasileiro: 10 PC = 1 PP, 10 PP = 1 PO e 10 PO = 1 PL (Capítulo 7, seção Moedas / Tabela 7-2).
- A CI valida testes unitários e compilação, mas não substitui o teste integrado com dois aparelhos. O outbox, pedidos, estoque e concessões pendentes permanecem em memória e são perdidos se o Mestre encerrar/reiniciar a hospedagem.
- PR #321 foi marcada pronta para revisão e mesclada por squash em 2026-11-06. Commit de merge: `3c8be2b69f9a027a147ad1bbdc8af38039910885`.
- CI #1814 (run `42146247120`) validou o commit funcional `f5adc83cca3571ce20ff2b86ebbd458f352628dd`, com `testDebugUnitTest`, `assembleDebug` e upload do APK debug concluídos com sucesso. A comparação entre esse commit e o head final `5c31d4d127e9f6261be42c529ded5e73d575db83` confirmou que o único commit adicional alterou somente este log (9 linhas adicionadas, sem mudanças de código). A CI #1816, disparada para o commit de documentação, estava em andamento no momento do merge.
- A mesclagem foi autorizada pelo usuário. O teste integrado em dois aparelhos continua pendente; portanto, a implementação está integrada ao `main`, mas a auditoria funcional de Recursos ainda não está integralmente concluída. Validar compra/loot, desconexão antes e depois da gravação, reconexão e ACK sem duplicação. A persistência após reinício do Mestre continua fora do escopo implementado: outbox, pedidos, estoque e grants pendentes são mantidos em memória e se perdem quando o processo/hospedagem é encerrado.


## 2026-11-06 — Correção complementar: aprovação de atualização de ficha

### Achado na revisão após o merge da PR #321
- O servidor recebia o snapshot de uma solicitação `CHARACTER_UPDATE` e o aplicava imediatamente em `receivedCharacters`, antes de o Mestre aprovar ou negar o pedido. Isso tornava a aprovação ineficaz para a cópia da ficha mantida na sessão e permitia que uma atualização negada já aparecesse no estado do Mestre.
- O problema foi identificado por leitura do fluxo `SessionServer.receiveActionRequest()` e do teste de integração existente, que verificava somente o estado depois da aprovação e não verificava o estado antes da decisão.

### Correção proposta nesta branch
- Atualizações de ficha deixam de ser aplicadas ao receber o pedido. O snapshot só é aplicado no caminho de aprovação `approveRequest()`.
- Na primeira revisão, pedidos de loot/compra ainda reconciliavam o snapshot; o achado adicional abaixo corrigiu esse comportamento. Na versão atual, nenhum pedido de recurso aplica snapshots enviados pelo jogador: a validação usa o último estado aprovado mantido pelo servidor.
- O teste de atualização aprovada agora verifica que o snapshot antigo permanece intacto enquanto o pedido aguarda aprovação.
- Foi acrescentado teste de atualização negada, que confirma que a ficha original permanece inalterada.

### Validação
- Alterações concentradas no servidor, nos testes de integração JVM e neste log.
- CI ainda pendente nesta branch; não declarar a correção validada até `testDebugUnitTest` e `assembleDebug` concluírem.
- Não altera regras de D&D 3.5, cálculos da ficha ou protocolo de rede.


### Achado adicional na revisão da PR #322 — snapshot em pedidos de Recursos
- Mesmo após separar `CHARACTER_UPDATE`, pedidos `TAKE_LOOT` e `BUY_FROM_SHOP` ainda aplicavam o snapshot completo enviado pelo cliente antes da aprovação do Mestre. Preservar apenas as moedas não impedia alterações antecipadas em PV, atributos ou inventário.
- Corrigido na mesma branch: o servidor não aplica snapshots anexados a pedidos de Recursos. A validação da compra usa a ficha e o saldo já aprovados e mantidos pelo servidor; alterações de ficha devem passar pelo pedido `CHARACTER_UPDATE`.
- Teste adicional envia uma compra com PV e saldo adulterados no snapshot e verifica que o estado do servidor permanece inalterado enquanto o pedido está pendente.
- Esta decisão reforça a autoridade do servidor e evita que um pedido pendente de loot/compra funcione como atualização implícita da ficha.


### Achado adicional na revisão da PR #322 — aprovação concorrente/duplicada
- `approveRequest()` e `denyRequest()` consultavam e alteravam a lista de pedidos pendentes sem sincronização. Duas chamadas simultâneas para aprovar o mesmo pedido poderiam processar a mesma compra/retirada mais de uma vez, além de atualizações concorrentes da lista poderem sobrescrever pedidos recém-chegados.
- Adicionado um lock dedicado para serializar inclusão, aprovação e negação dos pedidos. Uma segunda aprovação/negação do mesmo ID não encontra mais o pedido pendente e não repete a transação.
- Teste concorrente dispara duas aprovações para a mesma compra e verifica que o saldo é debitado uma única vez e o estoque é consumido uma única vez.
- Aguardar CI após o commit; teste ainda não validado nesta etapa.


### Diagnóstico da primeira CI do teste concorrente — PR #322
- A execução de `testDebugUnitTest` executou 451 testes e falhou somente no novo teste de aprovação concorrente da compra. A compilação Kotlin foi concluída; `assembleDebug` foi ignorado porque os testes falharam.
- A causa foi uma expectativa incorreta no teste, não uma falha demonstrada na serialização: o servidor normaliza o saldo para a denominação canônica de platina. O teste esperava 900 em `goldGp`, mas o fluxo de moeda representa 1.000 PO como 100 PP e, após uma compra de 100 PO, o saldo esperado é 90 PP e 0 PO.
- Corrigidas as asserções para verificar `goldGp = 0`, `platinumPp = 90` e o mesmo saldo na concessão recebida pelo jogador.
- A correção ainda depende de nova CI; não declarar o teste aprovado até a execução mais recente do head confirmar `testDebugUnitTest` e `assembleDebug`.


### Achado adicional na revisão da PR #322 — snapshot reenviado na reconexão
- A proteção de `CHARACTER_UPDATE` ainda podia ser contornada: ao reconectar, o cliente reenvia `SessionMessage.CharacterSheet` logo após o WELCOME. O servidor aceitava essa mensagem e sobrescrevia `receivedCharacters` sem aprovação.
- Corrigido no servidor: `CharacterSheet` só inicializa a ficha quando o participante está entrando pela primeira vez e ainda não possui snapshot recebido. Quando o token de reconexão identifica um participante existente, a ficha já mantida pelo Mestre é preservada; mudanças posteriores continuam usando o pedido `CHARACTER_UPDATE`.
- Adicionado teste de integração com socket TCP real: o participante entra com PV 30, cai, reconecta com o mesmo token e envia um snapshot adulterado com PV 5; o teste confirma que o Mestre mantém PV 30.
- Esta proteção também impede que a reconexão restaure moedas ou inventário a partir de um snapshot local antigo. CI do novo head ainda pendente.

- Após tornar `CharacterSheet` uma mensagem exclusivamente inicial, foi removido do helper o parâmetro antigo de preservação condicional de moedas: snapshots iniciais e snapshots aprovados passam por um único caminho explícito. O servidor não usa mais esse helper para aceitar atualizações espontâneas em reconexões.


### Validação da CI #1854 — PR #322 (2026-11-06)
- Commit funcional validado: `0f220566ff917d0a289d7aebe3cc086b2ec49431`.
- Workflow Android build nº 1854, run ID `42148579103`: sucesso. `testDebugUnitTest`, `assembleDebug`, upload dos esquemas Room e upload do APK debug concluídos.
- O run corresponde exatamente ao head SHA consultado da PR; não foi usada uma execução de commit anterior para declarar sucesso.
- A comparação com `main` confirmou base atual `8235c1f4987ce1f066eb23167f1fcda1e339ce81`, sem commits de atraso (`behind_by = 0`). A PR #322 permanece como única PR aberta no repositório.
- A revisão da branch não encontrou comentários nem threads de revisão pendentes.
- A CI valida testes automatizados e compilação, mas não substitui teste integrado em dois aparelhos. Continuam pendentes os cenários reais de aprovação/negação de atualização de ficha, reconexão, compra/loot e ACK de concessões sem duplicação. Não declarar a auditoria multiplayer integralmente concluída até esses testes.
- A PR permanece em rascunho e não foi mesclada.


### Revisão estática da entrega de grants e ACK — PR #322 (2026-11-06)
- O cliente recebe a concessão, encaminha o evento à tela de Recursos e chama `applySessionGrant()` antes de enviar `GrantAcknowledged`. A confirmação, portanto, ocorre somente depois que a ficha vinculada foi localizada e a gravação local terminou com sucesso.
- `applySessionGrant()` busca a ficha pelo ID persistente recebido no vínculo da sessão, grava inventário e saldo da compra na mesma atualização da entidade e usa `grantId` como ID do item. Se o mesmo grant for reapresentado após uma queda, a presença desse ID faz a operação retornar sucesso sem inserir outra cópia; então o cliente pode confirmar novamente.
- O servidor guarda grants pendentes por participante, reenvia os grants na reconexão reconhecida pelo token e remove cada grant ao receber o ACK correspondente.
- Não foi encontrado, na suíte atual, um teste de integração que percorra explicitamente a sequência completa: grant emitido -> conexão cai antes do ACK -> grant reenviado com o mesmo ID -> ACK recebido -> reconexão posterior sem novo envio. Os testes atuais cobrem identificadores/protocolo e aplicação local idempotente, mas não substituem esse cenário de transporte ponta a ponta.
- A revisão estática não identificou falha funcional demonstrada nesse fluxo. A cobertura de regressão para replay/ACK permanece uma lacuna e deve ser adicionada antes de declarar a entrega resiliente integralmente auditada.
- Limitação já conhecida e mantida: o outbox é em memória. O replay protege contra queda de conexão enquanto o processo do Mestre continua ativo; não protege contra encerramento/reinício do processo ou do aparelho do Mestre. Persistência durável exige uma etapa separada de armazenamento.
