# Relatório de correções — Ficha D&D 3.5

## Escopo desta rodada

Esta rodada corrige defeitos pontuais identificados por inspeção estática. O projeto original foi preservado como base; não houve reescrita geral nem alteração deliberada do formato das fichas já armazenadas.

## Alterações realizadas

1. **Proteção contra apagamento destrutivo do banco**
   - Removido `fallbackToDestructiveMigration()` da configuração Room.
   - A migração existente da versão 1 para a 2 foi preservada.
   - Consequência: uma versão futura sem migração explícita poderá falhar ao abrir o banco, mas não apagará automaticamente as fichas para contornar a falha. Antes de publicar novas versões, deve-se criar e testar a migração correspondente.

2. **Estado de equipamento respeitado**
   - Os cálculos agora consideram um item equipado somente quando `equipped == true`.
   - Um `equippedSlot` residual não concede bônus se o item estiver marcado como não equipado.

3. **Bônus de atributo de equipamentos**
   - O cálculo deixou de reconhecer apenas bônus +2/+4 escritos em padrões específicos.
   - Agora aceita qualquer bônus positivo com os códigos de atributo existentes (`FOR`, `DES`, `CON`, `INT`, `SAB`, `CAR`), mesmo quando há texto adicional depois do bônus.
   - O parser não inventa mais um bônus +2 quando não encontra o atributo.
   - A acumulação continua usando o maior bônus reconhecido, conforme o comportamento anterior; a classificação por tipo de bônus ainda precisa de modelagem própria para aplicar todas as regras de acumulação do D&D 3.5.

4. **Diagnóstico de JSON inválido**
   - Os adaptadores registram erros de desserialização no Logcat, identificando a categoria de dados afetada.
   - Os elementos lidos antes do erro são preservados na lista retornada, em vez de a exceção ser descartada sem qualquer diagnóstico.
   - Isso não substitui uma futura tela de recuperação/aviso ao usuário.

5. **Modificadores e capacidade por tamanho**
   - Criados modificadores explícitos de tamanho para CA/ataque e agarrar, incluindo categorias minúscula, diminuta, miúda, pequena, média, grande, enorme, imensa e colossal.
   - Corrigidos os multiplicadores de capacidade de carga para categorias menores; antes, três tamanhos diferentes recebiam o mesmo multiplicador de Pequeno.

6. **Testes unitários adicionados**
   - Bônus +6 de Força em descrição com texto adicional.
   - Item não equipado que conserva um espaço atribuído não deve alterar atributos.
   - Modificadores de tamanho e multiplicadores de capacidade de carga.

## Validação

- Revisão estática dos arquivos alterados e conferência dos padrões de código.
- Não foi possível executar a suíte Gradle nesta sessão: o ZIP não contém `gradlew` nem `gradle-wrapper.jar`, e não há instalação local do Gradle disponível no ambiente. Portanto, as alterações ainda precisam ser compiladas e testadas em um ambiente Android/Gradle.

## Próximas correções recomendadas

- Criar migrações Room explícitas e testes de upgrade usando bancos reais das versões anteriores.
- Estruturar efeitos de itens por tipo de bônus, atributo e regra de acumulação, em vez de depender de texto livre.
- Rever as condições de movimento rápido do bárbaro, limites de carga e restrições de armadura.
- Validar graduações máximas de perícias e progressões de multiclasse.
- Ampliar testes de referência para CA, BBA, resistências, magias, talentos e habilidades de classe.


## Auditoria do catálogo de magias — 7º nível (2026-10-24)

