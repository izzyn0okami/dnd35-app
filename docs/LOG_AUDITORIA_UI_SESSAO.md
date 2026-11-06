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


### Teste de integração de replay e ACK acrescentado — PR #322 (2026-11-06)
- Acrescentado teste com sockets TCP reais que simula: jogador recebe grant de loot; conexão cai sem ACK; jogador reconecta com o mesmo token e recebe o mesmo `grantId`; cliente envia ACK; uma reconexão posterior não recebe novamente o grant confirmado.
- O teste também verifica que a reconexão mantém o mesmo ID de participante e que a mensagem privada enviada depois do ACK funciona como barreira de ordenação TCP para confirmar que o servidor processou a confirmação antes da próxima reconexão.
- O teste foi adicionado à suíte `SessionServerClientTest` na branch da PR #322. A execução Android build #1860 (run `42150172672`) foi iniciada para validar a nova cobertura; registrar o resultado após conclusão.
- O teste cobre o replay/ACK enquanto o processo do Mestre permanece ativo. Não cobre persistência após reinício, que continua sendo uma limitação separada do outbox em memória.


### Validação de snapshot inválido na aprovação de ficha — PR #322 (2026-11-06)
- A revisão identificou que `approveRequest(CHARACTER_UPDATE)` chamava `receiveCharacterSheet()`, mas essa função ignorava silenciosamente JSON inválido ou backup sem ficha e não informava se a atualização havia sido aplicada. Mesmo assim, o pedido era respondido como aprovado.
- `receiveCharacterSheet()` agora retorna sucesso/falha. Na aprovação, o servidor só confirma o pedido quando consegue interpretar e aplicar a ficha; snapshot inválido ou ausente é negado com motivo explícito, sem alterar a última ficha recebida.
- Acrescentado teste TCP/cliente-servidor que envia snapshot malformado, aprova o pedido no Mestre e verifica resposta negativa, motivo e preservação dos PV anteriores.
- A correção e o teste foram adicionados à branch existente da PR #322. A CI deve ser conferida novamente no SHA final após o registro deste log.


### Serialização do ACK com registro de grants — PR #322 (2026-11-06)
- Revisão estática encontrou uma corrida possível entre `acknowledgeGrant()` e `registerPendingGrant()`: a remoção do último grant podia observar o mapa vazio e removê-lo do outbox enquanto uma nova concessão era inserida no mesmo mapa.
- `acknowledgeGrant()` agora usa o mesmo `requestLock` que serializa aprovação/registro de grants. Assim, ACK e criação de uma nova concessão não removem acidentalmente o mapa que contém a nova entrega.
- A correção foi aplicada na branch existente da PR #322. A nova CI precisa validar o SHA final antes de qualquer merge.



### Validação final da CI #1871 — PR #322 (2026-11-06)
- Commit de código validado: `eb9da3e9f209a9b643967ad00f2355778a3c1834`.
- Workflow Android build nº 1871, run ID `42151823349`: sucesso. `testDebugUnitTest`, `assembleDebug`, upload dos esquemas Room e upload do APK debug concluídos com sucesso.
- A execução corresponde exatamente ao head consultado da PR #322; não foi usada uma execução anterior para declarar sucesso.
- A comparação com `main` confirmou 29 commits à frente e nenhum commit atrasado (`behind_by = 0`). A PR #322 permanece como única PR aberta.
- A revisão estática confirmou que snapshots de ficha só são aplicados no caminho de aprovação de `CHARACTER_UPDATE`; snapshots anexados a pedidos de loot/compra não alteram o estado aprovado; reconexão não substitui a ficha mantida pelo Mestre; aprovação/negação e ACK/registro de grants são serializados pelo mesmo lock.
- Testes automatizados cobrem snapshot inválido, atualização aprovada/negada, tentativas concorrentes de compra e replay/ACK de grant por sockets TCP reais.
- A PR permanece em rascunho e não foi mesclada. A CI verde não substitui teste manual integrado em dois aparelhos.
- Limitações mantidas: outbox e estado da sessão são voláteis e se perdem quando o processo do Mestre é encerrado/reiniciado; teste real com dois aparelhos continua pendente.



