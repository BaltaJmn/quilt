# Formularios de App Store Connect, respuesta a respuesta

Todo lo que App Store Connect pregunta y no tiene API, con la respuesta cerrada y el hecho del codigo
que la sostiene. Se pega a mano. Si el codigo cambia algo de lo que aqui se afirma (un permiso, un SDK,
un dato que sale del telefono), se cambian en el mismo cambio este fichero, `store/privacy/index.html`
(y su copia publicada en `BaltaJmn/quilt-privacy`) y `iosApp/iosApp/PrivacyInfo.xcprivacy`.

Este fichero solo lleva la parte de App Store. Las respuestas de Play (seguridad de los datos, IARC,
publico objetivo) estan en `store/play-listing.md` y `store/produccion.md`; la numeracion 5 y 6 sigue la
de Chroma para que los dos repositorios se lean igual. El orden de la subida, en `store/app-store.md`.

Hechos de partida, comprobados en el codigo:

- Los habitos, el historial y las notas viven en un fichero del dispositivo (`habits.json`, en el App
  Group `group.com.baltajmn.habit`). No hay servidor propio, ni cuenta, ni analitica, ni publicidad, ni
  informes de fallos. Las unicas dependencias de red son `purchases-kmp` (RevenueCat); no hay Ktor,
  Supabase, Firebase ni nada parecido en `gradle/libs.versions.toml`.
- Lo unico que sale del telefono es lo de **RevenueCat**: un identificador anonimo de instalacion
  (`Purchases.configure` sin `appUserID`, y no hay `logIn` en ningun sitio), el historial de compras y
  datos tecnicos del dispositivo.
- `Billing.configure()` no hace nada mientras `revenueCatApiKey` sea `null`. Hoy es `null` en iOS
  (`Billing.ios.kt`), asi que **la build actual de iOS no habla con ninguna red**. Las respuestas de abajo
  son las de la build con la clave `appl_` puesta, que es la que se publica: declarar de mas no lo
  rechaza Apple, declarar de menos si.
- El `Info.plist` solo declara `NSPhotoLibraryAddUsageDescription` (guardar la imagen del ano en el
  carrete, solo anadir). No pide camara, ubicacion, contactos ni seguimiento
  (`NSUserTrackingUsageDescription` no existe, no hay `ATTrackingManager`).
- Los recordatorios son notificaciones **locales** (`UNUserNotificationCenter`), sin push ni
  entitlement de notificaciones remotas. El unico entitlement es el App Group.
- Siri y Atajos (`Shortcuts.swift`, App Intents) marcan un habito sin salir del dispositivo.

---

## 5. App Store: privacidad de la app

*App Store Connect > Quilt > Privacidad de la app.*

| Pregunta | Respuesta |
|---|---|
| ¿Recoges datos de esta app? | Si |
| Compras > Historial de compras | Recogido. Finalidades: funcionalidad de la app y analisis de datos, las dos que pide la documentacion de RevenueCat. **No** vinculado a la identidad. **No** usado para rastreo. Rellenado en App Store Connect el 05-10-2026 |
| Identificadores | No se marcan. RevenueCat pide *ID de usuario* solo con IDs propios e *ID de dispositivo* solo con integraciones que usen el IDFA, y la app usa su ID anonimo. Asi quedaron las cuatro apps de la familia. `PrivacyInfo.xcprivacy` declara ademas `DeviceID`: de mas, no de menos |
| El resto de tipos | No recogidos |
| URL de la politica de privacidad | `https://quilt.baltajmn.dev/` |
| URL de opciones de privacidad | Vacio |

Tiene que decir lo mismo que `iosApp/iosApp/PrivacyInfo.xcprivacy`: `PurchaseHistory` y `DeviceID`,
`Linked = false`, `Tracking = false`, finalidad `AppFunctionality`, y `NSPrivacyTracking = false` sin
dominios de rastreo. Las dos APIs de razon requerida que declara (`stat` con C617.1 y `NSUserDefaults`
con CA92.1) no salen en el formulario.

Lo que **no** se marca, y por que:

