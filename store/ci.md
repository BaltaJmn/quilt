# Publicar desde GitHub Actions

`.github/workflows/release.yml` compila el AAB firmado y lo sube al canal de prueba cerrada.

## Cómo se dispara

**Por etiqueta**, no en cada push a `main`. Cada subida quema un `versionCode` y le llega a los
probadores; hacerlo en cada commit es ruido y, durante el test cerrado, ruido que molesta a doce
personas.

```bash
git tag v1.2 && git push origin v1.2
```

También hay disparo manual desde la pestaña *Actions*, con un desplegable para elegir canal
(`alpha`, `internal`, `beta`, `production`). Sin elegir nada, la etiqueta va a `alpha`, que es la
prueba cerrada.

## La ficha de tienda

`.github/workflows/listings.yml` sube los cinco idiomas de golpe con la API de Android Publisher,
desde *Actions*, a mano. Los textos son [`store/listings/<idioma>/`](listings), tres ficheros por
idioma. Usa el mismo `PLAY_SERVICE_ACCOUNT_JSON`, pero la cuenta necesita ademas el permiso de ficha
de Play Store, que el de publicar versiones no incluye.

Un push que toque `store/listings/**` solo comprueba los limites de caracteres. No escribe en Play.

## Lo que el workflow no hace

**No sube el `versionCode`.** Sigue en `androidApp/build.gradle.kts` y lo cambias tú antes de
etiquetar. Es a propósito: una sola fuente de verdad, y quien decide publicar es quien decide el
número. Si se te olvida, Play rechaza la subida con "Version code 2 has already been used".

**No escribe las notas de la versión.** Se siguen pegando a mano en la consola desde
`release-notes-<versión>.txt`. Automatizarlo pide un fichero por idioma con un nombre concreto, y
mientras las notas se escriban a mano para cada versión no compensa.

## Secretos que hay que crear

En el repositorio: *Settings → Secrets and variables → Actions → New repository secret*.

| Secreto | Qué es |
|---|---|
| `KEYSTORE_BASE64` | El `.jks` de subida, en base64 |
| `KEYSTORE_PASSWORD` | La del almacén |
| `KEY_ALIAS` | `upload` |
| `KEY_PASSWORD` | La de la clave |
| `PLAY_SERVICE_ACCOUNT_JSON` | El JSON de una cuenta de servicio **distinta** de la de RevenueCat |

Para el primero:

```bash
base64 -i ~/keys/quilt-upload.jks | pbcopy
```

Y lo pegas en el formulario de GitHub. No lo dejes en un fichero del repositorio ni en un chat.

### La cuenta de servicio para publicar

**No reutilices la de RevenueCat.** Aquella se creó a propósito sin permisos de publicación: solo
lee pedidos. Esta necesita publicar, y son dos poderes que no deben vivir en la misma credencial.

Se crea igual que la otra (Google Cloud → cuenta de servicio → clave JSON), y en Play Console
*Usuarios y permisos → Invitar usuario* se le dan permisos sobre Quilt para **publicar en canales
de prueba**. Nada de datos financieros: no los necesita.

## Qué hace, en orden

1. Compila con JDK 17 y reconstruye `keystore.properties` desde los secretos.
2. Corre los tests de `shared` y genera el bundle.
3. **Comprueba que el AAB va firmado con tu clave de subida**, no con la de debug. El build cae a
   la clave de debug en silencio si falta `keystore.properties`, y Play no te lo dice hasta después
   de subirlo.
4. **Borra la clave del disco antes** de ejecutar la acción de terceros que sube a Play.
5. Sube al canal, con el estado `completed`.

## Los otros dos workflows

`.github/workflows/tests.yml` corre en cada push a `main` y en cada pull request. Dos trabajos:
`:shared:testAndroidHostTest` sobre Ubuntu, que es el rápido, y `:shared:iosSimulatorArm64Test`
sobre macOS, que es lo único que demuestra que `iosMain` sigue compilando y enlazando. El de macOS
cuesta diez veces más por minuto, y por eso el disparador no incluye ramas sueltas.

`.github/workflows/release-ios.yml` se dispara con la misma etiqueta `v*`, así que una etiqueta
publica en las dos tiendas. Archiva, exporta y sube a TestFlight en un solo `xcodebuild`
(`destination: upload` en el `ExportOptions.plist`, que evita tener que pasar el ipa por `altool`).

Mientras no existan los secretos de Apple el trabajo se salta solo y deja un aviso, en vez de salir
rojo en cada etiqueta y acostumbrarte a ignorar la marca roja de al lado, que sí importa.

El número de build de iOS lo pone el workflow desde `github.run_number`, no `Config.xcconfig`. App
Store Connect solo exige que suba, y así no hay que acordarse.

### Secretos de Apple

| Secreto | Qué es |
|---|---|
| `APPSTORE_KEY_ID` | El Key ID de la clave de la App Store Connect API |
| `APPSTORE_ISSUER_ID` | El Issuer ID, el mismo para todas las claves de la cuenta |
| `APPSTORE_PRIVATE_KEY` | El contenido del `.p8`, entero, con sus líneas `BEGIN`/`END` |
| `APPLE_TEAM_ID` | El Team ID de la cuenta de desarrollador |

La clave `.p8` se descarga **una sola vez** desde App Store Connect. Si se pierde, se revoca y se
crea otra. Necesita rol *App Manager* o superior para que `-allowProvisioningUpdates` pueda crear
el certificado y los perfiles por su cuenta: es lo que evita tener que meter un `.p12` en un
secreto.
