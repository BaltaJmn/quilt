# Lanzamiento, paso a paso

Marcado **[tú]** lo que solo puedes hacer tú (cuentas, contraseñas, formularios, subidas) y
**[yo]** lo que te puedo dejar hecho.

Regla general: **las funcionalidades no acercan la fecha de salida.** Lo que la marca son los
trámites y el test cerrado de 14 días. Todo lo que sea código cabe dentro de ese hueco muerto.

---

## Fase 0 — Decisiones que bloquean el resto

- [x] ~~**El nombre de la tienda.**~~ **Quilt**, aplicado en código y ficha. El `applicationId`
      (`com.baltajmn.habit`) se queda como está: es irreversible tras la primera subida y el nombre
      visible es independiente. Pendiente solo comprobar el registro de marca (clase 9).
- [x] ~~**El paywall.**~~ Decidido y aplicado: Pro abre **hábitos ilimitados** (gratis 3) y la
      **paleta completa** (gratis 4 de 8). Widgets, recordatorios, exportación y la cuadrícula anual
      entera son gratis para siempre. `pitch` reescrito en los cinco idiomas y el `TODO` fuera.
      Detalle en `revenuecat.md` §0.
- [x] ~~**El precio de Pro.**~~ **4,99 €** de base, conversión automática con redondeo, todos los
      países, sin prueba gratuita.

## Deudas conocidas antes de publicar

- [ ] **[yo] El texto del permiso de fotos en iOS está solo en español**
      (`NSPhotoLibraryAddUsageDescription`). Un usuario alemán o francés verá castellano al guardar
      una imagen. Se arregla con un `InfoPlist.strings` por idioma dentro de carpetas `.lproj`, lo
      que además obliga a tocar el proyecto de Xcode.
- [x] ~~**El paywall promete lo que no limita.**~~ Resuelto en Fase 0.
- [ ] **[yo] `:shared:iosSimulatorArm64Test` no enlaza en esta máquina.** El linker busca
      `swiftCompatibility56` para RevenueCat y no lo encuentra; en el log aparecen dos rutas de Xcode
      distintas (`Xcode.app` y `Xcode-16.4.app`), así que huele a `xcode-select` apuntando a una
      instalación y las herramientas a otra. Los 27 tests de `:shared:testAndroidHostTest` sí pasan,
      y la app compila y arranca en el simulador. Hay que arreglarlo antes de montar CI.

## Fase 1 — Infraestructura

- [x] ~~**[yo] `git init` y repo en GitHub.**~~ `BaltaJmn/quilt`, privado.
- [x] ~~**[yo] Publicar desde CI.**~~ `.github/workflows/release.yml`: etiqueta `v*` compila,
      verifica la firma y sube a prueba interna. Pasos y secretos en `ci.md`.
- [ ] **[yo] CI de tests en cada push.** Bloqueado por el `xcode-select`: compilar las dos
      plataformas saldría rojo desde el primer día.
- [x] **[tú] Crear el keystore de subida.** `~/keys/quilt-upload.jks`, alias `upload`, RSA 2048,
      válido hasta enero de 2054. Huella SHA-256 del certificado:
      `20:85:91:D4:76:99:6C:FC:3A:52:6C:C0:A0:A3:B1:8B:C7:C1:D0:32:B7:9B:87:E7:11:BC:07:9D:E3:BD:75:D5`.
      Play te enseñará esta misma huella al subir el primer AAB; si no coincide, es otro fichero.
      Comando original, por si algún día hay que rehacerlo:
      ```bash
      keytool -genkeypair -v -keystore ~/keys/quilt-upload.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
      ```
      Las credenciales viven en `keystore.properties` en la raíz, git-ignorado, con `storeFile`,
      `storePassword`, `keyAlias` y `keyPassword`. **El `.jks` necesita copia de seguridad fuera de
      este Mac**: es una clave de subida, así que Google puede resetearla abriendo un caso de soporte,
      pero eso tarda días y bloquea las actualizaciones mientras tanto.

      `keytool -list -v` peta con `MissingFormatArgumentException: Format specifier '%2$s'`. Es un fallo
      de la traducción al español del propio JDK, no del keystore. Añade `-J-Duser.language=en
      -J-Duser.country=US` y sale bien.
