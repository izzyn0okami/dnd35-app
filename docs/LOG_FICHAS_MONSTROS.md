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

- Adicionados testes para as listas superiores e variantes de Pixie, além de regressões para as cartas de invocação que estavam sem a flag (urso-atroz, elefante, polvo-gigante, lula-gigante). A interface identifica os modelos Celestial/Abissal e informa que os modificadores completos ainda não são aplicados à ficha de combate.

- Completo o catálogo de Invocar Criaturas II–VI, que também estavam ausentes do catálogo apesar de existirem no Livro do Jogador. Invocar Criaturas IX mantém os domínios Bem, Caos, Mal e Ordem; a seleção de invocações agora reconhece qualquer domínio clerical explicitamente registrado na magia. Ícones de todas as magias de invocação foram alinhados.

- Auditoria das listas de Invocar Aliado da Natureza: adicionadas as opções já existentes no catálogo que estavam sem a tag de invocação (Janni, Leão Marinho e Grig), nos círculos 5, 4 e 9, respectivamente.

- Incluídas no catálogo as duas opções de companheiro animal que existiam no Livro dos Monstros, mas ainda não tinham cartas: Texugo Atroz e Cobra Constritora Gigante. Ambas receberam dados do bloco oficial e tags de companheiro/invocável; foram adicionadas às listas corretas de Invocar Aliado da Natureza, Invocar Criaturas e Companheiro Animal.

- Pendência de cobertura identificada ao confrontar as listas do Livro do Jogador com o Livro dos Monstros: ainda faltam cartas para algumas opções de invocação, incluindo famílias de Elementais (quatro elementos e seis estágios), Xorns, Tojanidas, Salamandras, Gaviões Seta e Unicórnios, entre outras. Essas criaturas não são oferecidas como opções até que suas fichas oficiais sejam cadastradas; não criar placeholders nem inventar estatísticas. Texugo Atroz e Cobra Constritora Gigante já foram adicionados por serem necessários também às listas de Companheiro Animal.


## 2026-11-13 — Acabamento visual dos menus de criaturas em deck

### Escopo
- Mantidas integralmente as regras já implementadas de invocação, familiar e companheiro animal. Esta alteração é exclusivamente de apresentação e navegação.
- Criado um componente compartilhado de sobreposição integral (CreatureDeckOverlay) e um componente visual de carta (CreatureTradingCard), para manter a mesma identidade entre os diferentes menus de criaturas.
- Os diálogos de criaturas deixam de usar a janela pequena central de AlertDialog e passam a ocupar a tela, com cabeçalho, ação de fechar e área de cartas rolável.
- A consulta dos vínculos atuais exibe cada familiar/companheiro em uma carta individual; quando há múltiplos vínculos, as cartas aparecem em sequência no mesmo deck.
- A seleção de familiares e companheiros exibe cartas selecionáveis, mantendo o controle de opções aquáticas e os avisos de restrição de nível.
- O seletor de criaturas invocadas exibe cartas com círculo da lista e variante Celestial/Abissal, preservando a seleção e o registro no histórico da magia.
- O seletor de criaturas do Mestre preserva pesquisa, filtros por tipo/tamanho e o callback de adição à sessão, mas passa a apresentar resultados como cartas em uma tela integral.
- A área de ilustração é um espaço reservado estilizado; não foi associada arte fictícia nem alterado o catálogo.
- Nenhum dado mecânico, vínculo salvo, serialização, elegibilidade ou regra de acesso foi alterado.

### Validação pendente
- Aguardar CI e build Android desta branch.
- Testar no aparelho: abrir/fechar os quatro menus, rolagem de cartas, seleção, botão Voltar do Android, filtros do bestiário e alternância de opções aquáticas.
- Confirmar que a seleção continua salvando vínculos e registrando a criatura invocada como antes.


### Correção complementar — remoção de vínculos
- Restaurada a ação explícita **Remover vínculo** em cada carta de familiar ou companheiro animal atualmente vinculado.
- A remoção exige confirmação e altera somente a lista de vínculos salva no personagem; não apaga a criatura do bestiário nem altera suas regras ou dados.
- A remoção de uma carta não afeta os demais vínculos. O usuário pode adicionar outra criatura posteriormente.


## 2026-11-14 — Integração das ilustrações e padronização das cartas

