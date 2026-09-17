# Solicitar acceso a producción

El formulario de *Producción > Solicitar acceso a producción* son ocho respuestas libres de 300
caracteres y dos desplegables. Si Google rechaza la solicitud hay que rellenarlo entero otra vez,
así que el texto vive aquí y no solo en el navegador.

**En inglés a propósito.** El revisor no tiene garantizado el español y la ficha por defecto ya es
`en-US`.

Lo que se responde aquí es una declaración, no marketing: Google contrasta lo que escribes contra
las estadísticas reales de la prueba cerrada. Todo lo de abajo sale del repositorio o de un mensaje
real de un tester, y en el apartado final se dice de dónde sale cada dato.

## 1. Información sobre tu prueba cerrada

**¿Cómo reclutaste usuarios?** (280)

```
I asked friends, family and co-workers with Android phones one by one, in person, rather than posting in a group chat. The rest came from habit tracking and beta subreddits, after asking the moderators first. No paid services and no tester swap groups. The list is a Google Group.
```

**¿Cómo de fácil te ha resultado reclutar testers?** Difícil.

**Describe las interacciones de los testers** (268)

```
I asked them to track a real habit of their own, not a test one. Daily use across the 14 days, which is the normal pattern for this app. Most used the year grid, marking the day and the home screen widget. Fewer touched reminders, rest days, export and the share card.
```

**Resume los comentarios y cómo los recogiste** (279)

```
By direct message and by replying to comments, no form. One tester reported the share screen overflowing on a wide screen, fixed and shipped in 1.6 during the test. Another said the completion percentage is what keeps them coming back, at 73%, and asked for more personalization.
```

## 2. Sobre tu aplicación

**¿A qué audiencia va dirigida?** (288)

```
Adults and teenagers from 13 up who want to keep a daily habit, and people who already track habits on paper: year in pixels grids, bullet journals, wall calendars. It also suits privacy minded users, since there is no account and nothing leaves the phone. Nothing in it targets children.
```

Tiene que decir lo mismo que el IARC: **público objetivo 13+** y **no** a si la app atrae a
menores. Las respuestas del cuestionario están en `play-listing.md`.

**Describe cómo aporta valor** (283)

```
Most habit apps show today and a streak number. Quilt shows all 365 days as one grid, so progress is visible at a glance. Rest days can be skipped without breaking a streak. Widgets, reminders, export and the full year grid are free, and there is no account, no ads and no analytics.
```

**Descargas esperadas el primer año:** entre 0 y 10.000. El propio formulario dice que estas
respuestas no afectan a nada, así que se contesta lo que es.

## 3. Preparación para producción

**¿Qué cambios has hecho a partir de la prueba?** (287)

```
A tester on a wide screen found the share screen overflowing; it now fits on tablets and in landscape. Two more came out of the test: marking a habit wrote into yesterday's box past midnight, and the year grid stopped taking taps after the day changed. All three shipped during the test.
```

**¿Cómo decidiste que estaba lista?** (282)

```
That build ran the rest of the window with no new reports from testers, and every fix from the test is in it. The shared logic has automated tests that run on every push, and CI builds, signs and publishes the release, so production ships through the same pipeline the testers used.
```

## De dónde sale cada dato

| Afirmación | Prueba |
|---|---|
| El tester de la pantalla ancha | Cuerpo del commit `9b94503`: "es lo que describe el tester en Waydroid" |
| Marcar escribía en la casilla de ayer | `7326acf`, más `4b79003` para el caso de medianoche con la app abierta |
| La rejilla dejaba de aceptar toques | `01a2c2c` |
| Los tres llegaron a los testers | Los tres viajan en la 1.6, versionCode 7, única ejecución de `release.yml` que acabó en `alpha` |
| Tests en cada push | `.github/workflows/tests.yml` |
| El porcentaje y la personalización | Mensaje de un tester, citado entero en `ideas.md` |

**Lo único que no se puede verificar desde aquí es "no new reports from testers".** Antes de enviar,
mira *Monitorizar y mejorar > Android vitals* por si hay algún fallo o ANR que no te haya reportado
nadie, y repasa si alguien escribió entre el 1 y el 12 de septiembre sin que se arreglara.

## Lo que hay que tener cerrado antes de publicar, no antes de solicitar

La solicitud se puede enviar ya. Lo que no puede salir a producción sin esto:

- Data Safety y clasificación IARC, respuestas en `play-listing.md`
- El producto `pro_lifetime` a 4,99 €, o el botón de compra falla en producción. Pasos en
  `revenuecat.md`

## Cronología de la prueba cerrada

| Fecha | Qué |
|---|---|
| 26 ago 2026 | 1.1, versionCode 2, subida a mano a la Console. Es la que instalaron los testers |
| 29 ago 2026 | Tester número 12 acepta. Arranca el contador de 14 días |
| 1 sep 2026 | 1.6, versionCode 7, a `alpha` desde CI, con los tres arreglos |
| 12 sep 2026 | Ventana cumplida |

1.2, 1.3, 1.4 y 1.5 nunca llegaron a los testers: v1.2 falló, v1.3 y v1.4 no tuvieron etiqueta y
v1.5 acabó en `internal`, que no tiene testers. Su contenido viaja dentro de la 1.6. **No digas
"cinco versiones" en el formulario.**
