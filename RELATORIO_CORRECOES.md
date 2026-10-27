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


### Terceira correção das expectativas de 8º nível — CI #242 (2026-10-24)
- A execução `40126213999` voltou a falhar em dois testes. A análise das linhas apontadas mostrou que o teste ainda incluía `Padrão Cintilante`, `Clone` e `Evaporação` na lista arcana de 8º nível, embora sejam Feiticeiro/Mago 7 no Livro do Jogador.
- Corrigida a lista arcana de 8º nível, removendo essas três magias.
- Corrigida também a expectativa do campo geral `level`: não se deve inferir automaticamente o nível geral pelo maior valor em `classLevels`, porque `Criar Mortos-Vivos Maior` e `Símbolo da Morte` têm nível geral 7 e nível de Clérigo 8.
- A CI deve validar novamente estas correções. O merge continua bloqueado até testes passarem e a PR ficar sem conflitos.


### Correção adicional de nível por classe — Campo Antimagia (2026-10-24)
- A execução da CI `40126462315` reduziu as falhas a uma: `consolidatedEighthLevelSpellBatchMatchesPlayerHandbook`, na verificação do lote de magias de 8º nível.
- A conferência da entrada `Campo Antimagia` encontrou níveis arcanos registrados como Feiticeiro/Mago 7, embora o Livro do Jogador a liste como Feiticeiro/Mago 6; o nível de Clérigo permanece 8 e os níveis de domínio Magia/Proteção permanecem 6.
- Corrigidos apenas os níveis por classe na entrada do catálogo. A PR #242 permanece aberta; aguardar nova CI verde antes de qualquer merge.


### Catálogo de magias de 7º nível — lote de lacunas (2026-10-24)
- Após o merge da PR #242 (CI verde), aberta uma nova etapa para as lacunas encontradas ao comparar o catálogo com as listas do Livro do Jogador, páginas impressas 167–168 (listas de Feiticeiro/Mago), 157–158 (Clérigo) e páginas de descrição das magias.
- Adicionadas nove entradas: Banimento, Refugiar Itens, Reverter Magia, Vidência Maior, Visão Arcana Maior, Visão, Convocação Instantânea de Drawmij, Evocação de Sombras Maior e Curar Ferimentos Moderados em Massa.
- Preservadas as diferenças de nível por classe: Banimento é Clérigo 6 e Feiticeiro/Mago 7; Vidência Maior é Bardo 6 e Clérigo/Feiticeiro/Mago 7; as demais entradas seguem os níveis indicados no catálogo e no Livro do Jogador.
- Teste novo verifica presença única, níveis por classe e domínios de Reverter Magia. Conferir a CI antes de merge; metadados resumidos permanecem sujeitos à revisão final contra as descrições completas do livro.


### Revisão da descrição de Banimento — Livro do Jogador (2026-10-24)
- A leitura da descrição na página impressa 177 (PDF digital 181) mostrou que Banimento seleciona uma ou mais criaturas extra-planares que estejam a menos de 9 m entre si; não é descrita como uma magia que expulsa criaturas de uma área.
- Corrigido o resumo no catálogo para remover a referência incorreta a “área” e registrar o agrupamento dos alvos e a função dos componentes/objetos de foco contra criaturas específicas.
- Adicionado teste de regressão para escola, níveis por classe, alcance, teste de resistência, resistência à magia e os detalhes centrais dos alvos.


### Revisão de Refugiar Itens e Reverter Magia — Livro do Jogador (2026-10-24)
- Refugiar Itens: o texto anterior insinuava que o alvo era necessariamente uma criatura voluntária e deixava os detalhes mecânicos para o livro. Reescrito o resumo para registrar a proteção contra adivinhações, invisibilidade visual, animação suspensa e as limitações de detecção e resistência descritas na entrada do livro (PDF digital 241).
- Reverter Magia: corrigida a quantidade refletida de “sete a dez níveis” para “1d4+6 níveis”, conforme o resumo do livro e a descrição completa (PDF digital 244).
- Acrescentados testes de regressão para as duas descrições e seus metadados essenciais. A auditoria das demais magias do lote continua aberta.