### Imagens
- A pasta `app/src/main/assets/monsters/` foi integrada à branch de cartas sem copiar nem renomear os arquivos.
- Conferência por nome normalizado (sem acentos e ignorando espaços, hífens e sublinhados): **266 imagens para 266 registros do catálogo; 266 correspondências; nenhuma criatura sem imagem**.
- As ilustrações são carregadas sob demanda a partir de Assets, com decodificação fora da thread principal e redução de resolução para limitar memória.
- A frente das cartas usa enquadramento preenchido; a ficha ampliada usa `ContentScale.Fit` para exibir a imagem inteira, sem cortes.

### Interação comum das cartas
- Cartas do bestiário, familiares, companheiros e criaturas invocadas usam o mesmo componente visual.
- Toque simples alterna frente e verso.
- Pressão prolongada abre a ficha completa em tela integral, incluindo a ilustração inteira e os dados disponíveis no catálogo.
- Nos seletores, a ação de vincular/invocar/adicionar fica no verso da carta; virar a carta não confirma a seleção acidentalmente.
- A seção de familiares e companheiros exibe as criaturas vinculadas como cartas diretamente na ficha, preservando a remoção do vínculo e as regras de elegibilidade já existentes.

### Baralho de combate do Mestre
- A pilha horizontal passou a usar posicionamento explícito de sobreposição: cartas consecutivas avançam 72 dp, mantendo 148 dp da carta anterior visíveis.
- A rolagem horizontal permanece disponível quando o número de cartas ultrapassa a largura da tela.
- As cartas de combate agora usam a ilustração correspondente na frente; toque para virar e pressão prolongada para abrir a ficha completa continuam disponíveis.

### Escopo preservado
- Nenhuma estatística, regra de elegibilidade, progressão, serialização ou estrutura persistida de personagem/sessão foi alterada.
- Nenhuma imagem foi movida, renomeada ou duplicada.
- Validação de CI e conferência visual em aparelho permanecem pendentes para esta revisão.

## 2026-11-14 — Pilhas navegáveis, iniciativa de monstros e retrato do turno

### Decisões e implementação
- As listas de cartas do bestiário, seleção de monstros para a sessão, criaturas invocadas e criaturas vinculadas passam a usar uma pilha visual sobreposta.
- A carta da frente responde ao gesto horizontal: arrastar para a esquerda revela a próxima; arrastar para a direita retorna à anterior. A pilha mostra as cartas seguintes parcialmente, sem depender de uma rolagem horizontal longa.
- O baralho de monstros do Mestre usa o mesmo comportamento; toque continua virando a carta e pressão prolongada continua abrindo os detalhes.
- As criaturas adicionadas ao baralho da sessão passam a aparecer como candidatas no diálogo de início de combate. Cada instância recebe identificador próprio, PV derivados da ficha do bestiário e iniciativa informada pelo Mestre; a ordenação final continua sendo feita por iniciativa.
- O atalho flutuante multiplayer exibe um retrato circular do combatente ativo. No dispositivo do Mestre, a foto vem da ficha recebida do jogador ou da ficha NPC local; para monstros, usa a ilustração do bestiário. No dispositivo de jogador, a foto só pode ser exibida quando a ficha ativa está disponível localmente; o protocolo atual não distribui fotos personalizadas de outros participantes.
- A seção de criaturas vinculadas não exibe mais um cartão vazio ou aviso em fichas sem vínculos. Classes elegíveis ainda recebem ações compactas para criar o primeiro vínculo.

### Preservação
- As 266 ilustrações existentes em `assets/monsters` são reutilizadas; nenhuma imagem foi substituída, movida ou duplicada.
- Não foram alteradas regras de elegibilidade de familiares/companheiros, estatísticas das criaturas, regras de iniciativa, nem o protocolo multiplayer.
- A iniciativa de criaturas do baralho usa os valores de PV do catálogo como PV inicial; dano e cura passam a ser controlados pelo rastreador de combate.

### Validação pendente
- Executar testes unitários e compilação Android na CI da PR #368.
- Validar no aparelho: gesto para avançar/voltar em cada pilha, toque para virar, pressão prolongada, inclusão de monstros na iniciativa, ordem por iniciativa e retrato do turno.
- **Ajuste de CI:** a primeira compilação após a conversão das listas encontrou blocos antigos de `LazyColumn` que permaneceram após a substituição e um import de `zIndex` incorreto. Esses resíduos foram removidos/corrigidos; a nova execução está validando o head `25f914d6c1c6ad146f4d5c9022501dacc08a49f0`.


## 2026-11-14 — Cobertura das listas de invocação e vínculo com fichas

