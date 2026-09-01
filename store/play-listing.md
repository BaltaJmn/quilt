# Ficha de Google Play

Por qué cada texto de la ficha dice lo que dice. **El texto pegable no está aquí**, está en
[`store/listings/<idioma>/`](listings), un fichero por campo, porque de ahí lo lee también el script
que los sube. Este documento guarda el razonamiento, que es lo que un directorio de ficheros sueltos
no puede guardar.

Los saltos de línea de esos ficheros son los que Play va a mostrar. Cada párrafo viaja en una sola
línea larga a propósito: la descripción larga conserva los saltos tal cual, así que un texto cortado
a 100 columnas para que se lea en el editor sale con las frases partidas por la mitad en el móvil.

**La app está traducida a cinco idiomas** (inglés, español, portugués, alemán y francés), así que
la ficha debería ir en los cinco: en Play cada idioma es una ficha independiente y se indexa por
separado, y rellenarlas es gratis. Están los cinco escritos.
**Idioma predeterminado de la ficha: inglés (en-US).** No es donde se publica, es a qué idioma cae
un usuario cuyo idioma no hayas traducido. Con el inglés de base, los cinco mercados traducidos se
añaden encima y el resto del mundo ve algo legible.

---

## El nombre: Quilt

Elegido y aplicado en el código. Una colcha de retales es literalmente lo que dibuja la app: un
tablero de cuadrados de colores que se cose de uno en uno. Da nombre, icono, metáfora y el argumento
de toda la ficha.

Comprobado que no hay ningún tracker de hábitos así en ninguna de las dos tiendas (lo más cercano es
*QuiltTracker V2*, logística de almacén). **Antes de gastar dinero en marca, mira el registro de la
clase 9 en OEPM o EUIPO.**

El identificador del paquete sigue siendo `com.baltajmn.habit` y no se toca: es irreversible tras la
primera subida, y el nombre visible en la tienda es independiente.

## Textos en español

### Título (máx. 30 caracteres)
[`store/listings/es-ES/title.txt`](listings/es-ES/title.txt)
Veintinueve caracteres. Cambiado desde *Quilt: hábitos y rachas*, que gastaba solo 23 de los 30 y se
dejaba fuera la frase que la gente teclea de verdad en español: *seguimiento de hábitos*. «Rachas» se
pierde del título, pero sigue en la descripción corta y en la larga, que también se indexan.

### Descripción corta (máx. 80 caracteres)
[`store/listings/es-ES/short.txt`](listings/es-ES/short.txt)
La anterior se pasaba: 81 caracteres, uno por encima del tope, así que Play no la aceptaba tal cual.
Y gastaba el espacio en «retal», que no lo busca nadie, y en la privacidad, que se vende dentro de la
ficha pero no se teclea en el buscador. Esta línea es lo único que se lee debajo del título en los
resultados, así que las primeras cinco palabras son el argumento de por qué esta y no otra.

### Descripción larga (máx. 4000 caracteres)
[`store/listings/es-ES/full.txt`](listings/es-ES/full.txt)

---

## Textos en inglés

### Título (máx. 30)
[`store/listings/en-US/title.txt`](listings/en-US/title.txt)
Treinta caracteres justos. El título es lo que más pesa en la búsqueda de Play, así que lleva la
marca y el término que la gente teclea de verdad (*habit tracker*), no un sinónimo bonito.

**Se queda como está.** Usa los 30 de 30, empieza por la marca y contiene el término de categoría,
que es lo innegociable. La palabra más floja es *Streaks*, pero ninguna sustituta sale gratis:
*Widget* haría pensar que la app es solo un widget, y *Year* se lee como si los hábitos fueran
anuales. La diferenciación cabe mejor en la descripción corta, que tiene 80 caracteres en vez de
tres de sobra. Además Play manda a revisión cualquier cambio de nombre y reinicia parte del
histórico de posiciones: no se toca sin una razón mejor que un sinónimo.

### Descripción corta (máx. 80)
[`store/listings/en-US/short.txt`](listings/en-US/short.txt)
Setenta y tres caracteres. La anterior gastaba el hueco en *patch*, que es la metáfora de la marca y
no la busca nadie. Esta abre con el contraste contra toda la competencia (ellos enseñan el día, esta
el año) y de paso mete tres términos que el título no lleva: *grid*, *widget* y *account*.

### Descripción larga (máx. 4000)
[`store/listings/en-US/full.txt`](listings/en-US/full.txt)

---

## Notas de la versión 1.1

