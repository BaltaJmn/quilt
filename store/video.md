# Vídeo de promoción

Cómo se producen los verticales de TikTok de Quilt, qué se ha probado y qué se descartó.
Las herramientas viven en [`tools/video`](../tools/video). Este fichero es el cuaderno de la
tarea: lo que ya está resuelto y lo que queda por delante.

## Cómo se hace un vídeo

Dos comandos por clip.

```bash
cd tools/video
./grabar.sh 01-un-toque                      # emulador -> grabaciones/01-un-toque.mp4
.venv/bin/python quilt_video.py recetas/01-un-toque.json   # -> salida/quilt-01-un-toque.mp4
```

Los diez de golpe, cuando solo han cambiado las recetas:

```bash
cd tools/video && for r in recetas/*.json; do .venv/bin/python quilt_video.py "$r"; done
```

`grabar.sh` imprime al terminar los instantes en que cambió la pantalla. Esos números son
directamente los tiempos que hay que escribir en los rótulos de la receta. No hay que ir mirando
el vídeo fotograma a fotograma.

Todos los vídeos salen con la misma cabecera y el mismo cierre, que los pone `quilt_video.py` y no
la receta: 1,7 s de portada con el icono, el nombre **Quilt** y una frase corta que sí elige la
receta, y 2,8 s de cierre con el icono, el nombre y "Gratis en Google Play y App Store". Son
`INTRO_SEG`, `CIERRE_SEG`, `MARCA` y `CIERRE_SUB` en la cabecera del script. Con eso, un cuerpo de
10,5 s ya pasa de los 15 s que pide el formato.

### El guion de grabación

`guiones/<nombre>.txt`, una acción por línea. `esperar`, `tocar x y`, `pulsar x y ms`,
`deslizar x1 y1 x2 y2 ms`, `atras`, `inicio`, `texto`, `persiana`, `cerrar`, `noche on|off`,
`idioma es-ES`.

Un clip que necesite otro conjunto de datos lo pide con `DATOS`, y uno más largo que la marca por
defecto con `MAX_SEG`:

```bash
DATOS=dos.json MAX_SEG=36 ./grabar.sh 08-crear-habito
```

### El estado de partida

Es la mitad del trabajo. Un emulador que arrastra el estado de la toma anterior da un clip
distinto cada vez, y el fallo no avisa: el vídeo se genera igual y solo se ve al mirarlo. Lo que
`grabar.sh` deja fijo antes de cada grabación, y por qué:

| Qué | Por qué |
|---|---|
| `demo/generar.py` en cada toma | El historial se calcula a partir de la fecha de hoy y una semilla. Con ficheros fijos, al día siguiente "hoy" ya es otro día y todos los clips salen con cero hábitos hechos |
| `pm clear` y no solo `force-stop` | El SDK de compras guarda su copia del derecho en las preferencias. Sin borrarlas, un emulador donde alguna vez se probó la compra arranca en Pro |
| Wifi y datos apagados durante toda la toma | `pm clear` borra la copia local, pero con red el SDK vuelve a preguntar, la cuenta del emulador tiene la compra de prueba y la app se guarda `isPro: true` otra vez. Ningún clip necesita red |
| `accelerometer_rotation 0` y `user_rotation 0` | Un emulador que se quedó tumbado graba 1920x1030 y el montaje sale con la app dentro de una franja horizontal |
| `cmd uimode night no` y locale `es-ES` | El clip del modo oscuro deja el emulador de noche y el de idiomas en francés. El siguiente saldría con el tema o el idioma equivocado |
| Los dos IME desactivados | Ver más abajo |
| Barra de estado a las 9:41, batería al 100%, sin notificaciones | Modo demostración de SystemUI. No llega dentro de la persiana desplegada |

Por eso dos tomas del mismo guion salen iguales, y por eso el hábito que se marca en el vídeo
aparece siempre sin marcar al empezar.

### El teclado tapa el formulario

El formulario de hábito es una `Column` sin `verticalScroll` ni `imePadding`: con el teclado
abierto, "Crear hábito" queda fuera de la pantalla y todas las acciones posteriores del guion caen
al vacío. Y no vale cerrarlo con `atras` ni con escape, porque los dos cierran la hoja modal
entera.

La salida es desactivar el IME, que no toca la app: `input text` inyecta los eventos por
instrumentación, no por el teclado, así que el texto se escribe igual. Con dos matices que
costaron una tarde:

