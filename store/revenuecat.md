# RevenueCat, paso a paso

El código ya está escrito y probado contra el SDK real. Lo único que falta es la configuración de
las cuentas y una clave pública. **El orden importa**: cada paso depende del anterior.

Dato que condiciona todo: el código busca un derecho llamado **exactamente `pro`** y coge el
**primer paquete de la oferta actual**. Si el nombre no coincide o no hay oferta marcada como
actual, el paywall aparece vacío.

---

## Dónde está cada cosa en Play Console

Play Console tiene **dos niveles de navegación distintos**, y es la razón por la que la mitad de
estos menús "no aparecen": estás dentro de la app y viven fuera, o al revés.

- **Nivel de cuenta.** Sal de Quilt: arriba a la izquierda, *Todas las aplicaciones*. El menú
  cambia entero.
- **Nivel de app.** Entra en Quilt. Aquí están **Monetizar con Play → Productos**, la ficha, las
  versiones y todo lo demás.

| Lo que buscas | Nivel | Ruta |
|---|---|---|
| Crear el producto de pago | App | *Monetizar con Play → Productos → Productos integrados en la aplicación* |
| Perfil de pagos | Cuenta | *Ajustes → Monetización → Perfil de pagos* |
| Probadores que compran sin pagar | Cuenta | *Ajustes → Monetización → Licencia para testing* |
| Invitar la cuenta de servicio | Cuenta | *Usuarios y permisos → Invitar usuario* |
| Crear la cuenta de servicio | **Fuera de Play** | console.cloud.google.com |

**No busques "Acceso a la API" en Play Console: no hace falta para esto.** Esa página sirve para
vincular un proyecto de Cloud y llamar a la API tú mismo. RevenueCat no la usa. La cuenta de
servicio se crea entera en Google Cloud y luego se **invita como un usuario más** en Play.

Y dos cosas que no son un menú escondido sino una precondición:

- **Sin un AAB subido a algún canal, `Productos` sale vacío y no deja crear nada.** No hay atajo.
- Sin **perfil de pagos verificado**, tampoco. Es el paso con más latencia: tarda días.

---

## 0. Qué se vende, decidido

| | |
|---|---|
| Producto | Uno solo, compra única. Nunca suscripción. |
| Identificador | `pro_lifetime`. **Irreversible**: un ID borrado no se reutiliza jamás. |
| Nombre visible | Quilt Pro |
| Precio | 4,99 € de base, conversión automática al resto de monedas **con redondeo**. |
| Países | Todos. |
| Prueba gratuita | No. El plan gratis es la prueba. |

Qué abre el pago, y solo esto:

- **Hábitos ilimitados.** Gratis son 3 (`HabitRepository.FREE_HABIT_LIMIT`).
- **La paleta completa.** Gratis los 4 primeros colores (`FREE_COLOR_LIMIT`), de 8.

Qué **no** se cobra nunca, porque está prometido en la ficha y en la política de privacidad:
los tres tamaños de widget, los recordatorios, exportar e importar, y la cuadrícula anual entera.

> Si un día cambia cualquiera de estas dos listas, hay que tocar `pitch` en `Strings.kt` y la
> descripción larga de la ficha **en el mismo commit**. Vender una función que la versión gratis ya
> tiene es tergiversación, y Play lo trata como tal.

Un usuario gratis que acabe con más hábitos de los que le tocan (importó un backup, le reembolsaron
la compra) **los conserva todos**. `canAddHabit()` solo bloquea crear el siguiente. No se oculta
nada: pedirle a alguien que elija cuáles de sus hábitos sobreviven es peor que el agujero que tapa.

## 1. Antes de tocar RevenueCat: el producto en Play

No se puede conectar nada hasta que Play tenga qué vender.

1. Play Console → **dentro de Quilt** → **Monetizar con Play → Productos → Productos integrados
   en la aplicación**. (No se llama "productos de una sola compra": ese menú es *Suscripciones*.)
2. **Crear producto.**
3. Identificador: `pro_lifetime`.
4. Nombre y descripción por idioma, los de la tabla de abajo.
5. Precio 4,99 €, convierte al resto y pulsa **Redondear precios**: sin eso salen 5,37 zł y
   227,43 ¥, que leen como un error de la tienda.
6. **Actívalo.** Un producto inactivo no aparece por la API y el paywall sale sin precio.

| Idioma | Nombre | Descripción |
|---|---|---|
| en-US | Quilt Pro | Removes the habit limit for good and unlocks the full colour palette. One-time payment, not a subscription. Reminders, widgets, export and the full year grid stay free. |
| es-ES | Quilt Pro | Quita el límite de hábitos para siempre y desbloquea la paleta completa. Pago único, no es una suscripción. Recordatorios, widgets, exportación y rejilla anual siguen gratis. |
| pt-PT | Quilt Pro | Remove o limite de hábitos para sempre e desbloqueia a paleta completa. Pagamento único, não é uma assinatura. |
| de-DE | Quilt Pro | Hebt das Gewohnheiten-Limit dauerhaft auf und schaltet die komplette Farbpalette frei. Einmalzahlung, kein Abo. |
| fr-FR | Quilt Pro | Supprime la limite d'habitudes pour toujours et débloque la palette complète. Paiement unique, pas d'abonnement. |

> Requisito previo: **un AAB ya subido a un canal de prueba** y el **perfil de pagos verificado**.
> Sin las dos cosas la pantalla sale vacía y el botón de crear no hace nada. No es que el menú no
> exista: es que todavía no tienes qué vender ni cómo cobrarlo.

