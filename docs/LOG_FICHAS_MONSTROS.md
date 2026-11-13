# Log de implementação — Fichas do Livro dos Monstros

## 2026-11-12 — Estrutura visual inicial

### Decisões consolidadas
- Não pesquisar, baixar ou inserir imagens automaticamente.
- Reservar em cada ficha uma área visual vazia para imagem, que será escolhida manualmente pelo usuário em etapa futura.
- Reaproveitar o catálogo `CreatureCatalog` e seus 230 registros existentes; não duplicar dados.
- Manter a terminologia e a ordem conceitual do Livro dos Monstros 3.5 brasileiro.
- A tela de bestiário deve funcionar mesmo quando não houver personagem criado ou selecionado.

### Implementado nesta etapa
- Nova aba **Monstros** na navegação principal.
- Busca por nome e filtros combináveis por tipo e tamanho.
- Lista resumida com nome, tamanho, tipo e Nível de Desafio.
- Ficha detalhada com área reservada à imagem e os campos atualmente disponíveis no modelo:
  - Dados de Vida e PV;
  - Iniciativa e Deslocamento;
  - Classe de Armadura, incluindo CA de toque e surpresa no texto original;
  - Ataque Base/Agarrar, Ataque e Ataque Total;
  - Espaço/Alcance, Ataques Especiais e Qualidades Especiais;
  - Testes de Resistência;
  - seis Habilidades;
  - Perícias e Talentos;
  - Ambiente, Organização, Nível de Desafio, Tesouro, Tendência, Progressão e Ajuste de Nível.
- A navegação para a aba Monstros não depende da existência de ficha de personagem.

### Referência de regras e estrutura
Conferência inicial no PDF brasileiro fornecido, Introdução, páginas impressas 5–6 (páginas do arquivo PDF 9–10): o verbete apresenta bloco de estatísticas com nome, tamanho/tipo, Dados de Vida, Iniciativa, Deslocamento, CA, Ataque Base/Agarrar, Ataque, Ataque Total, Espaço/Alcance, ataques/qualidades especiais, resistências, habilidades, perícias, talentos, ambiente, organização, ND, tesouro, tendência, progressão e ajuste de nível. O livro também prevê texto descritivo e explicação de Combate após o bloco.

### Pendências conhecidas — não tratar como concluídas
- Extrair e conferir descrição narrativa e seção Combate de cada criatura, sem completar lacunas por inferência.
- Representar variantes/versões alternativas quando houver múltiplos blocos de estatísticas no mesmo verbete.
- Tornar o espaço de imagem capaz de receber e persistir uma imagem local escolhida pelo usuário; nesta etapa ele é apenas um placeholder.
- Auditar a cobertura do catálogo de 230 registros contra o índice/listagem do livro. A contagem atual não comprova, sozinha, cobertura integral do livro.
- Criar testes de interface e executar a suíte Gradle/CI.

### Regra contra redundância
Não reimplementar a listagem, busca, filtros por tipo/tamanho ou os campos mecânicos já presentes nesta etapa. Próximas alterações devem avançar nas pendências acima e atualizar este log.


### Complemento de estrutura — 2026-11-12
- A ficha também reserva seções próprias para **Descrição** e **Combate**. Elas permanecem explicitamente identificadas como conteúdo pendente até que o texto seja extraído e conferido no PDF; nenhum texto de regra é presumido.


## 2026-11-13 — Primeira criatura com descrição e Combate conferidos

### Abocanhador Matraqueante
- **Fonte:** Livro dos Monstros brasileiro, página impressa 8 (página 12 do PDF fornecido).
- **Descrição:** transcritos os cinco parágrafos descritivos do verbete, preservando as informações e a terminologia da edição.
- **Combate:** transcrito o parágrafo sob o título “Combate”, sem incorporar textos de habilidades especiais de outras partes do verbete.
- **Dados mecânicos:** mantidos no registro já existente; nenhuma ficha duplicada foi criada.
- **Imagem:** placeholder vazio mantido; nenhuma imagem foi pesquisada, baixada ou inserida.

