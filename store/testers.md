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

**2. Comunidades donde está tu público, no otros desarrolladores.**

`r/androiddev` prohíbe estos posts, y da una razón que conviene leer entera:

> Google associate Play Store accounts in mysterious ways, but it looks like one of the way they
> can associate accounts is the testing. Therefore if you look for testers amongst other developers
> you put your Play Account at risk of termination by association.

Es decir: los grupos de intercambio (`r/AndroidClosedTesting`, los Telegram de "12 testers", "yo
pruebo la tuya y tú la mía") no solo dan probadores malos que se caen el día 9. Te asocian la
cuenta con decenas de cuentas de desarrollador desconocidas, y si alguna de ellas acaba baneada,
el parecido lo pagas tú. Son 25 $ y todo el trabajo hecho hasta aquí.

**No lo hagas.** Ni recíproco, ni grupos de intercambio, ni servicios de pago (que son lo mismo
con factura).

Lo que sí, en dos niveles:

*Sitios donde pedir probadores es el tema del sub:*

- **r/alphaandbetausers** (~88k). Existe para esto. Usa el flair `[Beta]`.
- **r/androidapps**. Permite publicar tu propia app si sigues el formato y dices que eres el autor.
  Son usuarios de Android, no desarrolladores.

*Sitios donde está la gente que quiere un seguidor de hábitos:*

- **r/theXeffect**, seguimiento de hábitos con cuadrículas. Es literalmente Quilt en papel.
- **r/QuantifiedSelf**, gente que registra su vida en datos. El argumento del año entero les toca.
- **r/getdisciplined**, **r/decidingtobebetter**, **r/selfimprovement**, **r/habits**
- **r/productivity**
- **r/bulletjournal**, trackers analógicos, público exacto aunque prefieran el papel
- **r/degoogle** y comunidades de privacidad, por el "sin cuenta, sin nube, sin analítica"

En este segundo grupo casi todos prohíben la autopromoción por defecto. **Escribe al moderador
antes**, no publiques y reces. Un mensaje corto diciendo qué es y que buscas probadores suele
bastar, y te evita el borrado y la marca en el historial.

**Lo que no se hace nunca:** crear tus propias cuentas de Google para llegar a 12. Es la vía más
rápida a que te cierren la cuenta.

## Móntalo con un Grupo de Google

En Play Console puedes pegar correos sueltos o apuntar a un Grupo de Google. Usa el grupo:

- Añadir o quitar gente se hace en el grupo, sin tocar Play Console ni publicar release nueva.
- No expones la lista de correos de tus amigos dentro de la consola.

### Crearlo

1. [groups.google.com](https://groups.google.com) → **Crear grupo**.
2. Nombre `Quilt testers`, correo `quilt-testers`. Queda como
   `quilt-testers@googlegroups.com`.
3. Ajustes de privacidad, que es donde se decide si esto funciona o no:

| Ajuste | Valor | Por qué |
|---|---|---|
| Quién puede unirse | **Cualquiera puede pedir unirse** | Tú apruebas. Un bot que entra solo ocupa una plaza sin usar la app, y Google mira el uso real |
| Quién puede publicar | **Solo los administradores** | Si no, cualquiera puede escribir a los doce y el grupo acaba siendo un relé de spam |
| Quién puede ver los miembros | **Solo administradores** | No expones los correos de tus amigos a desconocidos que se apunten por Reddit |
| Quién puede ver las conversaciones | **Solo administradores** | El grupo es una lista de acceso, no un foro |

4. Play Console → *Probar y publicar → Pruebas → Pruebas cerradas → **Probadores*** →
   **Grupos de Google**, y pega `quilt-testers@googlegroups.com`.

**Aprueba las solicitudes a mano, en tandas.** Vas a estar contestando comentarios de todas formas.
Y así sabes a quién chasqueas el día 7 cuando alguien desinstale.

Si prefieres cero fricción, pon *Cualquiera puede unirse* y te ahorras aprobar. Convierte mejor en
Reddit, pero te llenas de cuentas que no abren la app, que es justo el patrón por el que Google
rechaza la solicitud de producción.

**El correo tiene que ser el de la cuenta de Google del móvil.** Alguien que se une al grupo con un
correo y tiene el Play Store con otro sigue viendo "no disponible". Es el fallo número uno y no da
ningún mensaje de error útil.

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

En inglés: esos subs lo son. **Nada de intercambio recíproco**, por lo que dice `r/androiddev` más
arriba. Hay dos versiones porque son dos públicos distintos, y el mismo texto en los dos sitios
funciona mal en ambos.

### Versión A, para r/alphaandbetausers y r/androidapps

Ahí buscar probadores es el tema, así que se puede decir de frente.

**Título:**

```
[Beta] Quilt: a habit tracker that shows your whole year as one grid. No account, no ads.
```

**Cuerpo:**

```markdown
I built Quilt because every habit app I tried showed me this week. I wanted to see the year.

One square per day, one grid per habit, twelve months on screen at once. You can see the month
you slipped and the seven straight weeks you didn't, in the same glance. That view is the whole
point of the app.

- Interactive home screen widgets in three sizes. One tap marks the day, no need to open the app.
- Rest days: long-press a day to skip it. Being ill or away doesn't break the streak or sink
  your completion rate.
- Counted habits: 8 glasses, 3 sets, 30 minutes, whatever you count.
- Per-habit reminders, only on the days you scheduled.
- A share card for your week, month or year.
- No account, no sign-up, no ads, no analytics. Everything is a file on your phone that you can
  export and take with you: https://quilt.baltajmn.dev/

Android 7.0+. English, Spanish, Portuguese, German and French. There is a paid tier, but nothing
you need to buy to use it or to test it.

It is in closed testing, so joining takes two steps:

1. Join the group: quilt-testers@googlegroups.com
2. Opt in: https://play.google.com/apps/testing/com.baltajmn.habit

Step 1 isn't optional, the test can't see you otherwise. Use the Google account your phone
actually signs into Play with. That mismatch is why people get "item not available".

I'd rather hear that something is wrong than be polite about it. The widgets are the part I am
least sure about.
```

### Versión B, para r/theXeffect, r/QuantifiedSelf, r/getdisciplined y similares

Ahí eres una persona enseñando algo que ha hecho, no un anuncio. **Pide permiso al moderador
antes.** Y si el sub tiene hilo semanal de autopromoción, va ahí y no al feed.

**Título:**

```
I got tired of habit apps that only show you this week, so I built one that shows the whole year
```

**Cuerpo:**

```markdown
I've been tracking habits on paper for years, in a grid, one box a day. Every app I tried
replaced that with a checklist for today and a streak number, and I always went back to paper.

So I built the paper version. One square per day, one grid per habit, the twelve months on
screen at once. Tap any past day to fill it in, including one you forgot.

Two things I wanted that most apps get wrong:

Skipped days. Long-press a day and it's marked as skipped. It doesn't count as a miss, doesn't
break the streak and doesn't drag your percentage down. Being ill shouldn't cost you 40 days.

The widget. Three sizes, on the home screen, and tapping a square marks the day without opening
anything. The app I want is the one I don't have to open.

There's no account and no sign-up, it doesn't collect anything, and there are no ads. Your data
is a file on your phone you can export whenever you want.

It's free for three habits, and there's a one-off unlock if you want more. Nothing to buy to
try it.

I need people actually using it before Google will let me publish, so it's in closed testing for
now. If you want in:

1. Join quilt-testers@googlegroups.com
2. Opt in at https://play.google.com/apps/testing/com.baltajmn.habit

Use the Google account your phone signs into the Play Store with, or it won't show up.

Genuinely after criticism, not compliments. If the year grid doesn't land for you I'd like to
know why.
```

### Si alguien comenta

```markdown
Thanks for trying it. If it doesn't show up after joining the group, it's almost always the
account: the one you joined with has to be the one your phone uses in the Play Store. You can
check it in Play Store, tap your profile picture.
```

### Al publicar

- **Lee las reglas del sub primero.** `r/androiddev` prohíbe estos posts del todo, no es que vayan
  a otro sitio. En los subs de hábitos, escribe al moderador antes de publicar.
- **No pegues el mismo texto en cinco sitios el mismo día.** Reddit lo detecta como spam y te
  puede caer un shadowban, que es peor que no publicar: sigues viendo tus posts y nadie más.
  Reescribe la primera frase en cada uno y espárcelo en varios días.
- **Contesta a todo el que comente.** El que no recibe respuesta se desinstala a los tres días.
- **Lleva la cuenta de quién se apunta y cuándo.** El día 7 se cae alguien, siempre, y necesitas
  saber a quién escribir antes de que el contador baje de 12.
- Los DM que te vendan "12 testers garantizados" aparecen a los minutos. Ignóralos.
