# 30 — In-App Updates (Fase 1, app aberto)

## Escopo

Verificação e fluxo **FLEXIBLE** com o app em foreground via Google Play In-App Updates.

## Não incluso nesta fase

- Worker com app fechado
- Immediate update obrigatória
- Scraping da Play Store

## Arquivos

- `InAppUpdateCoordinator.kt` — lógica isolada
- `MainActivity` — `onStart` chama `checkOnForeground()`
- Canal `app_updates_channel` preparado para fase 2

## Como testar de verdade

1. Publicar versão N na faixa interna da Play Console.
2. Instalar pelo link da faixa interna (não via Android Studio).
3. Publicar versão N+1 na mesma faixa.
4. Abrir o app instalado → deve aparecer o fluxo flexível.
5. Após download → diálogo “Atualização pronta para instalar” → “Atualizar agora”.

Debug instalado pelo Studio normalmente retorna `UPDATE_NOT_AVAILABLE` / `check_failed` — esperado.
