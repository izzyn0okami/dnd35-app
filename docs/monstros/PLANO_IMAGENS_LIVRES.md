# Plano de arte licenciada para as cartas de monstros

**Estado:** aprovado como direção de trabalho; fase de pesquisa e especificação  
**Escopo:** ilustrações para as 230 criaturas do Livro dos Monstros já presentes no catálogo do aplicativo. As 32 entradas complementares do SRD permanecem separadas.

## Objetivo

Substituir a ideia de usar recortes do Livro dos Monstros por imagens externas com licença verificável, mantendo a identidade visual das cartas e a independência entre dados de regras e arquivos de arte.

A moldura, tipografia, hierarquia de informações, cores e animação das cartas serão padronizadas. A arte poderá ter estilos de origem diferentes; não se deve deformar ou redesenhar o trabalho original apenas para simular uniformidade.

## Fontes e prioridade

| Prioridade | Fonte | Uso |
|---|---|---|
| 1 | [Openverse](https://openverse.org/) | Descoberta agregada; filtrar CC0, domínio público e CC BY; validar sempre na origem |
| 2 | [Wikimedia Commons](https://commons.wikimedia.org/) | Arte histórica, gravuras e criaturas mitológicas |
| 3 | [OpenGameArt](https://opengameart.org/) | Arte de monstros e criaturas produzida para jogos |
| 4 | [Smithsonian Open Access](https://www.si.edu/openaccess) | Referências históricas/mitológicas marcadas CC0 |
| 5 | [The Met Open Access](https://www.metmuseum.org/art/collection) | Obras históricas identificadas como Open Access/domínio público |
| 6 | [Kenney](https://kenney.nl/assets) | Recursos CC0 de estilo simplificado, quando adequados à direção visual |

Openverse é um mecanismo de descoberta, não uma garantia jurídica: a própria documentação recomenda verificar a licença de cada obra na fonte original. O Smithsonian e o Met identificam itens Open Access/CC0 ou domínio público; a elegibilidade é por item, não por instituição inteira.

## Política de licenças

- Preferir CC0 1.0, Public Domain Mark e domínio público confirmado.
- Aceitar CC BY 4.0 quando os dados de atribuição estiverem completos e os créditos forem incorporados ao aplicativo.
- CC BY-SA exige análise específica de ShareAlike antes de qualquer adaptação; não é licença padrão para o lote inicial.
- Rejeitar CC BY-NC, CC BY-ND e licenças desconhecidas/incompatíveis com a redistribuição e o tratamento visual pretendidos.
- Confirmar os direitos na página original; não confiar apenas em snippets, thumbnails ou filtros de buscador.
- Registrar também restrições de terceiros que possam existir independentemente do copyright.

## Critérios de correspondência

1. A imagem deve representar a criatura ou uma interpretação mitológica claramente compatível.
2. Pesquisar nomes em português e inglês, sinônimos e termos mitológicos relacionados.
3. Não associar automaticamente uma imagem de dragão, demônio ou aranha a uma criatura específica apenas por categoria genérica.
4. Se a representação for apenas aproximada, marcar como associação genérica ou inspirada, não como correspondência exata.
5. Não usar imagens com marcas d'água, texto incorporado, baixa resolução ou procedência incerta.
6. Se não houver uma imagem adequada, manter a carta sem arte individual, usando um placeholder visual consistente.

## Registro de proveniência

Criar um manifesto separado do índice do Livro dos Monstros. Cada asset deve registrar, no mínimo:

- ID da criatura e tipo de associação (individual, compartilhada, genérica ou inspirada);
- caminho local do asset e hash SHA-256;
- título da obra, autor/criador e página original;
- URL direta do arquivo, quando disponível;
- identificador e versão da licença, URL dos termos;
- data de obtenção;
- se houve recorte, conversão, retoque ou outra modificação;
- estado da revisão visual e observações.

Créditos CC BY devem ser acessíveis na interface (detalhes da imagem) e reunidos em um arquivo de créditos do aplicativo. Para CC0/domínio público, manter proveniência como boa prática mesmo quando não obrigatória.

## Organização técnica proposta

- Manter o catálogo de criaturas e suas estatísticas intactos.
- Criar um registro independente de arte indexado pelo ID estável da criatura.
- Armazenar assets locais otimizados para exibição offline, com tamanho adequado para carta vertical.
- Não fazer hotlink de imagens externas como dependência de funcionamento.
- Permitir substituição de arte sem migração de dados de personagem/NPC.
- Usar placeholders para criaturas sem imagem aprovada.
- Manter o índice de recortes do PDF apenas como referência de auditoria; não reutilizar esses recortes no APK.

## Lote-piloto

Antes de buscar arte para todo o catálogo, validar um pequeno lote com criaturas visualmente distintas:

- Beholder — criatura fantástica muito específica;
- Bugbear — humanoide monstruoso;
- Dríade — ser mitológico;
- Lich — morto-vivo;
- Tarrasque — monstro colossal;
- Mephits — família com variantes elementais, exigindo distinção cuidadosa.

O piloto deve testar qualidade visual, correspondência, licença, atribuição, tamanho do asset, funcionamento offline e apresentação nas cartas. Somente depois de aprovado o processo será expandido ao catálogo completo.

## Referências oficiais

- [Openverse API](https://api.openverse.org/v1/) — busca e filtros por licença.
- [Openverse: Made with Openverse](https://docs.openverse.org/api/reference/made_with_ov.html) — alerta para conferir a licença na fonte original.
- [Creative Commons: Reusing CC-Licensed Content](https://creativecommons.org/reusing-cc-licensed-content/) — atribuição TASL.
- [Wikimedia Commons: Reusing content outside Wikimedia](https://commons.wikimedia.org/wiki/Commons:Reusing_content_outside_Wikimedia) — condições por arquivo.
- [Smithsonian Open Access FAQ](https://www.si.edu/openaccess/faq) — itens CC0.
- [The Met Collection API](https://metmuseum.github.io/) — dados e imagens Open Access.
- [Kenney Support](https://kenney.nl/support) — licença CC0 dos assets publicados.
- [OpenGameArt: Monster Concept](https://opengameart.org/content/monster-concept) e [Tiny Creatures](https://opengameart.org/content/tiny-creatures) — exemplos de conteúdo de criaturas; conferir licença na página individual.

## Limites desta etapa

Nenhuma imagem externa foi aprovada ou incorporada. A política e o fluxo foram definidos; o próximo passo é executar o lote-piloto e revisar cada candidato antes de versionar assets.


## Pesquisa-piloto de candidatos (2026-11-12)

A busca inicial encontrou alguns candidatos reais, mas nenhum foi ainda promovido a asset aprovado do aplicativo. A licença indicada na página da fonte foi conferida; a adequação artística e a correspondência final continuam sujeitas à revisão visual.

| Criatura-alvo | Candidato | Licença informada na origem | Resultado preliminar |
|---|---|---|---|
| Beholder | [One Eye Armored Monster — Xacur / OpenGameArt](https://opengameart.org/content/one-eye-armored-monster) | CC BY 4.0 | Candidato promissor: a própria página marca a tag beholder e oferece arte colorida, P&B e line art. Preview do arquivo não carregou nesta auditoria; revisar imagem e atribuição antes de aceitar. |
| Dríade | [DnD Dryad.png — LadyofHats / Wikimedia Commons](https://commons.wikimedia.org/wiki/File:DnD_Dryad.png) | CC0 1.0 | Licença, autoria declarada e resolução (1.145 × 1.500) verificadas na página original; imagem visualizada. Boa correspondência temática, mas estética de desenho monocromático com fundo claro pode destoar da direção pictórica desejada. Manter como candidata, não aprovada. |
| Dríade (alternativa) | [Swamp Dryad — 5hiroi / OpenGameArt](https://opengameart.org/content/swamp-dryad) | CC0 | Página declara arte conceitual e CC0; arquivo de 42,9 KB. Preview não foi carregado, portanto adequação e resolução útil ainda não verificadas. |
| Lich | [Lich, Death Knight and Fallen Warrior — Jastiv / OpenGameArt](https://opengameart.org/content/lich-death-knight-and-fallen-warrior) | CC BY 4.0 / CC BY 3.0 | Correspondência temática encontrada; sprites de mortos-vivos, não necessariamente retrato vertical. Candidato secundário sujeito à inspeção e atribuição à criadora indicada (Lori Angela Nagel). |
| Bugbear | — | — | Nenhuma correspondência individual suficientemente específica foi validada nesta busca inicial. Continuar pesquisa; não substituir por goblin/ogro genérico sem rotular como inspiração. |
| Tarrasque | — | — | Nenhuma correspondência individual suficientemente específica foi validada nesta busca inicial. Continuar pesquisa; não associar dinossauro ou dragão genérico como se fosse Tarrasque. |
| Mephits | — | — | Nenhuma arte CC0/CC BY adequada para as variantes elementais foi validada nesta busca inicial. Pesquisar cada variante separadamente e preservar diferenças elementais. |

### Outros pacotes encontrados, mas não selecionados como arte principal

- [Tiny Creatures — OpenGameArt](https://opengameart.org/content/tiny-creatures), CC0: pacote com mais de 100 monstros e animais em sprites 16×16. É útil como referência de cobertura/placeholder, mas a resolução e o estilo pixel art não atendem à proposta de cartas ilustradas em alta qualidade.
- [50+ Monsters Pack 2D — OpenGameArt](https://opengameart.org/content/50-monsters-pack-2d), CC0: 56 monstros em pixel art 64×64; pode ajudar em placeholders ou versões compactas, não como arte principal das cartas.
- [The Free Monsters — OpenGameArt](https://lpc.opengameart.org/content/the-free-monsters), CC BY-SA 3.0/GPL: contém um Lichlord, mas a licença ShareAlike e o estilo do pacote exigem análise específica; não foi incluído no conjunto padrão.

**Conclusão do piloto:** a disponibilidade de arte livre é real, mas não garante correspondência visual ou consistência estética. A Dríade e o Beholder já têm candidatos pesquisáveis; nenhum arquivo foi baixado, recortado, aprovado ou adicionado ao repositório. Próxima etapa: ampliar a busca por criatura e fazer revisão visual dos arquivos originais antes de fechar o primeiro conjunto.


## Expansão da descoberta visual via Pinterest (2026-11-12)

Foi autorizada a utilização do Pinterest como base de descoberta visual provisória, para acelerar a localização de representações plausíveis das criaturas antes da auditoria manual. Foi criado o índice `docs/monstros/REFERENCIAS_PINTEREST.json`, com uma busca individual para cada uma das 230 criaturas do Livro dos Monstros 3.5, mantendo o ID estável do catálogo e um caminho de arquivo padronizado.

**Limite de direitos:** Pinterest é tratado apenas como mecanismo de descoberta. Um pin, thumbnail ou repost não comprova licença. Como o repositório é público, imagens não verificadas não serão baixadas nem versionadas. Cada candidato deve ser rastreado até a fonte original e ter autor/licença confirmados antes de entrar no APK ou no Git. Quando não houver autorização, a imagem será substituída por uma alternativa licenciada.

**Convenção de nomes:** `app/src/main/assets/monsters/<creatureId>.webp`. O índice reserva esse caminho por criatura, mas não declara que o arquivo já existe. Os campos `assetStatus`, `licenseStatus` e `reviewStatus` deixam explícito o estado pendente.

**Escopo:** 230 criaturas cuja origem é Livro dos Monstros 3.5; as entradas complementares do SRD permanecem fora deste lote, conforme o escopo aprovado. Nenhuma imagem foi baixada ou incorporada nesta atualização.


## Segunda rodada de pesquisa de candidatos (2026-11-12)

As páginas originais de cinco novos recursos do OpenGameArt foram abertas e seus autores, licenças declaradas e descrições foram conferidos. O resultado foi separado entre candidatos com identidade de criatura e pacotes genéricos; nenhum arquivo foi baixado ou inspecionado em resolução integral nesta rodada.

| Alvo/uso possível | Recurso | Licença declarada | Avaliação e limite |
|---|---|---|---|
| Beholder | [One Eye Armored Monster — Xacur](https://opengameart.org/content/one-eye-armored-monster) | CC BY 4.0 | A página traz a tag beholder e três versões PNG (colorida, P&B e line art; cerca de 10 MB cada). Melhor candidato de ilustração conceitual para o Beholder, ainda pendente de inspeção visual e atribuição. |
| Beholder | [Observers — AntumDeluge](https://opengameart.org/content/observers) | CC BY 4.0 / CC BY 3.0 / OGA-BY 3.0 | A descrição cita Young Beholder, Beholder e Elder Beholder, mas são sprites minúsculos (19×19, 24×32 e 48×64). Candidato secundário para placeholder, não para retrato principal. |
| Lich | [Lich, Death Knight and Fallen Warrior — Jastiv](https://opengameart.org/content/lich-death-knight-and-fallen-warrior) | CC BY 4.0 / CC BY 3.0 | A página descreve mortos-vivos esqueléticos e informa copyright Lori Angela Nagel 2018. São sprites; qualidade para carta vertical não confirmada. Exigir atribuição correta e conferir arquivo/termos antes de usar. |
| Dríade | [Swamp Dryad — 5hiroi](https://opengameart.org/content/swamp-dryad) | CC0 | Página identifica explicitamente dryad e disponibiliza JPEG de 42,9 KB. Candidata temática direta; resolução/qualidade visual ainda precisam ser inspecionadas. |
| Goblin (referência genérica) | [Goblin monster — ImogiaGames](https://opengameart.org/content/goblin-monster) | CC0 | Spritesheet 32×32, oito variações de cor. Correspondência nominal forte, mas dimensão insuficiente para retrato principal; possível placeholder pixel art. Não associar a bugbear ou orc. |
| Troll (referência genérica) | [Trolls — Blarumyrran](https://opengameart.org/content/trolls) | CC0 | Quatro sprites de troll, com estilo pixel/Wesnoth. Apenas placeholder; não há criatura Troll entre as entradas localizadas no índice atual de 230 IDs. |
| Dragão (genérico) | [2D Dragon Spritesheet — Daniel Quevedo](https://opengameart.org/content/2d-dragon-spritesheet) | CC0 | PNG de 170 KB e PSD; é um dragão genérico em spritesheet. Não atribuir a Pseudodragão ou Tartaruga-dragão, pois a identidade não corresponde. |
| Criaturas genéricas | [The Free Monsters — Justin Nichol](https://opengameart.org/content/the-free-monsters) | CC BY-SA 3.0 / GPL 3.0 / GPL 2.0 | Pacote de 148,1 MB com Naga, Ratman, Treefolk, Troll, Divine Guardian, Lichlord, Violence Demon, Goblin, Orc, Harpy e Dragon. A licença ShareAlike/GPL exige análise própria; mantido fora do lote padrão. |
| Criaturas genéricas | [Tiny Creatures — Clint Bellanger e Kenney](https://opengameart.org/content/tiny-creatures) | CC0 | 180 sprites de 16×16, mais de 100 monstros e 50 animais. Cobertura ampla, mas pixel art muito pequena para as cartas ilustradas principais. |
| Montagem de placeholders | [Monster Builder Pack — Kenney](https://opengameart.org/content/monster-builder-pack) | CC0 | 178 peças PNG modulares e fontes vetoriais. Pode gerar placeholders originais, mas não representa retratos prontos de criaturas específicas. |
| Demônio genérico | [Demon/Devil — Nerveona](https://opengameart.org/content/demondevil) | CC0 | Sprite direcional genérico. Não associar a demônios específicos do catálogo sem correspondência visual e conceitual comprovada. |

**Conclusão desta rodada:** Beholder, Lich e Dríade têm candidatos específicos com licença declarada CC BY/CC0, mas ainda não aprovados visualmente. Goblin e Troll têm opções CC0 em pixel art que podem servir apenas como placeholders se o usuário aceitar estilos mistos. Os pacotes ShareAlike foram preservados como pesquisa, não selecionados. Nenhum arquivo de imagem foi baixado, convertido ou incluído no app.