### Alterações estruturais
- O modelo CreatureDefinition agora aceita os campos opcionais description e combat, com valor vazio para registros ainda não auditados.
- O carregador JSON lê esses campos sem exigir que as 229 fichas restantes já os possuam.
- A interface exibe o texto transcrito quando disponível e mantém o aviso de pendência quando o campo estiver vazio.
- Próxima criatura: seguir a ordem alfabética do livro e conferir o verbete diretamente no PDF antes de registrar o texto.


### Achaierai
- **Fonte:** Livro dos Monstros brasileiro, página impressa 10 (PDF, página 14: descrição e bloco; página impressa 11/PDF 15: seção Combate).
- **Descrição:** transcritos os três parágrafos do verbete.
- **Combate:** transcritos os dois parágrafos táticos e a descrição de Nuvem Negra apresentada sob o título Combate.
- **Dados mecânicos:** registro existente reaproveitado, sem duplicação.
- **Imagem:** placeholder vazio mantido; nenhuma imagem foi pesquisada, baixada ou inserida.


## 2026-11-13 — Confirmação funcional da aba Monstros
- **Verificação manual do usuário:** a aba Monstros foi adicionada corretamente e está presente no aplicativo.
- Considerar a integração da aba à navegação como verificada; não repetir esse teste sem alteração relevante na navegação ou no ponto de entrada.
- **CI Android:** execução `43352262968` concluída com sucesso; testes unitários, compilação Android e publicação do APK de debug concluídos.
- A confirmação manual da aba e o resultado desse workflow ficam registrados separadamente. Antes do merge, conferir também o estado de validação associado ao commit mais recente do PR.


## 2026-11-13 — Abolete e variante Abolete Mago
- **Fonte:** Livro dos Monstros brasileiro, páginas impressas 9–10 (PDF, páginas 13–14).
- **Catálogo:** incluídos dois blocos distintos apresentados lado a lado no livro: Abolete (ND 7) e Abolete Mago, Mago de 10º Nível (ND 17). A variante foi mantida como registro próprio para preservar seus Dados de Vida, atributos, perícias, talentos e magias.
- **Descrição:** transcritos os parágrafos descritivos próprios do Abolete e do Abolete Mago.
- **Combate:** transcritos os trechos de combate e as habilidades descritas sob essa seção para ambos os blocos. As magias preparadas do mago foram mantidas no texto de Combate por constarem nesse trecho do verbete.
- **Imagem:** placeholders continuam vazios; nenhuma imagem pesquisada, baixada ou inserida.
- **Conferência:** conferir visualmente no app a apresentação dos campos longos e validar a interpretação dos Dados de Vida mistos do Abolete Mago antes de considerar o verbete encerrado.


## 2026-11-13 — Águia Gigante
- **Fonte:** Livro dos Monstros brasileiro, página impressa 11 (página 15 do PDF fornecido).
- **Descrição:** transcritos os quatro parágrafos do verbete, incluindo habitat social, comportamento predatório, dimensões e idiomas.
- **Combate e informações associadas:** transcritos os dois parágrafos táticos, Evasão, bônus racial de Observar, instruções de treinamento, valores de ovos/filhotes e capacidade de carga. Os subtítulos originais foram preservados dentro do campo narrativo disponível, sem criar campos de dados que o modelo ainda não possui.
- **Dados mecânicos:** ficha existente reaproveitada; nenhum registro duplicado.
- **Imagem:** placeholder vazio; nenhuma imagem pesquisada, baixada ou inserida.
- **Próxima criatura:** Allip (ordem alfabética do catálogo/livro).


