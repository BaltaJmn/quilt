package com.baltajmn.habit.i18n

/** Two-letter code of the device language. */
expect fun systemLanguage(): String

/** The languages the app ships. Anything else falls back to English. */
internal val SUPPORTED = listOf("en", "es", "pt", "de", "fr")

/**
 * Every user-facing string, in one table.
 *
 * Not Compose Resources on purpose: `stringResource()` only works inside a `@Composable`, and a
 * third of these strings are drawn from a BroadcastReceiver, a Glance widget, a Canvas and a
 * notification builder. Five languages and ~85 literals still fit in a table more cheaply than in
 * a codegen pipeline.
 *
 * ponytail: language is read once at first access. Both OSes restart the app on a language
 * change, so this only shows if live switching is ever needed.
 */
object S {

    /** English is the fallback: every language outside [SUPPORTED] lands there. */
    private val lang = systemLanguage().take(2).lowercase()
        .takeIf { it in SUPPORTED } ?: "en"

    private fun t(en: String, es: String, pt: String, de: String, fr: String): String = when (lang) {
        "es" -> es
        "pt" -> pt
        "de" -> de
        "fr" -> fr
        else -> en
    }

    // Home
    val startFirst = t(
        "Start your first habit",
        "Empieza tu primer hábito",
        "Comece seu primeiro hábito",
        "Starte deine erste Gewohnheit",
        "Commencez votre première habitude",
    )
    val emptyHint = t(
        "One square a day.\nTap + to create a habit.",
        "Un cuadrito por día.\nPulsa + para crear un hábito.",
        "Um quadradinho por dia.\nToque em + para criar um hábito.",
        "Ein Kästchen pro Tag.\nTippe auf +, um eine Gewohnheit anzulegen.",
        "Un carré par jour.\nAppuyez sur + pour créer une habitude.",
    )
    fun doneToday(done: Int, total: Int) = t(
        "$done of $total done today",
        "$done de $total hechos hoy",
        "$done de $total feitos hoje",
        "$done von $total heute erledigt",
        "$done sur $total faites aujourd'hui",
    )

    // Habit card and stats
    fun streak(n: Int) = streakText(n, lang)
    fun days(n: Int) = t(
        if (n == 1) "day" else "days",
        if (n == 1) "día" else "días",
        if (n == 1) "dia" else "dias",
        if (n == 1) "Tag" else "Tage",
        if (n == 1) "jour" else "jours",
    )
    fun rateThisYear(rate: Int) = t(
        "$rate% this year",
        "$rate% este año",
        "$rate% este ano",
        "$rate% dieses Jahr",
        "$rate% cette année",
    )
    val currentStreak = t("Current streak", "Racha actual", "Sequência atual", "Aktuelle Serie", "Série actuelle")
    val bestStreak = t("Best streak", "Mejor racha", "Melhor sequência", "Beste Serie", "Meilleure série")
    val totalDays = t("Total days", "Días totales", "Dias totais", "Tage insgesamt", "Jours au total")
    val sinceStart = t("since the start", "desde el inicio", "desde o início", "seit Beginn", "depuis le début")
    val completion = t("Completion", "Cumplimiento", "Cumprimento", "Erfüllung", "Réalisation")
    fun inYear(year: Int) = t("in $year", "en $year", "em $year", "in $year", "en $year")

    // Schedule
    val everyDay = t("Every day", "Todos los días", "Todos os dias", "Jeden Tag", "Tous les jours")
    val weekdays = t(
        "Weekdays",
        "De lunes a viernes",
        "De segunda a sexta",
        "Montag bis Freitag",
        "Du lundi au vendredi",
    )
    val weekends = t("Weekends", "Fines de semana", "Fins de semana", "Wochenenden", "Week-ends")
    /** Monday first, matching ISO day numbers. */
    val dayInitials = dayInitialsFor(lang)
    fun timesPerDay(n: Int) = t(
        "$n times a day",
        "$n veces al día",
        "$n vezes por dia",
        "$n Mal am Tag",
        "$n fois par jour",
    )
    fun time(minutes: Int) = formatTime(minutes, lang)

