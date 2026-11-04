# Exportar e importar fichas

O app não faz backup automático do Android (`allowBackup="false"`). Para guardar ou levar fichas
para outro aparelho, use o menu **⋮** na barra superior:

- **Exportar Ficha (arquivo)**: grava a ficha ativa em um arquivo `.fichadnd35`.
- **Exportar Todas as Fichas**: grava todas as fichas em um único arquivo.
- **Importar Fichas (arquivo)**: lê um arquivo exportado e cria **novas** fichas. Nada é sobrescrito;
  fichas com o mesmo nome continuam existindo lado a lado.

O arquivo é escolhido pelo seletor de arquivos do Android, então pode ser salvo no Drive, enviado por
mensageiro ou guardado no computador.

## Formato de ficha (versão 1)

```json
{
  "app": "ficha-dnd35",
  "formatVersion": 1,
  "exportedAt": 1761330000000,
  "characters": [ { "name": "...", "level": 5, "skillsJson": "[...]", "photoJpegBase64": "..." } ]
}
```

- Os campos simples da ficha (nome, atributos, PV, moedas etc.) são valores JSON normais.
- As listas (perícias, inventário, talentos, magias, espaços de magia, multiclasse, idiomas e
  histórico de pontos de perícia) são gravadas como **texto JSON**, exatamente como ficam no banco.
- O retrato personalizado vai embutido em `photoJpegBase64`; retratos predefinidos vão como
  `photoPreset`. Retratos que não estejam na pasta do app não são exportados.
- Campos ausentes usam o valor padrão; campos desconhecidos são ignorados.

## Regras da importação

- Recusa arquivos que não sejam deste app, com `formatVersion` maior que o suportado, com JSON
  inválido, sem fichas ou com mais de 500 fichas, e arquivos maiores que 30 MB.
- Valores fora da faixa são ajustados (por exemplo: nível entre 1 e 100, atributos entre 1 e 99,
  PV atual nunca acima do máximo, moedas nunca negativas).
- Listas com JSON inválido são descartadas e a importação mostra um aviso; o resto da ficha é mantido.

## Para quem altera o app

Ao mudar o esquema do banco (nova coluna em `CharacterEntity`), atualize `CharacterBackup`
(exportação e leitura) e `CharacterBackupTest`. Se a mudança não for compatível com a versão 1,
aumente `FORMAT_VERSION` e mantenha a leitura dos arquivos antigos.

## Exportar e importar itens

Na aba **Carga & Itens**, cada item pode ser exportado individualmente para um arquivo
`.itemdnd35`. O arquivo contém os dados do item e incorpora a imagem personalizada em Base64,
quando ela está disponível no armazenamento privado do aplicativo. O caminho local da imagem
nunca é compartilhado.

Ao importar um item, o usuário escolhe primeiro a ficha de destino. Os itens são acrescentados
ao inventário dessa ficha, sem substituir os itens existentes; cada item importado recebe um novo
identificador para evitar colisões. A imagem é extraída para a pasta privada `item_photos` do
aparelho receptor e o item passa a usar a nova URI local.

Arquivos antigos de item, que contêm apenas o array JSON sem envelope ou imagem incorporada,
continuam aceitos. Imagens inválidas ou maiores que 5 MB são ignoradas com aviso; os dados do
item continuam sendo importados. O arquivo completo continua sujeito ao limite geral de 30 MB.
