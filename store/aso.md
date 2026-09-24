# ASO: palabras clave y titulos por idioma

Investigacion de palabras clave para la ficha de Google Play de Quilt (rastreador de habitos anual,
`com.baltajmn.habit`). Objetivo: mas descargas desde la busqueda de Play, no solo texto bonito.

Formato de titulo de la casa: **"Quilt: <palabra que se busca>"**. El titulo pesa mas que ningun otro
campo en el ranking de busqueda de Play, asi que lleva siempre la marca (Quilt) mas el termino de
categoria que la gente teclea de verdad en ese idioma, nunca un sinonimo bonito ni una traduccion
literal palabra por palabra.

Tope de Play: titulo 30 caracteres, descripcion corta 80, descripcion larga 4000. Los recuentos de
abajo son caracteres (`len()` en Python), que es como Play los cuenta, incluidos los alfabetos no
latinos.

Este documento es autosuficiente: cualquier agente que traduzca despues los ficheros de
`store/listings/<idioma>/` puede partir solo de esta tabla sin releer la investigacion de mercado.

---

## es-ES (implementado)

Titulo: **Quilt: seguimiento de hábitos** (29/30)

Palabras clave objetivo:
- seguimiento de hábitos (frase que mas teclea el usuario hispanohablante, mejor que "rastreador" en volumen)
- rastreador de hábitos (calco de "tracker", tambien buscado, usado en la descripcion corta)
- rachas
- cuadrícula de hábitos / cuadrícula anual
- widget
- rutina diaria
- year in pixels (termino de nicho que trae usuarios muy cualificados: la gente que ya lleva un diario en papel)

## en-US (implementado)

Titulo: **Quilt: Habit Tracker & Streaks** (30/30)

Palabras clave objetivo:
- habit tracker (termino de categoria de mayor volumen, va en el titulo)
- streaks
- habit grid / year grid
- widget
- routine / daily routine
- year in pixels
- no account (angulo de privacidad que diferencia frente a Habitify/Habitica)

## de-DE (implementado)

Titulo: **Quilt: Gewohnheiten Tracker** (27/30)

Palabras clave objetivo:
- Gewohnheitstracker / Gewohnheiten Tracker (separado cubre busquedas de "Gewohnheiten" suelto, que es como buscan de verdad; el compuesto pegado "Gewohnheitstracker" tambien se usa y aparece en el cuerpo del texto)
- Serie / Serien (streak en aleman)
- Jahresraster / Raster
- Widget
- Routine / Tagesroutine
- ohne Konto (privacidad, diferenciador)

## fr-FR (implementado)

Titulo: **Quilt : suivi d'habitudes** (25/30)

Palabras clave objetivo:
- suivi d'habitudes (expresion de categoria en frances, mejor que "traqueur" que suena a traduccion automatica)
- habitudes
- série / séries (streak en frances)
- grille / grille annuelle
- widget
- routine quotidienne
- sans compte

## pt-BR (implementado)

Titulo: **Quilt: rastreador de hábitos** (28/30)

Palabras clave objetivo:
- rastreador de hábitos (forma corriente en Brasil, mas que "seguimento")
- sequência / sequências (streak en portugues de Brasil)
- grade anual / grade de hábitos
- widget
- rotina diária
- sem conta

## it-IT

Titulo: **Quilt: tracker di abitudini** (27/30)

Palabras clave objetivo:
- tracker di abitudini (forma mas buscada, el italiano usa "tracker" en ingles con frecuencia)
- monitoraggio abitudini (alternativa mas formal, cubrir las dos)
- abitudini quotidiane
- streak / serie
- routine giornaliera
- widget
- obiettivi giornalieri

## ja-JP

Titulo: **Quilt: 習慣トラッカー** (14/30)

Palabras clave objetivo:
- 習慣トラッカー (shuukan torakkaa, "habit tracker", el termino dominante en la Play Store japonesa)
- 習慣化アプリ (shuukanka apuri, "app para formar habitos", muy buscado tambien)
- ルーティン (ruutin, "routine")
- ストリーク (sutoriiku, "streak", usado por apps como Streaks/Habitify en japones)
- 継続記録 (keizoku kiroku, "registro de continuidad", forma nativa de "streak")
- ウィジェット (uijetto, "widget")
- 目標達成 (mokuhyou tassei, "logro de objetivos")