### Correção do nível de Evocação de Sombras Maior — Livro do Jogador (2026-10-24)
- A entrada do livro (PDF digital 207) lista Evocação de Sombras Maior como Fet/Mag 8, não 7. Corrigidos o nível da magia e os níveis por classe no catálogo.
- A descrição agora registra que reproduz evocações de 7º nível ou inferior e que, se desacreditada como evocação de sombras maior, causa 60% do dano normal. Adicionado teste de regressão.


### Correção do nível de Curar Ferimentos Moderados em Massa — Livro do Jogador (2026-10-24)
- A lista de magias do Livro do Jogador (PDF digital 161–162) coloca Curar Ferimentos Moderados em Massa no 6º nível de Clérigo e Druida, não no 7º. Corrigidos o nível geral e os níveis por classe no catálogo.
- Adicionado teste de regressão para os níveis e a descrição de cura.

### Revisão da magia Visão — mecânica do Livro do Jogador (2026-10-24)
- A conferência da descrição no PDF brasileiro, página impressa 251, identificou que o resumo anterior omitia as CDs e a rolagem para obter a visão.
- A descrição agora registra o teste de nível de conjurador (1d20 + nível, máximo +25), CD 20 para alvo/local próximo, CD 25 para informações detalhadas (resposta incompleta), CD 30 para rumores (resposta vaga) e o custo de 100 XP.
- Adicionado teste de regressão para os níveis de Feiticeiro/Mago, metadados básicos e os limites mecânicos explicitados no livro.
- Esta correção cobre apenas a magia Visão; a conferência final de Vidência Maior, Visão Arcana Maior e Convocação Instantânea de Drawmij permanece pendente.

### Revisão de Convocação Instantânea de Drawmij — Livro do Jogador (2026-10-24)
- A lista/descrição da magia no PDF brasileiro, página impressa 188, informa alcance “Veja texto”, falha se o objeto estiver sob posse de outra criatura, identificação do portador e localização aproximada, inscrição invisível legível com ler magias e componente material de safira de pelo menos 1.000 PO.
- Corrigido o alcance genérico “Ilimitado (veja texto)” para “Veja texto” e completada a descrição com essas condições e o custo do componente.
- Adicionado teste de regressão para níveis por classe, alcance, duração, resistência e detalhes da descrição.
- A auditoria de Vidência Maior e Visão Arcana Maior permanece pendente de fechamento.

### Reconciliação de fonte — Símbolo da Morte (2026-10-26)
- **Pergunta auditada:** o nível arcano de *Símbolo da Morte* é 6º ou 8º? O PDF brasileiro não é consistente internamente; portanto, não se trata de falha de OCR nem de leitura do algarismo.
- **Entrada individual no Livro do Jogador brasileiro:** página impressa 243 (página física 247 do PDF): o texto está legível e imprime literalmente `Nível: Clr 8, Fet/Mag 6`.
- **Índice/lista de magias:** página impressa 168 (página física 172 do PDF) inclui *Símbolo da Morte* no bloco de magias de 8º nível de Feiticeiro e Mago, antes do cabeçalho “Magias de 9º nível de Feiticeiro e Mago”.
- **Fontes externas consultadas:** D&D Tools identifica a magia como *Player’s Handbook v.3.5*, p. 289, e lista Cleric 8, Sorcerer 8 e Wizard 8; o SRD de D&D 3.5 também lista `Clr 8, Sor/Wiz 8`. Referências: https://dnd.arkalseif.info/spells/players-handbook-v35--6/symbol-of-death--2762/ e https://www.d20srd.org/srd/spells/symbolOfDeath.htm.
- **Conclusão:** há uma contradição impressa no próprio PDF brasileiro. A leitura “Fet/Mag 6” está correta quanto ao que aparece na página, mas é provavelmente um erro editorial/tradutório naquela linha da descrição: a lista por nível no mesmo livro e as referências externas independentes concordam com 8º nível arcano. Não registrar como erro de OCR.
- **Decisão do catálogo:** manter `level: 8` e `classLevels` Clérigo 8, Feiticeiro 8, Mago 8. Os dados e testes atuais já seguem essa decisão; não é necessária alteração funcional para esta divergência.
- **Reconciliação do histórico:** as notas anteriores deste relatório que classificaram *Símbolo da Morte* como magia arcana de 7º nível foram baseadas em interpretação incorreta da página impressa 168: as entradas que aparecem antes do cabeçalho “Magias de 9º nível” pertencem ao bloco de 8º nível. Essas notas ficam supersedidas por esta conclusão; preservadas como histórico para rastreabilidade.
- **Procedimento reforçado:** quando a descrição individual divergir de listas por classe ou de fontes externas, verificar a página impressa e o contexto visual, comparar com a fonte original/SRD e registrar explicitamente a divergência antes de alterar catálogo ou testes.