| Tipo | Por que no |
|---|---|
| Contenido del usuario (habitos, notas, fotos) | Viven en el dispositivo y no salen de el; la imagen del ano solo va a la hoja de compartir que el usuario abre |
| Ubicacion, contactos, salud y forma fisica, mensajes | La app no los toca. "Salud" tampoco: los habitos son texto libre del usuario, no datos de salud |
| Uso y diagnosticos | No hay SDK de analitica ni de informes de fallos |
| Datos de pago | Los trata Apple; la app solo recibe el resultado a traves de RevenueCat |

Si el informe de privacidad de Xcode sobre el primer archivo anade algo, se anade en los dos sitios
(este formulario y el manifiesto). Si algun dia se activa una integracion de analitica de RevenueCat o
se anade cualquier otro SDK, estas respuestas se quedan cortas y obligan a rehacer el formulario, la
politica y el manifiesto.

## 6. App Store: el resto de la ficha

| Campo | Valor |
|---|---|
| Plataforma y dispositivos | Solo iPhone. Depende de `TARGETED_DEVICE_FAMILY = 1` en `project.pbxproj`, que ahora mismo es un cambio **sin commit** en el repositorio; commiteado, no hacen falta capturas de iPad (la fase G de `app-store.md` aun las pide) |
| Nombre | `Quilt: Habit Tracker` (20/30). `Quilt: Habit Tracker & Streaks`, el titulo de Play, ya lo tenia reservado otra app el 05-10-2026 |
| Subtitulo, palabras clave, texto promocional, descripcion | `store/app-store/<idioma>/`, los 13 idiomas de `store/listings/`. Topes comprobados con `python3 tools/store/fichas.py` |
| Idioma principal | English (U.S.) |
| SKU | `quilt-ios-001` (el de `app-store.md`) |
| Bundle ID | `com.baltajmn.habit` |
| Categoria principal | **Productividad** (la misma razon que en Play: la app es agnostica al habito) |
| Categoria secundaria | Estilo de vida. Salud y forma fisica es la otra opcion razonable, pero promete metricas corporales que no hay |
| Clasificacion por edad | Cuestionario: **ninguno** / "no" en todos los contenidos (violencia, sexo, lenguaje, drogas, miedo, apuestas, temas medicos o de salud); **no** en contenido generado por usuarios, mensajeria, publicidad y acceso web sin restricciones (la politica se abre en el navegador del sistema, la app no tiene navegador propio). Resultado esperado **4+** |
| Derechos de contenido | No contiene ni accede a contenido de terceros |
| Cumplimiento de exportacion | No pregunta: `ITSAppUsesNonExemptEncryption = false` esta en `iosApp/iosApp/Info.plist`. **No esta en el `.pbxproj`** (alli solo hay `INFOPLIST_FILE`), y esta bien: el `Info.plist` se fusiona con el generado. El widget no lo lleva y no hace falta |
| Copyright | `2026 Baltasar Jiménez` |
| URL de soporte | `https://quilt.baltajmn.dev/` (lleva el correo de contacto) |
| URL de marketing | Vacio, o la misma |
| Politica de privacidad | `https://quilt.baltajmn.dev/` |
| Precio | Gratis; la compra es el producto de abajo |
| Disponibilidad | Todos los paises |
| Inicio de sesion para la revision | No hace falta: la app no tiene cuentas |
| Contacto del revisor | Nombre, correo `baltajmn@gmail.com` y **telefono** (obligatorio en este formulario, lo pone el usuario) |
| Que hay de nuevo | No se pide en la primera version |
| Publicacion | Manual, para decidir el dia de salida |

Comprobado con `curl -sI https://quilt.baltajmn.dev/` el 05-10-2026: responde `HTTP/2 200`. La pagina
publicada coincide con `store/privacy/index.html` (la unica diferencia es la ofuscacion de los correos
que anade Cloudflare), asi que el aviso de `app-store.md` sobre copiarla al repositorio publico ya esta
cumplido. Menciona "App Store" y "Google Play" como procesadores de pagos, que es lo que pide 5.1.1.

Usos declarados en el `Info.plist`, en los cinco idiomas de la app (`<lang>.lproj/InfoPlist.strings`):
solo `NSPhotoLibraryAddUsageDescription` ("Para guardar la imagen de tus habitos en tu carrete.").

