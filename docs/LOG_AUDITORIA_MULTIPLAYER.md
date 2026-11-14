# Log de auditoria — comunicação multiplayer

Data: 2026-11-05.

- Servidor: broadcasts, mensagens individuais e estado inicial passam por uma fila limitada por conexão. Um escritor por conexão executa as transmissões em I/O e mantém a ordem FIFO.
- Cliente: o BYE é enviado em coroutine de I/O; o socket é fechado depois da tentativa de envio.
- Hospedagem: SessionViewModel.hostSession inicia o ServerSocket e consulta o IP local em Dispatchers.IO, evitando I/O de rede na thread principal.
- Protocolo JSON, framing TCP, tokens de reconexão e sincronização de frames foram preservados.
- Teste de regressão com StrictMode cobre o broadcast iniciado pela thread chamadora; testes existentes continuam cobrindo conexão, reconexão, combate, pedidos, loot, loja e mensagens privadas.
- Pendente: CI completa e teste manual em aparelhos reais com Mestre e pelo menos dois jogadores. Não fazer merge antes da CI verde e revisão final.

## 2026-11-13 — Baú de loot, loja, distribuição de itens, kits iniciais e sistema métrico

### Decisões funcionais
- O baú de loot e a loja usam o mesmo seletor de itens: catálogo do Livro do Jogador e criação de item personalizado. O catálogo completo fica habilitado somente nos fluxos do Mestre (loot, loja e concessão direta); o diálogo normal do inventário abre em modo personalizado para jogadores. A importação de arquivos externos continua livre pelo fluxo normal de inventário.
- O estoque da loja é independente do inventário de uma ficha NPC. O Mestre pode criar uma loja vazia, incluir itens do catálogo, editar preço e quantidade, remover produtos e salvar/carregar modelos locais de estoque.
- O preço inicial de um produto vem do valor registrado no catálogo. A edição do item permite sobrescrever o preço e a quantidade em estoque.
- O preço é calculado em PO, mas a carteira do jogador é somada em PC equivalentes. PP, PO e PL são aceitas na mesma compra; o saldo é normalizado após o débito. A tela desativa a compra quando o saldo local não cobre o total, e o servidor valida novamente saldo, preço e quantidade antes de concluir.
- A quantidade comprada é enviada no pedido, validada contra o estoque e descontada integralmente. O grant recebido pelo jogador contém a mesma quantidade aprovada.
- O Mestre pode conceder itens diretamente a um jogador. A concessão usa o outbox persistente já utilizado pelo loot, com reenvio na reconexão e sem criar pedido de aprovação.
- Ouro no baú é um recurso de moeda: ao retirar e receber aprovação, o valor é creditado à carteira do jogador. Um registro técnico oculto mantém a idempotência da entrega; ele é excluído das listas visíveis de inventário e do PDF.
- Os modelos de loja são guardados em SharedPreferences no aparelho do Mestre. Não são sincronizados para outros aparelhos ou para a nuvem.
- Nenhuma ficha existente é migrada ou tem seus itens removidos. O kit inicial só é aplicado durante a criação de um personagem novo de 1º nível; adicionar uma classe por multiclasse não concede outro kit, pois os conjuntos do Livro do Jogador são conjuntos de equipamento inicial de 1º nível.

### Kits iniciais — Livro do Jogador brasileiro
- A criação de personagens agora aplica automaticamente o equipamento e o dinheiro restante dos conjuntos iniciais das classes básicas que possuem conjunto no PDF.
- As perícias, talentos e magias que aparecem nos exemplos do livro não são preenchidos automaticamente; continuam sob controle das seções próprias da ficha.
- O equipamento inicial do Patrulheiro/Ranger aparece no próprio bloco da classe, sem o título “Conjunto Inicial”: mochila, cantil, rações de um dia, saco de dormir, pederneira e isqueiro, três tochas, aljava com 20 flechas e 2d4 PO. O código aplica exatamente esse conjunto, em vez da riqueza alternativa da Tabela 7-1.
- O conjunto de Guerreiro Humano no PDF lista Brunea e Espada Larga, sem dinheiro restante indicado; o código mantém essa ausência como está na fonte. O conjunto de Guerreiro Anão é aplicado quando a raça é Anão.
- Pesos de armas, armaduras e escudos são ajustados para personagens Pequenos; itens de aventura não têm o peso reduzido por tamanho.

