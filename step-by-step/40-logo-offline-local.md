# 40 — Logo offline (cópia local)

- Download http(s) ou decode data:image → `filesDir/company_logo/company_logo.{ext}`
- Prefs: `company_logo_url`, `company_logo_local_path`, `company_logo_updated_at`
- Compose usa sempre arquivo local (Coil + File), não URL remota
- Máx. 2 MB; magic-bytes; hosts Alfatech
- Offline: último arquivo; sem arquivo → fallback Wi‑Fi
