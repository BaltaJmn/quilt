package com.baltajmn.habit

import com.baltajmn.habit.billing.ProState
import com.baltajmn.habit.billing.proState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProStateTest {

    @Test
    fun lifetimeAndActiveMonthlyBothAreProButAnEndedOneIsNot() {
        assertEquals(ProState.Lifetime, proState(active = true, everHad = true, expiresAtMillis = null))
        assertEquals(ProState.Monthly, proState(active = true, everHad = true, expiresAtMillis = 1_000L))
        assertEquals(ProState.Ended, proState(active = false, everHad = true, expiresAtMillis = 1_000L))
        assertEquals(ProState.Free, proState(active = false, everHad = false, expiresAtMillis = null))
        assertTrue(ProState.Lifetime.isPro && ProState.Monthly.isPro)
        assertFalse(ProState.Ended.isPro || ProState.Free.isPro)
    }
}
