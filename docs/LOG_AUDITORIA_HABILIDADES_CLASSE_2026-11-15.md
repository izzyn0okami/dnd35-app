# Log — auditoria das descrições de habilidades de classe

Data: 2026-11-15

## Escopo deste incremento
- Revisão de conteúdo das habilidades de Clérigo, Feiticeiro e Mago na aba Personagem.
- Nenhum cálculo, desbloqueio, progressão automática, dado salvo ou formato de ficha foi alterado.

## Correções realizadas
- **Clérigo — Aura:** esclarecido que a aura especial depende do alinhamento da divindade ou de domínio de alinhamento; não é uma aura universal para todo clérigo.
- **Clérigo — Expulsar/Fascinar Mortos-Vivos:** detalhados usos diários, escolha de energia para clérigos neutros, testes, dano de expulsão e bônus de Conhecimento (religião).
- **Clérigo — Conversão Espontânea:** alinhada a conversão de magias preparadas à forma de canalização positiva/negativa escolhida.
- **Clérigo — Domínios:** esclarecidos poderes concedidos, espaços de magia de domínio, restrições de alinhamento e escolha permanente.
- **Feiticeiro — Conjuração Espontânea:** esclarecidos repertório limitado, conjuração sem preparação e papel do Carisma.
- **Feiticeiro e Mago — Familiar:** ampliada a descrição do vínculo, dos benefícios progressivos e da distinção em relação ao companheiro animal.
- **Mago — Preparação com Grimório:** esclarecida a diferença entre conhecer magias, registrá-las no grimório e prepará-las.

## Referências
- d20 SRD — Clérigo: https://www.d20srd.org/srd/classes/cleric.htm
- d20 SRD — Feiticeiro e Mago: https://www.d20srd.org/srd/classes/sorcererWizard.htm
- d20 SRD — Expulsar ou Fascinar Mortos-Vivos: https://www.d20srd.org/srd/combat/specialAttacks.htm#turnOrRebukeUndead

## Limites e próximos pontos da auditoria
- O catálogo de habilidades continua sendo um resumo em português, não a transcrição integral do SRD ou do Livro do Jogador.
- As progressões de magias por nível continuam representadas nos dados próprios do sistema; este incremento não as altera.
- Ainda é necessário revisar as descrições resumidas de outras classes, especialmente habilidades com escolhas, usos diários e progressões escalonadas.
- Não interpretar ausência de uma habilidade na lista como regra de que ela não existe; comparar sempre com a classe de referência antes de concluir.


## Correção após primeira execução do CI

- A compilação Kotlin foi concluída; os dois workflows falharam em dois testes de conteúdo, não por erro de compilação.
- Os testes exigiam formulações explícitas sobre clérigos neutros e sobre a distinção entre Invocar Familiar e Companheiro Animal. As descrições foram ajustadas para explicitar essas regras sem alterar cálculos ou dados da ficha.
- Também foi corrigida a quebra de linha entre as entradas Conversão Espontânea e Domínios da Divindade.
- Commit corretivo: `2cf4d14a510a354f0803b7a3833bbfc67a9f9f7b`.
- Aguardar nova execução de Android CI e Android build antes de mesclar.


## Segunda correção após CI (15/11/2026)

- A segunda execução confirmou que a compilação Kotlin passa, mas dois testes de conteúdo ainda falhavam por diferenças literais de texto: o teste do feiticeiro exige a expressão `repertório limitado`, e o teste do clérigo procura `clérigos neutros` em minúsculas na descrição de Expulsar/Fascinar.
- Ajustadas as descrições para preservar o sentido das regras e atender aos critérios automatizados.
- Commit: `a06edd93d0d5cbacd05ce2357c2a867417250539`.
- Aguardar nova execução integral dos workflows antes do merge.
