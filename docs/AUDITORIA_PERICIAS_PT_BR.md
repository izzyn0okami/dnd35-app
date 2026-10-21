# Auditoria das perícias — D&D 3.5 em português brasileiro

## Escopo desta etapa

Esta etapa inicia a auditoria técnica das perícias usadas pela ficha. A fonte prioritária é o *Livro do Jogador* de D&D 3.5, edição brasileira da Devir. O SRD em português é auxiliar e não deve prevalecer quando divergir da tradução publicada.

## Correções aplicadas

- Confirmado que **Acrobacia** é uma perícia válida em D&D 3.5 na edição brasileira: ela corresponde à perícia *Tumble* do SRD em inglês. Não deve ser removida do catálogo nem das listas de classe. A lista e o teste existente que a reconhece estão corretos.
- Corrigida a lista de perícias de classe do **Bárbaro** para incluir **Ofício**, presente na lista da classe no Livro do Jogador. Foi adicionado teste de regressão para impedir que a perícia volte a faltar.
- Corrigidas também as listas de perícias de classe do **Clérigo** e do **Druida** para incluir **Ofício**, conforme as listas de classe do Livro do Jogador/SRD 3.5. Foram adicionados testes de regressão para ambas.
- Corrigidas as listas de perícias de classe do **Guerreiro**, **Monge**, **Paladino**, **Ranger** e **Ladino** para incluir **Ofício**. Foi adicionado um teste de regressão para confirmar a presença da perícia nas cinco classes.

- O catálogo e as listas de perícias de classe passam a usar os nomes completos **Conhecimento (Arquitetura e Engenharia)** e **Conhecimento (Nobreza e Realeza)**, em vez das formas abreviadas.
- Foram adicionados testes de regressão para impedir que essas duas formas abreviadas retornem ao catálogo.


- Confirmado no código que **Natação** recebe o dobro da penalidade total de armadura/carga aplicada às perícias afetadas. Adicionado teste de regressão comparando Natação (−6) com Escalar (−3) sob penalidade base de −3; a regra existente não foi alterada.

## Achados que ainda exigem auditoria antes de serem alterados

1. **Perícias de classe das 11 classes básicas:** comparar cada lista com a seção “Perícias de Classe” de cada classe e com a tabela geral de perícias. Não completar listas por inferência.
2. **Penalidade de armadura:** verificar quais perícias sofrem a penalidade e confirmar a regra de penalidade dobrada em Natação. O modelo atual guarda um indicador booleano por perícia; uma regra de multiplicador pode exigir mudança explícita no modelo e nos pontos de cálculo.
3. **Somente treinado:** conferir Acrobacia, Adestrar Animais, Conhecimento, Decifrar Escrita, Identificar Magia, Operar Mecanismo, Prestidigitação, Profissão e Usar Instrumento Mágico contra a tabela da edição. A interface e os cálculos devem respeitar o treinamento mínimo sem apagar graduações de fichas antigas.
4. **Nomes e contagem:** conferir os nomes das especializações de Conhecimento, a distinção entre a perícia Ofícios e suas especializações e a contagem do catálogo. Conhecimento possui especializações próprias, portanto a quantidade de entradas não deve ser comparada diretamente à contagem de perícias sem explicar esse critério.
5. **Compatibilidade:** antes de renomear outros valores armazenados, implementar testes ou migração para fichas existentes; nomes persistidos não devem ser alterados silenciosamente.

- Corrigida a lista de perícias de classe do **Bardo**: incluídas todas as especializações de Conhecimento, além de Decifrar Escrita, Natação, Ofício e Profissão; removida Observar, que não consta na lista básica da classe. A lista foi comparada com a referência SRD 3.5; Falar Idioma não foi incluída porque não existe como entrada de perícia no catálogo atual do aplicativo e exige decisão de modelagem própria.

## Fonte consultada nesta revisão

A tabela de perícias do *Livro do Jogador* 3.5 em português mostra os nomes completos “Conhecimento (arquitetura e engenharia)” e “Conhecimento (nobreza e realeza)”, além de marcar perícias de classe, uso sem treinamento e penalidade de armadura. A reprodução online consultada é uma referência secundária e não substitui a conferência direta com o exemplar da Devir.

Referências de apoio:
- https://anyflip.com/hgohy/pjla/basic/51-100
- https://pt.scribd.com/document/446078413/D-D-3-5-Livro-do-Jogador-Original-melhor-qualidade-pdf

## Limite desta entrega

Esta é uma correção inicial, não uma certificação completa do sistema de perícias. As listas de classe, regras de treinamento e penalidades permanecem explicitamente pendentes até serem conferidas e cobertas por testes. Nenhuma alteração de cálculo de regra foi feita nesta etapa.


## Ajuste de consistência da graduação máxima na interface

- A faixa informativa de graduação máxima da aba de perícias agora usa o nível total calculado pelo modelo (`stats.totalCharacterLevel`), a mesma fonte usada para calcular os limites efetivos. Isso evita divergência visual caso o campo legado `character.level` esteja dessincronizado dos níveis de classe salvos.
- Foi adicionado teste de regressão para o limite de graduação de personagem multiclasse. Esse teste valida o cálculo do modelo; a alteração visual é restrita à fonte do número exibido.
- A interface ainda mostra pontos de perícia investidos, mas não valida um orçamento de pontos por nível. Essa validação exige modelar os pontos recebidos em cada nível/classe e modificadores aplicáveis, sem inferir retroativamente custos a partir da ficha atual.
- **Falar Idioma** continua fora do catálogo de perícias testáveis: uma implementação correta deve ser uma seção de idiomas com escolhas conhecidas, e não uma perícia com graduações ou botão de rolagem. Como isso adiciona dados persistidos, deverá ser feito em mudança separada com migração Room e testes de preservação das fichas existentes.
