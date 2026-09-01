#!/usr/bin/env bash
# Graba un clip del emulador de Android siguiendo un guion de acciones.
#
#   ./grabar.sh 01-un-toque
#
# Lee guiones/<nombre>.txt, deja el emulador en estado de demostracion, ejecuta las
# acciones mientras graba y deja el resultado en grabaciones/<nombre>.mp4.
#
# El estado de partida se restaura siempre desde demo/habits.json, para que dos
# grabaciones del mismo guion salgan identicas. Sin eso, la segunda toma arranca con
# el habito ya marcado por la primera y el clip pierde justo el momento que vende.
#
# Lenguaje del guion, una accion por linea:
#   esperar <segundos>
#   tocar <x> <y>
#   pulsar <x> <y> <ms>        mantener pulsado
#   deslizar <x1> <y1> <x2> <y2> <ms>
#   atras
#   inicio
#   texto <cadena>
#   persiana                   despliega los ajustes rapidos
#   cerrar                     los recoge
#   noche <on|off>             tema oscuro del sistema
#   idioma <es-ES|en-US|...>   idioma de la app, sin tocar el del sistema
#
# El estado de partida sale de demo/habits.json. Un clip que necesite otro conjunto de
# datos lo pide con DATOS:
#
#   DATOS=agua.json ./grabar.sh 04-contador

set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ADB="${ADB:-$HOME/Library/Android/sdk/platform-tools/adb}"
PAQUETE="com.baltajmn.habit"
MAX_SEG="${MAX_SEG:-30}"
DATOS="${DATOS:-habits.json}"
GBOARD="com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME"
VOZ="com.google.android.tts/com.google.android.apps.speech.tts.googletts.settings.asr.voiceime.VoiceInputMethodService"

nombre="${1:-}"
guion="$RAIZ/guiones/${nombre}.txt"
[[ -n "$nombre" && -f "$guion" ]] || { echo "uso: $0 <nombre>   (falta $guion)"; exit 1; }
command -v "$ADB" >/dev/null || { echo "no encuentro adb en $ADB"; exit 1; }
[[ -n "$($ADB devices | sed -n '2p')" ]] || { echo "no hay emulador conectado"; exit 1; }

echo "preparando estado de demostracion"

# Los datos se regeneran en cada toma con la fecha de hoy. Son un historial calculado a
# partir de la fecha y una semilla, no un fichero escrito a mano: si se dejaran fijos, al
# dia siguiente "hoy" ya es otro dia y todos los clips salen con cero habitos hechos.
python3 "$RAIZ/demo/generar.py"
[[ -f "$RAIZ/demo/$DATOS" ]] || { echo "no existe demo/$DATOS"; exit 1; }

# pm clear y no solo force-stop. El SDK de compras guarda su propia copia del derecho en
# las preferencias de la app: sin borrarla, un emulador donde alguna vez se probo la
# compra arranca en Pro por mucho que el fichero pusheado diga que no, y el clip del plan
# gratis sale con la app desbloqueada. Borrar los datos deja tambien un identificador
# anonimo nuevo, que es lo que hace que el derecho no vuelva.
$ADB shell pm clear "$PAQUETE" >/dev/null

# Y sin red mientras se graba. pm clear borra la copia local del derecho de compra, pero
# con red el SDK vuelve a preguntar, la cuenta del emulador tiene la compra de prueba y la
# app se guarda isPro true otra vez: el clip del plan gratis salia con Pro activo. Ningun
# clip necesita red, asi que se corta entera y se devuelve al final.
$ADB shell svc wifi disable
$ADB shell svc data disable
$ADB push "$RAIZ/demo/$DATOS" /data/local/tmp/habits.json >/dev/null
$ADB shell "run-as $PAQUETE mkdir -p files"
$ADB shell "run-as $PAQUETE cp /data/local/tmp/habits.json files/habits.json"
$ADB shell "run-as $PAQUETE rm -f files/habits.json.bak"
$ADB shell pm grant "$PAQUETE" android.permission.POST_NOTIFICATIONS 2>/dev/null || true

# Tema y idioma se devuelven a su valor por defecto en cada toma. Sin esto, el clip que
# graba el modo oscuro deja el emulador de noche y el siguiente sale con el tema
# equivocado sin que nada lo avise.
$ADB shell cmd uimode night no >/dev/null
# Y en vertical. Un emulador que se quedo tumbado graba 1920x1030 y el montaje sale con la
# app dentro de una franja horizontal, sin avisar de nada.
$ADB shell settings put system accelerometer_rotation 0 >/dev/null
$ADB shell settings put system user_rotation 0 >/dev/null
$ADB shell cmd locale set-app-locales "$PAQUETE" --locales es-ES >/dev/null 2>&1 || true
$ADB shell cmd statusbar add-tile "$PAQUETE/com.baltajmn.habit.widget.QuickToggleTileService" >/dev/null 2>&1 || true

