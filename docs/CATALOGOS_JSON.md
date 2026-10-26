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