## Núcleo de regras de conjuração — etapa inicial (2026-10-26)

- Fonte normativa prioritária: PDF brasileiro do *Livro do Jogador* salvo na biblioteca.
- Centralizadas regras comuns de método de conjuração, requisito mínimo de atributo, espaços diários adicionais por atributo e nível de conjurador.
- Correção confirmada: o nível de conjurador do Ranger é metade do nível de Ranger a partir do 4º nível; Paladino usa nível de conjurador igual ao nível de Paladino menos 3. O cálculo anterior aplicava nível menos 3 às duas classes.
- Espaços bônus são calculados pelo modificador do atributo-chave e nível da magia; isso não aumenta a quantidade de magias conhecidas do Bardo ou Feiticeiro.
- Testes de regressão cobrem método de conjuração, requisito mínimo (inclusive truques), espaços bônus e níveis de conjurador de Ranger, Paladino e Mago.
- Escopo ainda incompleto: esta etapa cria o núcleo reutilizável, mas não inclui as tabelas completas de espaços diários nem as tabelas de magias conhecidas/preparadas por nível. A integração completa com a aba Magias continua pendente.

### CI #1362 — correção da expectativa de nível de conjurador do Ranger (2026-10-27)
- A suíte executou 275 testes e falhou apenas em `rangerSpellcastingStartsAtLevelFourWithCasterLevelOne`, que ainda esperava nível de conjurador 1 no 4º nível e 17 no 20º.
- O Livro do Jogador brasileiro determina que o Ranger não tem nível de conjurador até o 3º nível e, a partir do 4º, usa metade do nível de Ranger. A expectativa correta é 2 no 4º nível e 10 no 20º.
- Atualizado o teste legado em `ExampleUnitTest.kt` para refletir essa regra e renomeado o teste para não codificar a expectativa antiga.
- A CI precisa ser executada novamente após a correção. A auditoria da progressão completa de magias e a integração visual continuam pendentes.


### CI #1364 e separação das progressões de conjurador (2026-10-27)
- A execução #1364 usou o commit anterior à correção final e falhou no teste legado do Ranger; portanto, esse resultado não valida nem invalida o commit mais recente.
- Ao atualizar a expectativa do Ranger, os testes de Paladino e Ranger foram conferidos separadamente para impedir que as duas regras sejam confundidas: Paladino = nível de classe − 3 (níveis de conjuração a partir do 4º); Ranger = metade do nível de classe (a partir do 4º).
- O commit `b42a52ebcc1ad0bc0b2ef24c38a58d895ff88e20` contém as expectativas distintas e disparou a CI #1365. Aguardar o resultado antes de qualquer merge.

## Auditoria do catálogo de magias — conferência incremental (2026-10-25)

- Fonte de trabalho: `app/src/main/resources/dnd35/spells.json`; fonte normativa prioritária: PDF brasileiro do `D&D 3.5 — Livro do Jogador`.
- Snapshot inspecionado: 327 registros; nenhum nome exatamente duplicado. Essa verificação estrutural não comprova que a lista inteira do livro esteja coberta.
- Corrigido o campo `range` de **Mãos Flamejantes** de `4,5m` para `Cone de 4,5 m`, para deixar explícito que se trata de um cone e não de alcance de toque. A descrição já identificava corretamente o efeito como cone; nível 1, execução, duração e teste de resistência foram preservados.
- Conferidas no catálogo as entradas anteriormente apontadas como ausentes: `Vidência Maior`, `Visão Arcana Maior`, `Conjuração de Sombras Maior`, `Evocação de Sombras Maior`, `Curar Ferimentos Moderados em Massa`, `Símbolo da Morte` e `Criar Mortos-Vivos Maior`. Elas já existem; não foram duplicadas.
- Níveis por classe confirmados no JSON para este lote: `Símbolo da Morte` e `Criar Mortos-Vivos Maior` estão registrados como Clérigo 8, Feiticeiro 8 e Mago 8; `Evocação de Sombras Maior` como Feiticeiro/Mago 8; `Conjuração de Sombras Maior` como Feiticeiro/Mago 7.
- A lista do nível 7 ainda exige comparação sistemática, classe por classe, com as listas do capítulo de magias do PDF. A presença das entradas identificadas não deve ser tomada como prova de completude.
- Próximo lote: comparar a lista de 7º nível do Livro do Jogador com o catálogo por classe, validar campos de alcance/área/duração/resistência e registrar somente divergências confirmadas. Evitar reabrir as entradas acima sem nova evidência.


