# Subir Quilt a la App Store, paso a paso

Mismo criterio que `lanzamiento.md`: **[tú]** es lo que solo puedes hacer tú porque pide cuenta,
contraseña, dinero o un formulario; **[yo]** es lo que ya está hecho en el repositorio.

El orden importa. Cada fase desbloquea la siguiente, y hay dos decisiones irreversibles marcadas
como tales. Léelas antes de pulsar nada.

---

## Fase A. La cuenta. Bloquea absolutamente todo

Sin cuenta de desarrollador no se puede ni firmar en local para un dispositivo real, ni crear el
identificador, ni abrir la ficha. Es lo primero.

1. **[tú] Alta en el Apple Developer Program.** https://developer.apple.com/programs/enroll/
   Cuesta 99 dolares al ano, se renueva solo, y si caduca la app desaparece de la tienda.

   Hay que elegir tipo de cuenta y **no se puede cambiar despues sin migrar la app**:

   | Tipo | Quien aparece como vendedor | Requisitos | Tiempo |
   |---|---|---|---|
   | Individual | Tu nombre legal, "Baltasar Jimenez" | Solo identidad | Horas o pocos dias |
   | Organizacion | El nombre de la empresa | Numero D-U-N-S y entidad legal | Semanas |

   Para esto, **individual**. Aparecer con tu nombre en la ficha es el precio de no esperar un mes.

2. **[tú] Copiar el Team ID.** https://developer.apple.com/account, apartado *Membership details*.
   Son diez caracteres alfanumericos.

3. **[tú] Pegarlo en el repositorio.** Unica linea que cambia:

   ```
   iosApp/Configuration/Config.xcconfig
   TEAM_ID=XXXXXXXXXX
   ```

   No es un secreto: el Team ID va dentro de cada app publicada y se lee del binario. Se versiona.

---

## Fase B. Identificadores. Aqui esta lo irreversible

> **Decision irreversible.** El bundle identifier de iOS quedo fijado en `com.baltajmn.habit`, el
> mismo que el `applicationId` de Android, y el del widget en `com.baltajmn.habit.widget`. En cuanto
> subas la primera build a App Store Connect ese identificador queda atado a la app para siempre:
> cambiarlo obliga a crear otra ficha desde cero y los usuarios que ya la tuvieran no reciben la
> actualizacion. Si quieres otro, este es el ultimo momento, y hay que cambiarlo en
> `Config.xcconfig` (`APP_BUNDLE_ID`) antes de seguir.

4. **[tú] Registrar los dos App IDs.** https://developer.apple.com/account/resources/identifiers
   *Identifiers*, boton +, tipo *App IDs*, subtipo *App*:
   - `com.baltajmn.habit`, descripcion "Quilt". Marca la capacidad **App Groups**.
   - `com.baltajmn.habit.widget`, descripcion "Quilt Widget". Marca tambien **App Groups**.

5. **[tú] Registrar el App Group.** Misma pantalla, tipo *App Groups*, identificador exacto:

   ```
   group.com.baltajmn.habit
   ```

6. **[tú] Asociar el grupo a los dos App IDs.** Vuelve a cada App ID, edita la capacidad *App
   Groups*, marca `group.com.baltajmn.habit` y guarda. Son dos veces, una por identificador.

   Esto no es opcional ni cosmetico. `iosApp.entitlements` y `HabitWidget.entitlements` ya piden ese
   grupo, y es por donde app y widget se pasan `habits.json`. Si el grupo no existe en el portal, la
   firma falla; si existe pero no esta asociado a los dos, el widget compila, arranca y sale
   **vacio** para siempre, porque `containerURL(forSecurityApplicationGroupIdentifier:)` devuelve
   `nil` y no hay error visible en ningun sitio.

---

## Fase C. Crear la ficha

7. **[tú] Comprobar que el nombre esta libre.** https://appstoreconnect.apple.com, *Apps*, boton +.
   El nombre de la App Store es unico en todo el mundo y se reserva al crear la ficha. "Quilt" es una
   palabra comun del ingles, asi que puede estar cogido. Si lo esta, el nombre visible en iOS puede
   diferir del de Play sin problema tecnico, pero es peor de recordar: prueba antes
   `Quilt Habits` o `Quilt: Habit Tracker`.

