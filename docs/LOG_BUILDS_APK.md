# Registro de builds distribuíveis

## 2026-11-17 — APK de atualização após a PR #393

- Base: commit `6966c909f6eea9dde3829017d284a0e3f69d5529` (merge da PR #393).
- Artefato solicitado: APK Android `debug` assinado com a keystore persistente do repositório, restaurada pelo segredo `DND35_DEBUG_KEYSTORE_BASE64`.
- Workflows responsáveis: `.github/workflows/android.yml` (artefato `dnd35-debug-apk`) e `.github/workflows/android-build.yml` (artefato `dnd35-app-debug`).
- A assinatura persistente usa alias `androiddebugkey` e senha de armazenamento/chave `android`; o segredo não deve ser exibido nem versionado.
- Esta anotação também aciona os workflows configurados para push na branch `main`, gerando uma compilação nova do estado integrado.
- Só considerar o APK pronto para atualização depois de confirmar testes, compilação, upload do artefato e assinatura válida.
- Não confundir este APK de depuração assinado com uma publicação de produção `release`: a configuração release usa outra keystore e credenciais (`KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`), que não foram fornecidas nem alteradas nesta etapa.
