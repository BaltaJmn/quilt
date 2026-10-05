package com.baltajmn.habit.i18n

/** Two-letter code of the device language. */
expect fun systemLanguage(): String

/**
 * The languages the app ships, the same thirteen as the store listings. Anything else falls back to
 * English. The order is the order of every [S] row.
 */
internal val SUPPORTED = listOf("en", "es", "pt", "de", "fr", "it", "ja", "ko", "pl", "tr", "id", "ru", "nl")

/**
 * Every user-facing string, in one table.
 *
 * Not Compose Resources on purpose: `stringResource()` only works inside a `@Composable`, and a
 * third of these strings are drawn from a BroadcastReceiver, a Glance widget, a Canvas and a
 * notification builder. Thirteen languages and ~85 literals still fit in a table more cheaply than
 * in a codegen pipeline, and the signature of [t] is what keeps a language from being forgotten.
 *
 * Every row is en, es, pt, de, fr, it, ja, ko, pl, tr, id, ru, nl.
 *
 * ponytail: language is read once at first access. Both OSes restart the app on a language
 * change, so this only shows if live switching is ever needed.
 */
object S {

    private val lang = normalizeLanguage(systemLanguage())

    private fun t(
        en: String,
        es: String,
        pt: String,
        de: String,
        fr: String,
        it: String,
        ja: String,
        ko: String,
        pl: String,
        tr: String,
        id: String,
        ru: String,
        nl: String,
    ): String = when (lang) {
        "es" -> es
        "pt" -> pt
        "de" -> de
        "fr" -> fr
        "it" -> it
        "ja" -> ja
        "ko" -> ko
        "pl" -> pl
        "tr" -> tr
        "id" -> id
        "ru" -> ru
        "nl" -> nl
        else -> en
    }

    // Home
    val startFirst = t(
        "Start your first habit",
        "Empieza tu primer hábito",
        "Comece seu primeiro hábito",
        "Starte deine erste Gewohnheit",
        "Commencez votre première habitude",
        "Inizia la tua prima abitudine",
        "最初の習慣を始めましょう",
        "첫 습관을 시작해 보세요",
        "Zacznij swój pierwszy nawyk",
        "İlk alışkanlığını başlat",
        "Mulai kebiasaan pertamamu",
        "Начните первую привычку",
        "Begin je eerste gewoonte",
    )
    val emptyHint = t(
        "One square a day.\nTap + to create a habit.",
        "Un cuadrito por día.\nPulsa + para crear un hábito.",
        "Um quadradinho por dia.\nToque em + para criar um hábito.",
        "Ein Kästchen pro Tag.\nTippe auf +, um eine Gewohnheit anzulegen.",
        "Un carré par jour.\nAppuyez sur + pour créer une habitude.",
        "Un quadratino al giorno.\nTocca + per creare un'abitudine.",
        "1日1マス。\n+ をタップして習慣を作成します。",
        "하루에 한 칸.\n+를 눌러 습관을 만드세요.",
        "Jeden kwadracik dziennie.\nDotknij +, aby utworzyć nawyk.",
        "Günde bir kare.\nAlışkanlık oluşturmak için +'ya dokun.",
        "Satu kotak sehari.\nKetuk + untuk membuat kebiasaan.",
        "Один квадратик в день.\nНажмите +, чтобы создать привычку.",
        "Eén vakje per dag.\nTik op + om een gewoonte te maken.",
    )
    fun doneToday(done: Int, total: Int) = t(
        "$done of $total done today",
        "$done de $total hechos hoy",
        "$done de $total feitos hoje",
        "$done von $total heute erledigt",
        "$done sur $total faites aujourd'hui",
        "$done su $total fatte oggi",
        "今日は $total 件中 $done 件完了",
        "오늘 ${total}개 중 ${done}개 완료",
        "Dziś zrobione: $done z $total",
        "Bugün tamamlanan: $done / $total",
        "$done dari $total selesai hari ini",
        "Сегодня выполнено $done из $total",
        "$done van $total gedaan vandaag",
    )

    // Habit card and stats
    /** [weekly] habits count their run in weeks, so the unit has to follow. */
    fun streak(n: Int, weekly: Boolean = false) =
        if (weekly) weekStreakText(n, lang) else streakText(n, lang)
    fun days(n: Int, weekly: Boolean = false) = if (weekly) weeks(n) else t(
        if (n == 1) "day" else "days",
        if (n == 1) "día" else "días",
        if (n == 1) "dia" else "dias",
        if (n == 1) "Tag" else "Tage",
        if (n == 1) "jour" else "jours",
        if (n == 1) "giorno" else "giorni",
        "日",
        "일",
        if (n == 1) "dzień" else "dni",
        "gün",
        "hari",
        ruPlural(n, "день", "дня", "дней"),
        if (n == 1) "dag" else "dagen",
    )
    fun weeks(n: Int) = t(
        if (n == 1) "week" else "weeks",
        if (n == 1) "semana" else "semanas",
        if (n == 1) "semana" else "semanas",
        if (n == 1) "Woche" else "Wochen",
        if (n == 1) "semaine" else "semaines",
        if (n == 1) "settimana" else "settimane",
        "週",
        "주",
        plPlural(n, "tydzień", "tygodnie", "tygodni"),
        "hafta",
        "minggu",
        ruPlural(n, "неделя", "недели", "недель"),
        if (n == 1) "week" else "weken",
    )
    fun rateThisYear(rate: Int) = t(
        "$rate% this year",
        "$rate% este año",
        "$rate% este ano",
        "$rate% dieses Jahr",
        "$rate% cette année",
        "$rate% quest'anno",
        "今年 $rate%",
        "올해 $rate%",
        "$rate% w tym roku",
        "bu yıl %$rate",
        "$rate% tahun ini",
        "$rate% в этом году",
        "$rate% dit jaar",
    )
    /** Turkish writes the sign first. Everyone else in the table writes it after the number. */
    fun percent(n: Int) = if (lang == "tr") "%$n" else "$n%"
    val currentStreak = t(
        "Current streak",
        "Racha actual",
        "Sequência atual",
        "Aktuelle Serie",
        "Série actuelle",
        "Serie attuale",
        "現在の連続記録",
        "현재 연속",
        "Obecna seria",
        "Güncel seri",
        "Streak saat ini",
        "Текущая серия",
        "Huidige reeks",
    )
    val bestStreak = t(
        "Best streak",
        "Mejor racha",
        "Melhor sequência",
        "Beste Serie",
        "Meilleure série",
        "Serie migliore",
        "最長記録",
        "최고 연속",
        "Najlepsza seria",
        "En iyi seri",
        "Streak terbaik",
        "Лучшая серия",
        "Langste reeks",
    )
    val totalDays = t(
        "Total days",
        "Días totales",
        "Dias totais",
        "Tage insgesamt",
        "Jours au total",
        "Giorni totali",
        "合計日数",
        "총 일수",
        "Dni łącznie",
        "Toplam gün",
        "Total hari",
        "Всего дней",
        "Totaal dagen",
    )
    val sinceStart = t(
        "since the start",
        "desde el inicio",
        "desde o início",
        "seit Beginn",
        "depuis le début",
        "dall'inizio",
        "開始から",
        "시작부터",
        "od początku",
        "başlangıçtan beri",
        "sejak awal",
        "с начала",
        "sinds de start",
    )
    val completion = t(
        "Completion",
        "Cumplimiento",
        "Cumprimento",
        "Erfüllung",
        "Réalisation",
        "Completamento",
        "達成率",
        "달성률",
        "Realizacja",
        "Tamamlama",
        "Penyelesaian",
        "Выполнение",
        "Voltooiing",
    )
    fun inYear(year: Int) = t(
        "in $year",
        "en $year",
        "em $year",
        "in $year",
        "en $year",
        "nel $year",
        "${year}年",
        "${year}년",
        "w $year",
        "$year yılında",
        "pada $year",
        "в $year году",
        "in $year",
    )

