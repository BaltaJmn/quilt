# RevenueCat, paso a paso

El código ya está escrito y probado contra el SDK real. Lo único que falta es la configuración de
las cuentas y una clave pública. **El orden importa**: cada paso depende del anterior.

Dato que condiciona todo: el código busca un derecho llamado **exactamente `pro`** y coge el
**primer paquete de la oferta actual**. Si el nombre no coincide o no hay oferta marcada como
actual, el paywall aparece vacío.

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

1. Play Console → tu app → **Monetizar → Productos → Productos de una sola compra**.
2. Crear producto. Tipo: **compra única**, no suscripción.
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

> Requisito previo: haber subido ya un AAB a un canal de prueba. Sin binario, Play no deja crear
> productos.

## 2. La cuenta de servicio de Google Cloud

Es lo que permite a RevenueCat preguntarle a Google si una compra es real.

Camino corto: se empieza en Play, que crea el proyecto de Google Cloud por ti. No hace falta tener
uno antes.

1. Play Console → **Configuración → Acceso a la API** → *Vincular un proyecto de Google Cloud* →
   **Crear un proyecto nuevo**.
2. En esa misma página → **Crear cuenta de servicio**. Te lleva a Google Cloud.
3. Google Cloud → *Crear cuenta de servicio*. Nombre: `revenuecat`. **Sin roles**: los permisos se
   dan en Play, no aquí.
4. Sobre la cuenta creada → **Claves → Agregar clave → Crear nueva → JSON**. Se descarga.
5. Vuelta a Play Console → *Acceso a la API* → aparece la cuenta → **Conceder acceso**. Permisos
   sobre Quilt: **ver información de la app**, **ver datos financieros**, **gestionar pedidos y
   suscripciones**. Nada más.

> Ese JSON es una credencial. No lo pegues en un chat ni lo subas al repositorio: se sube
> directamente en el formulario de RevenueCat.

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

1. Play Console → **Configuración → Pruebas de licencia** → añade el correo de Google que vayas a
   usar. Ese correo compra sin que se le cobre.
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
