# 51 — Causa InvalidFragmentVersionForActivityResult (auditoria)

## Causa

`androidx.activity:activity-compose:1.10.1` (ActivityResult) + classpath com `androidx.fragment:fragment:1.1.0` (< 1.3.0).

## Quem traz Fragment 1.1.0

- `com.google.android.play:app-update:2.1.0` → `play-services-basement` → `fragment:1.1.0`
- `com.google.android.play:app-update-ktx:2.1.0` → `fragment:1.1.0`
- GMS/Firebase (`play-services-base`) também referencia `fragment:1.1.0`

Nada no catálogo força upgrade de Fragment.

## Correção mínima proposta (ainda não aplicada)

Declarar explicitamente `androidx.fragment:fragment` ≥ `1.3.0` (ex.: `1.8.6`) no `app` / `libs.versions.toml`.
