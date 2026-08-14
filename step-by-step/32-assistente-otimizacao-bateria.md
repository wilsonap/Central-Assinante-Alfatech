# 32 — Assistente de otimização de bateria

## Escopo

Aviso na primeira abertura quando a otimização de bateria está ativa.
Não altera configurações automaticamente — abre a tela do fabricante/sistema.

## Arquivos

- `BatteryOptimizationAssistant.kt` — detecção, prefs, intents OEM
- `BatteryOptimizationPromptDialog.kt` — UI do aviso
- `MainActivity.kt` — dispara na primeira composição

## Prefs

`battery_optimization_assistant` / `prompt_acknowledged`

## Log

`BATTERY_OPTIMIZATION enabled=true|false`
