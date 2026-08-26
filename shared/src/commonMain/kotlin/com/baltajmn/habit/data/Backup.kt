package com.baltajmn.habit.data

/**
 * Export and import of the whole habits file.
 *
 * Free forever on purpose: the app sells itself on "no account, your data is yours", and a backup
 * locked behind a paywall would contradict that in the one place people check.
 */
expect object Backup {
    /** Hands the JSON to the system so the user can save it or send it wherever they like. */
    fun export(text: String, filename: String)

    /** Opens a file picker. Calls back with the file's text, or null if cancelled or unreadable. */
    fun pickFile(onResult: (String?) -> Unit)
}

/** `quilt-2026-08-25.json` */
fun backupFilename(): String = "quilt-${today()}.json"

/** `quilt-2026-08-25.csv`. The extension is what tells the system, and the user, what this is. */
fun csvFilename(): String = "quilt-${today()}.csv"
