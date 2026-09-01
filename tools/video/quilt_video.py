#!/usr/bin/env python3
"""Monta un vertical de TikTok a partir de una grabacion de pantalla y una receta JSON.

El ffmpeg de Homebrew de esta maquina viene compilado sin freetype, asi que no tiene
drawtext ni el filtro subtitles: no hay forma de quemar texto con ffmpeg solo. La salida
es dibujar cada rotulo con Pillow a un PNG con transparencia y superponerlo con overlay,
que si esta disponible. Cuesta lo mismo y ademas da control tipografico real, que
drawtext no tiene.

Uso:
    quilt_video.py recetas/01-un-toque.json
    quilt_video.py --autocheck        # comprueba el montaje sin necesitar grabacion
"""

from __future__ import annotations

import json
import math
import shutil
import subprocess
import sys
import tempfile
from dataclasses import dataclass, field
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

RAIZ = Path(__file__).resolve().parent

# Paleta de Theme.kt. Si cambia alli, cambia aqui: los rotulos tienen que parecer
# parte de la app, no una capa pegada encima.
CREMA = (251, 248, 243)
TINTA = (57, 53, 46)
SALVIA = (111, 174, 155)
ACENTOS = {
    "rosa": (240, 175, 190),
    "melocoton": (245, 195, 155),
    "mantequilla": (237, 220, 152),
    "salvia": (182, 214, 171),
    "menta": (156, 211, 199),
    "cielo": (162, 195, 233),
    "lavanda": (180, 184, 236),
    "lila": (217, 175, 230),
}

FUENTES = [
    "/System/Library/Fonts/SFCompactRounded.ttf",
    "/System/Library/Fonts/SFNS.ttf",
    "/System/Library/Fonts/Supplemental/Avenir Next.ttc",
    "/System/Library/Fonts/Helvetica.ttc",
]

ANCHO, ALTO = 1080, 1920

# TikTok tapa la parte de abajo con la descripcion y los botones, y la derecha con la
# columna de acciones. Un rotulo fuera de esta caja lo pisa la interfaz.
MARGEN_ARRIBA = 200
MARGEN_ABAJO = 520
MARGEN_LADO = 70

FUNDIDO = 0.28

# Cabecera y cierre de marca. Van generados, no grabados: son la misma imagen en los diez
# clips, que es lo que hace que se reconozcan como una serie al pasar por el feed.
INTRO_SEG = 1.7
CIERRE_SEG = 2.8
MARCA = "Quilt"
INTRO_SUB = "Tu año entero, un cuadrito por día"
CIERRE_SUB = "Gratis en Google Play y App Store"


def fuente(tam: int, peso: int = 0) -> ImageFont.FreeTypeFont:
    for ruta in FUENTES:
        if Path(ruta).exists():
            try:
                f = ImageFont.truetype(ruta, tam)
                # Las .ttc traen varios cortes; el indice 0 es el regular y suele haber
                # un semibold mas arriba. Se pide por variacion cuando la fuente la trae.
                if peso:
                    try:
                        f.set_variation_by_axes([peso])
                    except (OSError, AttributeError):
                        pass
                return f
            except OSError:
                continue
    raise SystemExit("No hay ninguna fuente utilizable en el sistema")


@dataclass
class Rotulo:
    texto: str
    desde: float
    hasta: float
    pos: str = "abajo"
    tam: int = 64
    acento: str | None = None
    y: int | None = None
    png: Path | None = field(default=None, repr=False)
    caja: tuple[int, int] = field(default=(0, 0), repr=False)


def envolver(texto: str, f: ImageFont.FreeTypeFont, ancho_max: int) -> list[str]:
    """Parte por palabras respetando los saltos que ya trae el texto."""
    lineas: list[str] = []
    for parrafo in texto.split("\n"):
        actual = ""
        for palabra in parrafo.split():
            prueba = f"{actual} {palabra}".strip()
            if f.getbbox(prueba)[2] <= ancho_max or not actual:
                actual = prueba
            else:
                lineas.append(actual)
                actual = palabra
        lineas.append(actual)
    return lineas