# Sin teclado en pantalla. El formulario de habito es una Column sin scroll ni imePadding:
# con el teclado abierto, el boton de crear queda fuera de la pantalla y todas las acciones
# posteriores del guion caen al vacio. Y no vale cerrarlo con atras ni con escape, porque
# los dos cierran la hoja entera. Desactivar el IME es la unica salida que no toca la app:
# `input text` inyecta los eventos por instrumentacion, no por el teclado, asi que el texto
# se escribe igual. Hay que desactivar los dos IME, no solo Gboard: si queda uno habilitado
# el sistema cae a el, y el de voz ocupa media pantalla con "Tap to speak". Desactivar el
# paquete de Gboard con pm tampoco vale, por lo mismo. Para devolver el teclado al emulador
# cuando se termina de grabar: `adb shell ime enable "$GBOARD"`.
$ADB shell ime disable "$GBOARD" >/dev/null 2>&1 || true
$ADB shell ime disable "$VOZ" >/dev/null 2>&1 || true
$ADB shell am force-stop "${GBOARD%%/*}" >/dev/null 2>&1 || true
$ADB shell am force-stop "${VOZ%%/*}" >/dev/null 2>&1 || true

# Barra de estado limpia y fija. Sin esto, cada toma sale con otra hora y con los iconos
# de notificacion del emulador, y dos clips de la misma tanda no encajan.
$ADB shell settings put global sysui_demo_allowed 1 >/dev/null
demo() { $ADB shell am broadcast -a com.android.systemui.demo "$@" >/dev/null; }
demo -e command enter
demo -e command clock -e hhmm 0941
demo -e command battery -e level 100 -e plugged false
demo -e command network -e wifi show -e level 4
demo -e command network -e mobile hide
demo -e command notifications -e visible false

$ADB shell am start -n "$PAQUETE/.MainActivity" >/dev/null
sleep 2.5

echo "grabando $nombre"
$ADB shell screenrecord --bit-rate 12000000 --time-limit "$MAX_SEG" /sdcard/rec.mp4 &
grabador=$!
sleep 1.5   # screenrecord tarda en abrir el codificador

# El guion se lee por el descriptor 3, no por stdin: adb shell hereda stdin y se traga
# las lineas que quedan del fichero, con lo que el guion se corta tras la primera accion.
while read -r accion resto <&3; do
  [[ -z "${accion:-}" || "$accion" == \#* ]] && continue
  # shellcheck disable=SC2086
  case "$accion" in
    esperar)  sleep $resto ;;
    tocar)    $ADB shell input tap $resto ;;
    pulsar)   set -- $resto; $ADB shell input swipe "$1" "$2" "$1" "$2" "$3" ;;
    deslizar) $ADB shell input swipe $resto ;;
    atras)    $ADB shell input keyevent KEYCODE_BACK ;;
    inicio)   $ADB shell input keyevent KEYCODE_HOME ;;
    persiana) $ADB shell cmd statusbar expand-settings ;;
    cerrar)   $ADB shell cmd statusbar collapse ;;
    noche)    $ADB shell cmd uimode night "$([[ $resto == on ]] && echo yes || echo no)" ;;
    idioma)   $ADB shell cmd locale set-app-locales "$PAQUETE" --locales "$resto"
              # La tabla de textos lee el idioma una sola vez, al primer acceso. Cambiar el
              # locale recrea la Activity pero no el proceso, asi que sin reiniciar la app
              # la pantalla se queda en el idioma anterior.
              $ADB shell am force-stop "$PAQUETE"
              $ADB shell am start -n "$PAQUETE/.MainActivity" >/dev/null ;;
    texto)    $ADB shell input text "${resto// /%s}"
              # Escribir despierta a Gboard, que dibuja su barra flotante sobre el
              # formulario aunque el IME este desactivado. Matarlo la quita, y el texto ya
              # esta puesto porque no paso por el.
              $ADB shell am force-stop "${GBOARD%%/*}" ;;
    *)        echo "accion desconocida: $accion" >&2; exit 1 ;;
  esac
done 3< "$guion"

# Se espera a que screenrecord agote su propio --time-limit en vez de matarlo. Matandolo,
# los ultimos segundos se pierden de forma impredecible: dos tomas del mismo guion dieron
# 40,4 s y 33,8 s. Poner MAX_SEG unos segundos por encima de lo que dura el guion cuesta esa
# espera de mas, que luego se recorta en la receta, y a cambio la cola siempre esta.
echo "esperando a que screenrecord agote sus ${MAX_SEG}s"
wait "$grabador" 2>/dev/null || true
sleep 1.5   # el fichero se cierra despues de que el proceso termine

mkdir -p "$RAIZ/grabaciones"
$ADB pull /sdcard/rec.mp4 "$RAIZ/grabaciones/${nombre}.mp4" >/dev/null
$ADB shell rm -f /sdcard/rec.mp4
$ADB shell svc wifi enable
$ADB shell svc data enable

echo "grabaciones/${nombre}.mp4"
ffprobe -v error -show_entries format=duration -of default=nw=1:nk=1 "$RAIZ/grabaciones/${nombre}.mp4"
echo "instantes con cambio de pantalla (para escribir los tiempos de los rotulos):"
ffprobe -v error -select_streams v:0 -show_entries frame=pts_time -of csv=p=0 \
  "$RAIZ/grabaciones/${nombre}.mp4" | awk -F. '!seen[$1]++' | tr '\n' ' '
echo