8. **[tú] Rellenar la ficha nueva.**
   - Platform: iOS
   - Name: Quilt (o la alternativa)
   - Primary Language: English (U.S.), que es el idioma base del resto de la ficha
   - Bundle ID: `com.baltajmn.habit`, sale del desplegable si la fase B esta hecha
   - SKU: identificador interno tuyo, nunca visible. `quilt-ios-001` vale
   - User Access: Full Access

---

## Fase D. El dinero. Bloquea la compra, no la subida

9. **[tú] Firmar el Paid Applications Agreement.** App Store Connect, *Business*, *Agreements*.
   Hay que rellenar datos bancarios y fiscales. **Hasta que este activo no funciona ninguna compra,
   ni siquiera en Sandbox**, asi que la pantalla Pro se prueba sola en cuanto lo firmes y no antes.

10. **[tú] Crear el producto.** Tu app, *Monetization*, *In-App Purchases*, boton +:
    - Tipo: **Non-Consumable**
    - Reference Name: `Quilt Pro`
    - Product ID: `pro_lifetime`, **exactamente el mismo que en Play**
    - Precio: 4,99 EUR, el mismo escalon que en Play
    - Localizaciones: nombre y descripcion en los cinco idiomas
    - Review Screenshot: una captura de la pantalla Pro, obligatoria

11. **[tú] Conectar RevenueCat.** Dashboard de RevenueCat, proyecto Quilt, *+ New App*, plataforma
    *App Store*. Pide el bundle id y una clave de App Store Connect API del tipo *In-App Purchase*,
    que es la que le deja validar los recibos. Luego anade `pro_lifetime` al mismo *Entitlement*
    `pro` y al mismo *Offering* que ya usa Android, para que el codigo comun no distinga plataforma.

12. **[tú] Pegar la clave publica de iOS.** RevenueCat, *API Keys*, la que empieza por `appl_`:

    ```kotlin
    // shared/src/iosMain/kotlin/com/baltajmn/habit/billing/Billing.ios.kt
    actual val revenueCatApiKey: String? = "appl_..."
    ```

    Esa clave es publica a proposito, viaja dentro del binario y cualquiera puede sacarla: por eso se
    versiona, igual que la `goog_` de Android.

> **Seguridad, sin excepciones.** La clave secreta de RevenueCat (`sk_...`) no entra en este
> repositorio bajo ningun concepto, ni en un fichero, ni en un comentario, ni en un commit que luego
> se enmiende. Lo mismo para el `.p8` de App Store Connect: vive en los secretos de GitHub y en tu
> gestor de contrasenas, nunca en el arbol de trabajo.

Mientras `revenueCatApiKey` siga a `null`, la app arranca y funciona entera, pero la pantalla Pro no
tiene nada que vender. Es un callejon sin salida a proposito, no un fallo.

---

## Fase E. La primera subida, a mano

La primera conviene hacerla desde Xcode aunque el CI ya este montado: es el momento en que Xcode
crea por su cuenta el certificado de distribucion y los dos perfiles de aprovisionamiento, y donde
los errores de firma se ven explicados en vez de como un log rojo de `xcodebuild`.

13. **[tú] Anadir tu Apple ID a Xcode.** *Xcode*, *Settings*, *Accounts*, boton +.

14. **[tú] Comprobar la firma de los dos targets.** Abre `iosApp/iosApp.xcodeproj`. En *Signing &
    Capabilities*, con *Automatically manage signing* marcado y tu equipo elegido, primero en el
    target `iosApp` y despues en `HabitWidgetExtension`. En los dos tiene que aparecer la capacidad
    *App Groups* con `group.com.baltajmn.habit` marcado y sin triangulo de aviso.

15. **[tú] Archivar.** *Product*, *Destination*, **Any iOS Device (arm64)**. Despues *Product*,
    *Archive*. Con el simulador seleccionado la opcion *Archive* aparece en gris, que es la causa
    numero uno de creer que algo se ha roto.

16. **[tú] Subir.** En el Organizer que se abre solo: *Distribute App*, *App Store Connect*,
    *Upload*. Deja marcado subir simbolos, que es lo que hace legibles los informes de fallos.

17. **[tú] Esperar el correo.** Entre cinco y treinta minutos. Si App Store Connect responde algo,
    casi siempre es una de estas tres: falta un icono en un tamano, el `CURRENT_PROJECT_VERSION`
    repite uno ya subido, o falta declarar el cifrado. Las tres estan cubiertas ya: el catalogo de
    iconos esta completo, el numero de build lo pone el CI incrementandose solo, y el `Info.plist`
    declara `ITSAppUsesNonExemptEncryption = false`.