    // Schedule
    val everyDay = t(
        "Every day",
        "Todos los días",
        "Todos os dias",
        "Jeden Tag",
        "Tous les jours",
        "Ogni giorno",
        "毎日",
        "매일",
        "Codziennie",
        "Her gün",
        "Setiap hari",
        "Каждый день",
        "Elke dag",
    )
    val weekdays = t(
        "Weekdays",
        "De lunes a viernes",
        "De segunda a sexta",
        "Montag bis Freitag",
        "Du lundi au vendredi",
        "Dal lunedì al venerdì",
        "平日",
        "평일",
        "Od poniedziałku do piątku",
        "Hafta içi",
        "Senin sampai Jumat",
        "По будням",
        "Werkdagen",
    )
    val weekends = t(
        "Weekends",
        "Fines de semana",
        "Fins de semana",
        "Wochenenden",
        "Week-ends",
        "Fine settimana",
        "週末",
        "주말",
        "Weekendy",
        "Hafta sonu",
        "Akhir pekan",
        "По выходным",
        "Weekenden",
    )
    /** Monday first, matching ISO day numbers. */
    val dayInitials = dayInitialsFor(lang)
    val daysAWeek = t(
        "Days a week",
        "Días por semana",
        "Dias por semana",
        "Tage pro Woche",
        "Jours par semaine",
        "Giorni a settimana",
        "週あたりの日数",
        "주당 일수",
        "Dni w tygodniu",
        "Haftada kaç gün",
        "Hari per minggu",
        "Дней в неделю",
        "Dagen per week",
    )
    fun perWeek(n: Int) = t(
        if (n == 1) "1 day a week" else "$n days a week",
        if (n == 1) "1 día por semana" else "$n días por semana",
        if (n == 1) "1 dia por semana" else "$n dias por semana",
        if (n == 1) "1 Tag pro Woche" else "$n Tage pro Woche",
        if (n == 1) "1 jour par semaine" else "$n jours par semaine",
        if (n == 1) "1 giorno a settimana" else "$n giorni a settimana",
        "週${n}日",
        "주 ${n}일",
        if (n == 1) "1 dzień w tygodniu" else "$n dni w tygodniu",
        "Haftada $n gün",
        "$n hari per minggu",
        "$n ${ruPlural(n, "день", "дня", "дней")} в неделю",
        if (n == 1) "1 dag per week" else "$n dagen per week",
    )
    fun timesPerDay(n: Int) = t(
        "$n times a day",
        "$n veces al día",
        "$n vezes por dia",
        "$n Mal am Tag",
        "$n fois par jour",
        "$n volte al giorno",
        "1日${n}回",
        "하루 ${n}번",
        "$n ${plPlural(n, "raz", "razy", "razy")} dziennie",
        "Günde $n kez",
        "$n kali sehari",
        "$n ${ruPlural(n, "раз", "раза", "раз")} в день",
        "$n keer per dag",
    )
    fun time(minutes: Int) = formatTime(minutes, lang)

    // Habit form
    val newHabit = t(
        "New habit",
        "Nuevo hábito",
        "Novo hábito",
        "Neue Gewohnheit",
        "Nouvelle habitude",
        "Nuova abitudine",
        "新しい習慣",
        "새 습관",
        "Nowy nawyk",
        "Yeni alışkanlık",
        "Kebiasaan baru",
        "Новая привычка",
        "Nieuwe gewoonte",
    )
    val editHabit = t(
        "Edit habit",
        "Editar hábito",
        "Editar hábito",
        "Gewohnheit bearbeiten",
        "Modifier l'habitude",
        "Modifica abitudine",
        "習慣を編集",
        "습관 편집",
        "Edytuj nawyk",
        "Alışkanlığı düzenle",
        "Ubah kebiasaan",
        "Изменить привычку",
        "Gewoonte bewerken",
    )
    val namePlaceholder = t(
        "Drink water",
        "Beber agua",
        "Beber água",
        "Wasser trinken",
        "Boire de l'eau",
        "Bere acqua",
        "水を飲む",
        "물 마시기",
        "Pić wodę",
        "Su iç",
        "Minum air",
        "Пить воду",
        "Water drinken",
    )
    val icon = t("Icon", "Icono", "Ícone", "Symbol", "Icône", "Icona", "アイコン", "아이콘", "Ikona", "Simge", "Ikon", "Значок", "Icoon")
    val color = t("Colour", "Color", "Cor", "Farbe", "Couleur", "Colore", "色", "색상", "Kolor", "Renk", "Warna", "Цвет", "Kleur")
    val daysLabel = t("Days", "Días", "Dias", "Tage", "Jours", "Giorni", "曜日", "요일", "Dni", "Günler", "Hari", "Дни", "Dagen")
    val timesADay = t(
        "Times a day",
        "Veces al día",
        "Vezes por dia",
        "Mal am Tag",
        "Fois par jour",
        "Volte al giorno",
        "1日の回数",
        "하루 횟수",
        "Razy dziennie",
        "Günde kaç kez",
        "Kali sehari",
        "Раз в день",
        "Keer per dag",
    )
    val reminder = t(
        "Reminder",
        "Recordatorio",
        "Lembrete",
        "Erinnerung",
        "Rappel",
        "Promemoria",
        "リマインダー",
        "알림",
        "Przypomnienie",
        "Hatırlatıcı",
        "Pengingat",
        "Напоминание",
        "Herinnering",
    )
    val createHabit = t(
        "Create habit",
        "Crear hábito",
        "Criar hábito",
        "Gewohnheit anlegen",
        "Créer l'habitude",
        "Crea abitudine",
        "習慣を作成",
        "습관 만들기",
        "Utwórz nawyk",
        "Alışkanlık oluştur",
        "Buat kebiasaan",
        "Создать привычку",
        "Gewoonte maken",
    )
    val save = t("Save", "Guardar", "Salvar", "Speichern", "Enregistrer", "Salva", "保存", "저장", "Zapisz", "Kaydet", "Simpan", "Сохранить", "Opslaan")
    val remove = t("Remove", "Quitar", "Remover", "Entfernen", "Retirer", "Rimuovi", "解除", "해제", "Usuń", "Kaldır", "Hapus", "Убрать", "Verwijderen")
    val cancel = t("Cancel", "Cancelar", "Cancelar", "Abbrechen", "Annuler", "Annulla", "キャンセル", "취소", "Anuluj", "İptal", "Batal", "Отмена", "Annuleren")
    val none = t("None", "Ninguno", "Nenhum", "Keine", "Aucun", "Nessuno", "なし", "없음", "Brak", "Yok", "Tidak ada", "Нет", "Geen")

