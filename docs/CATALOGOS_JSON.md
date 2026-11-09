# Catálogos em JSON (talentos, itens, magias)

Os catálogos de talentos, itens e magias do Livro do Jogador não ficam mais como listas
Kotlin dentro de `Dnd35Rules.kt`. Eles vivem em:

```
app/src/main/resources/dnd35/feats.json
app/src/main/resources/dnd35/items.json
app/src/main/resources/dnd35/spells.json
```

`Dnd35Rules.kt` carrega cada um na primeira vez que o catálogo correspondente é acessado
(`PRESET_FEATS`, `PRESET_ITEMS`, `PRESET_SPELLS`, todos `by lazy`), lendo o arquivo pelo
classloader da aplicação (`Class.getResourceAsStream`). Isso funciona sem precisar de
`Context` do Android: igual no app, nos testes unitários comuns (`./gradlew testDebugUnitTest`)
e no Robolectric, porque `src/main/resources` é um source set Java/Kotlin comum, empacotado
no APK e também colocado no classpath de teste pelo Gradle.

## Por que não `assets/`

`app/src/main/assets/` exige um `Context` do Android (`AssetManager`) para ser lido. As três
telas que consomem esses catálogos (diálogos de talento, item e magia) e o `CharacterViewModel`
têm Context disponível, mas os testes de catálogo (`PresetFeatCatalogTest`,
`PresetEquipmentCatalogTest`, `PresetSpellCatalogTest`) são JUnit puro, sem Robolectric, e não
têm Context nenhum. Usar `resources/` evita ter que inventar uma forma de obter Context só para
rodar esses testes, e mantém a leitura idêntica em todos os contextos de execução.

## Formato dos arquivos

Cada arquivo é uma lista de objetos. Os campos usam exatamente os nomes das propriedades
Kotlin correspondentes.

**`feats.json`** → `Dnd35Rules.FeatDefinition`: `name`, `prerequisites`, `summary`, `benefit`.
Todos os campos são obrigatórios.

**`items.json`** → `Dnd35Rules.PresetItem`: `name`, `category`, `defaultSlot`, `weightLb`,
`costGp`, `damageOrAc`, `properties`. `defaultSlot` usa o texto literal do espaço (por
exemplo `"Mão Principal"`), não uma constante Kotlin — os valores válidos estão em
`Dnd35Rules.EquipmentSlots`.

**`spells.json`** → `Dnd35Rules.PresetSpell`: `name`, `level` (0 a 9), `school`, `classes`
(lista de nomes de classe), `castTime`, `components`, `range`, `duration`, `savingThrow`,
`spellResistance` (booleano), `description`. Dois campos são opcionais:
- `classLevels`: nível da magia por classe, quando difere do `level` geral (por exemplo,
  uma magia de nível 8 para Mago e nível 9 para Druida). Se omitido, assume `level` para
  todas as classes em `classes`.
- `domainLevels`: nível por domínio clerical. Se omitido, fica vazio (`{}`).

## Regras de carregamento

O carregador (`CatalogJson`, em `Dnd35Rules.kt`) falha com uma mensagem clara, em vez de
silenciar o erro ou inventar um valor, quando:
- falta um campo obrigatório;
- há dois itens com o mesmo nome (ignorando maiúsculas/minúsculas) no mesmo catálogo;
- o nível de uma magia está fora de 0–9.

Isso é proposital: um catálogo de centenas de magias é fácil de estragar sem perceber, e um
erro na inicialização do app (ou falha no teste) é preferível a uma ficha com uma magia errada.

## Para quem for completar o catálogo

Ao adicionar magias, talentos ou itens do Livro do Jogador:

1. Edite o `.json` diretamente (é só uma lista; qualquer editor de texto serve).
2. Rode `./gradlew testDebugUnitTest`. `CatalogJsonLoadingTest` confirma que o arquivo
   carrega, que não há nomes duplicados e que os níveis de magia são consistentes.
   `PresetFeatCatalogTest`, `PresetEquipmentCatalogTest` e `PresetSpellCatalogTest` continuam
   cobrindo conteúdo específico (nomes esperados, categorias, etc.).
