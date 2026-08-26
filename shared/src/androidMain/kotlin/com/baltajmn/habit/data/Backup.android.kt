package com.baltajmn.habit.data

actual object Backup {

    /**
     * Set by MainActivity. Both halves need an Activity result, which a bare Context cannot give,
     * so the Activity registers the launchers and hands them over here.
     */
    var onSaveFile: ((filename: String, text: String) -> Unit)? = null
    var onPickFile: ((onResult: (String?) -> Unit) -> Unit)? = null

    /**
     * Opens the system file saver rather than the share sheet: a backup has to land somewhere the
     * user can find it again and hand back to the importer, and every cloud drive shows up there.
     */
    actual fun export(text: String, filename: String) {
        onSaveFile?.invoke(filename, text)
    }

    actual fun pickFile(onResult: (String?) -> Unit) {
        val picker = onPickFile
        if (picker == null) onResult(null) else picker(onResult)
    }
}
