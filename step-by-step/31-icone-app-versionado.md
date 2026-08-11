# 31 — Ícone do app versionado (anti-perda no Git)

## Causa
AI Studio / geradores costumam recriar `drawable/ic_launcher_foreground.xml` e sobrescrever mipmaps.

## Correção
- Fonte canônica em `branding/alfatech-app-icon-source.png` (versionada)
- Script `scripts/sync-app-icon.ps1` regenera PNGs
- Adaptive icon fixo em `@mipmap/ic_launcher_foreground`
- Regra Cursor `.cursor/rules/app-icon.mdc`