## Auditoria de níveis por classe — Cura Completa (2026-10-25)

- **Fonte normativa:** Livro do Jogador brasileiro, capítulo 11, lista de magias de 7º nível de Druida (página impressa 162; PDF correspondente no arquivo da biblioteca). A entrada lista *Cura Completa* entre as magias de 7º nível de Druida.
- **Divergência confirmada:** o catálogo já tinha uma única entrada `Cura Completa (Heal)` com Clérigo 6 e Druida 6. O nível geral `level: 6` permanece como valor legado/geral do registro; a correção é específica ao campo `classLevels`, que representa o nível individual por classe.
- **Correção:** alterado `classLevels.Druida` de 6 para 7; mantido Clérigo 6. Nenhuma entrada duplicada foi criada.
- **Regressão:** adicionado teste garantindo Clérigo 6, Druida 7, alcance de toque e duração instantânea.
- **Escopo ainda aberto:** isso corrige uma divergência confirmada, mas não encerra a comparação integral das listas de 7º nível por classe.


## Auditoria do catálogo de magias — Alterar Forma

- Incluída a magia **Alterar Forma**, ausente no catálogo, conforme o verbete do Livro do Jogador brasileiro (página impressa 169; PDF, página 174).
- Níveis registrados: Druida 9, Feiticeiro 9 e Mago 9; domínio Animal 9 em `domainLevels`, sem tratar domínio como classe.
- Metadados conferidos no livro: Transmutação; componentes V, G, F; execução de 1 ação padrão; alcance Pessoal; duração de 10 minutos/nível (D); teste de resistência Nenhum; resistência à magia Não.
- Descrição resumida em paráfrase para preservar as regras centrais: formas não únicas de tamanho Minúsculo a Colossal, limite de DV, formas incorpóreas/gasosas, capacidades adquiridas e mudança de forma por ação livre; foco de anel de jade de pelo menos 1.500 PO.
- Acrescentado teste de regressão para presença única, níveis de classe/domínio e metadados principais. A auditoria de completude do catálogo continua em andamento; esta inclusão não significa que o catálogo inteiro esteja completo.



## Auditoria de classes e domínios — Invocar Criaturas IX

- **Fonte normativa:** Livro do Jogador brasileiro, lista de magias de 9º nível de Clérigo (página impressa 159) e listas dos domínios Animais, Bem, Caos, Mal e Ordem (páginas impressas 159–161; PDF, páginas 163–165).
- **Revisão da hipótese inicial:** a leitura da lista de Clérigo confirma que `Invocar Criaturas IX` também é magia-base de Clérigo 9. Portanto, não se deve removê-la das classes; a proposta inicial de exclusão foi corrigida antes de merge.
- **Divergência confirmada:** o registro não continha os domínios que também oferecem a magia. Foram registrados Animais 9, Bem 9, Caos 9, Mal 9 e Ordem 9 em `domainLevels`, mantendo Clérigo, Feiticeiro e Mago no nível 9.
- **Regressão:** teste verifica as três classes e os cinco domínios, sem duplicar a magia.
- **Escopo:** correção limitada à cobertura dos domínios; a auditoria das listas por classe continua aberta.


## Auditoria de domínios — magias de 9º nível