Play las pide en un solo campo, con una etiqueta por idioma. El tope de **500 caracteres es por
idioma**, no del bloque entero, y las etiquetas no cuentan. Los códigos tienen que coincidir con los
idiomas dados de alta en la ficha, o Play rechaza el bloque.

Pegable tal cual. También está suelto en `store/release-notes-1.1.txt`:

```
<en-US>
First release.

• Your whole year on one screen: one colour grid per habit, 365 squares.
• Interactive home-screen widgets in three sizes. One tap marks the day.
• Rest days: hold a day to skip it. Holidays and illness never break your streak.
• Counted habits: 8 glasses, 3 sets, whatever you count.
• Reminders per habit, only on the days you pick.
• Everything stays on your phone. No account, no cloud.
• Export and import your data, plus CSV for spreadsheets.
</en-US>
<es-ES>
Primera versión.

• Tu año entero en una pantalla: una cuadrícula de color por hábito, 365 cuadritos.
• Widgets interactivos en tres tamaños. Un toque marca el día.
• Días de descanso: mantén pulsado un día para saltarlo. Vacaciones o enfermedad no rompen la racha.
• Hábitos de cantidad: 8 vasos, 3 series, lo que cuentes.
• Recordatorios por hábito, solo los días que elijas.
• Todo se queda en tu móvil. Sin cuenta y sin nube.
• Exporta e importa tus datos, y CSV para hojas de cálculo.
</es-ES>
<pt-BR>
Primeira versão.

• O ano inteiro em uma tela: uma grade colorida por hábito, 365 quadradinhos.
• Widgets interativos em três tamanhos. Um toque marca o dia.
• Dias de descanso: segure um dia para pulá-lo. Férias e doença não quebram a sequência.
• Hábitos com contagem: 8 copos, 3 séries, o que você contar.
• Lembretes por hábito, só nos dias que você escolher.
• Tudo fica no seu celular. Sem conta e sem nuvem.
• Exporte e importe seus dados, e CSV para planilhas.
</pt-BR>
<de-DE>
Erste Version.

• Dein ganzes Jahr auf einem Bildschirm: ein Farbraster pro Gewohnheit, 365 Kästchen.
• Interaktive Homescreen-Widgets in drei Größen. Ein Tippen hakt den Tag ab.
• Ruhetage: Tag lange drücken zum Überspringen. Urlaub und Krankheit brechen die Serie nicht.
• Gewohnheiten mit Zähler: 8 Gläser, 3 Sätze, was du zählst.
• Erinnerungen pro Gewohnheit, nur an gewählten Tagen.
• Alles bleibt auf deinem Gerät. Kein Konto, keine Cloud.
• Export und Import deiner Daten, plus CSV.
</de-DE>
<fr-FR>
Première version.

• Toute votre année sur un écran : une grille de couleur par habitude, 365 carrés.
• Widgets interactifs en trois tailles. Un appui coche la journée.
• Jours de repos : appui long sur un jour. Vacances et maladie ne cassent pas la série.
• Habitudes à compter : 8 verres, 3 séries, ce que vous voulez.
• Rappels par habitude, seulement les jours choisis.
• Tout reste sur votre téléphone. Aucun compte, aucun cloud.
• Export et import de vos données, et CSV pour les tableurs.
</fr-FR>
```

Longitudes: en-US 464/500, es-ES 489/500, pt-BR 468/500, de-DE 490/500, fr-FR 495/500.

Nada de "correcciones de errores" en una 1.0: aquí lo que toca es contarle a alguien que no ha visto
nunca la app qué hace.

## Datos obligatorios de la ficha

| Campo | Valor |
|---|---|
| Nombre del paquete | `com.baltajmn.habit` |
| Categoría | **Productividad**. La app es agnóstica al hábito (leer, idiomas, agua, correr): Salud y fitness promete métricas corporales que no existen, y Estilo de vida no da grupo de comparación. Reversible en cualquier momento desde la ficha. |
| Etiquetas | **Productividad**, **Autoayuda**, **Monitores de actividad**. La lista de Play es cerrada, no se escriben a mano. Tres buenas antes que cinco: cada etiqueta elige contra qué apps te comparan. Fuera Entrenamiento, Meditación, Sueño y Dietas, que te ponen a competir con Strava y Calm. |
| Correo de contacto | El tuyo, público en la ficha |
| Política de privacidad | **https://quilt.baltajmn.dev/** |
| Anuncios | **No contiene anuncios** |
| Compras en la aplicación | **Sí**, un producto, pago único |
| Clasificación de contenido | Cuestionario IARC: sin violencia, sin sexo, sin contenido de usuario, sin apuestas, sin datos compartidos → sale **PEGI 3 / Everyone** |
| Público objetivo | 13+ (evita el régimen de Families, que trae requisitos extra) |
| Aplicación gubernamental | No |
| Aplicación financiera | No |

