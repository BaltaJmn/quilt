package com.baltajmn.habit.data

import java.io.File

actual object Storage {
    private val dir: File get() = AndroidContext.value.filesDir
    private val file: File get() = File(dir, "habits.json")
    private val backup: File get() = File(dir, "habits.bak.json")
    private val temp: File get() = File(dir, "habits.tmp.json")

    actual fun read(): String? = file.textOrNull() ?: backup.textOrNull()

    actual fun readPrevious(): String? = backup.textOrNull()

    /**
     * Write to a temp file and rename it into place, so a crash or a full disk mid-write cannot
     * truncate a year of history. The file being replaced becomes the backup.
     */
    actual fun write(text: String, rotateBackup: Boolean) {
        temp.writeText(text)
        if (rotateBackup && file.exists()) file.renameTo(backup)
        temp.renameTo(file)
    }

    private fun File.textOrNull(): String? = takeIf { it.exists() }?.readText()
}
