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
import platform.Photos.PHAccessLevelAddOnly
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIActivityViewController
import platform.UIKit.popoverPresentationController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

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
    val app = UIApplication.sharedApplication
    // keyWindow is deprecated and is nil whenever no window is key yet, which is enough to make
    // Share, Export and Import silently do nothing. Any window's root will do as a host.
    var controller = app.keyWindow?.rootViewController
        ?: app.windows.filterIsInstance<UIWindow>().firstNotNullOfOrNull { it.rootViewController }
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}

/** Both Photos callbacks arrive off the main thread; the caller writes Compose state. */
private fun onMain(block: () -> Unit) = dispatch_async(dispatch_get_main_queue()) { block() }

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

    /**
     * The old `UIImageWriteToSavedPhotosAlbum` with a nil target has no way to report anything, so
     * a denied permission still looked like a save. This path asks first and answers with what the
     * library actually did.
     */
    actual fun savePngToPhotos(png: ByteArray, onResult: (Boolean) -> Unit) {
        val image = png.toNSData()?.let { UIImage.imageWithData(it) }
        if (image == null) {
            onResult(false)
            return
        }
        PHPhotoLibrary.requestAuthorizationForAccessLevel(PHAccessLevelAddOnly) { status ->
            if (status != PHAuthorizationStatusAuthorized && status != PHAuthorizationStatusLimited) {
                onMain { onResult(false) }
                return@requestAuthorizationForAccessLevel
            }
            PHPhotoLibrary.sharedPhotoLibrary().performChanges(
                changeBlock = { PHAssetChangeRequest.creationRequestForAssetFromImage(image) },
                completionHandler = { success, _ -> onMain { onResult(success) } },
            )
        }
    }
}