## Recursos gráficos

| Recurso | Requisito | Estado |
|---|---|---|
| Icono | 512×512 PNG de 32 bits, sin transparencia | Se genera con `tools/generate_icons.py` |
| Gráfico de funciones | 1024×500 PNG o JPG. **Obligatorio** | Hecho: `store/graphics/feature-es.png`, `feature-en.png` |
| Capturas de teléfono | Mín. 2, máx. 8. Lado corto ≥ 320 px, lado largo ≤ 3840 px | Hechas a 1080×2400: `store/screenshots/es/`, `store/screenshots/en/` |
| Capturas de tableta | Solo si declaras soporte de tableta | No hace falta |
| Vídeo | URL de YouTube, opcional | Grabado: `store/graphics/quilt-flow-es.mp4`, `quilt-flow-en.mp4`. Falta subirlo a YouTube |

### Capturas, en este orden

Cuentan la historia sin leer una palabra. El nombre del fichero es el orden de subida.

| # | Fichero (es / en) | Qué vende |
|---|---|---|
| 1 | `01-hoy` / `01-today` | La pantalla principal: seis hábitos, cuadrículas llenas, "3 de 5 hechos hoy" |
| 2 | `02-detalle` / `02-detail` | El año entero de un hábito, las cuatro estadísticas y el recordatorio |
| 3 | `03-compartir` / `03-share` | La imagen del año lista para publicar, con la firma "Quilt · un retal al día" |
| 4 | `04-ajustes` / `04-settings` | Reordenar, exportar, importar, CSV: "tus datos son tuyos" |
| 5 | `05-editar` / `05-edit` | El editor: emoji, color, días, veces al día, recordatorio |
| 6 | `06-recordatorio-picker` (solo es) | El reloj del recordatorio |
| 7 | `07-widgets` | Los dos widgets sobre la pantalla de inicio, marcables de un toque |

Las capturas se sacan con el emulador en modo demo de SystemUI (`sysui_demo_allowed`), que fija
la hora a las 9:30, la batería al 100 % y esconde los iconos de notificación. Sin eso cada tanda
sale con una barra de estado distinta.

### El vídeo

44 segundos, sin audio, grabado con `adb shell screenrecord --size 1080x2400`. El recorrido es:
marcar tres hábitos, subir el contador de agua a 5/8, abrir el detalle con el año entero, pasar
por Año/Mes/Semana en compartir y terminar marcando desde el widget en la pantalla de inicio.
Play solo acepta el vídeo como enlace de YouTube, así que hay que subirlo primero (sin listar).

## Data Safety y clasificación de contenido

Los dos son formularios de la Console y **no tienen API**: `androidpublisher` expone ediciones,
canales, fichas y productos, nada más. Hay que rellenarlos a mano. Esto de aquí abajo es la hoja de
respuestas, contrastada contra el código, para que rellenarlos sea mecánico y no una interpretación.

### Lo que el código dice de verdad

Comprobado, no supuesto:

| Comprobación | Resultado |
|---|---|
| Llamadas de red en código propio | Ninguna. La única URL es la política de privacidad, y la abre el navegador ([`Pro.kt:195`](../shared/src/commonMain/kotlin/com/baltajmn/habit/ui/Pro.kt#L195)) |
| Analítica, crash reporting, publicidad | Ninguna. Ni Firebase, ni Crashlytics, ni AdMob, ni nada |
| Permisos del manifiesto fusionado | `INTERNET`, `ACCESS_NETWORK_STATE`, `BILLING`, `FOREGROUND_SERVICE`, `WAKE_LOCK`, `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED` |
| `com.google.android.gms.permission.AD_ID` | **Ausente.** Sin identificador de publicidad no hay nada que declarar en seguimiento |
| Cuándo habla con RevenueCat | En **cada arranque**, no solo al comprar |

Esa última fila es la que decide una respuesta del formulario. `App.kt:27` llama a
`Billing.configure()` sin condición y `Billing.refresh()` pide `awaitCustomerInfo()` acto seguido,
así que el SDK crea su identificador anónimo y llama a su API también para quien usa la app gratis y
no compra nunca. La recogida es **obligatoria**, no opcional. Decir lo contrario en el formulario
sería falso.

### Data Safety, respuestas

**¿Recoge o comparte datos de usuario?** Sí.

Dos tipos, y solo dos. Los dos con las mismas respuestas:

| Tipo | Dónde está en el formulario |
|---|---|
| Historial de compras | *Compras y transacciones financieras* |
| Identificadores de dispositivo u otros | *Identificadores* |

| Pregunta | Respuesta |
|---|---|
| ¿Se recoge? | Sí |
| ¿Se comparte? | **No** |
| ¿Es obligatoria la recogida? | Sí |
| Finalidad | Solo *funciones de la aplicación* |
| ¿Vinculado a la identidad del usuario? | No |
| ¿Se usa para seguimiento entre aplicaciones? | No |

**El "no" de compartir tiene truco y es el error clásico.** Play define *compartir* como transferir
a un tercero, y excluye expresamente al proveedor de servicios que procesa por cuenta tuya.
RevenueCat es eso, un encargado del tratamiento, así que la respuesta correcta es *no se comparte*.
El formulario no acepta matices: es un sí o un no, y el matiz va en la política de privacidad.

Todo lo demás va a **no**, y conviene marcarlo de una pasada para que no quede nada a medias:
ubicación, información personal, información financiera de pago, salud y fitness, mensajes, fotos y
vídeos, audio, archivos y documentos, calendario, contactos, actividad en aplicaciones, búsquedas
web, e información y rendimiento de la aplicación.

Los hábitos, el historial, las rachas y las notas **no se declaran porque no se recogen**: viven en
un fichero del dispositivo y no salen de ahí.

Sección de seguridad:

| Pregunta | Respuesta |
|---|---|
| ¿Se cifran los datos en tránsito? | Sí |
| ¿Puede el usuario pedir que se borren? | Sí, por el correo de contacto |
| ¿Revisión de seguridad independiente? | No |

Antes de enviar, contrasta los dos tipos con la guía de Data Safety que publica el propio
RevenueCat: son ellos los que saben qué campos manda su SDK, y si algún día se activa alguna de sus
integraciones de analítica esta tabla se queda corta.

### Clasificación de contenido (IARC)

Categoría del cuestionario: **utilidad, productividad, comunicación u otros**. No es un juego, y
elegir *juego* mete un cuestionario distinto y más largo.

Todo lo que pregunta va a **no**: violencia, sexo, lenguaje, sustancias, apuestas, miedo, contenido
generado por usuarios, interacción entre usuarios, compartir ubicación y compartir datos personales.
La única que va a **sí** es la de **compras digitales**, que no sube la clasificación pero es
obligatoria declararla.

Resultado esperado: **PEGI 3, ESRB Everyone, USK 0**.

**Público objetivo: 13 años o más**, y en la pregunta de si la app atrae a menores, no. Marcar una
franja infantil activa el régimen de Families, que trae requisitos extra que esta app no necesita.

---

## Portugués, alemán y francés

Los tres son traducción de la ficha española, no de la inglesa, porque la española es la que se
escribió primero y la que tiene la sección *NO VAS A ENCONTRAR AQUÍ*, que a la inglesa le falta.

El título es lo único que no se traduce literal, porque en cada idioma la gente teclea otra cosa:

| Idioma | Título | Por qué |
|---|---|---|
| `pt-BR` | Quilt: rastreador de hábitos (28) | *rastreador de hábitos* es la forma corriente en Brasil. *Sequência* se queda para la descripción corta. |
| `de-DE` | Quilt: Gewohnheiten Tracker (27) | El compuesto correcto sería *Gewohnheitstracker*, pero pegado no casa con quien busca *Gewohnheiten* suelto. Separado cubre las dos, y es como lo escriben las demás fichas alemanas. |
| `fr-FR` | Quilt : suivi d'habitudes (25) | *suivi d'habitudes* es la expresión de categoría. *Traqueur* existe pero suena a traducción automática. |

Las descripciones cortas repiten el patrón de la española: primero el contraste que justifica la app
(el año entero frente al día de hoy) y después los términos que el título no lleva.

## Subirlas todas de una vez

Play Console no tiene importación masiva, pero la API sí. `.github/workflows/listings.yml`, ejecución
manual desde *Actions*, manda los cinco idiomas en una sola edición. Es atómica: o entran los cinco o
no entra ninguno.

En un push a `store/listings/**` el mismo workflow solo comprueba los límites, no escribe en Play.

```bash
python3 tools/play-listing/subir.py          # comprueba los topes, no toca Play
```

Dos cosas antes de la primera ejecución:

- La cuenta de servicio necesita el permiso de **ficha de Play Store** en *Users and permissions*. La
  que ya existe se dio de alta para publicar versiones, y ese permiso no incluye editar la ficha.
- Cambiar el **título** manda la ficha a revisión de Play. Los otros dos campos entran solos.