- [x] **[yo] Generar el AAB firmado** — `androidApp/build/outputs/bundle/release/androidApp-release.aab`,
      7,1 MB, versionCode 1, versionName 1.0, firmado con la clave de subida. Se regenera con
      `./gradlew :androidApp:bundleRelease`.
- [x] **[tú] Publicar la política de privacidad** — **https://quilt.baltajmn.dev/**
      Escrita a medida contra lo que hace el código, en inglés y español, alojada en GitHub Pages
      desde el repositorio público `BaltaJmn/quilt-privacy`. El original vive en
      `store/privacy/index.html`; el repositorio público es una copia, así que un cambio hay que
      hacerlo aquí y copiarlo allí.

## Avisos de Play que se ignoran a propósito

**"Este App Bundle contiene código nativo, pero no has subido símbolos de depuración."**
No bloquea la publicación y no se puede arreglar desde aquí. El único código nativo del bundle es
`libandroidx.graphics.path.so` (10 KB, cuatro arquitecturas), que entra de rebote con Compose. El `.so`
que publica AndroidX ya viene *stripped*:

```
ELF 64-bit LSB shared object, ARM aarch64, ..., stripped
```

`ndk { debugSymbolLevel = "SYMBOL_TABLE" }` en el `buildType` de release no cambia nada — AGP solo
puede recoger símbolos que existan en el `.so`, y ahí no hay ninguno. Se probó y el AAB salió igual,
sin `BUNDLE-METADATA/.../nativeDebugMetadata`, así que se quitó la línea en vez de dejar configuración
que no hace nada. El día que metamos código nativo propio, se añade entonces.

## Fase 2 — Google Play Console

- [ ] **[tú] Crear la app** en Play Console con el nombre elegido.
- [ ] **[tú] Subir el primer AAB a un canal de prueba interna.** Manual y obligatorio: la Play
      Developer API no se puede usar hasta que hayas subido un binario a mano. Esto desbloquea
      todo lo automático de después.
- [ ] **[tú] Rellenar la ficha** con los textos de `play-listing.md`. Empieza por español e inglés.
- [x] **[yo] Capturas** en español e inglés, `store/screenshots/es/` y `store/screenshots/en/`.
      Sacadas con `adb` y el modo demo de SystemUI, así que la próxima tanda sale idéntica.
- [x] **[tú o yo] Gráfico de funciones 1024×500** — `store/graphics/feature-es.png` y `-en.png`.
- [ ] **[tú] Subir el vídeo a YouTube** (`store/graphics/quilt-flow-es.mp4`, `-en.mp4`) como *oculto*
      y pegar el enlace en la ficha. Play no acepta el fichero, solo la URL.
- [ ] **[tú] Data Safety.** Borrador en `play-listing.md`; contrástalo con la guía de RevenueCat.
- [ ] **[tú] Clasificación de contenido** (cuestionario IARC) y **público objetivo** 13+.
- [ ] **[tú] Crear el producto de compra** `pro_lifetime`, 4,99 €. Textos y pasos en
      `revenuecat.md` §1. Requiere el AAB ya subido y el perfil de pagos verificado.

## Fase 3 — RevenueCat

- [x] ~~**[tú] Crear el proyecto** y conectarlo a Play.~~ Cuenta de servicio creada en Google Cloud
      e invitada en *Usuarios y permisos*. Credenciales subidas.
- [x] ~~**[tú] Crear el derecho `pro`**, la oferta y asociarle el producto.~~ Derecho `pro` con
      `pro_lifetime` adjunto; oferta `default` con un paquete *Lifetime* (`$rc_lifetime`).
