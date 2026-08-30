# Quilt

Rastreador de hábitos anual, Android + iOS, Compose Multiplatform sobre Kotlin Multiplatform.
Nombre de producto **Quilt**. Identificador en las dos tiendas: `com.baltajmn.habit`.

Este fichero lo carga Claude Code solo en cualquier sesión abierta sobre este repositorio, desde
cualquier cuenta. Es el contexto permanente del proyecto: si algo hay que saber siempre, va aquí,
no en el chat.

## Dónde vive cada cosa

| Ruta | Qué es |
|---|---|
| `shared/src/commonMain` | Toda la interfaz y toda la lógica. Es donde se trabaja por defecto. |
| `shared/src/androidMain`, `shared/src/iosMain` | Solo los `actual` que el sistema obliga: almacenamiento, recordatorios, compartir, copia de seguridad, widgets, compras, idioma. |
| `androidApp` | Activity, permisos, Glance. Nada de lógica. |
| `iosApp/iosApp` | `iOSApp.swift`, `Info.plist`, `<lang>.lproj`. Grupo sincronizado: un fichero nuevo en la carpeta entra en el target sin tocar el `.pbxproj`. |
| `iosApp/HabitWidget` | Widget de WidgetKit. Reimplementa en Swift el modelo de `habits.json`: si cambia el JSON, cambian los dos lados. |
| `iosApp/Configuration/Config.xcconfig` | Versión, identificador y Team ID de iOS. No se editan en el `.pbxproj`. |
| `SPEC.md` | Spec de producto y hoja de ruta. Lo que la app hace y lo que aún no. |
| `store/lanzamiento.md` | Checklist de lanzamiento de las dos tiendas. `[yo]` es tarea de Claude, `[tú]` es tarea que solo puede hacer el humano. |
| `store/ci.md` | Secretos de GitHub y cómo publican los tres workflows. |
| `store/app-store.md` | Subida a la App Store, paso a paso. Lo que falta es todo `[tú]`. |
| `store/ideas.md` | Ideas de producto que pasan el filtro de no añadir pantallas. |
| `store/revenuecat.md` | Configuración de las compras. |
| `.github/workflows` | Publicación por tag. |

## Contratos que no se rompen

- `habits.json` en el App Group `group.com.baltajmn.habit` (iOS) y en el directorio de la app
  (Android) es el contrato entre app y widget. Se escribe de forma atómica y con copia `.bak`.
- `Strings.kt` obliga a los cinco idiomas (en, es, pt, de, fr) por firma de función. La tabla `L`
  del widget de iOS es su espejo en Swift y hay que tocarla a la vez.
- El `expect object` mínimo permite miembros extra en los `actual`. Los ganchos que la UI necesita
  (`Reminders.onNeedsPermission`, `Backup.onPickFile`) viven ahí, no en el común.

## Seguridad, sin excepciones

- El `.jks` de firma y `keystore.properties` nunca se suben al repositorio. Están en `.gitignore`.
- Los secretos viven solo en GitHub repository secrets.
- La clave secreta de RevenueCat (`sk_...`) nunca entra en el repositorio. Solo las públicas
  (`goog_`, `appl_`), que ya viajan dentro del binario.
- La cuenta de servicio que publica en Play es distinta de la de RevenueCat, que es de solo
  lectura a propósito.

## Cómo se marcan los cambios entre las dos cuentas

El registro es `git log`, no un fichero paralelo que se desincroniza al segundo día.

Cada commit hecho con Claude lleva dos trailers:

```
Co-Authored-By: Claude Opus 5 <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_<id>
```

Con eso:

- `git log --grep='Claude-Session'` lista todo lo que ha tocado Claude.
- `git log --grep='session_<id>'` reconstruye exactamente qué hizo una sesión concreta, y el
  enlace abre la conversación completa en la cuenta que la ejecutó.
- `git log --format='%h %an %s'` separa lo humano de lo asistido sin más herramientas.

El cuerpo del commit explica **por qué**, no qué. El diff ya dice qué.

## Estilo

- Documentación y commits en español, salvo `store/play-listing-en.txt` y demás material de tienda
  en inglés.
- Sin em dash y sin emoji en nada que escriba Claude. En texto, en comentarios y en commits.
- Comentarios: solo los que explican una decisión que el código no puede explicar solo.

## Comandos

```bash
./gradlew :shared:testAndroidHostTest          # tests comunes sobre JVM
./gradlew :shared:iosSimulatorArm64Test        # tests comunes sobre Kotlin/Native
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:bundleRelease            # necesita keystore.properties
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp \
  -destination 'generic/platform=iOS Simulator' build CODE_SIGNING_ALLOWED=NO
```

Publicar: `git tag v1.2 && git push origin v1.2` dispara el workflow de Android.