- **Fonte normativa:** Livro do Jogador brasileiro, listas de domínios nas páginas impressas 159–162 (PDF, páginas 163–166).
- **Esfera Prismática:** registrada nos domínios Proteção 9 e Sol 9; ambos aparecem explicitamente nas listas.
- **Sexto Sentido:** registrado no domínio Conhecimento 9.
- **Milagre:** registrado no domínio Sorte 9.
- **Alterar Forma:** padronizado o identificador de domínio para `Animais`, correspondente ao título “Domínio dos Animais” usado no livro; corrigido o valor anterior `Animal`.
- **Regressão:** teste automatizado cobre os quatro registros e seus níveis de domínio.
- **Escopo:** apenas metadados de domínio comprovados por listas do livro; a auditoria de completude permanece em andamento.


## Auditoria de listas por classe — Esplendor da Águia (2026-10-26)

- **Fonte normativa:** Livro do Jogador brasileiro, capítulo 11, lista de magias de 2º nível de Paladino e listas de magias de 1º nível de Feiticeiro/Mago (páginas impressas 164–165; PDF, páginas 168–169).
- **Divergência confirmada:** o catálogo atribuía *Esplendor da Águia* a Ranger 2, mas a lista de magias de 2º nível de Ranger na página impressa 164 não contém essa magia. A lista de 2º nível de Paladino contém a magia; a lista arcana de 1º nível também a contém.
- **Correção proposta:** removida somente a atribuição Ranger 2 de `classes` e `classLevels`; preservados os níveis de outras classes já registrados.
- **Regressão:** teste garante que Ranger não apareça entre as classes da magia e que os níveis de Paladino, Feiticeiro e Mago permaneçam em 2.
- **Escopo:** correção pontual de classificação; não implica conclusão da auditoria integral de magias por classe.


## Auditoria de identificadores de domínio — listas de 7º nível (2026-10-26)

- **Fonte normativa:** Livro do Jogador brasileiro, capítulo 11, listas de magias de 7º nível e listas dos domínios (PDF, páginas 162–164 e 167–171; páginas impressas correspondentes no capítulo de magias/domínios).
- **Divergências confirmadas:** o catálogo usava `Animal` para as magias do domínio dos Animais e `Lei` para o domínio chamado **Ordem** no livro. Isso criava chaves divergentes para o mesmo domínio e poderia afetar filtros/agrupamentos.
- **Correção:** padronizadas as chaves de `domainLevels` para `Animais` e `Ordem`; os níveis e as atribuições de classe das magias foram preservados. Nenhuma magia foi duplicada.
- **Regressão:** atualizados os testes de catálogo para exigir `Animais` no domínio de `Forma Animal` e `Invocar Aliado da Natureza VIII`, além de manter a lista de domínios de `Invocar Criaturas IX` em nomes canônicos.
- **Resultado parcial da auditoria de 7º nível:** as listas de Clérigo e Druida consultadas estão representadas no catálogo; a conferência de Feiticeiro/Mago e das listas de domínios continua em andamento. Não considerar o catálogo integralmente auditado.


## Auditoria de domínios de 7º nível — cobertura e magias ausentes (2026-10-26)

- **Fonte normativa:** Livro do Jogador brasileiro, capítulo 11, listas de domínios (PDF, páginas 163–166) e verbetes de *Desintegrar* (p. 197), *Lendas e Histórias* (p. 217) e *Névoa Ácida* (p. 227).
- **Divergências confirmadas:** além dos nomes não canônicos `Animal` e `Lei`, faltavam associações de 7º nível para os domínios Cura (*Regeneração*), Destruição (*Desintegrar*), Força (*Mão Poderosa de Bigby*), Guerra (*Palavra de Poder, Cegar*), Morte (*Destruição*) e Proteção (*Repulsão*). Também faltavam do catálogo as magias *Névoa Ácida*, *Lendas e Histórias* e *Desintegrar*, presentes no livro e necessárias para representar Água 7, Conhecimento 7 e Destruição 7.
- **Correção:** adicionadas as três magias com níveis por classe e metadados principais transcritos/parafraseados do Livro do Jogador; registrados os domínios de 7º nível confirmados nas listas. Mantidos os níveis por classe e não foram criadas duplicatas.
- **Regressão:** teste compara as atribuições de domínio de 7º nível e verifica níveis por classe, duração, componentes, alcance, teste de resistência e resistência à magia das três entradas adicionadas.
- **Escopo:** cobertura de domínios de 7º nível atualizada; a auditoria completa de todas as listas de magias por classe e de todos os níveis/domínios continua em andamento.