3. `CatalogJsonLoadingTest.catalogSizesMatchThePlayersHandbookSnapshotCapturedOnMigration`
   só verifica um piso (o tamanho não pode diminuir); não precisa editá-lo ao adicionar itens,
   só se algum dia um catálogo for legitimamente reduzido.

Os números dos catálogos no momento desta migração (2026-10-24): 52 talentos, 163 itens,
258 magias.

### Auditoria de magias de nível 7–9 (2026-10-25)

As listas de magias de Clérigo, Druida e Feiticeiro/Mago de nível 7 a 9 (as únicas classes
que chegam a esses níveis de conjuração) foram conferidas página a página contra o PDF do
Livro do Jogador 3.5 (edição brasileira, Devir). Isso corrigiu:

- 9 magias de Feiticeiro/Mago com o `classLevels` errado (estavam no nível 7, mas são do
  nível 8: Padrão Cintilante, Clone, Criar Mortos-Vivos Maior, Evaporação, Ondas da
  Exaustão, Símbolo da Morte, Corpo de Ferro, Estase Temporal, Metamorfosear Objetos);
- Vidência Maior sem a classe Druida (ela também está na lista de Druida, nível 7);
- 69 magias de nível 7-9 ausentes do catálogo, agora incluídas (Blasfêmia, Palavra do
  Caos, Palavra Sagrada, Ditado, Cura Completa em Massa, Drenar Energia, Portal, Prender a
  Alma, Desejo, Desejo Restrito, Chuva de Meteoros, entre outras).

Catálogo após a auditoria: 52 talentos, 163 itens, 327 magias.

### Auditoria de talentos e armas/armaduras (2026-10-26)

A Tabela 5-1 (Talentos, páginas 76-77) e a Tabela 7-5/7-6 (Armas e Armaduras/Escudos,
páginas 98-103) do Livro do Jogador foram conferidas contra `feats.json` e `items.json`:

- 6 talentos do catálogo estavam com nomes diferentes dos usados pelo livro, duplicando
  talentos que já existiam sob o nome correto (ex.: "Disparo Rápido" era a mesma magia que
  "Tiro Rápido", já catalogada) ou usando uma tradução não-oficial (ex.: "Magia Acelerada"
  → "Acelerar Magia", "Atropelar" → "Pisotear"). Renomeados/deduplicados.
- 59 talentos da Tabela 5-1 que estavam ausentes foram adicionados (talentos gerais,
  talentos de criação de item e talentos metamágicos).
- Armaduras e escudos já cobriam a Tabela 7-6 por completo; nenhuma mudança necessária.
- 2 armas da Tabela 7-5 que estavam ausentes (Sabre, Boleadeira) foram adicionadas.

As categorias "Itens Mágicos" e "Consumíveis" de `items.json` são, de propósito, uma
amostra curada (um item por tipo/slot) e não uma lista exaustiva — o capítulo de itens
mágicos do Livro do Jogador tem centenas de entradas. A tabela "Itens e Serviços"
(vestuário, comida, hospedagem, montarias, veículos, serviços) também não foi trazida
para o catálogo por ser, em grande parte, economia de mesa e não inventário de
personagem; ver conversa com o usuário sobre se isso deve ser expandido.

Catálogo após esta auditoria: 110 talentos, 165 itens, 327 magias.

 
### Contagem atual verificada (2026-11-09)
O arquivo `app/src/main/resources/dnd35/spells.json` contém 393 entradas. O número anterior de 327 neste documento estava desatualizado. Esta atualização registra a contagem observada no arquivo, sem alterar o conteúdo do catálogo.

### Poções e Óleos do Livro do Mestre (2026-11-09)