- [x] ~~**[tú] Pasarme la clave pública de Android.**~~ En `Billing.android.kt`. Es pública: viaja
      dentro del APK.
- [x] ~~**[yo] Pegarla y comprobar que arranca.**~~ El SDK configura y la app degrada bien: cuando
      no hay tienda enseña "La tienda no está disponible ahora mismo" y deja el botón apagado, sin
      romperse.
- [ ] **[tú] Confirmar el correo de RevenueCat.** Sigue sin confirmar.
- [ ] **[tú] Comprobar que la oferta `default` está marcada como *Current*.** Si no lo está, el
      paywall sale sin precio aunque todo lo demás esté bien.
- [ ] **[tú] Probar una compra real.** **No se puede en el emulador**: la imagen no trae Play
      Billing y el SDK devuelve `BILLING_UNAVAILABLE`. Hace falta un móvil de verdad, con la app
      instalada **desde el canal de prueba interna** y la cuenta añadida en *Ajustes → Monetización
      → Licencia para testing*. Es donde salen los fallos que importan.

## Fase 4 — El test cerrado (el camino crítico)

- [ ] **[tú] Reunir 12 probadores.** Recluta **16**, no 12: siempre se cae alguno y bajar de 12 un
      solo día reinicia el contador de 14. Cómo conseguirlos, el mensaje para mandarles y las
      trampas: `store/testers.md`.
- [ ] **[tú] Arrancar el test cerrado.** **Hazlo lo antes posible**: son 14 días seguidos y es el
      plazo más largo de todo el proyecto. Arráncalo mientras se hace el resto, no después.
- [ ] **[tú] Solicitar acceso a producción** al terminar los 14 días.
- [ ] **[tú] Publicar.**

## Fase 5 — Apple (en paralelo, empieza ya)

- [ ] **[tú] Abrir la cuenta de Apple Developer**, 99 $/año. La verificación tarda. Es lo único
      que no se puede acelerar después, así que ábrela hoy aunque iOS vaya detrás.
- [ ] **[tú] Contratos, fiscalidad y datos bancarios** en App Store Connect. Sin esto no se puede
      vender nada.
- [ ] **[tú] Crear la app** y el producto de compra.
- [ ] **[tú] Pasarme la clave pública de iOS** (`appl_...`).
- [ ] **[tú] Certificados y perfiles**, o dejar que Xcode los gestione.
- [ ] **[yo] Subir la primera build a TestFlight** una vez exista la cuenta.
- [ ] **[tú] Probar compras en el sandbox de Apple.**

---

## Lo que queda de producto, y mi recomendación

| Función | Estado | ¿En 1.0? |
|---|---|---|
| Todo el MVP | Hecho | — |
| Días saltados, racha máxima, archivar, contador de cantidad, exportar JSON | Hecho | — |
| Onboarding | Falta | **Sí.** Primera impresión, 1 jornada |
| Vista previa del widget en el selector de Android | Falta | **Sí.** Sin ella sale una caja gris |
| Reordenar hábitos | Falta | **Sí.** Con 5 hábitos gratis, querrás el importante arriba. Medio día |
| Exportar CSV | Falta | **Sí.** Una hora, y refuerza el argumento de "tus datos son tuyos" |
| Hábitos negativos ("no fumar") | Falta | **No.** Barato de construir, caro de acertar: hay que decidir qué significa racha y día saltado |
| Temporizador | Falta | **No.** Servicio en primer plano, notificación persistente y recuperación tras matar la app. El contador ya cubre "30 min" |
| Notas por día | Falta | **No.** v1.2 |
| Sincronización iCloud / Drive | Falta | **No.** v1.3 |
| Apple Health / Health Connect | Falta | **No.** v1.4 |
| Pulido en móvil real | Falta | **Sí.** Nada se ha probado en hardware. 3 jornadas y es la parte con más varianza |
