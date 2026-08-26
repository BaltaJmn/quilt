# Política de privacidad de Quilt

`index.html` es la política, en inglés y español en la misma página. Un solo fichero, sin
dependencias externas: se abre igual en local que alojado.

## Antes de publicarla

Sustituye las seis apariciones de `CONTACT_EMAIL` por el correo que quieras dar como público. Va a
ser visible para cualquiera, así que decide si usas tu correo personal o uno dedicado:

```bash
sed -i '' 's/CONTACT_EMAIL/tu@correo.com/g' store/privacy/index.html
```

Ese correo debería ser el mismo que pongas como contacto en la ficha de Play, o los revisores lo
marcan como incoherencia.

## Alojarla gratis con GitHub Pages

1. Crea un repositorio **público** nuevo, por ejemplo `quilt-privacy`.
2. Sube `index.html` a la raíz.
3. En el repositorio: *Settings → Pages → Build and deployment → Deploy from a branch*, rama `main`,
   carpeta `/ (root)`.
4. A los pocos minutos queda en `https://<tu-usuario>.github.io/quilt-privacy/`.

Esa URL es la que va en Play Console, en *Contenido de la aplicación → Política de privacidad*.
Tiene HTTPS, es pública y no pide iniciar sesión, que es exactamente lo que Play exige.

Si ya tienes dominio propio por tus otras apps, mejor ahí: `tudominio.com/quilt/privacidad`. Un
dominio tuyo sobrevive a que GitHub cambie de política; una URL de `github.io`, no del todo.

## Qué dice, y por qué dice eso

Está escrita contra lo que hace el código, no contra una plantilla. Los hechos que sostiene, todos
verificados:

- Permisos reales del APK de release: `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, `WAKE_LOCK`,
  `FOREGROUND_SERVICE`, `INTERNET`, `ACCESS_NETWORK_STATE`, `com.android.vending.BILLING`. Ni
  ubicación, ni contactos, ni cámara, ni almacenamiento.
- RevenueCat se configura sin `appUserID` (`Billing.kt`), así que el identificador es anónimo.
- Cero SDK de analítica, publicidad o informes de fallos en el árbol de dependencias.
- Guardar en la galería usa MediaStore y solo escribe; por debajo de Android 10 ni siquiera lo
  intenta, cae a la hoja de compartir (`Sharing.android.kt`).

**Si cambia cualquiera de esas cosas, esta política deja de ser cierta.** Añadir una analítica, un
`Purchases.logIn()` o un permiso nuevo obliga a tocarla y a rehacer el formulario de Data Safety.

## Aviso

Esto no es asesoramiento legal. Es un documento honesto escrito sobre hechos comprobados del código,
que para una app sin cuentas y sin servidor cubre lo que Play pide. Si algún día hay ingresos que lo
justifiquen, que lo revise alguien con título.