    // Habit form
    val newHabit = t("New habit", "Nuevo hábito", "Novo hábito", "Neue Gewohnheit", "Nouvelle habitude")
    val editHabit = t("Edit habit", "Editar hábito", "Editar hábito", "Gewohnheit bearbeiten", "Modifier l'habitude")
    val namePlaceholder = t("Drink water", "Beber agua", "Beber água", "Wasser trinken", "Boire de l'eau")
    val icon = t("Icon", "Icono", "Ícone", "Symbol", "Icône")
    val color = t("Colour", "Color", "Cor", "Farbe", "Couleur")
    val daysLabel = t("Days", "Días", "Dias", "Tage", "Jours")
    val timesADay = t("Times a day", "Veces al día", "Vezes por dia", "Mal am Tag", "Fois par jour")
    val reminder = t("Reminder", "Recordatorio", "Lembrete", "Erinnerung", "Rappel")
    val createHabit = t("Create habit", "Crear hábito", "Criar hábito", "Gewohnheit anlegen", "Créer l'habitude")
    val save = t("Save", "Guardar", "Salvar", "Speichern", "Enregistrer")
    val remove = t("Remove", "Quitar", "Remover", "Entfernen", "Retirer")
    val cancel = t("Cancel", "Cancelar", "Cancelar", "Abbrechen", "Annuler")
    val none = t("None", "Ninguno", "Nenhum", "Keine", "Aucun")

    // Detail
    val skipHint = t(
        "Press and hold a day to skip it. Holidays and illness do not break the streak.",
        "Mantén pulsado un día para saltarlo. Vacaciones o enfermedad no rompen la racha.",
        "Mantenha um dia pressionado para pulá-lo. Férias ou doença não quebram a sequência.",
        "Halte einen Tag gedrückt, um ihn zu überspringen. Urlaub oder Krankheit brechen die Serie nicht.",
        "Maintenez un jour appuyé pour le passer. Vacances ou maladie ne cassent pas la série.",
    )
    val archive = t("Archive", "Archivar", "Arquivar", "Archivieren", "Archiver")
    val unarchive = t("Unarchive", "Desarchivar", "Desarquivar", "Wiederherstellen", "Désarchiver")
    val archiveHint = t(
        "Hidden from the list, history is kept",
        "Se oculta de la lista, el historial se conserva",
        "Fica oculto da lista, o histórico é mantido",
        "Wird aus der Liste ausgeblendet, der Verlauf bleibt erhalten",
        "Masquée de la liste, l'historique est conservé",
    )
    val deleteHabit = t(
        "Delete habit",
        "Borrar hábito",
        "Excluir hábito",
        "Gewohnheit löschen",
        "Supprimer l'habitude",
    )
    val deleteHint = t(
        "Removes the habit and all its history",
        "Elimina el hábito y todo su historial",
        "Remove o hábito e todo o seu histórico",
        "Entfernt die Gewohnheit und ihren gesamten Verlauf",
        "Supprime l'habitude et tout son historique",
    )
    fun deleteTitle(name: String) = t(
        "Delete \"$name\"?",
        "¿Borrar \"$name\"?",
        "Excluir \"$name\"?",
        "\"$name\" löschen?",
        "Supprimer \"$name\" ?",
    )
    fun deleteBody(total: Int) = t(
        "The $total logged days are lost. This cannot be undone.",
        "Se pierden los $total días registrados. No se puede deshacer.",
        "Os $total dias registrados são perdidos. Não é possível desfazer.",
        "Die $total erfassten Tage gehen verloren. Das lässt sich nicht rückgängig machen.",
        "Les $total jours enregistrés seront perdus. Cette action est irréversible.",
    )
    val delete = t("Delete", "Borrar", "Excluir", "Löschen", "Supprimer")

