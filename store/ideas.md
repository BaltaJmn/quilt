# Ideas de producto

Filtro que se aplica aqui: **una funcionalidad entra si no anade una pantalla**. Quilt cabe hoy en
tres pantallas (lista, detalle, formulario) mas dos hojas (ajustes y compartir). Lo que obligue a una
sexta pantalla o a una pestana nueva se queda fuera aunque sea buena idea, porque el minimalismo es
la caracteristica, no el estilo.

Ordenadas por valor dividido entre coste, la primera es la que yo haria antes.

---

## 1. Atajos del sistema. Coste bajo, ninguna pantalla nueva

El `AppIntent` que ya usa el boton del widget de iOS sirve tal cual para Siri y Spotlight: basta un
`AppShortcutsProvider` que lo declare. A partir de ahi "Oye Siri, marca Agua en Quilt" funciona sin
abrir la app, y el intent aparece en Atajos para que el usuario lo meta en una automatizacion suya
("al llegar a casa, marca Gimnasio").

En Android el equivalente es un `TileService`, la baldosa de los ajustes rapidos: se despliega la
cortina, un toque, hecho.

Es la mejor relacion de todas: la logica ya existe y esta probada, solo esta sin exponer.

## 2. Widget de un hábito con el año entero. Coste medio, ninguna pantalla nueva

Hoy el widget ensena varias filas con la semana. Falta el que ensena **un** habito con sus 365
cuadritos, que es justamente la imagen que vende la app. En iOS es una `Widget` nueva con
`AppIntentConfiguration` para elegir cual, y en Android otro `GlanceAppWidget`. El dibujado ya
existe en `YearGrid.kt`.

Ademas resuelve de paso la deuda anotada de que el widget pequeno deja media caja vacia.

## 3. Widget de pantalla de bloqueo, iOS. Coste bajo, solo iOS

Anadir las familias `accessoryCircular` y `accessoryRectangular` al `TimelineProvider` que ya hay:
un anillo con los habitos de hoy hechos sobre el total. Son unas pocas decenas de lineas y el mismo
`entry()`.

Android no tiene equivalente, asi que rompe la paridad. Se puede asumir: es una ventaja de la
plataforma, no una funcion que falte en la otra.

## 4. Objetivo semanal: "N dias por semana". Coste alto, pero es lo que mas piden

Hoy un habito se programa con dias fijos (`scheduleDays`). Falta lo otro: "correr tres veces por
semana, me da igual cuales". Es la peticion numero uno en cualquier app de habitos, porque los dias
fijos convierten un mal martes en una racha rota.

En el modelo es un campo al lado del que ya hay, algo como `weeklyTarget: Int?`. El coste no esta en
el campo, esta en que la racha, el porcentaje de cumplimiento y el estado de cada cuadrito dejan de
poder decidirse mirando un solo dia: hay que mirar la semana entera. Y `habits.json` es contrato
compartido, asi que el `Habit` de Swift del widget tiene que reflejar el campo el mismo dia.

Merece la pena, pero es la unica de la lista que no cabe en una tarde.

## 5. El recordatorio se calla si ya lo hiciste. Coste medio, invisible

Ahora el recordatorio suena a su hora aunque hayas marcado el habito hace dos horas. Es la clase de
detalle que hace que la gente apague las notificaciones, y apagar las notificaciones es la primera
mitad de desinstalar.

En Android es barato: se reprograma la alarma al marcar. En iOS no tanto, porque el disparador es un
`UNCalendarNotificationTrigger` repetitivo y no se puede saltar un solo dia: habria que pasar a
disparadores de una sola vez que se renuevan al abrir la app, y eso choca con el limite de 64
peticiones pendientes que ya esta anotado como deuda. Hay solucion, pero pide pensar el esquema
entero de recordatorios de iOS de nuevo.

## 6. Una nota corta por dia. Coste medio, riesgo para el minimalismo

Un campo de texto al pulsar un cuadrito: "hoy 5 km". Convierte la cuadricula en un diario sin
convertir la app en un diario.

Es la unica de la lista que puede estropear lo que tienes. Si entra, que sea sin sitio propio: solo
visible al abrir un dia concreto, nunca en la lista ni en el widget, y sin busqueda. En cuanto pida
una pantalla para buscar notas, ya es otra app.

---

## Lo que no hay que anadir, aunque lo pida el mercado

- **Sincronizacion en la nube.** "Todo se queda en tu movil" es la mitad del argumento de venta y
  toda la politica de privacidad. Anadirla obliga a servidor, cuentas, RGPD real y rehacer la ficha
  de las dos tiendas. La exportacion y la importacion ya cubren el cambio de telefono.
- **Insignias, niveles, puntos.** La cuadricula ya es la recompensa.
- **Nada social.** Compartir la imagen es suficiente, y no trae moderacion.
- **Analitica.** Hoy la politica de privacidad puede decir que no hay ninguna, y eso vale mas que
  saber cuanta gente pulsa que boton.
- **Anuncios.** Incompatible con el precio unico y con no recoger nada.
