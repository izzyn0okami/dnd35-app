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
