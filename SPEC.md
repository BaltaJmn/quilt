# Habit Tracker anual, spec de producto

App Compose Multiplatform (Android + iOS) de seguimiento de hábitos con vista **anual tipo heatmap**:
un cuadrito por día, un año entero por hábito en una sola pantalla.

---

## 1. Benchmark: qué copiar de cada app

| App | Modelo | Lo que hace bien (copiar) | Su punto débil (nuestra oportunidad) |
|---|---|---|---|
| **HabitKit** | Pago único / Pro | Grid tipo GitHub, muy visual, widgets bonitos, temas de color | Poca analítica, sin ritmos flexibles avanzados |
| **Streaks** | Pago único ~6 € | Límite de 12 hábitos *a propósito* (foco), integración Apple Health, timers y contadores, todo por iCloud sin recoger datos | Solo Apple. Sin Android ni web |
| **Habitify** | Freemium + suscripción + lifetime | Multiplataforma real, categorías, resúmenes diario/semanal/mensual, integraciones (Notion, Apple Health) | UI cargada, se siente "app de productividad" |
| **Loop Habit Tracker** | Gratis, open source | Puntuación de fuerza del hábito (no solo racha), gráficas honestas | Diseño anticuado, solo Android |
| **Habitica** | Freemium, gems | Gamificación fuerte, comunidad | Demasiada fricción para quien solo quiere marcar casillas |
| **Finch** | Suscripción | Tono emocional, cuidado personal, retención altísima | No sirve como tracker serio de datos |
| **Way of Life** | Freemium (límite 3) | Modelo sí/no/skip de tres estados, exportar CSV | Muy limitado en gratis |

**Posicionamiento nuestro:** el rigor visual de HabitKit + la calma estética de Finch + el "no te distraigo" de Streaks,
pero multiplataforma desde el día uno. Una frase: *"tu año entero en una pantalla"*.

---

## 2. Funcionalidad esencial

### MVP (lo mínimo para publicar)
- [x] Crear hábito: nombre, emoji, color pastel, días de la semana, veces al día
- [x] Marcar/desmarcar hoy con un toque; tocar cualquier día pasado del grid lo marca también
- [x] Grid anual por hábito (52-53 columnas x 7 filas) con etiquetas de mes
- [x] Racha actual + % de cumplimiento del año
- [x] Persistencia local (JSON), sin cuenta ni login
- [x] Pantalla de detalle del hábito: editar, archivar, borrar, estadísticas, cambio de año
- [x] Recordatorios locales por hábito (hora + días programados)
- [x] Widgets de pantalla de inicio interactivos (Glance + WidgetKit)
- [x] Compartir / guardar imagen de semana, mes o año

### v1.1. Lo que la gente pide en las reseñas
- Tres estados por día: hecho / fallado / **saltado** (enfermedad, vacaciones) para no romper rachas injustamente
- Hábitos de cantidad ("8 vasos", "30 min") con contador y temporizador
- Hábitos negativos ("no fumar"), se marcan al final del día
- Racha máxima histórica, no solo la actual
- Reordenar hábitos, archivar sin perder historial
- Exportar CSV / JSON (imprescindible para no dar sensación de secuestro de datos)

### v1.2. Retención
- Notas por día
- Apple Health / Health Connect: auto-marcar pasos, ejercicio, sueño
- Sincronización iCloud + Google Drive (archivo cifrado, sin backend propio)

### Descartado a propósito

- **Atajos de Siri / App Intents y tile de Ajustes rápidos.** El widget ya cubre "marcar sin abrir la app".
  Un tile solo dispara una acción, así que habría que elegir hábito y añadir configuración para ello.
- **Que iOS se salte el aviso si el hábito ya está hecho.** Exigiría triggers no repetitivos programados por
  adelantado, y con el tope de 64 pendientes eso son 12 días de margen con 5 hábitos, o 4 días con 15. Si el
  usuario tarda en abrir la app, los recordatorios se acaban en silencio. Peor que una notificación de más.

