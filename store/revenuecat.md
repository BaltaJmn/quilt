# RevenueCat, paso a paso

El código ya está escrito y probado contra el SDK real. Lo único que falta es la configuración de
las cuentas y una clave pública. **El orden importa**: cada paso depende del anterior.

Dato que condiciona todo: el código busca un derecho llamado **exactamente `pro`** y coge el
**primer paquete de la oferta actual**. Si el nombre no coincide o no hay oferta marcada como
actual, el paywall aparece vacío.

---

## 1. Antes de tocar RevenueCat: el producto en Play

No se puede conectar nada hasta que Play tenga qué vender.

1. Play Console → tu app → **Monetizar → Productos → Productos de una sola compra**.
2. Crear producto. Tipo: **compra única**, no suscripción.
3. Identificador sugerido: `pro_lifetime`. Anótalo, hace falta luego.
4. Ponle precio y **actívalo**. Un producto inactivo no aparece por la API.

> Requisito previo: haber subido ya un AAB a un canal de prueba. Sin binario, Play no deja crear
> productos.

## 2. La cuenta de servicio de Google Cloud

Es lo que permite a RevenueCat preguntarle a Google si una compra es real.

1. [Google Cloud Console](https://console.cloud.google.com) → el proyecto asociado a tu cuenta de
   Play → **IAM y administración → Cuentas de servicio → Crear**.
2. Sin roles en Google Cloud. Los permisos se dan en Play, no aquí.
3. Dentro de la cuenta creada → **Claves → Agregar clave → Crear nueva → JSON**. Se descarga.
4. Play Console → **Usuarios y permisos → Invitar usuario** → pega el correo de la cuenta de
   servicio (`...@....iam.gserviceaccount.com`).
5. Dale permisos de app para Quilt: **ver información de la app**, **ver datos
   financieros**, **gestionar pedidos y suscripciones**.

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
