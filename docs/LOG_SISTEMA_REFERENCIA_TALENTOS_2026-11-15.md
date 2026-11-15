# Log — sistema de referência e glossário de talentos

Data: 2026-11-15

## Escopo implementado
- Criado índice de pesquisa textual do catálogo de talentos do Livro do Jogador.
- Pesquisa por nome, pré-requisitos, resumo e benefício.
- Busca sem distinção entre maiúsculas/minúsculas e acentuação.
- Resultados priorizam correspondência exata e início do nome.
- Criado diálogo de glossário com lista de resultados e visualização detalhada.
- A ficha permite abrir o glossário pelo botão de consulta e tocar no nome de um talento registrado para abrir a entrada correspondente.
- No catálogo de inclusão, tocar no nome abre a referência detalhada; tocar no cartão continua adicionando o talento, preservando o fluxo atual.

## Referência bibliográfica
- Livro do Jogador brasileiro, Capítulo 5 — Talentos, páginas impressas 73–89.
- O catálogo consultado é `app/src/main/resources/dnd35/feats.json`, já usado pelo app como catálogo do Livro do Jogador.
- A referência é identificada no glossário como Livro do Jogador, Capítulo 5. O conteúdo apresentado é o cadastro atual do app, não uma reprodução integral do texto editorial.

## Limites preservados
- A avaliação de pré-requisitos continua informativa: não bloqueia inclusão, edição ou manutenção de talentos na ficha.
- Talentos personalizados continuam editáveis e não são inseridos automaticamente no catálogo oficial.
- Nenhum cálculo, regra mecânica, dado persistido ou formato de ficha foi alterado.
- A pesquisa é textual e não afirma validar automaticamente escolhas que a ficha não registra (por exemplo, arma ou escola específica).

## Validação pendente
- Executar testes unitários, CI e compilação Android.
- A auditoria de fidelidade de cada benefício e pré-requisito do catálogo continua sendo uma etapa separada; esta alteração implementa o sistema de consulta, não certifica individualmente todos os verbetes.


## Separação entre resumo da ficha e referência completa (15/11/2026)

- O primeiro diálogo repetia os campos `summary` e `benefit` do catálogo de ficha. Isso não atendia ao objetivo de oferecer uma consulta realmente explicativa.
- Criados catálogos independentes: `feat_references.json` e `class_feature_references.json`. Eles não substituem nem alteram os textos curtos exibidos na ficha.
- Cada referência detalhada tem campos próprios para funcionamento, regra normal, casos especiais, ativação, duração, restrições, progressão e fonte/página, conforme o tipo de entrada.
- O diálogo de talentos agora apresenta pré-requisitos e, quando auditado, o texto expandido; não repete o resumo/benefício da ficha. Entradas ainda não auditadas são identificadas como pendentes, em vez de receberem um texto curto disfarçado de explicação completa.
- O glossário de habilidades de classe segue a mesma regra: a ficha mantém a descrição curta e o glossário exibe campos detalhados separados; verbetes pendentes são identificados.
- Referências conferidas no PDF brasileiro do Livro do Jogador:
  - Talentos: Ataque Poderoso (p. 78), Foco em Arma (p. 81), Esquiva (p. 81), Iniciativa Aprimorada (p. 82), Combater com Duas Armas (p. 78), Mobilidade (p. 83), Tiro Preciso (p. 85), Vitalidade (p. 86) e Ataque em Movimento (p. 75).
  - Bárbaro: Fúria (pp. 21–22), Esquiva Sobrenatural, Sentir Armadilhas, Esquiva Sobrenatural Aprimorada e Redução de Dano (p. 23).
- Cobertura atual deste incremento: 9 de 110 talentos e 5 referências de habilidades do Bárbaro. As demais entradas continuam disponíveis na busca, mas aparecem explicitamente como pendentes de auditoria detalhada.
- A falha inicial dos testes do glossário foi causada por o novo teste usar JUnit puro ao acessar o catálogo JSON que depende do runtime Android/Robolectric. O teste agora usa `RobolectricTestRunner`, como o teste existente de carregamento dos catálogos. Não foi alterado o carregador de produção.
- Incluídos testes para confirmar que Ataque Poderoso tem referência mais extensa que o resumo da ficha e que Fúria do Bárbaro aponta para as regras detalhadas e páginas corretas.
- Não foram alterados cálculos, progressões, pré-requisitos automáticos, persistência ou formato de ficha.
- CI e compilação Android devem ser executados novamente; não mesclar enquanto ambos não passarem.


## Ampliação da auditoria detalhada (15/11/2026)