### Explícitamente fuera de alcance
Comunidad, retos sociales, chat, IA de coaching. Aumentan soporte y coste sin mover retención en esta categoría.

---

## 3. Diseño

**Principios**
1. La pantalla principal se entiende en 2 segundos: el año y el estado de hoy.
2. Marcar un hábito debe costar **un toque desde el icono de la app** (por eso los widgets son prioridad, no extra).
3. Nada de rojo para los fallos. Los huecos vacíos ya comunican; culpar es lo que hace que la gente desinstale.
4. Sin números grandes de "0%" en pantalla si no hay datos.

**Paleta** (`ui/theme/Theme.kt`)
- Fondo crema cálido `#FBF8F3` claro / `#17150F` oscuro. Nunca blanco puro ni negro puro
- Acento primario salvia `#6FAE9B`
- 8 pastel para hábitos: rosa, melocotón, mantequilla, salvia, menta, cielo, pervinca, lila
- Radios generosos (18 a 32 dp), bordes de 1 dp en vez de sombras
- Tinta fija oscura `#2E2A24` sobre los pastel. Los acentos son claros en ambos temas y el texto del sistema no contrastaría

---

## 4. Widgets y recordatorios (plan técnico)

| Pieza | Android | iOS |
|---|---|---|
| Widget | ✅ Glance, marcar con un toque (`ActionCallback`) | ✅ WidgetKit + `AppIntent`, marcar con un toque |
| Datos del widget | Mismo `habits.json` de `filesDir`, mismo proceso | App Group `group.com.baltajmn.habit` |
| Recordatorios | ✅ `AlarmManager` inexacto + `BroadcastReceiver` que reprograma | ✅ `UNCalendarNotificationTrigger` repetitivo, uno por día de la semana |

### El widget de hoy, en tres tamaños (implementado y verificado en ambas plataformas)

El mismo diseño en iOS (`HabitWidget.swift`) y Android (`HabitWidget.kt`), 1:1:

| Tamaño | Contenido |
|---|---|
| Pequeño | Cabecera `Hoy` + `3/6`, barra de progreso, rejilla 2×2 de cuadrados tocables (emoji o ✓). Con un solo hábito, un cuadrado grande |
| Mediano | Cabecera + barra + 3 filas: cuadrado tocable, nombre, racha con llama, últimos 7 días. `+N` si sobran hábitos |
| Grande | Lo mismo con 7 filas y cuadrados más grandes |

Decisiones que sostienen el diseño:

- **El cuadrado, no el círculo.** Es la misma forma del grid anual: el widget se lee como un zoom del año.
- **Los últimos 7 días.** Un tick de hoy no dice nada; siete cuadrados dicen si vas o no vas. Verde/color =
  hecho, hueco = pendiente, gris = saltado a propósito, casi invisible = no tocaba.
- **Relleno parcial.** Un hábito de 8 vasos enseña `3` sobre un cuadrado lleno hasta 3/8. Progreso, no binario.
- **La racha solo a partir de 2.** Una racha de 1 es "hecho hoy" y ya lo dice el cuadrado.
- **`+N` explícito.** Un widget que esconde hábitos en silencio es un widget en el que dejas de confiar.
- **Verde solo al terminar.** El número y la barra se ponen verdes únicamente con todo hecho.

Diferencias obligadas por la plataforma, no por gusto: las celdas de un escritorio Android son más
estrechas, así que los cortes responsive son 140/210/260 dp en vez de las familias de WidgetKit, y la fila
grande de Android mantiene la tira de 8 dp para no comerse el nombre del hábito.

Cada cuadrado marca por sí solo, también en el tamaño pequeño: allí no hay fila donde colgar el toque,
así que el `clickable` va en el propio cuadrado.