## 2026-11-13 — Allip
- **Fonte:** Livro dos Monstros brasileiro, páginas impressas 11–12 (páginas 15–16 do PDF fornecido; bloco de estatísticas na p. 11 e descrição/Combate na p. 12).
- **Descrição:** transcritos os três parágrafos descritivos, incluindo a origem espectral e a incapacidade de falar.
- **Combate:** transcritos o parágrafo tático e as três habilidades descritas sob Combate: Murmúrio, Enlouquecer e Drenar Sabedoria.
- **Dados mecânicos:** registro existente reaproveitado, sem duplicação.
- **Imagem:** placeholder vazio; nenhuma imagem pesquisada, baixada ou inserida.
- **Próxima criatura:** Anão (o livro inicia o verbete na mesma página 12; preservar a separação entre ficha de monstro e bloco racial quando aplicável).


## 2026-11-13 — Anjo, Deva Astral
- **Fonte:** Livro dos Monstros brasileiro, página impressa 19 (página 23 do PDF fornecido).
- **Descrição:** transcritos os três parágrafos específicos do deva astral.
- **Combate:** transcritos os parágrafos táticos, o uso de armas sagradas, a lista de habilidades similares a magia e as habilidades Atordoamento e Esquiva Sobrenatural.
- **Escopo:** as características gerais compartilhadas por todos os anjos aparecem em um bloco comum no livro; não foram copiadas como se fossem texto exclusivo do deva astral. Permanecem representadas pelas qualidades especiais já presentes na ficha numérica quando aplicável.
- **Dados mecânicos:** registro existente reaproveitado, sem duplicação.
- **Imagem:** placeholder vazio; nenhuma imagem pesquisada, baixada ou inserida.


## 2026-11-13 — Anjo, Planetário
- **Fonte:** Livro dos Monstros brasileiro, páginas impressas 19–20 (PDF, páginas 23–24).
- **Descrição:** transcritos os dois parágrafos específicos do planetário, incluindo sua aparência, função como general dos exércitos celestiais e dimensões.
- **Combate:** transcritos os trechos táticos, armas sagradas, Regeneração, habilidades similares a magia, habilidades sempre ativas, capacidade de conjuração como clérigo de 17º nível e lista de magias preparadas dos níveis 0 a 9.
- **Conferência adicional:** a página impressa 20 foi conferida visualmente para confirmar a continuação da lista de magias preparadas, incluindo as magias de 9º nível e a nota sobre os domínios Destruição e Bem.
- **Dados mecânicos:** registro existente `anjo-planetario` reaproveitado; nenhum dado numérico foi alterado.
- **Imagem:** placeholder vazio; nenhuma imagem pesquisada, baixada ou inserida.
- **Contagem:** 264 registros, 264 IDs únicos; 8 fichas com Descrição e Combate.
- **Próxima criatura:** Anjo, Solar.


## 2026-11-13 — Anjo, Solar
- **Fonte:** Livro dos Monstros brasileiro, páginas impressas 20–21 (PDF, páginas 24–25).
- **Descrição:** transcritos os dois parágrafos específicos do solar, incluindo aparência, papel entre os anjos, voz e dimensões.
- **Combate:** transcritos os trechos táticos, armas sagradas, Regeneração, habilidades similares a magia, habilidades sempre ativas, conjuração divina como clérigo de 20º nível e magias preparadas dos níveis 0 a 9.
- **Dados mecânicos:** registro existente `anjo-solar` reaproveitado; nenhum dado numérico foi alterado.
- **Imagem:** placeholder vazio; nenhuma imagem pesquisada, baixada ou inserida.
- **Contagem:** 264 registros, 264 IDs únicos; 9 fichas com Descrição e Combate.
- **Próxima criatura:** seguir a ordem alfabética do Livro dos Monstros após os anjos.


## 2026-11-13 — Elegibilidade para companheiro animal, invocação e familiar

### Alteração solicitada
Cada registro do catálogo recebe três flags independentes:
- `animalCompanion`: a criatura consta na lista básica ou em uma lista alternativa de companheiros animais do druida/ranger.
- `summonedCreature`: a criatura consta em uma lista de **Invocar Criaturas** ou **Invocar Aliado da Natureza**.
- `familiar`: a espécie consta na tabela básica de familiares do Livro do Jogador.

