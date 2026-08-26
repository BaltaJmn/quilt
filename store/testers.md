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