### Correção complementar identificada na revisão de reconexão — PR #322 (2026-11-06)
- A revisão estática identificou um caso-limite: se a primeira conexão recebesse o WELCOME e caísse antes de enviar `CharacterSheet`, a reconexão seria reconhecida pelo token, mas o servidor descartaria o primeiro snapshot porque condicionava a aceitação a `existingId == null`.
- Ajustado o servidor para aceitar `CharacterSheet` sempre que ainda não existir ficha recebida para aquele participante, inclusive após reconexão. Se já houver ficha no estado do Mestre, o snapshot reenviado continua sendo ignorado; alterações posteriores permanecem restritas ao fluxo aprovado de `CHARACTER_UPDATE`.
- Acrescentado teste TCP real que interrompe a primeira entrada antes do snapshot, reconecta com o mesmo token e confirma que a primeira ficha é então recebida e vinculada.
- Esta correção complementa, sem enfraquecer, a proteção contra sobrescrita de fichas já aprovadas. A CI do novo head deve ser conferida antes de qualquer merge.


### Sincronização dos PV do combate com a ficha do jogador — PR #322 (2026-11-06)
- Decisão do usuário: a sincronização automática dos PV entre Combate e Personagem é o comportamento definitivo.
- Achado: CombatUpdate atualizava somente o estado de sessão (PlayerSessionUiState); não havia persistência do combatente do jogador na ficha Room local. A sincronização precisa continuar ativa mesmo quando o jogador navega para fora da tela/aba Sessão.
- O ID de Combatant para jogadores é o ID do participante da sessão; para NPCs é npc:<characterId>. A sincronização filtra estritamente combatant.id == selfId e usa o ID da ficha local capturado no momento da entrada, sem depender da ficha atualmente selecionada.
- A coleta foi colocada no nível de DndMainScreen, para permanecer ativa enquanto o jogador navega pelas abas do aplicativo. A ficha vinculada é atualizada somente nos campos currentHp e tempHp; PV máximos, inventário, moedas e demais dados não são substituídos.
- A gravação consulta a entidade mais recente pelo ID e usa o mesmo characterSaveMutex dos grants de loot/compra, evitando atualizar uma ficha diferente ou disputar uma gravação de concessão.
- O modelo de combate já consome PV temporários antes dos PV normais, mantém PV negativos e não restaura PV temporários com cura. A rotina de ajuste local da ficha foi corrigida para também preservar PV negativos e permitir cura a partir de valores negativos, sem ultrapassar o máximo.
- Testes acrescentados para dano absorvido por PV temporários, dano abaixo de zero, cura a partir de PV negativos, limite máximo de cura e preservação de todos os campos da ficha fora dos PV atuais/temporários.
- A sincronização não altera NPCs, fichas não vinculadas nem a UI. A reformulação visual continua planejada para uma etapa posterior.
- Validação da CI e teste integrado em dois aparelhos ainda pendentes para este novo head. Manter a PR em rascunho e não mesclar até concluir os testes manuais já combinados.


### Complemento da sincronização de PV — estado aprovado no servidor e PV temporários (2026-11-06)
- A revisão do fluxo revelou que sincronizar apenas o banco local do jogador deixaria o snapshot aprovado mantido pelo Mestre desatualizado. Isso poderia fazer um combate posterior começar com PV antigos e manter a ficha do servidor divergente após o fim do encontro.
- SessionServer.applyDamage agora atualiza, para combatentes jogadores vinculados, somente currentHp e tempHp também em receivedCharacters antes de transmitir a atualização de combate. Assim, o servidor conserva os PV autoritativos para encontros seguintes e validações futuras.
- O diálogo de início de combate agora carrega tempHp da ficha recebida do jogador ou da ficha NPC do Mestre para Combatant. Isso evita apagar PV temporários existentes quando o combate começa.
- Teste TCP/cliente-servidor acrescentado: a ficha aprovada começa com 20 PV e 4 PV temporários; 10 de dano resulta em 14 PV e 0 temporários no estado de combate e no snapshot aprovado; 5 de cura eleva para 19 PV sem ultrapassar o máximo.
- A sincronização local do jogador continua sendo executada no DndMainScreen, inclusive fora da tela de sessão. O snapshot do servidor é atualizado somente pelo comando de dano/cura do Mestre.
- CI do head atualizado ainda pendente. Não considerar a correção validada até testes unitários e compilação concluírem; teste manual em dois aparelhos continua obrigatório.

