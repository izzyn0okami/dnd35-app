# Atualização dos APKs de desenvolvimento

## Causa identificada

O workflow Android gerava um novo `debug.keystore` a cada execução do GitHub Actions. Como o Android exige a mesma chave de assinatura para atualizar um aplicativo instalado, os APKs de execuções diferentes eram tratados como incompatíveis. O projeto também mantinha `versionCode = 1`.

## Correções propostas

- O workflow restaura o keystore a partir do segredo `DND35_DEBUG_KEYSTORE_BASE64`, sem armazenar a chave privada no repositório.
- O `versionCode` e o `versionName` usam o número da execução do GitHub Actions.
- A configuração de assinatura de release não é alterada. A chave descrita aqui serve apenas para builds de desenvolvimento.

## Configurar a chave uma única vez

Antes de executar o workflow, gere um keystore de desenvolvimento em uma máquina confiável que tenha o JDK instalado. Não envie o arquivo para o repositório nem o compartilhe publicamente.

```bash
keytool -genkeypair -v \
  -keystore dnd35-debug.keystore \
  -storepass android \
  -alias androiddebugkey \
  -keypass android \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -dname "CN=DND35 Development,O=Development,C=BR"

base64 -w0 dnd35-debug.keystore
```

Copie a saída completa do comando `base64`. No GitHub, abra **Settings → Secrets and variables → Actions → New repository secret** e crie o segredo:

- **Name:** `DND35_DEBUG_KEYSTORE_BASE64`
- **Secret:** a saída completa de `base64`

O workflow valida o alias e a senha esperados antes de compilar. Não regenere essa chave após começar a distribuir APKs; perder ou trocar a chave impede atualizações diretas futuras.

## Limitação da primeira atualização

Os APKs anteriores foram assinados com chaves temporárias diferentes, geradas nos runners do GitHub Actions e não preservadas. Não é possível assinar uma nova versão com a chave privada dessas builds antigas apenas a partir do APK instalado.

Assim, a primeira instalação da versão assinada com a nova chave ainda pode exigir desinstalar a versão anterior. **Desinstalar pode apagar as fichas e os dados locais. Não desinstale antes de exportar ou fazer uma cópia recuperável dos dados importantes.** Depois que uma versão com a chave estável estiver instalada, as builds seguintes poderão atualizar normalmente.

Se ainda não houver uma exportação que permita restaurar as fichas dentro do app, interrompa a migração e implemente/teste primeiro um mecanismo de backup e restauração.
