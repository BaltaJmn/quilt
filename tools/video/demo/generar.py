#!/usr/bin/env python3
"""Genera los conjuntos de datos de demostracion de tools/video.

Un clip tiene que salir igual hoy que dentro de tres meses, asi que los datos no se
escriben a mano: se generan a partir de una fecha y una semilla. Cambiar la fecha
reconstruye el mismo historial desplazado, con las mismas rachas y los mismos huecos.

    ./generar.py                 usa la fecha de hoy
    ./generar.py 2026-08-31      congela la fecha
"""

from __future__ import annotations

import json
import random
import sys
from datetime import date, timedelta
from pathlib import Path

AQUI = Path(__file__).resolve().parent

ROSA, MELOCOTON, MANTEQUILLA, SALVIA = 0xFFF0AFBE, 0xFFF5C39B, 0xFFEDDC98, 0xFFB6D6AB
MENTA, CIELO, LAVANDA, LILA = 0xFF9CD3C7, 0xFFA2C3E9, 0xFFB4B8EC, 0xFFD9AFE6


def dias(desde: date, hasta: date):
    d = desde
    while d <= hasta:
        yield d
        d += timedelta(days=1)


def habito(
    id_, nombre, emoji, color, desde: date, hasta: date, *,
    exito: float, semilla: int, target: int = 1,
    recordatorio: int | None = None, vacaciones: tuple[date, date] | None = None,
    hoy_hecho: bool | None = None, hoy_parcial: int | None = None,
):
    """Un habito con historial verosimil: ni racha perfecta ni ruido uniforme.

    La probabilidad de marcar sube con la racha viva. Un exito plano reparte los huecos
    como puntos de sal y ningun usuario real se parece a eso: se falla en tandas y se
    vuelve, y eso es lo que tiene que ver quien mira el video.
    """
    azar = random.Random(semilla)
    log: dict[str, int] = {}
    saltados: list[str] = []
    racha = 0
    for d in dias(desde, hasta):
        clave = d.isoformat()
        if vacaciones and vacaciones[0] <= d <= vacaciones[1]:
            saltados.append(clave)
            continue
        if d == hasta and hoy_hecho is not None:
            if hoy_parcial:
                log[clave] = hoy_parcial
            elif hoy_hecho:
                log[clave] = target
            continue
        if azar.random() < min(0.97, exito + 0.03 * min(racha, 6)):
            log[clave] = target
            racha += 1
        else:
            racha = 0
    return {
        "id": id_, "name": nombre, "emoji": emoji, "colorArgb": color,
        "target": target, "scheduleDays": [1, 2, 3, 4, 5, 6, 7],
        "weeklyTarget": None, "reminderMinute": recordatorio,
        "createdAt": desde.isoformat(), "archived": False,
        "log": log, "skipped": saltados,
    }


def semanal(id_, nombre, emoji, color, desde: date, hasta: date, *, veces: int,
            semilla: int, hoy_hecho: bool = False):
    """Habito de cuota semanal: se marcan `veces` dias sueltos de cada semana."""
    azar = random.Random(semilla)
    log: dict[str, int] = {}
    lunes = desde - timedelta(days=desde.weekday())
    while lunes <= hasta:
        cumple = azar.random() < 0.83
        elegidos = azar.sample(range(7), veces if cumple else max(1, veces - 1))
        for offset in sorted(elegidos):
            d = lunes + timedelta(days=offset)
            if desde <= d <= hasta and not (d == hasta and not hoy_hecho):
                log[d.isoformat()] = 1
        lunes += timedelta(days=7)
    return {
        "id": id_, "name": nombre, "emoji": emoji, "colorArgb": color,
        "target": 1, "scheduleDays": [1, 2, 3, 4, 5, 6, 7],
        "weeklyTarget": veces, "reminderMinute": None,
        "createdAt": desde.isoformat(), "archived": False,
        "log": log, "skipped": [],
    }


def escribe(nombre: str, habitos: list[dict], pro: bool) -> None:
    (AQUI / nombre).write_text(
        json.dumps({"version": 1, "isPro": pro, "habits": habitos}, ensure_ascii=False),
        encoding="utf-8",
    )
    print(f"{nombre}: {len(habitos)} habitos, isPro={pro}")


def main() -> None:
    hoy = date.fromisoformat(sys.argv[1]) if len(sys.argv) > 1 else date.today()
    enero = date(hoy.year, 1, 1)
    febrero = date(hoy.year, 2, 1)
    # Una semana de vacaciones cerrada hace mes y medio: es el hueco gris que el clip del
    # dia saltado necesita tener ya en la rejilla antes de empezar a grabar.
    vac = (hoy - timedelta(days=52), hoy - timedelta(days=46))

    leer = habito("a1", "Leer", "📚", SALVIA, enero, hoy, exito=0.86, semilla=7,
                  recordatorio=1320, vacaciones=vac, hoy_hecho=True)
    correr = semanal("b2", "Correr", "🏃", ROSA, enero, hoy, veces=3, semilla=11,
                     hoy_hecho=False)
    meditar = habito("c3", "Meditar", "🧘", MELOCOTON, febrero, hoy, exito=0.74,
                     semilla=23, hoy_hecho=True)
    agua = habito("d4", "Agua", "💧", CIELO, enero, hoy, exito=0.80, semilla=31,
                  target=8, hoy_hecho=False, hoy_parcial=3)

    escribe("habits.json", [leer, correr, meditar], pro=False)
    escribe("agua.json", [agua, leer, correr, meditar], pro=True)
    escribe("dos.json", [leer, meditar], pro=False)


if __name__ == "__main__":
    main()