- Revisão de concorrência: a atualização de PV do servidor agora compartilha requestLock com a aprovação/negação de pedidos. Isso impede que um snapshot aprovado concorra com a escrita dos PV do combate e sobrescreva um dos estados.

- Diagnóstico da CI #1893: a compilação Kotlin passou, mas um teste legado ainda exigia que dano nunca reduzisse PV abaixo de zero. Como isso contradiz as regras de estados incapacitado/morrendo e a sincronização agora preserva PV negativos, a expectativa antiga foi atualizada para -5 PV (e -999 no caso extremo limitado pelo modelo). Nova CI necessária no head atualizado.


### Validação da sincronização de PV — CI #1906 / PR #322 (2026-11-06)
- Head validado: dad79a588ca35fa934105b9565e505c8eefd5d26.
- Workflow Android build nº 1906, run ID 42218656288: sucesso. testDebugUnitTest, assembleDebug, upload dos esquemas Room e upload do APK debug concluídos com sucesso.
- Artefato APK debug: dnd35-app-debug, ID 13501944388, aproximadamente 20,4 MB; expira em 2026-11-13. Esquemas Room: artefato ID 13501454933.
- A execução validou o head exato da PR #322. A compilação Kotlin, os testes unitários e o empacotamento Android passaram.
- Testes manuais em dois aparelhos: o usuário já confirmou os cenários 1 e 2 da lista de integração anterior. Cenários 3 a 6 ainda precisam ser executados, incluindo grants de loot/compra e reconexão/ACK.
- PR permanece aberta, em rascunho, mergeável, sem commits atrasados e sem revisões pendentes. Não mesclar até os testes restantes e autorização explícita do usuário.


### Correção de concorrência no estado de combate — PR #322 (2026-11-06)
- Revisão estática encontrou que `applyDamage()` usava o lock compartilhado, mas `startCombat()`, `nextTurn()` e `endCombat()` liam/alteravam `_combatState` fora dele. Operações simultâneas podiam sobrescrever alterações recentes e transmitir atualizações fora da ordem do estado efetivamente persistido.
- O mecanismo foi renomeado para `stateLock` e passou a proteger, na mesma seção crítica, início, dano/cura, avanço de turno, encerramento e os broadcasts correspondentes. A atualização dos PV da ficha aprovada continua sob o mesmo lock; aprovações/negações de pedidos e registro/ACK de grants também permanecem serializados.
- A limpeza do estado de combate em `stop()` também usa o lock, evitando competir com uma mutação em andamento.
- Adicionado teste concorrente com duas threads: 1.000 aplicações de dano e 1.000 avanços de turno partem simultaneamente; o resultado esperado é 0 PV e rodada 1.001, sem perda de atualização.
- Esta correção foi aplicada diretamente na branch existente `fix/session-character-update-approval` da PR #322, sem abrir outra PR. A validação automatizada do novo head ainda precisa ser concluída; não mesclar antes da CI e da autorização explícita.


### Validação da CI #1913 e atualização dos testes integrados — PR #322 (2026-11-06)
- Head de código validado: `aed32f13a3ba8c05b082ad0ff612beaddab7a9f8`.
- Workflow Android build nº 1913, run ID `42228831033`: sucesso. `testDebugUnitTest`, `assembleDebug`, upload dos esquemas Room e upload do APK debug concluídos.
- A execução corresponde exatamente ao head de código consultado da PR #322.
- O usuário confirmou os cenários integrados 1 e 2 e orientou considerar os cenários 3 a 6 aprovados provisoriamente. Registrar essa aprovação como provisória, sem afirmar que houve nova execução técnica desses cenários nesta etapa.
- A comparação com `main` confirma 52 commits à frente e nenhum commit atrasado (`behind_by = 0`). A PR #322 continua sendo a única PR aberta e o GitHub informa que ela é mergeável.
- A sincronização automática de PV atuais e temporários entre Combate e a ficha vinculada está implementada e coberta por testes automatizados. A CI passou para o head atual.
- Limitação conhecida: outbox e estado da sessão continuam em memória; encerramento/reinício do processo do Mestre perde pedidos, estoque e grants pendentes. Persistência durável não foi incluída nesta PR.
- A PR ainda não foi mesclada. Não realizar merge sem autorização explícita do usuário.