La previsualización del selector de widgets de Android (`res/layout/widget_preview.xml`) es un dibujo
estático, no un render del widget real: el selector infla `RemoteViews`, que solo admite una lista corta
de vistas. `View` a secas no está en ella, hay que usar `ImageView`. Al cambiar el diseño del widget hay
que acordarse de tocar también ese XML. No lleva nombres de hábito a propósito: los emoji valen igual en
los cinco idiomas.

El widget interactivo de marcar es el gancho: `alarmee` no lo cubre, hay que escribirlo nativo en cada
plataforma.

Recordatorios: hora por hábito, respetando los días programados, más un aviso opcional "cierra el día" a las 21:00.
Nada de notificaciones agresivas: son la causa número uno de desinstalación en esta categoría.

---

## 5. Monetización

### El dato que decide el modelo

**Esta app no tiene coste marginal por usuario.** No hay backend, ni cuentas, ni IA, ni almacenamiento nuestro.
Un usuario cuesta 0 € al mes. Cobrar una suscripción por algo que no cuesta nada de mantener es exactamente
lo que el mercado está castigando: cuando el valor es estático, el usuario pregunta por qué sigue pagando
después de haber "comprado" el producto, y lo dice en las reseñas.

La contrapartida honesta: la venta única no compone. Un suscriptor de 3 años vale más que una compra de 9,99 €.
Pero eso solo se cumple si la retención aguanta, y en esta categoría la gente se da de baja justo cuando deja
de usar la app, que es pronto.

**Decisión: pago único como único producto de pago. Nada de suscripción mientras no haya coste recurrente real.**

### Los tres niveles

| | Gratis, para siempre | Pro, pago único |
|---|---|---|
| Hábitos | 5 | Ilimitados |
| Grid anual, rachas, % del año | ✅ | ✅ |
| Recordatorios | Ilimitados | Ilimitados |
| Días saltados / vacaciones | ✅ | ✅ |
| **Exportar e importar tus datos** | ✅ | ✅ |
| Compartir semana / mes / año | ✅ | ✅ |
| Widgets | El de "Hoy" | Todos + el del grid anual |
| Paletas y temas | La base | Todas |
| Estadísticas | Año en curso | Histórico completo, mejor racha, comparativas |
| Diseños de tarjeta | 3 (semana, mes, año) | + variantes y fondos |

Tres cosas **no se tocan nunca**, y son argumento de venta:

1. **Los recordatorios son gratis.** Es el corazón de un tracker de hábitos. Cobrarlos es cobrar por que la app funcione.
2. **Exportar e importar es gratis.** Vendemos "sin cuenta, tus datos son tuyos". Poner la copia de seguridad detrás de un muro contradice el mensaje entero y es lo que hace odiar a Habitify.
3. **Los días saltados son gratis.** Es la función humana que nos diferencia; cobrar por no sentirte culpable es feo.

Lo que se vende es **más de lo mismo y más bonito**: más hábitos, más widgets, más colores, más historia.
Nada de lo que se vende es necesario para que la app cumpla su promesa.

### Precio

| Producto | Precio de referencia (ES/US/UK) | Nota |
|---|---|---|
| **Pro para siempre** | **9,99 €** | 6,99 € las primeras 4-6 semanas como precio de lanzamiento |
| Propina pequeña | 1,99 € | Consumible, solo en Ajustes |
| Propina mediana | 4,99 € | |
| Propina grande | 9,99 € | |

**Precios regionales activados.** Ambas tiendas permiten fijar precio por país sin tocar una línea de código.
Es la forma real de que sea asequible para todos los públicos: 9,99 € en España, el equivalente a 3-4 € en
Latinoamérica, India o el Sudeste Asiático. Sin esto, "asequible" es solo una palabra.

El rango de 2,99 a 9,99 € concentra la mayoría de las compras únicas del mercado, así que 9,99 € es el techo
razonable y 6,99 € un lanzamiento cómodo.

### Lo que llega de verdad al bolsillo

Con la comisión reducida del 15 % (Apple hay que solicitarla, Google la aplica sola por debajo de 1 M$ al año)
y el IVA español del 21 % ya incluido en el precio de escaparate:

