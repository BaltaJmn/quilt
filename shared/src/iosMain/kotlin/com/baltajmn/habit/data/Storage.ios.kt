package com.baltajmn.habit.data

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

/** Shared with the widget extension, so the file lives in the App Group container. */
const val APP_GROUP = "group.com.baltajmn.habit"

@OptIn(ExperimentalForeignApi::class)
actual object Storage {
    private val documentsPath: String
        get() {
            val dir = NSSearchPathForDirectoriesInDomains(
                NSDocumentDirectory, NSUserDomainMask, true
            ).first() as String
            return "$dir/habits.json"
        }

    private val path: String
        get() {
            val shared = NSFileManager.defaultManager
                .containerURLForSecurityApplicationGroupIdentifier(APP_GROUP)?.path
                ?: return documentsPath
            return "$shared/habits.json"
        }

    private val backupPath: String get() = "$path.bak"

    /** Falls back to the backup, then to the pre-App-Group location, so no update loses history. */
    actual fun read(): String? =
        textAt(path) ?: textAt(backupPath) ?: textAt(documentsPath)

    actual fun readPrevious(): String? = textAt(backupPath)

    /** iOS writes atomically, so the only thing missing is keeping the version being replaced. */
    actual fun write(text: String, rotateBackup: Boolean) {
        if (rotateBackup) {
            textAt(path)?.let { NSString.create(string = it).writeToFile(backupPath, true, NSUTF8StringEncoding, null) }
        }
        NSString.create(string = text).writeToFile(path, true, NSUTF8StringEncoding, null)
    }

    private fun textAt(filePath: String): String? =
        NSString.stringWithContentsOfFile(filePath, NSUTF8StringEncoding, null)
}
