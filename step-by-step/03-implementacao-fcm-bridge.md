# Step-by-step — Implementação FCM bridge (mínima)

**Data:** 2026-08-08  
**Status:** Implementado — aguarda teste no dispositivo

## Arquivos alterados

1. `app/src/main/java/com/example/ui/components/CentralWebView.kt`
2. `app/src/main/java/com/example/ui/MainViewModel.kt`
3. `app/src/main/java/com/example/service/MyFirebaseMessagingService.kt`

## O que cada alteração faz

### `FcmTokenStore` (em `MyFirebaseMessagingService.kt`)
- Estado/SharedPreferences do token FCM atual
- `mask()` para logs
- Atualizado por `getToken` e `onNewToken`
- **Não** faz POST a endpoint inventado

### `MainViewModel.kt`
- Persiste token obtido em `FcmTokenStore`
- Log `FCM_TOKEN_READY` com token mascarado
- Mantém UI `isLoggedIn` (não usado como gate exclusivo do FCM)

### `CentralWebView.kt`
- Bridge `AndroidBridge.onReactNativeMessage` recebe JSON da página
- `getFcmToken()` lê store vivo (não congela token no construtor)
- Pós-auth comprovada: injeta **somente** `window.ReactNativeWebView.postMessage`
- Bloqueia/remove shim em login, cadastro, troca/recuperação de senha
- Em `firebase_token`: obtém token via `FirebaseMessaging.getInstance().token` e responde com `MessageEvent` (payload JSON escapado via `JSONObject.quote`)
- Após resposta, completa `promisesWebView[promiseID].resolve` se necessário (late-injection; resolve é idempotente)
- **Não** registra segundo `addEventListener('message')` na habilitação
- **Não** chama `iniciaFirebase()`
- Fallback único: `new HotsiteWeb().updateFirebaseToken()` após 6s sem pedido natural, só se sessão/bridge OK

## Logs a observar (Logcat)

| Tag | Quando |
|-----|--------|
| `FCM_AUTH_STATE` | Probe de sessão / bloqueio de path |
| `FCM_BRIDGE_ENABLED` | Shim postMessage ativo |
| `FCM_TOKEN_REQUEST` | Página pediu firebase_token + promiseID |
| `FCM_TOKEN_READY` | Token obtido (mascarado) |
| `FCM_TOKEN_RESPONSE` | MessageEvent enviado |
| `FCM_TOKEN_REGISTERED` | Resposta entregue / fallback updateFirebaseToken |

## Roteiro de teste obrigatório

1. App limpo (dados limpos)
2. Login manual → não pode travar
3. Sessão autenticada
4. `FCM_BRIDGE_ENABLED` só após auth
5. Pedido `firebase_token` (natural ou fallback `updateFirebaseToken`)
6. Resposta com mesmo `promiseID`
7. Network: `dados_cliente.php` com `ACTION=updateFirebaseToken`
8. Fechar app → push pelo sistema real da Central
9. Background e app morto

## Reversão

Se o login quebrar: remover habilitação do shim / reverter estes 3 arquivos. Não alterar o fluxo de login da página.