### Serialização das mutações de sessão e snapshots iniciais — PR #322 (2026-11-06)
- Revisão estática após a CI #1913 encontrou mutações de estado compartilhado fora de `stateLock`: alterações diretas no baú/loja, atualizações do roster e a leitura dos snapshots de estado enviados a uma nova conexão podiam intercalar-se com aprovações e broadcasts.
- As mutações de adicionar/remover loot, abrir/fechar loja, adicionar participante e marcar desconexão agora usam o mesmo lock que serializa aprovações, grants e estado de combate.
- O snapshot inicial enviado a uma conexão (combate, baú, loja e grants pendentes) também é preparado/enfileirado sob o lock, evitando que uma atualização mais antiga seja colocada na fila depois de um broadcast mais recente.
- O encerramento limpa os estados mutáveis da sessão sob o mesmo lock.
- Acrescentado teste concorrente com 200 adições ao baú, verificando que nenhum item é perdido.
- Estas alterações e o novo teste foram adicionados à PR #322 existente. A CI precisa ser executada novamente no head atualizado antes de considerar a revisão concluída.


## 2026-11-06 — Validação final e merge da PR #322

### Validação final
- Head validado: `a530dfa5306795b77ba45eb51283cf2f0d8f9c5c`.
- Android build #1923, run ID `42229638697`: sucesso, associado exatamente ao head acima. `testDebugUnitTest`, `assembleDebug` e publicação dos artefatos foram concluídos.
- A PR estava aberta, pronta para revisão e mergeável; não havia revisões, threads ou comentários pendentes. Era a única PR aberta no repositório.
- O usuário autorizou explicitamente a mesclagem em 2026-11-06.

### Resultado
- PR #322 (`fix/session-character-update-approval`) mesclada por squash.
- Commit de merge: `8527a7b13a47a71b7117fd01534450bd990bd8fe`.
- A implementação de autoridade dos snapshots, aprovação de atualização de ficha, reconexão, grants/ACK, sincronização de PV e serialização de estado foi integrada à branch `main`.
- Os cenários integrados 3 a 6 permanecem registrados como aprovação provisória do usuário, não como execução técnica independente confirmada nesta validação.

### Limitações e continuidade
- Outbox, pedidos, estoque e grants pendentes continuam em memória e podem ser perdidos se a hospedagem/processo do Mestre for encerrado ou reiniciado. Persistência durável permanece fora do escopo desta PR.
- Teste integrado físico em dois aparelhos continua sendo uma validação recomendada para compra/loot, desconexão, reconexão e ACK; não confundir CI verde com teste de campo.
- O acompanhamento automático da PR #322 foi encerrado após o merge.


## 2026-11-06 — Auditoria complementar de concorrência no chat multiplayer

### Constatações
- A leitura do estado após o merge da PR #322 encontrou duas rotas de escrita concorrentes em `_privateChatLog`: mensagens recebidas de vários jogadores são processadas em handlers TCP distintos, enquanto o Mestre também pode registrar mensagens enviadas pela interface.
- As duas rotas faziam leitura-modificação-escrita do mesmo `MutableStateFlow` sem sincronização. Intercalações podiam descartar uma ou mais entradas do histórico, apesar de as mensagens terem sido recebidas.
- No cliente, os eventos `RequestResolved`, `LootGranted`, `ShopPurchaseGranted` e `PrivateMessage` não verificavam a geração da conexão. Uma mensagem já lida por uma conexão antiga poderia ser emitida depois de uma reconexão mais recente ter começado.

### Correções nesta branch
- As duas rotas de escrita do histórico privado agora usam o mesmo `stateLock` que protege o estado compartilhado do servidor. O envio de rede da mensagem privada do Mestre permanece fora da seção crítica.
- O cliente agora confere `connectionGeneration` antes de emitir resultados de pedidos, concessões de loot/compra e mensagens privadas; eventos de uma conexão obsoleta são descartados.
- Acrescentado teste de integração com seis clientes TCP concorrentes, cada um enviando 40 mensagens privadas. O teste exige que as 240 entradas distintas apareçam no histórico do Mestre, sem perda.