    // Share
    val share = t("Share", "Compartir", "Compartilhar", "Teilen", "Partager")
    val preview = t("Preview", "Vista previa", "Pré-visualização", "Vorschau", "Aperçu")
    val savedToPhotos = t(
        "Saved to your photos",
        "Guardado en tus fotos",
        "Salvo nas suas fotos",
        "In deinen Fotos gespeichert",
        "Enregistré dans vos photos",
    )
    val saveFailed = t(
        "Could not save, try Share instead",
        "No se pudo guardar, prueba con Compartir",
        "Não foi possível salvar, tente Compartilhar",
        "Speichern fehlgeschlagen, versuche es mit Teilen",
        "Échec de l'enregistrement, essayez Partager",
    )
    val privacyPolicy = t(
        "Privacy policy",
        "Politica de privacidad",
        "Politica de privacidade",
        "Datenschutzerklarung",
        "Politique de confidentialite",
    )
    val week = t("Week", "Semana", "Semana", "Woche", "Semaine")
    val month = t("Month", "Mes", "Mês", "Monat", "Mois")
    val year = t("Year", "Año", "Ano", "Jahr", "Année")
    /** Carries the name: a shared image is the one thing that travels without the app. */
    val shareFooter = t(
        "Quilt · one patch a day",
        "Quilt · un retal al día",
        "Quilt · um retalho por dia",
        "Quilt · ein Flicken pro Tag",
        "Quilt · un carré par jour",
    )
    val months = monthNames(lang)
    val monthsShort = monthAbbreviations(lang)
    fun weekOf(day: Int, monthIndex: Int) = t(
        "Week of ${months[monthIndex]} $day",
        "Semana del $day de ${months[monthIndex]}",
        "Semana de $day de ${months[monthIndex]}",
        "Woche vom $day. ${months[monthIndex]}",
        "Semaine du $day ${months[monthIndex]}",
    )
    fun shareSummary(habits: Int, done: Int, scheduled: Int, rate: Int): String {
        val what = t(
            if (habits == 1) "habit" else "habits",
            if (habits == 1) "hábito" else "hábitos",
            if (habits == 1) "hábito" else "hábitos",
            if (habits == 1) "Gewohnheit" else "Gewohnheiten",
            if (habits == 1) "habitude" else "habitudes",
        )
        val of = t("of", "de", "de", "von", "sur")
        return "$habits $what · $done $of $scheduled · $rate%"
    }

    // Reminders and widget
    val reminderChannel = t(
        "Habit reminders",
        "Recordatorios de hábitos",
        "Lembretes de hábitos",
        "Gewohnheits-Erinnerungen",
        "Rappels d'habitudes",
    )
    val reminderBody = t(
        "Have you done it today?",
        "¿Lo has hecho hoy?",
        "Você já fez isso hoje?",
        "Schon erledigt heute?",
        "Vous l'avez fait aujourd'hui ?",
    )
    val widgetTitle = t("Today", "Hoy", "Hoje", "Heute", "Aujourd'hui")
    val widgetEmpty = t(
        "Nothing scheduled for today",
        "Nada programado para hoy",
        "Nada programado para hoje",
        "Für heute nichts geplant",
        "Rien de prévu aujourd'hui",
    )