## Correção de nível por classe — Curar Ferimentos Moderados em Massa (2026-10-26)

- **Fonte normativa:** Livro do Jogador brasileiro, lista de magias de 7º nível de Druida (PDF, página 167; página impressa correspondente do capítulo 11).
- **Divergência confirmada:** o registro tinha Clérigo 6 e Druida 6, mas a lista de Druida do livro inclui *Curar Ferimentos Moderados em Massa* no 7º nível.
- **Correção:** `classLevels.Druida` alterado de 6 para 7; Clérigo permanece no 6º nível e o nível geral legado do registro permanece 6, pois a estrutura separa nível geral de nível por classe.
- **Regressão:** teste atualizado para Clérigo 6/Druida 7.


- **Revisão adicional das listas de domínio:** removida a atribuição indevida de *Cajado Vivo* ao domínio Planta; *Passagem Invisível* foi ajustada para Viagem 8 (não 7), conforme a ordem da lista do domínio; removida a atribuição de *Viagem Planar* ao domínio Viagem. Mantido *Teletransporte Maior* como Viagem 7.


## Correção de domínio fora do Livro do Jogador — Força (2026-10-26)

- **Fonte normativa:** Livro do Jogador brasileiro, capítulo 11, seção de domínios. A lista do livro inclui Água, Animais, Ar, Bem, Caos, Conhecimento, Cura, Destruição, Enganação, Fogo, Guerra, Magia, Mal, Morte, Ordem, Planta, Proteção, Sol, Sorte, Terra e Viagem; não há domínio Força.
- **Erro corrigido:** a auditoria anterior havia atribuído indevidamente o domínio `Força` a *Mão Poderosa de Bigby* e *O Punho Cerrado de Bigby*. Removidas essas duas associações; níveis e dados por classe não foram alterados.
- **Regressão:** testes agora garantem que todos os identificadores de domínio do catálogo pertençam à lista de domínios do Livro do Jogador e que *O Punho Cerrado de Bigby* não tenha uma associação de domínio inventada.
- **Nota de auditoria:** esta correção retifica explicitamente a associação indevida introduzida durante a etapa anterior; a revisão de todos os níveis e domínios continua em andamento.


## Interface de criação de personagem — rótulos de BBA (2026-10-26)

- Corrigida a exibição de identificadores internos do enum (`FULL`, `THREE_QUARTERS`, `HALF`) no cartão da classe. A interface agora mostra rótulos em português: progressão integral, de três quartos e de metade, explicando BBA como Bônus Base de Ataque.
- Alteração somente de apresentação; cálculos e enums não foram modificados. Testes unitários cobrem os três rótulos.
- Aguardar CI antes do merge.


## CI #1366 — núcleo de conjuração (2026-10-27)