    // Detail
    val skipHint = t(
        "Press and hold a day to skip it. Holidays and illness do not break the streak.",
        "Mantén pulsado un día para saltarlo. Vacaciones o enfermedad no rompen la racha.",
        "Mantenha um dia pressionado para pulá-lo. Férias ou doença não quebram a sequência.",
        "Halte einen Tag gedrückt, um ihn zu überspringen. Urlaub oder Krankheit brechen die Serie nicht.",
        "Maintenez un jour appuyé pour le passer. Vacances ou maladie ne cassent pas la série.",
        "Tieni premuto un giorno per saltarlo. Vacanze o malattia non spezzano la serie.",
        "日付を長押しするとスキップできます。休暇や体調不良で連続記録は途切れません。",
        "날짜를 길게 누르면 건너뜁니다. 휴가나 아픈 날은 연속 기록을 끊지 않습니다.",
        "Przytrzymaj dzień, aby go pominąć. Urlop czy choroba nie przerywają serii.",
        "Bir günü atlamak için basılı tut. Tatil ya da hastalık serini bozmaz.",
        "Tekan lama satu hari untuk melewatinya. Liburan atau sakit tidak memutus streak.",
        "Удерживайте день, чтобы пропустить его. Отпуск или болезнь не прерывают серию.",
        "Houd een dag ingedrukt om hem over te slaan. Vakantie of ziekte breekt de reeks niet.",
    )
    val skipYesterday = t(
        "Yesterday didn't count",
        "Ayer no contaba",
        "Ontem não contava",
        "Gestern zählte nicht",
        "Hier ne comptait pas",
        "Ieri non contava",
        "昨日は対象外にする",
        "어제는 제외하기",
        "Wczoraj się nie liczy",
        "Dün sayılmasın",
        "Kemarin tidak dihitung",
        "Вчера не в счёт",
        "Gisteren telde niet",
    )
    val archive = t(
        "Archive",
        "Archivar",
        "Arquivar",
        "Archivieren",
        "Archiver",
        "Archivia",
        "アーカイブ",
        "보관",
        "Archiwizuj",
        "Arşivle",
        "Arsipkan",
        "В архив",
        "Archiveren",
    )
    val unarchive = t(
        "Unarchive",
        "Desarchivar",
        "Desarquivar",
        "Wiederherstellen",
        "Désarchiver",
        "Ripristina",
        "アーカイブを解除",
        "보관 해제",
        "Przywróć",
        "Arşivden çıkar",
        "Batalkan arsip",
        "Из архива",
        "Terugzetten",
    )
    val archiveHint = t(
        "Hidden from the list, history is kept",
        "Se oculta de la lista, el historial se conserva",
        "Fica oculto da lista, o histórico é mantido",
        "Wird aus der Liste ausgeblendet, der Verlauf bleibt erhalten",
        "Masquée de la liste, l'historique est conservé",
        "Nascosta dalla lista, la cronologia resta",
        "リストから非表示になり、記録は残ります",
        "목록에서 숨겨지고 기록은 유지됩니다",
        "Znika z listy, historia zostaje",
        "Listeden gizlenir, geçmiş korunur",
        "Disembunyikan dari daftar, riwayat tetap tersimpan",
        "Скрывается из списка, история сохраняется",
        "Verborgen uit de lijst, de geschiedenis blijft",
    )
    val deleteHabit = t(
        "Delete habit",
        "Borrar hábito",
        "Excluir hábito",
        "Gewohnheit löschen",
        "Supprimer l'habitude",
        "Elimina abitudine",
        "習慣を削除",
        "습관 삭제",
        "Usuń nawyk",
        "Alışkanlığı sil",
        "Hapus kebiasaan",
        "Удалить привычку",
        "Gewoonte verwijderen",
    )
    val deleteHint = t(
        "Removes the habit and all its history",
        "Elimina el hábito y todo su historial",
        "Remove o hábito e todo o seu histórico",
        "Entfernt die Gewohnheit und ihren gesamten Verlauf",
        "Supprime l'habitude et tout son historique",
        "Elimina l'abitudine e tutta la sua cronologia",
        "習慣とすべての記録を削除します",
        "습관과 모든 기록을 삭제합니다",
        "Usuwa nawyk i całą jego historię",
        "Alışkanlığı ve tüm geçmişini siler",
        "Menghapus kebiasaan beserta seluruh riwayatnya",
        "Удаляет привычку и всю её историю",
        "Verwijdert de gewoonte en de hele geschiedenis",
    )
    fun deleteTitle(name: String) = t(
        "Delete \"$name\"?",
        "¿Borrar \"$name\"?",
        "Excluir \"$name\"?",
        "\"$name\" löschen?",
        "Supprimer \"$name\" ?",
        "Eliminare \"$name\"?",
        "「$name」を削除しますか？",
        "\"$name\" 습관을 삭제할까요?",
        "Usunąć \"$name\"?",
        "\"$name\" silinsin mi?",
        "Hapus \"$name\"?",
        "Удалить «$name»?",
        "\"$name\" verwijderen?",
    )
    fun deleteBody(total: Int) = t(
        "The $total logged days are lost. This cannot be undone.",
        "Se pierden los $total días registrados. No se puede deshacer.",
        "Os $total dias registrados são perdidos. Não é possível desfazer.",
        "Die $total erfassten Tage gehen verloren. Das lässt sich nicht rückgängig machen.",
        "Les $total jours enregistrés seront perdus. Cette action est irréversible.",
        "Si perdono i $total giorni registrati. Non si può annullare.",
        "記録した${total}日分が失われます。元に戻せません。",
        "기록된 ${total}일이 사라집니다. 되돌릴 수 없습니다.",
        "Zapisane dni ($total) przepadną. Tego nie da się cofnąć.",
        "Kaydedilen $total gün silinecek. Bu işlem geri alınamaz.",
        "$total hari yang tercatat akan hilang. Tindakan ini tidak bisa dibatalkan.",
        "Отмеченные дни ($total) будут потеряны. Это нельзя отменить.",
        "De $total gelogde dagen gaan verloren. Dit kan niet ongedaan worden gemaakt.",
    )
    val delete = t("Delete", "Borrar", "Excluir", "Löschen", "Supprimer", "Elimina", "削除", "삭제", "Usuń", "Sil", "Hapus", "Удалить", "Verwijderen")