    // Pro and settings
    val settings = t("Settings", "Ajustes", "Ajustes", "Einstellungen", "Réglages")
    val unlimitedHabits = t(
        "Unlimited habits",
        "Hábitos ilimitados",
        "Hábitos ilimitados",
        "Unbegrenzte Gewohnheiten",
        "Habitudes illimitées",
    )
    val pitch = t(
        "Pro removes the habit limit and unlocks the full colour palette.\n\n" +
            "One-time purchase, no subscription. Every widget, every reminder, the whole year " +
            "and exporting your data stay free forever.",
        "Pro quita el límite de hábitos y desbloquea la paleta de colores completa.\n\n" +
            "Pago único, sin suscripción. Todos los widgets, todos los recordatorios, el año " +
            "entero y la exportación de tus datos siguen siendo gratis siempre.",
        "O Pro remove o limite de hábitos e desbloqueia a paleta de cores completa.\n\n" +
            "Pagamento único, sem assinatura. Todos os widgets, todos os lembretes, o ano " +
            "inteiro e a exportação dos seus dados continuam sempre gratuitos.",
        "Pro hebt das Gewohnheiten-Limit auf und schaltet die komplette Farbpalette frei.\n\n" +
            "Einmalzahlung, kein Abo. Alle Widgets, alle Erinnerungen, das ganze Jahr und der " +
            "Export deiner Daten bleiben für immer kostenlos.",
        "Pro supprime la limite d'habitudes et débloque la palette de couleurs complète.\n\n" +
            "Paiement unique, sans abonnement. Tous les widgets, tous les rappels, l'année " +
            "entière et l'export de vos données restent gratuits pour toujours.",
    )
    fun freeIncludes(limit: Int) = t(
        "The free plan includes $limit habits.",
        "El plan gratis incluye $limit hábitos.",
        "O plano gratuito inclui $limit hábitos.",
        "Der kostenlose Plan enthält $limit Gewohnheiten.",
        "Le plan gratuit inclut $limit habitudes.",
    )
    fun freePlan(limit: Int) = t(
        "Free plan: $limit habits.",
        "Plan gratis: $limit hábitos.",
        "Plano gratuito: $limit hábitos.",
        "Kostenloser Plan: $limit Gewohnheiten.",
        "Plan gratuit : $limit habitudes.",
    )
    val storeUnavailable = t(
        "The store is not available right now.",
        "La tienda no está disponible ahora mismo.",
        "A loja não está disponível no momento.",
        "Der Store ist gerade nicht verfügbar.",
        "La boutique n'est pas disponible pour le moment.",
    )
    val restorePurchase = t(
        "Restore purchase",
        "Restaurar compra",
        "Restaurar compra",
        "Kauf wiederherstellen",
        "Restaurer l'achat",
    )
    val noPreviousPurchase = t(
        "We could not find a previous purchase.",
        "No encontramos ninguna compra anterior.",
        "Não encontramos nenhuma compra anterior.",
        "Wir konnten keinen früheren Kauf finden.",
        "Nous n'avons trouvé aucun achat précédent.",
    )
    val purchaseFailed = t(
        "The purchase could not be completed.",
        "No se pudo completar la compra.",
        "Não foi possível concluir a compra.",
        "Der Kauf konnte nicht abgeschlossen werden.",
        "L'achat n'a pas pu être finalisé.",
    )
    fun buyFor(price: String) = t(
        "Buy for $price",
        "Comprar por $price",
        "Comprar por $price",
        "Für $price kaufen",
        "Acheter pour $price",
    )
    val buyPro = t("Buy Pro", "Comprar Pro", "Comprar Pro", "Pro kaufen", "Acheter Pro")
    val notNow = t("Not now", "Ahora no", "Agora não", "Jetzt nicht", "Pas maintenant")
    val getPro = t("Get Pro", "Conseguir Pro", "Obter Pro", "Pro holen", "Obtenir Pro")
    val proActive = t(
        "Pro active. Thanks for supporting the app 🌿",
        "Pro activo. Gracias por sostener la app 🌿",
        "Pro ativo. Obrigado por apoiar o app 🌿",
        "Pro aktiv. Danke, dass du die App unterstützt 🌿",
        "Pro activé. Merci de soutenir l'application 🌿",
    )
    val proRestored = t(
        "Pro restored.",
        "Pro restaurado.",
        "Pro restaurado.",
        "Pro wiederhergestellt.",
        "Pro restauré.",
    )

    // Backup
    val backup = t("Backup", "Copia de seguridad", "Backup", "Backup", "Sauvegarde")
    val backupHint = t(
        "Your habits live only on this device. Save a copy now and then.",
        "Tus hábitos viven solo en este dispositivo. Guarda una copia de vez en cuando.",
        "Seus hábitos vivem apenas neste dispositivo. Salve uma cópia de vez em quando.",
        "Deine Gewohnheiten liegen nur auf diesem Gerät. Sichere ab und zu eine Kopie.",
        "Vos habitudes n'existent que sur cet appareil. Enregistrez une copie de temps en temps.",
    )
    val reorderHabits = t(
        "Reorder habits",
        "Reordenar hábitos",
        "Reordenar hábitos",
        "Gewohnheiten sortieren",
        "Réorganiser les habitudes",
    )
    val moveUp = t("Move up", "Subir", "Subir", "Nach oben", "Monter")
    val moveDown = t("Move down", "Bajar", "Descer", "Nach unten", "Descendre")
    val exportCsv = t(
        "Export as CSV",
        "Exportar como CSV",
        "Exportar como CSV",
        "Als CSV exportieren",
        "Exporter en CSV",
    )
    val csvHint = t(
        "One row per day, for spreadsheets.",
        "Una fila por día, para hojas de cálculo.",
        "Uma linha por dia, para planilhas.",
        "Eine Zeile pro Tag, für Tabellen.",
        "Une ligne par jour, pour les tableurs.",
    )
    val exportData = t(
        "Export my data",
        "Exportar mis datos",
        "Exportar meus dados",
        "Meine Daten exportieren",
        "Exporter mes données",
    )
    val importBackup = t(
        "Import a backup",
        "Importar una copia",
        "Importar um backup",
        "Backup importieren",
        "Importer une sauvegarde",
    )
    val importTitle = t(
        "Import a backup?",
        "¿Importar una copia?",
        "Importar um backup?",
        "Backup importieren?",
        "Importer une sauvegarde ?",
    )
    val importBody = t(
        "This replaces all your habits with the ones in the file. What you have now is kept as a " +
            "backup, but there is no undo inside the app.",
        "Sustituye todos tus hábitos por los del fichero. Lo que tengas ahora queda guardado " +
            "como copia, pero desde la app no hay forma de deshacerlo.",
        "Isto substitui todos os seus hábitos pelos do arquivo. O que você tem agora fica guardado " +
            "como backup, mas não há como desfazer dentro do app.",
        "Das ersetzt alle deine Gewohnheiten durch die aus der Datei. Der aktuelle Stand wird als " +
            "Sicherung behalten, aber in der App gibt es kein Rückgängig.",
        "Cela remplace toutes vos habitudes par celles du fichier. Ce que vous avez maintenant est " +
            "conservé comme sauvegarde, mais il n'y a pas d'annulation dans l'application.",
    )
    val pickFile = t("Choose file", "Elegir fichero", "Escolher arquivo", "Datei wählen", "Choisir un fichier")
    val imported = t(
        "Backup imported.",
        "Copia importada.",
        "Backup importado.",
        "Backup importiert.",
        "Sauvegarde importée.",
    )
    val notABackup = t(
        "That file is not a backup from this app.",
        "Ese fichero no es una copia de esta app.",
        "Esse arquivo não é um backup deste app.",
        "Diese Datei ist kein Backup dieser App.",
        "Ce fichier n'est pas une sauvegarde de cette application.",
    )
}