```
9,99 € escaparate
÷ 1,21 (IVA)      = 8,26 €
− 15 % comisión   = 7,02 € netos
```

Redondeando, **te queda el 70 % del precio de escaparate**. A partir de ahí:

| Objetivo | Ventas necesarias | Descargas necesarias (al 2,5 % de conversión) |
|---|---|---|
| 500 €/mes | ~71 al mes | ~2.900 al mes |
| 1.000 €/mes | ~143 al mes | ~5.700 al mes |
| 3.000 €/mes | ~430 al mes | ~17.000 al mes |

Ese es el tamaño real del negocio. Una app de hábitos bien diseñada y compartible puede llegar a los primeros
dos escalones; el tercero ya exige que el bucle de compartir funcione de verdad.

### Cuándo sí tocaría una suscripción

Solo cuando exista un coste recurrente que justificarla. Hoy no lo hay, y el plan de sincronización es
**usar el iCloud y el Drive del propio usuario**, que no nos cuesta nada, así que sigue sin haberlo.

Se replantea si algún día aparece: servidor de sincronización propio, versión web, o funciones con IA.
En ese caso el modelo sería pago único para la app + suscripción **solo** para el servicio con coste,
que es el reparto que la gente acepta sin protestar.

### Reglas de implementación

1. **El plan gratis es la prueba.** No existen las pruebas gratuitas para compras únicas en las tiendas; el nivel gratuito generoso hace ese papel. Es un argumento, no una limitación.
2. **El paywall aparece al chocar**, no al arrancar: al crear el sexto hábito, al abrir el widget del año, o al tocar una paleta de Pro. Nunca en el primer arranque.
3. **Empezar con 5 hábitos gratis y medir.** Si a los tres meses la conversión no llega al 1,5 %, bajar a 4 o 3 en una versión y comparar. `HabitRepository.FREE_HABIT_LIMIT` está ahí para eso.
4. **"Restaurar compra" visible en Ajustes.** Es requisito de Apple y evita reseñas de una estrella.
5. **Nada de anuncios.** Rompen la estética y en esta categoría rinden mucho peor que una compra única.
6. **RevenueCat con su SDK de Kotlin Multiplatform.** Aunque solo haya un producto no consumible, resuelve de una vez las dos tiendas, el estado de la compra y el restaurar. La alternativa es escribir StoreKit 2 y Play Billing por separado.
7. **Solicitar el Small Business Program de Apple el primer día.** Es un formulario y es la diferencia entre quedarte el 70 % o el 55 %.

## 6. Estado actual del código

```
shared/src/commonMain/kotlin/com/baltajmn/habit/
├── model/Habit.kt          hábito + historial + racha + % cumplimiento (con tests)
├── data/Storage.kt         expect: leer/escribir el JSON  (actuals: filesDir / NSDocumentDirectory)
├── data/Reminders.kt       expect: sync/cancel + cálculo del próximo aviso (con tests)
├── data/HabitRepository.kt fuente única de verdad, estado Compose, límite del plan gratis
├── ui/theme/Theme.kt       paleta pastel claro/oscuro + formas
├── ui/YearGrid.kt          el grid anual, un solo Canvas
├── ui/HabitCard.kt         tarjeta: emoji, racha, botón de marcar, grid
├── ui/HabitForm.kt         mismo formulario para crear y para editar
├── ui/HabitDetailScreen.kt año navegable, 4 estadísticas, editar/archivar/borrar
├── ui/ShareScreen.kt       selector de periodo, vista previa, guardar y compartir
├── share/ShareCard.kt      dibuja la tarjeta 1080x1350 (semana / mes / año)
├── share/Sharing.kt        expect: PNG + hoja de compartir + guardar en fotos
├── data/Backup.kt          expect: guardar copia + elegir fichero (exportar/importar, gratis siempre)
├── billing/Billing.kt      expect: clave de RevenueCat + comprar / restaurar / refrescar
├── ui/Pro.kt               paywall y hoja de Ajustes
└── ui/HomeScreen.kt        lista, alta de hábito, ajustes, paywall
```

