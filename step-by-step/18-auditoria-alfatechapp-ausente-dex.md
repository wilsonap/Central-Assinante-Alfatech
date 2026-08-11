# 18 — Auditoria ClassNotFound: AlfatechApp ausente do DEX

## Evidências (antes da correção)

### 1) AlfatechApp.kt (completo)
- Path: `app/src/main/java/com/example/AlfatechApp.kt`
- Package: `com.example`
- Classe concreta `AlfatechApp : Application()`
- Sem `@HiltAndroidApp`
- Preserva `NotificationChannels.ensureCreated(this)`

### 2) Source sets / Gradle
- Só existe `app/build.gradle.kts` (não há `app/build.gradle`)
- Sem `sourceSets` custom, sem flavors, sem exclude de `java/`
- Source set efetivo: `main` → `app/src/main/java/...`

### 3) Módulos / manifests
- Único módulo `:app`
- Único Manifest: `app/src/main/AndroidManifest.xml`
- Único `AlfatechApp.kt`: path acima

### 4) Merged manifest (debug)
- `android:name="com.example.AlfatechApp"` (resolvido a partir de `.AlfatechApp`)
- `package="br.com.sac2.alfatechtelecom.com.br"` (applicationId)

### 5–6) APK/DEX e .class (ANTES)
- Task `compileDebugKotlin`: **NÃO EXISTIA**
- `AlfatechApp.class`: **NÃO**
- `Lcom/example/AlfatechApp` no DEX: **NÃO**
- `Lcom/example/MainActivity` no DEX: **NÃO** (todo o Kotlin estava de fora)

### Causa
`org.jetbrains.kotlin.plugin.compose` estava aplicado, mas **faltava** `org.jetbrains.kotlin.android`.  
Sem o plugin Android Kotlin, os `.kt` não entram no APK. O Manifest ainda apontava para `com.example.AlfatechApp` → ClassNotFoundException.

## Correção mínima
1. `gradle/libs.versions.toml` — alias `kotlin-android`
2. `build.gradle.kts` (root) — `kotlin.android` apply false
3. `app/build.gradle.kts` — aplicar `kotlin.android` + `kotlinOptions.jvmTarget = "11"`

Sem mudar applicationId, namespace, Manifest, Firebase, WebView, navegação ou ícone.

## Depois da correção
- `compileDebugKotlin` executou
- `AlfatechApp.class`: SIM
- Classe no DEX: SIM
