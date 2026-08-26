# Política de privacidad de Quilt

`index.html` es la política, en inglés y español en la misma página. Un solo fichero, sin
dependencias externas: se abre igual en local que alojado.

## Dónde está publicada

**https://baltajmn.github.io/quilt-privacy/**

El correo de contacto que aparece en la página es `baltajmn@gmail.com`. Tiene que ser el mismo
que pongas de contacto en la ficha de Play, o los revisores lo marcan como incoherencia.

Servida por GitHub Pages desde el repositorio público `BaltaJmn/quilt-privacy`. Ese repositorio es
una **copia**: el original es este fichero. Para cambiarla, edita aquí y copia allí, o los dos
divergen y gana el publicado.

No se metió en `bybalta` a propósito, aunque ya tenga Pages montado: su README avisa de que un
proceso automático lo reescribe, y una URL de la que depende la publicación en Play no puede vivir
donde un script puede borrarla.

## Cómo se montó, por si hay que repetirlo

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
