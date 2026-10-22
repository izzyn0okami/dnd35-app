# Log de trabalho — auditoria de perícias D&D 3.5

- Data: 2026-10-22
- Repositório: `izzyn0okami/dnd35-app`
- Base de trabalho: branch `main`
- Finalidade: registrar o que foi efetivamente feito nesta sessão e o estado da auditoria antes de continuar.

## Alterações em arquivos de código

**Nenhuma.** Nesta sessão não foram editados arquivos de código, testes ou configurações. Nenhuma correção foi aplicada ao aplicativo.

## Operações no repositório

- Consultado o estado de PRs abertas; a busca não retornou PRs abertas.
- Consultados arquivos da branch padrão, incluindo `README.md`, `RELATORIO_CORRECOES.md`, `Dnd35Rules.kt` e `CalculatedCharacterStats.kt`.
- Criada a branch documental `docs/log-auditoria-pericias-2026-10-22` para registrar esta sessão.
- Este log é a única alteração documental proposta nesta branch.
- No momento inicial registrado nesta seção, nenhum merge havia sido realizado; consulte a seção final para o estado posterior.

## Inspeções realizadas

### `CalculatedCharacterStats.kt`

A inspeção estática do trecho de cálculo de perícias encontrou:

- Perícias catalogadas são consideradas perícias de classe quando pertencem a qualquer uma das classes do personagem.
- Graduações são normalizadas para incrementos de 0,5 e limitadas pelo máximo calculado.
- O bônus usa `ranks.toInt()`, coerente com a decisão registrada de que meia graduação não aumenta, por si só, o modificador do teste.
- A penalidade é aplicada conforme `armorCheckPenaltyApplies` na definição da perícia.
- Natação recebe o dobro de `combinedArmorCheckPenalty`.
- Na inspeção inicial, a composição ainda não estava confirmada. A implementação corrigida posteriormente usa `minOf(totalArmorCheckPenalty, encumbrance.armorCheckPenalty)`.
- A elegibilidade para rolagem considera `trainedOnly` e a existência de pelo menos 1 graduação.

### `Dnd35Rules.kt`

- A definição de Acrobacia está com atributo-chave Destreza, penalidade de armadura ativa e sem exigência de treinamento, preservando as decisões anteriores.
- A existência desses sinalizadores foi inspecionada, mas não equivale a uma validação completa de todas as perícias contra a referência oficial.

## Conclusões que ainda NÃO foram estabelecidas

Esta sessão não confirmou se a implementação inteira está correta. Permanecem pendentes:

1. Rastrear a origem de `totalArmorCheckPenalty`, incluindo os valores de armadura e escudo e a prevenção de dupla contagem.
2. Verificar como `encumbrance.armorCheckPenalty` é calculada e confrontar o resultado com as regras oficiais de carga.
3. Conferir a aplicação da penalidade até a interface e a rolagem efetiva.
4. Localizar e revisar os testes existentes relacionados a penalidades, com atenção à penalidade dobrada de Natação.
5. Concluir a comparação das definições de perícias, treinamento obrigatório e listas de perícias de classe com as regras oficiais, respeitando o registro prévio de decisões.
6. Consultar o histórico de PRs relevante antes de propor qualquer correção.

## Decisões anteriores preservadas

- Acrobacia não exige treinamento e sofre penalidade de armadura.
- Arte da Fuga permanece separada de Acrobacia.
- Druida mantém Observar e Ouvir como perícias de classe; não reverter a correção histórica.
- Graduações fracionárias são mantidas em incrementos de 0,5, mas o modificador do teste usa a parte inteira.
- Nenhuma decisão aprovada foi alterada nesta sessão.

## Validação

- Foi feita inspeção estática do código remoto.
- **Não foram executados testes Gradle nesta sessão.**
- Nenhuma alegação de compilação, CI ou teste bem-sucedido é feita neste log.

## Próximo passo

Continuar a auditoria sem editar código: rastrear a origem e a composição das penalidades de armadura, escudo e carga; localizar testes existentes; depois acompanhar o valor calculado até a interface e a rolagem. Qualquer correção deve ser baseada em defeito confirmado, respeitar o registro de decisões e incluir teste de regressão quando aplicável.


## Atualização posterior — PRs #81 e #82

- **PR #81 integrada:** o próprio log foi incorporado à `main`; commit de merge `0f500fe73f441d81a08a9f8965426086f43a308f`.
- **PR #82 integrada:** corrigida a combinação entre penalidade de armadura/escudo e penalidade de carga. A implementação agora usa a mais desfavorável das duas, sem somá-las; Natação continua dobrando a penalidade resultante.
- Commit de merge da PR #82: `39a42a9dbb56adab2c413b039cd893ce81c79ffa`.
- A CI da PR #82 concluiu com sucesso: testes unitários e compilação do APK de depuração passaram no workflow Android build. Isso não substitui teste manual no aparelho.