Trampas del proyecto de iOS, por si alguien vuelve a tocar el `Info.plist`:

- `CADisableMinimumFrameDurationOnPhone` tiene que estar y valer `true`. Compose Multiplatform lo
  comprueba al arrancar (`androidx.compose.ui.uikit.PlistSanityCheck`) y **aborta el proceso** si falta,
  con lo que la app se cierra sola nada más abrirla y en el Simulador parece que ni ha llegado a lanzarse.
- `plutil -extract CLAVE fmt fichero` **reescribe el fichero de entrada** si no le pasas `-o -`. Es la
  forma más rápida que hay de vaciar un `Info.plist` creyendo que solo lo estabas leyendo.

Almacenamiento: un único `habits.json` reescrito en cada cambio. Un año de un hábito diario son ~365 entradas;
no hace falta base de datos hasta que haya sincronización o varios años de historial de muchos hábitos.

Los días saltados viven en un `Set<String>` de fechas ISO junto al historial. El widget de iOS decodifica
ese fichero a un struct y lo vuelve a escribir, así que el campo tiene que existir también en Swift o
marcar desde el widget lo borraría. Allí es `Set<String>?`: la única forma cuyo codificador sintetizado
tolera que falte la clave en ficheros viejos **y** vuelve a omitirla en vez de escribir `null`, que el
`Set<String>` no nulable de Kotlin rechazaría. Comprobado con un script de Swift sobre las dos formas.

Exportar e importar están en Ajustes y son gratis para siempre. El importador **rechaza cualquier JSON
que no tenga forma de copia nuestra**: con `ignoreUnknownKeys`, un `{"foo":1}` decodificaría a un almacén
vacío y borraría el año en silencio. Y devuelve solo la lista de hábitos, nunca `isPro`, porque
restaurarlo sería saltarse el muro de pago con un editor de texto.

La escritura es **atómica** (fichero temporal y renombrado) y deja como copia la versión anterior en
`habits.bak.json`. Si el fichero principal aparece ilegible, se carga la copia en vez de arrancar vacío:
arrancar vacío haría que el siguiente guardado borrase el año entero. Al recuperarse, la app **repara el
fichero principal en el acto** y conserva la copia buena en lugar de degradar la dañada, para que un
segundo fallo no se lleve los dos. Probado corrompiendo el fichero a mano en el emulador.

Navegación: un `String?` con el id del hábito abierto en `App.kt`. Dos pantallas no justifican una librería de navegación.

**Siguiente paso sugerido:** copia de seguridad y exportación. Es la única objeción que hoy puede tumbar la app.

### Cómo funcionan los recordatorios

`Reminders.sync(habits)` es un resync completo: cancela todo y reprograma. Se llama al cargar y en cada
alta, edición o borrado, pero **no** al marcar una casilla.

- **Android**: una alarma por hábito, con la próxima ocurrencia calculada en código común (`nextReminderAt`).
  Alarma **inexacta** (`setAndAllowWhileIdle`) a propósito: la exacta exigiría el permiso `SCHEDULE_EXACT_ALARM`
  y un aviso de hábito puede llegar cinco minutos tarde sin problema. `ReminderReceiver` notifica y reprograma
  la siguiente; `BootReceiver` reprograma todo tras un reinicio. Si el hábito ya está hecho hoy, no avisa.
- **iOS**: un `UNCalendarNotificationTrigger` repetitivo por cada (hábito, día de la semana). El sistema los
  mantiene solo, sin receptores ni reprogramación. No puede saltarse el aviso si ya está hecho.
- El permiso se pide **cuando el usuario guarda el primer recordatorio**, no al arrancar la app.

---

---

## 7. Puntos fuertes y puntos débiles (revisión tras el MVP)

