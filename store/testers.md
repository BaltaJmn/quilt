# Conseguir los 12 probadores

El plazo más largo de todo el lanzamiento. Catorce días seguidos que no se pueden acelerar con
dinero ni con prisa, así que arráncalo antes que cualquier otra cosa de la lista.

## La regla, exacta

Cuentas de desarrollador personales creadas después del **13/11/2023** necesitan un test cerrado
con **12 probadores durante 14 días continuos** antes de poder pedir acceso a producción. Bajó de
20 a 12 en diciembre de 2024. Las cuentas de empresa y las personales anteriores a esa fecha están
exentas.

Lo que de verdad importa de esa frase:

- **Continuos.** No son 14 días sueltos. Si el día 7 se cae uno y bajas de 12, el contador vuelve a
  cero. No es una advertencia teórica: es el motivo por el que la gente tarda dos meses.
- **El reloj arranca tarde.** Empieza cuando la release está aprobada *y* hay 12 aceptaciones, no
  cuando tú creas el test.
- **Cuentas de Google distintas, dispositivos reales.** Emuladores y cuentas duplicadas no cuentan.
- **Google mira el uso, no solo la instalación.** Doce personas que instalan y no abren nunca es el
  patrón que hace que rechacen la solicitud.

Por eso se recluta a **16, no a 12**. Dos o tres se caen siempre: cambian de móvil, desinstalan,
ignoran el mensaje. El colchón es lo que evita reiniciar el contador.

## Por dónde empezar, en orden

**1. Tu gente.** Es la fuente buena y casi nadie la explota bien. Familia, amigos, compañeros,
el grupo de la comida. Dieciséis personas con Android es menos de lo que parece si preguntas
directamente en vez de poner un mensaje en un grupo y esperar.

Pide el correo **de la cuenta de Google del móvil**, que a menudo no es el que usan a diario. Es el
fallo número uno: aceptan con un correo y tienen el móvil con otro, y no les aparece la app.

**2. Comunidades de testeo recíproco.** Tú pruebas la suya, ellos la tuya. Gratis y funciona, pero
la rotación es alta y son justo los que se caen el día 9. Sirven para rellenar los últimos huecos,
no como base.

- r/AndroidClosedTesting, r/androidtesting y r/androiddev en Reddit
- Grupos de Telegram y Discord de `closed testing` / `12 testers`

**3. Servicios de pago.** Existen, cobran entre 30 y 100 €, y salen todos en la primera página de
Google cuando buscas esto — de hecho casi todo lo que se lee sobre el tema son sus blogs. Míralo
con cuidado: son cuentas alquiladas, y ese es exactamente el patrón que las comprobaciones de uso
real de Google buscan. Si uno de esos servicios usa granjas de cuentas, el riesgo no lo corre el
servicio, lo corres tú con tu cuenta de desarrollador. No lo recomiendo mientras la opción 1 no
esté agotada de verdad.

**Lo que no se hace nunca:** crear tus propias cuentas de Google para llegar a 12. Es la vía rápida
a que te cierren la cuenta de desarrollador, y son 25 $ y todo el trabajo hecho hasta aquí.

## Móntalo con un Grupo de Google

En Play Console puedes pegar correos sueltos o apuntar a un Grupo de Google. Usa el grupo:

- Añadir o quitar gente se hace en el grupo, sin tocar Play Console ni publicar release nueva.
- No expones la lista de correos de tus amigos dentro de la consola.

`groups.google.com` → crear grupo, por ejemplo `quilt-testers`. En Play Console:
*Pruebas → Pruebas cerradas → Probadores → Grupos de Google*, y pegas la dirección del grupo.

## Quilt juega a favor aquí

El requisito de "que la usen de verdad" es un problema para una calculadora de propinas. Para un
seguidor de hábitos no: la app pide que la abras a diario, que es literalmente lo que Google quiere
ver. El test cerrado y el producto quieren la misma cosa.

Aprovéchalo: pídeles que creen **un hábito real suyo** el primer día, no uno de prueba. Un hábito
inventado se abandona el martes; uno real aguanta los 14 días solo.

## Mensaje para mandar

> Estoy publicando mi app, **Quilt**. Es un seguidor de hábitos: un año entero en una cuadrícula,
> sin cuentas, sin anuncios y sin recoger nada tuyo.
>
> Google me pide 12 personas probándola 14 días seguidos antes de dejarme publicarla, y ahí es
> donde necesito una mano.
>
> Son dos minutos: aceptas por un enlace, la instalas desde Play y marcas tu hábito cada día. Con
> abrirla un momento al día vale.
>
> Solo necesito el correo de la cuenta de Google que tengas en el móvil (el del Play Store, que a
> veces no es el de siempre). Te paso el enlace y listo.
>
> Lo único importante: **no la desinstales hasta que te avise**, aunque no la uses. Si alguien se
> sale, el contador de los 14 días se me reinicia desde cero.

