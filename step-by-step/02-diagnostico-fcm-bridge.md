# Step-by-step — Diagnóstico FCM (registro do token)

**Data:** 2026-08-08  
**Status:** Diagnóstico completo — aguardando aprovação para implementar  
**Escopo:** somente associação do token FCM ao assinante via protocolo da Central

## Fontes auditadas (não inventadas)

### App nativo
- `app/src/main/java/com/example/ui/components/CentralWebView.kt`
- `app/src/main/java/com/example/ui/MainViewModel.kt`
- `app/src/main/java/com/example/service/MyFirebaseMessagingService.kt`
- `app/src/main/java/com/example/MainActivity.kt`

### Scripts da Central (baixados de produção)
- `https://sac2.alfatechtelecom.com.br/central_assinante_web/assets/js/rotas.js`
- `https://sac2.alfatechtelecom.com.br/central_assinante_web/assets/js/main.js`
- `https://sac2.alfatechtelecom.com.br/central_assinante_web/assets/js/classes/class_1.js`

Cópias locais em `step-by-step/central-js/`.

---

## 1) Bridge JS atual do app

| Item | Valor |
|------|--------|
| Nome exposto no JS | `AndroidBridge` (`addJavascriptInterface(..., "AndroidBridge")`) |
| Métodos `@JavascriptInterface` | `getFcmToken(): String`, `showToast(message: String)` |
| Recebe JSON da página? | **Não** — nenhum método recebe payload/postMessage |
| Nativo → JS | **Sim**, via `evaluateJavascript` (hoje só auditoria/diagnóstico) |
| `window.ReactNativeWebView` | **Não existe** no app novo |
| Usado pela Central? | **Não** — a página nunca chama `AndroidBridge` |

Bug adicional: `WebAppInterface` captura `fcmToken` no `factory` da WebView; se o token chega depois, `getFcmToken()` pode devolver string vazia.

---

## 2) Protocolo real da página (como resolve `promiseID`)

Definido em `rotas.js`:

```javascript
const promisesWebView = [];

function postMessageMobile(type, dados) {
    const id = `_${Math.random().toString(36).substr(2, 9)}`;
    promisesWebView[id] = {};
    promisesWebView[id].promise = new Promise((resolve, reject) => {
        promisesWebView[id].resolve = resolve;
        promisesWebView[id].reject = reject;
        window.ReactNativeWebView.postMessage(JSON.stringify({ type, promiseID: id, dados }));
    });
    return promisesWebView[id].promise;
}

if (typeof window.ReactNativeWebView !== 'undefined') {
    addEventListener('message', (event) => {
        const data = JSON.parse(event.data);
        if (data.type === 'getUrl') { /* ... */ }
        return promisesWebView[data.promiseID].resolve(event);
    }, true);
}
```

Fluxo FCM pós-auth (`main.js` + `class_1.js`):

1. `iniciaFirebase()` (só se `window.ReactNativeWebView` existir)
2. `new HotsiteWeb().updateFirebaseToken()`
3. `getTokenAPP()` → `postMessageMobile('firebase_token')`
4. App nativo responde com MessageEvent cujo `event.data` é JSON com `promiseID` + `token` (ou `retorno`)
5. Listener resolve `promisesWebView[promiseID]`
6. `$.post(.../dados_cliente.php, { ACTION: 'updateFirebaseToken', TOKEN: token })`

**Quem resolve o promiseID:** o listener `addEventListener('message', ...)` em `rotas.js`, chamando `promisesWebView[data.promiseID].resolve(event)`.

---

## 3) Por que o login quebra se `ReactNativeWebView` existe cedo

Em `HotsiteWeb.iniciarSessao()` (`class_1.js`), se `ReactNativeWebView` existir, o login **espera** `postMessageMobile('firebase_token')` antes de `getValidaLogin`. Sem resposta correta ao `promiseID`, o login trava.

Por isso: **não expor `ReactNativeWebView` na tela de login**.

---

## 4) Onde o fluxo FCM falta no app novo

1. Página só fala com `window.ReactNativeWebView.postMessage` — app não implementa.
2. `AndroidBridge.getFcmToken()` existe, mas a Central não o usa.
3. `sendTokenToBackend()` no serviço FCM só loga.
4. Listener de `message` em `rotas.js` só é registrado se `ReactNativeWebView` existir **no load** do script — injeção tardia exige recriar esse listener (ou resolver `promisesWebView` direto).
5. Após login autenticado, `iniciaFirebase()` já pode ter rodado sem bridge; após habilitar o shim, precisa haver um novo pedido `firebase_token` (navegação autenticada ou chamada mínima equivalente — sem `iniciaFirebase()` se possível; candidato: `updateFirebaseToken()`).

---

## 5) Condição de autenticação (app)

Em `MainViewModel.onUrlChanged`:
- paths: `dados_cliente`, `faturas`, `planos`, `consumos`, `relatorios`, `atendimentos`, `configuracoes`, `principal`, `home`, `painel`, `dashboard`
- **não** inclui `/central_assinante_web/` puro após login
- tabs/atalhos forçam `isLoggedIn = true`

Condição segura proposta para ativar bridge FCM:
- URL **não** contém `/login` (nem cadastro/troca de senha)
- **e** há evidência de sessão: `isLoggedIn == true` **ou** path autenticado **ou** cookie `sessao` (checagem read-only via JS)

Nunca ativar em `/central_assinante_web/login`.

---

## 6) Arquivos a alterar (mínimo)

1. `CentralWebView.kt` — estender bridge; capturar postMessage; responder promiseID; habilitar shim só pós-auth
2. `MainViewModel.kt` — token atualizável; helper de auth-safe; logs FCM_*
3. `MyFirebaseMessagingService.kt` — `onNewToken` atualiza token local (sem inventar API HTTP própria)

Sem: Manifest/Gradle/namespace/telas/deps/UI.

---

## 7) Alteração mínima planejada (após aprovação)

1. Estender `WebAppInterface` com método que recebe a string JSON do `postMessage` da página.
2. Após autenticação segura, injetar **somente**:
   - `window.ReactNativeWebView = { postMessage: fn → AndroidBridge... }`
   - listener `message` idêntico ao de `rotas.js` (se ainda não existir)
3. Em `type === "firebase_token"`: obter token vivo via `FirebaseMessaging.getInstance().token` e responder com `evaluateJavascript` disparando `MessageEvent('message', { data: '{"promiseID":"...","token":"..."}' })`.
4. `onNewToken` grava token local para a próxima resposta.
5. Logs: `FCM_AUTH_STATE`, `FCM_TOKEN_REQUEST`, `FCM_TOKEN_READY`, `FCM_TOKEN_RESPONSE`, `FCM_TOKEN_REGISTERED` (token mascarado).

Critério de sucesso: push do sistema real da Central chega no app novo, sem quebrar login/cookies/navegação.
