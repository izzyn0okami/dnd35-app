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


## Busca contextual no glossário de habilidades de classe

- O nome de cada habilidade de classe agora exibe um indicador de pesquisa e abre o glossário com o termo já preenchido; tocar no restante da linha continua abrindo o detalhe e a progressão da habilidade.
- O diálogo permite editar a consulta e apresenta resultados com classe, nível e explicação textual.
- A pesquisa ignora diferenças entre maiúsculas/minúsculas e acentos.
- O mecanismo foi isolado em `ClassFeatureGlossarySearch.kt`: consulta apenas nomes e descrições estáticas das habilidades. Não recebe ficha, atributos, níveis efetivos ou estatísticas calculadas; não altera progressões, cálculos nem dados persistidos.
- A fonte textual inicial é o catálogo estático de habilidades de classe já existente. O índice pode receber sinônimos e verbetes próprios em incrementos posteriores sem acoplar a busca ao motor de regras.
- Teste automatizado cobre busca sem acentos e consulta vazia.
- A descrição de Domínios do Clérigo também foi ajustada para explicitar o espaço de magia adicional por nível, corrigindo a expectativa do teste de conteúdo sem alterar a mecânica.


## Auditoria de Bárbaro, Bardo e Druida (15/11/2026)

- Fonte primária: Livro do Jogador brasileiro, Capítulo 3, Bárbaro (pp. 21–22), Bardo (pp. 23–25) e Druida (pp. 29–31), PDF da biblioteca do projeto.
- **Bárbaro — Movimento Rápido:** encontrada divergência mecânica: a implementação impedia o bônus com armadura média e carga média, embora o livro permita ambos e determine que o bônus seja aplicado antes da redução normal de deslocamento. Corrigido o cálculo para permitir armadura leve/média e carga leve/média; armadura pesada e carga pesada/sobrecarga continuam impedindo o bônus. O teste antigo que afirmava a regra incorreta foi substituído e foi acrescentado teste para carga média.
- **Bárbaro — Fúria:** descrição ampliada com duração, modificadores, restrições principais e fadiga ao término; corrigido o nome para **Vontade Inabalável**, conforme o livro.
- **Bárbaro — Analfabetismo:** descrição ajustada para os idiomas conhecidos e para a alfabetização automática ao adquirir nível em outra classe.
- **Bardo:** detalhados usos diários, requisitos de Atuação, alcance, duração e efeitos principais de Música de Proteção, Fascinar, Inspirar Coragem, Inspirar Competência, Sugestão, Inspirar Grandeza, Melodia da Libertação, Inspirar Heroísmo e Sugestão em Massa.
- **Druida:** detalhadas opções e nível efetivo do Companheiro Animal, alcance/teste de Empatia Selvagem, Caminho da Floresta, Rastro Invisível, limites de tamanho e DV da Forma Selvagem, restrição das formas vegetais e progressão elemental.
- Nenhuma progressão de nível, desbloqueio, dado persistido ou estrutura de ficha foi alterada. A única mudança mecânica neste incremento é a correção de Movimento Rápido do Bárbaro.
- Testes atualizados para confirmar que armadura média e carga média permitem o bônus antes da redução normal de deslocamento.


## Auditoria de Paladino e Ranger (15/11/2026)

- Fonte primária: Livro do Jogador brasileiro, Capítulo 3, Paladino (pp. 45–47) e Ranger (pp. 47–49), PDF da biblioteca do projeto.
- **Paladino — Remover Doença:** corrigida a progressão que estava antecipada em três níveis. Os usos semanais corretos são 1/semana no 6º nível, 2/semana no 9º, 3/semana no 12º, 4/semana no 15º e 5/semana no 18º. A entrada indevida de 6 usos no 18º foi removida.
- **Paladino — Imposição de Mãos e Aura de Coragem:** descrições ampliadas com Carisma mínimo 12, possibilidade de dividir a cura ou causar dano a mortos-vivos, alcance de 3 m e bônus de moral contra medo.
- **Ranger — Camuflagem e Mimetismo:** corrigidos nomes e níveis conforme o livro: Camuflagem no 13º nível e Mimetismo no 17º. A entrada incorreta Esconder-se à Plena Vista no 18º foi removida.
- **Ranger — Rastreador Eficaz:** registrada a penalidade de -10 ao rastrear com o dobro do deslocamento (em vez de -20).
- Testes automatizados confirmam os níveis de Remover Doença, Camuflagem e Mimetismo.
