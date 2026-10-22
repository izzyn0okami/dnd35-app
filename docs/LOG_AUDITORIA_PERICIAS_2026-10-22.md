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
