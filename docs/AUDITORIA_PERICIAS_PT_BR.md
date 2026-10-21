# Auditoria das perícias — D&D 3.5 em português brasileiro

## Escopo desta etapa

Esta etapa inicia a auditoria técnica das perícias usadas pela ficha. A fonte prioritária é o *Livro do Jogador* de D&D 3.5, edição brasileira da Devir. O SRD em português é auxiliar e não deve prevalecer quando divergir da tradução publicada.

## Correções aplicadas

- Incluída a perícia **Abrir Fechaduras** (*Open Lock*) no catálogo: usa Destreza, exige treinamento e não sofre penalidade de armadura. Incluída também na lista de perícias de classe do **Ladino**, sem adicioná-la à lista do Guerreiro. Um teste de regressão cobre esses atributos.

- Confirmado que **Acrobacia** é uma perícia válida em D&D 3.5 na edição brasileira: ela corresponde à perícia *Tumble* do SRD em inglês. Não deve ser removida do catálogo nem das listas de classe. A lista e o teste existente que a reconhece estão corretos.
- Corrigida a lista de perícias de classe do **Bárbaro** para incluir **Ofícios**, presente na lista da classe no Livro do Jogador. Foi adicionado teste de regressão para impedir que a perícia volte a faltar.
- Corrigidas também as listas de perícias de classe do **Clérigo** e do **Druida** para incluir **Ofícios**, conforme as listas de classe do Livro do Jogador/SRD 3.5. Foram adicionados testes de regressão para ambas.
- Corrigidas as listas de perícias de classe do **Guerreiro**, **Monge**, **Paladino**, **Ranger** e **Ladino** para incluir **Ofícios**. Foi adicionado um teste de regressão para confirmar a presença da perícia nas cinco classes.

- O catálogo e as listas de perícias de classe passam a usar os nomes completos **Conhecimento (Arquitetura e Engenharia)** e **Conhecimento (Nobreza e Realeza)**, em vez das formas abreviadas.
- Foram adicionados testes de regressão para impedir que essas duas formas abreviadas retornem ao catálogo.


- Confirmado no código que **Natação** recebe o dobro da penalidade total de armadura/carga aplicada às perícias afetadas. Adicionado teste de regressão comparando Natação (−6) com Escalar (−3) sob penalidade base de −3; a regra existente não foi alterada.

## Achados que ainda exigem auditoria antes de serem alterados

1. **Perícias de classe das 11 classes básicas:** comparar cada lista com a seção “Perícias de Classe” de cada classe e com a tabela geral de perícias. Não completar listas por inferência.
2. **Penalidade de armadura:** verificar quais perícias sofrem a penalidade e confirmar a regra de penalidade dobrada em Natação. O modelo atual guarda um indicador booleano por perícia; uma regra de multiplicador pode exigir mudança explícita no modelo e nos pontos de cálculo.
3. **Somente treinado:** conferir Adestrar Animais, Conhecimento, Decifrar Escrita, Identificar Magia, Operar Mecanismo, Prestidigitação, Profissão e Usar Instrumento Mágico contra a tabela da edição. A interface e os cálculos devem respeitar o treinamento mínimo sem apagar graduações de fichas antigas.
4. **Nomes e contagem:** conferir os nomes das especializações de Conhecimento, a distinção entre a perícia Ofícios e suas especializações e a contagem do catálogo. Conhecimento possui especializações próprias, portanto a quantidade de entradas não deve ser comparada diretamente à contagem de perícias sem explicar esse critério.
5. **Compatibilidade:** antes de renomear outros valores armazenados, implementar testes ou migração para fichas existentes; nomes persistidos não devem ser alterados silenciosamente.

- Corrigida a lista de perícias de classe do **Bardo**: incluídas todas as especializações de Conhecimento, além de Decifrar Escrita, Natação, Ofícios e Profissão; removida Observar, que não consta na lista básica da classe. A lista foi comparada com a referência SRD 3.5; Falar Idioma não foi incluída porque não existe como entrada de perícia no catálogo atual do aplicativo e exige decisão de modelagem própria.

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
## Padronização de nomes e compatibilidade

- Padronizados os nomes genéricos da perícia para **Saltar** e **Ofícios**, em vez de **Salto** e **Ofício**, no catálogo e nas listas de perícias de classe.
- Fichas antigas podem continuar contendo os nomes anteriores no JSON salvo. O cálculo agora reconhece esses dois nomes legados, mantém graduações e bônus e apresenta o rótulo canônico na lista calculada. Os dados persistidos não são regravados por uma migração automática.
- Foi adicionado teste de regressão que carrega uma ficha com os dois nomes antigos, confirma que as graduações permanecem e verifica o reconhecimento como perícias de classe do Bárbaro.


## Preservação de meias graduações em multiclasse

