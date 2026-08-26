package com.baltajmn.habit.share

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.popoverPresentationController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageWriteToSavedPhotosAlbum
import platform.UIKit.UIViewController

actual fun ImageBitmap.encodeToPng(): ByteArray {
    val data = Image.makeFromBitmap(asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)
    return data?.bytes ?: ByteArray(0)
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData? {
    if (isEmpty()) return null
    return usePinned { NSData.create(bytes = it.addressOf(0), length = size.toULong()) }
}

internal fun topViewController(): UIViewController? {
    var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}

@OptIn(ExperimentalForeignApi::class)
actual object Sharing {

    actual fun sharePng(png: ByteArray) {
        val image = png.toNSData()?.let { UIImage.imageWithData(it) } ?: return
        val host = topViewController() ?: return
        val sheet = UIActivityViewController(activityItems = listOf(image), applicationActivities = null)
        // iPad presents this as a popover and needs an anchor.
        sheet.popoverPresentationController?.sourceView = host.view
        host.presentViewController(sheet, animated = true, completion = null)
    }

    actual fun savePngToPhotos(png: ByteArray, onResult: (Boolean) -> Unit) {
        val image = png.toNSData()?.let { UIImage.imageWithData(it) }
        if (image == null) {
            onResult(false)
            return
        }
        UIImageWriteToSavedPhotosAlbum(image, null, null, null)
        onResult(true)
    }
}
