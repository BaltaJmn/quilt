package com.baltajmn.habit.data

import com.baltajmn.habit.share.topViewController
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.popoverPresentationController
import platform.UniformTypeIdentifiers.UTTypeData
import platform.UniformTypeIdentifiers.UTTypeJSON
import platform.UniformTypeIdentifiers.UTTypePlainText
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual object Backup {

    /** The picker only keeps a weak reference to its delegate, so it has to be held here. */
    private var delegate: PickerDelegate? = null

    actual fun export(text: String, filename: String) {
        val path = NSTemporaryDirectory() + filename
        NSString.create(string = text).writeToFile(path, true, NSUTF8StringEncoding, null)
        val host = topViewController() ?: return
        val sheet = UIActivityViewController(
            activityItems = listOf(NSURL.fileURLWithPath(path)),
            applicationActivities = null,
        )
        // iPad presents this as a popover and needs an anchor.
        sheet.popoverPresentationController?.sourceView = host.view
        host.presentViewController(sheet, animated = true, completion = null)
    }

    actual fun pickFile(onResult: (String?) -> Unit) {
        val host = topViewController()
        if (host == null) {
            onResult(null)
            return
        }
        // A backup that travelled through Drive or email often loses its JSON type, so accept plain
        // data too and validate the contents instead.
        val picker = UIDocumentPickerViewController(
            forOpeningContentTypes = listOf(UTTypeJSON, UTTypePlainText, UTTypeData)
        )
        val held = PickerDelegate { text ->
            delegate = null
            onResult(text)
        }
        delegate = held
        picker.delegate = held
        host.presentViewController(picker, animated = true, completion = null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private class PickerDelegate(
    private val onResult: (String?) -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        val path = url?.path
        if (url == null || path == null) {
            onResult(null)
            return
        }
        // Files outside the app's own container need this handshake before they can be read.
        val scoped = url.startAccessingSecurityScopedResource()
        val text = NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)
        if (scoped) url.stopAccessingSecurityScopedResource()
        onResult(text)
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onResult(null)
    }
}
