# Promocion

Donde encaja Quilt fuera de Play y en que orden atacarlo. Discord y TikTok tienen su propio
fichero (`discord.md`, `tiktok.md`); esto cubre el resto.

A 28/09/2026: 10+ descargas en Play, 0 resenas. iOS sin publicar, asi que todo dice "Android only
for now". Enlace: <https://play.google.com/store/apps/details?id=com.baltajmn.habit>

## Orden

1. **AlternativeTo.** Quilt no aparece. HabitKit tiene 91 alternativas listadas y entra gente cada
   semana: es trafico que ya busca exactamente esto y no caduca como un post.
2. **Reddit, uno o dos subreddits por semana.** Donde mas usuarios reales salen para una app de
   habitos. Tambien donde mas facil es que te baneen.
3. **Discord**, segun `discord.md`.
4. **Product Hunt**, cuando haya unas cuantas resenas en Play. Sin nadie que vote el primer dia se
   hunde, y un lanzamiento solo se hace una vez.

Fuera: Hacker News (una app de habitos con compra dentro no pasa de la primera pagina de "new"),
foros genericos de autopromocion y comprar instalaciones.

## AlternativeTo

Hay que entrar con cuenta propia. "Add app" arriba a la derecha; despues, en la ficha de cada app
de la lista, "Suggest alternative" y elegir Quilt.

Enviada el 28/09/2026 como `Quilt Habit Tracker` ("Quilt" a secas ya estaba cogido), sin pagar la
cola prioritaria. Pendiente de revision: se sigue en "My submissions" del menu de perfil.
Alternativas sugeridas el mismo dia: HabitKit, Loop Habit Tracker, Everyday, Habitify, Streaks y
Way of Life. Ficha: <https://alternativeto.net/software/quilt-habit-tracker/> (solo la ve el
usuario hasta que la aprueben: no compartir el enlace antes).

- **Nombre:** Quilt Habit Tracker
- **Web:** https://play.google.com/store/apps/details?id=com.baltajmn.habit
- **Descripcion corta:** A habit tracker that shows your whole year, one square per day. Home screen widgets, skipped days that don't break streaks, no account, no ads.
- **Licencia:** Freemium, Proprietary. Pay once, no subscription.
- **Plataformas:** Android
- **Categoria:** Habit Tracker
- **Propiedades:** Privacy focused, No registration required, Works Offline, Ad-free, Dark Mode, Reminders, Calendar View, Widgets, Export to CSV/JSON
- **Alternativa a:** HabitKit, Loop Habit Tracker, Streaks, Habitify, Way of Life, Everyday, Year in Pixels

## Reddit

**Antes de cada post:** leer las normas del subreddit (barra lateral y wiki). Cambian a menudo y
no se han podido comprobar desde aqui: el navegador integrado no abre reddit.com. Decir siempre que
eres el desarrollador. Un post por subreddit, nunca el mismo texto en dos el mismo dia, y quedarse
contestando las primeras horas.

| Subreddit | Encaje | Post | Publicado |
|---|---|---|---|
| r/androidapps | Usuarios de Android buscando apps. Admite a desarrolladores con su flair. | A | |
| r/theXeffect | Marcar una X por dia en un calendario: es la cuadricula de Quilt en papel. | B | |
| r/SideProject | Proyectos personales, se espera autopromocion. | C | |
| r/QuantifiedSelf | Registrar la vida en datos. Les importa la exportacion. | A, abrir por la exportacion | |
| r/habits | Pequeno pero exacto. | B | |
| r/AlphaAndBetaUsers | Gente que prueba apps nuevas a cambio de dar opinion. | A | |

**No:** r/productivity y r/getdisciplined (prohiben autopromocion), r/Android (solo noticias),
r/bulletjournal (papel, lo ven como intrusion).

**Mejor que cualquier post:** los hilos de "what habit tracker do you use?" que salen cada semana
en r/androidapps y r/theXeffect. Contestar a la pregunta, con Quilt como una opcion entre varias y
diciendo que es tuya.

### A. r/androidapps y parecidos

Titulo: `[DEV] Quilt: a habit tracker that shows your whole year, not just this week`

```
I'm the developer. Quilt is a habit tracker built around one idea: each habit gets a full year grid, one square per day, so you see all twelve months at once instead of a streak number.

What it does:
- Interactive home screen widgets in three sizes: tap to mark the day without opening the app
- Quick Settings tile that ticks off your next pending habit
- Skipped days (sick, travelling, rest day) don't break the streak or lower your percentage
- Weekly targets (gym 3x a week, any 3 days) and counted habits (8 glasses of water)
- Tap any past day to fix it if you forgot
- No account, no ads, no analytics. Everything stays on the phone, with JSON/CSV export and import

Free with 3 habits and every feature. A one-time purchase removes the limit. No subscription, ever.

https://play.google.com/store/apps/details?id=com.baltajmn.habit

Android only for now. It's new, so I'd really like to hear what's missing or what feels wrong.
```

### B. r/theXeffect y r/habits

Titulo: `I made the X effect calendar into an app, with the whole year on one screen`

```
I kept habit calendars on paper for a while and the part that worked was seeing the whole year: the month I slipped and the weeks I didn't, all at once. Most apps only show you this week, so I built one that doesn't.

Quilt gives every habit a year grid, one square per day. A home screen widget marks today in one tap, and a long press marks a day as skipped so a cold or a trip doesn't wipe the chain.

Free, no account, no ads. Android only for now. Disclosure: I'm the developer.

https://play.google.com/store/apps/details?id=com.baltajmn.habit

Happy to hear how you track yours and what an app would need to replace the paper for you.
```

### C. r/SideProject

Titulo: `Quilt: a year-grid habit tracker, built with Kotlin Multiplatform, no backend`

```
Shipped my first app to Google Play: Quilt, a habit tracker where each habit is a full year grid, one square per day.

Some decisions behind it:
- No backend and no account. Data lives in a JSON file on the device, with export and import
- One-time purchase instead of a subscription; free tier has every feature with 3 habits
- Kotlin Multiplatform + Compose Multiplatform for Android and iOS, with native widgets on each side (Glance, WidgetKit)
- Skipped days as a third state, because breaking a 60-day streak for a flu is the main reason people quit these apps

https://play.google.com/store/apps/details?id=com.baltajmn.habit

It has about 10 installs, so any feedback on the listing or the app itself helps a lot.
```

## Product Hunt

"Quilt" a secas ya existe alli (una bomba de calor). Lanzar como `Quilt: Habit Tracker`.
Tagline: `Your whole year of habits, one square per day`. Martes a jueves, a las 00:01 hora del
Pacifico. Avisar antes a quien vaya a votar y comentar; sin eso no merece la pena.
