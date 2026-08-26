# Publicar desde GitHub Actions

`.github/workflows/release.yml` compila el AAB firmado y lo sube al canal de prueba interna.

## Cómo se dispara

**Por etiqueta**, no en cada push a `main`. Cada subida quema un `versionCode` y le llega a los
probadores; hacerlo en cada commit es ruido y, durante el test cerrado, ruido que molesta a doce
personas.

```bash
git tag v1.2 && git push origin v1.2
```

También hay disparo manual desde la pestaña *Actions*, con un desplegable para elegir canal
(`internal`, `alpha`, `beta`, `production`).

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

## Lo que no hay, y por qué

No hay CI de tests en cada push. `:shared:iosSimulatorArm64Test` no enlaza en esta máquina
(`swiftCompatibility56`, ver `lanzamiento.md`), así que un workflow que compile las dos plataformas
saldría rojo desde el primer día. Cuando se arregle el `xcode-select`, es añadir un fichero.