> **Producto creado el 26 ago 2026**: `pro_lifetime`, *Quilt Pro*, activo en 173 países,
> clasificación 13+, tipo impositivo *Ventas de apps digitales*. La opción de compra aparece como
> `prolifetime` y marcada *Retrocompatible*. Eso es normal: Play deriva el ID de la opción quitando
> el guion bajo, y "retrocompatible" es justo el modo que necesita RevenueCat. El identificador que
> se usa en todas partes sigue siendo `pro_lifetime`.

## 2. La cuenta de servicio de Google Cloud

Es lo que permite a RevenueCat preguntarle a Google si una compra es real.

El modelo mental, que es lo que se pierde entre tanto menú: **la cuenta de servicio es un robot con
su propia dirección de correo**. Se fabrica en Google Cloud, y luego se le da de alta en Play como
si fuera un compañero de trabajo, en la misma pantalla con la que invitarías a una persona. No es
"release manager": no publica nada. Solo lee pedidos.

### En Google Cloud (console.cloud.google.com)

1. Crea un proyecto, o usa uno que ya tengas. Da igual cuál.
2. **APIs y servicios → Biblioteca**. Activa tres:
   - Google Play Android Developer API
   - Google Play Developer Reporting API
   - Cloud Pub/Sub API
3. **IAM y administración → Cuentas de servicio → Crear cuenta de servicio**. Nombre: `revenuecat`.
4. En el paso de roles, dale dos: **Editor de Pub/Sub** y **Visualizador de Monitoring**.
5. Termina, entra en la cuenta creada → **Claves → Agregar clave → Crear nueva → JSON**. Se descarga.
6. Copia su correo, que es de la forma `revenuecat@<proyecto>.iam.gserviceaccount.com`.

### En Play Console (nivel de cuenta)

7. **Usuarios y permisos → Invitar usuario**. Pega ese correo como si invitaras a una persona.
8. Permisos. Son cuatro, y **ninguno es de publicar**:
   - Ver información de la aplicación y descargar informes masivos
   - Ver datos financieros, pedidos y respuestas de encuestas de cancelación
   - Gestionar pedidos y suscripciones
   - Gestionar la presencia en la tienda

> Ese JSON es una credencial. No lo pegues en un chat ni lo subas al repositorio: se sube
> directamente en el formulario de RevenueCat.

> **Hasta 36 horas** para que las credenciales funcionen contra la API de Google. Mientras tanto
> RevenueCat da errores de validación y no significa que esté mal montado. Truco que a veces lo
> acelera: edita y guarda la descripción del producto en *Monetizar*.

## 3. El proyecto en RevenueCat

1. Crear cuenta en [revenuecat.com](https://www.revenuecat.com) → **Create new project**.
2. **Apps → + Play Store**.
   - Package name: `com.baltajmn.habit`
   - Service Account Credentials JSON: sube el fichero del paso 2.
3. Espera a que el estado pase a verde. Puede tardar hasta 36 h la primera vez, es normal.

## 4. Producto, derecho y oferta

Los tres, en este orden:

1. **Products → + New** → Store: Play Store → Product ID: `pro_lifetime` (el mismo del paso 1).
2. **Entitlements → + New** → Identifier: **`pro`**, exactamente en minúsculas. Es el nombre que
   busca el código en `Billing.ENTITLEMENT`. → **Attach** el producto del punto anterior.
3. **Offerings → + New** → Identifier: `default` → márcala como **Current**.
   → Dentro, **+ Package** → tipo *Lifetime* → asóciale el producto.

Comprobación mental: si en Offerings no hay una marcada como *Current*, o esa oferta no tiene
ningún paquete, la app enseñará el paywall sin precio y sin botón de compra.

## 5. La clave

1. **Project settings → API keys**.
2. Copia la **clave pública de Android**, la que empieza por `goog_`.
3. Pásamela. Esa se puede compartir: va compilada dentro del APK y cualquiera puede extraerla del
   binario, por eso es pública.
4. **La clave secreta (`sk_...`) no me la pases nunca ni la pongas en el código.** Solo sirve para
   llamadas de servidor a servidor.

Yo la pego en `revenueCatApiKey` (el `actual` de Android) y verifico compra y restauración.

## 6. Probar una compra de verdad

1. Play Console, nivel de cuenta → **Ajustes → Monetización → Licencia para testing** → añade el
   correo de Google que vayas a usar. Ese correo compra de verdad, con diálogo real, sin cargo.
2. Instala la app desde el canal de prueba, no por `adb`. Una compra solo funciona si el binario
   viene de Play.
3. Compra, y comprueba en RevenueCat → **Customer History** que aparece el evento y que el derecho
   `pro` queda activo.
4. Desinstala, reinstala, y usa **Restaurar compra**. Es el camino que más se rompe y el que más
   reseñas de una estrella genera.

## 7. iOS, cuando llegue

Mismo proyecto de RevenueCat, otra app dentro:

- **Apps → + App Store**, con el bundle ID y la *App-Specific Shared Secret* de App Store Connect.
- Crear allí el producto con **el mismo identificador**, `pro_lifetime`.
- Asociarlo al **mismo derecho `pro`** y al mismo paquete de la oferta.
- Copiar la clave pública de iOS (`appl_...`) y pasármela.

Compartir derecho y oferta entre las dos tiendas es lo que hace que un usuario que compró en
Android no vuelva a pagar si un día se pasa a iPhone.