### Lo que ya tenemos y hay que defender

| Fuerza | Por qué importa | Contra quién juega |
|---|---|---|
| **Multiplataforma real desde el día uno** | Un solo código, misma app en iOS y Android | Streaks y Loop son de una sola plataforma; Habitify lo tiene pero exige cuenta |
| **Widget interactivo en las dos** | Marcar sin abrir la app es el gesto que sostiene el hábito | HabitKit muestra, no marca; Streaks solo en Apple |
| **Tarjeta compartible semana / mes / año** | Es el canal orgánico de la categoría: la gente enseña su grid | Solo HabitKit lo explota, y sin variantes de periodo |
| **Sin cuenta, sin red, sin telemetría** | Argumento de venta directo y verificable | Habitify y Habitica piden cuenta; Finch vive de la nube |
| **Estética calma, sin rojo ni culpa** | Los huecos vacíos ya comunican; culpar hace desinstalar | Casi todas marcan los fallos en rojo |
| **Días saltados de primera clase** | Vacaciones y enfermedad no cuentan como fallo, ni en la racha ni en el porcentaje | Es la queja más repetida en las reseñas de toda la categoría, y nadie la resuelve |
| **Recordatorio que no molesta si ya está hecho** | Menos notificación basura (hoy solo en Android) | Nadie lo hace bien |

### Lo que falta y hoy nos puede tumbar

Ordenado por daño real, no por esfuerzo.

1. **No hay hábitos de cantidad ni temporizador.** "8 vasos", "30 minutos" es la mitad de los hábitos reales. Streaks gana aquí.
2. **La monetización está cableada pero sin tienda detrás.** RevenueCat ya compra, restaura y refresca; falta crear el proyecto, los productos en las dos tiendas y pegar las dos claves públicas.
3. **Sin localización.** Todo el texto está en español dentro del código.
4. **Sin sincronización entre dispositivos.** Ahora es viable: se monta sobre el fichero de copia, que ya existe.
5. **iOS no puede saltarse el aviso si el hábito ya está hecho** (los triggers repetitivos no lo permiten, y la alternativa es peor: ver "Descartado a propósito").
6. **Detalles de acabado**: sin onboarding. La previsualización del widget en el selector de Android ya está (`res/layout/widget_preview.xml`).

### Dónde son débiles los demás, y cómo atacamos

| App | Su punto flojo | Nuestro ataque |
|---|---|---|
| **HabitKit** | Widgets de solo lectura, recordatorios pobres, paywall sobre lo básico | Widget que marca de un toque, recordatorios por día de la semana, 3 hábitos gratis de verdad |
| **Streaks** | Solo Apple, tope duro de 12 hábitos, sin web | Android e iOS con la misma compra, sin tope artificial en Pro |
| **Habitify** | Exige cuenta, suscripción para lo básico, interfaz recargada | Sin cuenta, funciona sin red, abre en un segundo |
| **Loop** | Solo Android, diseño de 2016, widgets flojos | Mismo rigor de datos con diseño actual y iOS |
| **Habitica / Finch** | Gamificación que cansa; no sirven como herramienta de datos | "Sin mascotas, sin puntos de experiencia: tu año y ya" |
| **Toda la categoría** | Ansiedad de racha: un día malo borra meses | Días saltados de primera clase, y racha que no se rompe por un día no programado |

### Plan de ataque, en orden

