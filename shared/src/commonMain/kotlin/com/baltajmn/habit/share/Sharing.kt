package com.baltajmn.habit.share

import androidx.compose.ui.graphics.ImageBitmap

/** PNG bytes of the card, ready to hand to the system. */
expect fun ImageBitmap.encodeToPng(): ByteArray

expect object Sharing {
    /** Opens the system share sheet with the image attached. */
    fun sharePng(png: ByteArray)

    /** Writes the image straight to the photo library. Returns false when it is not possible. */
    fun savePngToPhotos(png: ByteArray, onResult: (Boolean) -> Unit)
}
