# 19 — Novo ícone do app (logo enviado)

## Fonte
- Imagem do usuário (1024×1024), fundo preto + logo circular
- Copiada para `assets/app_icon_source.png`

## Gerado
Em `mipmap-mdpi` … `mipmap-xxxhdpi`:
- `ic_launcher.png`
- `ic_launcher_round.png` (máscara circular)
- `ic_launcher_foreground.png` (adaptive)

## Adaptive (API 26+)
- Background: `@color/ic_launcher_background` (`#FF000000`)
- Foreground: `@mipmap/ic_launcher_foreground`

## Observação
Ícone de notificação FCM permanece em `@drawable/ic_launcher_foreground` (vetor), adequado à máscara monocromática do sistema.

## Como ver
Desinstalar o app ou limpar cache do launcher e reinstalar o APK.