### Conferência do Livro do Jogador
- Tabelas de **Invocar Aliado da Natureza** e **Invocar Criaturas**, páginas impressas 210–211, conferidas novamente.
- As listas do app foram ampliadas para incluir todas as opções da tabela que já possuem ficha em `CreatureCatalog`, mantendo o círculo de origem e permitindo as listas inferiores conforme a magia.
- Corrigidas dez flags `summonedCreature` que estavam falsas apesar de a criatura constar nas tabelas: Carcajú Atroz, Javali Atroz, Tigre Atroz, Arconte Luminar, Bralani, Fera do Caos, Avoral, Corvo, Urso Polar e Víbora Enorme.
- A lista Celestial/Abissal agora registra as variantes no seletor sem criar uma ficha duplicada para o modelo. As variantes de Sátiro (com/sem flauta) e Pixie (sem flechas especiais, com flechas soníferas e com flechas soníferas/de perda de memória) permanecem separadas por círculo.
- O seletor continua usando o ID da ficha original do bestiário. Pressionar e segurar uma carta abre a ficha completa; virar a carta e acionar **Invocar** confirma a seleção. O texto do seletor foi atualizado para tornar esse caminho explícito.
- O histórico de magias também transforma a criatura registrada em um acesso direto à ficha completa: o registro persiste o ID do catálogo, e tocar no nome da criatura invocada abre seus detalhes. Isso mantém o vínculo mesmo após fechar o seletor ou reabrir a ficha.

### Fichas ainda ausentes no catálogo
Não foram criados placeholders nem estatísticas presumidas. As opções abaixo aparecem nas tabelas do Livro do Jogador, mas ainda não têm ficha própria em `CreatureCatalog`; por isso não são oferecidas no seletor até a extração e conferência dos blocos no Livro dos Monstros.

**Invocar Aliado da Natureza**
- Elementais: Pequeno (2º), Médio (4º), Grande (5º), Enorme (6º), Maior (7º) e Ancião (9º).
- Gavião-Seta: Jovem (4º), Adulto (5º) e Ancião (7º).
- Salamandras: Irmã de Chama (4º), Média (6º) e Nobre (8º).
- Tojanidas: Jovem (4º), Adulto (5º) e Anciã (8º).
- Xorns: Menor (4º), Médio (6º) e Ancião (7º).
- Unicórnio (4º) e Unicórnio, Arauto Celestial (9º).

**Invocar Criaturas**
- Elementais: Pequeno (3º), Médio (5º), Grande (6º), Enorme (7º), Maior (8º) e Ancião (9º).
- Insetos gigantes: Abelha e Besouro (2º), Louva-a-Deus (4º) e Gafanhoto (5º).
- Lêmure (2º), Cão Yeth (4º), Arconte Guardião (5º), Xill (6º), Leonal (9º) e Bruxa da Noite (9º).
- Baleia Celestial (7º); a tabela não especifica uma espécie de baleia para essa entrada.

As fichas-base devem ser reutilizadas por ID sempre que a tabela pedir apenas um modelo Celestial/Abissal; não duplicar registros para representar o modelo. Para Elementais, manter as fichas separadas por elemento e estágio, pois seus blocos de estatísticas e habilidades não são intercambiáveis.

### Testes
- Acrescentados testes para as criaturas existentes que estavam sem flag e para suas listas/círculos corretos.
- `unclassifiedCatalogCreatureIds()` continua exigindo que toda ficha marcada como invocável tenha um círculo explícito nas tabelas.
- Nenhuma estatística de criatura foi alterada nesta etapa.


## 2026-11-14 — Primeira expansão de fichas de invocação e auditoria de vínculos

- **Companheiros animais:** auditoria dos 58 IDs exigidos pelas regras de seleção já implementadas. Todos possuem registro no catálogo, estão marcados como companheiro animal e têm requisito de nível configurado; não foram criadas duplicatas.
- **Familiares:** as dez opções existentes do Livro do Jogador (Coruja, Corvo, Falcão, Gato, Lagarto, Morcego, Rato, Sapo, Texugo e Víbora Pequena) já possuem ficha e marcação de familiar. Nenhuma ficha nova era necessária nessa categoria.
- **Cão Yeth:** a ficha já existia, mas estava com `summonedCreature=false`. Corrigida a elegibilidade e incluído na lista de Invocar Criaturas IV.
- **Lêmure:** ficha adicionada com estatísticas conferidas no Livro dos Monstros brasileiro, p. 70; incluído em Invocar Criaturas II. A descrição e o resumo de combate foram redigidos de forma concisa a partir do verbete, sem copiar extensos trechos.
- **Imagens:** nenhuma imagem foi gerada ou associada nesta etapa. A regra visual passa a ser explícita: gerar/associar arte somente quando a ficha realmente não tiver imagem; preservar imagens já existentes. Não usar arte vetorial.
- **Pendências:** as demais criaturas listadas no registro de auditoria anterior ainda exigem extração e conferência individual dos blocos de estatísticas antes de entrar no catálogo. Não criar placeholders nem inferir valores.


