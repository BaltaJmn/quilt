package com.baltajmn.habit.data

/**
 * Whole-file read/write of the single JSON blob that holds every habit.
 *
 * The app has no cloud copy, so this file *is* the user's year. Writes must never be able to
 * leave it truncated, and the version before the last write is kept as a fallback.
 */
expect object Storage {
    /** The current file, falling back to the backup when it is missing. */
    fun read(): String?

    /** The copy taken before the last successful write. */
    fun readPrevious(): String?

    /**
     * [rotateBackup] false keeps the existing backup instead of demoting the current file.
     * That matters when the current file is the damaged one being repaired: rotating it in
     * would throw away the only good copy.
     */
    fun write(text: String, rotateBackup: Boolean = true)
}