    // Share
    val share = t("Share", "Compartir", "Compartilhar", "Teilen", "Partager", "Condividi", "共有", "공유", "Udostępnij", "Paylaş", "Bagikan", "Поделиться", "Delen")
    val preview = t(
        "Preview",
        "Vista previa",
        "Pré-visualização",
        "Vorschau",
        "Aperçu",
        "Anteprima",
        "プレビュー",
        "미리보기",
        "Podgląd",
        "Önizleme",
        "Pratinjau",
        "Предпросмотр",
        "Voorbeeld",
    )
    val savedToPhotos = t(
        "Saved to your photos",
        "Guardado en tus fotos",
        "Salvo nas suas fotos",
        "In deinen Fotos gespeichert",
        "Enregistré dans vos photos",
        "Salvata nelle tue foto",
        "写真に保存しました",
        "사진에 저장했습니다",
        "Zapisano w zdjęciach",
        "Fotoğraflarına kaydedildi",
        "Tersimpan di foto",
        "Сохранено в фото",
        "Opgeslagen in je foto's",
    )
    val saveFailed = t(
        "Could not save, try Share instead",
        "No se pudo guardar, prueba con Compartir",
        "Não foi possível salvar, tente Compartilhar",
        "Speichern fehlgeschlagen, versuche es mit Teilen",
        "Échec de l'enregistrement, essayez Partager",
        "Impossibile salvare, prova con Condividi",
        "保存できませんでした。共有をお試しください",
        "저장하지 못했습니다. 공유를 이용해 보세요",
        "Nie udało się zapisać, spróbuj Udostępnij",
        "Kaydedilemedi, Paylaş'ı dene",
        "Gagal menyimpan, coba Bagikan",
        "Не удалось сохранить, попробуйте «Поделиться»",
        "Opslaan mislukt, probeer Delen",
    )
    val privacyPolicy = t(
        "Privacy policy",
        "Política de privacidad",
        "Política de privacidade",
        "Datenschutzerklärung",
        "Politique de confidentialité",
        "Informativa sulla privacy",
        "プライバシーポリシー",
        "개인정보 처리방침",
        "Polityka prywatności",
        "Gizlilik politikası",
        "Kebijakan privasi",
        "Политика конфиденциальности",
        "Privacybeleid",
    )
    val week = t("Week", "Semana", "Semana", "Woche", "Semaine", "Settimana", "週", "주간", "Tydzień", "Hafta", "Minggu", "Неделя", "Week")
    val month = t("Month", "Mes", "Mês", "Monat", "Mois", "Mese", "月", "월간", "Miesiąc", "Ay", "Bulan", "Месяц", "Maand")
    val year = t("Year", "Año", "Ano", "Jahr", "Année", "Anno", "年", "연간", "Rok", "Yıl", "Tahun", "Год", "Jaar")
    /** Carries the name: a shared image is the one thing that travels without the app. */
    val shareFooter = t(
        "Quilt · one patch a day",
        "Quilt · un retal al día",
        "Quilt · um retalho por dia",
        "Quilt · ein Flicken pro Tag",
        "Quilt · un carré par jour",
        "Quilt · una toppa al giorno",
        "Quilt · 1日1枚のパッチ",
        "Quilt · 하루 한 조각",
        "Quilt · jeden kawałek dziennie",
        "Quilt · her gün bir yama",
        "Quilt · sepotong kain sehari",
        "Quilt · по лоскутку в день",
        "Quilt · elke dag een lapje",
    )
    val months = monthNames(lang)
    val monthsShort = monthAbbreviations(lang)
    private val monthsOf = monthNamesGenitive(lang)
    fun weekOf(day: Int, monthIndex: Int) = t(
        "Week of ${months[monthIndex]} $day",
        "Semana del $day de ${months[monthIndex]}",
        "Semana de $day de ${months[monthIndex]}",
        "Woche vom $day. ${months[monthIndex]}",
        "Semaine du $day ${months[monthIndex]}",
        "Settimana del $day ${months[monthIndex]}",
        "${months[monthIndex]}${day}日の週",
        "${months[monthIndex]} ${day}일 주간",
        "Tydzień od $day ${monthsOf[monthIndex]}",
        "$day ${months[monthIndex]} haftası",
        "Minggu mulai $day ${months[monthIndex]}",
        "Неделя с $day ${monthsOf[monthIndex]}",
        "Week van $day ${months[monthIndex]}",
    )
    /** Title of a month-long share card. Japanese and Korean put the year first. */
    fun monthTitle(monthIndex: Int, year: Int) = when (lang) {
        "ja" -> "${year}年${monthIndex + 1}月"
        "ko" -> "${year}년 ${monthIndex + 1}월"
        else -> "${months[monthIndex].replaceFirstChar { it.uppercase() }} $year"
    }
    fun shareSummary(habits: Int, done: Int, scheduled: Int, rate: Int) = t(
        "$habits ${if (habits == 1) "habit" else "habits"} · $done of $scheduled · $rate%",
        "$habits ${if (habits == 1) "hábito" else "hábitos"} · $done de $scheduled · $rate%",
        "$habits ${if (habits == 1) "hábito" else "hábitos"} · $done de $scheduled · $rate%",
        "$habits ${if (habits == 1) "Gewohnheit" else "Gewohnheiten"} · $done von $scheduled · $rate%",
        "$habits ${if (habits == 1) "habitude" else "habitudes"} · $done sur $scheduled · $rate%",
        "$habits ${if (habits == 1) "abitudine" else "abitudini"} · $done su $scheduled · $rate%",
        "習慣 ${habits}件 · ${scheduled}日中${done}日 · $rate%",
        "습관 ${habits}개 · ${scheduled}일 중 ${done}일 · $rate%",
        "$habits ${plPlural(habits, "nawyk", "nawyki", "nawyków")} · $done z $scheduled · $rate%",
        "$habits alışkanlık · $done / $scheduled · %$rate",
        "$habits kebiasaan · $done dari $scheduled · $rate%",
        "$habits ${ruPlural(habits, "привычка", "привычки", "привычек")} · $done из $scheduled · $rate%",
        "$habits ${if (habits == 1) "gewoonte" else "gewoonten"} · $done van $scheduled · $rate%",
    )

    // Reminders and widget
    val reminderChannel = t(
        "Habit reminders",
        "Recordatorios de hábitos",
        "Lembretes de hábitos",
        "Gewohnheits-Erinnerungen",
        "Rappels d'habitudes",
        "Promemoria delle abitudini",
        "習慣のリマインダー",
        "습관 알림",
        "Przypomnienia o nawykach",
        "Alışkanlık hatırlatıcıları",
        "Pengingat kebiasaan",
        "Напоминания о привычках",
        "Gewoonteherinneringen",
    )
    val reminderBody = t(
        "Have you done it today?",
        "¿Lo has hecho hoy?",
        "Você já fez isso hoje?",
        "Schon erledigt heute?",
        "Vous l'avez fait aujourd'hui ?",
        "L'hai fatto oggi?",
        "今日はもうできましたか？",
        "오늘 하셨나요?",
        "Zrobione dzisiaj?",
        "Bugün yaptın mı?",
        "Sudah dilakukan hari ini?",
        "Сегодня уже сделали?",
        "Heb je het vandaag gedaan?",
    )
    val widgetTitle = t("Today", "Hoy", "Hoje", "Heute", "Aujourd'hui", "Oggi", "今日", "오늘", "Dziś", "Bugün", "Hari ini", "Сегодня", "Vandaag")
    val pickHabit = t(
        "Choose a habit",
        "Elige un hábito",
        "Escolha um hábito",
        "Gewohnheit wählen",
        "Choisissez une habitude",
        "Scegli un'abitudine",
        "習慣を選択",
        "습관 선택",
        "Wybierz nawyk",
        "Bir alışkanlık seç",
        "Pilih kebiasaan",
        "Выберите привычку",
        "Kies een gewoonte",
    )
    val quickTile = t(
        "Mark habit",
        "Marcar hábito",
        "Marcar hábito",
        "Gewohnheit abhaken",
        "Cocher une habitude",
        "Segna abitudine",
        "習慣をチェック",
        "습관 체크",
        "Odhacz nawyk",
        "Alışkanlığı işaretle",
        "Centang kebiasaan",
        "Отметить привычку",
        "Gewoonte afvinken",
    )
    val allDoneToday = t(
        "All done",
        "Todo hecho",
        "Tudo feito",
        "Alles erledigt",
        "Tout est fait",
        "Tutto fatto",
        "すべて完了",
        "모두 완료",
        "Wszystko zrobione",
        "Hepsi tamam",
        "Semua selesai",
        "Всё выполнено",
        "Alles gedaan",
    )
    fun pendingToday(n: Int) = t(
        if (n == 1) "1 left today" else "$n left today",
        if (n == 1) "1 pendiente hoy" else "$n pendientes hoy",
        if (n == 1) "1 pendente hoje" else "$n pendentes hoje",
        if (n == 1) "1 offen heute" else "$n offen heute",
        if (n == 1) "1 restante aujourd'hui" else "$n restantes aujourd'hui",
        if (n == 1) "1 rimasta oggi" else "$n rimaste oggi",
        "今日あと${n}件",
        "오늘 ${n}개 남음",
        "Na dziś zostało: $n",
        "Bugün $n kaldı",
        "$n tersisa hari ini",
        "Осталось на сегодня: $n",
        "Nog $n te gaan vandaag",
    )
    val widgetEmpty = t(
        "Nothing scheduled for today",
        "Nada programado para hoy",
        "Nada programado para hoje",
        "Für heute nichts geplant",
        "Rien de prévu aujourd'hui",
        "Niente in programma per oggi",
        "今日の予定はありません",
        "오늘 예정된 습관이 없습니다",
        "Nic zaplanowanego na dziś",
        "Bugün için planlanan bir şey yok",
        "Tidak ada jadwal hari ini",
        "На сегодня ничего не запланировано",
        "Niets gepland voor vandaag",
    )