- Hay que desactivar **los dos** IME, Gboard y el de voz de Google TTS. Si queda uno habilitado el
  sistema cae a él, y el de voz ocupa media pantalla con "Tap to speak".
- `pm disable-user` sobre Gboard no vale, por lo mismo.
- Escribir despierta a Gboard, que dibuja su barra flotante sobre el formulario aunque el IME esté
  desactivado. La acción `texto` lo mata justo después. El adb tarda medio segundo en hacerlo, así
  que la barra se ve un instante en la grabación y el tramo de la receta lo salta.

Para devolverle el teclado al emulador cuando se termina de grabar:

```bash
adb shell ime enable com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME
```

### Cambiar el idioma reinicia el proceso

`Strings.kt` lee el idioma una sola vez, al primer acceso de `object L`. `cmd locale
set-app-locales` recrea la Activity pero no el proceso, así que sin reiniciar la app la pantalla se
queda en el idioma anterior. La acción `idioma` hace `force-stop` y vuelve a arrancar, y eso mete
dos segundos de pantalla de arranque que la receta recorta con un tramo por idioma.

### La receta de montaje

`recetas/<nombre>.json`. Recorte, encuadre y lista de rótulos con sus tiempos.

```json
{
  "salida": "quilt-01-un-toque.mp4",
  "fuente": "grabaciones/01-un-toque.mp4",
  "recorte": { "desde": 2.0, "hasta": 13.2 },
  "encuadre": { "x": 0, "y": 90, "w": 1080, "h": 1920 },
  "rotulos": [
    { "t": [0.2, 2.4], "texto": "Ocho meses marcando\nun cuadrito al día",
      "pos": "arriba", "tam": 66, "acento": "salvia" }
  ]
}
```

`pos` acepta `arriba`, `centro` y `abajo`. Para colocar un rótulo en un sitio concreto, `y` en
píxeles gana a `pos`. Los acentos son los ocho colores de `HabitPalette`.

Un vertical casi nunca se deja encuadrar con un solo recorte. Cuando el clip pasa por pantallas
que piden ventanas distintas, `recorte` y `encuadre` se sustituyen por `tramos`, que además sirven
para saltarse un trozo de la grabación:

```json
"tramos": [
  { "desde": 2.5, "hasta": 9.6,  "encuadre": { "x": 0, "y": 90,  "w": 1080, "h": 1920 } },
  { "desde": 9.9, "hasta": 15.2, "encuadre": { "x": 0, "y": 480, "w": 1080, "h": 1920 } }
]
```

Los tiempos de los rótulos van sobre el cuerpo ya montado, no sobre la grabación: si el primer
tramo dura 7,1 s, el segundo empieza en el 7,1.

El montaje alarga la grabación con `tpad=stop_mode=clone` seis segundos, así que un tramo puede
pasarse hasta seis segundos del final del fichero y se queda congelado en el último fotograma. Eso
obliga a que el último fotograma sea el que se quiere ver: el guion de `03` termina con la hoja de
editar abierta, sin `atras`, por eso.

El encuadre `y: 90` quita la barra de estado del 1080x2400 del emulador y deja 1080x1920 exactos.
También se lleva por delante el botón flotante de añadir hábito, que no hace falta en un clip.

## Decisiones y por qué

### El texto lo dibuja Pillow, no ffmpeg

El ffmpeg de Homebrew de esta máquina está compilado sin freetype: no tiene `drawtext` ni el
filtro `subtitles` ni libass. Se comprueba con `ffmpeg -filters | grep drawtext`, que no devuelve
nada. Sí tiene `overlay`, `fade`, `crop`, `scale`, `zoompan` y `xfade`.

La salida no es recompilar ffmpeg. Es dibujar cada rótulo con Pillow a un PNG con transparencia y
superponerlo con `overlay` y `enable='between(t,a,b)'`, con el alfa animado por `fade`. Cuesta lo
mismo y da control tipográfico que `drawtext` no tiene: pastilla con esquinas redondeadas, sombra
difuminada, barra de acento y partido de línea medido con la fuente real.

Única dependencia: Pillow, en `tools/video/.venv`. Nada más.

### La trampa de screenrecord: frame rate variable

`adb shell screenrecord` solo emite un fotograma cuando la pantalla cambia. Una grabación de 15
segundos de Quilt, que es una app estática, trae **33 fotogramas en total**, agrupados en los
instantes en que algo se movió:

```
0.000  4.120 ... 4.943  6.620  10.757
```