## Continuação da inspeção após a PR #82

- Confirmado no código atualizado que a penalidade de armadura é extraída de `item.notes` pelo padrão `Pen -N`, tanto para armaduras quanto para escudos equipados.
- A penalidade de armadura e a do escudo são acumuladas entre si, enquanto a penalidade de carga é combinada pela mais desfavorável; essa separação é compatível com o tratamento das categorias.
- Ainda não está concluído o rastreamento end-to-end do campo de inventário até a interface de edição e rolagem. Também falta conferir se a interface instrui claramente o formato `Pen -N` e se os dados existentes usam consistentemente esse formato.
- Não foi aberta outra PR de código nesta etapa: é necessário confirmar o contrato de entrada da interface e a cobertura antes de declarar novo defeito.

## Estado atualizado

- PR #81: integrada.
- PR #82: integrada; CI aprovada.
- Próxima verificação: rastrear o campo de penalidade no formulário de armadura/escudo, o valor salvo e a rolagem de perícias; depois retomar a comparação de definições e listas com as regras oficiais.

## Auditoria complementar — formulário de inventário e listas de perícias de classe

### Fluxo da penalidade de armadura até a rolagem

- Inspecionado `app/src/main/java/com/example/dnd35/ui/dialogs/AddEditItemDialog.kt`: o campo “Propriedades / Detalhes” tem placeholder `Des Max +3, Pen -2`; ao salvar, o texto é atribuído a `CharacterInventoryItem.notes`.
- Inspecionado `CharacterModels.kt`: `CharacterInventoryItem` mantém `notes` como campo persistido do item.
- Inspecionado `CalculatedCharacterStats.kt`: para armaduras e escudos equipados, o cálculo lê `item.notes` com o padrão `Pen\s*(-?\d+)`. Itens cujo texto não contenha esse padrão resultam em penalidade zero. O catálogo de armaduras e escudos verificado usa esse formato em `properties`, e a seleção do catálogo copia `preset.properties` para `notes`.
- Inspecionado `SkillsTab.kt`: a ação de rolagem passa `item.totalBonus` ao `DiceRollDialog`.
- Conclusão: o caminho principal está ligado corretamente; não foi confirmado defeito de persistência ou de propagação para a rolagem. Melhoria de usabilidade possível: explicar no rótulo/ajuda que o formato `Pen -N` é interpretado automaticamente. Nenhuma alteração de código foi feita nesta etapa.

### Comparação inicial das listas de perícias de classe com SRD D&D 3.5

- Fonte de referência consultada: SRD 3.5 em d20srd.org para Bardo e Monge.
- **Correção necessária no Monge: não confirmada na `main` atual.** A inspeção posterior mostrou que `classSkills` já inclui Conhecimento (Arcano) e Conhecimento (Religião), e o teste `monkClassSkillsMatchCoreSkillList` já verifica ambos. O apontamento anterior sobre omissão no Monge estava desatualizado/incorreto e não deve gerar uma alteração de código.
- **Discrepância confirmada no Ladino:** a lista `classSkills` omitia Profissão, embora a lista oficial do Ladino inclua essa perícia. A correção foi preparada separadamente na PR #85, com atualização do teste `remainingCoreClassSkillsMatchExpectedLists`.
- **Bardo:** a lista da aplicação representa individualmente as especialidades de Conhecimento, coerente com a regra oficial “Knowledge (all skills, taken individually)”. Não tratar automaticamente essa representação como defeito. Continuar verificando correspondência e nomes traduzidos antes de alterar.
- As demais listas de classe ainda precisam ser comparadas sistematicamente. A inspeção parcial não permite declarar a auditoria das onze classes concluída.
- Histórico consultado: PRs #5, #6, #7, #12, #24, #74, #75, #76, #77, #81, #82 e #83. As decisões anteriores sobre Acrobacia, Druida e graduações fracionárias permanecem preservadas.

### Estado e próximo passo

- Nenhum arquivo de código ou teste foi alterado; nenhum teste local foi executado nesta etapa.
- Próximo passo: revisar a CI da PR #85 para a correção de Profissão do Ladino; depois completar a comparação das demais classes e dos sinalizadores `trainedOnly`/penalidade de armadura contra o SRD.


## Continuação após merge da PR #85 (registro histórico, atualizado)