- A normalização de graduações agora preserva valores em incrementos de 0,5 mesmo quando uma perícia passa a ser considerada de classe após a aquisição de outra classe. Isso evita arredondar 1,5 para 2 ou descartar a meia graduação em fichas existentes.
- A regra do SRD 3.5 esclarece que meias graduações não aumentam o modificador do teste até completarem uma graduação inteira. Foi adicionado teste de regressão para personagem Guerreiro/Bardo com 1,5 graduação em Acrobacia.
- A validação do orçamento de pontos continua fora do escopo: o modelo atual não registra em qual nível cada graduação foi comprada, então não é possível reconstruir com segurança o custo histórico de cada perícia.


## Correção das perícias de classe do Monge

- A lista do Monge não incluía **Atuação** nem **Profissão**, embora ambas constem da lista de perícias de classe da classe no D&D 3.5.
- As duas perícias foram adicionadas à lista e foi criado um teste de regressão que compara o conjunto completo de perícias de classe do Monge com a lista esperada.
- A alteração não modifica graduações já registradas nem o formato dos dados salvos.

## Correção das perícias de classe do Feiticeiro

- A lista do Feiticeiro continha **Blefar**, que não consta entre as perícias de classe do Feiticeiro no D&D 3.5. A lista foi ajustada para manter Concentração, Conhecimento (Arcano), Identificar Magia, Ofícios e Profissão.
- Foi adicionado teste de regressão para comparar o conjunto completo de perícias de classe do Feiticeiro com a lista esperada.
- A alteração não modifica graduações existentes nem os dados salvos das fichas.


## Correção das perícias de classe do Bardo — Saltar

- A lista de perícias de classe do Bardo estava sem **Saltar** (*Jump*), que consta da lista básica da classe no D&D 3.5. A perícia foi incluída sem alterar graduações já registradas.
- O teste de regressão da lista completa do Bardo agora exige **Saltar**.
- Aproveitando a revisão documental, removida a referência incorreta a Acrobacia como perícia somente treinada: em D&D 3.5, Acrobacia pode ser usada sem treinamento, embora sofra penalidade de armadura.
- Corrigido o erro tipográfico “Ofícioss” na documentação.

## Cobertura de regressão das listas de perícias de classe

- Adicionado teste de conjunto completo para Bárbaro, Clérigo, Druida, Guerreiro, Paladino, Ranger, Ladino e Mago.
- Bardo, Monge e Feiticeiro já possuem testes de conjunto completo próprios.
- A cobertura compara os nomes modelados no aplicativo e ajuda a detectar omissões ou inclusões acidentais. Ela não substitui a revisão de terminologia da tradução brasileira nem resolve a modelagem especial de Falar Idioma.
## Testes de requisitos de treinamento e penalidade de armadura

- Adicionados testes de conjunto para as perícias que exigem treinamento e para as perícias às quais se aplica a penalidade de armadura.
- Acrobacia deve aceitar uso sem treinamento, mas continua sujeita à penalidade de armadura.
- Natação continua sujeita à penalidade de armadura dobrada, coberta por teste de cálculo existente.
- A lista de Falar Idioma permanece fora do catálogo comum de perícias com graduações; sua implementação precisa respeitar a regra especial de idiomas conhecidos, sem inventar testes ou pontos de perícia.



## Implementação separada de Falar Idioma

- Idiomas conhecidos agora são armazenados em campo próprio (`languagesJson`), separado do JSON de perícias; não recebem graduações, bônus ou rolagens.
- A aba Perícias apresenta uma seção para adicionar e remover idiomas, com deduplicação sem diferenciar maiúsculas/minúsculas.
- A base Room passa da versão 2 para a 3 com migração aditiva que cria a coluna com valor padrão vazio. A migração preserva as fichas existentes e não usa migração destrutiva.
- Testes cobrem normalização, remoção de entradas vazias, duplicatas e compatibilidade com fichas sem dados de idiomas.

## Especialidades independentes de Ofícios

- **Ofícios** funciona como categoria de perícia: cada especialidade cadastrada é uma perícia normal e independente, por exemplo **Ofícios (Armas)** e **Ofícios (Armaduras)**.
- Cada especialidade possui suas próprias graduações e bônus, usa Inteligência, participa do limite de graduações por nível e realiza testes normalmente. O estado genérico legado **Ofícios** continua preservado.
- A especialidade herda a condição de perícia de classe da perícia-base **Ofícios** quando qualquer classe do personagem tem Ofícios em sua lista.
- A interface permite cadastrar uma especialidade pelo nome curto (ex.: Armas) ou pelo nome completo (ex.: Ofícios (Armas)). O armazenamento existente de perícias é reutilizado; não há migração destrutiva dos dados salvos.
- A interface oferece sugestões comuns (Armas, Armaduras, Alquimia, Carpintaria, Ferraria, Joalheria, Arco e flechas e Construção), mas elas são apenas atalhos: o jogador pode informar qualquer outra especialidade apropriada à campanha. Não existe limite fixo de especialidades.
