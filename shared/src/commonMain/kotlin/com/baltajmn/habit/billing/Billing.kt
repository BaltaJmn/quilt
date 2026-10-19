package com.baltajmn.habit.billing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.baltajmn.habit.data.HabitRepository
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.PurchasesTransactionException
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.ktx.awaitRestore
import com.revenuecat.purchases.kmp.models.Package

/**
 * The public SDK key of the RevenueCat project, one per store. Null until the project exists,
 * and the app then simply runs as free-only instead of crashing.
 */
expect val revenueCatApiKey: String?

/** The store that bills the subscription, for the renewal text. */
expect val storeName: String

/**
 * Two ways to the same [ENTITLEMENT]: the lifetime purchase (the one that is pushed) and a monthly
 * subscription (mostly the anchor beside it). Product ids and prices live in the RevenueCat
 * dashboard, never here, so a price change or a launch discount does not need an app update.
 */
object Billing {

    const val ENTITLEMENT = "pro"

    private var configured = false

    /** What the user holds, for the "your plan ended" note on the paywall. */
    var state by mutableStateOf(ProState.Free)
        private set

    private fun apply(info: CustomerInfo): Boolean {
        val e = info.entitlements[ENTITLEMENT]
        val next = proState(e?.isActive == true, e != null, e?.expirationDateMillis)
        state = next
        HabitRepository.updatePro(next.isPro)
        return next.isPro
    }

    /** Call once at startup. Safe to call when no key is set: it just does nothing. */
    fun configure() {
        if (configured) return
        val key = revenueCatApiKey ?: return
        configured = true
        Purchases.logLevel = LogLevel.WARN
        Purchases.configure(PurchasesConfiguration.Builder(key).build())
    }

    /**
     * Re-checks the entitlement against the store. A failure leaves the locally cached value
     * alone on purpose, so a paying user offline keeps their Pro.
     */
    suspend fun refresh() {
        if (!configured) return
        runCatching { Purchases.sharedInstance.awaitCustomerInfo() }
            .onSuccess { apply(it) }
    }

    /** What to sell, or null while offline or before the dashboard is filled in. */
    suspend fun proOffer(): ProOffer? {
        if (!configured) return null
        val offering = runCatching { Purchases.sharedInstance.awaitOfferings().current }.getOrNull() ?: return null
        val lifetime = offering.lifetime ?: offering.availablePackages.firstOrNull { it != offering.monthly }
        return if (lifetime == null && offering.monthly == null) null else ProOffer(lifetime, offering.monthly)
    }

    suspend fun purchase(pack: Package): PurchaseOutcome = try {
        // Same guard as every other entry point. Without a key the SDK is not configured and
        // `sharedInstance` throws, which would surface as a crash instead of a failed purchase.
        if (!configured) return PurchaseOutcome.Failed
        val purchase = Purchases.sharedInstance.awaitPurchase(packageToPurchase = pack)
        // The store took it and the entitlement did not move: a dashboard mistake, not a purchase.
        if (apply(purchase.customerInfo)) PurchaseOutcome.Success else PurchaseOutcome.Failed
    } catch (e: PurchasesTransactionException) {
        if (e.userCancelled) PurchaseOutcome.Cancelled else PurchaseOutcome.Failed
    } catch (e: Exception) {
        PurchaseOutcome.Failed
    }

    /** Both stores require this to be reachable without buying anything first. */
    suspend fun restore(): Boolean {
        if (!configured) return false
        val info = runCatching { Purchases.sharedInstance.awaitRestore() }.getOrNull() ?: return false
        return apply(info)
    }
}

/** The two packages of the current offering; either can be missing while the dashboard is half filled. */
class ProOffer(val lifetime: Package?, val monthly: Package?)

enum class ProState(val isPro: Boolean) { Free(false), Monthly(true), Lifetime(true), Ended(false) }

/**
 * Lifetime or an active monthly plan is Pro; a monthly plan that lapsed is [ProState.Ended], which
 * is Free plus a note on the paywall. A lifetime purchase has no expiration date.
 */
internal fun proState(active: Boolean, everHad: Boolean, expiresAtMillis: Long?): ProState = when {
    active && expiresAtMillis == null -> ProState.Lifetime
    active -> ProState.Monthly
    everHad -> ProState.Ended
    else -> ProState.Free
}

enum class PurchaseOutcome { Success, Cancelled, Failed }
