# 16 — Correção: retorno WebView → Home nativa completa

## Problema

Após abrir a Central (WebView) e voltar para a Home nativa, os atalhos/menus da Home podiam não aparecer (estado intermediário / Home “vazia”).

## Causa

Não há `NavController`. O fluxo usa `MainViewModel.currentScreen` (`0` = Home, `1` = WebView).

A `CentralWebView` permanece **sempre** na composition (sessão/cookies). A `HomeScreen` era apenas sobreposta quando `currentScreen == 0`. O `AndroidView`/`WebView` nativo pode continuar desenhando por cima do Compose (z-order), cobrindo os atalhos.

Além disso, a seta da barra azul usava `canWebViewGoBack()` e podia permanecer na WebView em vez de restaurar a Home nativa.

## Arquivos alterados

| Arquivo | Função / alteração |
|---------|-------------------|
| `MainActivity.kt` | Seta da barra azul → sempre `navigateToHome()`. `BackHandler`: raiz da Central ou sem histórico → Home; subpágina com histórico → `goBack()`. Passa `isVisible = currentScreen == 1`. |
| `CentralWebView.kt` | Novo parâmetro `isVisible`; no `update` do `AndroidView`, `View.GONE`/`VISIBLE` sem destruir a WebView. |
| `MainViewModel.kt` | `isAtCentralRoot()` para decidir saída da WebView no Back do sistema. `navigateToHome()` inalterado (`currentScreen=0`, `selectedTab=0`). |

## O que NÃO foi alterado

URL IXC, autenticação, cookies, sessão, login automático, FCM, Firebase, AndroidBridge, ReactNativeWebView, Room, notificações, design da Home/Central.

## Comportamento esperado

1. Home nativa → Central WebView → seta azul → Home nativa **completa** (todos os atalhos).
2. Back Android em subpágina com `canGoBack()` → histórico web.
3. Back Android na raiz da Central (ou sem histórico) → Home nativa.
4. Sessão WebView preservada (WebView só ocultada, não destruída).

## Como validar

1. Login → Início → abrir atalho (ex.: Faturas) → seta azul → Home com todos os atalhos.
2. Abrir Central Completa → navegar subpágina → Back Android (histórico) → na raiz, Back → Home completa.
3. Confirmar que sessão/login na Central permanece ao reabrir.