### Validação pendente
- Executar `testDebugUnitTest` e `assembleDebug` na CI para o SHA final desta branch.
- Revisar o teste concorrente e a proteção de eventos obsoletos após a execução.
- Nenhum merge foi realizado. A limitação de persistência volátil do outbox/estoque/pedidos/grants permanece fora deste escopo.


### Ajuste após a primeira CI
- A primeira execução compilou o código e executou 464 testes; 463 passaram e o teste novo expirou ao aguardar as 240 mensagens.
- A causa provável foi o desenho do teste: o método público `sendPrivateMessage` é fire-and-forget e o teste disparava 240 corrotinas em rajada, sem aguardar a conclusão de cada lote. Isso tornava a validação excessivamente dependente do agendamento das corrotinas.
- O teste foi reestruturado para manter seis conexões concorrentes, enviar um lote de seis mensagens por rodada e aguardar o histórico atingir o total esperado antes de iniciar a rodada seguinte. São mantidas 40 rodadas e 240 mensagens distintas. O timeout por lote foi ampliado para 30 segundos.
- Esta alteração corrige a determinabilidade do teste; não é apresentada como prova de que o timeout anterior demonstrou perda de mensagens.
- A CI precisa ser executada novamente no novo SHA antes de qualquer merge.

- A segunda CI mostrou que a espera entre lotes também usava igualdade exata (`size == esperado`) em um `StateFlow`. Como `StateFlow` pode conflar estados intermediários, o coletor poderia observar um tamanho posterior e nunca satisfazer a igualdade. O predicado agora usa `size >= esperado`, mantendo a espera monotônica sem depender da observação de cada tamanho intermediário.
- A segunda execução voltou a falhar somente no teste novo; a terceira CI deve validar o predicado corrigido antes de qualquer merge.

- A terceira CI voltou a expirar no teste, indicando que a espera por estado do cliente ainda deixava o teste acoplado ao comportamento assíncrono de envio/recepção do `SessionClient`. Para isolar o alvo da auditoria, o teste foi simplificado para seis sockets TCP reais e independentes: cada um faz HELLO/WELCOME, o teste aguarda o roster completo e então seis workers enviam 40 frames privados cada. A validação aguarda o histórico do servidor atingir pelo menos 240 entradas e confere quantidade e unicidade.
- A quarta CI deverá validar esse teste de integração isolado. Até lá, não há autorização técnica para merge.


### Validação final e merge da PR #323 (2026-11-06)
- Head validado: `dee7039ee94e4490e17b24b8bfdaad05e68d17a7`.
- Android build nº 1942, run ID `42236392921`: sucesso. `testDebugUnitTest`, `assembleDebug`, upload dos esquemas Room e upload do APK debug concluídos.
- Revisão final do diff confirmou a sincronização das duas rotas que escrevem em `_privateChatLog`, a verificação de geração antes de emitir os eventos assíncronos no cliente e o teste TCP com seis conexões/240 mensagens distintas.
- A PR não tinha reviews, threads nem comentários pendentes; a branch estava atualizada em relação a `main` e o GitHub informou `mergeable=true`.
- A PR #323 foi marcada pronta para revisão e mesclada por squash após autorização prévia do usuário para mesclar quando possível.
- Commit de merge: `75ac4bd87b1b9f114e073534a1a0017b818b7235`.
- As três primeiras execuções do teste concorrente expiraram; a quarta, no head final, passou. O histórico de tentativas permanece registrado acima para evitar repetição do diagnóstico.
- A correção de concorrência do chat e o descarte de eventos de conexões obsoletas estão integrados em `main`.
- A persistência durável do outbox, pedidos, estoque e grants continua não implementada. A próxima etapa da auditoria deve primeiro mapear o ciclo de vida e os dados que precisam sobreviver a encerramento inesperado do processo do Mestre; qualquer implementação de persistência deve ser tratada como escopo próprio.


## 2026-11-06 — Diagnóstico de persistência e recuperação da sessão do Mestre

### Escopo inspecionado
- `SessionServer.kt`: criação/encerramento do servidor, estado da sessão, pedidos, fichas recebidas, combate, baú, loja, chat privado, tokens de reconexão e outbox de grants.
- `SessionViewModel.kt`: ciclo de vida da hospedagem, estado apresentado à UI e limpeza em `onCleared()`.
- `AppDatabase.kt`, `CharacterDao.kt` e `CharacterModels.kt`: armazenamento Room existente e entidades persistidas.

