# 20 — Auditoria: login automático da Central (sem alteração)

**Data:** 2026-08-11  
**Escopo:** somente WebView/login — nenhuma alteração de código nesta etapa.

## Como o login automático funcionava

Não há injeção nativa de usuário/senha. O “login automático” é da **própria Central (JS IXC)** usando dados persistidos na WebView:

1. Cookie `manter_conectado` = `S`
2. `localStorage` / `sessionStorage` com `sessao` (e `dados` / `parametros`)
3. Cookie `sessao`
4. AJAX `getValidaSessao` em `login.php`

Trecho de referência (JS da Central): `validaSessao` / `manter_conectado` em  
`step-by-step/central-js/central_assinante_web_assets_js_classes_class_1.js` (~4083–4128).

O app só **habilita a persistência**:

| Local | Papel |
|-------|--------|
| `MainActivity.onCreate` | `CookieManager.setAcceptCookie(true)` |
| `CentralWebView` factory | `setAcceptCookie` + `setAcceptThirdPartyCookies` |
| Settings | `domStorageEnabled = true` (localStorage) |
| `onPageFinished` | `CookieManager.flush()` |

`window.iniciaFirebase()` **não faz login**. Roda após área autenticada, se existir `ReactNativeWebView`, só para FCM (`updateFirebaseToken`).

## O que mudou no código WebView/login

`git diff 3f18dac HEAD` em:

- `CentralWebView.kt`
- `MainActivity.kt`
- `MainViewModel.kt`

→ **0 linhas**. Fluxo de cookies / `onPageFinished` / bridge FCM está **igual** ao estado em que o login automático funcionava.

A alteração `isVisible` / `View.GONE` (commit `6b0cd6f`) foi **revertida** em `9332f65`. Hoje **não** há `isVisible` no tree.

## Condição que impede o login automático agora

Não há trecho novo no Kotlin que desligue o auto-login. A condição que falha é a da **Central**:

```
dados && parametros && sessao && $.cookie('manter_conectado')
→ getValidaSessao → sessao_existe
```

Se cookies/`localStorage` foram limpos (desinstalação/reinstalação no diagnóstico ClassNotFound/ícone, ou clear data), `manter_conectado`/`sessao` somem → Central mostra login.

## Correção mínima sugerida (ainda NÃO aplicar)

1. **Se dados foram limpos:** não há bug de código — logar uma vez com “manter conectado”; cookies voltam a persistir com o código atual.
2. **Se a sessão ainda existe no aparelho e mesmo assim falha:** aí sim investigar runtime (CookieManager / DomStorage) com logs — sem reverter Home/WebView.
3. **Não** restaurar login via injetar credenciais; **não** chamar `iniciaFirebase()` no login.
4. Lateral: `app/google-services.json` foi removido em `9332f65` — afeta Firebase no build, **não** o mecanismo de cookie do auto-login. Restaurar só se precisar do FCM de novo.

## Fora de escopo (confirmado)

AlfatechApp, Manifest, Gradle/package, ícones, Room, navegação Home — não alteram o algoritmo de auto-login da Central.