Sobre ese flujo, `trim=start=1.0` no corta en el segundo 1: descarta el fotograma que se estaba
viendo en ese momento, porque su PTS es 0, y el reloj salta al siguiente fotograma real, que
está en 4.12. El montaje sale desplazado más de tres segundos y los rótulos caen sobre la pantalla
equivocada.

La solución es `fps=30` **antes** de `trim`, no después. Así existe un fotograma en cada 1/30 de
segundo y cualquier operación por tiempo significa lo que dice. Está en `quilt_video.py` con su
comentario. Es el error que más caro sale en toda la cadena, porque el vídeo se genera sin fallar
y solo se nota al mirarlo.

### grabar.sh y el descriptor 3

`adb shell` hereda stdin. Si el bucle que lee el guion lee de stdin, la primera llamada a `adb` se
traga el resto del fichero y el guion se corta tras una acción. El guion se lee por el descriptor
3 (`while read ... <&3` ... `done 3< "$guion"`).

### MoneyPrinterTurbo

Está instalado en `~/tools/MoneyPrinterTurbo`. Como producto no sirve aquí: compone vídeo
"faceless" a partir de un tema, con guion de un LLM, material de archivo de Pexels y voz sintética.
Nuestro material es una grabación real de la app, que es justo lo que ese flujo no acepta como
entrada.

Lo aprovechable era su entorno: su `.venv` ya traía moviepy 2.1.2, Pillow 11.3.0, numpy,
faster-whisper y edge-tts sobre Python 3.13. Ahí se confirmó que moviepy dibuja el texto con
Pillow y no con `drawtext`, que es lo que desbloqueó todo. Al final ni siquiera hace falta moviepy:
Pillow y ffmpeg directos son menos piezas y bastante más rápidos, porque moviepy pasa cada
fotograma a numpy en Python.

Queda pendiente para cuando haya voz en off: `edge-tts` para la locución y `faster-whisper` para
sacar los tiempos palabra a palabra de los subtítulos. Los dos ya están probados en ese entorno.

### Lo que se descartó

- **Recompilar ffmpeg con freetype.** Media hora de compilación para algo que Pillow ya hace mejor.
- **moviepy.** Envoltorio de lo que hacen Pillow y ffmpeg, más lento y una dependencia grande más.
- **Remotion y frameworks de vídeo programático en Node.** Exigen Chrome headless y montar una
  plantilla en React para un problema que son cuarenta líneas de Python. Además hay que mirar su
  licencia antes de usarlos con ánimo comercial.
- **Rótulos quemados frente a los del editor de TikTok.** Se hacen las dos cosas. Los rótulos
  quemados sobreviven a que alguien descargue el vídeo y lo vuelva a subir, y sostienen el ritmo.
  El texto nativo de TikTok, encima, lo lee el buscador de la propia app.

## Zonas seguras de TikTok

Sobre 1080x1920, la interfaz de TikTok tapa unos 420 px abajo con la descripción y los botones, y
unos 220 px a la derecha con la columna de acciones. En `quilt_video.py`: `MARGEN_ARRIBA = 200`,
`MARGEN_ABAJO = 520`, `MARGEN_LADO = 70`. Un rótulo fuera de esa caja lo pisa la interfaz y no se
lee, y un `y` mayor de 1400 cae debajo aunque `pos` no lo ponga ahí.

## Puntos fuertes de la app, ordenados para vídeo

Sale de barrer `SPEC.md`, el código de interfaz, las superficies de sistema y la competencia del
nicho. Marcado **dif.** lo que los rastreadores de referencia (HabitKit, Habitify, Loop, Habitica,
Streaks, Finch) no tienen.

