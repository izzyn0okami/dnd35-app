# Auditoria das perícias — D&D 3.5 em português brasileiro

## Escopo desta etapa

Esta etapa inicia a auditoria técnica das perícias usadas pela ficha. A fonte prioritária é o *Livro do Jogador* de D&D 3.5, edição brasileira da Devir. O SRD em português é auxiliar e não deve prevalecer quando divergir da tradução publicada.

## Correções aplicadas

- Confirmado que **Acrobacia** é uma perícia válida em D&D 3.5 na edição brasileira: ela corresponde à perícia *Tumble* do SRD em inglês. Não deve ser removida do catálogo nem das listas de classe. A lista e o teste existente que a reconhece estão corretos.
- Corrigida a lista de perícias de classe do **Bárbaro** para incluir **Ofício**, presente na lista da classe no Livro do Jogador. Foi adicionado teste de regressão para impedir que a perícia volte a faltar.

- O catálogo e as listas de perícias de classe passam a usar os nomes completos **Conhecimento (Arquitetura e Engenharia)** e **Conhecimento (Nobreza e Realeza)**, em vez das formas abreviadas.
- Foram adicionados testes de regressão para impedir que essas duas formas abreviadas retornem ao catálogo.

## Achados que ainda exigem auditoria antes de serem alterados

1. **Perícias de classe das 11 classes básicas:** comparar cada lista com a seção “Perícias de Classe” de cada classe e com a tabela geral de perícias. Não completar listas por inferência.
2. **Penalidade de armadura:** verificar quais perícias sofrem a penalidade e confirmar a regra de penalidade dobrada em Natação. O modelo atual guarda um indicador booleano por perícia; uma regra de multiplicador pode exigir mudança explícita no modelo e nos pontos de cálculo.
3. **Somente treinado:** conferir Acrobacia, Adestrar Animais, Conhecimento, Decifrar Escrita, Identificar Magia, Operar Mecanismo, Prestidigitação, Profissão e Usar Instrumento Mágico contra a tabela da edição. A interface e os cálculos devem respeitar o treinamento mínimo sem apagar graduações de fichas antigas.
4. **Nomes e contagem:** conferir os nomes das especializações de Conhecimento, a distinção entre a perícia Ofícios e suas especializações e a contagem do catálogo. Conhecimento possui especializações próprias, portanto a quantidade de entradas não deve ser comparada diretamente à contagem de perícias sem explicar esse critério.
5. **Compatibilidade:** antes de renomear outros valores armazenados, implementar testes ou migração para fichas existentes; nomes persistidos não devem ser alterados silenciosamente.

## Fonte consultada nesta revisão

A tabela de perícias do *Livro do Jogador* 3.5 em português mostra os nomes completos “Conhecimento (arquitetura e engenharia)” e “Conhecimento (nobreza e realeza)”, além de marcar perícias de classe, uso sem treinamento e penalidade de armadura. A reprodução online consultada é uma referência secundária e não substitui a conferência direta com o exemplar da Devir.

Referências de apoio:
- https://anyflip.com/hgohy/pjla/basic/51-100
- https://pt.scribd.com/document/446078413/D-D-3-5-Livro-do-Jogador-Original-melhor-qualidade-pdf

## Limite desta entrega

Esta é uma correção inicial, não uma certificação completa do sistema de perícias. As listas de classe, regras de treinamento e penalidades permanecem explicitamente pendentes até serem conferidas e cobertas por testes. Nenhuma alteração de cálculo de regra foi feita nesta etapa.