### Fonte e critério
- Conferência feita no PDF brasileiro do **Livro do Jogador 3.5**, nas seções de Companheiro Animal, Companheiros Animais Alternativos, Familiares, Invocar Criaturas e Invocar Aliado da Natureza.
- O tipo de criatura, tamanho ou aparência não são usados como substitutos da lista de regras: somente entradas identificadas nas listas recebem a flag.
- As listas alternativas de companheiro animal preservam a restrição de nível efetivo do druida/ranger indicada pelo livro; a flag sinaliza elegibilidade eventual, não disponibilidade em qualquer nível.
- A flag de invocação abrange as duas famílias de magia. Algumas entradas de **Invocar Criaturas** são formas celestiais/abissais; a flag indica a entrada correspondente da lista de invocação, não que o bloco-base sem modelo seja idêntico à criatura invocada.
- Familiares são limitados às espécies indicadas na tabela básica; a flag não presume acesso a familiares especiais de suplementos ou talentos que não constam no escopo desta conferência.

### Interface e compatibilidade
- As três flags são independentes: uma criatura pode receber nenhuma, uma, duas ou as três.
- As flags são exibidas na ficha detalhada em uma seção própria.
- Registros antigos/JSON sem essas propriedades continuam compatíveis: o carregador assume `false` para campos ausentes.
- Não foram alterados os blocos de estatísticas, nomes, descrições ou textos de combate das criaturas.

### Validação
- Testes de regressão cobrem lobo, coruja, gato e abolete, incluindo sobreposição entre categorias e um caso sem elegibilidade.
- Registros do catálogo: 264; IDs únicos preservados.
- IDs candidatos que não existem no catálogo atual e, portanto, não receberam registro novo: texugo-atroz, xorn, corvo-abissal, bruxa-da-noite.


### Revisão de cobertura das listas de invocação
- Conferidas também as progressões de centopeias, aranhas e escorpiões monstruosos presentes na tabela de **Invocar Criaturas**. Todas as fichas correspondentes existentes no catálogo receberam a flag de invocação.
- O teste automatizado verifica os tamanhos registrados para esses três grupos, evitando que uma futura edição marque apenas uma variante e deixe as demais sem indicação.
- Total atualizado: 264 fichas; 45 companheiros animais; 103 criaturas invocáveis; 10 familiares.

- Ajuste de nomenclatura: a entrada inglesa *dire weasel* da lista alternativa corresponde ao **Arminho Atroz** do Livro dos Monstros brasileiro; a ficha existente `arminho-atroz` foi marcada como companheiro animal, sem criar duplicata.

- Complemento de apresentação: as categorias elegíveis também aparecem resumidas diretamente em cada linha da lista de monstros, além da seção completa na ficha detalhada.


## 2026-11-13 — Atalhos de monstros no modo Mestre

### Decisão de arquitetura
- A ação **Adicionar monstro** no modo Mestre cria um card/atalho para uma criatura já existente em `CreatureCatalog`.
- O atalho guarda somente um identificador próprio da instância e o `creatureId` de origem. Não cria, duplica ou converte `CharacterEntity`.
- Várias instâncias da mesma criatura são permitidas, cada uma com identidade independente.
- Remover um card remove somente o atalho da sessão; o registro original do bestiário permanece intacto.
- Os atalhos são privados do Mestre nesta etapa e são persistidos no snapshot da sessão hospedada. Não são transmitidos aos jogadores.
- A seleção reutiliza o catálogo existente e oferece busca por nome e filtros por tipo e tamanho. Os filtros e a busca da aba Monstros não são modificados.
- Toque simples alterna frente/verso do card; pressão prolongada abre os detalhes disponíveis da criatura.
- Os cards usam os dados do catálogo em tempo de exibição. Campos ainda não auditados continuam identificados como pendentes; a imagem permanece no estado de placeholder existente.
- A coleção aparece na área de Combate do Mestre como uma pilha visual. Nesta etapa, os cards são atalhos de consulta e ainda não são inseridos automaticamente na ordem de iniciativa.
- O snapshot mantém compatibilidade com sessões anteriores: ausência do campo `monsterCards` é interpretada como lista vazia, sem aumentar a versão do formato.