- PR #85 integrada por squash no commit `2fe2c96e776324b84c02bc302b93c3f9ae99e60b`; sua CI passou (testes unitários, compilação Android e upload do APK de depuração).
- Nova discrepância confirmada na lista de perícias do Ladino: faltava Conhecimento (Local), listado nas regras oficiais do SRD junto de Profissão. Fonte: https://www.d20srd.org/srd/classes/rogue.htm.
- A correção foi integrada pela PR #86; ver estado atual ao final deste documento.
- A auditoria geral continua aberta para verificação sistemática das listas de classe e dos sinalizadores `trainedOnly` e penalidade de armadura. As expectativas dos testes precisam ser confrontadas com o SRD; não presumir que o teste isoladamente prova conformidade.


## Estado consolidado — atualização de 2026-10-22

Esta seção prevalece sobre qualquer status anterior conflitante no histórico acima. As seções anteriores são mantidas para preservar a trilha de auditoria.

### PRs e CI

- PR #85: integrada por squash no commit `2fe2c96e776324b84c02bc302b93c3f9ae99e60b`. A CI anterior ao merge passou.
- PR #86: integrada por squash no commit `546e6f0a36fdbb65a4bb1fc7ec039ed26a2ad360`. A mudança inclui Conhecimento (Local) na lista de perícias de classe do Ladino e atualiza o teste correspondente.
- O resultado pós-merge da PR #86 não foi confirmado por esta consulta: a busca por execuções associadas ao commit não retornou execuções. Não declarar a CI pós-merge aprovada sem verificar uma execução correspondente.
- A PR #85 e a PR #86 não devem ser recriadas para repetir essas correções.

### Cálculo vigente da penalidade de armadura e carga

A implementação atual usa `combinedArmorCheckPenalty = minOf(totalArmorCheckPenalty, encumbrance.armorCheckPenalty)`, evitando somar a penalidade de carga à penalidade de armadura/escudo. Armadura e escudo equipados continuam compondo `totalArmorCheckPenalty`; Natação aplica o dobro da penalidade combinada. A regressão `encumbrancePenaltyDoesNotStackWithArmorPenalty` verifica Escalar com -3 e Natação com -6 no cenário coberto.

### Estado da auditoria

- Mantêm-se as decisões aprovadas sobre Acrobacia, Arte da Fuga, Druida, graduações fracionárias e penalidades.
- Profissão e Conhecimento (Local) do Ladino foram corrigidas; não reabrir sem nova evidência.
- A auditoria completa das listas de perícias de classe das onze classes ainda está pendente, assim como a comparação sistemática de `trainedOnly` e `armorCheckPenaltyApplies` para todas as perícias com o SRD.
- A próxima etapa é concluir essa comparação antes de qualquer nova alteração de código. Correções só devem ser propostas com discrepância demonstrada e teste de regressão quando aplicável.
- Esta atualização é documental. Nenhum teste Gradle foi executado por meio desta operação.


## Correção de auditoria — perícias de classe do Feiticeiro

- Evidência nova: a referência oficial SRD 3.5 lista **Bluff (Blefar)** entre as perícias de classe do Sorcerer/Feiticeiro, junto de Concentração, Ofícios, Conhecimento (Arcano), Profissão e Identificar Magia (Spellcraft). Fonte: https://www.d20srd.org/srd/classes/sorcererWizard.htm.
- Revisão de histórico: a PR #21 removeu Blefar com a justificativa incorreta de que não fazia parte da lista padrão. A conclusão anterior e o teste que a perpetuava estavam errados; esta correção revoga somente essa decisão, com base na fonte oficial.
- Alteração integrada pela PR #88 em 2026-10-22: Blefar foi adicionada a `Dnd35Rules.CLASSES` para o Feiticeiro e `sorcererClassSkillsMatchCoreSkillList` exige sua presença.
- A CI do workflow Android build concluiu com sucesso para o commit da PR. Não houve teste manual no aparelho.
- Não foram alteradas outras classes, perícias, graduações ou fichas salvas.

## Verificação de requisitos de treinamento e penalidade de armadura

