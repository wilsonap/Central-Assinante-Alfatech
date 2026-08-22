# 55 — Teste de velocidade (Speedtest Custom)

## Auditoria da URL

`https://alfatechtelecom.speedtestcustom.com/` (Ookla Speedtest Custom)

| Recurso | Necessário? |
|---------|-------------|
| JavaScript | **Sim** |
| DOM Storage | **Sim** |
| Cookies | Opcional (não bloqueante) |
| WebSockets (via JS) | **Sim** (TCP test) |
| WebRTC | **Não** (STC usa HTML5/WebSockets) |
| Geolocalização Android | **Não** (servidores sem GPS) |
| Mixed content | **Não** (HTTPS) |
| Permissão Android extra | **Não** |

## Implementação

- Atalho Home `__speedtest__`
- `SpeedTestScreen` WebView dedicada (isolada da Central)
- Allowlist: `*.speedtestcustom.com`
- Links externos → navegador
- Offline: mensagem + Tentar novamente
