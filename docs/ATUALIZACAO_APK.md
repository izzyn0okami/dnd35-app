# Atualização dos APKs de desenvolvimento

## Causa identificada

O workflow Android gerava um novo `debug.keystore` a cada execução do GitHub Actions. Como o Android exige que uma atualização seja assinada com a mesma chave da versão instalada, os APKs de execuções diferentes eram tratados como aplicativos incompatíveis. Além disso, o projeto mantinha `versionCode = 1`.

## Correções

- O repositório passa a conter um keystore **exclusivo para builds de desenvolvimento**. O workflow não gera uma chave nova a cada execução.
- O `versionCode` e o `versionName` das builds do GitHub Actions passam a usar o número da execução, para que novas builds tenham versões crescentes.
- A configuração de assinatura de release não é alterada; o keystore de desenvolvimento não deve ser usado para publicar uma versão de produção.

## Limitação da primeira atualização

Os APKs anteriores foram assinados com chaves temporárias diferentes, geradas nos runners do GitHub Actions e não armazenadas como artefatos. Não é possível produzir um APK novo com a mesma chave privada de uma dessas builds antigas apenas a partir do APK instalado.

Por isso, a primeira instalação do APK gerado após esta correção ainda pode exigir desinstalar a versão antiga. **Desinstalar pode apagar os dados locais do aplicativo.** Antes dessa transição, exporte ou faça cópia de segurança de todas as fichas e dados importantes. Se o app ainda não oferecer uma exportação restaurável dos dados, não desinstale até que uma forma de backup/recuperação esteja disponível.

Depois que o APK assinado com a chave estável estiver instalado, as builds subsequentes do GitHub Actions deverão atualizar normalmente, sem exigir desinstalação e preservando os dados locais.