### Sistema métrico
- Peso de itens, peso total, carga, limites de carga, formulários de criação/edição e PDF passam a exibir quilogramas.
- Campos de entrada de peso recebem kg e convertem para a unidade legada ao salvar.
- Os campos persistidos `weightLb` e os valores já gravados não são reescritos, preservando compatibilidade com fichas e itens antigos.
- A equivalência imperial entre parênteses na descrição de Queda Suave (60 pés) foi removida; o alcance exibido permanece em 18 m.
- A redução de deslocamento por carga foi reexpressa em incrementos métricos de 1,5 m, equivalentes ao degrau de 5 pés da regra. Os valores de movimento exibidos permanecem métricos.

### Geração de tesouro por ND
- Foi adicionada a opção de gerar o valor médio de tesouro por nível de encontro (ND 1–20), usando a Tabela 3-3 do Livro do Mestre 3.5.
- Registro histórico: na primeira implementação, esta opção criava apenas um lote de ouro equivalente ao valor médio do encontro. Esse estado foi superado pelo complemento de 2026-11-13 abaixo, que adicionou o modo aleatório com três rolagens independentes. O modo médio continua disponível como alternativa e não deve ser confundido com o modo aleatório.
- Referência externa usada para a tabela de valor médio: [D&D Tools — Using the Treasure Table, Dungeon Master's Guide 3.5, p. 55](https://dndtools.one/index.php/rules/treasure/using-the-treasure-table-214).

### Testes acrescentados
- Conversão e crédito de moedas; débito de várias unidades usando todas as denominações; bloqueio de saldo insuficiente e quantidade inválida.
- Serialização de quantidade no protocolo de pedidos de compra.
- Compra de múltiplas unidades com débito total e redução equivalente do estoque.
- Concessão direta do Mestre sem pedido de aprovação.
- Conversões de libras/pés para quilogramas/metros e testes dos kits iniciais, incluindo o fallback de riqueza do Patrulheiro.
- A CI executa `assembleDebug` após os testes e publica o artefato `dnd35-debug-apk` por 14 dias para facilitar o teste manual.
- Validação de CI e teste manual em aparelhos permanecem pendentes antes do merge.


### 2026-11-13 — Geração aleatória de tesouro (complemento)
- Mantido o modo de tesouro médio por ND como alternativa rápida.
- Adicionado o modo aleatório com três rolagens d% independentes da Tabela 3-5 do Livro do Mestre: moedas, bens e itens.
- As moedas usam as expressões de dados e denominações da tabela; ao serem retiradas, seu valor é convertido para PO equivalentes e creditado à carteira do jogador, preservando o mecanismo de entrega idempotente já existente.
- Bens geram gemas e objetos de arte com rolagens complementares de valor conforme as tabelas próprias.
- Itens mundanos geram resultados por categoria e itens mágicos recebem rolagem de raridade e categoria pela Tabela 7-1. Quando existe item compatível no catálogo local, ele é escolhido; se não existir, o baú recebe um resultado genérico identificando a categoria para o Mestre definir o objeto específico.
- Limitação registrada: a seleção de objetos mundanos dentro de algumas categorias e a escolha final de itens mágicos ainda não reproduzem todas as tabelas secundárias do Livro do Mestre. O resultado mágico genérico não deve ser tratado como um item específico até o Mestre o substituir/definir.
- Os itens gerados entram como novos registros no baú; nenhum item já existente é removido ou alterado.
- Fonte de auditoria das tabelas: Tabela 3-5 e regras de tesouro do SRD 3.5, compatíveis com o Livro do Mestre; Tabela 7-1 para a categoria de item mágico.
- Testes acrescentados para as três rolagens d%, validade do ND e preservação do modo médio. CI ainda precisa validar compilação e testes.

- Complemento técnico: a tabela de itens mundanos agora executa subrolagens próprias para alquímicos, armaduras/escudos e ferramentas/equipamentos; armas obra-primas recebem a categoria correta, ficando a escolha do modelo específico no catálogo.
- Referências consultadas para as tabelas aleatórias: [SRD 3.5 — Treasure](https://www.d20srd.org/srd/treasure.htm) e [SRD 3.5 — Magic Item Basics](https://www.d20srd.org/srd/magicItems/magicItemBasics.htm). O PDF do Livro do Mestre não está disponível na biblioteca desta sessão; por isso, a conferência foi feita nas regras abertas do SRD 3.5.


### 2026-11-13 — Correções da revisão do gerador e validação
- Corrigida a geração de armaduras e escudos: agora ocorre a rolagem adicional de tamanho da Tabela 3-8 (01–10 Pequena; 11–100 Média), com o resultado registrado no item e nas notas do baú.
- Os itens gerados que possuem correspondência no catálogo passam a usar o peso catalogado. Armaduras e escudos Pequenos recebem metade do peso do modelo Médio.
- Resultados genéricos que ainda exigem que o Mestre escolha um modelo específico não recebem peso fictício: ficam identificados com aviso de peso pendente. Antes de contabilizar a carga, o Mestre deve substituir/definir o modelo concreto.
- Corrigida a carga das moedas da carteira conforme o Livro do Jogador brasileiro: 100 moedas pesam 1 kg, independentemente da denominação. A conversão para a unidade interna legada é feita por MetricUnits.
- Testes determinísticos cobrem limites da rolagem de tamanho, peso de itens catalogados, redução de peso para tamanho Pequeno, resultados genéricos sem peso conhecido e reprodutibilidade com semente fixa. Teste de unidade cobre 100 moedas = 1 kg.
- A CI passa a ser acionada também em pull requests cujo destino seja uma branch feature/**, permitindo validar PRs empilhadas como a #369.
- Limitação mantida: as tabelas secundárias completas de seleção de cada objeto mundano/mágico não foram implementadas; categorias genéricas continuam exigindo escolha do Mestre.

- Correção adicional após CI #347: os testes do gerador acessam `Dnd35Rules.PRESET_ITEMS`, que usa `org.json` da plataforma Android. O teste estava sendo executado com o runner JUnit puro, cujos stubs Android lançam `RuntimeException`. O conjunto `Dnd35LootGeneratorTest` agora usa Robolectric (SDK 34), assim como os testes de catálogo que já carregam recursos Android. A causa foi inferida pela falha comum nos testes que inicializam o catálogo; a CI seguinte deve confirmar a correção.


### 2026-11-13 — Correção da assinatura do APK na CI
- Na execução #351, os 574 testes passaram, mas a tarefa assembleDebug falhou em validateSigningDebug porque o workflow não preparava o arquivo debug.keystore exigido pela configuração debugConfig do Gradle.
- O workflow agora gera uma chave de depuração temporária com keytool quando o arquivo não existe, antes de montar o APK. A chave é usada somente para o artefato debug da CI; não substitui nem altera a chave de publicação/release.
- A validação final depende de uma nova execução completa: testes, montagem do APK e upload do artefato.


### 2026-11-13 — Ajuste final da conversão de peso das moedas
- Corrigido o peso exibido na carteira: a tela do inventário usava a razão antiga de 50 moedas por libra, incompatível com a regra métrica da edição brasileira. Agora utiliza a mesma conversão central `MetricUnits.coinsToPounds` do cálculo de carga: 100 moedas = 1 kg.
- Corrigida a explicação exibida no formulário de moedas para informar 100 moedas = 1 kg.
- A exibição do peso total carregado, dos itens individuais, da carteira, dos campos de edição e do PDF permanece em quilogramas; `weightLb` continua sendo apenas a unidade interna de compatibilidade.
- A restrição do catálogo completo continua aplicada somente aos fluxos do Mestre. Jogadores podem ver os itens que o Mestre disponibiliza no baú/loja, mas não abrem o catálogo global para adicionar itens diretamente à ficha.
- A criação de personagem na branch da PR já serializa o kit inicial no inventário e o dinheiro inicial na carteira. O conjunto de Guerreiro Humano foi conferido no PDF: Brunea e Espada Larga; o equipamento de mochila e as 2d4 PO mostrados no exemplo da classe não fazem parte do bloco “Conjunto Inicial para Humano Guerreiro”.
- A execução #355 da CI para a revisão anterior concluiu com sucesso. Esta correção exige nova execução de testes e montagem do APK antes da validação final.

- Adicionado teste de integração com banco Room em memória: cria uma ficha de Guerreiro Humano pelo CharacterRepository, relê o registro persistido e confirma que Brunea e Espada Larga estão efetivamente no inventário, não apenas no objeto do kit.

### 2026-11-13 — Permissões do catálogo e assinatura dos APKs de CI
- Decisão confirmada: jogadores podem criar itens personalizados e importar itens por arquivo; somente o catálogo global do Livro do Jogador deve ficar restrito ao Mestre.
- Diagnóstico da atualização sobre instalação existente: a workflow Android CI desta branch não consome nenhum segredo de assinatura. O build `debug` usa `debug.keystore` na raiz do projeto, e a workflow o gera novamente em cada execução com `keytool`. Como o runner é descartável, a chave privada muda entre execuções e o Android recusa atualizar um APK assinado pela chave anterior.
- A configuração de assinatura `release` do Gradle utiliza `KEYSTORE_PATH`, `STORE_PASSWORD` e `KEY_PASSWORD`, mas a workflow atual compila `assembleDebug` com `debugConfig`, que não usa esses valores. Não há keystore versionada no repositório.
- A execução #362 concluiu testes e montagem do APK com sucesso; isso não garante compatibilidade de assinatura entre execuções.
- Correção definitiva pendente: ligar a CI a uma keystore persistente e estável, sem expor a chave privada no repositório. A API de GitHub disponível nesta auditoria não permite consultar valores nem nomes de Actions Secrets; confirmar apenas o nome do segredo já configurado, sem compartilhar seu valor.

### 2026-11-14 — Assinatura persistente nos dois workflows de APK
- O usuário confirmou que o segredo de Actions existente se chama `DND35_DEBUG_KEYSTORE_BASE64`; nenhum valor secreto ou senha foi solicitado ou compartilhado na conversa.
- Corrigido também `.github/workflows/android.yml`, que ainda gerava uma keystore temporária e publicava um segundo APK com assinatura diferente.
- Os workflows `android.yml` e `android-build.yml` agora restauram a mesma keystore a partir de `secrets.DND35_DEBUG_KEYSTORE_BASE64`, validando o alias `androiddebugkey` e a senha de armazenamento `android` antes de compilar.
- A validação final depende da execução da CI após este commit. Se o segredo estiver ausente, vazio, mal codificado ou contiver uma keystore incompatível com essas credenciais/alias, a etapa falhará explicitamente em vez de publicar um APK com assinatura nova.
- Mesmo com a chave estável configurada, um aparelho que tenha instalado a versão assinada por uma chave temporária anterior poderá exigir uma reinstalação única. Fazer backup das fichas antes de qualquer desinstalação.


### 2026-11-14 — Revisão da PR #369 sobre a main atual
- A PR estava empilhada sobre uma versão anterior da branch de criaturas. O conteúdo foi reaplicado sobre a `main` após o merge da PR #368, preservando o deck de cartas e as mudanças já integradas.
- Revisão do Livro do Jogador brasileiro, p. 52: o conjunto inicial do Patrulheiro Elfo inclui corselete de couro batido, espada longa, espada curta e arco longo, além da mochila, provisões, tochas, aljava com 20 flechas e 2d4 PO. A implementação anterior só incluía o bloco de equipamento complementar; os quatro itens do conjunto inicial foram acrescentados.
- O catálogo usa o nome `Couro Batido`, enquanto o texto do conjunto usa `Corselete de couro batido`. O kit agora resolve esse nome para o item oficial do catálogo, preservando categoria, bônus de +3 na CA, espaço de equipamento, preço e ícone. Isso também corrige o kit de Bardo Gnomo.
- Os testes dos kits agora verificam o bônus de armadura, o espaço de equipamento e o armamento completo do Patrulheiro.
- Testes e compilação do APK na branch atualizada ainda pendentes; não mesclar até CI verde e validação funcional.
