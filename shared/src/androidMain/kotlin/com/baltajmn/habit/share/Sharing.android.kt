package com.baltajmn.habit.share

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.content.FileProvider
import com.baltajmn.habit.data.AndroidContext
import java.io.ByteArrayOutputStream
import java.io.File

actual fun ImageBitmap.encodeToPng(): ByteArray {
    val out = ByteArrayOutputStream()
    asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, out)
    return out.toByteArray()
}

actual object Sharing {

    actual fun sharePng(png: ByteArray) {
        val context = AndroidContext.value
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(dir, "habitos.png").apply { writeBytes(png) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(send, "Compartir").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    actual fun savePngToPhotos(png: ByteArray, onResult: (Boolean) -> Unit) {
        // Scoped storage needs no permission; below Q it would, so fall back to the share sheet.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            onResult(false)
            return
        }
        val resolver = AndroidContext.value.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "quilt-${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Quilt")
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri == null) {
            onResult(false)
            return
        }
        onResult(
            runCatching { resolver.openOutputStream(uri)?.use { it.write(png) } ?: error("no stream") }
                .isSuccess
        )
    }
}
