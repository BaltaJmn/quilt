package com.baltajmn.habit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baltajmn.habit.billing.Billing
import com.baltajmn.habit.billing.ProOffer
import com.baltajmn.habit.billing.ProState
import com.baltajmn.habit.billing.PurchaseOutcome
import com.baltajmn.habit.data.Backup
import com.baltajmn.habit.data.HabitRepository
import com.baltajmn.habit.data.backupFilename
import com.baltajmn.habit.data.csvFilename
import com.revenuecat.purchases.kmp.models.Package
import kotlinx.coroutines.launch
import com.baltajmn.habit.i18n.S

@Composable
private fun PlanOption(
    title: String,
    price: String,
    caption: String?,
    badge: String?,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(12.dp)
    Column(
        Modifier.fillMaxWidth()
            .clip(shape)
            .border(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant, shape)
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Text(price, style = MaterialTheme.typography.titleSmall)
        }
        badge?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = colors.primary) }
        caption?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant) }
    }
}

/** The only paywall. Shown when a free user hits a wall, never at first launch. */
@Composable
fun ProDialog(onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    var offer by remember { mutableStateOf<ProOffer?>(null) }
    var picked by remember { mutableStateOf<Package?>(null) }
    var busy by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        offer = Billing.proOffer()
        if (offer == null) note = S.storeUnavailable
    }

    // The lifetime purchase is the default and the highlighted one; the month is mostly its anchor.
    val target = picked ?: offer?.let { it.lifetime ?: it.monthly }
    val monthly = offer?.monthly
    val lifetime = offer?.lifetime
    val uriHandler = LocalUriHandler.current
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        title = { Text(S.unlimitedHabits) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${S.freeIncludes(HabitRepository.FREE_HABIT_LIMIT)} ${S.pitch}")
                if (Billing.state == ProState.Ended) {
                    Text(S.subscriptionEnded, style = MaterialTheme.typography.bodySmall)
                }
                if (lifetime != null && monthly != null) {
                    PlanOption(
                        title = S.planForever,
                        price = lifetime.storeProduct.price.formatted,
                        caption = S.foreverNote,
                        badge = S.bestValue,
                        selected = target == lifetime,
                        enabled = !busy,
                        onClick = { picked = lifetime },
                    )
                    PlanOption(
                        title = S.planMonthly,
                        price = S.perMonth(monthly.storeProduct.price.formatted),
                        caption = null,
                        badge = null,
                        selected = target == monthly,
                        enabled = !busy,
                        onClick = { picked = monthly },
                    )
                    // Apple 3.1.2: renewal, how to cancel and the two documents, next to the purchase.
                    Text(
                        S.subscriptionTerms,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = { runCatching { uriHandler.openUri("https://quilt.baltajmn.dev/terms.html") } }) {
                            Text(S.termsOfUse)
                        }
                        TextButton(onClick = { runCatching { uriHandler.openUri("https://quilt.baltajmn.dev/") } }) {
                            Text(S.privacyPolicy)
                        }
                    }
                }
                note?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                TextButton(
                    enabled = !busy,
                    onClick = {
                        busy = true
                        scope.launch {
                            val found = Billing.restore()
                            busy = false
                            if (found) onDismiss() else note = S.noPreviousPurchase
                        }
                    },
                ) { Text(S.restorePurchase) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !busy && target != null,
                onClick = {
                    busy = true
                    scope.launch {
                        val outcome = Billing.purchase(target!!)
                        busy = false
                        when (outcome) {
                            PurchaseOutcome.Success -> onDismiss()
                            PurchaseOutcome.Cancelled -> Unit
                            PurchaseOutcome.Failed -> note = S.purchaseFailed
                        }
                    }
                },
            ) {
                val price = target?.storeProduct?.price?.formatted
                Text(
                    when {
                        price == null -> S.buyPro
                        target == monthly -> S.subscribe(price)
                        else -> S.buyFor(price)
                    },
                )
            }
        },
        dismissButton = { TextButton(enabled = !busy, onClick = onDismiss) { Text(S.notNow) } },
    )
}

