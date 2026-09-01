package com.baltajmn.habit.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baltajmn.habit.data.HabitRepository
import com.baltajmn.habit.share.SharePeriod
import com.baltajmn.habit.share.Sharing
import com.baltajmn.habit.share.encodeToPng
import com.baltajmn.habit.share.renderShareCard
import com.baltajmn.habit.i18n.S

@Composable
fun ShareScreen(onBack: () -> Unit) {
    val today = HabitRepository.today
    val habits = HabitRepository.activeHabits
    var period by remember { mutableStateOf(SharePeriod.YEAR) }
    var status by remember { mutableStateOf<String?>(null) }
    val measurer = rememberTextMeasurer()

    // Redraw only when the picture would actually change.
    val card = remember(period, habits.size, habits.sumOf { it.log.size }) {
        renderShareCard(habits, period, today, measurer)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeContentPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) { Text("‹", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Spacer(Modifier.size(14.dp))
            Text(S.share, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SharePeriod.entries.forEach { option ->
                PeriodChip(
                    label = option.label,
                    selected = option == period,
                    onClick = { period = option; status = null },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // The card is taller than it is wide, so sizing it by the width alone makes it 1.25 screens
        // tall on anything landscape or tablet shaped. This column does not scroll and does not
        // clip, so the header, the chips and the buttons were pushed off the screen and what was
        // left looked like a picture with no explanation. The box takes whatever height is left
        // over and the card is fitted inside it, by width on a phone and by height on a wide one.
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Image(
                bitmap = card,
                contentDescription = S.preview,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .aspectRatio(1080f / 1350f)
                    .clip(MaterialTheme.shapes.large)
                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.large),
            )
        }

        status?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            ActionButton(
                label = S.save,
                filled = false,
                modifier = Modifier.weight(1f),
                onClick = {
                    Sharing.savePngToPhotos(card.encodeToPng()) { saved ->
                        status = if (saved) S.savedToPhotos else S.saveFailed
                    }
                },
            )
            ActionButton(
                label = S.share,
                filled = true,
                modifier = Modifier.weight(1f),
                onClick = { Sharing.sharePng(card.encodeToPng()) },
            )
        }
    }
}

@Composable
private fun PeriodChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.shapes.medium,
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ActionButton(label: String, filled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = modifier.background(
            if (filled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.shapes.medium,
        ),
    ) {
        Text(
            label,
            fontWeight = FontWeight.SemiBold,
            color = if (filled) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
