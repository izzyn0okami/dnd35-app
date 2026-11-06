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

## 2026-11-06 — Implementação em andamento: transações e entregas da área Recursos

### Alterações já registradas nesta branch
- O HELLO do jogador transmite o ID local da ficha escolhida, separado do JSON de backup; o cliente preserva esse vínculo ao reconectar.
- O Mestre mantém o vínculo entre participante e ficha no roster local. O ID local não é transmitido no roster para os demais jogadores.
- Pedidos de pegar loot e comprar enviam um snapshot atualizado da ficha escolhida; o servidor reconcilia esse snapshot antes de avaliar o pedido.
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
- Deve ser revisado o comportamento de compra de itens com quantidade maior que 1: nesta etapa a loja trata o registro/stack como uma unidade de estoque e debita o preço registrado no item.
- A CI precisa confirmar compilação, testes unitários e empacotamento do APK antes de abrir solicitação de merge.