/**
 * Ajustes. Holds the two things that must be reachable without hitting a wall: restoring a purchase
 * (both stores require it) and the backup, which is the app's answer to "your data is yours".
 */
@Composable
fun SettingsSheet(onOpenPro: () -> Unit) {
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var proNote by remember { mutableStateOf<String?>(null) }
    var backupNote by remember { mutableStateOf<String?>(null) }
    var confirmImport by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxWidth()
            // The reorder list grows with the habit count, so the sheet has to scroll: without
            // this, everything below it is unreachable once there are more than a handful.
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(S.settings, style = MaterialTheme.typography.titleLarge)

        if (HabitRepository.isPro) {
            Caption(S.proActive)
        } else {
            Caption(S.freePlan(HabitRepository.FREE_HABIT_LIMIT))
            TextButton(onClick = onOpenPro) { Text(S.getPro) }
            TextButton(
                enabled = !busy,
                onClick = {
                    busy = true
                    scope.launch {
                        val found = Billing.restore()
                        busy = false
                        proNote = if (found) S.proRestored else S.noPreviousPurchase
                    }
                },
            ) { Text(S.restorePurchase) }
            proNote?.let { Caption(it) }
        }

        HorizontalDivider(
            Modifier.padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        val habits = HabitRepository.activeHabits
        if (habits.size > 1) {
            Text(S.reorderHabits, style = MaterialTheme.typography.titleMedium)
            // Compact rows rather than dragging the cards themselves: a habit card is a whole year
            // grid tall, and long-pressing one is already how a day gets skipped.
            habits.forEachIndexed { index, habit ->
                ReorderRow(
                    habit = habit,
                    canMoveUp = index > 0,
                    canMoveDown = index < habits.lastIndex,
                    onMove = { delta -> HabitRepository.moveBy(habit.id, delta) },
                )
            }

            HorizontalDivider(
                Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }

        Text(S.backup, style = MaterialTheme.typography.titleMedium)
        Caption(S.backupHint)
        TextButton(
            onClick = { Backup.export(HabitRepository.exportJson(), backupFilename()) },
        ) { Text(S.exportData) }
        TextButton(onClick = { confirmImport = true }) { Text(S.importBackup) }
        TextButton(
            onClick = { Backup.export(HabitRepository.exportCsv(), csvFilename()) },
        ) { Text(S.exportCsv) }
        Caption(S.csvHint)
        backupNote?.let { Caption(it) }

        HorizontalDivider(
            Modifier.padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        // App Review 5.1.1(i): an app that sells anything has to reach its privacy policy from
        // inside itself, not only from the store listing.
        val uriHandler = LocalUriHandler.current
        TextButton(
            onClick = { uriHandler.openUri("https://quilt.baltajmn.dev/") },
        ) { Text(S.privacyPolicy) }
    }

    if (confirmImport) {
        AlertDialog(
            onDismissRequest = { confirmImport = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large,
            title = { Text(S.importTitle) },
            text = {
                Text(S.importBody)
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmImport = false
                    Backup.pickFile { text ->
                        backupNote = when {
                            text == null -> null
                            HabitRepository.importJson(text) -> S.imported
                            else -> S.notABackup
                        }
                    }
                }) { Text(S.pickFile) }
            },
            dismissButton = {
                TextButton(onClick = { confirmImport = false }) { Text(S.cancel) }
            },
        )
    }
}

@Composable
private fun ReorderRow(
    habit: com.baltajmn.habit.model.Habit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMove: (Int) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(28.dp).background(Color(habit.colorArgb).copy(alpha = 0.28f), CircleShape),
            contentAlignment = Alignment.Center,
        ) { Text(habit.emoji, fontSize = 14.sp) }
        Spacer(Modifier.width(10.dp))
        Text(
            habit.name,
            maxLines = 1,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
        )
        TextButton(enabled = canMoveUp, onClick = { onMove(-1) }) { Text("↑") }
        TextButton(enabled = canMoveDown, onClick = { onMove(1) }) { Text("↓") }
    }
}

@Composable
private fun Caption(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp),
    )
}
