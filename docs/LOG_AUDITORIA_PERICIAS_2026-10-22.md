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

- Correção funcional do cálculo de deslocamento do Bárbaro: a implementação anterior concedia Movimento Rápido com armadura média e carga média, contrariando a regra da classe. Agora o bônus de +3 m só é aplicado com armadura leve ou sem armadura e carga leve; testes antigos que esperavam o comportamento incorreto foram corrigidos para 6 m com Gibão de Peles/carga média. Isso corrige o cálculo, não apenas o texto da habilidade.

- Completada a descrição de Passo Etéreo: além do uso diário semelhante a Porta Dimensional, o nível de conjurador é metade do nível de monge, arredondado para baixo. Adicionado teste de regressão com base na descrição da habilidade no Livro do Jogador brasileiro.

- Nova divergência confirmada no Paladino: Remover Doença estava deslocado um nível de progressão e terminava em 5 usos semanais. Pela Tabela 3-16 brasileira, começa no nível 3 com 1 uso por semana e aumenta a cada três níveis, chegando a 6 usos semanais no nível 18. Corrigidos os níveis e adicionada cobertura de regressão. Fonte: Livro do Jogador brasileiro, Tabela 3-16, https://anyflip.com/hgohy/pjla/basic.

- Correção de auditoria da Tabela 3-14: a anotação anterior interpretou incorretamente o “+2” exibido na coluna de bônus de CA sem armadura como uma característica “Ataque Chi (+2)”. A progressão de Ataque Chi da classe base é Mágico no 4º nível, Ordeiro no 10º e Adamante no 16º; o bônus +2 pertence à coluna de CA sem armadura e não deve ser listado como Ataque Chi. Removida a entrada incorreta e adicionado teste de regressão que impede seu retorno. Fonte de conferência: Livro do Jogador brasileiro, Tabela 3-14, https://anyflip.com/hgohy/pjla/basic; cotejo complementar com a tabela e a descrição da habilidade Ki Strike no Player’s Handbook 3.5.


## Continuação da auditoria de características de classe — após PR #102

- PR #102 foi integrada por squash no commit `e21ea7e48a834d2dec36a687b33bc337ebed15e0`. A CI da PR no commit `c8774ed0da11c7fc0ecec17d41c29459ccaa1c9e` concluiu com sucesso: testes unitários, compilação Android e upload do APK de depuração passaram. A CI pós-merge do commit integrado concluiu com sucesso (testes unitários, compilação Android e upload do APK de depuração): https://github.com/izzyn0okami/dnd35-app/actions/runs/39872090026.
- Revisão estática das características do **Druida**: níveis de Forma Selvagem (1/dia no 5º, 2/dia no 6º, 3/dia no 7º, 4/dia no 10º, 5/dia no 14º, 6/dia no 18º), tamanhos Grande/Miúdo/Enorme (8º/11º/15º), forma vegetal (12º), imunidade a venenos (9º), Mil Faces (13º), Corpo Atemporal (15º) e formas elementais (16º/18º/20º) correspondem à tabela SRD 3.5 consultada. Nenhuma correção de código proposta nesta passagem. Referência auxiliar: https://www.darkshire.net/jhkim/rpg/srd/srd_revised_html/ClassesI.html.
- Revisão estática do **Paladino**: Destruir o Mal nos níveis 1/5/10/15/20; Graça Divina e Imposição de Mãos no 2º; Aura de Coragem, Saúde Divina e Remover Doença 1/semana no 3º; Expulsar Mortos-Vivos e conjuração no 4º; Montaria Especial no 5º. A progressão de Remover Doença foi corrigida na PR #102 para níveis 3/6/9/12/15/18, chegando a 6 usos semanais. Nenhuma nova divergência de nível foi confirmada nesta leitura estática.
- Revisão estática do **Ranger**: Inimigo Predileto nos níveis 1/5/10/15/20; Estilo de Combate 2/6/11; Tolerância no 3º; Companheiro Animal e conjuração no 4º; Caminho da Floresta no 7º; Rastreador Veloz no 8º; Evasão no 9º; Camuflagem no 17º e Esconder-se à Plena Vista no 18º. Os níveis observados correspondem à progressão SRD 3.5; nenhuma correção de código proposta nesta passagem. A comparação terminológica integral com a edição brasileira ainda precisa ser concluída.
- A revisão de progressões do Druida, Paladino e Ranger foi estática e parcial; não equivale à validação de todos os textos/efeitos nem encerra a auditoria das onze classes. Não houve execução local do Gradle nem teste manual no aparelho nesta etapa.
- Revisão estática adicional do **Clérigo**: Aura, Expulsar/Fascinar Mortos-Vivos, Conversão Espontânea e Domínios estão registrados no 1º nível; não foi identificada nesta passagem uma divergência de nível. A aplicação representa regras e poderes de domínio como seleção/efeitos gerais, por isso esta verificação não valida a lista de magias nem todos os poderes de domínio.
- Revisão estática do **Guerreiro**: talentos bônus nos níveis 1, 2 e em cada nível par de 4 a 20; a sequência confere com a tabela de progressão 3.5. Nenhuma correção proposta.
- Revisão estática do **Feiticeiro**: conjuração espontânea e Invocar Familiar no 1º nível; não foi identificada divergência de nível nesta leitura. A progressão de magias conhecidas e usos diários é tratada fora desta lista de características.
- Revisão estática do **Mago**: Preparação com Grimório, Escrever Pergaminho, Invocar Familiar e escolha de especialização no 1º nível; talentos bônus nos níveis 5/10/15/20. A sequência corresponde à progressão básica 3.5 consultada; não foi identificada divergência de nível.
- Estas quatro conferências foram estáticas e focadas nos marcos de nível, não em uma auditoria completa de todas as descrições, pré-requisitos, magias ou opções. A conferência terminológica integral com o Livro do Jogador brasileiro ainda está pendente.
- Próximo foco: fazer uma passada cruzada nas onze classes para detectar duplicações, termos inconsistentes ou descrições que omitam condições relevantes; corrigir apenas divergências concretas e manter testes de regressão.


## Continuação — PR #103 e auditoria cruzada de características

- PR #103 foi integrada por squash no commit `799d7fb711335289626da770eeaddfe1b4c474f8`; a CI de compilação Android no commit `e2a59f880eea2bfbabdb41f9ddfb8f83e2d2fbc5` concluiu com sucesso. A PR #104 também foi integrada por squash no commit `140554c89c1d54a8446d1ce908f4b5441e6dd21d`; a CI de compilação Android no commit `03f899a53d97496213949792b851110d676c6422` concluiu com sucesso. A #104 completou a condição de armadura da Evasão do Ranger.
- Na passada cruzada das descrições, foi confirmada a mesma omissão na Evasão do Ladino (2º nível): a descrição dizia apenas que um sucesso em Reflexos evita o dano, sem registrar que a habilidade só funciona sem armadura ou com armadura leve. A regra da classe base exige essa condição. Corrigida a descrição e adicionado teste de regressão; não altera cálculos de dano nem outras mecânicas.
- Esta correção segue para CI antes de merge. A auditoria cruzada das onze classes continua aberta; a revisão é estática e não substitui a comparação integral de todas as habilidades com o Livro do Jogador brasileiro.


## Aquisição de idiomas — implementação proposta em 2026-10-22

- Regra mantida: “Falar Idioma” não recebe graduações como perícia comum; idiomas adquiridos por raça, Inteligência, habilidade de classe, talento ou outra fonte não devem ser confundidos com idiomas comprados.
- Novo modelo registra idioma, fonte, nível de personagem e pontos gastos. Uma aquisição marcada como comprada exige nível válido e exatamente 1 ponto; fontes gratuitas não podem registrar gasto de pontos.
- Banco atualizado da versão 4 para 5 por migração aditiva de `languageAcquisitionsJson`, com valor padrão vazio. Fichas antigas preservam `languagesJson`; não são convertidas automaticamente em compras nem se inferem gastos retroativos.
- Interface de perícias permite escolher a fonte e, para compra, informar o nível. A compra só é aceita se houver registro de orçamento para esse nível e um ponto ainda disponível; o gasto é lançado como `Falar Idioma (nome)` no histórico de pontos de perícia.
- Testes adicionados para validação de custo/fonte e compatibilidade com histórico antigo vazio. A cobertura direta da serialização foi retirada temporariamente porque depende do runtime Android e falhou no ambiente de testes JVM; precisa de estratégia de teste compatível antes de considerar esse ponto coberto.
- Status: branch `feat/language-acquisition-history`; CI em nova execução após ajuste do teste. Não houve teste local nem teste manual no aparelho. Não fazer merge até CI verde.


## Estado atualizado após PR #106 — 2026-10-23

- PR #106 integrada por squash no commit `ba0f48c612ac7ea44502858431586b15ecbbd3d2`; a CI do commit final `c2b8320c6e7d0c5f53860369e6207d30546b920d` concluiu com sucesso no run [39875428504](https://github.com/izzyn0okami/dnd35-app/actions/runs/39875428504).
- O registro de aquisição de idiomas distingue fontes gratuitas e compras, grava o custo de 1 ponto por idioma comprado no nível informado e remove a alocação correspondente quando o registro comprado é excluído.
- Limitação preservada: testes JVM cobrem validação e compatibilidade com histórico antigo vazio, mas não cobrem diretamente a serialização Android `org.json`; essa cobertura exige uma estratégia de teste compatível. Não houve teste manual no aparelho.
- Não há PRs abertas no repositório no momento da atualização.
- Próximo foco da auditoria: passada cruzada das características das onze classes básicas para encontrar duplicações, terminologia inconsistente ou condições omitidas, comparando com prioridade ao Livro do Jogador 3.5 em português. Depois, retomar os pontos pendentes de progressão de perícias apenas quando houver discrepância concreta. Consultar este log antes de abrir qualquer PR para evitar duplicação.


## Correção proposta — Esconder-se à Plena Vista do Ranger

- A revisão cruzada identificou que a descrição no código permitia usar a habilidade em terreno natural enquanto observado, mas não documentava a condição de estar a até 3 metros de terreno natural.
- A descrição foi ajustada para incluir a distância e esclarecer que cobertura/camuflagem não são necessárias. Foi acrescentado teste de regressão para a condição de 3 metros, observação e ausência de cobertura/camuflagem.
- Branch: `fix/ranger-hide-in-plain-sight-range`. Aguardar CI antes de merge; nenhum teste local nem teste manual no aparelho foi executado.


## Resultado da PR #107 — 2026-10-23

- PR #107 integrada por squash no commit `730cd8e11f08e9b285e548729480e1cb939cf8f3`; CI aprovada no run [39876038619](https://github.com/izzyn0okami/dnd35-app/actions/runs/39876038619).
- A descrição de Esconder-se à Plena Vista do Ranger agora registra a condição de estar a até 3 metros de terreno natural, além de poder agir sob observação e sem cobertura/camuflagem. Teste de regressão incluído.
- Próxima passada: revisar características com progressão escalonada do Bárbaro e do Monge, procurando diferenças objetivas contra o Livro do Jogador 3.5; não reabrir habilidades já cobertas por testes sem evidência de erro.


## Correção proposta — Encontrar Armadilhas do Ladino (2026-10-23)

- A descrição anterior confundia localizar armadilhas com CD acima de 20 e desarmar armadilhas mágicas. A regra separa as funções: Procurar permite localizar armadilhas de CD superior a 20; Operar Mecanismo permite desarmar armadilhas mágicas.
- A descrição foi reescrita sem alterar a lógica das rolagens e foi acrescentado teste de regressão.
- Branch: `fix/rogue-trapfinding-description`; aguardar CI antes de merge. Não houve teste Gradle local nem teste manual no aparelho.


## Resultado da PR #108 — 2026-10-23

