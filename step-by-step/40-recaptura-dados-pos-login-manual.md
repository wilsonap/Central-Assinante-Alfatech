# 40 — Recaptura de dados da Central após login manual

## Auditoria (antes)

| Item | Valor |
|------|--------|
| Onde a captura disparava | Somente `onPageFinished` → `extractWhatsAppFromDom` + `extractClientProfileFromStorage` |
| Como o login manual termina | Sessão IXC (`cookie/localStorage/sessionStorage` `sessao`) detectada em `FcmBridgeController.evaluateAuthAndMaybeEnable` |
| Por que não recapturava | SPA pós-login pode não gerar `onPageFinished` útil; probe de auth saía cedo se `bridgeEnabled` já era true |
| Funções reutilizadas | `extractWhatsAppFromDom`, `extractClientProfileFromStorage` (mesmo JS) |

## Depois

- `refreshCentralCustomerData` / `scheduleCentralCustomerDataRefresh` (retries 0 / 500 / 1000 / 2000 ms)
- Triggers: `page_finished`, `login_authenticated`, `session_restored`, `network_reload`
- Transição NÃO autenticado → autenticado via probe de `sessao` (sem novo login)
- Dados válidos não são apagados em falha temporária de rede (`updateWhatsAppConfig` / `updateClientProfile` só sobrescrevem com valor não vazio)
- Logs: `CENTRAL_DATA_REFRESH`

## Arquivo

`CentralWebView.kt` apenas.