1. ~~**Copia de seguridad + exportar/importar JSON**~~. Hecho. Guardar usa el guardador de ficheros del sistema, no la hoja de compartir: una copia tiene que aterrizar donde el usuario la encuentre y se la pueda devolver al importador, y ahí salen todos los discos en la nube.
2. ~~**Días saltados**~~. Hecho. Campo aditivo `skipped`, sin migración: los ficheros viejos caen al valor por defecto. Pulsación larga sobre el día; un salto no cuenta ni en la racha ni en el denominador del cumplimiento, y apaga el recordatorio y la fila del widget de ese día.
3. **Hábitos de cantidad y temporizador.** Cierra el hueco frente a Streaks.
4. ~~**Monetización real con RevenueCat**~~. Código hecho. Queda la configuración de tiendas, ver §8.
5. ~~**Widgets con cara y ojos**~~. Hecho. Pequeño, mediano y grande, idénticos en iOS y Android, con racha, relleno parcial y la tira de los últimos 7 días (ver §4). Queda pendiente el widget del **grid anual completo** de un hábito, que es material de Pro.
6. **Sincronización iCloud / Drive** sobre el fichero de copia de seguridad.
7. ~~**Localización, 5 idiomas**~~. Hecho. Inglés, español, portugués, alemán y francés, en tabla única `S` en común más `L` en el widget de iOS. El idioma sale de las preferencias del sistema, no del bundle; cualquier idioma fuera de los cinco cae a inglés. Declarados en `CFBundleLocalizations` y en `locales_config.xml`, así que la App Store los lista y Android ofrece el selector de idioma por app. **Pendiente: repaso nativo de DE, FR y PT antes de promocionar.**
8. **Resumen anual automático** en diciembre, con aviso: el pico de compartir del año.

---

## 8. Identidad visual

### El nombre: Quilt

Una colcha de retales es exactamente lo que dibuja la app: un tablero de cuadrados de colores que se
cose de uno en uno. El nombre trae gratis el icono, la metáfora y el argumento de la ficha,
*cada día es un retal; al final del año tienes la colcha entera*.

Descartados: **Tesela** (libre, pero a una letra de Tesla: contamina la búsqueda e invita a una queja
de marca) y **Peldaño** (ocupado por una app de reparto). Comprobado que no hay ningún tracker de
hábitos llamado Quilt en ninguna de las dos tiendas; falta mirar el registro de marca de la clase 9.

El `applicationId` sigue siendo `com.baltajmn.habit`. Es irreversible tras la primera subida y no
tiene por qué coincidir con el nombre visible.

### El icono

El icono es el propio grid de la app: cuatro columnas de cuadraditos que suben 1-2-3-4, o sea cuatro
hábitos con rachas distintas. Colores de `HabitPalette` (rosa, melocotón, salvia, menta) sobre el fondo
del tema oscuro de la app (`#2C2820` a `#17150F`).

El fondo oscuro no es un capricho: en la comparativa a 48 px la versión crema desaparecía sobre un
launcher claro. Con el fondo oscuro los pastel son los protagonistas, que es exactamente la identidad.

- **iOS**: PNG de 1024 a sangre, el sistema aplica su máscara.
- **Android adaptativo**: fondo con degradado + grid en primer plano + capa `monochrome` para iconos
  tematizados. El grid mide 46 de 108 porque un cuadrado de lado L solo cabe en el círculo de zona segura
  de 66 si L ≤ 66/√2 = 46,7. Dimensionarlo a 62 hacía que el launcher recortara las esquinas.
- **Android heredado** (API 24-25, sin iconos adaptativos): PNG cuadrados y redondos en las cinco densidades.
- **Icono de notificación**: la misma silueta, solo las celdas llenas, blanco sobre transparente.

Todo sale de un solo sitio: `tools/generate_icons.py` (necesita `rsvg-convert`). Cambiar un color o la
geometría es tocar ese fichero y volver a ejecutarlo; escribe los PNG de iOS, los de Android y los
vectores adaptativos, monocromo y de notificación.

## 9. Estimación para salir al mercado

### Lo que ya está hecho y verificado en dispositivo

| Bloque | Estado |
|---|---|
| Modelo, historial, rachas, % de cumplimiento | hecho, con tests |
| Persistencia en `habits.json` | hecho, las dos plataformas |
| Home, detalle de hábito, formulario, grid anual | hecho |
| Recordatorios | hecho, avisos recibidos en emulador y simulador |
| Widgets interactivos, 3 tamaños | hecho, marcado y tamaños comprobados en emulador y simulador |
| Previsualización del widget en el selector de Android | hecho, `res/layout/widget_preview.xml` |
| Compartir / guardar PNG semana-mes-año | hecho, PNG escrito en disco |
| Compra, restaurar y comprobación de derecho | hecho, contra el SDK real |

