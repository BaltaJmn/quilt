package com.baltajmn.habit

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.baltajmn.habit.data.AndroidContext
import com.baltajmn.habit.data.Backup
import com.baltajmn.habit.data.Reminders

class MainActivity : ComponentActivity() {

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private var pendingImport: ((String?) -> Unit)? = null
    private var pendingExport: String? = null

    private val createDocument =
        registerForActivityResult(// Any type: the same saver hands out both the JSON backup and the CSV export, and the
        // extension in the suggested filename is what marks them apart.
        ActivityResultContracts.CreateDocument("*/*")) { uri ->
            val text = pendingExport
            pendingExport = null
            if (uri != null && text != null) {
                runCatching {
                    contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
                }
            }
        }

    private val openDocument =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            val text = uri?.let {
                runCatching {
                    contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                        reader.readText()
                    }
                }.getOrNull()
            }
            pendingImport?.invoke(text)
            pendingImport = null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AndroidContext.init(this)

        // Only asked for once a habit actually has a reminder.
        Reminders.onNeedsPermission = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // A backup that travelled through Drive or email often loses its JSON type, so accept
        // anything and validate the contents instead.
        Backup.onPickFile = { onResult ->
            pendingImport = onResult
            openDocument.launch(arrayOf("*/*"))
        }

        Backup.onSaveFile = { filename, text ->
            pendingExport = text
            createDocument.launch(filename)
        }

        setContent {
            App()
        }
    }

    override fun onDestroy() {
        Reminders.onNeedsPermission = null
        Backup.onPickFile = null
        Backup.onSaveFile = null
        super.onDestroy()
    }
}