## 2026-11-14 — Expansão das fichas de invocação: Gaviões-Seta, Tojanidas e Salamandras

- Foram adicionadas fichas completas para as três idades do Gavião-Seta (adolescente/jovem, adulto e ancião), com estatísticas da tabela do Livro dos Monstros, pp. 132–133.
- Foram adicionadas fichas completas para Tojanida Jovem, Tojanida Adulto e Tojanida Ancião, Livro dos Monstros, p. 241.
- Foram adicionadas fichas para Irmão das Chamas (Salamandra), Salamandra e Salamandra Nobre, Livro dos Monstros, p. 227. Mantida a nomenclatura do bestiário para a primeira forma.
- As entradas foram vinculadas às listas de Invocar Aliado da Natureza nos círculos correspondentes do Livro do Jogador: Gavião-Seta Jovem (4º), Adulto (5º), Ancião (7º); Tojanida Jovem (4º), Adulto (5º), Ancião (8º); Irmão das Chamas (4º), Salamandra (6º), Salamandra Nobre (8º).
- O Unicórnio foi adicionado ao catálogo com ID `unicornio` e vinculado ao círculo 4.
- Tojanida Adulto permanece no círculo 5, conforme a tabela; sua disponibilidade em círculos superiores é automática.
- Unicórnio foi incluído no catálogo e no círculo 4, sem duplicar registros. A variante “Unicórnio, Arauto Celestial” ainda precisa de extração e validação separadas.
- Nenhuma arte foi gerada ou substituída. As fichas que não possuem arte serão identificadas em auditoria própria para futura geração rasterizada; imagens existentes serão preservadas.


## 2026-11-14 — Xorns e vinculação das listas de invocação

- Adicionadas fichas do Xorn Menor, Xorn e Xorn Ancião com estatísticas do Livro dos Monstros, pp. 260–261.
- Vinculados às opções de Invocar Aliado da Natureza: Xorn Menor (4º), Xorn Médio (6º) e Xorn Ancião (7º).
- Gavião-Seta, Tojanida, Salamandra e Unicórnio também foram vinculados às listas e círculos conferidos no Livro do Jogador.
- O Xorn Médio é nomeado apenas “Xorn” no verbete do Livro dos Monstros; a identificação “Médio” é mantida na ficha para corresponder à designação da tabela de invocação.
- Permanecem pendentes as seis categorias de tamanho/estágio de Elementais (Água, Ar, Fogo e Terra), além de insetos gigantes, Arconte Guardião, Xill, Leonal, Bruxa da Noite, Baleia Celestial e a variante Unicórnio, Arauto Celestial.


## 2026-11-14 — Fichas completas dos quatro tipos de Elemental

- Adicionadas 24 fichas: Água, Ar, Fogo e Terra, cada qual em seis categorias: Pequeno, Médio, Grande, Enorme, Maior (Superior no verbete) e Ancião.
- Estatísticas extraídas das tabelas do Livro dos Monstros 3.5, pp. 101–104. As fichas incluem DV/PV, iniciativa, deslocamento, CA, ataques, espaço/alcance, ataques e qualidades especiais, resistências, habilidades, perícias, talentos, ND, progressão e tendência.
- A tabela do Elemental do Ar repete “Superior” na coluna final e repete os mesmos valores de DV, PV, CA, ataques, resistências e habilidades. Para permitir a opção “Elemental Ancião” da tabela do Livro do Jogador, a ficha de Ancião reproduz exatamente o bloco impresso repetido; não foram inventados valores para preencher a divergência editorial.
- O bloco do Elemental do Fogo Médio registra literalmente “4d5+8” no PDF, com 26 PV; a grafia foi preservada para auditoria posterior, sem substituir silenciosamente o dado por outro.
- Vinculação corrigida conforme Livro do Jogador, pp. 210–211:
  - Invocar Aliado da Natureza: Pequeno (2º), Médio (4º), Grande (5º), Enorme (6º), Maior (7º), Ancião (9º).
  - Invocar Criaturas: Pequeno (3º), Médio (5º), Grande (6º), Enorme (7º), Maior (8º), Ancião (9º).
- As quatro variantes elementais estão disponíveis em cada estágio. Nenhuma imagem existente foi modificada e nenhuma arte vetorial foi criada.