### Constatações confirmadas
1. O estado autoritativo da sessão é mantido em memória no objeto `SessionServer`: roster, fichas recebidas, combate, pedidos pendentes, baú, loja, chat privado, tokens de reconexão e grants aguardando ACK.
2. `SessionServer.stop()` fecha o servidor e os sockets, limpa tokens e grants e redefine os demais estados para os valores iniciais. Não há rotina de restauração de sessão.
3. `SessionViewModel` mantém o servidor apenas em uma propriedade de instância. `onCleared()` chama `stopHosting()`; portanto, a hospedagem não tem mecanismo próprio de restauração após destruição do processo/ViewModel.
4. O banco Room atual contém somente a entidade `CharacterEntity`, na tabela `characters` (versão 9). Não há entidade/DAO para sessões, participantes, pedidos, estoque/baú, estado de combate ou outbox de grants.
5. A ficha do jogador é persistida localmente, mas isso não torna duráveis os dados de sessão mantidos no aparelho do Mestre. A reconexão atual depende de `rejoinTokens` e do estado de participantes que existem apenas na memória do servidor.
6. A aplicação já usa Room com migrações explícitas e evita migração destrutiva; qualquer expansão do banco deve preservar as fichas existentes.

### Impacto
- Se o processo do Mestre for encerrado inesperadamente, não existe estado persistido para reconstruir a sessão, restaurar os tokens de reconexão, recuperar pedidos pendentes, reabrir a loja/baú, reconstruir o combate ou reenviar grants não confirmados.
- A reconexão atual cobre queda/retorno de conexão enquanto o mesmo servidor permanece vivo; não equivale a retomar uma sessão após reinício do processo.
- Os testes unitários e a CI não demonstram recuperação após morte do processo. Isso requer testes de persistência/reinicialização, além dos testes físicos em dois aparelhos já registrados.

### Decisões ainda necessárias antes de implementar
- Definir se o requisito é retomar a sessão automaticamente após recriar o processo do Mestre ou apenas permitir salvar/encerrar uma sessão de forma controlada. Não presumir que uma sessão encerrada voluntariamente deva ser retomável.
- Definir quais dados precisam sobreviver: identidade/convite da sessão, participantes e tokens, fichas aprovadas, combate, pedidos pendentes, baú, loja e grants pendentes.
- Definir política de expiração/segurança dos tokens e o que fazer com jogadores que tentem reconectar depois de a sessão ser restaurada.
- Garantir atomicidade entre débito de moedas/alteração de estoque, criação do grant e gravação no outbox; o ACK só deve remover um grant depois da confirmação de persistência local pelo jogador.
- Projetar entidades e migrações Room sem alterar ou apagar fichas existentes. A gravação e a restauração devem ser serializadas com as mutações do estado autoritativo.

### Situação
- Diagnóstico estático concluído; nenhuma implementação de persistência foi feita nesta etapa.
- A persistência durável é uma frente separada e maior que a correção de concorrência da PR #323. Não iniciar alteração de protocolo, serviço em primeiro plano ou migração do banco sem definir primeiro o requisito de retomada e o ciclo de vida desejado.


### Decisão de produto — retomada após falha e encerramento manual (2026-11-06)
- Requisito confirmado pelo usuário: se o aplicativo/processo do Mestre terminar inesperadamente, a sessão deve poder ser retomada, recuperando o estado persistido necessário e permitindo que os jogadores reconectem.
- Encerramento manual da sessão deve exigir confirmação em diálogo antes de executar `stopHosting()`. A ação confirmada é intencional e encerra a sessão; não deve deixar uma sessão marcada como recuperável para retomada automática.
- Diferenciar explicitamente os estados de sessão ativa, recuperação pendente/recuperável e encerrada manualmente. O diálogo deve explicar que encerrar desconecta os jogadores e descarta a possibilidade de retomar aquela sessão, caso esse seja o comportamento final definido.
- Persistência e retomada devem ser implementadas em etapas: primeiro modelo durável e transações atômicas; depois restauração/reconexão; por fim ciclo de vida da hospedagem e diálogo de confirmação, com testes de morte/recriação do processo.
- A sessão não deve ser considerada encerrada apenas porque o processo morreu; a intenção é preservar o último estado confirmado no armazenamento local do Mestre.