- A cobertura de talentos foi ampliada de 9 para **25 referências detalhadas de 110 entradas** do catálogo. As 85 restantes continuam marcadas na interface como pendentes, sem reutilizar o texto curto da ficha.
- Novos talentos auditados: Agarrar Aprimorado, Desviar Objetos, Trespassar, Fintar Aprimorado, Magia Natural, Acuidade com Arma, Especialização em Combate, Especialização em Arma, Tiro Certeiro, Tiro Longo, Combater com Duas Armas Maior, Tiro Rápido, Desarme Aprimorado, Lutar às Cegas, Ataque Giratório e Reflexos de Combate, além dos nove já registrados acima.
- Corrigido o nome do talento no catálogo para **Reflexos de Combate**, conforme a nomenclatura do Livro do Jogador brasileiro. A mudança afeta apenas o nome apresentado no catálogo de seleção; não altera cálculos ou bônus de fichas existentes.
- A cobertura de habilidades de classe foi ampliada para **44 referências detalhadas**, abrangendo as 11 classes básicas: Bárbaro, Bardo, Clérigo, Druida, Feiticeiro, Guerreiro, Ladino, Mago, Monge, Paladino e Ranger.
- Foram acrescentadas referências de Música de Bardo, Forma Selvagem, Inimigo Predileto, Estilo de Combate, companheiro animal do ranger, habilidades centrais do paladino, monge, ladino, clérigo, mago e feiticeiro, além de talentos bônus do guerreiro.
- Corrigido o rótulo da habilidade do ranger para **Maestria do Estilo de Combate**, conforme o texto do livro.
- A busca de habilidades de classe agora prefere a referência de nome mais específico quando há nomes sobrepostos (por exemplo, Esquiva Sobrenatural e Esquiva Sobrenatural Aprimorada), evitando exibir o verbete errado.
- Validação estrutural dos JSONs: 25 referências de talentos correspondem a nomes existentes no catálogo; nenhuma duplicidade ou campo obrigatório ausente foi encontrada. As 44 referências de classe possuem chaves únicas e campos obrigatórios preenchidos.
- Foram adicionados testes de regressão para as referências de bárbaro, bardo, druida, ranger, paladino, monge, ladino, clérigo, feiticeiro, mago e guerreiro, além da separação entre resumo e texto detalhado dos talentos.
- Ainda falta executar e aprovar CI e compilação Android no commit final. O PR permanece aberto e não deve ser mesclado enquanto os dois fluxos não passarem.


## Revisão de regras de combate e pesquisa textual (15/11/2026)

- Cobertura atualizada para **26 de 110 talentos**. Acrescentados Combater com Duas Armas e Combater com Duas Armas Aprimorado; corrigido o texto de Reflexos de Combate para explicitar que o personagem mantém o ataque de oportunidade normal e recebe ataques adicionais em quantidade igual ao modificador de Destreza.
- A busca agora também consulta os campos detalhados dos verbetes (funcionamento, regra normal, especial, ativação, duração, restrições e progressão), não apenas os nomes e resumos da ficha.
- Testes adicionais verificam buscas por termos que aparecem somente no texto expandido e as regras de ataques de oportunidade e combate com duas armas.


## Ampliação de talentos gerais e perícias (15/11/2026)

- Cobertura atualizada para **45 de 110 talentos**.
- Foram acrescentadas referências para talentos de bônus em perícias e testes de resistência, além de Foco em Perícia, Prontidão, Corrida e Tolerância.
- As referências simples utilizam a descrição mecânica da Tabela 5-1; quando há regra normal ou exceção de classe (como o talento Tolerância concedido automaticamente ao ranger), ela foi registrada separadamente.
- Testes adicionados para Foco em Perícia e Tolerância.


## Auditoria de talentos metamágicos e conjuração (15/11/2026)

- Cobertura atualizada para **57 de 110 talentos**.
- Acrescentadas referências detalhadas para Magia em Combate, Foco em Magia, Foco em Magia Maior, Magia Penetrante, Magia Penetrante Maior, Magia Sem Gestos, Magia Silenciosa, Maximizar Magia, Potencializar Magia, Estender Magia, Ampliar Magia e Acelerar Magia.
- As entradas registram custo de nível de magia, exceções, escolas afetadas, condições de uso e efeitos que não são modificados, conforme o Livro do Jogador brasileiro.
- Testes adicionados para custos metamágicos e exceções específicas de conjuradores.


## Ajustes finais de validação (15/11/2026)