    // Pro and settings
    val settings = t(
        "Settings",
        "Ajustes",
        "Ajustes",
        "Einstellungen",
        "Réglages",
        "Impostazioni",
        "設定",
        "설정",
        "Ustawienia",
        "Ayarlar",
        "Pengaturan",
        "Настройки",
        "Instellingen",
    )
    val unlimitedHabits = t(
        "Unlimited habits",
        "Hábitos ilimitados",
        "Hábitos ilimitados",
        "Unbegrenzte Gewohnheiten",
        "Habitudes illimitées",
        "Abitudini illimitate",
        "習慣を無制限に",
        "무제한 습관",
        "Nieograniczone nawyki",
        "Sınırsız alışkanlık",
        "Kebiasaan tanpa batas",
        "Безлимитные привычки",
        "Onbeperkt gewoonten",
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
        "Pro rimuove il limite di abitudini e sblocca la palette di colori completa.\n\n" +
            "Acquisto unico, nessun abbonamento. Tutti i widget, tutti i promemoria, l'anno " +
            "intero e l'esportazione dei tuoi dati restano gratis per sempre.",
        "Proにすると習慣の上限がなくなり、すべての色を使えるようになります。\n\n" +
            "買い切りで、サブスクリプションではありません。すべてのウィジェット、すべての" +
            "リマインダー、一年まるごとの表示、データの書き出しはずっと無料です。",
        "Pro는 습관 개수 제한을 없애고 모든 색상을 열어 줍니다.\n\n" +
            "한 번 구매로 끝나며 구독이 아닙니다. 모든 위젯, 모든 알림, 1년 전체 보기, " +
            "데이터 내보내기는 앞으로도 계속 무료입니다.",
        "Pro znosi limit nawyków i odblokowuje pełną paletę kolorów.\n\n" +
            "Jednorazowy zakup, bez subskrypcji. Wszystkie widgety, wszystkie przypomnienia, " +
            "cały rok i eksport Twoich danych pozostają darmowe na zawsze.",
        "Pro, alışkanlık sınırını kaldırır ve tüm renk paletini açar.\n\n" +
            "Tek seferlik satın alma, abonelik yok. Tüm widget'lar, tüm hatırlatıcılar, yılın " +
            "tamamı ve verilerini dışa aktarma sonsuza dek ücretsiz kalır.",
        "Pro menghapus batas kebiasaan dan membuka semua pilihan warna.\n\n" +
            "Sekali bayar, tanpa langganan. Semua widget, semua pengingat, tampilan setahun " +
            "penuh, dan ekspor datamu tetap gratis selamanya.",
        "Pro снимает ограничение на число привычек и открывает всю палитру цветов.\n\n" +
            "Разовая покупка, без подписки. Все виджеты, все напоминания, весь год и экспорт " +
            "ваших данных остаются бесплатными навсегда.",
        "Pro haalt de limiet op gewoonten weg en ontgrendelt het volledige kleurenpalet.\n\n" +
            "Eenmalige aankoop, geen abonnement. Alle widgets, alle herinneringen, het hele " +
            "jaar en het exporteren van je gegevens blijven altijd gratis.",
    )
    fun freeIncludes(limit: Int) = t(
        "The free plan includes $limit habits.",
        "El plan gratis incluye $limit hábitos.",
        "O plano gratuito inclui $limit hábitos.",
        "Der kostenlose Plan enthält $limit Gewohnheiten.",
        "Le plan gratuit inclut $limit habitudes.",
        "Il piano gratuito include $limit abitudini.",
        "無料プランでは習慣を${limit}個まで作成できます。",
        "무료 플랜에는 습관 ${limit}개가 포함됩니다.",
        "Darmowy plan obejmuje $limit ${plPlural(limit, "nawyk", "nawyki", "nawyków")}.",
        "Ücretsiz plan $limit alışkanlık içerir.",
        "Paket gratis mencakup $limit kebiasaan.",
        "Бесплатный план включает $limit ${ruPlural(limit, "привычку", "привычки", "привычек")}.",
        "Het gratis plan bevat $limit gewoonten.",
    )
    fun freePlan(limit: Int) = t(
        "Free plan: $limit habits.",
        "Plan gratis: $limit hábitos.",
        "Plano gratuito: $limit hábitos.",
        "Kostenloser Plan: $limit Gewohnheiten.",
        "Plan gratuit : $limit habitudes.",
        "Piano gratuito: $limit abitudini.",
        "無料プラン：習慣${limit}個",
        "무료 플랜: 습관 ${limit}개",
        "Darmowy plan: $limit ${plPlural(limit, "nawyk", "nawyki", "nawyków")}.",
        "Ücretsiz plan: $limit alışkanlık.",
        "Paket gratis: $limit kebiasaan.",
        "Бесплатный план: $limit ${ruPlural(limit, "привычка", "привычки", "привычек")}.",
        "Gratis plan: $limit gewoonten.",
    )
    val storeUnavailable = t(
        "The store is not available right now.",
        "La tienda no está disponible ahora mismo.",
        "A loja não está disponível no momento.",
        "Der Store ist gerade nicht verfügbar.",
        "La boutique n'est pas disponible pour le moment.",
        "Lo store non è disponibile al momento.",
        "現在ストアを利用できません。",
        "지금은 스토어를 이용할 수 없습니다.",
        "Sklep jest teraz niedostępny.",
        "Mağaza şu anda kullanılamıyor.",
        "Toko sedang tidak tersedia.",
        "Магазин сейчас недоступен.",
        "De winkel is nu niet beschikbaar.",
    )
    val restorePurchase = t(
        "Restore purchase",
        "Restaurar compra",
        "Restaurar compra",
        "Kauf wiederherstellen",
        "Restaurer l'achat",
        "Ripristina acquisto",
        "購入を復元",
        "구매 복원",
        "Przywróć zakup",
        "Satın alımı geri yükle",
        "Pulihkan pembelian",
        "Восстановить покупку",
        "Aankoop herstellen",
    )
    val noPreviousPurchase = t(
        "We could not find a previous purchase.",
        "No encontramos ninguna compra anterior.",
        "Não encontramos nenhuma compra anterior.",
        "Wir konnten keinen früheren Kauf finden.",
        "Nous n'avons trouvé aucun achat précédent.",
        "Non abbiamo trovato acquisti precedenti.",
        "以前の購入が見つかりませんでした。",
        "이전 구매 내역을 찾지 못했습니다.",
        "Nie znaleźliśmy wcześniejszego zakupu.",
        "Önceki bir satın alma bulunamadı.",
        "Kami tidak menemukan pembelian sebelumnya.",
        "Не удалось найти предыдущую покупку.",
        "We hebben geen eerdere aankoop gevonden.",
    )
    val purchaseFailed = t(
        "The purchase could not be completed.",
        "No se pudo completar la compra.",
        "Não foi possível concluir a compra.",
        "Der Kauf konnte nicht abgeschlossen werden.",
        "L'achat n'a pas pu être finalisé.",
        "Non è stato possibile completare l'acquisto.",
        "購入を完了できませんでした。",
        "구매를 완료하지 못했습니다.",
        "Nie udało się dokończyć zakupu.",
        "Satın alma tamamlanamadı.",
        "Pembelian tidak dapat diselesaikan.",
        "Не удалось завершить покупку.",
        "De aankoop kon niet worden voltooid.",
    )
    fun buyFor(price: String) = t(
        "Buy for $price",
        "Comprar por $price",
        "Comprar por $price",
        "Für $price kaufen",
        "Acheter pour $price",
        "Acquista a $price",
        "$price で購入",
        "${price}에 구매",
        "Kup za $price",
        "$price karşılığında satın al",
        "Beli seharga $price",
        "Купить за $price",
        "Kopen voor $price",
    )
    val buyPro = t(
        "Buy Pro",
        "Comprar Pro",
        "Comprar Pro",
        "Pro kaufen",
        "Acheter Pro",
        "Acquista Pro",
        "Proを購入",
        "Pro 구매",
        "Kup Pro",
        "Pro satın al",
        "Beli Pro",
        "Купить Pro",
        "Pro kopen",
    )
    val notNow = t(
        "Not now",
        "Ahora no",
        "Agora não",
        "Jetzt nicht",
        "Pas maintenant",
        "Non ora",
        "今はしない",
        "나중에",
        "Nie teraz",
        "Şimdi değil",
        "Nanti saja",
        "Не сейчас",
        "Niet nu",
    )
    val getPro = t(
        "Get Pro",
        "Conseguir Pro",
        "Obter Pro",
        "Pro holen",
        "Obtenir Pro",
        "Passa a Pro",
        "Proにする",
        "Pro 받기",
        "Przejdź na Pro",
        "Pro'ya geç",
        "Dapatkan Pro",
        "Получить Pro",
        "Pro nemen",
    )
    val proActive = t(
        "Pro active. Thanks for supporting the app 🌿",
        "Pro activo. Gracias por sostener la app 🌿",
        "Pro ativo. Obrigado por apoiar o app 🌿",
        "Pro aktiv. Danke, dass du die App unterstützt 🌿",
        "Pro activé. Merci de soutenir l'application 🌿",
        "Pro attivo. Grazie per sostenere l'app 🌿",
        "Pro有効。アプリを応援してくれてありがとうございます 🌿",
        "Pro 활성화됨. 앱을 응원해 주셔서 감사합니다 🌿",
        "Pro aktywne. Dzięki, że wspierasz aplikację 🌿",
        "Pro etkin. Uygulamayı desteklediğin için teşekkürler 🌿",
        "Pro aktif. Terima kasih sudah mendukung aplikasi ini 🌿",
        "Pro активен. Спасибо, что поддерживаете приложение 🌿",
        "Pro actief. Bedankt dat je de app steunt 🌿",
    )
    val proRestored = t(
        "Pro restored.",
        "Pro restaurado.",
        "Pro restaurado.",
        "Pro wiederhergestellt.",
        "Pro restauré.",
        "Pro ripristinato.",
        "Proを復元しました。",
        "Pro를 복원했습니다.",
        "Przywrócono Pro.",
        "Pro geri yüklendi.",
        "Pro dipulihkan.",
        "Pro восстановлен.",
        "Pro hersteld.",
    )