## ko-KR

Titulo: **Quilt: 습관 트래커** (13/30)

Palabras clave objetivo:
- 습관 트래커 (seupgwan teuraekeo, "habit tracker")
- 습관 관리 (seupgwan gwanli, "gestion de habitos", forma alternativa muy buscada)
- 루틴 (rutin, "routine")
- 스트릭 (seuteurik, "streak")
- 위젯 (wijet, "widget")
- 목표 달성 (mokpyo dalseong, "logro de objetivos")
- 습관 기록 (seupgwan girok, "registro de habitos")

## pl-PL

Titulo: **Quilt: tracker nawyków** (22/30)

Palabras clave objetivo:
- tracker nawyków (forma dominante, calco directo de "habit tracker")
- monitorowanie nawyków (alternativa mas formal)
- nawyki
- passy / serie (las dos formas de "streak" en polaco, "passy" es coloquial)
- rutyna dnia
- widget
- planer nawyków

## tr-TR

Titulo: **Quilt: alışkanlık takibi** (24/30)

Palabras clave objetivo:
- alışkanlık takibi ("seguimiento de habitos", forma mas natural en turco)
- alışkanlık takip uygulaması (version larga, "app de seguimiento de habitos")
- seri (streak en turco)
- günlük rutin
- widget
- hedef takibi (seguimiento de objetivos)
- alışkanlık oluşturma (formacion de habitos)

## id (Indonesia)

Titulo: **Quilt: pelacak kebiasaan** (24/30)

Palabras clave objetivo:
- pelacak kebiasaan ("habit tracker", forma dominante)
- aplikasi kebiasaan
- rutinitas harian (rutina diaria)
- streak (se usa en ingles tal cual, muy extendido en el mercado indonesio)
- pelacak rutinitas
- widget
- target harian (objetivo diario)

## ru-RU

Titulo: **Quilt: трекер привычек** (22/30)

Palabras clave objetivo:
- трекер привычек (tréker privíchek, "habit tracker", forma dominante)
- отслеживание привычек (otslézhivanie privíchek, alternativa mas formal)
- привычки (privíchki, "habitos")
- серия / стрик (séria o strik, las dos formas de "streak" en ruso)
- ежедневная рутина (ezhednévnaya rutína, "rutina diaria")
- виджет (vidzhet, "widget")
- планировщик привычек (planirovschik privíchek, "planificador de habitos")

## nl-NL

Titulo: **Quilt: gewoontetracker** (22/30)

Palabras clave objetivo:
- gewoontetracker (compuesto, forma mas buscada en el Play Store neerlandes)
- gewoonten bijhouden (forma verbal, "llevar seguimiento de habitos")
- dagelijkse routine
- streak / reeks (las dos formas, "reeks" es la traduccion nativa)
- widget
- doelen bijhouden (seguimiento de objetivos)

---

## Notas para quien traduzca las descripciones cortas y largas

- Primera linea de la `short` y de la `full`: siempre el beneficio ("ves el año entero, no solo hoy"),
  nunca una lista de funciones. Las funciones van despues.
- Repetir la palabra clave principal del titulo 3-5 veces en la `full`, de forma natural, no en una
  lista. En los cinco idiomas ya implementados la frase aparece: en la primera frase del segundo
  parrafo ("Quilt es/is/ist/est/é un [tracker]..."), en la seccion "gratis de verdad" y en la frase de
  cierre. Es una plantilla reutilizable para el resto de idiomas.
- No traducir literal: cada idioma tiene su propia forma dominante de "habit tracker" y de "streak"
  (ver tabla de arriba). Usar la forma local, no el calco de la palabra inglesa o española.
- Lo que es verdad de la app y no se puede inventar: 3 habitos gratis (`FREE_HABIT_LIMIT` en
  `HabitRepository.kt`), sin cuenta ni analitica propia (solo RevenueCat al comprar), grid anual con
  cuadrados no circulos, widgets interactivos en tres tamaños en Android e iOS, dias saltados que no
  rompen la racha, habitos de cantidad con contador, exportar/importar siempre gratis, pago unico sin
  suscripcion (Quilt Pro).