- Comparação do catálogo `SKILL_DEFINITIONS` com o SRD 3.5: os sinalizadores `trainedOnly` e `armorCheckPenaltyApplies` estão consistentes com as listas de referência consultadas.
- Requisitos de treinamento marcados no catálogo: Abrir Fechaduras, Adestrar Animais, todas as especialidades de Conhecimento, Decifrar Escrita, Identificar Magia, Operar Mecanismo, Prestidigitação, Profissão e Usar Instrumento Mágico.
- Exceção deliberada preservada: Acrobacia não está marcada como treinamento obrigatório por decisão anterior explicitamente aprovada para este app; não foi alterada apesar de a perícia Tumble do SRD ser treinada apenas.
- Penalidade de armadura cobre Acrobacia, Arte da Fuga, Equilíbrio, Escalar, Esconder-se, Furtividade, Natação, Prestidigitação e Saltar. Natação recebe penalidade dobrada. Os testes existentes verificam esse conjunto e o tratamento de carga/armadura.
- A lógica de `canRoll` impede testes de perícias marcadas `trainedOnly` quando o personagem tem menos de 1 graduação. Graduações fracionárias são preservadas, mas não contam como uma graduação inteira para o bônus do teste.
- Resultado desta etapa: nenhuma nova divergência comprovada; não abrir PR de código apenas por preferência de implementação. A lista de perícias de classe das classes básicas tem testes de regressão e não apresentou discrepância nova nesta revisão.
- Limitação: esta foi uma auditoria estática do catálogo, dos cálculos e dos testes existentes; não foi executada uma sessão Gradle local nem um teste manual no aparelho.


## Correção de idiomas adicionais élficos

- Divergências encontradas na auditoria do catálogo `LanguageRules`: as listas para Elfo, Alto Elfo e Elfo da Floresta omitiam Dracônico e Gnoll; a variante Halfling Robusto não tinha entrada para suas regras de idioma.
- Correção proposta na branch `fix/elf-bonus-languages`: as três variantes élficas agora incluem Dracônico, Gnoll, Gnomo, Goblin, Orc e Silvestre como opções de idiomas adicionais; Halfling Robusto recebe as mesmas regras de idiomas do Halfling.
- Foram acrescentados testes de regressão para verificar o conjunto completo de opções élficas e a equivalência linguística entre Halfling e Halfling Robusto.
- PR #90 integrada por squash em 2026-10-22, commit `cd6d4327e24ca9079d8e66993e997ab69e56b48a`; a CI do commit final da PR concluiu com sucesso. Não houve teste manual no aparelho.


## Auditoria de idiomas raciais — Drow