    // Backup
    val backup = t(
        "Backup",
        "Copia de seguridad",
        "Backup",
        "Backup",
        "Sauvegarde",
        "Backup",
        "バックアップ",
        "백업",
        "Kopia zapasowa",
        "Yedekleme",
        "Cadangan",
        "Резервная копия",
        "Back-up",
    )
    val backupHint = t(
        "Your habits live only on this device. Save a copy now and then.",
        "Tus hábitos viven solo en este dispositivo. Guarda una copia de vez en cuando.",
        "Seus hábitos vivem apenas neste dispositivo. Salve uma cópia de vez em quando.",
        "Deine Gewohnheiten liegen nur auf diesem Gerät. Sichere ab und zu eine Kopie.",
        "Vos habitudes n'existent que sur cet appareil. Enregistrez une copie de temps en temps.",
        "Le tue abitudini esistono solo su questo dispositivo. Salvane una copia ogni tanto.",
        "習慣のデータはこの端末にしかありません。ときどきコピーを保存してください。",
        "습관 데이터는 이 기기에만 있습니다. 가끔 사본을 저장해 두세요.",
        "Twoje nawyki są zapisane tylko na tym urządzeniu. Co jakiś czas zapisz kopię.",
        "Alışkanlıkların yalnızca bu cihazda duruyor. Arada bir kopyasını kaydet.",
        "Kebiasaanmu hanya tersimpan di perangkat ini. Simpan salinannya sesekali.",
        "Ваши привычки хранятся только на этом устройстве. Время от времени сохраняйте копию.",
        "Je gewoonten staan alleen op dit apparaat. Bewaar af en toe een kopie.",
    )
    val reorderHabits = t(
        "Reorder habits",
        "Reordenar hábitos",
        "Reordenar hábitos",
        "Gewohnheiten sortieren",
        "Réorganiser les habitudes",
        "Riordina abitudini",
        "習慣の並べ替え",
        "습관 순서 변경",
        "Zmień kolejność nawyków",
        "Alışkanlıkları sırala",
        "Urutkan kebiasaan",
        "Порядок привычек",
        "Gewoonten ordenen",
    )
    val moveUp = t("Move up", "Subir", "Subir", "Nach oben", "Monter", "Sposta su", "上へ", "위로", "W górę", "Yukarı taşı", "Naikkan", "Выше", "Omhoog")
    val moveDown = t("Move down", "Bajar", "Descer", "Nach unten", "Descendre", "Sposta giù", "下へ", "아래로", "W dół", "Aşağı taşı", "Turunkan", "Ниже", "Omlaag")
    val exportCsv = t(
        "Export as CSV",
        "Exportar como CSV",
        "Exportar como CSV",
        "Als CSV exportieren",
        "Exporter en CSV",
        "Esporta in CSV",
        "CSVで書き出す",
        "CSV로 내보내기",
        "Eksportuj do CSV",
        "CSV olarak dışa aktar",
        "Ekspor sebagai CSV",
        "Экспорт в CSV",
        "Exporteren als CSV",
    )
    val csvHint = t(
        "One row per day, for spreadsheets.",
        "Una fila por día, para hojas de cálculo.",
        "Uma linha por dia, para planilhas.",
        "Eine Zeile pro Tag, für Tabellen.",
        "Une ligne par jour, pour les tableurs.",
        "Una riga per giorno, per i fogli di calcolo.",
        "1日1行。表計算ソフト向けです。",
        "하루에 한 줄, 스프레드시트용입니다.",
        "Jeden wiersz na dzień, do arkuszy kalkulacyjnych.",
        "Her gün için bir satır, tablolar için.",
        "Satu baris per hari, untuk spreadsheet.",
        "Одна строка на день, для таблиц.",
        "Eén regel per dag, voor spreadsheets.",
    )
    val exportData = t(
        "Export my data",
        "Exportar mis datos",
        "Exportar meus dados",
        "Meine Daten exportieren",
        "Exporter mes données",
        "Esporta i miei dati",
        "データを書き出す",
        "내 데이터 내보내기",
        "Eksportuj moje dane",
        "Verilerimi dışa aktar",
        "Ekspor dataku",
        "Экспортировать мои данные",
        "Mijn gegevens exporteren",
    )
    val importBackup = t(
        "Import a backup",
        "Importar una copia",
        "Importar um backup",
        "Backup importieren",
        "Importer une sauvegarde",
        "Importa un backup",
        "バックアップを読み込む",
        "백업 가져오기",
        "Importuj kopię zapasową",
        "Yedeği içe aktar",
        "Impor cadangan",
        "Импортировать копию",
        "Back-up importeren",
    )
    val importTitle = t(
        "Import a backup?",
        "¿Importar una copia?",
        "Importar um backup?",
        "Backup importieren?",
        "Importer une sauvegarde ?",
        "Importare un backup?",
        "バックアップを読み込みますか？",
        "백업을 가져올까요?",
        "Zaimportować kopię zapasową?",
        "Yedek içe aktarılsın mı?",
        "Impor cadangan?",
        "Импортировать копию?",
        "Back-up importeren?",
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
        "Sostituisce tutte le tue abitudini con quelle del file. Quello che hai ora viene " +
            "conservato come backup, ma dall'app non si può annullare.",
        "すべての習慣がファイルの内容に置き換わります。今のデータはバックアップとして残りますが、" +
            "アプリ内で元に戻すことはできません。",
        "모든 습관이 파일의 내용으로 바뀝니다. 지금 데이터는 백업으로 남지만, " +
            "앱 안에서 되돌릴 수는 없습니다.",
        "Zastępuje wszystkie Twoje nawyki tymi z pliku. Obecne dane zostaną zachowane jako " +
            "kopia, ale w aplikacji nie da się tego cofnąć.",
        "Bu işlem tüm alışkanlıklarını dosyadakilerle değiştirir. Şu anki verilerin yedek " +
            "olarak saklanır, ancak uygulama içinden geri alınamaz.",
        "Ini akan mengganti semua kebiasaanmu dengan yang ada di file. Data saat ini disimpan " +
            "sebagai cadangan, tapi tidak bisa dibatalkan dari dalam aplikasi.",
        "Все ваши привычки будут заменены привычками из файла. Текущие данные сохранятся как " +
            "резервная копия, но отменить это в приложении нельзя.",
        "Dit vervangt al je gewoonten door die uit het bestand. Wat je nu hebt blijft bewaard " +
            "als back-up, maar in de app kun je het niet ongedaan maken.",
    )
    val pickFile = t(
        "Choose file",
        "Elegir fichero",
        "Escolher arquivo",
        "Datei wählen",
        "Choisir un fichier",
        "Scegli file",
        "ファイルを選択",
        "파일 선택",
        "Wybierz plik",
        "Dosya seç",
        "Pilih file",
        "Выбрать файл",
        "Bestand kiezen",
    )
    val imported = t(
        "Backup imported.",
        "Copia importada.",
        "Backup importado.",
        "Backup importiert.",
        "Sauvegarde importée.",
        "Backup importato.",
        "バックアップを読み込みました。",
        "백업을 가져왔습니다.",
        "Zaimportowano kopię zapasową.",
        "Yedek içe aktarıldı.",
        "Cadangan diimpor.",
        "Копия импортирована.",
        "Back-up geïmporteerd.",
    )
    val notABackup = t(
        "That file is not a backup from this app.",
        "Ese fichero no es una copia de esta app.",
        "Esse arquivo não é um backup deste app.",
        "Diese Datei ist kein Backup dieser App.",
        "Ce fichier n'est pas une sauvegarde de cette application.",
        "Questo file non è un backup di questa app.",
        "このファイルはこのアプリのバックアップではありません。",
        "이 파일은 이 앱의 백업이 아닙니다.",
        "Ten plik nie jest kopią zapasową tej aplikacji.",
        "Bu dosya bu uygulamanın yedeği değil.",
        "File itu bukan cadangan dari aplikasi ini.",
        "Этот файл не является копией из этого приложения.",
        "Dit bestand is geen back-up van deze app.",
    )
}