- Atualizado o teste de catálogo para esperar o nome oficial “Reflexos de Combate”, em vez do rótulo anterior.
- Corrigido o termo de busca do teste de Ataque Poderoso para corresponder à forma plural “armas leves” presente no texto de referência.
- A cobertura permanece em 57 talentos e 44 habilidades de classe; nenhum dado de ficha ou regra de cálculo foi modificado por esses ajustes.
- Aguardar a conclusão dos fluxos CI e compilação Android no commit final antes de mesclar.


## Ampliação de talentos de combate e usos especiais (15/11/2026)

- A cobertura passou de 57 para **69 de 110 talentos**. Foram acrescentadas 12 referências detalhadas independentes do resumo da ficha.
- Novos verbetes: Foco em Arma Maior, Expulsão Adicional, Expulsão Aprimorada, Imobilização Aprimorada, Investida Implacável, Investida Montada, Saque Rápido, Sucesso Decisivo Aprimorado, Tiro em Movimento, Tiro Múltiplo, Tiro Preciso Aprimorado e Trespassar Maior.
- As referências registram pré-requisitos, ações, limites de uso, progressão por BBA, exceções de classe e interações relevantes, incluindo os benefícios especiais concedidos a guerreiros, monges e rangers.
- Fonte primária conferida: Livro do Jogador brasileiro, Capítulo 5 — Talentos, páginas impressas 81–85 (páginas do PDF correspondentes verificadas no arquivo da biblioteca).
- Foram adicionados testes de regressão para os detalhes de Foco em Arma Maior, Expulsão Adicional, Imobilização Aprimorada, Saque Rápido, Tiro Múltiplo, Tiro Preciso Aprimorado e Trespassar Maior.
- A cobertura das 44 referências de habilidades de classe permanece inalterada. Não foram modificados cálculos, pré-requisitos automáticos, progressões ou dados persistidos.
- Próximo trabalho: continuar a auditoria dos 41 talentos ainda sem referência detalhada, começando pelos talentos de combate e montaria, sem repetir os 69 verbetes já auditados.


## Correção da validação do PR #385 (15/11/2026)

- A primeira execução de CI e compilação falhou em um único teste: `additionalCombatAndItemCreationFeatReferencesExplainSpecificExceptions`, na asserção de Saque Rápido.
- A referência do talento registra corretamente o pré-requisito por extenso como “bônus base de ataque +1”; o teste procurava a abreviação “BBA +1”, que não existe no campo especial desse verbete. Portanto, a falha era uma expectativa textual incorreta no teste, não um erro na regra cadastrada.
- Ajustada a asserção para verificar a expressão efetivamente cadastrada. Nenhuma regra, texto de referência, cálculo ou dado persistido foi alterado.
- Aguardar nova execução de Android CI e Android build no novo commit antes de mesclar.


## Auditoria de manobras de combate e combate montado (15/11/2026)

- Cobertura ampliada de 69 para **78 de 110 talentos**; permanecem 32 verbetes sem referência detalhada.
- Acrescentadas referências para Apanhar Objetos, Ataque Desarmado Aprimorado, Ataque Atordoante, Arquearia Montada, Combate Montado, Atropelar Aprimorado, Encontrão Aprimorado, Pisotear e Separar Aprimorado.
- As descrições foram conferidas no Livro do Jogador brasileiro, Capítulo 5 — Talentos, páginas impressas 75, 78, 80, 83 e 84.
- Registradas condições de uso, ações, testes, CD, frequência, imunidades, mãos livres, exceções de monge e talentos adicionais de guerreiro.
- Corrigido o benefício curto de Investida Montada em `feats.json`: ele permite continuar o deslocamento após o ataque da investida montada; o dano dobrado/triplicado pertence a Investida Implacável. O texto detalhado de Investida Montada já estava correto.
- Explicitados os pré-requisitos de Força 13 e Ataque Poderoso em Atropelar Aprimorado, Encontrão Aprimorado e Separar Aprimorado.
- Testes de regressão acrescentados para os nove verbetes e para as exceções de combate montado e manobras.
- Nenhum cálculo, regra automatizada de elegibilidade, persistência ou formato de ficha foi alterado. O catálogo de resumo foi corrigido apenas onde descrevia incorretamente Investida Montada.
- Correção de nomenclatura após conferência cruzada: `Derrubar Aprimorado` não é um talento separado na edição brasileira. O benefício corresponde a `Imobilização Aprimorada` (Improved Trip), já presente no catálogo e no glossário detalhado. Removida a entrada duplicada `Derrubar Aprimorado` do catálogo de seleção e corrigido o resumo de `Imobilização Aprimorada`, incluindo Inteligência 13, Especialização em Combate, bônus de +4, ausência de ataque de oportunidade ao tentar a manobra desarmado e ataque adicional após derrubar. Registros já salvos como `CharacterFeat` permanecem preservados, pois são dados próprios da ficha e não são substituídos pelo catálogo.
- Próxima etapa: continuar os 32 verbetes pendentes, priorizando os talentos de combate restantes e criação de itens mágicos.


