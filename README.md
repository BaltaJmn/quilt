# Quilt

Un rastreador de hábitos anual para Android e iOS. Cada día que cumples es un retal; al final del año
tienes la colcha entera.

Compose Multiplatform sobre Kotlin Multiplatform: una sola interfaz para las dos plataformas, con
`expect`/`actual` solo donde el sistema obliga (almacenamiento, recordatorios, compartir, widgets).

## Qué hace

- Cuadrícula anual por hábito, 52-53 columnas × 7 filas, con etiquetas de mes.
- Marcar de un toque, hoy o cualquier día pasado.
- **Días saltados**: mantén pulsado un día y queda excusado. Ni rompe la racha ni cuenta contra el
  porcentaje de cumplimiento.
- Hábitos de cantidad (8 vasos, 30 minutos) con objetivo por día.
- Racha actual, racha máxima, días totales y porcentaje del año.
- **Widgets interactivos** en las dos plataformas, en tres tamaños, con racha y últimos 7 días.
- Recordatorios locales por hábito, respetando los días programados.
- Compartir una imagen de la semana, el mes o el año.
- Exportar e importar la copia completa en JSON, y exportar a CSV.
- Cinco idiomas: inglés, español, portugués, alemán y francés.

Sin cuenta, sin registro y sin analítica. Todo vive en un fichero JSON en el dispositivo. La única
conexión que hace la app es la de procesar una compra.

## Estructura

| Carpeta | Qué hay |
|---|---|
| `shared/src/commonMain` | Modelo, repositorio, interfaz, tabla de idiomas. Casi todo el código |
| `shared/src/androidMain` | Almacenamiento, recordatorios (`AlarmManager`), widget de Glance |
| `shared/src/iosMain` | Almacenamiento en App Group, recordatorios (`UNCalendarNotificationTrigger`) |
| `androidApp` | La `MainActivity` y los recursos de Android |
| `iosApp` | El punto de entrada de iOS y el widget de WidgetKit en Swift |
| `tools` | `generate_icons.py`, fuente única de todos los iconos, y `video/`, la producción de los verticales de promoción |
| `store` | Ficha de tienda, guía de RevenueCat, lista de lanzamiento y `video.md` |

`SPEC.md` tiene el porqué de cada decisión y `MAPA.md` el inventario completo de qué hay en cada
carpeta.

## Compilar

```bash
./gradlew :androidApp:assembleDebug
./gradlew :shared:testAndroidHostTest
```

Para iOS, abre `iosApp/iosApp.xcodeproj`. El esquema `iosApp` incluye la extensión del widget.

Para una compilación de release hace falta un `keystore.properties` en la raíz con `storeFile`,
`storePassword`, `keyAlias` y `keyPassword`. Ese fichero y el `.jks` están ignorados por git y no
deben subirse nunca.
