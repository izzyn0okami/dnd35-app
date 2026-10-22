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
- Nenhum merge foi realizado.

## Inspeções realizadas

### `CalculatedCharacterStats.kt`

A inspeção estática do trecho de cálculo de perícias encontrou:

- Perícias catalogadas são consideradas perícias de classe quando pertencem a qualquer uma das classes do personagem.
- Graduações são normalizadas para incrementos de 0,5 e limitadas pelo máximo calculado.
- O bônus usa `ranks.toInt()`, coerente com a decisão registrada de que meia graduação não aumenta, por si só, o modificador do teste.
- A penalidade é aplicada conforme `armorCheckPenaltyApplies` na definição da perícia.
- Natação recebe o dobro de `combinedArmorCheckPenalty`.
- `combinedArmorCheckPenalty` é calculada como `totalArmorCheckPenalty + encumbrance.armorCheckPenalty`.
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
- **Discrepância confirmada no Monge:** a lista `classSkills` em `Dnd35Rules.kt` inclui Conhecimento (Religião), mas não inclui Conhecimento (Arcano). A lista oficial do Monge inclui ambos.
- **Discrepância confirmada no Ladino:** a lista `classSkills` não inclui Profissão, embora a lista oficial do Ladino inclua essa perícia. A correção deve ser específica e acompanhada de testes de regressão para ambas as omissões; nenhuma foi aplicada nesta etapa.
- **Bardo:** a lista da aplicação representa individualmente as especialidades de Conhecimento, coerente com a regra oficial “Knowledge (all skills, taken individually)”. Não tratar automaticamente essa representação como defeito. Continuar verificando correspondência e nomes traduzidos antes de alterar.
- As demais listas de classe ainda precisam ser comparadas sistematicamente. A inspeção parcial não permite declarar a auditoria das onze classes concluída.
- Histórico consultado: PRs #5, #6, #7, #12, #24, #74, #75, #76, #77, #81, #82 e #83. As decisões anteriores sobre Acrobacia, Druida e graduações fracionárias permanecem preservadas.

### Estado e próximo passo

- Nenhum arquivo de código ou teste foi alterado; nenhum teste local foi executado nesta etapa.
- Próximo passo: corrigir a omissão de Conhecimento (Arcano) para Monge com teste de regressão, após confirmar a forma de teste existente; depois completar a comparação das outras classes e dos sinalizadores `trainedOnly`/penalidade de armadura contra o SRD.