### Testes adicionados
- Serialização e restauração dos cards no snapshot.
- Snapshot antigo/sem cards retorna coleção vazia.
- Adição e remoção de atalhos no estado do servidor sem criação de fichas.


## 2026-11-13 — Baralho de monstros em sobreposição de tela inteira

### Alteração solicitada
- Substituir o seletor compacto em janela de diálogo por uma experiência visual de baralho que ocupe a tela inteira.
- Evitar a janela central limitada: pesquisa, filtros e catálogo passam a ocupar uma camada integral, com rolagem própria para a lista.
- A seleção mantém a busca por nome e os filtros por tipo e tamanho já existentes.
- A ficha completa também passa a ocupar a tela inteira, com retorno explícito ao baralho e conteúdo organizado em cartões/sections roláveis.
- O baralho da sessão continua usando atalhos para o catálogo; nenhuma ficha de personagem é criada e os dados mecânicos não são duplicados.
- O comportamento de adicionar/remover e a persistência dos atalhos permanecem inalterados.

### Implementação visual
- Sobreposição de tela inteira sem moldura de AlertDialog.
- Cabeçalho e ação de fechar separados da lista.
- Campo de busca e filtros mantidos no topo.
- Lista de criaturas em LazyColumn com rolagem independente e cartões maiores, simulando cartas de um baralho.
- Detalhes completos em uma camada integral rolável, com retorno ao baralho.

### Validação pendente
- Aguardar CI para validar compilação e testes automatizados.
- Testar no aparelho a abertura/fechamento, botão Voltar do Android, rolagem da lista, filtros, seleção e visualização de detalhes.
- Não mesclar até a validação manual do usuário.


## 2026-11-13 — Identidade visual de cartas e baralho empilhado

### Referências visuais recebidas
- Primeira imagem: cartas físicas parcialmente sobrepostas, com deslocamento horizontal regular e as bordas das cartas posteriores visíveis.
- Segunda imagem: hierarquia de carta colecionável com faixa de título, grande área de ilustração, linha de tipo, área de texto/regras e bloco de valores no rodapé.

### Decisões de implementação
- A coleção do Mestre passa a ser um baralho horizontal com cartas sobrepostas e navegação lateral, em vez de cartões largos empilhados verticalmente.
- Cada carta adota proporção vertical e moldura própria, com cabeçalho do nome/ND, área de ilustração, linha de tipo, painel de estatísticas e rodapé.
- A frente prioriza PV, CA, BBA, iniciativa e ataque.
- O verso organiza os seis atributos, ataques/qualidades especiais e talentos.
- Toque alterna frente/verso; pressão prolongada abre os detalhes completos; remoção continua vinculada à instância da sessão.
- O seletor e os detalhes completos mantêm os atalhos de catálogo e a persistência existentes.
- A área de ilustração continua como placeholder: não foi criada nem presumida uma fonte de imagens para criaturas.

### Validação
- Aguardar CI da nova revisão.
- Testar no aparelho o deslocamento horizontal, a sobreposição, o toque para virar, pressão prolongada e remoção de instâncias duplicadas.
- Não mesclar antes da validação manual.


## Correção de compilação — 2026-11-13

A primeira execução de CI após a reformulação visual apontou referências ausentes em `SessionHubScreen.kt`. Foram implementados no mesmo arquivo e na mesma branch:

- Seletor de criaturas com busca por nome e filtros combináveis por tipo e tamanho, usando diretamente `CreatureCatalog.filter`.
- Diálogo de detalhes que consulta os dados originais de `CreatureDefinition`, sem criar ou modificar fichas.
- Funções auxiliares locais para exibir modificadores e valores positivos com sinal.
- Correção da separação entre o título e o botão de fechar no cabeçalho dos painéis flutuantes, que causava erro adicional de análise Kotlin.

A alteração foi enviada ao PR #367 no commit `6b8a5eb32cf202c1393ad95803500f7e4db5ce37`. A validação final depende da nova execução do GitHub Actions; não considerar aprovado até testes e compilação concluídos.


