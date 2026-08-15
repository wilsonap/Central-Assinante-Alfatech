# 39 — Logo da Central no cabeçalho nativo

## Seletor DOM

O logo da Central **não** é um `<img>` principal. É:

```html
<div class="logo" :style="{ backgroundImage: 'url(\'' + logo_base + '\')' }">
```

Origem de `logo_base` (JS IXC):
- `data:…base64` de `hotsite_logo_base`, ou
- `{SERVER}/assets/img/logo.png`

Probe Android:
1. `window.app.logo_base`
2. `div.logo` → `getComputedStyle().backgroundImage`
3. Fallback `img[src*="logo"]` (exclui pix/bancos)

URL típica HTTP: `https://sac2.alfatechtelecom.com.br/central_assinante_web/assets/img/logo.png`

## Arquivos

- `CompanyLogoStore` — cache SharedPreferences + StateFlow
- `CompanyLogoImage` — Coil Fit 48dp + fallback Wi‑Fi
- Bridge `AndroidBridge.onCompanyLogoFound`
- Coil habilitado em `app/build.gradle.kts`