A Tabela 7–17 (Poções e Óleos, Livro do Mestre 3.5 brasileiro, p. 230) foi importada por
completo para `items.json`: 84 itens novos na categoria "Consumíveis" (poções e óleos),
com o preço de mercado da tabela. As três colunas (Menor/Médio/Maior) cobrem 01–100 sem
lacunas nem sobreposições; as 3 poções que já existiam (Curar Ferimentos Leves/Moderados e
Invisibilidade) foram mantidas, sem duplicar.

Decisões e limitações:
- Entradas "poção ou óleo" (Invisibilidade, Levitação) viraram dois itens.
- Entradas com variável do livro ficaram genéricas, com o marcador entre parênteses:
  `(tipo)` de energia em Resistência/Proteção contra Elementos e `(tendência)` em Proteção
  contra / Círculo Mágico contra. O jogador escolhe ao registrar o item.
- A tabela não traz peso. Usei 0,5 lb, o mesmo valor das poções que já estavam no catálogo
  (frasco de 30 ml); revisar se o livro indicar outro valor.
- A tabela não lista o efeito numérico (cura, bônus): `damageOrAc` fica "-" e a descrição
  remete à magia correspondente, como diz o livro ("poções comuns são simplesmente magias
  em forma líquida").
- Duas correções de erros de impressão/extração do PDF: "Roupa encantada + 7 (óleo)" → `+1`
  (o preço, 750 PO, e a sequência +2…+5 só fecham com +1), e "Ajuda (poção) (poção)" →
  Ajuda. "Proteção contra elementos (tipo)", sem marcação no livro, foi tratada como poção.

### Anéis do Livro do Mestre (2026-11-09)

A Tabela 7–18 (Anéis, Livro do Mestre 3.5 brasileiro, p. 233) foi importada para
`items.json`: 44 anéis novos em "Itens Mágicos" (46 linhas da tabela; Proteção +1 e
Sustento já existiam). Cada anel traz o preço de mercado da tabela, espaço "Dedo (Anel 1)"
e peso 0,1 lb (convenção dos anéis que já estavam no catálogo; o livro diz só que anéis não
têm peso considerável). Os anéis de Proteção +2 a +5 têm o bônus de deflexão preenchido.
Os demais ficam com descrição genérica apontando para o livro: a descrição estruturada de
cada anel é parte da fase de base de dados detalhada, ainda não feita.

Observações:
- A coluna "Médio" da tabela impressa repete 50–51 em dois anéis (Cativar animais 47–51 e
  Resistência a elementos menor 50–56); é erro do livro e não afeta o catálogo, que não usa
  as faixas de rolagem.
- Os nomes seguem a tabela, inclusive "Escalada/Salto/Natação aprimorado" e "Arcano (I–IV)"
  (aqui como "Anel Arcano (I–IV)").

### Bastões do Livro do Mestre (2026-11-09)

A Tabela 7–19 (Bastões, Livro do Mestre 3.5 brasileiro, Cap. 7) foi importada para
`items.json`: 36 bastões novos em "Itens Mágicos", com o preço de mercado da tabela (as duas
colunas, Médio e Maior, cobrem 01–100 sem lacunas nem sobreposições). Espaço "Mão Principal"
e peso 5,5 lb, que é o que o livro diz (cerca de 2,5 kg). Os nomes seguem a tabela
("Bastão Metamágico, Aumentar (menor)" etc.) e a descrição é genérica, apontando para o livro;
o detalhe de cada bastão fica para a fase de base de dados.

### Cajados do Livro do Mestre (2026-11-09)

A Tabela 7–25 (Cajados, Livro do Mestre 3.5 brasileiro, Cap. 7) foi importada para
`items.json`: 21 cajados novos em "Itens Mágicos", com o preço de mercado da tabela (as duas
colunas, Menor e Maior, cobrem 01–100 sem lacunas nem sobreposições). Espaço "Mão Principal",
peso 5,5 lb (cerca de 2,5 kg, como diz o livro); a descrição lembra que o cajado nasce com
50 cargas, e o resto aponta para o livro.

Três erros de extração do PDF nessa tabela foram corrigidos conferindo as descrições
individuais: "65.000 FO" (Encantamento) → PO, "65:000" (Ilusão) → 65.000 e "Vido" → Vida.
Todos os cajados das oito escolas custam 65.000 PO, o que bate com cada descrição.

### Varinhas do Livro do Mestre (2026-11-09)

A Tabela 7–26 (Varinhas, Livro do Mestre 3.5 brasileiro, Cap. 7) foi importada para
`items.json`: 80 varinhas novas, com o preço de mercado da tabela (as três colunas, Menor,
Médio e Maior, cobrem 01–100 sem lacunas nem sobreposições; 81 linhas, uma já existia).
Ficam em "Consumíveis", na mesma categoria da varinha que já estava no catálogo (as cargas
se esgotam), no espaço "Mão Principal" e com peso 0,7 lb (o livro diz "menos de 300 g").

Decisões:
- A linha "Mísseis mágicos (1º)" não foi duplicada: já existe "Varinha de Mísseis Mágicos
  (Nv 1)" (750 PO, mesmo preço). Não renomeei o item antigo para não quebrar fichas que o
  referenciam pelo nome.
- Varinhas de nível de conjurador especial mantêm o sufixo do livro, como "(5º)", e as
  versões "elevado" ficam como "Enfeitiçar Pessoa, elevado (magia de 3º nível)".
- Restauração e Pele Rochosa levam, na descrição, o custo de criação das notas de rodapé da
  tabela (10.500 PO + 840 XP + componentes).
- Erros de extração corrigidos: "Ancora dimensional" → Âncora Dimensional, "Muralha de
  Ceio" → Muralha de Gelo (grafia do próprio livro em outras páginas) e "Invocar criaturas
  1" → I.
- Pendência: o catálogo de magias do app ainda não tem várias das magias que essas varinhas
  reproduzem (ex.: Sugestão, Lentidão, Levitação); isso é do catálogo de magias, não desta
  tabela.

### Armaduras e escudos específicos do Livro do Mestre (2026-11-09)

As Tabelas 7–7 (Armaduras Específicas) e 7–8 (Escudos Específicos) do Livro do Mestre 3.5
brasileiro foram importadas para `items.json`: 12 armaduras e 8 escudos novos, com o preço
de mercado da tabela (as colunas de rolagem cobrem 01–100 sem lacunas). O cabeçalho do PDF
diz "Modificador do Preço Base", mas os valores batem com o "Preço" de cada descrição, então
são o preço final do item e não um acréscimo sobre o item base.

Os valores de combate saem do item base + bônus de melhoria que o texto de cada item
informa (ex.: Armadura Demoníaca = armadura de batalha +4 = +12 CA; Escudo do Leão = escudo
grande de metal +2 = +4 CA). Onde o livro dá os números (Camisão de Mitral, Cota de Malha
Élfica, Armadura Celestial, Armadura de Mitral da Velocidade), usei os do livro. Armaduras
de adamante são sempre obras-primas (penalidade 1 ponto menor, conforme Materiais
Especiais), e o Couro de Dragão é "armadura de batalha (obra-prima)".

Divergências do PDF resolvidas pela tabela:
- Loriga Segmentada da Sorte: tabela 18.900 PO, descrição 18.000 PO → usei 18.900.
- Escudo do Leão: descrição "0.170 PO" (extração quebrada), tabela 9.170 PO → usei 9.170.
- Escudo do Conjurador: 3.153 PO é sem a magia inscrita (o livro soma o valor dela).
Habilidades de ativação aparecem só resumidas na descrição; o texto completo fica para a fase
de base de dados detalhada. Armas específicas (Tabela 7–16) vêm numa PR própria.
