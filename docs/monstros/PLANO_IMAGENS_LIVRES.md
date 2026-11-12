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
