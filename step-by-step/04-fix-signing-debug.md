# Step-by-step — Correção signing debug

**Data:** 2026-08-08

## Problema
`validateSigningDebug` falhava: `debug.keystore` local do projeto não existia.

## Alteração
Em `app/build.gradle.kts`:
- Removido `signingConfigs.debugConfig` que apontava para `${rootDir}/debug.keystore`
- Removido `buildTypes.debug.signingConfig = debugConfig`
- Debug passa a usar a assinatura debug padrão do Android/Gradle
- `signingConfigs.release` **não** foi alterado (keystore/alias de produção preservados)