### Achado confirmado; correção ainda pendente
- Fonte primária desta verificação: PDF brasileiro **D&D 3.5 — Livro do Jogador**, capítulo 11, listas de magias por classe (incluindo a lista de 7º nível de feiticeiro/mago na página impressa 167 e a lista de 7º nível de druida na página impressa 162).
- Na versão de `Dnd35Rules.PRESET_SPELLS` examinada nesta auditoria, há 246 entradas após as inclusões desta etapa (o snapshot anterior tinha 244). O catálogo contém entradas de 7º nível, mas a comparação por classe identificou erros de classificação e ausências nas listas gerais. A contagem anterior de 245 estava incorreta.
- Há entradas com nível por classe ou domínio igual a 7 (por exemplo, Dedo da Morte para mago/feiticeiro; Tempestade de Fogo para druida; e alguns níveis de domínio). Esses casos pontuais não substituem o catálogo completo do círculo.
- Esta constatação é uma **lacuna pendente**, não uma declaração de que todos os níveis ou todos os metadados de magia já foram verificados.
- Não alterar o significado do campo `level` nem reclassificar entradas isoladamente antes de concluir a conferência de cada magia por classe no PDF. Preservar `classLevels` e `domainLevels` quando a magia tiver níveis diferentes conforme a lista.
- Próximo passo: transcrever/conferir as listas de 7º nível de todas as classes conjuradoras presentes no Livro do Jogador, comparar nomes canônicos, classe/nível e duplicatas, adicionar as entradas ausentes e testes de regressão. Só então solicitar revisão de CI e merge.


### Correção parcial — níveis de magias arcanas (2026-10-24)
- Conferência direta das listas de 7º e 8º nível de feiticeiro/mago no PDF, páginas impressas 167–168.
- Corrigidos os níveis de Feiticeiro/Mago para 7 em: Padrão Cintilante, Clone, Evaporação, Corpo de Ferro, Estase Temporal e Metamorfosear Objetos.
- Corrigidos os níveis individuais de Criar Mortos-Vivos Maior e Símbolo da Morte: Clérigo permanece 8; Feiticeiro e Mago passam a 7.
- Preservados níveis de domínio diferentes quando explícitos (por exemplo, Água 8 para Evaporação; Morte 8 para Criar Mortos-Vivos Maior).
- Adicionado teste de regressão para os níveis por classe acima.
- **Ainda pendente:** adicionar as magias ausentes das listas de 7º nível (incluindo Ressurreição, Restauração Maior, Curar Ferimentos Moderados em Massa e outras entradas listadas no PDF), conferir todas as classes/domínios, e executar CI. Esta PR não deve ser tratada como conclusão da auditoria completa.


### Inclusões parciais — lista arcana de 7º nível (2026-10-24)
- Adicionadas **Invocar Criaturas VII** e **Ondas da Exaustão**, ausentes do catálogo e explicitamente listadas no Livro do Jogador.
- Adicionadas verificações de regressão para que ambas apareçam como magias de 7º nível para as classes arcanas e, no caso de Invocar Criaturas VII, também Clérigo.
- A descrição de Ondas da Exaustão foi resumida a partir do texto da magia no PDF; revisar a ficha completa (área, alcance e duração) antes de considerar a entrada finalizada.
- Permanecem ausentes outras magias de 7º nível já identificadas: Banimento, Refugiar Itens, Reverter Magia, Vidência Maior, Visão Arcana Maior, Visão, Convocação Instantânea de Drawmij, Evocação de Sombras Maior, Ressurreição, Restauração Maior, Curar Ferimentos Moderados em Massa e Invocar Aliado da Natureza VII. Conferir aliases e níveis por classe/domínio antes de inclusão.


### Revisão de metadados — Ondas da Exaustão (2026-10-24)
- A ficha inicial usava alcance médio, o que não corresponde ao efeito em cone descrito no Livro do Jogador. O alcance foi corrigido para **cone de 18 m**; mantidos execução de 1 ação padrão, duração instantânea, ausência de teste de resistência e resistência à magia aplicável.
- Incluído teste de regressão para alcance, duração, resistência e teste de resistência.
- A estrutura atual de `PresetSpell` não tem campo separado para área; o alcance em cone está registrado no campo `range`. Isso é uma limitação do modelo, não uma alteração das regras.


