# Step-by-step — Análise inicial da estrutura

**Data:** 2026-08-08  
**Objetivo:** Mapear o app antes das melhorias.

## O que é o projeto

- App Android nativo (Kotlin + Jetpack Compose), versão **2.0.0**
- Application ID: `br.com.sac2.alfatechtelecom.com.br`
- Namespace do código (legado): `com.example`
- Função: shell nativo + WebView da Central Web em  
  `https://sac2.alfatechtelecom.com.br/central_assinante_web/`
- Push: Firebase Cloud Messaging + persistência local Room

## Arquivos Kotlin e função

| Arquivo | Função | Linhas (aprox.) |
|---------|--------|-----------------|
| `MainActivity.kt` | Entry point, shell Compose, TopBar/BottomNav, deep links | ~397 |
| `ui/MainViewModel.kt` | Estado (tela, tabs, URL, login heurístico, FCM, notificações) | ~151 |
| `ui/components/CentralWebView.kt` | WebView, cookies, JS bridge, PDF/WhatsApp | ~522 |
| `ui/screens/HomeScreen.kt` | Home nativa com atalhos | ~293 |
| `ui/screens/NotificationsScreen.kt` | Lista de avisos (bottom sheet) | — |
| `ui/screens/InvoicesScreen.kt` | UI nativa de faturas | **não usada** |
| `ui/screens/SupportScreen.kt` | Contatos suporte | **não usada** |
| `service/MyFirebaseMessagingService.kt` | Recebe FCM, salva Room, notifica | — |
| `data/local/*` | Room: entity, DAO, database | — |
| `ui/theme/*` | Cores, tema Material 3, tipografia | — |

## Arquitetura atual

```
MainActivity
  └─ AlfatechMainApp
       ├─ MainViewModel (StateFlow)
       ├─ CentralWebView (sempre montado)
       ├─ HomeScreen (overlay se screen=0)
       └─ NotificationsScreen (ModalBottomSheet)
```

- MVVM leve, sem DI, sem Navigation Compose em uso, sem Clean Architecture.

## Dívida técnica identificada (prioridades futuras)

1. Namespace `com.example` vs branding real
2. Telas e deps mortas (Invoices/Support, Retrofit/Moshi/firebase-ai)
3. Arquivos grandes (CentralWebView, MainActivity)
4. Token FCM não enviado ao backend
5. `markAsRead` incompleto
6. Login por heurística de URL
7. Testes quebrados / template
8. Sem flavors de ambiente (dev/staging/prod)
9. `usesCleartextTraffic=true`, minify desligado

## Próximo passo

Definir com o usuário o foco das melhorias (UX, arquitetura, FCM, limpeza, branding, etc.).