Unas 3.100 líneas de Kotlin y Swift.

### Lo que falta, en jornadas de trabajo

**Producto (bloqueante para publicar), unas 6 jornadas**

| Tarea | Jornadas | Por qué bloquea |
|---|---|---|
| Localización inglés + español | 2 | Sin inglés el mercado se reduce a una décima parte |
| Onboarding | 1 | El icono, el de notificación y la previsualización del widget ya están hechos |
| Pulido y errores en dispositivos reales | 3 | Todo se ha probado en emulador y simulador |

**Tienda (no es código), unas 5 jornadas**

| Tarea | Jornadas | Nota |
|---|---|---|
| Cuentas de desarrollador | trámite | Apple 99 $/año, Google 25 $ una vez |
| Proyecto RevenueCat, productos, derecho `pro`, oferta | 0,5 | Después solo hay que pegar las dos claves públicas |
| Probar compras de verdad (sandbox de Apple, licencia de Play) | 1 | Es donde aparecen los fallos reales de compra |
| Política de privacidad + formularios App Privacy y Data Safety | 0,5 | Hace falta una URL pública |
| Firma: keystore, certificados, perfiles, primera subida | 1 | |
| Capturas y textos de ficha, en dos idiomas y dos tiendas | 2 | Es la mitad de la conversión de descarga |

### Los plazos que no dependen del trabajo

1. **Google, 12 probadores durante 14 días seguidos.** Obligatorio para cuentas personales creadas después del 13/11/2023, y desde 2026 Google además mira que esos probadores usaran la app de verdad. Las cuentas de empresa están exentas. **Es el plazo más largo: hay que arrancarlo mientras se hace el pulido, no después.**
2. **Verificación de identidad de Google Play**: de días a un par de semanas.
3. **Revisión de Apple**: normalmente 24-48 h, pero conviene contar con un rechazo y una semana.

### Resumen

**≈ 11 jornadas de trabajo. De 5 a 7 semanas de calendario** si el test cerrado de Google se lanza pronto y en paralelo.

### Una corrección al argumento de venta

El SPEC dice "sin cuenta, sin red, sin telemetría". Con RevenueCat dentro, lo tercero deja de ser cierto:
la app llama a `api.revenuecat.com` y crea un identificador anónimo. Sigue sin haber cuenta, sin correo y
sin analítica de uso, pero el mensaje honesto es **"sin cuenta y sin analítica; la única conexión que hace la
app es la de la compra"**. Hay que declararlo en los formularios de privacidad de las dos tiendas.

---

## Fuentes

- [12 best habit tracking apps in 2026, 2sync](https://2sync.com/blog/best-habit-tracker-apps)
- [The 10 Best Habit Tracker Apps of 2026, Reclaim](https://reclaim.ai/blog/habit-tracker-apps)
- [Best HabitKit Alternatives for Data-Driven Habit Tracking, Aftertone](https://www.aftertone.io/guides/best-habitkit-alternatives)
- [Best Habit Tracker Apps 2026: Tested & Compared, loggd.life](https://loggd.life/blog/best-habit-tracker-apps-2026)
- [State of Subscription Apps 2026, RevenueCat](https://www.revenuecat.com/state-of-subscription-apps)
- [App Monetization Strategies That Actually Work in 2026, Octy](https://octy.dev/blog/app-monetization-strategies-2026/)
- [Alarmee: alarmas y notificaciones locales en KMP](https://github.com/Tweener/alarmee)
- [Widgets con SwiftUI y Compose sobre código KMP, John O'Reilly](https://johnoreilly.dev/posts/ios-android-widget-kmp/)