---

## Fase F. Dejar que lo haga el CI

Hecho esto una vez, `.github/workflows/release-ios.yml` repite el proceso entero en cada etiqueta.

18. **[tú] Crear la clave de API.** App Store Connect, *Users and Access*, *Integrations*,
    *App Store Connect API*, pestana *Team Keys*, boton +. Acceso **App Manager**, que es el minimo
    con el que `-allowProvisioningUpdates` puede crear certificados y perfiles por su cuenta.

    El fichero `.p8` **se descarga una sola vez**. Si lo pierdes, se revoca y se crea otro.

19. **[tú] Guardar los cuatro secretos.**

    ```bash
    gh secret set APPSTORE_KEY_ID      --body 'XXXXXXXXXX'
    gh secret set APPSTORE_ISSUER_ID   --body 'xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx'
    gh secret set APPLE_TEAM_ID        --body 'XXXXXXXXXX'
    gh secret set APPSTORE_PRIVATE_KEY < ~/Downloads/AuthKey_XXXXXXXXXX.p8
    ```

    El ultimo lee el fichero entero, con sus lineas `BEGIN` y `END`. Borra la descarga despues.

20. A partir de ahi, una etiqueta publica en las dos tiendas a la vez:

    ```bash
    git tag v1.6 && git push origin v1.6
    ```

    Mientras los secretos no existan, el trabajo de iOS se salta solo y deja un aviso en vez de
    salir rojo. El de Android no cambia.

---

## Fase G. La ficha, antes de poder enviar a revision