- Fonte consultada: Monster Manual 3.5, entrada Drow (p. 103; referências: https://dndtools.one/drow-6 e https://www.dndtools.org/races/monster-manual-v35--5/drow--6/).
- Divergência confirmada: embora a raça Drow (Elfo Negro) esteja no catálogo do app, `LanguageRules` não tinha uma entrada para ela; portanto, a ficha não podia obter as línguas automáticas e as opções raciais específicas por essa função.
- Correção proposta na branch `fix/drow-language-rules`: idiomas automáticos Comum, Élfico e Subterrâneo (representação usada no app para Undercommon); opções adicionais Abissal, Aquático, Dracônico, Linguagem de Sinais Drow, Gnomo e Goblin. Também foi incluído o alias `Drow` além do nome de catálogo `Drow (Elfo Negro)`.
- O catálogo de idiomas foi ampliado com Linguagem de Sinais Drow. Kuo-toan não faz parte da lista de idiomas adicionais da entrada Drow no Monster Manual 3.5 e foi removido desta correção; ele não deve ser acrescentado como se fosse uma opção do livro básico.
- Teste de regressão verifica o conjunto de idiomas e ambos os nomes aceitos.
- Revisão complementar corrigiu a lista para corresponder ao Monster Manual 3.5: Kuo-toan não é uma das opções adicionais da entrada Drow nessa fonte. A CI deve ser verificada novamente no último commit; nenhuma compilação local nem teste manual no aparelho foi executado.


## Estado final — auditoria de idiomas do Drow

- PR #91 integrada por squash em 2026-10-22; commit na `main`: `4bbbfa6ad0edd9c9ad539112446eae5a26bd7d60`.
- A CI do commit final da PR concluiu com sucesso antes do merge; a execução pós-merge na `main` também concluiu com sucesso.
- A implementação inclui idiomas automáticos Comum, Élfico e Subterrâneo, além das opções adicionais Abissal, Aquático, Dracônico, Linguagem de Sinais Drow, Gnomo e Goblin.
- Kuo-toan foi excluído porque não integra as opções de idioma da entrada Drow no Monster Manual 3.5. Não reabrir sem nova evidência ou uma decisão explícita de cenário específico.
- O teste de regressão cobre a lista e os aliases `Drow` e `Drow (Elfo Negro)`.
- Não houve teste manual no aparelho. A auditoria estática das opções raciais listadas em `LanguageRules` não encontrou outra discrepância confirmada nesta passagem; a continuação deve avançar para a próxima área pendente sem recriar PRs de idiomas já corrigidos.


## Continuação — PR #94 e verificação pós-merge

- PR #94 integrada por squash em 2026-10-22. Commit na `main`: `2b2fd52d5091eb542f37323e798fa80068b45491`.
- A correção do parser de gastos mantém nomes personalizados e especialidades de Ofícios, preserva a canonização de aliases conhecidos e rejeita linhas inválidas, gastos negativos/fracionários e nomes duplicados após canonização.
- O workflow Android Build pós-merge concluiu com sucesso no run [39762675175](https://github.com/izzyn0okami/dnd35-app/actions/runs/39762675175): testes unitários, compilação do APK de depuração e upload do artefato passaram.
- Após o merge, a consulta não encontrou PRs abertas.

## Próxima etapa de auditoria — sincronização de raça/tamanho e cálculo de perícias

- Revisado o cálculo de perícias em `CalculatedCharacterStats.kt`: bônus raciais são calculados a partir da raça registrada; o modificador de tamanho para Esconder-se usa o campo `CharacterEntity.size`.
- A criação inicial de personagem define `size` a partir de `RaceInfo.size` no repositório. Porém, `CharacterViewModel.updateGeneralInfo` recebe raça e tamanho como parâmetros independentes e grava ambos; ainda é necessário confirmar na interface se alterar a raça também atualiza o tamanho ou se o tamanho é uma escolha independente intencional.
- **Não classificar isto como defeito confirmado ainda.** Próximo passo: rastrear o formulário de edição geral e seus testes; verificar o comportamento ao mudar entre raças Pequenas e Médias e avaliar se preservar tamanho manual é necessário para efeitos mágicos. Só propor correção se a regra de produto ficar clara e houver caso reproduzível.
- Nenhum código foi alterado nesta etapa de inspeção. A CI pós-merge da PR #94 está aprovada; não houve teste manual no aparelho.


### Resultado da verificação de raça e tamanho

- A inspeção de `EditGeneralInfoDialog.kt` confirmou que tamanho é um campo editável separado, sem vínculo automático com o seletor de raça. A criação inicial, por outro lado, usa o tamanho padrão definido em `RaceInfo`.
- Como o app permite edição manual de tamanho e o tamanho pode ser alterado por efeitos de jogo, a independência dos campos não é, por si só, defeito. Não fazer sincronização automática ao trocar raça sem requisito explícito.
- A hipótese de defeito foi encerrada sem alteração de código. A auditoria prossegue para outras validações de histórico e cálculo de perícias.


## Continuação da auditoria — histórico de gastos e graduações

- Verificada a execução pós-merge da PR #95: workflow Android Build [39763499111](https://github.com/izzyn0okami/dnd35-app/actions/runs/39763499111) concluiu com sucesso, incluindo testes unitários, compilação do APK de depuração e upload do artefato.
- Inspeção estática de `SkillsTab.kt`, `CharacterViewModel.kt` e `CalculatedCharacterStats.kt`: o histórico de pontos registra orçamento e gastos por nível/classe; a ficha calcula as graduações atuais e seus limites com base no nível total e na classificação atual da perícia.
- A classificação atual não deve ser usada para reescrever retroativamente os gastos por nível: multiclasse pode mudar se uma perícia é de classe para o personagem sem alterar os gastos legítimos feitos em níveis anteriores.
- O histórico permite registrar divergência entre orçamento e gastos e exibe o excedente ou saldo. Não foi encontrada evidência suficiente para bloquear esse registro; mantê-lo como instrumento de auditoria, não como validação automática de legalidade.
- Nesta inspeção não foi confirmada uma divergência nova que justifique alteração de código. Nenhum arquivo de código foi modificado e não houve execução local de Gradle nem teste manual no aparelho.
- Próximo passo: revisar testes e o fluxo de atualização de classe/nível para confirmar que o limite de graduações e os botões de edição são recalculados sem alterar o histórico salvo; só abrir PR de código se surgir caso reproduzível.


## Correção de auditoria — especialidades de Ofícios

- Divergência confirmada na inspeção de 2026-10-22: nomes personalizados como `Ofícios (Armadilhas)` não coincidiam literalmente com a entrada genérica `Ofícios` nas listas de perícias de classe. Além disso, especialidades novas sem definição individual herdavam por padrão o atributo FOR, embora Ofícios use Inteligência.
- A correção proposta na branch `fix/craft-specialty-class-skill` centraliza a correspondência de especialidades com a perícia-base e determina INT para especialidades de Ofícios, tanto para novas entradas quanto para dados já salvos.
- A PR #26 anterior foi consultada; esta correção é limitada à classificação de classe e atributo-chave, sem reabrir a implementação mais ampla de especialidades independentes.
- Testes de regressão adicionados para correspondência com Ofícios, rejeição quando a entrada de classe não corresponde e atributo-chave INT, incluindo variação de maiúsculas/minúsculas.
- Status: alteração preparada em branch; aguardar revisão e CI antes de integrar. Não houve teste manual no aparelho.


## Correção confirmada — controles de graduações fracionárias

- Na auditoria do fluxo de progressão de classe, foi reproduzido um problema de interface: ao passar de perícia entre-classe para perícia de classe por multiclasse, os botões de graduação mudavam imediatamente para passos de 1,0. Personagens com 0,5 ou 1,5 graduação não conseguiam alcançar o valor adjacente válido usando esses botões.
- A correção mantém passo de 0,5 enquanto o valor atual tiver fração; quando a graduação é inteira e a perícia é de classe, o passo volta a 1,0. Perícias entre-classe continuam usando 0,5.
- A mudança afeta apenas a edição atual das graduações; não reescreve o histórico de pontos gastos por nível nem altera os limites calculados.
- Foi incluída uma função auxiliar testável; os testes de regressão devem cobrir graduação fracionária em perícia de classe, graduação inteira em perícia de classe e perícia entre-classe.
- PR #98 integrada por squash em 2026-10-22; commit na `main`: `ec490e3e91c05a37f7f6dec522f964f7e8e5c891`. A CI da PR concluiu com sucesso; a verificação pós-merge está em andamento no momento desta atualização. Não houve teste manual no aparelho.


## Correção em andamento — penalidade de XP para raças com classe favorecida "Qualquer"

- A inspeção de `ManageMulticlassDialog.kt` encontrou um defeito confirmado: a interface escolhia a classe de maior nível para exibição, mas também a usava para calcular penalidade de XP. Isso fazia Humano e Meio-Elfo, cuja classe favorecida é "Qualquer", poderem receber penalidade por multiclasse.
- A regra foi isolada em `MulticlassRules.kt`: quando qualquer classe é favorecida, a penalidade é sempre zero; nas raças com classe favorecida específica, a regra compara somente as classes não favorecidas e aplica 20% por classe pelo menos dois níveis abaixo da mais alta entre elas.
- A interface passa a identificar todas as classes como favorecidas quando a raça usa "Qualquer", e a explicação informa que não há penalidade de XP por multiclasse.
- Testes de regressão adicionados para raça com qualquer classe favorecida, raça com classe favorecida específica e caso com apenas uma classe não favorecida.
- Status: proposta na branch `fix/any-favored-class-xp-penalty`; aguardar CI e revisão antes do merge. Não houve teste manual no aparelho.


## Revisão pelo Livro do Jogador — penalidade de XP para Humano e Meio-Elfo

- Correção da análise anterior: a classe favorecida "Qualquer" não significa imunidade total à penalidade de XP.
- O Livro do Jogador 3.5, capítulo 2 ("Classe Favorecida") e capítulo 3, p. 60 ("Experiência para Personagens Multiclasse"), estabelece que, para Humanos e Meio-elfos, a classe de nível mais alto não conta no cálculo. As classes restantes ainda são comparadas entre si; cada classe que estiver dois ou mais níveis abaixo da mais alta entre elas gera penalidade de 20%.
- Exemplo de regressão: Guerreiro 8 / Mago 5 / Ladino 2, para Humano ou Meio-Elfo: Guerreiro é excluído; Ladino fica três níveis abaixo de Mago, resultando em penalidade de 20%.
- A PR #99 foi revisada antes do merge e permanece aberta; a implementação inicial que removia toda penalidade foi substituída pela regra correta. Testes cobrem o exemplo com penalidade, classes restantes equilibradas, raça com classe favorecida específica e caso com apenas uma classe não favorecida.
- Fontes de conferência: edição brasileira do Livro do Jogador 3.5, capítulo 2 e capítulo 3, p. 60. Referência auxiliar em inglês: Player's Handbook 3.5, p. 60.
- Status: aguardar CI da PR #99 após a correção; não houve teste manual no aparelho.


## Estado consolidado após PR #99 — 2026-10-22

Esta seção prevalece sobre os status de andamento anteriores, que foram preservados como histórico.

- PR #98: integrada; a CI da PR passou e a CI pós-merge também foi verificada como concluída com sucesso.
- PR #99: integrada por squash no commit `68d3be941d1b87f205821797eb24bbcfb963b9ab`. A CI da PR passou e a CI pós-merge concluiu com sucesso no run [39863991497](https://github.com/izzyn0okami/dnd35-app/actions/runs/39863991497).
- Regra corrigida conforme o *Livro do Jogador* 3.5: para Humanos e Meio-elfos, uma única classe — a de nível mais alto — é desconsiderada no cálculo de penalidade de XP; as classes restantes ainda podem gerar penalidade de 20% cada quando estiverem pelo menos dois níveis abaixo da mais alta entre elas.
- A regra também está aplicada ao cálculo geral de estatísticas em `CalculatedCharacterStats.kt`: a classe favorecida efetiva para Humanos/Meio-elfos é a classe de nível mais alto, e a função de penalidade compara as demais classes. Não foi encontrada divergência entre esse cálculo e a regra corrigida da interface.
- A inspeção do fluxo de progressão confirmou que `updateClasses` recalcula o nível total e persiste as entradas de classe, sem alterar `skillPointHistoryJson`. `updateSkill` recalcula a graduação máxima usando os níveis atuais, normaliza graduações em incrementos de 0,5 sem arredondar para cima e mantém o histórico de gastos separado.
- Não foi confirmada nesta passagem uma regressão nova que justifique PR de código. A verificação foi estática, baseada no código remoto; não foi executado Gradle local nem teste manual no aparelho.
- Não há PRs abertas no repositório no momento desta atualização.

## Próxima etapa

Continuar a auditoria de regras sem repetir os assuntos resolvidos. Próximo foco: revisar as progressões de BBA e testes de resistência das classes básicas, inclusive níveis de transição, comparando as funções de cálculo e os testes existentes com as tabelas oficiais do *Livro do Jogador* 3.5. Só abrir correção quando uma divergência concreta estiver demonstrada e incluir teste de regressão.


## Auditoria de características de classe — correções de progressão identificadas em 2026-10-22

- A revisão das listas `ClassInfo.features` identificou omissões/descrição incorreta confirmadas nas progressões do Livro do Jogador 3.5: os aumentos de Sentir Armadilhas do Bárbaro nos níveis 6, 9, 12, 15 e 18; Sugestão em Massa do Bardo no nível 18; os talentos bônus de Monge nos níveis 2 e 6; Golpe Ki (Leal) no nível 10; Passo Abundante no nível 12; Golpe Ki (Adamantino) no nível 16; e a descrição de Rajada Maior, que concede um segundo ataque adicional no bônus base de ataque máximo, não apenas remove a penalidade.
- As progressões de referência consultadas indicam Sentir Armadilhas +1 no 3º nível e aumento de +1 a cada três níveis depois disso; Sugestão em Massa consta no 18º nível do Bardo. Referências auxiliares consultadas: Player’s Handbook 3.5, tabela do Bárbaro e tabela do Bardo (https://xbladerunner.com/wp-content/uploads/sites/13/2025/12/DD-3rd-Edition-3.5.pdf; https://anyflip.com/efpcn/inzd/basic). A conferência direta de todas as onze tabelas na edição brasileira ainda está pendente; não declarar a auditoria completa. Referência auxiliar confirma também que o Bárbaro começa analfabeto e que Rajada Maior acrescenta um segundo ataque no bônus base máximo (https://www.d20srd.org/srd/classes/barbarian.htm; https://www.d20srd.org/srd/classes/monk.htm).
- Correção proposta na branch `fix/class-feature-progression-audit`: adicionadas as cinco entradas de Sentir Armadilhas, a entrada de Sugestão em Massa, os talentos bônus de Monge dos níveis 2 e 6, Golpe Ki (Leal), Passo Abundante e Golpe Ki (Adamantino), e corrigida a descrição de Rajada Maior. A descrição de Movimento Rápido agora explicita que o bônus aumenta a cada três níveis. Também foi registrada a característica Analfabetismo do Bárbaro no nível 1. Testes de regressão verificam os níveis e os nomes esperados, incluindo a descrição de Rajada Maior.
- Status: aguardar CI e revisão da PR antes de integrar. A mudança foi feita por API do GitHub; nenhum Gradle local nem teste manual no aparelho foi executado por esta operação.
- Próximo passo: continuar a comparação classe por classe das características listadas com as tabelas do Livro do Jogador brasileiro, registrando apenas divergências concretas e evitando repetir correções já integradas.

- Revisão adicional da própria PR: removida uma entrada redundante/incorreta de “Talento Bônus de Monge (1º nível)”, pois Ataque Desarmado Aprimorado já é o talento bônus de 1º nível; adicionados os aumentos discretos de Movimento Rápido do Monge nos níveis 6, 9, 12, 15 e 18, com teste de regressão. A CI deve validar a revisão mais recente antes do merge.

- Conferência adicional da Tabela 3-14 do *Livro do Jogador* brasileiro (cópia digital consultada em https://anyflip.com/hgohy/pjla/basic) identificou terminologia e omissões no Monge: o nome da habilidade é Integridade Corporal (7º), Salto das Nuvens (7º), Ataque Chi (Ordem) (10º), Ataque Chi (+2) (13º), Passo Etéreo (12º) e Ataque Chi (Adamante) (16º). A PR foi ajustada para refletir esses nomes/níveis e os testes correspondentes. O Bárbaro também foi ajustado para indicar que Movimento Rápido não funciona com armadura média/pesada nem carga média/pesada.

- Ajuste fino da conferência da Tabela 3-14: o Monge recebe também um talento adicional no 1º nível (Agarrar Aprimorado ou Ataque Atordoante), além de Ataque Desarmado Aprimorado. A entrada de escolha do 1º nível foi mantida separada e recebeu teste específico; não é uma duplicação do talento Ataque Desarmado Aprimorado.

- Verificação adicional da seção do Bardo no Livro do Jogador brasileiro (Tabela 3-4 e descrição das habilidades): corrigidos os nomes oficiais “Música de Proteção” e “Melodia da Libertação”; explicitada a progressão de alvos de Fascinar nos níveis 1, 4, 7, 10, 13, 16 e 19; registradas as progressões de alvos adicionais de Inspirar Grandeza (níveis 12, 15 e 18) e Inspirar Heroísmo (nível 18). Testes de regressão adicionados. Referência direta: https://anyflip.com/hgohy/pjla/basic, linhas da Tabela 3-4 e descrições das habilidades do Bardo. A PR deve passar pela CI atualizada antes do merge.

- Nova divergência confirmada na progressão do Ladino: a lista de características continha apenas Sentir Armadilhas +1 no 3º nível, mas a habilidade aumenta +1 a cada três níveis, até +6 no 18º. Adicionadas as entradas dos níveis 6, 9, 12, 15 e 18 e um teste de regressão. A correção está na mesma PR de auditoria e precisa passar pela CI antes do merge.

- Conferência terminológica adicional da Tabela 3-14 do *Livro do Jogador* brasileiro: padronizados os nomes “Queda Suave”, “Ataque Chi (Mágico)”, “Mão Vibrante”, “Idiomas do Sol e da Lua” e “Auto-Perfeição”, que estavam divergentes da tabela brasileira. As asserções existentes foram atualizadas para acompanhar a terminologia oficial. Fonte: https://anyflip.com/hgohy/pjla/basic (Tabela 3-14: O Monge).

- Correção de fonte após cotejo direto com a Tabela 3-14 da edição brasileira 3.5: “Salto das Nuvens” não aparece na progressão do Monge da tabela consultada de 3.5 e foi removido da lista e do teste. A referência secundária que sugeria essa habilidade aparenta misturar outra versão/edição; prevalece a tabela brasileira 3.5. Este registro substitui a anotação anterior que listava Salto das Nuvens como confirmado. Fonte: https://anyflip.com/hgohy/pjla/basic (Tabela 3-14).

- Revisão da descrição dos talentos bônus do Monge: nível 2 oferece Reflexos em Combate ou Desviar Objetos; nível 6 oferece Desarme Aprimorado ou Imobilização Aprimorada. As escolhas não exigem os pré-requisitos normais. A descrição anterior era genérica e afirmava incorretamente que era preciso cumprir pré-requisitos; corrigida e coberta por teste. Referência: descrição de “Talento Adicional” após a Tabela 3-14 do Livro do Jogador brasileiro.

- A descrição de Auto-Perfeição do Monge foi completada: além de ser tratado como extra-planar para magias e efeitos, o personagem recebe Redução de Dano 10/mágica. Foi adicionado teste de regressão. Fonte direta: Livro do Jogador brasileiro, seção Auto-Perfeição, https://anyflip.com/hgohy/pjla/basic.

- Ajustadas as descrições das condições de Evasão, Bônus na CA e Movimento Rápido do Monge: a velocidade extra exige ausência de qualquer armadura/escudo e carga leve; o bônus de CA exige também carga leve; Evasão funciona sem armadura ou com armadura leve. A lógica de cálculo já exigia carga leve e ausência de armadura/escudo para o deslocamento, então a alteração alinha a descrição exibida às regras e ao cálculo.
