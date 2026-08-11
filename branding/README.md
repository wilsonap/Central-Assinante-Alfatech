# Branding — ícone do app (fonte canônica)

Arquivo oficial do ícone:

- `alfatech-app-icon-source.png`

## Regenerar mipmaps

No PowerShell, na raiz do repositório:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/sync-app-icon.ps1
```

Isso atualiza `app/src/main/res/mipmap-*/ic_launcher*.png` a partir desta fonte.

## Por que o ícone “some” após AI Studio / commits

Ferramentas de AI/Android Studio costumam:

1. regenerar `drawable/ic_launcher_foreground.xml` (vetor genérico);
2. apontar o adaptive icon para `@drawable/...` em vez de `@mipmap/ic_launcher_foreground`;
3. sobrescrever PNGs dos mipmaps.

**Regra:** o foreground do adaptive icon deve continuar:

```xml
<foreground android:drawable="@mipmap/ic_launcher_foreground" />
```

Não apague os PNGs em `mipmap-*`. Não versione um vetor genérico como foreground.

Após regenerar, faça commit de `branding/` + `mipmap-*` juntos.
