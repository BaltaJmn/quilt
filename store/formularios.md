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
| Subtitulo, palabras clave, texto promocional, descripcion | `store/app-store/<idioma>/`, los 13 idiomas de `store/listings/`. Topes comprobados por `listings.yml` en cada push |
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

Desde el 06-10-2026 son las siete respuestas que Apple pidió a Chroma en su primera revisión (2.1,
*Information Needed*, por ser una cuenta con poco historial), para adelantarse. El vídeo se grabó en un
iPhone el 06-10-2026 y va como archivo adjunto de la información para la revisión; el punto 1 cuenta
lo que se ve en él.

```
1. Screen recording
Attached: a recording made on a physical iPhone running the latest iOS, starting from a fresh install: creating a habit with its days and a daily reminder, marking it done for today, Settings with the backup, import and CSV export, the Quilt Pro screen, buying Quilt Pro with a sandbox account, the Pro feature of having more than three habits (the free limit), and the home screen widget. The Buy button shows the price StoreKit returns. The recording was made before Quilt Pro dropped to $1.99: the button shows the US price at the time ($3.99), because the device had not signed in to the sandbox store yet, and the purchase sheet the Spanish one (4.99 EUR). Quilt has no account registration, login or account deletion, and nothing is shared with other users (no user-generated content), so those flows do not exist.

2. Purpose and audience
Quilt is a yearly habit tracker. Each habit is a grid of the whole year: every day you keep it becomes a filled patch, and by the end of the year you have the whole quilt. It is for people who want to build habits with a clear visual overview and no pressure: no accounts, no social feeds and no ads. Everything stays on the device.

3. How to use it
No login, no setup and no sample files are needed.
- Tap + to create a habit: name, icon, color, the days it is due and an optional reminder.
- Tap a habit's check to mark today, or tap any past day in its grid to mark it. Long-press a day to mark it as skipped: it is excused and does not break the streak.
- Tap a habit to open its detail: the year grid and its stats.
- Share the year as an image. The only photo permission is add-only, to save that image.
- Settings (gear icon, top right): reminders, export and import, Get Pro, Restore purchase and the privacy policy.
- Widgets, and Siri and Shortcuts ("Mark a habit in Quilt").
Reminders are local notifications; the permission prompt appears when the first reminder is set.

4. External services
- Apple In-App Purchase (StoreKit), for the single purchase.
- RevenueCat (revenuecat.com), to validate that purchase and know whether Quilt Pro is active. It receives the purchase and an anonymous ID, never a name or an email.
Nothing else: no account system, no server of our own, no analytics, no ads and no AI services.

5. Regions
The app works the same in every region. It is available in every App Store country except mainland China, in 13 languages, and Quilt Pro costs $1.99 (1.99 EUR in Spain), with Apple's regional pricing elsewhere.

6. Regulated industries and third-party material
Not applicable: Quilt is not in a regulated industry and includes no protected third-party material.

7. In-App Purchase
One non-consumable product, Quilt Pro (com.baltajmn.habit.pro_lifetime): a one-time payment, no subscription. The free plan allows 3 habits and the first 4 of the 8 colors; Pro removes both limits. Widgets, reminders, export and import and the full year grid are free for everyone. The Pro dialog opens when you tap + with 3 habits already created, when you tap a locked color in the habit form, and from Settings > Get Pro; tap "Buy for <price>". Restore purchase is in Settings and inside the Pro dialog; both are hidden once Pro is active.
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
| Precio | 1,99 EUR de base en España (con IVA), igual en las dos tiendas con `compra.py` de `BaltaJmn/ci` (las dos desde el 06-10-2026) |
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