| # | Clip | Por qué | Dónde se graba |
|---|---|---|---|
| 1 | Un toque marca el día, ficha con racha y cumplimiento | Ya hecho. Es la promesa base | Android |
| 2 | Pulsación larga: día saltado que no rompe la racha | **dif.** La ansiedad de racha es la queja número uno del nicho. Toca la culpa | Android |
| 3 | Baldosa de ajustes rápidos: bajar la persiana y marcar | **dif.** Ninguna app del nicho ocupa esa superficie. Gesto que nadie espera | Android |
| 4 | Marcar desde el widget sin que la app se abra | **dif.** Contradice lo que el espectador cree que es un widget | Android |
| 5 | Borro la app, la reinstalo y mi año vuelve | **dif.** El miedo del nicho es el secuestro de datos y los precios que se van a suscripción | Android |
| 6 | Objetivo semanal: tres veces por semana, los días que sean | **dif.** Mata la objeción que todo el mundo piensa mientras mira | Android |
| 7 | La tarjeta para compartir cambia: semana, mes, año | **dif.** Termina en una imagen que el espectador quiere para sí | Android |
| 8 | Contador parcial: el cuadrito se llena como un vaso | **dif.** Animación satisfactoria, y se ve igual en el widget | Android |
| 9 | Modo avión: la app entera sin cuenta y sin red | **dif.** Habitify y Habitica exigen cuenta, Finch vive de la nube | Android |
| 10 | El widget del año entero en la pantalla de inicio | 365 cuadritos junto al reloj. Envidia de pantalla de inicio | Android |
| 11 | Widget de pantalla de bloqueo: cuenta pero no delata | **dif.** Los widgets de pantalla de bloqueo son un género propio en TikTok | iOS |
| 12 | Oye Siri, marca meditar | **dif.** Cero toques. Ninguna app del nicho marca por voz | iOS |
| 13 | El mismo widget en iPhone y Android a la vez | **dif.** La paridad es el argumento, no el widget | Los dos |
| 14 | Recordatorio que se calla si ya lo hiciste | **dif.** Plano corto, la frase se escribe sola | Android |
| 15 | Pago único, y nunca una suscripción | **dif.** En un feed lleno de quejas por suscripciones, esa pantalla es el gancho | Android |

El 11 y el 12 necesitan simulador de iOS. El 13 necesita los dos y montaje a pantalla partida.

Dos de la lista no se pueden guionizar por adb: **no existe `cmd appwidget`**, así que un widget
no se puede colocar en la pantalla de inicio desde la línea de comandos. Los clips de widget hay
que grabarlos a mano o cambiarlos por otra cosa. Es lo que pasó con el 4 y el 10.

## Los diez que están montados

En `tools/video/salida`. Todos 1080x1920 a 30 fps, mudos, entre 16,9 s y 21,5 s.

| Clip | Qué enseña | Datos | Notas de grabación |
|---|---|---|---|
| `01-un-toque` | Marcar el día y la ficha del hábito | `habits.json` | |
| `02-dia-saltado` | Pulsación larga: la racha pasa de 5 a 20 | `habits.json` | |
| `03-objetivo-semanal` | Tres veces por semana, y dónde se cambia | `habits.json` | Termina con la hoja de editar abierta a propósito |
| `04-contador` | Ocho vasos, el cuadrito se va llenando | `agua.json` | |
| `05-compartir` | La tarjeta: semana y año | `habits.json` | Sin el paso "Mes": el día 1 sale casi vacía |
| `06-sin-cuenta` | Ajustes: sin cuenta, sin nube, con exportación | `habits.json` | |
| `07-baldosa` | Bajar la persiana y marcar desde la baldosa | `habits.json` | |
| `08-crear-habito` | Crear un hábito de cero | `dos.json`, `MAX_SEG=36` | El color se elige entre los cuatro primeros: del quinto en adelante son de Pro y abren la hoja de compra |
| `09-modo-oscuro` | El mismo año de día y de noche | `habits.json` | |
| `10-idiomas` | Español, inglés, portugués, alemán, francés | `habits.json`, `MAX_SEG=34` | Cinco segundos por idioma: los dos primeros son pantalla de arranque y la receta los recorta |

## Estado

- [x] Cadena de producción entera, de emulador a mp4 listo para subir
- [x] Datos de demostración con rachas creíbles, generados con `demo/generar.py`
- [x] Estado de partida determinista: datos, compras, red, rotación, tema, idioma y teclado
- [x] Los diez clips de Android grabados, montados y revisados fotograma a fotograma
- [ ] Clips 11 y 12 en simulador de iOS
- [ ] Clips de widget: a mano, porque `cmd appwidget` no existe
- [ ] Voz en off con `edge-tts` y subtítulos con `faster-whisper`, si hace falta
- [ ] Subirlos, con sonido en tendencia puesto desde el editor de TikTok

## Reglas de la copia

Sin guiones largos y sin emoji, en los rótulos, en las descripciones y en los comentarios. Vale
para todo lo que se publique bajo el nombre de Quilt.

Los vídeos salen mudos. Hay que ponerles un sonido en tendencia desde el editor de TikTok: la
plataforma penaliza el vídeo sin audio.