## Auditoria de criação de itens mágicos e talentos de combate (15/11/2026)

- Cobertura ampliada de **78 para 88 de 110 talentos**; 22 continuam sem referência detalhada.
- Conferidos diretamente no Livro do Jogador brasileiro, Capítulo 5, os oito talentos de criação de itens: Criar Armaduras e Armas Mágicas (p. 79), Criar Bastão (p. 79), Criar Cajado (p. 79), Criar Item Maravilhoso (p. 79), Criar Varinha (p. 79), Escrever Pergaminho (p. 80), Forjar Anel (p. 81) e Preparar Poção (p. 83).
- Registrados para cada criação: nível mínimo de conjurador, tipo de item, fórmula de preço quando apresentada, tempo de trabalho, custo de XP, matéria-prima, cargas quando aplicável, custos extras de componentes e possibilidade de reparo quando o livro a concede.
- Acrescentadas referências para Bloqueio Ambidestro (p. 78) e Rapidez de Recarga (p. 83), incluindo o bônus defensivo em combate com duas armas, a exigência de Destreza 15, ações de recarga e ataques de oportunidade.
- Corrigido o pré-requisito curto de Bloqueio Ambidestro no catálogo: o livro exige Destreza 15 e Combater com Duas Armas; o registro anterior omitia Destreza 15.
- **Derrubar Aprimorado permanece sem referência detalhada:** não aparece na Tabela 5-1 nem nas descrições do Capítulo 5 consultadas nas páginas impressas 76–86. A entrada do catálogo não será tratada como talento oficial do Livro do Jogador sem confirmação textual; mantida como pendência de reconciliação, sem apagar dados existentes.
- Os oito talentos de criação remetem explicitamente ao Livro do Mestre para regras específicas dos itens. Este glossário registra somente os parâmetros efetivamente apresentados no Livro do Jogador; não completa requisitos ou fórmulas ausentes com material externo.
- Testes de regressão adicionados para fórmulas/custos de criação, cargas, exceções de XP, pré-requisitos de Bloqueio Ambidestro e ações de Rapidez de Recarga.
- Nenhum cálculo automático, elegibilidade, progressão, persistência ou formato de ficha foi alterado.


## Correção de nomenclatura: Imobilização Aprimorada (15/11/2026)

- A edição brasileira do Livro do Jogador usa o nome `Imobilização Aprimorada` para o talento que o SRD inglês identifica como `Improved Trip`. A descrição oficial confirma a manobra, o bônus de +4 nos testes resistidos de Força e o ataque adicional após derrubar o adversário (p. 82).
- O SRD confirma que `Improved Trip` e `Improved Bull Rush` são talentos diferentes; o segundo corresponde a `Encontrão Aprimorado`. Portanto, `Derrubar Aprimorado` era uma entrada duplicada/alias não oficial, não um talento separado.
- Removida a entrada duplicada do catálogo de seleção; corrigidos os pré-requisitos e o benefício curto de `Imobilização Aprimorada` para corresponder ao verbete detalhado já auditado.
- Não foi realizada migração destrutiva: talentos previamente registrados nas fichas permanecem armazenados como `CharacterFeat` e não dependem da presença do nome no catálogo.
- Teste de regressão atualizado para garantir que o catálogo contenha o nome brasileiro e não ofereça a duplicata.


## Verificação da CI e ajuste do teste de contagem (15/11/2026)

- A segunda tentativa do fluxo Android CI para o commit anterior concluiu com sucesso. Os dois fluxos disparados após a correção do nome falharam no mesmo teste de snapshot de contagem: `CatalogJsonLoadingTest.catalogSizesMatchThePlayersHandbookSnapshotCapturedOnMigration`, que exigia pelo menos 110 talentos.
- A falha foi causada pelo piso histórico do teste, não por erro de compilação nem por campo ausente: o catálogo passou legitimamente de 110 para 109 entradas ao remover o alias duplicado `Derrubar Aprimorado`. A entrada canônica `Imobilização Aprimorada` permanece no catálogo.
- Atualizado o piso para 109, com comentário explicando a redução intencional. Os testes de nomes, campos obrigatórios e ausência de duplicatas permanecem ativos.
- Nenhuma CI foi considerada aprovada para o commit mais recente ainda; aguardar a nova execução antes do merge.