/**
 * Russian picks one of three forms by the last digits: 1, 21, 31 take [one]; 2-4, 22-24 take
 * [few]; 5-20, 25-30 and 11-14 take [many].
 */
internal fun ruPlural(n: Int, one: String, few: String, many: String): String {
    val mod10 = n % 10
    val mod100 = n % 100
    return when {
        mod10 == 1 && mod100 != 11 -> one
        mod10 in 2..4 && mod100 !in 12..14 -> few
        else -> many
    }
}

/** Polish is Russian with one difference: only 1 itself takes [one], 21 already takes [many]. */
internal fun plPlural(n: Int, one: String, few: String, many: String): String = when {
    n == 1 -> one
    n % 10 in 2..4 && n % 100 !in 12..14 -> few
    else -> many
}

/**
 * Monday first, matching ISO day numbers. Pure so every language can be length-checked. Two letters
 * where one would repeat a neighbour inside the same week and read as a typo: Polish, Turkish,
 * Indonesian, Russian and Dutch.
 */
internal fun dayInitialsFor(lang: String): List<String> = when (lang) {
    "es" -> "L M X J V S D"
    "pt" -> "S T Q Q S S D"
    "de" -> "M D M D F S S"
    "fr" -> "L M M J V S D"
    "it" -> "L M M G V S D"
    "ja" -> "月 火 水 木 金 土 日"
    "ko" -> "월 화 수 목 금 토 일"
    "pl" -> "Pn Wt Śr Cz Pt So Nd"
    "tr" -> "Pt Sa Ça Pe Cu Ct Pz"
    "id" -> "Sn Sl Rb Km Jm Sb Mg"
    "ru" -> "Пн Вт Ср Чт Пт Сб Вс"
    "nl" -> "ma di wo do vr za zo"
    else -> "M T W T F S S"
}.split(" ")

