# 37 — Warnings de compilação (Locale, Icons, databaseEnabled)

## Auditoria

### Seguros (aplicados)
- `Locale("pt","BR")` → `Locale.forLanguageTag("pt-BR")`
- `Icons.Filled.Chat/Assignment` → `Icons.AutoMirrored.Filled.*`
- `databaseEnabled` removido (WebSQL; app usa `domStorageEnabled` + localStorage/sessionStorage)

### Cuidado — FCM NÃO migrado
- BOM: `34.15.0` → messaging ~25.1.x
- `onNewToken` → oficial: `onRegistered(installationId)` (FID)
- `token`/`getToken` → oficial: `register()` + fluxo FID
- Risco: **ALTO** — Central IXC ainda usa `updateFirebaseToken` com token FCM clássico; FID quebraria o registro

## Arquivos alterados
ReceiptImageStamper, ReceiptShareHelper, NotificationsScreen, ReceiptHistoryScreen, HomeScreen, SupportScreen, CentralWebView (só remoção de `databaseEnabled`).