### Inclusões verificadas — magias de 7º nível (2026-10-24)
- Adicionadas `Ressurreição` (Clérigo 7), `Restauração Maior` (Clérigo 7) e `Invocar Aliado da Natureza VII` (Druida 7; domínio Animal 7), seguindo as entradas e listas do Livro do Jogador em português.
- As descrições registram os custos/efeitos centrais presentes no livro; a auditoria integral de todos os campos e listas por classe ainda está em andamento.
- Testes de regressão adicionados para nível por classe, alcance/duração de Ressurreição, tempo/custo de XP de Restauração Maior e nível de domínio da magia de invocação.
- Contagem esperada do catálogo após estas três inclusões: 249 entradas; verificar contagem e nomes duplicados antes de concluir a etapa.


### Revisão corretiva — Ressurreição e invocação natural (2026-10-24)
- Conferida a entrada completa de Ressurreição no PDF brasileiro, página impressa 239 (página digital 243): componente material de água benta e diamantes no valor total de pelo menos 10.000 PO. Corrigido o valor anterior de 5.000 PO e incluído teste de regressão.
- Removida a atribuição de nível 7 ao domínio Animal em Invocar Aliado da Natureza VII: a lista consultada confirma Druida 7, mas não fundamenta esse nível de domínio.
- Corrigido o teste de níveis arcanos: Ressurreição e Restauração Maior são verificadas separadamente como Clérigo 7, em vez de exigir níveis de Feiticeiro/Mago.
- A auditoria e a validação da CI permanecem em andamento; não considerar a PR concluída nem integrada.


### Correção dos testes de níveis arcanos — revisão do PDF (2026-10-24)
- A CI da PR #242 confirmou quatro falhas de asserção na suíte PresetSpellCatalogTest; as tarefas de compilação Kotlin e de compilação dos testes terminaram. A falha ocorreu em expectativas de níveis/listas de 8º nível.
- A lista do Livro do Jogador, páginas impressas 167–168 (PDF digital 171–172), coloca Padrão Cintilante, Clone, Criar Mortos-Vivos Maior (Feiticeiro/Mago), Evaporação, Ondas da Exaustão, Símbolo da Morte (Feiticeiro/Mago), Corpo de Ferro, Estase Temporal e Metamorfosear Objetos entre as magias arcanas de 7º nível.
- Ajustados os testes de Padrão Cintilante, Criar Mortos-Vivos Maior e Símbolo da Morte; removidas Corpo de Ferro, Estase Temporal e Metamorfosear Objetos das expectativas arcanas genéricas de 8º nível. Isso corrige as expectativas dos testes, sem alterar regras do catálogo nesta mudança.
- A CI ainda precisa executar novamente e confirmar os resultados. Não considerar a PR concluída nem integrada até testes e verificações de merge passarem.


### Segunda correção das expectativas de 8º nível — CI #242 (2026-10-24)
- A execução da CI `40125784404` falhou em dois testes: `consolidatedEighthLevelSpellBatchMatchesPlayerHandbook` e `eighthLevelPlayerHandbookListsAreComplete`. Os outros testes passaram (220 testes executados, 2 falhas).
- O campo geral `level` de magias com níveis diferentes por classe deve corresponder ao maior nível de classe representado no registro. Corrigida a asserção para validar essa regra sem confundir o nível de feiticeiro/mago com o nível de clérigo.
- Removido `Criar Mortos-Vivos Maior` da lista de magias arcanas de 8º nível: no Livro do Jogador, é Feiticeiro/Mago 7 e Clérigo 8.
- Removidas do bloco de verificações de conteúdo do lote de 8º nível as entradas `Corpo de Ferro`, `Estase Temporal` e `Metamorfosear Objetos`, que são magias arcanas de 7º nível no PDF (página impressa 168; página digital 172).
- A CI precisa ser executada novamente após esta correção. Não fazer merge antes de a suíte passar e de confirmar a condição de merge da PR.