- A execução de CI associada ao commit `bd16f3570e9ba4a9bdcd12d0f5a747f698a8a8f3` terminou com **sucesso** (execução #1366).
- Isso valida o estado atual dos testes e da compilação para esse commit; não significa que a auditoria de conjuração esteja concluída.
- A PR #265 permanece em rascunho. Ainda faltam as tabelas completas de progressão por classe (magias diárias e conhecidas/preparadas), o cálculo de espaços totais com bônus de atributo e a integração correta na aba Magias.
- Referência normativa: PDF brasileiro do *Livro do Jogador* salvo na biblioteca. As progressões devem ser transcritas das tabelas por classe, sem inferir valores ausentes por padrão ou reutilizar uma tabela de outra classe.
- PR relacionada #261 continua aberta e com conflito indicado pelo GitHub; não fazer merge automático nem abrir PR duplicada para o mesmo escopo. Resolver o conflito após verificar a base atual e a CI correspondente.


## Progressões de magias conhecidas — Bardo e Feiticeiro (2026-10-27)

- Transcritas para `SpellcastingRules` as progressões de magias conhecidas das Tabelas 3-5 (Bardo) e 3-10 (Feiticeiro) do PDF brasileiro do *Livro do Jogador*.
- A API `knownSpells(className, classLevel, spellLevel)` devolve a quantidade fixa da tabela por nível de classe e círculo; retorna `null` para classes sem tabela implementada ou nível de classe inválido, e zero quando a classe ainda não conhece magias daquele círculo.
- A tabela de magias conhecidas permanece separada dos espaços diários e dos bônus de atributo. Carisma alto não altera o total de magias conhecidas do Bardo/Feiticeiro.
- Testes acrescentados para níveis iniciais, transições de círculos, níveis máximos e entradas inválidas.
- Escopo ainda parcial: não foram implementadas aqui as progressões de magias por dia, nem as tabelas de Mago, Clérigo, Druida, Paladino e Ranger; a aba Magias ainda não consome esta API.


## Progressão de espaços diários — Bardo e Feiticeiro (2026-10-27)

- Transcritas as tabelas de espaços-base por dia das Tabelas 3-4 (Bardo) e 3-9 (Feiticeiro) do PDF brasileiro do *Livro do Jogador* (páginas impressas 24 e 33).
- Adicionada a API `baseDailySlots(className, classLevel, spellLevel)`, separada de magias conhecidas e dos espaços bônus por atributo. Valores zero representam círculos sem espaços-base na linha; `null` indica classe/tabela não implementada ou entrada inválida.
- Testes de regressão cobrem níveis iniciais, desbloqueio de círculos, níveis avançados e entradas não suportadas.
- Correção estrutural no mesmo arquivo: as tabelas de magias conhecidas estavam fora do objeto `SpellcastingRules`; foram recolocadas dentro do objeto junto com as novas tabelas para que a API seja compilável.
- Escopo continua parcial: progressões diárias e conhecidas de Mago, Clérigo, Druida, Paladino e Ranger, integração da aba Magias e composição de espaços totais com bônus de atributo continuam pendentes. Não considerar a auditoria concluída nem fazer merge antes de CI verde e integração funcional.


### CI #1372 — correção da tabela diária do Feiticeiro (2026-10-27)

- A CI executou 279 testes e encontrou uma falha no teste de espaços diários do Feiticeiro no 14º nível, círculo 7.
- Conferência direta da Tabela 3-9 no PDF brasileiro (página impressa 33) confirmou a progressão dos círculos 7º a 9º: o 7º começa no 14º nível com 3 espaços; o 8º começa no 16º com 3; o 9º começa no 18º com 3. A linha do 20º nível tem 6 espaços para cada círculo de 0º a 9º.
- Corrigida a transcrição dos níveis 14–20. A execução #1372 falhou somente no teste citado; é necessário aguardar nova CI após a correção.


### Revisão adicional do teste diário do Feiticeiro — nível 20 (2026-10-27)

- Na revisão após a CI #1374, foi encontrada uma segunda expectativa inconsistente no teste: o 20º nível, círculo 9, ainda esperava 4 espaços-base.
- A Tabela 3-9 do PDF brasileiro do *Livro do Jogador* (página impressa 33) mostra 6 espaços-base por dia em cada círculo de 0º a 9º no 20º nível. A tabela de implementação já refletia esse valor; o defeito estava na expectativa do teste.
- Corrigido o teste para esperar 6 espaços no 9º círculo. Esta alteração alinha a regressão com a fonte normativa; não comprova a CI até que uma nova execução termine.
- A auditoria continua aberta: faltam tabelas por classe e integração da aba Magias. Não fazer merge da PR #265 enquanto a CI não estiver verde e o escopo acordado não estiver funcional.


### Progressão diária de magias do Druida (2026-10-27)

- **Fonte normativa:** Tabela 3-8, “O Druida”, no PDF brasileiro do *Livro do Jogador* (capítulo 3, página do arquivo 32; tabela impressa no capítulo de classes).
- **Implementação:** transcrita a progressão diária-base completa do Druida, níveis 1–20 e círculos 0º–9º, em `SpellcastingRules.druidDaily`. A API `baseDailySlots` agora consulta essa tabela além das tabelas já existentes de Bardo e Feiticeiro.
- **Separação de regras:** a tabela representa espaços-base; bônus por Sabedoria permanecem calculados separadamente. O Druida não recebe o espaço adicional de domínio da tabela do Clérigo.
- **Regressão:** testes para níveis 1, 3, 7, 13 e 20, incluindo acesso a círculos altos e entrada inválida acima do nível 20.
- **Escopo:** somente a progressão diária do Druida foi adicionada nesta rodada. Progressões de Mago e Clérigo (incluindo espaço de domínio), Paladino e Ranger, além da integração da aba Magias, seguem pendentes. CI ainda precisa validar a alteração antes de qualquer merge.


## Progressão diária de magias do Clérigo (2026-10-26)

- Fonte normativa: Livro do Jogador brasileiro, Tabela 3-6 (Clérigo), conferida em conjunto com a progressão diária já transcrita da Tabela 3-8 (Druida).
- `baseDailySlots("Clérigo", nível, círculo)` agora consulta a progressão-base diária correspondente, sem incorporar espaços adicionais por atributo nem o espaço de domínio.
- Adicionada `domainSlots(...)`: retorna um espaço de domínio para círculos de 1º a 9º que o Clérigo já pode conjurar; retorna zero para truques e círculos ainda indisponíveis. Classes sem domínio ou entradas inválidas retornam `null`.
- Testes de regressão cobrem níveis baixos e altos, disponibilidade de círculos e separação entre espaço-base e espaço de domínio.
- Pendente nesta linha: transcrever e testar a tabela diária do Mago; depois implementar as progressões de Paladino e Ranger e integrar os cálculos à aba Magias. A validação CI ainda deve ser conferida antes de qualquer merge.


## Progressão diária de magias do Mago (2026-10-26)

- Fonte normativa: Livro do Jogador brasileiro, Tabela 3-13 (O Mago), página impressa 45.
- Adicionada a tabela completa de espaços-base por dia para níveis de classe 1–20 e círculos 0–9 em `SpellcastingRules.baseDailySlots`. Espaços adicionais por Inteligência continuam separados.
- Testes de regressão cobrem níveis iniciais, transições para círculos superiores, nível 20 e entrada inválida.
- A tabela ainda não está integrada à interface de Magias. Permanecem pendentes a revisão/validação da tabela e os cálculos de Paladino e Ranger, além da integração da UI. Não fazer merge antes da CI e da conclusão do escopo acordado.


## Correção da progressão diária do Feiticeiro — nível 13 (2026-10-26)

- A execução CI #1381 apontou falha no teste de espaços diários do Feiticeiro: a tabela tinha 19 linhas em vez de 20, deslocando os valores a partir do nível 13.
- Inserida a linha do 13º nível, mantendo o acesso ao 7º círculo para o 14º nível e a progressão dos níveis seguintes alinhada à Tabela 3-9 do Livro do Jogador brasileiro.
- A correção foi commitada; é necessário aguardar nova CI na cabeça atual para confirmar que o teste e a compilação passam.


### Ajuste do teste legado do Mago após inclusão da tabela (2026-10-26)
- O teste de espaços diários do Feiticeiro continha uma expectativa antiga que afirmava que a tabela do Mago não existia. Como a Tabela 3-13 foi implementada nesta mesma PR, o teste foi atualizado para verificar quatro espaços-base de 9º círculo no 20º nível do Mago.
- Isso evita que o teste legado contradiga a implementação recém-adicionada. A CI deve confirmar o resultado no commit atualizado.


## Progressão diária de magias do Ranger (2026-10-26)

- Fonte normativa: Livro do Jogador brasileiro, Tabela 3-17 (O Ranger), página impressa 48.
- Adicionada a tabela de espaços-base por dia para os níveis 1–20 e círculos 0–4 em `SpellcastingRules.baseDailySlots`. O Ranger não recebe espaços-base antes do 4º nível; a tabela preserva os zeros explícitos de transição.
- Incluídos testes de regressão para o 4º, 6º, 8º, 10º, 12º, 14º, 15º e 20º níveis, além de entrada inválida.
- A execução CI #1388 passou no commit anterior, mas esta alteração ainda precisa de uma nova CI. A progressão do Paladino e a integração à aba Magias continuam pendentes.
- Correção documental: o sumário do PDF brasileiro localiza a Tabela 3-13 (O Mago) na página impressa 40; a anotação anterior que indicava página 45 estava incorreta.
