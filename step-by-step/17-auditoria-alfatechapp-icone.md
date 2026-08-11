# 17 — Auditoria ClassNotFound AlfatechApp + ícone

## Auditoria (sem mudança de Application)

| Item | Valor |
|------|--------|
| `android:name` | `.AlfatechApp` (já correto) |
| Classe | `com.example.AlfatechApp` em `app/src/main/java/com/example/AlfatechApp.kt` |
| `namespace` | `com.example` |
| `applicationId` | `br.com.sac2.alfatechtelecom.com.br` |
| Ícone Manifest | `@mipmap/ic_launcher` / `@mipmap/ic_launcher_round` |

### Por que o sistema procura `com.example.AlfatechApp`

Nomes relativos no Manifest são resolvidos pelo **`namespace`**, não pelo `applicationId`.  
`.AlfatechApp` + namespace `com.example` = `com.example.AlfatechApp`. Isso é normal.

O `ClassNotFoundException` com a classe presente no source costuma indicar APK/instalação desatualizada (Clean + reinstall).

## Correção aplicada

- **Application / Manifest / applicationId / namespace:** sem alteração (já alinhados).
- **Adaptive icon (API 26+):** restauradas referências originais em:
  - `mipmap-anydpi-v26/ic_launcher.xml`
  - `mipmap-anydpi-v26/ic_launcher_round.xml`
  - de `@color` + `@mipmap/ic_launcher_foreground` → `@drawable/ic_launcher_background` + `@drawable/ic_launcher_foreground`

Nenhum PNG novo gerado. Drawables originais reutilizados.

## Validação recomendada

1. Build → Clean Project
2. Desinstalar o app do aparelho
3. Instalar novamente o debug/release
4. Confirmar abertura sem ClassNotFound e ícone adaptive original no launcher