def dibujar(rot: Rotulo, destino: Path) -> tuple[int, int]:
    """Pinta un rotulo con fondo de pastilla y lo guarda con transparencia."""
    f = fuente(rot.tam)
    ancho_texto_max = ANCHO - 2 * MARGEN_LADO - 2 * 44
    lineas = envolver(rot.texto, f, ancho_texto_max)

    alto_linea = int(rot.tam * 1.26)
    anchos = [f.getbbox(l)[2] - f.getbbox(l)[0] for l in lineas]
    ancho_texto = max(anchos) if anchos else 0

    pad_x, pad_y = 44, 32
    barra = 8 if rot.acento else 0
    w = ancho_texto + 2 * pad_x + barra
    h = alto_linea * len(lineas) + 2 * pad_y

    # Margen extra para que la sombra no se corte en el borde del PNG.
    halo = 28
    img = Image.new("RGBA", (w + 2 * halo, h + 2 * halo), (0, 0, 0, 0))

    sombra = Image.new("RGBA", img.size, (0, 0, 0, 0))
    ImageDraw.Draw(sombra).rounded_rectangle(
        (halo, halo + 6, halo + w, halo + h + 6), radius=30, fill=(20, 18, 15, 70)
    )
    from PIL import ImageFilter

    img.alpha_composite(sombra.filter(ImageFilter.GaussianBlur(14)))

    d = ImageDraw.Draw(img)
    d.rounded_rectangle((halo, halo, halo + w, halo + h), radius=30, fill=(*TINTA, 240))

    if rot.acento:
        color = ACENTOS.get(rot.acento, SALVIA)
        d.rounded_rectangle(
            (halo + 22, halo + pad_y, halo + 22 + barra, halo + h - pad_y),
            radius=barra // 2,
            fill=(*color, 255),
        )

    x0 = halo + pad_x + barra
    for i, linea in enumerate(lineas):
        d.text((x0, halo + pad_y + i * alto_linea), linea, font=f, fill=(*CREMA, 255))

    img.save(destino)
    return img.size