## 2026-11-13 — Regras e vínculos persistentes de familiares/companheiros

### Implementação
- Criado o modelo `CharacterCreatureBond`, que armazena somente o ID da criatura do catálogo e o tipo de vínculo; não duplica fichas de monstros.
- Adicionada a coluna `creatureBondsJson` à ficha, com migração Room 10→11 e valor padrão vazio para preservar personagens existentes.
- Serialização/deserialização tolerante a dados antigos e inclusão do campo nos backups.
- Criado `CreatureBondRules` para filtrar familiares e companheiros conforme classes e níveis atuais.
- Mago e Feiticeiro acumulam níveis para determinar o familiar.
- Druida soma seus níveis aos níveis de Ranger divididos por dois (arredondamento para baixo) para o companheiro animal.
- Listas alternativas de companheiros aplicam requisitos de 4º/7º nível e penalidades efetivas de -3/-6.
- Opções aquáticas permanecem ocultas por padrão e só aparecem quando a campanha é explicitamente marcada como aquática.
- Adicionada à ficha uma seção com consulta dos vínculos atuais e seleção filtrada de novas criaturas.
- Corrigido o teste de migração da base para percorrer a cadeia 9→10→11 e verificar que o novo campo começa vazio.

### Testes adicionados
- Progressão multiclasse de familiar e companheiro animal.
- Penalidades e requisitos das listas alternativas.
- Restrição de companheiros aquáticos.
- Validação de vínculos contra as flags do catálogo.

### CI
- Corrigida a estrutura duplicada/inválida de gatilhos no workflow Android.
- A CI agora executa em pushes nas branches `main`, `fix/**` e `feature/**`, além de PRs direcionadas a `main`.
- O workflow usa JDK 17, alinhado à configuração atual do projeto.

- Revisão da tabela oficial de Invocar Criaturas: removida a elegibilidade de `escorpiao-colossal`; o Livro do Jogador lista o escorpião monstruoso abissal Imenso (`escorpiao-imenso`), não a versão Colossal.
- Criado mapeamento explícito de círculos para as listas de Invocar Criaturas e Invocar Aliado da Natureza. Opções de círculos inferiores permanecem disponíveis conforme a descrição das magias.

- Ajustados os testes do catálogo para refletir a lista impressa: Escorpião Colossal não é invocável pelo Livro do Jogador; Escorpião Imenso é a opção válida. A migração 10→11 foi tornada idempotente para suportar bancos de teste e instalações intermediárias que já tenham a coluna.

- Auditoria ampliada da tabela de Companheiros Animais Alternativos: adicionados os patamares de 10º, 13º e 16º nível, com penalidades -9, -12 e -15; opções aquáticas foram limitadas aos ambientes aquáticos. Flags do catálogo foram alinhadas à tabela do Livro do Jogador, incluindo Megaraptor, Baleia Orca, Leão Atroz, Tubarão Enorme, Elefante, Urso Atroz, Polvo Gigante, Lula Gigante, Tricerátops, Tigre Atroz, Tubarão Atroz e Tiranossauro.

- Ajustado o workflow auxiliar Android para executar somente `testDebugUnitTest`; a geração do APK permanece no workflow de build existente, que restaura o keystore de depuração. Isso evita falha duplicada de assinatura causada por `./gradlew build` no workflow auxiliar.

- Conferidas as tabelas completas de Invocar Aliado da Natureza e Invocar Criaturas: adicionadas as cartas existentes Urso Atroz, Elefante, Polvo Gigante e Lula Gigante às listas e tags de invocação. Pixie agora possui as três variantes de lista (6º, 7º e 9º círculo); Satyr mantém as versões sem/com flauta (3º e 5º).

- Corrigida lacuna no catálogo de magias: adicionadas Invocar Aliado da Natureza V e VI, ausentes da lista de magias apesar de constarem no Livro do Jogador. O domínio Animais concede Invocar Aliado da Natureza IV e VIII (não I); a seleção para Clérigo agora respeita apenas esses círculos de domínio.
