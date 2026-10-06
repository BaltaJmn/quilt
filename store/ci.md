# Publicar desde GitHub Actions

Igual en las cuatro apps de la familia: el proceso, los workflows y los scripts viven en
`BaltaJmn/ci` (`README.md` y `PUBLICAR.md`). Aquí, solo lo que es de Quilt.

| Qué | Valor |
|---|---|
| Paquete de Play y bundle id de Apple | `com.baltajmn.habit` |
| Clave de subida | `~/keys/quilt-upload.jks`, alias `upload`, `CN=Baltasar` (el `signer-cn` de `release.yml`) |
| Versión de las dos tiendas | `versionName` y `versionCode` de `androidApp/build.gradle.kts` |
| Web: contacto de Play, soporte y privacidad de Apple | https://quilt.baltajmn.dev/ |
| Pro | `pro_lifetime` en Play, `com.baltajmn.habit.pro_lifetime` en Apple; 1,99 EUR en las dos |

Ya está en producción en Play: la etiqueta va a `alpha` como en las demás, y se promociona a producción desde la consola.

## Publicar una versión

1. Subir el `versionCode` (y `versionName` si toca). Un `versionCode` no se reutiliza nunca.
2. Si cambia algo visible, reescribir `store/whatsnew/whatsnew-<idioma>` en todos los idiomas (tope 500).
3. Commit, y `git tag vX.Y && git push origin vX.Y`: Play `alpha` y TestFlight, la misma versión.
4. Producción en Play y el envío a revisión de Apple, a mano en cada consola.

Otro canal o una sola tienda: `gh workflow run release.yml -f stores=play -f track=internal`
(`-f status=draft` mientras la app no haya publicado nada en Play).

## Ficha

Textos, gráficos y capturas en `store/`, con la estructura del README de `ci`. Cada push a `store/`
la comprueba; para subirla, `gh workflow run listings.yml -f target=play` (o `app-store`, `both`).
La App Store solo acepta cambios con una versión en preparación.

## Secretos del repositorio

Los cinco de Play salen de la clave de esta app y de la cuenta de servicio que publica. Se ponen sin
que el valor pase por la pantalla:

```bash
base64 -i ~/keys/quilt-upload.jks | gh secret set KEYSTORE_BASE64 -R BaltaJmn/quilt
sed -n 's/^storePassword=//p' keystore.properties | tr -d '\n' | gh secret set KEYSTORE_PASSWORD -R BaltaJmn/quilt
sed -n 's/^keyPassword=//p' keystore.properties | tr -d '\n' | gh secret set KEY_PASSWORD -R BaltaJmn/quilt
printf upload | gh secret set KEY_ALIAS -R BaltaJmn/quilt
gh secret set PLAY_SERVICE_ACCOUNT_JSON -R BaltaJmn/quilt < ~/keys/play-service-account.json
```

Los ocho de Apple (`APPSTORE_*`, `APPLE_*`) son de cuenta, iguales en las cuatro apps: los pone
`~/keys/credenciales.sh sincronizar`, que también renueva `PLAY_SERVICE_ACCOUNT_JSON` al rotarla.