Notas para el revisor, en ingles:

```
Quilt has no account and no server. Everything is stored on the device, so no demo account is needed.

The free plan allows 3 habits and the first 4 of the 8 colors. Quilt Pro is a one-time non-consumable in-app purchase (product com.baltajmn.habit.pro_lifetime, entitlement "pro" in RevenueCat) that removes both limits. Widgets, reminders, export and import, and the full year grid are free for everyone.

To reach the purchase screen: tap the + button to create habits; once there are 3, tapping + again opens the Pro dialog. It also opens when you tap a locked color in the habit form (colors after the first 4), and from Settings (gear icon, top right) > "Get Pro". Tap "Buy for <price>" and complete the purchase with a Sandbox account.

Restore Purchase: Settings (gear icon, top right) > "Restore purchase", and also inside the Pro dialog. Both are hidden once Pro is active. The privacy policy link is at the bottom of Settings.

Reminders are local notifications; the permission prompt appears when the first reminder is set. The only photo permission is add-only, used to save a share image of the year to the camera roll. Siri and Shortcuts can mark a habit ("Mark a habit in Quilt").
```

### Producto: `pro_lifetime` (Play), `com.baltajmn.habit.pro_lifetime` (App Store)

*App Store Connect > Quilt > Monetizacion > Compras dentro de la app.* El codigo no nombra el producto: busca
el derecho `pro` (`Billing.ENTITLEMENT`) y vende el **primer paquete de la oferta actual** de RevenueCat
(`Billing.proPackage()`), asi que el identificador y el precio viven en los paneles
(`store/revenuecat.md`), no en el codigo.

| Campo | Valor |
|---|---|
| Tipo | No consumible |
| Nombre de referencia | `Quilt Pro` |
| ID de producto | `com.baltajmn.habit.pro_lifetime`. En Apple un ID no se repite entre apps de la misma cuenta, y Chroma y FlowTime tambien venden `pro_lifetime`. No se puede reutilizar si se borra |
| Precio | 4,99 EUR como base en Espana (en Apple ya lleva IVA), el mismo escaparate que pide `revenuecat.md`; el resto de paises por conversion automatica |
| Disponibilidad | Todos los paises |
| Compartir en familia | Sin marcar |
| Captura para la revision | La pantalla Pro (el dialogo "Hábitos ilimitados"), obligatoria |
| Notas de la revision | "Opens from the + button once there are 3 habits, from a locked color in the habit form, and from Settings > Get Pro. Restore Purchase is in Settings and inside the dialog." |
| En RevenueCat | Producto `com.baltajmn.habit.pro_lifetime` en el mismo derecho `pro` y el mismo paquete de la oferta `default` que Android; clave publica `appl_` en `Billing.ios.kt` |

Nombre visible (maximo 30) y descripcion (maximo 45), por idioma. El nombre es `Quilt Pro` en todos:

| Idioma | Descripcion |
|---|---|
| en-US | No habit limit, full color palette |
| es-ES | Sin límite de hábitos y paleta completa |
| pt-BR | Sem limite de hábitos e paleta completa |
| de-DE | Unbegrenzte Gewohnheiten, alle Farben |
| fr-FR | Habitudes illimitées et palette complète |
| it-IT | Abitudini illimitate e tutti i colori |
| nl-NL | Onbeperkt gewoonten en alle kleuren |
| pl-PL | Bez limitu nawyków i pełna paleta |
| id | Kebiasaan tanpa batas dan semua warna |
| ru-RU | Без лимита привычек и вся палитра |
| tr-TR | Sınırsız alışkanlık ve tüm renkler |
| ko-KR | 습관 개수 제한 해제와 전체 색상 팔레트 |
| ja-JP | 習慣数の上限解除とすべてのカラー |

Lo que abre la compra, y solo esto: habitos ilimitados (gratis son 3, `FREE_HABIT_LIMIT`) y la paleta
completa (gratis los 4 primeros de 8, `FREE_COLOR_LIMIT`). Si cambia cualquiera de las dos cosas hay que
tocar estas descripciones, `pitch` en `Strings.kt` y la descripcion larga de la ficha en el mismo cambio.