## Instrucciones para el probador

1. Abre el enlace de invitación y pulsa *Become a tester* / *Convertirme en probador*.
2. Desde esa misma página, *Download it on Google Play*, y la instalas.
3. Crea un hábito tuyo de verdad y márcalo cada día.
4. No la desinstales ni salgas del programa hasta que te avisen.

Si les dice que la app no está disponible: casi siempre es que aceptaron con un correo y tienen el
móvil con otro. Que comprueben la cuenta en Play Store → foto de perfil.

## Durante los 14 días

- Mira el recuento de probadores cada pocos días. Si baja de 12, hay que reponer antes de que se
  note.
- Un aviso a mitad de camino, sin agobiar. La gente lo desinstala por olvido, no por molestia.
- Puedes subir versiones nuevas durante el test. No reinicia el contador, y arregla lo que te
  reporten.
- Al terminar, *Acceso a producción → Solicitar acceso*. La revisión suele tardar unos días más.

## Post para Reddit

En inglés: esos subs lo son. Está escrito para leerse como un dev pidiendo ayuda, no como un
anuncio — en Reddit lo segundo se hunde en downvotes o lo borra un moderador.

**Antes de publicarlo hace falta el Grupo de Google.** Los dos enlaces de abajo no le sirven a un
desconocido: en una prueba **cerrada**, quien no está en la lista de probadores ve "no disponible".
El grupo es lo que deja que se apunten solos, sin que tú vayas recogiendo correos por DM. Créalo y
sustituye `<GRUPO>` por su dirección (`quilt-testers@googlegroups.com` o como lo llames).

Sin grupo, cambia el paso 1 por: *"comment or DM me the Gmail your phone uses and I'll add you"*.
Funciona, pero te obliga a estar pendiente y añade horas de retraso a cada persona.

### Título (elige uno)

```
[Closed testing] Quilt — a habit tracker that shows your whole year as a grid. I'll test yours back.
```

```
Need 12 testers for 14 days (Quilt, habit tracker). Reciprocal — drop your link and I'll join yours today.
```

### Cuerpo

```markdown
Same wall everyone hits: 12 testers, 14 continuous days, before Google unlocks production.

**Reciprocal** — drop your link in the comments and I'll opt into yours the same day and stay
installed the full 14. I won't uninstall on you.

**What it is:** Quilt shows your whole year as a grid — one square per day, one grid per habit.
Tap a square to mark the day. It's built for looking back at a year rather than clearing a
to-do list.

- Interactive home screen widgets in three sizes. One tap marks the day.
- Rest days: long-press a day to skip it. Holidays and sick days don't break the streak.
- Counted habits — 8 glasses, 3 sets, whatever you count.
- Per-habit reminders, only on the days you choose.
- A share card for your year.
- No account, no ads, no analytics, no tracking. Everything stays on the device:
  https://quilt.baltajmn.dev/

Android 7.0+. English, Spanish, Portuguese, German, French. There's a paid tier, but there is
nothing you need to buy to test it.

**To join:**

1. Join the group: <GRUPO>
2. Opt in: https://play.google.com/apps/testing/com.baltajmn.habit
3. Install: https://play.google.com/store/apps/details?id=com.baltajmn.habit

Step 1 isn't optional — the closed test can't see you otherwise. Use the Google account your
phone actually signs into Play with. That mismatch is the usual reason people get
"item not available".

**What actually helps:** set up a real habit of yours, not a test one, and mark it daily. And
please don't uninstall for 14 days even if you lose interest — if the count drops below 12 for a
single day, my clock resets to zero.

Feedback very welcome, especially on the widgets.
```

### Respuesta para los comentarios

```markdown
Joined yours and installed — I'm in for the full 14 days. Mine's here if you're up for it:
https://play.google.com/apps/testing/com.baltajmn.habit (group first: <GRUPO>)
```

### Al publicar

- **Lee las reglas del sub primero.** `r/androiddev` no admite estos posts en el feed: van a un
  hilo fijado. Publicarlo suelto es que te lo borren y te ganes una marca.
- **No pegues el mismo texto en cinco sitios el mismo día.** Reddit lo detecta como spam y te
  puede caer un shadowban, que es peor que no publicar: sigues viendo tus posts y nadie más.
  Reescribe la primera frase en cada uno y espárcelo en varios días.
- **Contesta a todo el que comente.** Es la mitad del trato; el que no recibe respuesta se
  desinstala.
- **Apunta a quién aceptaste tú.** Vas a tener 15 apps ajenas en el móvil 14 días, y desinstalar
  antes de tiempo le rompe el contador a otro igual que a ti.
- Los DM que te vendan "12 testers garantizados" aparecen a los minutos. Ignóralos.