## 2026-11-06 — Primeira camada de armazenamento durável da sessão

### Implementação iniciada na branch `feature/durable-session-storage`
- Criada a entidade Room `HostedSessionEntity`, com registro singleton para a sessão hospedada neste aparelho.
- O registro separa identificador da sessão, nome do Mestre, estado (`ACTIVE`/`ENDED`), versão do formato do snapshot, conteúdo JSON e instante da última gravação.
- Criado `HostedSessionDao` com leitura exclusiva de sessão recuperável, leitura do registro atual, substituição do checkpoint, marcação de encerramento manual condicionada ao ID da sessão e limpeza explícita.
- A entidade não armazena objetos de runtime (sockets, Bitmap, Flow ou corrotinas); esses elementos deverão ser reconstruídos na recuperação.
- `AppDatabase` foi atualizado da versão 9 para 10, incluindo a entidade e a migração não destrutiva `MIGRATION_9_10`, que cria somente a tabela `hosted_session`. A tabela `characters` e suas fichas não são alteradas.

### Limites desta fatia
- Esta camada fornece armazenamento, mas ainda não conecta checkpoints às mutações do `SessionServer`; não considerar a recuperação funcional nesta etapa.
- O formato de snapshot está versionado, mas o codec completo e a captura/restauração de participantes, fichas aprovadas, combate, pedidos, baú, loja, chat, tokens e grants ainda precisam ser implementados.
- Compras/loot ainda exigem transação durável coordenando saldo, estoque/baú, grant e outbox antes de enviar a concessão; o DAO isolado não resolve essa atomicidade.
- Ainda não foram executados testes Android/Room nem CI para esta branch. Não mesclar até validar migração 9→10, preservação de fichas existentes e integração transacional.

## 2026-11-06 — Codec versionado e captura/restauração do snapshot

- PR #324: SHA inicial desta etapa `05560913fa3c60000721ae9b8a566779b37d5eb4`; CI anterior concluiu com sucesso para a base Room 9→10.
- Criado `HostedSessionSnapshot` e `HostedSessionSnapshotCodec`, com envelope `dnd35-hosted-session` e versão explícita 1.
- Snapshot inclui: ID/nome da sessão, roster, vínculos de ficha, fichas recebidas, combate, pedidos pendentes, baú, loja, chat privado, tokens de reconexão e grants pendentes por participante.
- A lista persistida de participantes usa serialização própria e preserva `characterId`. Não reutiliza `SessionMessage.Roster`, cujo formato de rede deliberadamente omite o ID local da ficha.
- Fichas recebidas são codificadas pelo formato de backup existente; o vínculo local de ficha permanece no campo `Participant.characterId`. URIs privadas de retratos não são transportadas pelo formato de backup e não são necessárias para reconstruir a autoridade de sessão.
- Decodificação rejeita formato ou versão desconhecida e falha diante de ficha, pedido ou grant inválido; não descarta silenciosamente partes críticas do snapshot.
- `SessionServer.createSnapshot()` captura o estado sob `stateLock`. `restoreSnapshot()` restaura os dados antes de abrir a porta, marca jogadores desconectados e mantém tokens e grants para reconexão/entrega.
- Testes adicionados para round-trip dos componentes do snapshot, versão futura, preservação do ID de ficha no roster e restauração do estado no servidor.
- **Limitação deliberada:** ainda não há gravação automática dos snapshots no Room, nem retomada no `SessionViewModel`. As mutações ainda não aguardam commit durável; compra/loot continuam sem garantia transacional após queda do processo. Não anunciar recuperação funcional nem mesclar esta PR como solução completa.
- Próximas etapas: versionar o schema Room 10 gerado; integrar checkpoints sequenciados ao ciclo de vida do servidor; fazer compra/loot persistirem saldo/estoque/grant/outbox antes do envio e só remover outbox após ACK durável; restaurar o host e gerar QR/endereço/porta novos; implementar confirmação explícita para encerramento manual; validar migração 9→10 e reinício real do processo.

