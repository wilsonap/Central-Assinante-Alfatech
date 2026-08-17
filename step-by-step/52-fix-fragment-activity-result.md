# 52 — Fix InvalidFragmentVersionForActivityResult

## Correção mínima

- `libs.versions.toml`: `fragment = "1.8.6"` + `androidx-fragment`
- `app/build.gradle.kts`: `implementation(libs.androidx.fragment)`

Sem alteração Kotlin, sem lint-baseline/suppress.

## Validação

| Check | Resultado |
|-------|-----------|
| `fragment` resolvido | `1.1.0 -> 1.8.6` |
| `InvalidFragmentVersionForActivityResult` | **ausente** no lintRelease |
| `lintRelease` | FALHA por **5 NewApi** pré-existentes (`NotificationChannels` API 26) — não relacionados a Fragment |
| `assembleRelease` | FALHA por keystore ausente (`debug.keystore` / release) — não relacionado a Fragment |
