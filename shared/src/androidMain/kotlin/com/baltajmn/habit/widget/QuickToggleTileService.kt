package com.baltajmn.habit.widget

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.baltajmn.habit.data.AndroidContext
import com.baltajmn.habit.data.HabitRepository
import com.baltajmn.habit.data.today
import com.baltajmn.habit.i18n.S
import com.baltajmn.habit.model.Habit

/**
 * One tap from the quick settings shade marks the next habit still owed today. No picker and no
 * screen: the subtitle says how many are left and the tap always lands on the first of them.
 *
 * The subtitle counts instead of naming, because a quick settings tile is readable on the lock
 * screen and a habit name is not something to publish there.
 */
class QuickToggleTileService : TileService() {

    private fun pending(): List<Habit> {
        AndroidContext.init(applicationContext)
        // The widget and the app both write this file from their own process.
        HabitRepository.reload()
        val date = today()
        return HabitRepository.activeHabits.filter { it.countsOn(date) && !it.isDoneOn(date) }
    }

    override fun onStartListening() = refresh(pending())

    override fun onClick() {
        val left = pending()
        val next = left.firstOrNull()
        if (next == null) {
            refresh(left)
            return
        }
        HabitRepository.cycle(next.id, today())
        // A habit with a target above one may still be pending after the tap.
        refresh(pending())
    }

    private fun refresh(left: List<Habit>) {
        val tile = qsTile ?: return
        tile.state = if (left.isEmpty()) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
        tile.label = S.quickTile
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (left.isEmpty()) S.allDoneToday else S.pendingToday(left.size)
        }
        tile.updateTile()
    }
}