- O schema Room `app/schemas/com.example.dnd35.data.AppDatabase/10.json` foi versionado. Foi comparado com o artefato `room-schemas` produzido pela CI da base Room 10; preserva a entidade `characters` e inclui somente `hosted_session`. A migração 9→10 cria a mesma definição SQL da entidade.
- O decoder valida também a integridade estrutural das listas de inventário: JSON truncado/inválido não pode virar uma lista vazia silenciosamente.

## 2026-11-06 — Integração dos checkpoints, retomada e barreira transacional

- `SessionServer` passou a emitir snapshots após mutações autoritativas: participantes/conexões, fichas recebidas, combate, pedidos, baú, loja, chat e ACKs da outbox.
- `SessionViewModel` grava checkpoints na entidade Room singleton por uma fila serial de comandos. A fila preserva a ordem entre checkpoints, encerramento manual e novas sessões.
- Ao iniciar o ViewModel, procura uma sessão `ACTIVE`, valida a versão e a identidade do snapshot, restaura o domínio antes de abrir o TCP e gera novo QR Code com endereço/porta atuais. Jogadores são apresentados como desconectados e podem reconectar usando os tokens restaurados.
- Falha de leitura/decodificação não apaga nem encerra o registro salvo; a UI apresenta erro e mantém os dados para diagnóstico/tentativa posterior.
- Encerramento manual agora exige diálogo de confirmação. A confirmação enfileira `markManuallyEnded`; `onCleared` não marca sessão como encerrada e tenta concluir os checkpoints/encerramentos já enfileirados antes de liberar o ViewModel.
- Compras e retiradas do baú foram alteradas para montar um snapshot com saldo/estoque/baú atualizado e grant na outbox, aguardar o commit Room e só então transmitir a concessão e a resposta de aprovação. Em falha de persistência, o servidor restaura o snapshot anterior e nega o pedido. A barreira é serializada com a fila de checkpoints, evitando que um snapshot antigo sobrescreva o commit da transação.
- O ACK continua idempotente: se a remoção da outbox não chegar a ser persistida antes de uma queda, a concessão pode ser reenviada; o cliente deve reconhecer o mesmo `grantId` e não duplicar a aplicação.
- Teste de integração adicionado para manter a compra bloqueada enquanto o commit não é liberado, verificando que nenhum grant chega antes da persistência.
- **Ainda pendente:** confirmar que o workflow Android passa para o SHA mais recente; teste real de morte/recriação do processo; teste físico Mestre/Jogador; teste de migração 9→10 sobre base real com fichas; revisão de que todos os fluxos de mutação disparam checkpoint. A retomada automática não deve ser considerada validada até esses testes.

- O cliente do Jogador agora aceita o novo IP/porta do QR recuperado e tenta reconectar com o token já recebido, desde que o ID da sessão seja o mesmo. Foi adicionado teste de integração com dois servidores sequenciais e preservação do mesmo ID de participante.
- Limitação do lado Jogador: o token de reconexão ainda vive na memória de `SessionClient`; se o processo do Jogador também for encerrado, ele não tem token local persistido e deverá entrar novamente como participante novo. A retomada do processo do Mestre permanece o requisito desta etapa.
- Decoder e restauração agora recusam referências órfãs (fichas, pedidos, tokens ou outbox sem participante no roster), em vez de descartá-las silenciosamente.

- Aprovações do Mestre agora suprimem checkpoints intermediários durante a mutação composta e emitem um único snapshot após resolver o pedido. Isso evita persistir, por exemplo, dano aplicado com o pedido de PV ainda pendente e reaplicar o dano após recuperação.

- Teste de migração adicionado com banco Room v9 criado de verdade por uma definição legada contendo `CharacterEntity`: insere uma ficha com PV, nível, moedas e inventário, abre a versão 10 usando `MIGRATION_9_10` e verifica a preservação dos dados e a disponibilidade da nova tabela. A execução ainda depende da CI.

- Encerramento manual agora aguarda a confirmação do comando `EndAndWait` na mesma fila serial, depois de checkpoints anteriores. Assim, o método não retorna ao fluxo da UI antes de gravar `ENDED`; falha de gravação é registrada e exibida como erro.

- O encerramento do ViewModel agora captura o snapshot final e fecha o servidor/sockets sob o mesmo `stateLock`; só depois aguarda o commit final e drena a fila. Isso evita uma mutação concorrente entre o checkpoint final e o fechamento do host.