def marca(lado: int) -> Image.Image:
    """El icono de la app dibujado, no cargado: cuatro columnas que suben 1-2-3-4.

    Se redibuja aqui en vez de leer el PNG de `androidApp/src/main/res` porque el icono de
    la tienda va a sangre y sin esquinas, y una cabecera necesita la forma redondeada que
    aplica el sistema. La geometria es la misma de tools/generate_icons.py.
    """
    img = Image.new("RGBA", (lado, lado), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle((0, 0, lado - 1, lado - 1), radius=int(lado * 0.22), fill=(44, 40, 32, 255))

    hueco = max(2, int(lado * 0.046))
    celda = int((lado * 0.46 - 3 * hueco) / 4)
    rejilla = celda * 4 + hueco * 3
    x0 = (lado - rejilla) // 2
    y0 = (lado - rejilla) // 2
    colores = [ACENTOS["rosa"], ACENTOS["melocoton"], ACENTOS["salvia"], ACENTOS["menta"]]
    for columna in range(4):
        for fila in range(columna + 1):
            x = x0 + columna * (celda + hueco)
            y = y0 + rejilla - (fila + 1) * celda - fila * hueco
            d.rounded_rectangle(
                (x, y, x + celda, y + celda),
                radius=max(2, celda // 4),
                fill=(*colores[columna], 255),
            )
    return img


def tarjeta(sub: str, destino: Path) -> tuple[int, int]:
    """Icono, nombre y una linea, sobre transparencia. El fondo crema lo pone el video."""
    lado = 250
    f_nombre = fuente(140)
    f_sub = fuente(54)
    lineas = envolver(sub, f_sub, ANCHO - 2 * MARGEN_LADO - 40)

    hueco_icono, hueco_sub = 54, 26
    alto_nombre = f_nombre.getbbox(MARCA)[3] - f_nombre.getbbox(MARCA)[1]
    alto_linea = int(54 * 1.3)
    alto = lado + hueco_icono + alto_nombre + hueco_sub + alto_linea * len(lineas)
    ancho = max(
        [lado, f_nombre.getbbox(MARCA)[2]] + [f_sub.getbbox(l)[2] for l in lineas]
    ) + 8

    img = Image.new("RGBA", (ancho, alto), (0, 0, 0, 0))
    img.alpha_composite(marca(lado), ((ancho - lado) // 2, 0))

    d = ImageDraw.Draw(img)
    y = lado + hueco_icono
    d.text(((ancho - f_nombre.getbbox(MARCA)[2]) // 2, y - f_nombre.getbbox(MARCA)[1]),
           MARCA, font=f_nombre, fill=(*TINTA, 255))
    y += alto_nombre + hueco_sub
    for linea in lineas:
        d.text(((ancho - f_sub.getbbox(linea)[2]) // 2, y), linea, font=f_sub,
               fill=(*TINTA, 200))
        y += alto_linea

    img.save(destino)
    return img.size


def coloca(rot: Rotulo) -> tuple[int, int]:
    w, h = rot.caja
    x = (ANCHO - w) // 2
    if rot.y is not None:
        return x, rot.y
    if rot.pos == "arriba":
        y = MARGEN_ARRIBA
    elif rot.pos == "centro":
        y = (ALTO - h) // 2
    else:
        y = ALTO - MARGEN_ABAJO - h
    return x, y


def tramos_de(receta: dict) -> list[dict]:
    """Normaliza la receta a una lista de tramos.

    Un vertical casi nunca se deja encuadrar con un solo recorte: el panel de ajustes de
    Quilt vive por debajo de los 1920 px que caben, asi que ese trozo necesita bajar la
    ventana. La forma corta (recorte + encuadre sueltos) es un tramo unico.
    """
    if "tramos" in receta:
        return receta["tramos"]
    corte = receta.get("recorte", {})
    return [{
        "desde": corte.get("desde", 0),
        "hasta": corte.get("hasta"),
        "encuadre": receta.get("encuadre", {"x": 0, "y": 0, "w": ANCHO, "h": ALTO}),
    }]


def construye(receta: dict, tmp: Path) -> list[str]:
    fuente_video = (RAIZ / receta["fuente"]).resolve()
    if not fuente_video.exists():
        raise SystemExit(f"No existe la grabacion: {fuente_video}")

    tramos = tramos_de(receta)
    total = 0.0
    reloj = []
    for t in tramos:
        reloj.append(total)
        total += float(t["hasta"]) - float(t["desde"])

    rotulos = [
        Rotulo(
            texto=r["texto"],
            desde=float(r["t"][0]),
            hasta=float(r["t"][1]),
            pos=r.get("pos", "abajo"),
            tam=int(r.get("tam", 64)),
            acento=r.get("acento"),
            y=r.get("y"),
        )
        for r in receta.get("rotulos", [])
    ]
    for i, rot in enumerate(rotulos):
        rot.png = tmp / f"rot{i}.png"
        rot.caja = dibujar(rot, rot.png)

    intro_sub = receta.get("intro", {}).get("sub", INTRO_SUB)
    cierre_sub = receta.get("cierre", {}).get("sub", CIERRE_SUB)
    png_intro, png_cierre = tmp / "intro.png", tmp / "cierre.png"
    caja_intro = tarjeta(intro_sub, png_intro)
    caja_cierre = tarjeta(cierre_sub, png_cierre)

    cmd = ["ffmpeg", "-y", "-v", "error", "-i", str(fuente_video)]
    for rot in rotulos:
        cmd += ["-loop", "1", "-framerate", "30", "-t", f"{total:.3f}", "-i", str(rot.png)]
    cmd += ["-loop", "1", "-framerate", "30", "-t", f"{INTRO_SEG:.3f}", "-i", str(png_intro)]
    cmd += ["-loop", "1", "-framerate", "30", "-t", f"{CIERRE_SEG:.3f}", "-i", str(png_cierre)]

    filtros = []
    # fps antes de trim, no despues. screenrecord solo emite un frame cuando la pantalla
    # cambia, asi que una grabacion de 40 s de una app estatica trae unas decenas de frames
    # sueltos. Sobre ese flujo, trim descarta el frame que se estaba viendo en el instante
    # de corte y el reloj salta al siguiente frame real, que puede estar segundos despues.
    # Pasar a frame rate constante primero hace que exista un frame en cada 1/30 s y que
    # cualquier operacion por tiempo signifique lo que dice.
    # tpad clona el ultimo frame unos segundos. screenrecord no escribe nada mientras la
    # pantalla no cambia, asi que el fichero termina en el ultimo cambio y no en el segundo
    # en que se paro de grabar: la espera final del guion existe, pero sin un solo frame.
    # Sin este relleno no hay material para el plano de cierre.
    filtros.append("[0:v]fps=30,tpad=stop_mode=clone:stop_duration=6,"
                   "format=yuv420p,split=" + str(len(tramos))
                   + "".join(f"[s{i}]" for i in range(len(tramos))))

    for i, t in enumerate(tramos):
        e = t["encuadre"]
        filtros.append(
            f"[s{i}]trim=start={t['desde']}:end={t['hasta']},setpts=PTS-STARTPTS,"
            f"crop={e['w']}:{e['h']}:{e['x']}:{e['y']},"
            f"scale={ANCHO}:{ALTO}:flags=lanczos,setsar=1[t{i}]"
        )

    if len(tramos) == 1:
        filtros.append("[t0]null[base]")
    else:
        filtros.append("".join(f"[t{i}]" for i in range(len(tramos)))
                       + f"concat=n={len(tramos)}:v=1:a=0[base]")

    base = "base"
    for i, rot in enumerate(rotulos):
        d_in = min(FUNDIDO, max(0.05, (rot.hasta - rot.desde) / 3))
        etq = f"c{i}"
        filtros.append(
            f"[{i + 1}:v]format=rgba,"
            f"fade=t=in:st={rot.desde:.3f}:d={d_in:.3f}:alpha=1,"
            f"fade=t=out:st={max(rot.desde, rot.hasta - d_in):.3f}:d={d_in:.3f}:alpha=1[{etq}]"
        )
        x, y = coloca(rot)
        salida = f"v{i}"
        filtros.append(
            f"[{base}][{etq}]overlay={x}:{y}:"
            f"enable='between(t,{rot.desde:.3f},{rot.hasta:.3f})'[{salida}]"
        )
        base = salida

    # Cabecera y cierre. Se generan aqui en vez de grabarse: el nombre y las dos tiendas
    # tienen que salir identicos en los diez clips, y una tarjeta dibujada no depende de
    # que el emulador este en el mismo estado dos semanas despues.
    # La tarjeta entra subiendo cincuenta pixeles mientras aparece. Es la unica animacion:
    # una imagen fija de dos segundos al principio se lee como diapositiva y se desliza.
    crema = f"0x{CREMA[0]:02X}{CREMA[1]:02X}{CREMA[2]:02X}"
    for nombre, indice, segundos in (
        ("intro", len(rotulos) + 1, INTRO_SEG),
        ("cierre", len(rotulos) + 2, CIERRE_SEG),
    ):
        filtros.append(f"color=c={crema}:s={ANCHO}x{ALTO}:d={segundos:.3f}:r=30[{nombre}bg]")
        filtros.append(
            f"[{indice}:v]format=rgba,fade=t=in:st=0:d=0.45:alpha=1[{nombre}fg]"
        )
        filtros.append(
            f"[{nombre}bg][{nombre}fg]"
            f"overlay=(W-w)/2:'(H-h)/2+50-50*min(t/0.6,1)',"
            f"format=yuv420p,setsar=1,fps=30[{nombre}]"
        )
    filtros.append(f"[{base}]format=yuv420p,setsar=1,fps=30[cuerpo]")
    filtros.append("[intro][cuerpo][cierre]concat=n=3:v=1:a=0[final]")
    base = "final"

    print("tramos en tiempo de salida:")
    for i, (t, ini) in enumerate(zip(tramos, reloj)):
        print(f"  {i}: {ini:6.2f} -> {ini + float(t['hasta']) - float(t['desde']):6.2f}"
              f"   (fuente {t['desde']} a {t['hasta']})")
    print(f"  cuerpo {total:.2f} s"
          f"   +intro {INTRO_SEG:.2f} +cierre {CIERRE_SEG:.2f}"
          f"   = {total + INTRO_SEG + CIERRE_SEG:.2f} s")

    cmd += ["-filter_complex", ";".join(filtros), "-map", f"[{base}]"]
    cmd += [
        "-c:v", "libx264", "-preset", "slow", "-crf", "19",
        "-pix_fmt", "yuv420p", "-movflags", "+faststart", "-an",
        str((RAIZ / "salida" / receta["salida"]).resolve()),
    ]
    return cmd


def monta(ruta_receta: Path) -> Path:
    receta = json.loads(ruta_receta.read_text(encoding="utf-8"))
    (RAIZ / "salida").mkdir(exist_ok=True)
    with tempfile.TemporaryDirectory() as td:
        cmd = construye(receta, Path(td))
        subprocess.run(cmd, check=True)
    destino = RAIZ / "salida" / receta["salida"]
    print(f"listo: {destino}")
    return destino


def autocheck() -> None:
    """Comprueba el montaje entero sin depender de ninguna grabacion.

    Fabrica un video de prueba con el color de fondo de la app, le mete dos rotulos y
    verifica que la salida existe, dura lo pedido y mide 1080x1920. Si esto pasa, el
    unico eslabon que queda por validar es la grabacion del emulador.
    """
    with tempfile.TemporaryDirectory() as td:
        tmp = Path(td)
        falso = tmp / "falso.mp4"
        subprocess.run(
            ["ffmpeg", "-y", "-v", "error", "-f", "lavfi",
             "-i", f"color=c=0x{CREMA[0]:02X}{CREMA[1]:02X}{CREMA[2]:02X}:s=1080x2400:d=8:r=30",
             "-c:v", "libx264", "-pix_fmt", "yuv420p", str(falso)],
            check=True,
        )
        receta = {
            "salida": "_autocheck.mp4",
            "fuente": str(falso),
            "recorte": {"desde": 0.5, "hasta": 6.5},
            "encuadre": {"x": 0, "y": 90, "w": 1080, "h": 1920},
            "rotulos": [
                {"t": [0.2, 3.0], "texto": "Un cuadrito al día", "pos": "arriba", "acento": "salvia"},
                {"t": [3.2, 5.8], "texto": "Nada más que eso", "pos": "abajo", "tam": 72},
            ],
        }
        r = tmp / "receta.json"
        r.write_text(json.dumps(receta), encoding="utf-8")
        salida = monta(r)

    info = subprocess.run(
        ["ffprobe", "-v", "error", "-select_streams", "v:0",
         "-show_entries", "stream=width,height", "-show_entries", "format=duration",
         "-of", "json", str(salida)],
        capture_output=True, text=True, check=True,
    )
    datos = json.loads(info.stdout)
    w = datos["streams"][0]["width"]
    h = datos["streams"][0]["height"]
    dur = float(datos["format"]["duration"])
    assert (w, h) == (ANCHO, ALTO), f"encuadre mal: {w}x{h}"
    esperado = 6.0 + INTRO_SEG + CIERRE_SEG
    assert math.isclose(dur, esperado, abs_tol=0.2), f"duracion mal: {dur}, esperaba {esperado}"
    print(f"autocheck OK: {w}x{h}, {dur:.2f}s")
    salida.unlink()


if __name__ == "__main__":
    if not shutil.which("ffmpeg"):
        raise SystemExit("falta ffmpeg")
    if len(sys.argv) == 2 and sys.argv[1] == "--autocheck":
        autocheck()
    elif len(sys.argv) == 2:
        monta(Path(sys.argv[1]).resolve())
    else:
        raise SystemExit(__doc__)