internal fun monthNames(lang: String): List<String> = when (lang) {
    "es" -> "enero febrero marzo abril mayo junio julio agosto septiembre octubre noviembre diciembre"
    "pt" -> "janeiro fevereiro março abril maio junho julho agosto setembro outubro novembro dezembro"
    "de" -> "Januar Februar März April Mai Juni Juli August September Oktober November Dezember"
    "fr" -> "janvier février mars avril mai juin juillet août septembre octobre novembre décembre"
    "it" -> "gennaio febbraio marzo aprile maggio giugno luglio agosto settembre ottobre novembre dicembre"
    "ja" -> "1月 2月 3月 4月 5月 6月 7月 8月 9月 10月 11月 12月"
    "ko" -> "1월 2월 3월 4월 5월 6월 7월 8월 9월 10월 11월 12월"
    "pl" -> "styczeń luty marzec kwiecień maj czerwiec lipiec sierpień wrzesień październik listopad grudzień"
    "tr" -> "Ocak Şubat Mart Nisan Mayıs Haziran Temmuz Ağustos Eylül Ekim Kasım Aralık"
    "id" -> "Januari Februari Maret April Mei Juni Juli Agustus September Oktober November Desember"
    "ru" -> "январь февраль март апрель май июнь июль август сентябрь октябрь ноябрь декабрь"
    "nl" -> "januari februari maart april mei juni juli augustus september oktober november december"
    else -> "January February March April May June July August September October November December"
}.split(" ")

/**
 * The form a month takes after a day number. Polish and Russian decline it ("5 października",
 * "5 октября"), so a week title built from [monthNames] would read as a grammar mistake there.
 */
internal fun monthNamesGenitive(lang: String): List<String> = when (lang) {
    "pl" -> "stycznia lutego marca kwietnia maja czerwca lipca sierpnia września października listopada grudnia"
        .split(" ")
    "ru" -> "января февраля марта апреля мая июня июля августа сентября октября ноября декабря".split(" ")
    else -> monthNames(lang)
}

/**
 * Three letters in every alphabet on purpose: these sit above the year grid, where a four-letter
 * label would collide with the next month's column. Japanese and Korean write the number and one
 * glyph, which takes about the same room as three Latin capitals.
 */
internal fun monthAbbreviations(lang: String): List<String> = when (lang) {
    "es" -> "ENE FEB MAR ABR MAY JUN JUL AGO SEP OCT NOV DIC"
    "pt" -> "JAN FEV MAR ABR MAI JUN JUL AGO SET OUT NOV DEZ"
    "de" -> "JAN FEB MÄR APR MAI JUN JUL AUG SEP OKT NOV DEZ"
    "fr" -> "JAN FÉV MAR AVR MAI JUN JUL AOU SEP OCT NOV DÉC"
    "it" -> "GEN FEB MAR APR MAG GIU LUG AGO SET OTT NOV DIC"
    "ja" -> "1月 2月 3月 4月 5月 6月 7月 8月 9月 10月 11月 12月"
    "ko" -> "1월 2월 3월 4월 5월 6월 7월 8월 9월 10월 11월 12월"
    "pl" -> "STY LUT MAR KWI MAJ CZE LIP SIE WRZ PAŹ LIS GRU"
    "tr" -> "OCA ŞUB MAR NİS MAY HAZ TEM AĞU EYL EKİ KAS ARA"
    "id" -> "JAN FEB MAR APR MEI JUN JUL AGU SEP OKT NOV DES"
    "ru" -> "ЯНВ ФЕВ МАР АПР МАЙ ИЮН ИЮЛ АВГ СЕН ОКТ НОЯ ДЕК"
    "nl" -> "JAN FEB MRT APR MEI JUN JUL AUG SEP OKT NOV DEC"
    else -> "JAN FEB MAR APR MAY JUN JUL AUG SEP OCT NOV DEC"
}.split(" ")

/** Pure so the plural rules can be tested without a device locale. */
internal fun streakText(n: Int, lang: String): String = when (lang) {
    "es" -> if (n == 1) "1 día seguido" else "$n días seguidos"
    "pt" -> if (n == 1) "1 dia seguido" else "$n dias seguidos"
    "de" -> if (n == 1) "1 Tag in Folge" else "$n Tage in Folge"
    "fr" -> if (n == 1) "1 jour d'affilée" else "$n jours d'affilée"
    "it" -> if (n == 1) "1 giorno di fila" else "$n giorni di fila"
    "ja" -> "${n}日連続"
    "ko" -> "${n}일 연속"
    "pl" -> if (n == 1) "1 dzień z rzędu" else "$n dni z rzędu"
    "tr" -> "$n gün üst üste"
    "id" -> "$n hari berturut-turut"
    "ru" -> "$n ${ruPlural(n, "день", "дня", "дней")} подряд"
    "nl" -> if (n == 1) "1 dag op rij" else "$n dagen op rij"
    else -> "$n day streak"
}

/** The weekly-target twin of [streakText]: the unit is the week, not the day. */
internal fun weekStreakText(n: Int, lang: String): String = when (lang) {
    "es" -> if (n == 1) "1 semana seguida" else "$n semanas seguidas"
    "pt" -> if (n == 1) "1 semana seguida" else "$n semanas seguidas"
    "de" -> if (n == 1) "1 Woche in Folge" else "$n Wochen in Folge"
    "fr" -> if (n == 1) "1 semaine d'affilée" else "$n semaines d'affilée"
    "it" -> if (n == 1) "1 settimana di fila" else "$n settimane di fila"
    "ja" -> "${n}週連続"
    "ko" -> "${n}주 연속"
    "pl" -> "$n ${plPlural(n, "tydzień", "tygodnie", "tygodni")} z rzędu"
    "tr" -> "$n hafta üst üste"
    "id" -> "$n minggu berturut-turut"
    "ru" -> "$n ${ruPlural(n, "неделя", "недели", "недель")} подряд"
    "nl" -> if (n == 1) "1 week op rij" else "$n weken op rij"
    else -> "$n week streak"
}

/** English expects 12-hour time with a meridiem; every other language in the table reads 24-hour. */
internal fun formatTime(minutes: Int, lang: String): String {
    val hour = minutes / 60
    val minute = (minutes % 60).toString().padStart(2, '0')
    if (lang != "en") return "${hour.toString().padStart(2, '0')}:$minute"
    val twelve = if (hour % 12 == 0) 12 else hour % 12
    return "$twelve:$minute ${if (hour < 12) "AM" else "PM"}"
}

/**
 * The language table to read for a system tag. The region is dropped, so "es-419" and "es_ES" are
 * both Spanish, and anything outside [SUPPORTED] lands in English.
 *
 * Android still reports Indonesian with its pre-1989 code, "in", where iOS says "id": both have to
 * land on the same row.
 */
internal fun normalizeLanguage(raw: String): String {
    val code = raw.take(2).lowercase().let { if (it == "in") "id" else it }
    return code.takeIf { it in SUPPORTED } ?: "en"
}