- PR #108 integrada por squash no commit `00c136de62fe34969b7d768a8a7e862822e54578`.
- CI do commit final da PR aprovada no run [39876382183](https://github.com/izzyn0okami/dnd35-app/actions/runs/39876382183).
- A descrição de Encontrar Armadilhas do Ladino agora distingue Procurar para localizar armadilhas com CD acima de 20 de Operar Mecanismo para desarmar armadilhas mágicas. Teste de regressão incluído.
- A alteração é descritiva e de teste; não altera a lógica de rolagem. Não houve teste Gradle local nem teste manual no aparelho.
- Próximo foco: continuar a passada cruzada das características escalonadas do Ladino e do Ranger, e então revisar condições de progressão/uso das classes básicas, conferindo contra o Livro do Jogador brasileiro. Consultar este log antes de propor PR para evitar duplicações.


## Auditoria de BBA e testes de resistência — 2026-10-23

- Conferidas as fórmulas de `calculateBaseAttackBonus` e `calculateBaseSave`: BBA integral = nível; três quartos = piso(3 × nível / 4); metade = piso(nível / 2); resistência boa = 2 + piso(nível / 2); resistência ruim = piso(nível / 3). Os resultados conferem com as progressões 3.5 nos níveis de 1 a 20.
- Conferidos os tipos de BBA, Fortitude, Reflexos e Vontade das onze classes básicas contra as progressões do Livro do Jogador 3.5. As atribuições de Bárbaro, Bardo, Clérigo, Druida, Guerreiro, Monge, Paladino, Ranger, Ladino, Feiticeiro e Mago estão consistentes; nenhuma divergência concreta encontrada nesta passagem.
- Os testes existentes cobrem os resultados numéricos das três fórmulas ao longo dos níveis e verificam tipos representativos de classes. A conferência estática não equivale a teste local ou manual; nenhum Gradle local foi executado nesta etapa.
- Conclusão: não abrir PR de código para BBA/resistências nesta passagem. Próximo foco: rastrear o orçamento de pontos de perícia por nível, incluindo nível 1, modificador de Inteligência, bônus racial humano e multiclasse, verificando a coerência com o histórico de gastos já registrado. Consultar este log antes de propor mudanças.


## Histórico de pontos de perícia após redução de nível — 2026-10-23

- A auditoria do fluxo de edição confirmou que registros de níveis acima do nível atual são preservados intencionalmente e excluídos do total de gastos atuais. Porém, a lista ainda os exibia como registros comuns, sem indicar a exclusão do total, podendo confundir o usuário após reduzir o nível do personagem.
- Alteração proposta na PR: exibir um aviso nos registros cujo nível excede `character.level`, informando que o registro foi preservado, mas não entra no total atual. Não apagar nem recalcular o histórico automaticamente.
- A classe registrada por nível continua sendo uma informação histórica manual; a ficha não armazena a ordem exata em que cada classe foi adquirida, então não é seguro inferi-la automaticamente a partir apenas dos níveis atuais de cada classe.
- A PR ainda depende de CI. Não houve teste local Gradle nem teste manual em aparelho.


## Resultado da PR #109 — 2026-10-23

- PR #109 integrada por squash no commit `2ef2c297cf8d874dc51ad68f31070128d47a380c`.
- A CI do commit final `2d0671482a64b66fca561aa79ac5607f8d663c47` concluiu com sucesso no run [39877327072](https://github.com/izzyn0okami/dnd35-app/actions/runs/39877327072).
- A interface sinaliza registros de orçamento de perícias cujo nível excede o nível atual do personagem. Os registros são preservados e explicitamente indicados como fora do total atual; nenhum gasto histórico é apagado ou recalculado.
- Não houve teste Gradle local nem teste manual no aparelho.
- Próximo passo: auditar a regra de pontos de perícia por nível e o limite de graduações em fichas multiclasse, consultando este log e o histórico de PRs para evitar repetir decisões já resolvidas.


## Conferência do orçamento e limite de graduações — 2026-10-23

- Conferidos os pontos de perícia por nível das onze classes básicas no catálogo: Bárbaro 4; Bardo 6; Clérigo 2; Druida 4; Guerreiro 2; Monge 4; Paladino 2; Ranger 6; Ladino 8; Feiticeiro 2; Mago 2. Os valores conferem com a progressão padrão de D&D 3.5.
- Conferida a estimativa: no 1º nível, aplica-se o mínimo de 1 ponto após somar pontos da classe e modificador de Inteligência, multiplica-se por 4 e acrescenta-se o bônus humano de 4; nos níveis seguintes, soma-se o bônus humano de 1 por nível. A fórmula está coberta por testes para valores positivos e modificadores negativos.
- Conferido o limite de graduações: nível de personagem + 3 para perícia de classe de qualquer classe do personagem; metade desse valor para perícia entreclasse. A normalização preserva incrementos de 0,5, e a correção de multiclasse que impede saltos indevidos entre 0,5 e 1,5 já está integrada na PR #98; não recriar essa correção.
- O histórico é separado por nível, preserva registros acima do nível atual e não recalcula automaticamente os orçamentos históricos a partir da Inteligência atual. O aviso para registros acima do nível atual foi integrado pela PR #109 e sua CI passou.
- Resultado: não foi encontrada nova discrepância concreta nas fórmulas nem nos valores das onze classes; nenhuma PR de código aberta nesta passagem. A revisão foi estática e baseada nos testes existentes; não houve Gradle local nem teste manual no aparelho.
- Próximo foco: revisar as transições de classe de perícia na multiclasse e a consistência entre graduações registradas, limite efetivo e bônus exibido, sem reabrir a correção da PR #98 salvo evidência nova.


## Revisão de transição entre perícia de classe e entreclasse — 2026-10-23

- Revisados `CharacterCalculator.compute`, `CharacterViewModel.updateSkill`, `skillRankAdjustmentStep`, `normalizeEditedSkillRanks` e os testes `SkillRankAdjustmentTest` e `SkillPointBudgetTest`.
- A classificação das perícias conhecidas é recalculada considerando todas as classes atuais do personagem. Se a perícia pertence a qualquer uma delas, o limite atual é nível total + 3; caso contrário, é metade desse limite. Isso corresponde à regra de perícia de classe na multiclasse.
- A edição preserva incrementos de 0,5 ao mudar de entreclasse para classe, sem arredondar graduações antigas para cima; a normalização limita valores ao máximo vigente. Os testes existentes cobrem os passos de edição, mas não testam diretamente o cálculo completo de uma ficha em que a perícia muda de categoria.
- O custo histórico merece distinção: pontos já gastos como entreclasse não se tornam retroativamente mais baratos quando a perícia passa a ser de classe. A aplicação mantém um histórico de gastos por nível; quando ele está incompleto, exibe apenas uma estimativa baseada nas graduações e na classificação atuais. Como essa estimativa já está identificada como estimativa e o histórico manual permite registrar os gastos originais, não foi confirmado um defeito que justifique PR nesta passagem.
- Não foi encontrada discrepância concreta no cálculo do limite ou no passo de edição. Nenhuma PR de código aberta; revisão estática, sem execução local de testes ou teste manual no aparelho.
- Próxima verificação: conferir as regras de perícias treinadas apenas e a disponibilidade de testes sem graduação, comparando os sinalizadores do catálogo com o Livro do Jogador e evitando revisitar decisões já documentadas.


## Exceção de Conhecimento sem treinamento — 2026-10-23

- Defeito confirmado comparando o comportamento com o Livro do Jogador 3.5: a ficha bloqueava rolagens de Conhecimento com zero graduações por tratar o marcador “somente treinada” como proibição absoluta. A regra específica de Conhecimento permite teste de Inteligência sem treinamento para recordar apenas informações comuns (CD 10 ou inferior).
- Alteração proposta na PR: permitir a rolagem sem graduação para especialidades de Conhecimento e exibir na ficha a restrição de informações comuns (CD 10 ou inferior). As demais perícias “somente treinadas”, como Identificar Magia e Operar Mecanismo, continuam bloqueadas sem graduação.
- Adicionado teste unitário para diferenciar Conhecimento das demais perícias treinadas. A mudança não altera limites de graduação, custos de pontos ou listas de perícias de classe.
- Referência consultada: Livro do Jogador 3.5, seção de Conhecimento (p. 78 na edição em inglês), que explicita o teste sem treinamento para conhecimento comum. A terminologia final em português deve seguir a edição brasileira quando houver diferença de tradução.
- Resultado: PR #110 integrada por squash no commit `4c336d93c19279c40d8c6a45a2841b3f8299f5fb`. A CI do commit final `c01b94eb5514737f5c888f8591e37603ef2e7490` passou no workflow [39878676967](https://github.com/izzyn0okami/dnd35-app/actions/runs/39878676967). Não houve teste manual no aparelho.


## Conferência das perícias somente treinadas — 2026-10-23

- Conferidos os sinalizadores do catálogo de perícias contra as regras de uso sem treinamento do D&D 3.5: Abrir Fechaduras, Adestrar Animais, Conhecimento (especialidades), Decifrar Escrita, Identificar Magia, Operar Mecanismo, Prestidigitação, Profissão e Usar Instrumento Mágico permanecem marcadas como somente treinadas.
- A marcação de Conhecimento não significa bloqueio absoluto: a exceção para informações comuns (CD 10 ou inferior) foi implementada na PR #110. As demais perícias da lista continuam sem rolagem quando não possuem graduação.
- Não foi encontrada outra divergência concreta nos sinalizadores revisados que justifique uma nova PR. Esta é uma revisão estática do catálogo e do fluxo de rolagem, não uma certificação de todas as regras possíveis de cada perícia.
- Situação da etapa: revisão de perícias treinadas e uso sem graduação concluída. A auditoria geral de perícias pode ser encerrada após a conferência final do log e das alterações integradas; não reabrir os tópicos registrados como resolvidos sem evidência nova.


## Encerramento da etapa de auditoria de perícias — 2026-10-23

- Conferência final do estado integrado em `main`: a exceção de Conhecimento sem treinamento está presente em `Dnd35Rules.kt`, aplicada no cálculo de rolagem em `CalculatedCharacterStats.kt`, comunicada na interface em `SkillsTab.kt` e coberta por teste unitário em `ExampleUnitTest.kt`.
- O histórico acima registra as decisões verificadas nesta etapa: orçamento e limite de graduações, multiclasse e transição entre perícia de classe/entreclasse, histórico de gastos por nível, sinalização de registros de níveis superiores ao nível atual, sinalizadores de perícias somente treinadas e exceção de Conhecimento para informações comuns (CD 10 ou inferior). Correções já integradas não devem ser recriadas sem evidência nova.
- Encerramento desta etapa: não foi identificada pendência concreta que justifique outra PR de perícias com base nas verificações registradas. Isso encerra o escopo auditado aqui, não certifica exaustivamente todas as regras de todas as perícias nem substitui conferência integral com a edição brasileira do Livro do Jogador.
- Limitações: revisão estática do código e dos testes existentes; não foi executado Gradle local nem teste manual no aparelho nesta etapa. A CI da PR #110 passou; não se presume CI para este commit documental.


## 2026-10-22 — Auditoria de UI e usabilidade

Correções implementadas nesta PR; aguardar CI e teste manual antes do merge:
- Exclusão de talentos, magias e itens exige confirmação explícita.
- Alvos de toque de editar/excluir e controles de espaços de magia ampliados para 48 dp nos pontos revisados.
- Rolagem de d20 usa o título da ação; 20/1 naturais fora de ataques deixam de ser chamados de acerto/falha crítica, e dano da arma só aparece em ataques.
- Entradas de PV rejeitam valores inválidos/negativos e não fecham o diálogo sem alteração válida; nível inválido não é convertido silenciosamente em nível 1.
- Ouro rápido rejeita entradas inválidas ou não positivas em vez de converter silenciosamente para zero ou inverter gasto/ganho.
- Diálogos de item, magia, talento, armadura e atributos bloqueiam o salvamento quando os dados numéricos obrigatórios são inválidos ou o nome está vazio.
- Controles de PV reorganizados em duas linhas para reduzir compressão/corte em telas estreitas.

Limitações: a validação estática não substitui build/CI nem teste visual em telas estreitas. Nenhuma regra de D&D 3.5 foi alterada. A validação numérica restante dos diálogos de armadura/atributos/itens e a padronização visual global precisam de revisão adicional antes do merge.


## 2026-10-23 — Orientação de edição para multiclasse

- Na edição de dados gerais, os campos Classe e Nível ficam desabilitados quando o personagem possui mais de uma classe; uma mensagem direciona para “Gerenciar multiclasse e níveis”. Isso evita apresentar campos que `updateGeneralInfo` não aplica a personagens multiclasse.
- Padronizado o rótulo “Gerenciar multiclasse e níveis”.
- Não altera cálculos, progressões ou regras de D&D 3.5.


## 2026-10-23 — Padronização de rótulos da interface

- Padronizados títulos e descrições nas telas de perícias, inventário, magias e talentos.
- Trocados usos de “&” por “e” em títulos em português e reduzidas maiúsculas desnecessárias.
- Padronizada a nomenclatura visual para “graduações”, “bônus diversos e de talentos”, “riqueza e moedas”, “espaços de magia por dia” e “nível da magia”.
- Atualizados os textos acessíveis dos controles de equipamento e de gasto/recuperação de espaços de magia.
- Alterações limitadas à apresentação textual: nenhum cálculo, catálogo, regra ou decisão de auditoria foi modificado.
- Validação pendente: CI da PR e inspeção de eventuais efeitos colaterais de substituições de rótulos.


## 2026-10-23 — Alvos de toque e rótulo de nível de magia

- Aumentados para 48 dp os alvos dos controles de rolagem e ajuste de graduações na lista de perícias, para facilitar o uso por toque.
- Padronizado o rótulo de nível de magia no cartão de espaços diários para “Nível”, evitando a abreviação inconsistente “Nv”.
- Mudanças limitadas à usabilidade e apresentação; cálculos, regras e dados de personagem não foram alterados.
- Aguardar CI e revisar a compilação antes do merge. A disposição em aparelhos estreitos ainda precisa de validação manual.


## 2026-10-23 — Rótulos acessíveis dos itens do inventário

- Os ícones de edição e exclusão de cada item agora incluem o nome do item em suas descrições acessíveis (por exemplo, “Editar espada longa” e “Excluir espada longa”), ajudando usuários de leitores de tela a distinguir ações em listas com vários itens.
- Alteração limitada à acessibilidade da interface; não modifica regras, cálculos, dados nem comportamento de exclusão.
- Aguardar CI da PR antes do merge. A revisão é estática; não houve teste manual com leitor de tela ou no aparelho.


## 2026-10-23 — Feedback para idioma duplicado

- Ao tentar cadastrar um idioma já existente, a seção de idiomas agora mostra a mensagem “Esse idioma já está cadastrado.” em vez de ignorar silenciosamente a ação.
- A mudança melhora o feedback da interface e não altera o catálogo de idiomas, regras de aquisição, pontos de perícia ou cálculos.
- Aguardar CI; teste manual em aparelho ainda não realizado.


## 2026-10-23 — Rótulos acessíveis das magias

- Os botões de edição e exclusão na lista de magias agora incluem o nome da magia em suas descrições acessíveis (por exemplo, “Editar Bola de Fogo” e “Excluir Bola de Fogo”), permitindo distinguir ações quando há várias magias na lista.
- Alteração limitada à acessibilidade da interface; não modifica regras, cálculos, dados nem comportamento de exclusão.
- Aguardar CI da PR antes do merge. A revisão é estática; não houve teste manual com leitor de tela ou no aparelho.


## 2026-10-23 — Rótulos acessíveis dos talentos

- Os botões de edição e exclusão na lista de talentos agora incluem o nome do talento em suas descrições acessíveis (por exemplo, “Editar Ataque Poderoso” e “Excluir Ataque Poderoso”), ajudando a distinguir ações em listas com vários talentos.
- Alteração limitada à acessibilidade da interface; não modifica regras, cálculos, dados nem comportamento de exclusão.
- Aguardar CI da PR antes do merge. A revisão é estática; não houve teste manual com leitor de tela ou no aparelho.


## 2026-10-23 — Rótulos acessíveis contextualizados em perícias, equipamento e espaços de magia

- `SkillsTab.kt`: os botões de aumentar/diminuir graduação passam a anunciar a perícia específica.
- `InventoryTab.kt`: a ação de desequipar anuncia o nome do item, útil quando há vários espaços/equipamentos.
- `SpellsTab.kt`: o botão de configuração anuncia o nível de magia correspondente.
- Alterações limitadas a conteúdo acessível; sem mudança em regras, cálculos, estado ou comportamento das ações.
- Validação de compilação e testes fica a cargo da CI desta PR; teste manual com leitor de tela/dispositivo não realizado.


## 2026-10-23 — Ação de rolagem de ataque sem controles interativos aninhados

- `OverviewCombatTab.kt` (`AttackRow`): removida a ação de clique duplicada do cartão, mantendo a rolagem no botão explícito com o bônus de ataque.
- Evita que o cartão e o botão interno sejam expostos como duas ações sobrepostas para leitores de tela.
- A rolagem continua usando a mesma função e os mesmos cálculos; nenhuma regra ou dado foi alterado.
- A área de ação fica concentrada no botão visível; validar a usabilidade em aparelho físico posteriormente.


## 2026-10-23 — Alvos de toque no cabeçalho da ficha

- No cabeçalho de personagem (OverviewCombatTab.kt), ampliado de 34 dp para 48 dp o alvo do controle de edição da foto.
- Ampliada de 34 dp para 48 dp a altura do botão “Gerenciar” classes/multiclasse, para facilitar o acionamento por toque.
- Alterações de apresentação e usabilidade; não alteram regras, cálculos ou dados do personagem.
- Aguardando CI. A adequação visual e o comportamento em aparelhos físicos ainda precisam de teste manual.


## 2026-10-23 — Alvos de toque nos controles de Pontos de Vida

- Em `HpBarSection.kt`, o controle de edição dos PV máximos passa de 36 dp para 48 dp.
- O botão “Descanso” passa a ter altura mínima de 48 dp, em vez de altura fixa de 32 dp.
- Objetivo: facilitar o toque sem alterar ações, regras de descanso, PV ou quaisquer cálculos.
- Aguardando CI; a disposição em telas estreitas e em aparelho físico ainda precisa de teste manual.


## 2026-10-23 — Indicador de pontos investidos por perícia

- Defeito de usabilidade confirmado: o total geral não mostrava quanto cada perícia representa em pontos, embora as graduações e a categoria classe/entreclasse estejam disponíveis.
- Correção proposta na branch `fix/skill-points-spent-indicator`: cada linha de perícia exibe um indicador de pontos investidos estimados a partir das graduações atuais (custo de 1 ponto por graduação de classe e 2 por graduação entreclasse).
- O indicador é explicitamente identificado como estimativa; não altera o histórico manual por nível nem presume que a distribuição atual seja igual aos gastos históricos. O total geral existente continua distinguindo histórico completo de estimativa atual.
- Não modifica graduações, limites, cálculos de bônus ou regras de rolagem.
- Aguardar CI antes de merge. Teste visual em aparelho e em tela estreita ainda pendente.


## 2026-10-23 — Saldo agregado do histórico de pontos de perícia

- O resumo geral da aba de perícias passa a exibir o orçamento total registrado e o saldo restante quando existe um registro para cada nível atual do personagem.
- Se o histórico não cobre todos os níveis atuais, a interface informa que o saldo não está disponível em vez de apresentar uma conta incompleta como se fosse saldo real.
- Se os gastos registrados excederem o orçamento, o resumo informa o excesso e usa a cor de erro; saldo positivo e saldo zero são diferenciados.
- O cálculo usa somente registros dos níveis atuais, mantendo o comportamento existente para registros de níveis futuros. Não altera nem gera automaticamente gastos históricos, graduações ou orçamentos.
- Aguardar CI da PR. A validação manual em aparelho e em tela estreita permanece pendente.

## 2026-10-23 — Histórico de perícias com níveis duplicados

- A revisão do saldo agregado identificou um caso em que o histórico poderia ser considerado completo mesmo com dois registros para o mesmo nível, desde que todos os níveis também estivessem representados.
- Como esses registros podem duplicar o orçamento e os gastos, o histórico passa a ser marcado como incompleto se qualquer nível atual aparecer mais de uma vez. A interface então não apresenta saldo histórico confirmado.
- Adicionado teste automatizado para cobrir a duplicação. Os registros existentes não são apagados nem corrigidos automaticamente.
- Aguardar CI da PR antes do merge; teste manual em aparelho continua pendente.

## 2026-10-23 — Proteção ao contabilizar idiomas comprados

- A revisão das ações da aba encontrou que a inclusão de um idioma comprado escolhia o primeiro registro de pontos do nível e atualizava todos os registros que compartilhassem esse nível.
- A ação agora exige um nível de personagem dentro do nível atual da ficha e exatamente um registro de orçamento para esse nível. Se houver duplicidade, orienta corrigir o histórico antes de contabilizar o gasto.
- A mudança evita propagar um gasto para registros duplicados e evita usar níveis futuros ou inválidos. Não remove nem altera automaticamente registros existentes.
- Aguardar CI da PR. Teste manual em aparelho físico continua pendente.



## 2026-10-23 — Corrige mensagem de validação de idioma comprado

- Revisão após a PR #126 encontrou um erro textual na mensagem de nível inválido: o escape `\$` fazia a interface mostrar literalmente `${stats.totalCharacterLevel}` em vez do nível atual do personagem.
- A correção remove apenas o escape indevido para que o Kotlin interpole o nível atual na mensagem. Não altera validações, regras ou contabilização de pontos.
- CI da PR e validação pós-merge ainda precisam ser verificadas; teste manual da mensagem no aparelho permanece pendente.


## 2026-10-23 — Remoção individual de registros duplicados de perícias

- A revisão da tela de histórico identificou que o botão de remover um registro filtrava pelo número do nível; quando havia registros duplicados, um único toque apagava todos os registros daquele nível.
- A lista agora mantém o índice original e remove apenas o registro correspondente ao botão acionado, preservando os demais registros duplicados para correção deliberada pelo usuário.
- Corrige comportamento destrutivo em históricos ambíguos; não altera cálculos nem remove dados automaticamente além do registro explicitamente selecionado.
- Aguardar CI da PR e validação pós-merge; teste manual em aparelho continua pendente.

## 2026-10-22 — Revisão da perícia Saltar para o Ranger

- **Correção da auditoria anterior:** a conclusão de que “Saltar” não seria perícia de classe do Ranger estava incorreta.
- A lista de perícias de classe do Ranger no SRD de D&D 3.5 inclui Jump (Saltar). Referências consultadas: SRD 3.5, seção Ranger — https://www.35srd.com/players/classes/base-classes/ranger/ e https://www.darkshire.net/~jhkim/rpg/srd/srd_revised_html/ClassesII.html.
- Mantida “Saltar” na lista de perícias de classe do Ranger; o código em main já a contém.
- PR #129 integrada por squash em 2026-10-23 no commit 31465d4d629d1b9e5e07c8f36baa356c335ff7d7. CI de push e pull request passou nos runs https://github.com/izzyn0okami/dnd35-app/actions/runs/39891991824 e https://github.com/izzyn0okami/dnd35-app/actions/runs/39891995755.
- Teste manual no Android continua pendente.

## 2026-10-23 — Revisão estática da transição de perícias de classe em multiclasse

- Consultado este log antes da revisão; a verificação continua a etapa pendente de consistência entre classificação atual, limite de graduações e bônus exibido, sem reabrir a correção de Saltar nem a correção de passos fracionários da PR #98.
- Em CharacterCalculator.compute, perícias catalogadas e especialidades de Ofícios determinam a classificação de classe pela lista de qualquer classe atual do personagem; o limite é nível total + 3 se for perícia de classe de ao menos uma classe, ou metade desse valor se for entreclasse.
- Em CharacterViewModel.updateSkill, a classificação e o limite são recalculados usando as classes atuais. O normalizador preserva passos de 0,5 e limita o valor sem arredondar para cima. A interface usa o mesmo limite calculado e mantém passo de 0,5 quando há graduação fracionária.
- O bônus exibido usa ranks.toInt(), de modo que uma meia graduação isolada não acrescenta +1 ao teste, conforme decisão já registrada.
- Não foi confirmado defeito novo nesta inspeção estática. Os testes atuais cobrem a função de passo de edição e histórico de orçamento, mas não foi localizada nesta passada uma regressão integrada que monte uma ficha multiclasse e confirme em conjunto classificação, limite, graduação e bônus.
- Próximo passo: avaliar como testar CharacterCalculator.compute em cenários multiclasse de forma isolada; só abrir alteração de código se o caso de teste revelar divergência. Nenhum teste local/Gradle nem teste manual no Android foi executado nesta inspeção.

## 2026-10-23 — Cobertura integrada do cálculo de perícias em multiclasse

- PR #130 foi integrada por squash no commit `5393a6e2a7b3bc4070e89a3fd65678e95b03765e`; a CI passou, incluindo testes unitários, compilação do APK de depuração e envio do artefato.
- Adicionado teste de regressão integrado para `CharacterCalculator.compute` com Ranger 2 / Guerreiro 2. O cenário verifica que Ouvir é perícia de classe por pertencer à lista do Ranger, apesar do sinalizador legado salvo como falso; o limite é 7 graduações, e 5,5 graduações contribuem +5 ao bônus.
- O mesmo cenário verifica que Atuação não vira perícia de classe apenas porque o sinalizador legado está verdadeiro: seu limite entreclasse é 3,5, o valor é limitado de 4,5 para 3,5 e o bônus de graduação é +3.
- O teste cobre classificação, limite, normalização/limitação e bônus em conjunto. Ele é uma adição de cobertura; não afirma que os testes passaram até que a CI desta PR conclua.
- Nenhuma regra nem cálculo de produção foi alterado. Não foi executado Gradle local nem teste manual no Android.
- Próximo passo: aguardar CI desta PR. Se aprovada, integrar e seguir para a próxima pendência concreta da auditoria, consultando este log antes de qualquer nova alteração.

## 2026-10-23 — Resultado da CI do teste integrado de multiclasse

- PR #131 integrada por squash no commit `87d209d3eb4d35458462f24a4f2e4680ac3f791e`.
- CI `Android build` concluída com sucesso no run https://github.com/izzyn0okami/dnd35-app/actions/runs/39892750480. A execução inclui a etapa de testes unitários e a compilação do APK de depuração.
- O novo teste de regressão está agora em `main`; a integração não alterou o código de produção nem as regras.
- A lacuna de cobertura integrada de classificação, limite e bônus foi atendida por esse teste. A auditoria de perícias não deve ser reaberta sem evidência nova; o teste manual em aparelho segue pendente.
- Próximo foco da auditoria geral: retomar a revisão das progressões de BBA e testes de resistência das classes básicas e, em seguida, a conferência classe por classe das características escalonadas, priorizando o Livro do Jogador brasileiro 3.5. Não declarar a auditoria geral concluída enquanto essa conferência estiver incompleta.

## 2026-10-23 — Revisão de progressões de BBA e testes de resistência

- Após integrar a PR #131, a CI confirmou o teste integrado de perícias multiclasse; a PR #132 também foi integrada para registrar esse resultado no log.
- Inspeção estática de `Dnd35Rules.kt`: as onze classes básicas estão configuradas com os tipos esperados de BBA e testes de Fortitude, Reflexos e Vontade; as fórmulas de progressão usam `nível`, `3 × nível / 4`, `nível / 2`, `2 + nível / 2` e `nível / 3`, respectivamente, com divisão inteira.
- Não foi identificada divergência concreta no código de produção nesta passagem. Para evitar regressões, a PR atual adiciona testes que conferem os tipos das onze classes e as fórmulas em todos os níveis de 1 a 20.
- A conclusão depende da CI da PR atual. Esta verificação não substitui cotejo integral com a edição brasileira do Livro do Jogador; a auditoria geral de características e terminologia permanece aberta. Não houve teste manual no Android.
- Próximo passo: após CI aprovada, integrar os testes e continuar a comparação das características escalonadas das classes básicas contra o Livro do Jogador brasileiro, sem reabrir itens resolvidos sem evidência nova.


## 2026-10-23 — Correção de histórico: auditoria prévia de BBA, resistências e características de classe

- Revisão retrospectiva após a PR #133: as progressões de BBA e resistências já tinham sido auditadas e cobertas parcialmente pela PR #101, que verifica as fórmulas em todos os níveis de 1 a 20 e confere as configurações de Guerreiro, Ladino, Mago e Monge. Portanto, não registrar essa etapa como uma auditoria inédita.
- A PR #133 foi integrada por squash no commit `2d5fcaa4fccdb19e30e93b6678da7a660528c6f6`; a CI `Android build` passou no run https://github.com/izzyn0okami/dnd35-app/actions/runs/39893110675. Seu teste de configuração das onze classes amplia a cobertura da PR #101, enquanto o segundo teste repete a verificação das fórmulas gerais já existente. Não houve alteração de código de produção.
- As características escalonadas também já foram auditadas em parte: PR #102 corrigiu progressões concretas e PR #103 documentou a revisão estática das onze classes, incluindo as conferências do Druida, Paladino, Ranger, Clérigo, Guerreiro, Feiticeiro e Mago, além da revisão cruzada de condições de Evasão que levou às PRs #104 e #105.
- O estado correto é: as progressões gerais de BBA/resistências e várias progressões de classe já foram examinadas; a auditoria integral de todas as descrições, condições e terminologia da edição brasileira continua aberta. Não reabrir as correções já integradas sem evidência nova.
- Próximo passo: continuar a revisão cruzada ainda pendente das descrições de características de classe, usando o Livro do Jogador brasileiro como referência prioritária e registrando apenas discrepâncias novas e comprováveis. Não repetir a verificação de fórmulas gerais já coberta pela PR #101.
- Validação manual Android continua pendente; a CI aprovada não substitui teste no aparelho.


## Correção de interface — validação do nome ao editar dados gerais (2026-10-23)

- Defeito confirmado: `EditGeneralInfoDialog.kt` aceitava salvar nome vazio ou composto apenas por espaços; `CharacterViewModel.updateGeneralInfo()` armazenava o valor recebido sem validação adicional.
- Correção proposta na branch `fix/validate-character-name-edit`: normalizar o nome com `trim()`, impedir o salvamento quando ficar vazio e mostrar mensagem de validação no campo. O nome enviado ao ViewModel agora é o valor normalizado.
- Adicionado teste unitário para rejeitar vazio/espaços, remover espaços nas extremidades e preservar espaços internos.
- Escopo intencionalmente isolado: não altera o gerenciamento de multiclasse, a persistência geral nem os formulários de outros campos.
- PR #136 aberta: https://github.com/izzyn0okami/dnd35-app/pull/136. A CI ainda precisa concluir testes unitários, compilação do APK de depuração e upload do artefato. Teste manual Android pendente; não integrar sem CI aprovada e autorização explícita.


## Correção de formulário — ajuste detalhado de moedas (PR em andamento)

- Corrigido o diálogo **Ajustar Moedas** para não substituir silenciosamente entradas inválidas pelos saldos anteriores.
- O formulário agora valida os quatro valores antes de atualizar qualquer saldo; em caso de erro, mostra mensagem e permanece aberto, sem chamar a atualização.
- Valores negativos, vazios, malformados, NaN e infinitos são rejeitados. A entrada aceita vírgula decimal para usuários em português.
- Adicionado teste unitário para valores inteiros/decimais, vírgula decimal e entradas inválidas.
- PR #136 integrada por squash em `main` (commit `2a019e86a2dab5ce56168f37a193bd7a199b7f9a`); CI aprovada antes do merge. Teste manual no aparelho continua pendente.
- Esta correção de moedas será proposta em PR separada; não integrada ainda.

## Ajuste detalhado e conversão de moedas — atualização (2026-10-23)

- PR #137 integrada por squash após CI aprovada: https://github.com/izzyn0okami/dnd35-app/pull/137. O formulário valida todos os quatro saldos antes de salvar, rejeita valores vazios, inválidos, negativos e não finitos, e aceita vírgula decimal.
- Auditoria do botão **Converter**: o comportamento esperado é consolidar o equivalente total em PO, PP e PC, zerando PL, conforme as relações 10 PC = 1 PP, 10 PP = 1 PO e 10 PO = 1 PL.
- Corrigida a implementação para fazer a decomposição a partir do valor equivalente em PC usando `Double` e manter eventual fração de PC, em vez de truncar cada parte com `toInt()`. Entradas negativas ou não finitas são rejeitadas sem alterar o personagem.
- Adicionado `CoinConversionTest.kt`, com testes de equivalência de valor, conversão de platina, frações e combinações inteiras em lote.
- PR de conversão aberta em branch separada: `fix/preserve-coin-value-on-conversion`. CI e revisão ainda pendentes; teste manual no aparelho também pendente.


## Retomada da auditoria de UI — retrato e cartão de personagem (2026-10-23)

- Retomada a partir das decisões de interface já estabelecidas: retrato grande e quadrado; nome/classe/nível em painel abaixo da foto; remover o controle deslizante de zoom.
- Em `OverviewCombatTab.kt`, o retrato usa proporção 1:1, o painel com nome, classes, nível, raça, tendência e divindade fica logo abaixo, e o slider de enquadramento foi removido. O botão de troca de foto permanece acessível no canto superior.
- Não foi adicionada ainda a ferramenta de recorte quadrado no fluxo de seleção da foto; essa será uma correção separada, para não misturar o comportamento de seleção/recorte com a composição do cabeçalho.
- PR de UI aberta em branch `ui/square-portrait-info-panel-below`; aguardar CI de testes unitários e compilação do APK antes de considerar merge. Teste visual manual em aparelho ainda pendente.


## Recorte quadrado de retrato — PR em andamento

- Implementação proposta em `PhotoPickerModal.kt`: após escolher uma imagem da galeria, abre uma etapa de recorte quadrado com gesto de arrastar e pinça para zoom; a foto só é aplicada após confirmação em **Usar foto**.
- O recorte confirmado é salvo como JPEG no armazenamento privado persistente do app e o URI salvo no personagem aponta para essa cópia. Cancelar o recorte retorna ao seletor sem substituir a foto atual. Retratos predefinidos continuam usando seus identificadores `preset:` e não passam pelo recortador.
- A imagem original não é alterada. A implementação ainda precisa passar por CI/compilação e revisão do cálculo de recorte; não considerar integrada até merge aprovado. Teste visual no aparelho permanece pendente.


## Pós-merge do recortador e proteção contra retratos grandes — 2026-10-23

- PR #141 integrada por squash no commit `1b0ba3510a40fed8b8eda66536f4af3ec329d518`; CI passou no run https://github.com/izzyn0okami/dnd35-app/actions/runs/39898087367.
- O fluxo de recorte quadrado está em `PhotoPickerModal.kt`: arrastar/pinça, confirmação antes de substituir a foto e gravação privada do recorte. Teste visual em aparelho continua pendente.
- Revisão pós-merge identificou risco de consumo excessivo de memória ao decodificar fotos originais em resolução total. Correção proposta separadamente na branch `fix/sample-large-portrait-before-crop`: primeiro ler dimensões e depois decodificar com `inSampleSize`, limitando a maior dimensão decodificada a aproximadamente 2048 px. Isso reduz risco de falta de memória; não altera a imagem original.
- Não considerar essa proteção integrada até CI aprovada e merge. Manter o teste manual Android pendente.


## Estado inicial sem personagens — PR #143 integrada (2026-10-23)

- PR #142 integrada por squash no commit `56ac8a774da4dd556dcd5e69436ba0ca24e570fe`; CI `Android build` passou no run https://github.com/izzyn0okami/dnd35-app/actions/runs/39898410891. A proteção do recortador decodifica imagens com amostragem e dimensão máxima aproximada de 2048 px.
- Próxima pendência de UI confirmada em `DndMainScreen.kt`: quando não há personagem ativo, a tela mostrava apenas um indicador de carregamento, mesmo quando a lista de fichas estava vazia. Isso não orientava o usuário a iniciar uma ficha.
- Correção integrada na PR #143, squash no commit `afa30fcede4fbb250275b335f4598b94b1ffedb6`; CI `Android build` passou no run https://github.com/izzyn0okami/dnd35-app/actions/runs/39898957652. Quando não existe personagem ativo e a lista está vazia, a tela exibe estado vazio com explicação e botão **Criar personagem**, que abre o diálogo existente. Se há fichas mas ainda falta personagem/estatísticas ativas, mantém o indicador de carregamento.
- O teste visual no aparelho permanece pendente.


## 2026-10-23 — Contagens visíveis em seções limitadas do PDF (PR em preparação)

- Revisão estática de `CharacterPdfExporter.kt` confirmou limites fixos que descartam itens adicionais da representação visual sem indicar isso: até 4 armas equipadas, 6 talentos, 9 magias e 10 itens de inventário.
- Correção proposta na branch `fix/pdf-exporter-visible-item-counts`: os títulos dessas seções passam a mostrar a quantidade total cadastrada e o limite exibido, deixando explícito quando o PDF mostra apenas uma parte dos dados.
- A mudança não altera nem remove dados da ficha e não muda o layout/paginação do PDF; ela torna a limitação existente visível ao usuário. Uma solução futura de paginação completa pode ser considerada separadamente, pois exige redesenhar o fluxo de páginas e limites verticais.
- PR #144 integrada por squash no commit `f09ceb987f75db0f7189d2acb3b8d2e547b7697a`; CI aprovada no run https://github.com/izzyn0okami/dnd35-app/actions/runs/39899299861. Teste manual de exportação com listas acima dos limites continua pendente.


## 2026-10-23 — PV temporários absorvem dano antes dos PV normais (PR #145 integrada)

- Inspeção de `CharacterViewModel.adjustHp()` confirmou que o dano era aplicado diretamente aos PV normais, sem reduzir os PV temporários; o limite superior também permitia PV normais até `maxHp + tempHp`.
- Na branch `fix/temp-hp-absorbs-damage`, a lógica foi extraída para `calculateHpAdjustment()`: dano consome PV temporários primeiro e só então reduz PV normais; cura restaura apenas PV normais até o máximo e preserva PV temporários.
- Foram adicionados testes unitários para dano parcialmente absorvido, dano que excede os PV temporários, cura com PV temporários existentes, teto de PV máximos e dano extremo.
- PR #145 integrada por squash em 2026-10-23 no commit `243b49601be6859c1a2c39296a84212d24218923`; CI aprovada no run https://github.com/izzyn0okami/dnd35-app/actions/runs/39899738511. Validação manual no aplicativo permanece pendente.


## 2026-10-23 — Esquiva Sobrenatural: condições de surpresa, oponente invisível e imobilização (PR #139 integrada)

- A descrição de Esquiva Sobrenatural do Ladino foi completada conforme o Livro do Jogador brasileiro 3.5: conserva o bônus de Destreza quando surpreendido e contra oponentes invisíveis, mas perde o bônus quando imobilizado.
- A revisão cruzada encontrou a mesma omissão na descrição do Bárbaro; a PR #139 inclui também a condição de imobilização para essa classe.
- Testes de regressão em `ClassProgressionTest.kt` cobrem as condições para Ladino (4º nível) e Bárbaro (2º nível). A alteração é descritiva e não muda o cálculo mecânico da CA.
- Referência primária: Livro do Jogador brasileiro 3.5, https://anyflip.com/hgohy/pjla/basic; confirmação complementar: https://www.35srd.com/players/classes/base-classes/rogue/.
- PR #139 integrada por squash no commit `3515139197bd8476e94430c5542c8a82c2ee1baf`; CI aprovada nos runs https://github.com/izzyn0okami/dnd35-app/actions/runs/39900886039 e https://github.com/izzyn0okami/dnd35-app/actions/runs/39900881138. Teste manual no Android permanece pendente.

## 2026-10-23 — Seletores de categoria e espaço no item personalizado (PR #146 em revisão)

- Revisado `AddEditItemDialog.kt` na aba Carga e itens > Adicionar item > Item personalizado.
- Os estados e listas de categoria e espaço de equipamento já existiam, mas os seletores estavam implementados como `OutlinedTextField(readOnly = true)` com `clickable` aplicado ao próprio campo. Isso torna a interação pouco confiável e pode impedir a abertura dos menus em alguns contextos.
- Na PR #146, ambos os campos foram substituídos por botões de seleção explícitos com seta e menus suspensos. Selecionar categoria atualiza `category`; selecionar espaço atualiza `equippedSlot` e sincroniza a opção “Equipado atualmente”.
- A alteração não muda categorias disponíveis nem regras de equipamento. Após a sincronização com a nova `main`, a CI precisa ser executada novamente antes do merge. Teste manual no Android continua pendente.


## 2026-10-23 — Expansão do catálogo de equipamentos (PR em preparação)

- Ampliado `Dnd35Rules.PRESET_ITEMS` de uma seleção curta para 163 entradas predefinidas, incluindo armas simples, marciais e exóticas adicionais, munições, escudos, equipamento de aventura e consumíveis.
- Mantidas as categorias e os espaços de equipamento já utilizados pelo inventário; os itens predefinidos continuam sendo convertidos pelo fluxo existente de adicionar item.
- Criado `PresetEquipmentCatalogTest` para verificar itens representativos, validade de categorias/espaços e ausência de nomes exatamente duplicados.
- Escopo: catálogo-base ampliado para o Livro do Jogador/SRD 3.5. A lista não deve ser considerada uma transcrição integral de todos os itens mágicos do Livro do Mestre ou de suplementos; essa cobertura precisa de uma etapa própria e validação contra fontes oficiais em português.
- CI e revisão de valores/terminologia ainda pendentes. Não integrar até a CI passar e a lista ser revisada.


## 2026-10-23 — Revisão inicial de talentos do Livro do Jogador

- Consultado o registro antes de alterar o catálogo; não havia entrada anterior documentando uma revisão completa de talentos nem PR aberta para este trabalho.
- Na branch `feat/catalogo-phb-magias-talentos`, corrigidas seis descrições de `PRESET_FEATS`: Especialização em Arma agora explicita proficiência com a arma escolhida; Foco em Arma Aprimorado esclarece que o bônus total passa a +2; Magia Acelerada descreve ação rápida e aumento de quatro níveis; Escrever Pergaminho restringe a magia àquela conhecida e que o criador possa conjurar e remete aos custos/tempo normais; Criar Varinha explicita a exigência de magia de 4º nível ou inferior conhecida e os custos/tempo normais; Tiro Certeiro foi ajustado para indicar corretamente o alcance de 9 m.
- Esta é uma correção parcial de conteúdo já existente, não a conclusão do catálogo: a lista atual ainda contém apenas 27 talentos e 42 magias. O objetivo de referência é conferir os 110 talentos do PHB 3.5 e catalogar as magias do mesmo livro, evitando material de suplementos e entradas duplicadas.
- Não afirmar que o catálogo está completo. Antes de integrar, ainda é necessário acrescentar cobertura de testes, expandir os dados com referência verificável para pré-requisitos/benefícios e metadados de magias, revisar terminologia em português e aguardar CI.
- Validação local/Gradle e teste manual no Android não foram executados nesta etapa.


## 2026-10-23 — Expansão parcial do catálogo de talentos do Livro do Jogador

- Consultado o estado de PRs abertas antes de iniciar: nenhuma PR aberta foi encontrada.
- Na branch `feat/expand-phb-feats-catalog`, o catálogo `PRESET_FEATS` passou de 27 para 41 entradas, adicionando 14 talentos do Livro do Jogador 3.5: Acuidade com Arma, Agarrar Aprimorado, Derrubar Aprimorado, Desarme Aprimorado, Reflexos de Combate, Combater com Duas Armas, Combater com Duas Armas Aprimorado, Combater com Duas Armas Maior, Saque Rápido, Magia em Combate, Foco em Magia, Foco em Magia Maior, Sucesso Decisivo Aprimorado e Tiro Longo.
- Adicionado `PresetFeatCatalogTest.kt`, verificando presença dessas entradas e ausência de nomes duplicados.
- Isto é uma expansão parcial, não a conclusão dos 110 talentos listados no catálogo de referência do PHB 3.5. Os pré-requisitos e textos mecânicos ainda precisam de revisão item a item contra o Livro do Jogador brasileiro; não declarar o catálogo completo nem usar material de suplementos.
- O catálogo de magias também permanece incompleto. Não foram acrescentadas magias nesta etapa, pois o modelo exige dados mecânicos por magia e classe; não preencher esses campos por suposição. A próxima etapa deve estruturar e conferir a lista de magias do Livro do Jogador, separando entradas únicas de níveis por classe.
- Nenhum teste Gradle foi executado localmente nesta operação. A CI da PR precisa passar antes do merge; teste manual no Android continua pendente.


## 2026-10-23 — Expansão parcial de talentos PHB, lote 2

- PR #149 foi integrada após CI bem-sucedida (squash merge, commit `a278446867009136e7ddf0eb8b1ea72f2c12888d`).
- Antes de abrir novo trabalho, a busca de PRs abertas não encontrou PRs pendentes.
- Segundo lote adiciona 11 talentos à lista, levando-a de 41 para 52 entradas: Ataque Desarmado Aprimorado, Lutar às Cegas, Tiro Rápido, Tiro Múltiplo, Tiro Preciso Aprimorado, Tiro em Movimento, Ataque Giratório, Fintar Aprimorado, Combate Montado, Investida Montada e Atropelar.
- Teste de catálogo atualizado para exigir pelo menos 52 entradas e verificar os nomes novos, além da unicidade.
- A lista ainda é parcial e precisa de auditoria de todos os requisitos/benefícios com a edição brasileira do Livro do Jogador 3.5. Não usar suplementos. Catálogo de magias continua pendente; ainda não foi alterado neste lote.
- Sem execução Gradle local ou teste manual Android; aguardar CI antes de merge.


## 2026-10-23 — Expansão inicial do catálogo de magias do Livro do Jogador

- PR #150 integrada após CI aprovada: catálogo de talentos passou a 52 entradas; commit de merge `8241b5daaedead7633b5a3eefda165ccdea0a0dc`.
- Antes de abrir este lote, a busca de PRs abertas não encontrou PR pendente.
- Iniciada a ampliação do catálogo de magias com oito truques adicionais: Criar Água, Detectar Veneno, Consertar, Mensagem, Abrir/Fechar, Prestidigitação, Purificar Alimentos e Bebidas e Atordoar.
- O modelo `PresetSpell` agora aceita `classLevels`, para registrar o nível específico de cada classe sem depender de um nível único. O filtro por classe e a seleção de magia usam o nível da classe selecionada; a interface também exibe os níveis por classe.
- Para Detectar Veneno, foram registrados os níveis 0 para Clérigo/Druida e 1 para Paladino/Ranger. O nível geral permanece 0 como referência para o filtro “Todas”.
- Adicionado `PresetSpellCatalogTest.kt` para verificar as novas entradas, nomes únicos, níveis válidos e correspondência entre classes e níveis.
- Este é somente o primeiro lote: o catálogo ainda está longe de conter todas as magias do Livro do Jogador. Os metadados existentes também precisam de auditoria item a item contra a edição brasileira; não tratar os dados atuais como integralmente validados.
- Sem teste Gradle local ou teste manual Android nesta etapa. Aguardar CI aprovada antes de integrar.


## Revisão complementar da PR #151 — metadados e filtro por nível

- A CI da PR #151 concluiu com sucesso: testes unitários, compilação do APK de depuração e upload do artefato passaram.
- A revisão estática, porém, encontrou pontos que exigiam correção antes do merge: Detectar Veneno não listava Mago/Feiticeiro (nível 0); a descrição não informava o teste de Sabedoria CD 20 para identificar o veneno; Purificar Alimentos e Bebidas usava alcance incorreto de 10 m em vez de 3 m; o filtro por nível usava o nível geral da magia mesmo quando a classe selecionada tem nível diferente.
- Corrigidos esses pontos na própria branch da PR: classes/níveis de Detectar Veneno, descrição com teste de identificação, alcance de Purificar Alimentos e Bebidas e filtro de nível dependente da classe selecionada. Testes de regressão ampliados para esses metadados.
- Referência técnica de conferência: SRD 3.5 / Player's Handbook, Detect Poison e Purify Food and Drink: https://www.d20srd.org/srd/spells/detectPoison.htm e https://www.d20srd.org/srd/spells/purifyFoodAndDrink.htm. A revisão de todos os metadados do catálogo continua pendente; não declarar o catálogo completo nem validado integralmente.
- Aguardar nova execução da CI após as correções antes do merge.


## 2026-10-23 — Remoção da ficha de exemplo em instalação limpa

- Removido do callback de criação do banco o cadastro automático de “Sir Valerius”. Instalações novas passam a iniciar sem fichas de exemplo.
- Mantido o estado vazio já existente em `DndMainScreen.kt`, com o botão **Criar personagem**, que abre o diálogo normal de criação.
- Não foi adicionada migração destrutiva nem exclusão automática de fichas existentes. Fichas já salvas em instalações atualizadas permanecem intactas; esta mudança atende ao comportamento de instalação limpa.
- O callback do Room permanece, sem inserir dados de demonstração.
- Aguardar CI da PR antes de integrar; teste manual em dispositivo não realizado nesta etapa.


## 2026-10-23 — Ampliação de truques do Livro do Jogador, lote 2

- Após a integração da PR #152 (remoção da ficha de exemplo em instalações limpas), não havia outras PRs abertas; iniciado um lote isolado do catálogo de magias.
- Adicionados 12 truques do Livro do Jogador 3.5: Espirro Ácido, Marca Arcana, Raio de Gelo, Desestabilizar Mortos-Vivos, Toque de Fadiga, Lampejo, Conhecer Direção, Canção de Ninar, Invocar Instrumento, Curar Ferimentos Mínimos, Infligir Ferimentos Mínimos e Virtude.
- Os níveis por classe são representados pela propriedade `classLevels` já introduzida no modelo; estes truques são de nível 0 para as classes listadas.
- O teste de catálogo verifica presença das 12 entradas, nomes únicos, níveis válidos e classes/níveis consistentes, além de disponibilidade de algumas magias específicas.
- A lista continua parcial: não cobre todos os truques nem os níveis superiores do Livro do Jogador. A nomenclatura de algumas traduções ainda precisa de confronto final com a edição brasileira; esta etapa não inclui suplementos.
- Referência de conferência das listas de truques: Player's Handbook 3.5/SRD, incluindo listas de Bardo, Clérigo, Druida e Mago/Feiticeiro: https://srd.dndtools.org/srd/meta/books/3.5/PlayersHandbook.html e https://www.d20srd.org/srd/spellLists/sorcererWizardSpells.htm.
- CI necessária antes do merge; não foi executado Gradle localmente nem teste manual no Android.


## 2026-10-23 — Ampliação de magias de 1º círculo, lote 1

- Adicionadas nove magias de 1º círculo ao catálogo: Alarme, Causar Medo, Compreender Idiomas, Enfeitiçar Pessoa, Graxa, Detectar Portas Secretas, Retirada Acelerada, Proteção contra o Mal e Detectar Mortos-Vivos.
- Registradas as classes que podem conjurá-las, além de alcance, duração, componentes, resistência à magia, testes de resistência e resumo mecânico.
- Testes de regressão ampliados para presença das novas entradas, nomes únicos e coerência de classes/níveis; verificações específicas incluem Alarme, Causar Medo e o nível de Proteção contra o Mal para Paladino.
- Este lote é parcial. A lista de magias de 1º círculo do Livro do Jogador ainda está incompleta, e a nomenclatura de cada magia deve ser confrontada com a edição brasileira antes de declarar a auditoria terminada.
- Referência de conteúdo/listas de classe para confronto mecânico: https://srd.dndtools.org/srd/meta/books/3.5/PlayersHandbook.html e https://www.d20srd.org/srd/spellLists/sorcererWizardSpells.htm. As referências em inglês não substituem a verificação da tradução brasileira.
- CI necessária antes do merge; não foi executado Gradle localmente nem teste manual no Android.


## 2026-10-23 — Ampliação de magias de 1º círculo, lote 2

- Após a integração da PR #154 com CI aprovada, iniciado o segundo lote de magias de 1º círculo, sem incluir suplementos.
- Adicionadas oito entradas: Disfarçar-se, Servo Invisível, Convocar Monstro I, Identificação, Raio do Enfraquecimento, Aumentar Pessoa, Arma Mágica e Salto.
- Testes ampliados para presença das entradas, unicidade de nomes e classes/níveis; testes específicos cobrem classes de Convocar Monstro I, Raio do Enfraquecimento, Arma Mágica e Salto.
- A nomenclatura e a redação resumida ainda exigem confronto final com a edição brasileira do Livro do Jogador; as listas SRD em inglês servem apenas como verificação mecânica auxiliar.
- A CI desta nova PR deve passar antes de qualquer merge; não houve teste manual em dispositivo nesta etapa.

- Revisão estática posterior identificou e corrigiu o alcance de Identificação para 18 m; teste específico adicionado. A CI deve validar o commit atualizado.


## 2026-10-23 — Ampliação de magias de 1º círculo, lote 3

- Adicionadas oito entradas candidatas do Livro do Jogador: Mãos Flamejantes, Enfeitiçar Animal, Emaranhar, Bom Fruto, Passos sem Pegadas, Falar com Animais, Produzir Chama e Raio Colorido.
- Testes ampliados para presença, unicidade e disponibilidade por classe; verificações específicas cobrem as listas de classe e a resistência à magia de algumas entradas.
- As descrições são resumos mecânicos, não transcrições do livro. A nomenclatura e todos os detalhes ainda precisam de confronto final com a edição brasileira.
- Esta PR não deve ser integrada até a CI passar; não foi realizado teste manual em dispositivo.

- Revisão estática do lote 3: corrigido o alcance de Mãos Flamejantes para 4,5 m e incluído teste; Enfeitiçar Animal e Emaranhar incluem também Ranger nas classes, com testes atualizados. A CI deve validar o estado final da PR.


## 2026-10-23 — Ampliação de magias de 1º círculo, lote 4

- Adicionadas oito entradas candidatas do Livro do Jogador: Favor Divino, Comando, Santuário, Escudo da Fé, Presas Mágicas, Passos Longos, Fogo das Fadas e Remover Medo.
- Testes ampliados para presença, unicidade e disponibilidade por classe; novos testes cobrem classes e algumas propriedades mecânicas do lote.
- A nomenclatura e os resumos mecânicos ainda exigem confronto final com a edição brasileira do Livro do Jogador; esta alteração não é apresentada como auditoria terminológica concluída.
- A CI da PR deve passar antes do merge; não foi realizado teste local do Gradle nem teste manual em dispositivo.


## 2026-10-23 — Ampliação de magias de 1º círculo, lote 5

- Adicionadas oito entradas candidatas do Livro do Jogador: Bênção Maldita, Abençoar Água, Desespero, Resistir aos Elementos, Pedra Mágica, Porrete Mágico, Detectar Animais ou Plantas e Ocultar-se de Animais.
- Ampliados os testes de presença, nomes únicos, classes e propriedades mecânicas selecionadas.
- Escopo continua limitado ao Livro do Jogador 3.5; as descrições são resumos funcionais, não transcrições.
- A tradução dos nomes deve ser confrontada com a edição brasileira antes de considerar a terminologia fechada. As classes e mecânicas devem ser revistas também contra as listas do PHB/SRD; referências em inglês não substituem essa conferência.
- Não considerar o catálogo completo. A CI do commit final deve passar antes de integrar; teste manual no Android continua pendente.
- Referência mecânica auxiliar: https://www.d20srd.org/srd/spells/bane.htm ; https://www.d20srd.org/srd/spells/blessWater.htm ; https://www.d20srd.org/srd/spells/doom.htm ; https://www.d20srd.org/srd/spells/endureElements.htm ; https://www.d20srd.org/srd/spells/magicStone.htm ; https://www.d20srd.org/srd/spells/shillelagh.htm ; https://www.d20srd.org/srd/spells/detectAnimalsOrPlants.htm ; https://www.d20srd.org/srd/spells/hideFromAnimals.htm.


## 2026-10-23 — Revisão de nomes e magias de 1º círculo, lote 6

- Corrigidos dois nomes provisórios do lote 5: “Bênção Maldita” para “Perdição” e “Resistir aos Elementos” para “Suportar Elementos”. A terminologia ainda deve ser conferida diretamente na edição brasileira do Livro do Jogador.
- Adicionadas oito magias candidatas: Amaldiçoar Água, Escudo Entrópico, Ocultar-se de Mortos-Vivos, Vigília da Morte, Detectar Bem, Detectar Mal, Detectar Caos e Detectar Ordem.
- Testes ampliados para presença, unicidade, classes e propriedades mecânicas selecionadas.
- A lista de classes por magia e alguns campos mecânicos precisam de revisão final com o Livro do Jogador; nomes traduzidos de fontes secundárias não são considerados prova definitiva.
- Não realizar merge sem CI verde para o SHA mais recente.
- Referências mecânicas auxiliares: https://www.d20srd.org/srd/spells/curseWater.htm ; https://www.d20srd.org/srd/spells/entropicShield.htm ; https://www.d20srd.org/srd/spells/hideFromUndead.htm ; https://www.d20srd.org/srd/spells/deathwatch.htm ; https://www.d20srd.org/srd/spells/detectGood.htm ; https://www.d20srd.org/srd/spells/detectEvil.htm ; https://www.d20srd.org/srd/spells/detectChaos.htm ; https://www.d20srd.org/srd/spells/detectLaw.htm.


## 2026-10-23 — Magias de proteção contra tendências, lote 7

- Acrescentadas três entradas candidatas de 1º círculo: Proteção contra o Caos, Proteção contra o Bem e Proteção contra a Lei.
- Testes cobrem presença indireta por consulta ao catálogo, listas de classe, alcance, duração, resistência à magia e unicidade global dos nomes.
- As listas de classe e a terminologia permanecem provisórias até confronto direto com a edição brasileira do Livro do Jogador 3.5; SRD em inglês é apenas referência mecânica.
- Não considerar concluída a cobertura de magias de 1º círculo. Exigir CI verde no SHA final antes de integrar; teste manual Android continua pendente.
- Referências mecânicas auxiliares: https://www.d20srd.org/srd/spells/protectionFromChaos.htm ; https://www.d20srd.org/srd/spells/protectionFromGood.htm ; https://www.d20srd.org/srd/spells/protectionFromLaw.htm.


## 2026-10-23 — Magias de natureza de 1º círculo, lote 8

- Adicionadas três entradas candidatas ao catálogo do Livro do Jogador: Acalmar Animais, Detectar Laços e Covas e Convocar Aliado da Natureza I.
- Acrescentados testes para classes, alcance, duração, tempo de execução e resistência à magia.
- Nomes localizados, redações resumidas e listas de classe permanecem provisórios até conferência direta com a edição brasileira do Livro do Jogador 3.5; a SRD é somente referência mecânica auxiliar.
- Não considerar completa a lista de magias de 1º círculo. Exigir CI verde no SHA final antes do merge; não houve teste manual em dispositivo nesta etapa.
- Referências mecânicas auxiliares: https://www.d20srd.org/srd/spells/calmAnimals.htm ; https://www.d20srd.org/srd/spells/detectSnaresAndPits.htm ; https://www.d20srd.org/srd/spells/summonNaturesAllyI.htm.


## 2026-10-23 — Magias arcanas de 1º círculo, lote 9

- Integrado o lote anterior (#161) após CI verde; foram adicionadas cinco entradas candidatas: Animar Corda, Ataque Certeiro, Hipnotismo, Montaria e Imagem Silenciosa.
- Testes cobrem classes, alcance/duração e resistência à magia para propriedades selecionadas.
- Descrições resumidas, componentes e terminologia ainda exigem revisão direta na edição brasileira do Livro do Jogador 3.5. A lista não é considerada completa; alguns campos de duração/efeitos foram resumidos para caber no modelo atual.
- Exigir CI verde para o SHA final desta PR antes do merge. Teste manual em dispositivo Android não realizado.
- Referências mecânicas auxiliares: https://www.d20srd.org/srd/spells/animateRope.htm ; https://www.d20srd.org/srd/spells/trueStrike.htm ; https://www.d20srd.org/srd/spells/hypnotism.htm ; https://www.d20srd.org/srd/spells/mount.htm ; https://www.d20srd.org/srd/spells/silentImage.htm.


### Revisão posterior do lote 9

- Corrigido o foco de Ataque Certeiro: pequena réplica de um alvo de arquearia, não de um arco.
- Corrigido o tempo de execução de Hipnotismo para 1 rodada; teste automatizado acrescentado para esse campo.
- As correções foram feitas após a primeira execução de CI; exigir nova CI verde no SHA final antes do merge.


## 2026-10-23 — Magias arcanas de 1º círculo, lote 10

- Adicionadas três entradas candidatas: Disco Flutuante, Reduzir Pessoa e Toque Chocante.
- Incluídos testes para listas de classe e propriedades mecânicas selecionadas.
- Os nomes e resumos são provisórios até conferência direta com o Livro do Jogador brasileiro; as referências SRD servem apenas como apoio mecânico.
- Não integrar antes da CI passar no SHA final e da revisão do estado da PR. Teste manual em dispositivo Android não realizado.
- Referências mecânicas auxiliares: https://www.d20srd.org/srd/spells/floatingDisk.htm ; https://www.d20srd.org/srd/spells/reducePerson.htm ; https://www.d20srd.org/srd/spells/shockingGrasp.htm.


## 2026-10-23 — Magias arcanas de 1º círculo, lote 11

- Adicionadas três entradas candidatas: Trancar Portal, Ventriloquismo e Aura Mágica de Nystul.
- Incluídos testes para listas de classe, alcance, duração e resistência à magia.
- Os nomes localizados e resumos são provisórios até conferência direta com a edição brasileira do Livro do Jogador 3.5; SRD em inglês é referência mecânica auxiliar, não autoridade para a tradução.
- Não integrar antes de CI verde no SHA final e nova verificação do estado da PR. Teste manual em dispositivo Android não realizado.
- Referências mecânicas auxiliares: https://www.d20srd.org/srd/spells/holdPortal.htm ; https://www.d20srd.org/srd/spells/ventriloquism.htm ; https://www.d20srd.org/srd/spells/nystulsMagicAura.htm.


## 2026-10-23 — Magias arcanas de 1º círculo, lote 12

- Adicionadas duas entradas candidatas: Apagar e Toque Gélido.
- Incluídos testes para listas de classe, duração/alcance e resistência à magia.
- A tradução oficial brasileira de “Erase” e “Chill Touch”, bem como a redação resumida dos efeitos, precisa de conferência direta no Livro do Jogador 3.5; SRD em inglês é referência mecânica auxiliar.
- Não integrar antes de CI verde no SHA final e nova verificação do estado da PR. Teste manual Android não realizado.
- Referências mecânicas auxiliares: https://www.d20srd.org/srd/spells/erase.htm ; https://www.d20srd.org/srd/spells/chillTouch.htm.


## 2026-10-23 — Revisão mecânica de magias de 1º círculo

- Corrigido o campo de resistência à magia de **Suportar Elementos**: a magia não permite resistência à magia.
- Acrescentado teste de regressão para evitar que o campo volte a indicar resistência.
- A conferência de cobertura das magias de 1º círculo continua em andamento; as descrições localizadas devem ser comparadas diretamente com o Livro do Jogador brasileiro. A SRD é referência mecânica auxiliar.
- Referência mecânica auxiliar: https://www.d20srd.org/srd/spells/endureElements.htm.
- Não integrar antes de CI verde no SHA final e revalidação da PR; teste manual Android não realizado.


---

## Índice de continuidade e prevenção de redundâncias — catálogo de magias do Livro do Jogador

**Atualizado em 2026-10-23.** Esta seção é um índice rápido para consultas futuras; as entradas cronológicas acima continuam sendo a trilha detalhada. Antes de abrir uma PR sobre magias, ler este índice e procurar o nome/efeito no catálogo e no histórico de PRs.

### Escopo e regras de trabalho vigentes

- O objetivo atual é auditar e completar o catálogo de magias do **Livro do Jogador D&D 3.5**, começando pelas magias de 1º círculo; talentos serão retomados depois desta etapa, conforme combinado.
- A edição brasileira do *Livro do Jogador* é a autoridade para nomes, terminologia e descrição localizada. A SRD em inglês pode auxiliar na mecânica, mas **não comprova a tradução oficial brasileira**.
- As entradas adicionadas são candidatas enquanto não houver conferência direta da fonte brasileira. Não declarar a cobertura completa só porque existem testes ou porque a CI passou.
- Antes de adicionar uma magia, pesquisar o catálogo atual por nome e possíveis variantes/duplicatas; conferir também listas de classe, nível, escola, componentes, tempo de execução, alcance, duração, resistência e teste de resistência.
- Se uma magia já existir, corrigir o registro existente em vez de criar outra entrada. Não abrir PR repetindo lotes já integrados. Alterações de mecânica devem ter teste de regressão.
- A CI verde confirma apenas as verificações automatizadas executadas naquele commit. Não equivale a teste manual no Android nem à validação editorial contra o livro.

### Lotes recentes já integrados

- **PR #162 — lote 9:** Animar Corda, Ataque Certeiro, Hipnotismo, Montaria e Imagem Silenciosa. Ajustes registrados: foco de Ataque Certeiro é uma pequena réplica de alvo de arquearia; tempo de execução de Hipnotismo é 1 rodada.
- **PR #163 — lote 10:** Disco Flutuante, Reduzir Pessoa e Toque Chocante.
- **PR #164 — lote 11:** Trancar Portal, Ventriloquismo e Aura Mágica de Nystul.
- **PR #165 — lote 12:** Apagar e Toque Gélido.
- Esses lotes não devem ser recriados como novas adições. Suas classes e campos ainda podem receber correções pontuais se uma comparação confiável identificar erro real; nesse caso, citar o lote original e adicionar teste em vez de duplicar a magia.

### Correções de catálogo integradas

- **PR #167 — Corrige nível de Curar Ferimentos Leves para Ranger:** integrada em 2026-10-24 no commit `34ae71115e5ffca3b1d1091489e9bfb0acb059cb`; CI Android #993 passou no SHA `4110ce6b37c97211e3d69caf7535993d64351b7a`. Adiciona `classLevels` (Clérigo 1, Druida 1, Bardo 1, Paladino 1, Ranger 2), corrige o indicador do catálogo para classe selecionada e inclui teste de regressão. Conferência direta com o Livro do Jogador brasileiro continua pendente.

### Correção de catálogo integrada anteriormente

- **PR #166 — Corrige resistência à magia de Suportar Elementos:** altera `spellResistance` para `false` e adiciona teste de regressão. A SRD indica que a magia não permite resistência à magia: https://www.d20srd.org/srd/spells/endureElements.htm.
- **Integrada em 2026-10-23** no commit `1668ebe7141a7b53d5abd1bddfe79eab6a71746e`; CI Android #986 concluiu com sucesso no SHA da PR `0cbed7f70c74d4d2d74a9ef064568c4bfcd03297`. Não houve teste manual em dispositivo.

### Contagem observada e limites

- Na inspeção do código remoto após a PR #165, foram encontradas **77 entradas de nível 1** no catálogo, contando entradas por nome de `PresetSpell`. Essa contagem é uma fotografia daquele commit, não uma afirmação de cobertura integral do *Livro do Jogador*.
- O número inclui somente registros reconhecidos pelo padrão de código usado na contagem; a cobertura oficial, os nomes localizados, as classes e todos os campos mecânicos ainda precisam de auditoria sistemática.
- O catálogo contém entradas de níveis superiores e truques; esta fase não deve expandir escopo para suplementos além do *Livro do Jogador*.

### Próxima sequência recomendada

1. PR #166 integrada com CI verde; a correção está na `main`.
2. Continuar a auditoria de 1º círculo por comparação sistemática com o *Livro do Jogador*, verificando primeiro ausências reais e depois os campos das entradas existentes.
3. Consultar este índice e os registros por lote antes de cada nova PR; agrupar alterações pequenas e coerentes, com testes e atualização do log na mesma PR.
4. Só depois de concluir e registrar a etapa de magias, retomar a auditoria de talentos conforme o escopo combinado.


---

## 2026-10-23 — Níveis de magia específicos por classe

- Durante a revisão do modelo de níveis por classe, foi identificado que **Curar Ferimentos Leves** estava cadastrado como nível 1 para todas as classes, embora seja nível 2 para Ranger na lista de classe da SRD 3.5.
- O modelo `PresetSpell` já possui `classLevels`, e a tela de catálogo já usa esse mapa para filtrar e criar a magia com o nível correto quando uma classe específica está selecionada. Porém, a entrada de Curar Ferimentos Leves não fornecia o mapa explícito.
- Corrigida a entrada para registrar Clérigo 1, Druida 1, Bardo 1, Paladino 1 e Ranger 2.
- Corrigido o indicador de nível no cartão do catálogo para mostrar o nível da classe selecionada, em vez de sempre mostrar o campo genérico `preset.level`.
- Adicionado teste de regressão `cureLightWoundsUsesClassSpecificSpellLevel`.
- Referência mecânica auxiliar: lista de magias SRD 3.5, que registra Curar Ferimentos Leves como magia de 2º nível para Ranger: https://www.d20srd.org/indexes/spellLists.htm.
- A SRD não confirma o nome oficial em português; conferir a redação e a lista brasileira do *Livro do Jogador* continua pendente.
- Contagem do catálogo de nível 1 não deve ser interpretada como cobertura completa. Continuar a comparação por classe e revisar todas as magias com níveis divergentes.
- **PR #167 integrada em 2026-10-24** no commit `34ae71115e5ffca3b1d1091489e9bfb0acb059cb`; CI Android #993 concluiu com sucesso no SHA `4110ce6b37c97211e3d69caf7535993d64351b7a`. Não houve teste manual em dispositivo Android.


---

## 2026-10-24 — Correções pontuais de descrição de magias

- **Escudo Arcano:** corrigida a descrição que começava incorretamente com “Disco flutuante”, confundindo o efeito de Escudo Arcano com a magia Disco Flutuante. A descrição agora registra o disco invisível de força, o bônus de escudo de +4 na CA e a anulação de Mísseis Mágicos. A entrada existente foi corrigida; nenhuma magia duplicada foi criada.
- **Queda Suave:** corrigida a taxa de queda da descrição, de 3m por rodada para aproximadamente 18m por rodada (60 pés), mantendo a informação de que o dano de queda é anulado enquanto o efeito durar. A resistência à magia e o teste de resistência existentes foram preservados; o campo booleano atual não expressa exceções específicas a objetos/alvos.
- Adicionados testes de regressão para as duas descrições e para a preservação dos campos de resistência/teste de Queda Suave.
- Referências mecânicas auxiliares: Escudo Arcano / Shield — https://dndtools.org/m/spells/players-handbook-v35--6/shield--2364/ ; Queda Suave / Feather Fall — https://www.dndtools.org/spells/players-handbook-v35--6/feather-fall--2813/ e https://www.d20srd.org/srd/spells/featherFall.htm.
- Estas referências sustentam a mecânica, mas não substituem a conferência de nomes e redação no *Livro do Jogador* brasileiro. Não houve teste manual em dispositivo Android.


---

## 2026-10-24 — Conferência da lista de magias de Bardo no Livro do Jogador

- **Apagar:** corrigida a lista de classes para incluir Bardo. A lista “Magias de 1º nível de Bardo” do *Livro do Jogador* brasileiro inclui explicitamente Apagar; o catálogo da aplicação havia cadastrado apenas Mago e Feiticeiro.
- Atualizado o teste de regressão existente para verificar as três classes. A entrada existente foi corrigida, sem duplicação.
- Fonte primária consultada: arquivo fornecido “D&D 3.5 - Livro do Jogador”, capítulo 11, seção “Magias de 1º nível de Bardo” (página impressa 155/156 na extração do PDF).
- Esta verificação é diretamente apoiada pelo livro em português; a auditoria da lista completa de magias de Bardo continua em andamento.


---

## 2026-10-24 — Nomes oficiais conferidos no Livro do Jogador brasileiro

- **Animar Cordas:** corrigido o nome do catálogo, antes registrado no singular como “Animar Corda”. A lista “Magias de 1º nível de Bardo” do livro usa “Animar Cordas”.
- **Invocar Criaturas I:** corrigido o nome do catálogo, antes registrado como “Convocar Monstro I”. A lista de magias do livro usa “Invocar Criaturas I”; foi renomeada a entrada existente, sem criar duplicata.
- Atualizados os testes de regressão que consultavam os nomes antigos.
- Fonte primária: arquivo fornecido “D&D 3.5 - Livro do Jogador”, capítulo 11, listas de magias e índice de magias.
- Esta alteração cobre nomes e consistência do catálogo; a auditoria completa das descrições e dos níveis por classe continua em andamento. Nenhum teste manual em dispositivo Android foi realizado nesta etapa.


---

## 2026-10-24 — Auditoria da lista de magias de 1º nível de Bardo

- Conferida diretamente no *Livro do Jogador* brasileiro a lista de magias de 1º nível de Bardo (capítulo 11, páginas impressas 155–156).
- Adicionadas entradas ausentes: **Boca Encantada**, **Confusão Menor**, **Dissimular Tendência**, **Obscurecer Objeto**, **Recuo Acelerado**, **Riso Histérico de Tasha, O** e **Transformação Momentânea**.
- Renomeada a entrada existente **Graxa** para **Área Escorregadia**, nome usado na lista de magias do livro; a entrada foi ampliada para incluir Bardo.
- Incluídos níveis por classe nas magias com níveis diferentes para Bardo, Clérigo, Mago e Feiticeiro; adicionados testes de regressão para presença, níveis e ausência de duplicata.
- Fonte primária: arquivo fornecido “D&D 3.5 - Livro do Jogador”, listas de magias de Bardo e descrições das magias. A auditoria de componentes, descrições completas e listas das demais classes continua em andamento.
- Nenhum teste manual em dispositivo Android foi realizado nesta etapa; CI será a validação automatizada.


---

## 2026-10-23 — Correção de Dissimular Tendência com base no Livro do Jogador

- A conferência direta da entrada no capítulo 11 identificou dados incorretos na entrada existente: a lista de classes havia incluído Mago e Feiticeiro e omitido Paladino; o alcance estava como Toque em vez de Curto; os componentes estavam abreviados incorretamente como V, S.
- Corrigida a lista para **Bardo 1, Clérigo 2 e Paladino 2**, alcance para **Curto (7,5 m + 1,5 m/2 níveis)** e componentes para **V, G**. A descrição foi ajustada para refletir que a magia esconde a tendência de criatura ou objeto contra todas as formas de adivinhação.
- Adicionados testes de regressão para classes, níveis por classe, alcance e componentes. Entrada existente corrigida; nenhuma duplicata criada.
- Fonte primária: arquivo fornecido “D&D 3.5 - Livro do Jogador”, capítulo 11, verbete “Dissimular Tendência” (página impressa 196).
- O restante dos campos e das magias adicionadas no lote anterior continua sujeito a revisão individual; CI não substitui teste manual no Android.


---

## 2026-10-23 — Revisão mecânica de Área Escorregadia

- Conferida a descrição de **Área Escorregadia** diretamente no verbete do Livro do Jogador brasileiro (capítulo 11, página impressa 173).
- Corrigidos os componentes de **V, S, M** para **V, G, M**; duração para **1 rodada/nível (D)**; e teste de resistência para **Reflexos anula (veja texto)**. A descrição agora distingue a área de 3 m quadrados do uso sobre um objeto e registra os testes repetidos, a movimentação reduzida com Equilíbrio e a possibilidade de derrubar objeto em uso.
- Mantidas as classes Bardo, Mago e Feiticeiro e a ausência de resistência à magia, coerentes com a entrada do livro. A entrada existente foi corrigida; nenhuma duplicata criada.
- Adicionados testes de regressão para componentes, duração, resistência, teste de resistência e informação de área.
- Nenhum teste manual em dispositivo Android foi realizado nesta etapa.


---

## 2026-10-23 — Revisão mecânica de Confusão Menor

- Conferido o verbete **Confusão Menor** no PDF brasileiro do Livro do Jogador, capítulo 11 (texto extraído do verbete na página impressa 185).
- Corrigidos os componentes de **V, S** para **V, G, FD**; removido o teste de resistência que não consta no verbete; e corrigida a resistência à magia para **Não**. A descrição agora especifica uma única criatura viva e remete à magia Confusão para a condição exata.
- Mantida a entrada única de Bardo 1, alcance curto e duração de 1 rodada. Adicionados testes de regressão e atualizado este log.
- Nenhum teste manual em dispositivo Android foi realizado nesta etapa.

---

## 2026-10-23 — Revisão de Obscurecer Objeto e convenção de componentes

- Conferido o verbete **Obscurecer Objeto** no Livro do Jogador brasileiro, capítulo 11, página impressa 158.
- Mantidos os componentes já registrados na ficha sem tratar **somático (S)** e **gestual (G)** como diferenças mecânicas: são termos equivalentes para o mesmo componente. Nesta auditoria, a terminologia do livro é a referência; a escolha de rótulo no aplicativo deve ser consistente, sem alterações em massa que não corrijam regra alguma.
- A descrição foi detalhada para registrar o limite de **50 kg por nível** e os dois resultados contra adivinhação: falha automática quando o objeto protegido é o alvo e incapacidade de senti-lo quando a magia procura outro alvo próximo.
- Mantidos classes e níveis (Bardo 1, Clérigo 3, Mago 2 e Feiticeiro 2), alcance Toque, duração 8 horas (D), teste de Vontade anula (objeto) e resistência à magia Sim (objeto), conforme o verbete.
- Adicionados testes de regressão para esses campos e para os detalhes da descrição. Nenhum teste manual em dispositivo Android foi realizado nesta etapa.

---

## 2026-10-23 — Duplicata de Recuo Acelerado

- Conferido o catálogo de magias de 1º nível com o índice e o verbete do Livro do Jogador brasileiro. O nome oficial encontrado no livro é **Recuo Acelerado** (Brd 1, Fet/Mag 1); não há uma entrada distinta chamada **Retirada Acelerada** no catálogo consultado.
- Removida a entrada redundante **Retirada Acelerada**, que duplicava o efeito de Recuo Acelerado e não registrava Bardo como classe. Mantida a entrada canônica com Bardo, Mago e Feiticeiro e níveis estruturados.
- Adicionado teste de regressão para impedir que a duplicata volte ao catálogo. Essa remoção altera apenas o catálogo predefinido; não foi realizada migração de fichas salvas que já contenham o texto antigo. Nenhum teste manual em dispositivo Android foi realizado nesta etapa.

---

## 2026-10-23 — Expansão dos truques de feiticeiro e mago

- Comparadas as listas de truques e os verbetes do Livro do Jogador brasileiro: adicionadas as entradas ausentes **Brilho** (Brd/Drd/Fet/Mag 0), **Pasmar** (Brd/Fet/Mag 0), **Raio de Ácido** (Fet/Mag 0) e **Romper Morto-Vivo** (Fet/Mag 0).
- Registrados componentes, alcance, duração, testes de resistência, resistência à magia e efeitos conforme os verbetes do capítulo 11 (Brilho, p. 178; Pasmar, p. 226; Raio de Ácido e Romper Morto-Vivo, pp. 235–236).
- Adicionados testes de regressão para unicidade, nível, classes e campos mecânicos essenciais.
- Esta é uma expansão parcial do catálogo, não uma confirmação de completude do Livro do Jogador. Nenhum teste manual em dispositivo Android foi realizado nesta etapa.

---

## 2026-10-23 — Nome oficial de Orientação

- Conferido o verbete **Orientação** no Livro do Jogador brasileiro, p. 225: Clérigo 0/Druida 0, componentes V, G, alcance Toque, duração 1 minuto ou até ser descarregada, Vontade anula (inofensiva) e resistência à magia Sim.
- Renomeada a entrada predefinida de **Orientação Divina** para **Orientação**, conforme a nomenclatura da referência. Atualizados os componentes e o texto do teste de resistência para acompanhar o verbete.
- Criado teste de regressão para o nome, nível, classes e campos mecânicos.
- Não foi encontrada referência a esse nome nos testes automatizados anteriores. A mudança afeta o nome mostrado ao selecionar a magia predefinida; fichas já salvas com o texto antigo não foram migradas. Nenhum teste manual em dispositivo Android foi realizado nesta etapa.


---

## 2026-10-24 — Nomes e classes dos truques de detecção/direção

- Conferidas as listas de magias e os verbetes do Livro do Jogador brasileiro (capítulo 11): os nomes usados são **Intuir Direção** e **Detectar Venenos**.
- Renomeada a entrada **Conhecer Direção** para **Intuir Direção** e **Detectar Veneno** para **Detectar Venenos**, sem criar entradas duplicadas.
- Corrigida a lista de classes de Detectar Venenos: Clérigo 0, Druida 0, Mago 0, Feiticeiro 0 e Paladino 1. Ranger não consta no verbete/lista correspondente do livro.
- Ajustados os componentes para a notação portuguesa do livro (**V, G**). Como já estabelecido na auditoria, a diferença de abreviação S/G não altera a mecânica.
- Atualizados os testes de catálogo e de níveis por classe. Os nomes antigos deixam de ser usados nas opções predefinidas; fichas já salvas com esses nomes não são migradas automaticamente. Nenhum teste manual em dispositivo Android foi realizado nesta etapa.


---

## 2026-10-24 — Deduplicação e nomes canônicos de truques

- Comparados os nomes e efeitos com as listas e descrições do capítulo 11 do Livro do Jogador brasileiro.
- Removidas quatro entradas não canônicas que duplicavam efeitos já presentes sob os nomes do livro: **Atordoar** (Pasmar), **Espirro Ácido** (Raio de Ácido), **Desestabilizar Mortos-Vivos** (Romper Morto-Vivo) e **Lampejo** (Brilho).
- Corrigidos os nomes **Toque de Fadiga** para **Toque da Fadiga** e **Purificar Alimentos e Bebidas** para **Purificar Alimentos**. Ajustados os componentes desses verbetes para V, G, M e V, G, respectivamente.
- Atualizados os testes para impedir o retorno dos aliases e verificar a presença única dos nomes canônicos. As mudanças afetam as opções predefinidas; fichas salvas com nomes antigos não são migradas automaticamente. Nenhum teste manual em dispositivo Android foi realizado nesta etapa.


---

## 2026-10-24 — Magias de 1º nível ausentes nas listas de bardo

- Confrontada a lista de magias de 1º nível de Bardo do capítulo 11 com o catálogo predefinido do aplicativo.
- Adicionadas **Apagar**, **Aura Mágica de Nystul**, **Imagem Silenciosa** e **Ventriloquismo**, ausentes do catálogo, com níveis por classe, componentes, alcance, duração, resistência e resumo dos efeitos conforme os verbetes do livro.
- Acrescentados testes de regressão para existência única, nível, classes e campos importantes.
- A expansão é parcial: ainda é necessário confrontar as listas de 1º nível das demais classes e níveis superiores. Nenhum teste manual em dispositivo Android foi realizado nesta etapa.


---

## Complemento — correção da PR #183 após falhas do CI

- O CI detectou que **Apagar**, **Aura Mágica de Nystul**, **Imagem Silenciosa** e **Ventriloquismo** já existiam no catálogo. A proposta inicial criou duplicatas, portanto não deve ser integrada como estava.
- Removidas as quatro novas duplicatas e aproveitadas as entradas preexistentes, adicionando nelas os níveis por classe ausentes.
- Ajustados os testes para validar as entradas únicas e respeitar a notação dos componentes já registrada no catálogo.
- A PR #183 permanece aberta até o CI ser executado novamente e passar.


---

## 2026-10-24 — Magias ausentes na lista de 1º nível de Clérigo

- Conferida a lista **Magias de 1º nível de Clérigo** no capítulo 11 do PDF brasileiro fornecido (página impressa 157) e os verbetes das magias.
- Adicionadas ao catálogo, após busca por nomes para evitar duplicatas: **Auxílio Divino**, **Invisibilidade Contra Mortos-Vivos**, **Pedra Encantada** e **Visão da Morte**.
- Registrados os níveis por classe, componentes, alcance, duração, teste de resistência, resistência à magia e resumo dos efeitos conforme os verbetes do livro. **Auxílio Divino** também consta como Paladino 1; **Pedra Encantada** como Druida 1 além de Clérigo 1.
- Acrescentados testes de regressão para presença única, níveis, classes e campos mecânicos importantes.
- A extração de texto do PDF distorceu o nome de **Invisibilidade Contra Mortos-Vivos** na lista, mas o título e a entrada do verbete na página impressa 209 confirmam essa grafia; foi usado o nome do verbete, não a leitura OCR incorreta.
- Esta PR cobre apenas quatro ausências confirmadas; ainda é necessário comparar integralmente as listas de 1º nível das demais classes e verificar os campos de cada entrada. CI e testes automatizados não substituem teste manual no Android.


---

## 2026-10-24 — Corrige nome e parâmetros de Maldição Menor

- Conferidos a lista de magias de 1º nível de Clérigo (página impressa 157) e o verbete Maldição Menor (página impressa 215) no PDF brasileiro enviado.
- A entrada anteriormente chamada Perdição correspondia ao efeito de Maldição Menor, mas usava nome e alcance incompatíveis com o verbete. Corrigidos nome, escola detalhada, componentes (V, G, FD), alcance fixo de 15 m e descrição do efeito, incluindo a contramágica contra Bênção.
- A mudança foi feita na entrada existente para evitar uma segunda cópia; teste atualizado e teste de regressão acrescentado.
- A grafia Perdição não aparece como nome dessa magia na lista consultada; não manter a entrada com esse nome sem localizar outro verbete correspondente no livro.
- A PR #184 adicionou Invisibilidade Contra Mortos-Vivos, nome confirmado pelo verbete do livro; a lista impressa usa uma forma distorcida pelo OCR, não uma magia distinta.


---

## 2026-10-24 — Nomes canônicos na lista de 1º nível de Druida

- Conferidas as listas de 1º nível de Druida e Ranger no Livro do Jogador brasileiro (capítulo 11; páginas impressas 162–164) e comparadas com o catálogo existente.
- Renomeadas as entradas existentes **Emaranhar** para **Constrição**, **Presas Mágicas** para **Presa Mágica** e **Convocar Aliado da Natureza I** para **Invocar Aliado da Natureza I**, conforme a nomenclatura das listas do livro. As entradas foram renomeadas, sem duplicação.
- Removida a entrada duplicada **Pedra Mágica**, cujo efeito já está coberto por **Pedra Encantada**; mantida a entrada canônica com Druida e Clérigo.
- Atualizados os testes para os nomes canônicos e acrescentada regressão contra os aliases/duplicata. Outras magias de Druida ainda exigem conferência dos campos mecânicos; este lote não declara concluída a auditoria da classe.
- Fonte primária: PDF brasileiro “D&D 3.5 - Livro do Jogador” fornecido nesta conversa. Nenhum teste manual em dispositivo Android foi realizado nesta etapa.


---

## 2026-10-23 — Magias adicionais de 1º nível (feiticeiro/mago)

- Consultada a lista de magias de 1º nível de feiticeiro e mago e os verbetes correspondentes no Livro do Jogador brasileiro.
- Adicionadas **Trancar Portal**, **Disco Flutuante de Tenser**, **Leque Cromático** e **Montaria Arcana**, com nível por classe, componentes, tempo de execução, alcance, duração, resistência à magia e efeito resumido.
- Na revisão pós-CI, corrigido o nome para **Trancar Portal** e completadas as durações escalonadas de Leque Cromático (2d4/1d4 rodadas conforme DV). A checagem de duplicatas encontrou uma entrada antiga de Trancar Portal; a nova duplicata foi removida e a entrada canônica existente foi atualizada com os campos mecânicos revisados. Os testes de regressão foram ampliados.
- Adicionados testes de regressão para unicidade, nível, classes, níveis por classe e campos mecânicos centrais.
- A auditoria da lista arcana de 1º nível continua incompleta; esta alteração não declara o catálogo integral. Não foi realizado teste manual em dispositivo Android.

- CI posterior identificou uma expectativa de teste desatualizada para **Trancar Portal** (espaçamento do alcance e indicação de duração dissipável `(D)`), não uma falha de compilação do catálogo. As asserções foram alinhadas aos campos canônicos da magia; aguardar nova CI antes de integrar.


---

## 2026-10-24 — Consolidação de magias arcanas duplicadas semanticamente

- Após a integração da PR #187, a revisão semântica do catálogo identificou que **Disco Flutuante de Tenser** duplicava a entrada preexistente **Disco Flutuante**, e **Montaria Arcana** duplicava a magia já catalogada como **Montaria**. A verificação anterior cobria nomes repetidos literalmente, mas não equivalência de nomes/efeitos.
- A correção proposta mantém uma única entrada por magia: padroniza a entrada de Disco Flutuante como **Disco Flutuante de Tenser**, remove a nova duplicata e enriquece a descrição mecânica da entrada canônica; remove **Montaria Arcana** e atualiza **Montaria** com alcance, duração dissipável, descrição e níveis por classe.
- Os testes passam a verificar que os nomes redundantes não existem e que **Montaria** mantém a disponibilidade para Bardo, Mago e Feiticeiro. A CI desta correção ainda precisa ser aprovada antes do merge.
- Lição de auditoria: além de unicidade textual, comparar nomes alternativos, classes, componentes e efeitos para evitar duplicação semântica. O catálogo de magias arcanas de 1º nível permanece em auditoria; não declarar a lista completa.


- **Atualização de CI (PR #188):** a primeira execução falhou em dois testes de lotes anteriores que ainda esperavam os dados antigos: `Montaria` sem `(D)` na duração e `Disco Flutuante` como nome. Os testes foram atualizados para refletir a entrada canônica consolidada (`Montaria`, duração `2 horas/nível (D)`, e `Disco Flutuante de Tenser`). Isso é ajuste das expectativas de teste ao catálogo proposto; a CI precisa ser executada novamente antes de integrar.


---

## 2026-10-24 — Padronização das abreviações de componentes mágicos em português

- A revisão do catálogo encontrou nomenclatura inconsistente nos componentes: `S` (abreviação inglesa de somático) misturada com `G` (gestual, em português), e `DF` misturado com `FD` (foco divino).
- A alteração proposta padroniza os campos `components` do catálogo para `V, G, M, F, FD` conforme aplicável, sem mudar as regras mecânicas das magias. As expectativas correspondentes nos testes são ajustadas na mesma mudança.
- Esta é uma correção terminológica transversal; a CI deve confirmar que nenhum teste depende das abreviações anteriores. A auditoria de cobertura das magias de 1º nível continua separada e incompleta.


---

## 2026-10-24 — Cobertura de magias de Paladino e Ranger

- Comparadas as listas de magias de 1º nível de Paladino e Ranger com as entradas do catálogo.
- Adicionada **Abençoar Arma** (Paladino 1), que estava ausente.
- Corrigidos os níveis por classe: **Ler Magias** agora registra Paladino 1 e Ranger 1; **Criar Água**, **Resistência** e **Virtude** registram Paladino 1; **Detectar Mortos-Vivos** passa a incluir Paladino; **Restauração Menor** passa a incluir Ranger 1 além de Clérigo 2, Druida 2 e Paladino 1.
- Corrigido **Curar Ferimentos Leves**: Ranger 1, não Ranger 2.
- Ajustados componentes de foco divino e o teste de resistência de **Virtude** conforme a referência; adicionados testes de regressão para níveis por classe e presença única.
- Referências de conferência: *Livro do Jogador* brasileiro, capítulo de Magias/listas de Paladino e Ranger; listas e verbetes SRD D&D 3.5 usadas como verificação cruzada. A auditoria de cobertura de outras classes e níveis continua em andamento; nenhum teste manual em dispositivo Android foi realizado nesta etapa.


- **Complemento da auditoria de Ranger:** incluídas **Suportar Energia** (Ranger 1; Clérigo/Druida/Paladino/Mago/Feiticeiro 2) e **Retardar Envenenamento** (Ranger 1; Clérigo/Druida/Paladino 2), ambas ausentes do catálogo. **Detectar Venenos** também foi corrigida para incluir Ranger 1; o teste anterior que o excluía contrariava a lista de magias do Livro do Jogador.


---

## 2026-10-24 — Correção pós-merge: níveis de Ranger e nome oficial da magia de resistência

- A conferência direta do PDF brasileiro do *Livro do Jogador* (lista de magias de Ranger, p. 164; verbete “Resistência à Elementos”, seção de magias) contradisse duas alterações integradas na PR #190.
- **Curar Ferimentos Leves** permanece Paladino 1, mas é **Ranger 2** no livro; corrigido o mapa por classe e o teste de regressão.
- A magia chamada na PR #190 de **Suportar Energia** é, no livro, **Resistência à Elementos**. Corrigidos o nome, o nível de referência geral (2), os componentes para **V, G, FD** e o teste de resistência para **Fortitude anula (inofensiva)**; seus níveis por classe são Clérigo 2, Druida 2, Paladino 2, Ranger 1, Mago 2 e Feiticeiro 2. “Suportar Elementos” é outra magia e não deve ser confundida com esta.
- Esta correção substitui explicitamente os dados incorretos introduzidos na PR #190; a CI precisa passar antes de integrar. Fonte primária: PDF da Biblioteca do usuário, *D&D 3.5 — Livro do Jogador*, pp. 164 e verbete “Resistência à Elementos”.
