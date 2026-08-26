#!/usr/bin/env python3
"""Regenerates every app icon from one geometry. Needs rsvg-convert (brew install librsvg).

Run from anywhere:  python3 tools/generate_icons.py
"""
import os, pathlib, subprocess

S = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(S)

# The app's own dark-theme tokens, so the icon is literally a slice of the product.
BG_TOP, BG_BOT, EMPTY = "#2C2820", "#17150F", "#3A352B"
COLS = ["#F0AFBE", "#F5C39B", "#B6D6AB", "#9CD3C7"]  # rose, peach, sage, mint

def rr(x, y, w, h, r):
    return (f"M{x+r:.2f},{y:.2f} H{x+w-r:.2f} A{r:.2f},{r:.2f} 0 0 1 {x+w:.2f},{y+r:.2f} "
            f"V{y+h-r:.2f} A{r:.2f},{r:.2f} 0 0 1 {x+w-r:.2f},{y+h:.2f} "
            f"H{x+r:.2f} A{r:.2f},{r:.2f} 0 0 1 {x:.2f},{y+h-r:.2f} "
            f"V{y+r:.2f} A{r:.2f},{r:.2f} 0 0 1 {x+r:.2f},{y:.2f} Z")

def cells(canvas, extent):
    """Four columns climbing 1..4: four habits, four streak lengths."""
    cell = extent / 4.54
    gap, r = cell * 0.18, cell * 0.30
    off = (canvas - extent) / 2
    for col in range(4):
        for row in range(4):
            yield (rr(off + col * (cell + gap), off + row * (cell + gap), cell, cell, r),
                   COLS[col], col >= 3 - row)

def svg(canvas, extent, shape):
    if shape == "circle":
        plate = f'<circle cx="{canvas/2}" cy="{canvas/2}" r="{canvas/2}" fill="url(#bg)"/>'
    elif shape == "rounded":
        rx = canvas * 0.22
        plate = f'<rect width="{canvas}" height="{canvas}" rx="{rx}" fill="url(#bg)"/>'
    else:
        plate = f'<rect width="{canvas}" height="{canvas}" fill="url(#bg)"/>'
    body = [plate] + [f'<path d="{d}" fill="{c if f else EMPTY}"/>' for d, c, f in cells(canvas, extent)]
    return (f'<svg xmlns="http://www.w3.org/2000/svg" width="{canvas}" height="{canvas}" '
            f'viewBox="0 0 {canvas} {canvas}"><defs>'
            f'<linearGradient id="bg" x1="0" y1="0" x2="0" y2="1">'
            f'<stop offset="0" stop-color="{BG_TOP}"/><stop offset="1" stop-color="{BG_BOT}"/>'
            f'</linearGradient></defs>' + "".join(body) + "</svg>")

def png(svg_text, out, size):
    src = f"{S}/_tmp.svg"
    pathlib.Path(src).write_text(svg_text)
    pathlib.Path(out).parent.mkdir(parents=True, exist_ok=True)
    subprocess.run(["rsvg-convert", "-w", str(size), "-h", str(size), src, "-o", out], check=True)

# --- iOS: full bleed, the system applies its own mask ---
png(svg(1024, 800, "square"), f"{ROOT}/iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png", 1024)

# --- Android legacy launcher icons (API 24-25 have no adaptive icons) ---
for folder, size in [("mdpi", 48), ("hdpi", 72), ("xhdpi", 96), ("xxhdpi", 144), ("xxxhdpi", 192)]:
    base = f"{ROOT}/androidApp/src/main/res/mipmap-{folder}"
    png(svg(1024, 800, "rounded"), f"{base}/ic_launcher.png", size)
    png(svg(1024, 660, "circle"), f"{base}/ic_launcher_round.png", size)

# --- Android adaptive: background layer is the gradient, foreground the grid ---
res = f"{ROOT}/androidApp/src/main/res"
pathlib.Path(f"{res}/drawable/ic_launcher_background.xml").write_text(
    '<?xml version="1.0" encoding="utf-8"?>\n'
    '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
    '    xmlns:aapt="http://schemas.android.com/aapt"\n'
    '    android:width="108dp" android:height="108dp"\n'
    '    android:viewportWidth="108" android:viewportHeight="108">\n'
    '    <path android:pathData="M0,0h108v108h-108z">\n'
    '        <aapt:attr name="android:fillColor">\n'
    '            <gradient android:type="linear"\n'
    '                android:startX="0" android:startY="0" android:endX="0" android:endY="108">\n'
    f'                <item android:offset="0" android:color="#FF{BG_TOP[1:]}"/>\n'
    f'                <item android:offset="1" android:color="#FF{BG_BOT[1:]}"/>\n'
    '            </gradient>\n'
    '        </aapt:attr>\n'
    '    </path>\n'
    '</vector>\n')

def vector(canvas, extent, size_dp, only_filled, color=None):
    lines = ['<?xml version="1.0" encoding="utf-8"?>',
             '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
             f'    android:width="{size_dp}dp" android:height="{size_dp}dp"',
             f'    android:viewportWidth="{canvas}" android:viewportHeight="{canvas}">']
    for d, c, f in cells(canvas, extent):
        if only_filled and not f:
            continue
        lines.append(f'    <path android:fillColor="{color or (c if f else EMPTY)}" android:pathData="{d}"/>')
    lines.append("</vector>")
    return "\n".join(lines) + "\n"

# A square of side L fits the 66dp safe-zone circle only if L <= 66/sqrt(2) = 46.7.
pathlib.Path(f"{res}/drawable-v24/ic_launcher_foreground.xml").write_text(vector(108, 46, 108, False))
pathlib.Path(f"{res}/drawable/ic_launcher_monochrome.xml").write_text(
    vector(108, 46, 108, True, "#FFFFFFFF"))

for name in ("ic_launcher.xml", "ic_launcher_round.xml"):
    pathlib.Path(f"{res}/mipmap-anydpi-v26/{name}").write_text(
        '<?xml version="1.0" encoding="utf-8"?>\n'
        '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
        '    <background android:drawable="@drawable/ic_launcher_background" />\n'
        '    <foreground android:drawable="@drawable/ic_launcher_foreground" />\n'
        '    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />\n'
        '</adaptive-icon>\n')

# --- Notification icon: Android tints it white, so it must be a flat silhouette ---
pathlib.Path(f"{ROOT}/shared/src/androidMain/res/drawable").mkdir(parents=True, exist_ok=True)
pathlib.Path(f"{ROOT}/shared/src/androidMain/res/drawable/ic_notification.xml").write_text(
    vector(24, 19, 24, True, "#FFFFFFFF"))

pathlib.Path(f"{S}/_tmp.svg").unlink()
pathlib.Path(f"{S}/icon-master.svg").write_text(svg(1024, 800, "square"))
print("assets written")
