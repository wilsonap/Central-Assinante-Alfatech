# Step-by-step — Novo ícone do app

**Data:** 2026-08-08

## Fonte
- Imagem enviada pelo usuário (1024x1024)
- Copiada para `assets/app_icon_source.png`

## Alterações
- Gerados PNGs em `mipmap-mdpi` … `mipmap-xxxhdpi`:
  - `ic_launcher.png`
  - `ic_launcher_round.png`
  - `ic_launcher_foreground.png` (adaptive)
- Adaptive icon (`mipmap-anydpi-v26`) aponta foreground para `@mipmap/ic_launcher_foreground`
- Background do ícone: preto (`#000000`) alinhado à arte
- Ícone de notificação FCM permanece no drawable vetorial antigo (máscara monocromática do sistema)

## Como ver
Desinstalar o app antigo ou limpar cache do launcher, depois instalar o debug APK novamente.
