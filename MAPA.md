# Mapa del repositorio

Inventario de todo lo que hay y dónde está. Los otros dos documentos de la raíz tienen otro
trabajo: [`README.md`](README.md) presenta el producto y [`CLAUDE.md`](CLAUDE.md) son las reglas de
trabajo que se cargan solas en cualquier sesión de Claude Code. [`SPEC.md`](SPEC.md) explica el
porqué de cada decisión de producto.

## Raíz

| Fichero | Qué es |
|---|---|
| `README.md` | Qué es Quilt y cómo se compila |
| `SPEC.md` | Spec de producto y hoja de ruta |
| `CLAUDE.md` | Contexto permanente y contratos que no se rompen |
| `MAPA.md` | Este fichero |
| `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `gradlew`, `gradle/` | Gradle |
| `keystore.properties`, `local.properties` | Locales, ignorados por git, nunca se suben |

## Código

| Ruta | Qué hay |
|---|---|
| `shared/src/commonMain/kotlin/com/baltajmn/habit` | Casi todo. Subcarpetas `model`, `data`, `ui`, `ui/theme`, `i18n`, `share`, `billing` |
| `shared/src/androidMain/.../habit` | `data`, `i18n`, `share`, `billing`, `widget` (Glance) |
| `shared/src/iosMain/.../habit` | `MainViewController.kt`, `data`, `i18n`, `share`, `billing` |
| `shared/src/commonTest` | `HabitTest.kt`, `DataTest.kt` |
| `shared/src/androidHostTest`, `shared/src/iosTest` | Arranque de los tests comunes en cada plataforma |
| `androidApp/src/main/kotlin/.../habit` | `MainActivity.kt`, `YearWidgetConfigActivity.kt` |
| `iosApp/iosApp` | `iOSApp.swift`, `Info.plist`, los `<lang>.lproj` |
| `iosApp/HabitWidget` | `HabitWidget.swift`, `HabitYearWidget.swift`, `HabitStore.swift` |
| `iosApp/Configuration/Config.xcconfig` | Versión, identificador y Team ID de iOS |
| `.github/workflows` | `tests.yml`, `release.yml` (Android), `release-ios.yml` |

`HabitStore.swift` reimplementa en Swift el modelo de `habits.json`. Un campo nuevo en `Habit` se
añade en los dos sitios en el mismo cambio, o el widget de iOS lo borra del historial del usuario
la primera vez que alguien toca el widget.

## Tienda

| Ruta | Qué hay |
|---|---|
| `store/lanzamiento.md` | Checklist de las dos tiendas. `[yo]` es de Claude, `[tú]` solo lo puede hacer el humano |
| `store/app-store.md` | Subida a la App Store, paso a paso |
| `store/ci.md` | Secretos de GitHub y cómo publican los tres workflows |
| `store/revenuecat.md` | Configuración de las compras |
| `store/ideas.md` | Ideas de producto que pasan el filtro de no añadir pantallas |
| `store/testers.md` | Reclutamiento de probadores |
| `store/video.md` | **Producción de los vídeos de promoción.** Cómo se hacen, qué se descartó y el orden de los clips |
| `store/play-listing.md`, `store/play-listing-en.txt` | Ficha de Google Play |
| `store/whatsnew/` | Novedades por idioma, cinco ficheros. Es el mecanismo vivo |
| `store/release-notes-1.1.txt` | Notas del lanzamiento 1.1. Histórico, lo sustituye `whatsnew/` |
| `store/screenshots/` | Capturas por idioma (`en`, `es`) y las de Play |
| `store/graphics/` | Iconos, gráfico destacado y los dos `quilt-flow-*.mp4` |
| `store/privacy/` | Política de privacidad. Se publica aparte, en el repositorio `quilt-privacy` |

## Herramientas

| Ruta | Qué hay |
|---|---|
| `tools/generate_icons.py`, `tools/icon-master.svg` | Fuente única de todos los iconos de las dos plataformas |
| `tools/video/` | Producción de vídeo vertical. Ver abajo |

### `tools/video`

```
grabar.sh              guioniza el emulador y graba          -> grabaciones/
quilt_video.py         monta rotulos y encuadre              -> salida/
guiones/<n>.txt        acciones de la grabacion              se versiona
recetas/<n>.json       recorte, encuadre y rotulos           se versiona
demo/generar.py        calcula el historial de demostracion  se versiona
demo/*.json            lo que genera, para hoy               ignorado
.venv/                 Pillow, unica dependencia             ignorado
grabaciones/           mp4 en bruto del emulador             ignorado
salida/                mp4 listo para subir                  ignorado
```

Se usa así:

```bash
cd tools/video
DATOS=dos.json MAX_SEG=36 ./grabar.sh 08-crear-habito
.venv/bin/python quilt_video.py recetas/08-crear-habito.json
```

El entorno se rehace con `uv venv .venv && uv pip install --python .venv/bin/python pillow`.
`quilt_video.py --autocheck` comprueba la cadena entera sin necesitar ninguna grabación.

Lo que se versiona son los guiones, las recetas y el generador de datos, que es lo que hace falta
para reproducir cualquier vídeo. Los mp4 y el entorno no, porque se regeneran con esos dos comandos
y pesan diecinueve megas. Los datos tampoco: son función de la fecha de hoy, así que un fichero
fijo en el repositorio caduca al día siguiente y todos los clips salen con cero hábitos hechos.

El cuaderno de la tarea, con lo que se probó y lo que se descartó, está en
[`store/video.md`](store/video.md).

## Lo que git ignora

Salidas de compilación (`build/`, `.gradle`, `.kotlin`, `xcuserdata`), la firma de release
(`keystore.properties`, `*.jks`, `*.keystore`), `local.properties`, `.DS_Store` y las tres carpetas
de `tools/video` de arriba. La firma y los secretos no entran nunca en el repositorio: viven en
GitHub repository secrets, y la clave secreta de RevenueCat (`sk_...`) en ningún sitio del árbol.

## Limpieza hecha en esta pasada

- Borrados los `.DS_Store` que quedaban en la raíz y en `androidApp/build/outputs/bundle`.
- Añadido `tools/video` con su `.gitignore`, para que las grabaciones y los montajes no acaben
  pesando en el historial.
- Creado `store/video.md` con lo aprendido sobre generación de vídeo.
- Nada del código de la app se ha tocado.