21. **[tú] Capturas.** Apple pide dos tamanos, y solo dos:

    | Dispositivo | Resolucion | Cuantas |
    |---|---|---|
    | iPhone 6.9 pulgadas | 1320x2868 o 1290x2796 | de 3 a 10 |
    | iPad 13 pulgadas | 2064x2752 o 2048x2732 | de 3 a 10 |

    Las de iPad son obligatorias porque el proyecto declara `TARGETED_DEVICE_FAMILY = "1,2"`, o sea
    que la app se ofrece tambien en iPad. Si no quieres mantener iPad, cambia ese ajuste a `"1"` en
    `project.pbxproj` y desaparece la fila, pero pierdes el mercado de iPad y volver a activarlo
    despues es una version nueva.

    Se sacan del simulador (iPhone 16 Pro Max y iPad Pro 13"), con Cmd+S, que guarda al tamano
    exacto. Reaprovecha los mismos encuadres de `store/screenshots/` para que las dos tiendas
    cuenten lo mismo.

22. **[tú] Textos.** Reutiliza `store/play-listing.md` y `store/play-listing-en.txt`, pero los
    limites de Apple son otros:

    | Campo | Limite | De donde sale |
    |---|---|---|
    | Name | 30 caracteres | Quilt |
    | Subtitle | 30 caracteres | La frase corta de Play |
    | Promotional text | 170 caracteres, editable sin revision | Lo que quieras destacar |
    | Description | 4000 caracteres | La descripcion larga de Play |
    | Keywords | 100 caracteres, separadas por comas **sin espacios** | Play no tiene equivalente, hay que escribirlas |
    | What's New | 4000 caracteres | `store/release-notes-1.1.txt` |

    Los cinco idiomas: en, es, pt, de, fr. Los mismos que la app.

23. **[tú] Privacy Policy URL.** `https://quilt.baltajmn.dev/`

    **La politica se ha actualizado en este repositorio** para dejar de decir que Google Play es el
    unico procesador de pagos y para aclarar que la tabla de permisos es la de Android. Como la
    pagina publicada es una copia en `BaltaJmn/quilt-privacy`, hay que copiar el
    `store/privacy/index.html` nuevo alli y hacer push, o la URL seguira sirviendo la version vieja.
    Una politica que dice algo distinto de lo que hace la app es motivo de rechazo por si sola.

24. **[tú] Cuestionario de App Privacy.** Es una declaracion jurada, no un formulario: hay que
    contestar lo que hace el codigo. Con lo que hay hoy:

    | Pregunta | Respuesta |
    |---|---|
    | Purchases, Purchase History | Si, se recoge |
    | Identifiers, Device ID | Si, se recoge (el identificador anonimo de RevenueCat) |
    | Ambos: vinculado a la identidad del usuario | **No**. No hay cuentas ni `Purchases.logIn()` |
    | Ambos: usado para seguimiento | **No** |
    | Ambos: para que | App Functionality |
    | Location, Contacts, Health, Diagnostics, Usage Data | **No** |

    Anadir cualquier SDK de analitica o de informes de fallos invalida estas respuestas y obliga a
    rehacer el formulario y la politica.

25. **[tú] Age Rating.** 4+. Sin contenido generado por usuarios, sin publicidad, sin navegador,
    sin apuestas.

26. **[tú] Notas para el revisor.** No hace falta cuenta de demostracion porque la app no tiene
    cuentas. Merece la pena escribir dos lineas: que Quilt Pro es una compra unica no consumible que
    abre habitos ilimitados y la paleta completa, y que todo lo demas es gratis.

27. **Export compliance.** Ya declarado en `Info.plist` (`ITSAppUsesNonExemptEncryption = false`),
    asi que App Store Connect no volvera a preguntarlo en cada build.

---

## Fase H. TestFlight y revision

28. **[tú] Prueba interna.** *TestFlight*, *Internal Testing*, crea un grupo y anade tu Apple ID.
    Es el equivalente de la prueba interna de Play: disponible en minutos y **sin revision**.
    Hasta 100 personas, y todas tienen que estar dadas de alta en *Users and Access*.

29. **[tú] Prueba externa, si la quieres.** Hasta 10.000 personas con un enlace publico, pero pasa
    por *Beta App Review*, que tarda uno o dos dias. Es la unica forma de que prueben personas que
    no esten en tu equipo. iOS no tiene el requisito de los 14 dias con 12 personas que impone Play:
    en iOS puedes ir directo a revision.

30. **[tú] Enviar a revision.** En la version de la ficha, elige la build ya procesada y pulsa
    *Add for Review*. La primera revision suele tardar de 24 a 48 horas. Elige publicacion manual si
    quieres decidir tu el dia de salida, o automatica si te da igual.

---

## Lo que rechaza la revision, y donde queda cubierto

| Regla | Que exige | Estado |
|---|---|---|
| 2.1 App Completeness | Nada a medias ni de relleno | La app esta entera; el paywall es lo unico inerte y solo hasta la fase D |
| 2.3.7 | El nombre de la ficha coincide con lo que hace | Coincide |
| 3.1.1 In-App Purchase | Todo bien digital se vende con StoreKit, sin enlaces de pago externos | RevenueCat usa StoreKit; no hay ningun enlace de pago |
| 4.2 Minimum Functionality | Mas que una pagina web envuelta | Widget, recordatorios, exportacion, cuadricula anual |
| 5.1.1(i) | Politica de privacidad **accesible desde dentro de la app** | Anadido: enlace en la hoja de Ajustes |
| 5.1.2 | Los textos de permiso explican el uso real, en el idioma del usuario | Cinco `InfoPlist.strings`, uno por idioma |
| 5.1.1(v) | No pedir datos que la app no necesita | La app no pide ninguno |

---

## Lo que ya esta comprobado en el codigo

Verificado sobre el `.app` construido, no sobre lo que deberia pasar:

| Comprobacion | Resultado |
|---|---|
| `CFBundleIdentifier` de la app | `com.baltajmn.habit` |
| `CFBundleIdentifier` del widget | `com.baltajmn.habit.widget`, derivado del de la app |
| `CFBundleShortVersionString` | 1.6, en paralelo con Android |
| `ITSAppUsesNonExemptEncryption` | `false` |
| Idiomas dentro del `.app` | `de`, `en`, `es`, `fr`, `pt` |
| Texto del permiso de fotos en espanol | "Para guardar la imagen de tus habitos en tu carrete." |
| `CODE_SIGN_IDENTITY` fijado a "Apple Development" | Eliminado. Fijado impedia exportar para la App Store |
| Esquema compartido para el CI | `xcshareddata/xcschemes/iosApp.xcscheme` versionado |
| Version minima de iOS | 17.0. Antes 18.2, que dejaba fuera todo lo anterior a diciembre de 2024 |
| `:shared:iosSimulatorArm64Test` | Verde |
| Compilacion Release de la app y del widget | Verde |

---

## Lo unico que falta, y solo puedes hacerlo tu

1. `TEAM_ID` en `Config.xcconfig` (fase A)
2. Los dos App IDs y el App Group en el portal (fase B)
3. La clave `appl_` en `Billing.ios.kt` (fase D)
4. Las capturas de iPhone y de iPad (fase G)
5. Copiar la politica de privacidad actualizada al repositorio publicado (fase G)
