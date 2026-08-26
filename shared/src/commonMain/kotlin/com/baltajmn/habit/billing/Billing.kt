package com.baltajmn.habit.billing

import com.baltajmn.habit.data.HabitRepository
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
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

/**
 * The single paid product: a non-consumable granting the [ENTITLEMENT] entitlement forever.
 * Product id and price live in the RevenueCat dashboard, never here, so a price change or a
 * launch discount does not need an app update.
 */
object Billing {

    const val ENTITLEMENT = "pro"

    private var configured = false

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
            .onSuccess { HabitRepository.updatePro(it.entitlements[ENTITLEMENT]?.isActive == true) }
    }

    /** The package to sell, or null while offline or before the dashboard is filled in. */
    suspend fun proPackage(): Package? {
        if (!configured) return null
        return runCatching {
            Purchases.sharedInstance.awaitOfferings().current?.availablePackages?.firstOrNull()
        }.getOrNull()
    }

    suspend fun purchase(pack: Package): PurchaseOutcome = try {
        val purchase = Purchases.sharedInstance.awaitPurchase(packageToPurchase = pack)
        HabitRepository.updatePro(purchase.customerInfo.entitlements[ENTITLEMENT]?.isActive == true)
        PurchaseOutcome.Success
    } catch (e: PurchasesTransactionException) {
        if (e.userCancelled) PurchaseOutcome.Cancelled else PurchaseOutcome.Failed
    } catch (e: Exception) {
        PurchaseOutcome.Failed
    }

    /** Both stores require this to be reachable without buying anything first. */
    suspend fun restore(): Boolean {
        if (!configured) return false
        val info = runCatching { Purchases.sharedInstance.awaitRestore() }.getOrNull() ?: return false
        val active = info.entitlements[ENTITLEMENT]?.isActive == true
        HabitRepository.updatePro(active)
        return active
    }
}

enum class PurchaseOutcome { Success, Cancelled, Failed }