/** Monday first, matching ISO day numbers. Pure so every language can be length-checked. */
internal fun dayInitialsFor(lang: String): List<String> = when (lang) {
    "es" -> "L M X J V S D"
    "pt" -> "S T Q Q S S D"
    "de" -> "M D M D F S S"
    "fr" -> "L M M J V S D"
    else -> "M T W T F S S"
}.split(" ")

internal fun monthNames(lang: String): List<String> = when (lang) {
    "es" -> "enero febrero marzo abril mayo junio julio agosto septiembre octubre noviembre diciembre"
    "pt" -> "janeiro fevereiro março abril maio junho julho agosto setembro outubro novembro dezembro"
    "de" -> "Januar Februar März April Mai Juni Juli August September Oktober November Dezember"
    "fr" -> "janvier février mars avril mai juin juillet août septembre octobre novembre décembre"
    else -> "January February March April May June July August September October November December"
}.split(" ")

/**
 * Three letters in every language on purpose: these sit above the year grid, where a four-letter
 * label would collide with the next month's column.
 */
internal fun monthAbbreviations(lang: String): List<String> = when (lang) {
    "es" -> "ENE FEB MAR ABR MAY JUN JUL AGO SEP OCT NOV DIC"
    "pt" -> "JAN FEV MAR ABR MAI JUN JUL AGO SET OUT NOV DEZ"
    "de" -> "JAN FEB MÄR APR MAI JUN JUL AUG SEP OKT NOV DEZ"
    "fr" -> "JAN FÉV MAR AVR MAI JUN JUL AOU SEP OCT NOV DÉC"
    else -> "JAN FEB MAR APR MAY JUN JUL AUG SEP OCT NOV DEC"
}.split(" ")

/** Pure so the plural rules can be tested without a device locale. */
internal fun streakText(n: Int, lang: String): String = when (lang) {
    "es" -> if (n == 1) "1 día seguido" else "$n días seguidos"
    "pt" -> if (n == 1) "1 dia seguido" else "$n dias seguidos"
    "de" -> if (n == 1) "1 Tag in Folge" else "$n Tage in Folge"
    "fr" -> if (n == 1) "1 jour d'affilée" else "$n jours d'affilée"
    else -> "$n day streak"
}

/** English expects 12-hour time with a meridiem; the other four all read 24-hour. */
internal fun formatTime(minutes: Int, lang: String): String {
    val hour = minutes / 60
    val minute = (minutes % 60).toString().padStart(2, '0')
    if (lang != "en") return "${hour.toString().padStart(2, '0')}:$minute"
    val twelve = if (hour % 12 == 0) 12 else hour % 12
    return "$twelve:$minute ${if (hour < 12) "AM" else "PM"}"
}
