package com.servalabs.chat.billing

import com.servalabs.chat.core.util.billing.BillingApi
import com.servalabs.chat.core.util.billing.BillingDependencies

/**
 * Website builds do not support google play billing.
 */
object BillingFactory {
  @JvmStatic
  fun create(billingDependencies: BillingDependencies, isBackupsAvailable: Boolean): BillingApi {
    return BillingApi.Empty
  }
}
