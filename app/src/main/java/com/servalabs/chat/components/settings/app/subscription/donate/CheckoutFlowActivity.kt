/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.components.settings.app.subscription.donate

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.Fragment
import kotlinx.parcelize.Parcelize
import com.servalabs.chat.core.util.getParcelableExtraCompat
import com.servalabs.chat.core.util.getSerializableCompat
import com.servalabs.chat.donations.InAppPaymentType
import com.servalabs.chat.components.FragmentWrapperActivity

/**
 * Home base for all checkout flows.
 */
class CheckoutFlowActivity : FragmentWrapperActivity() {

  companion object {
    private const val ARG_IN_APP_PAYMENT_TYPE = "in_app_payment_type"
    const val RESULT_DATA = "result_data"

    fun createIntent(context: Context, inAppPaymentType: InAppPaymentType): Intent {
      return Contract().createIntent(context, inAppPaymentType)
    }
  }

  private val inAppPaymentType: InAppPaymentType by lazy {
    intent.extras!!.getSerializableCompat(ARG_IN_APP_PAYMENT_TYPE, InAppPaymentType::class.java)!!
  }

  override fun getFragment(): Fragment {
    return CheckoutNavHostFragment.create(inAppPaymentType)
  }

  class Contract : ActivityResultContract<InAppPaymentType, Result?>() {

    override fun createIntent(context: Context, input: InAppPaymentType): Intent {
      return Intent(context, CheckoutFlowActivity::class.java).putExtra(ARG_IN_APP_PAYMENT_TYPE, input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Result? {
      return intent?.getParcelableExtraCompat(RESULT_DATA, Result::class.java)
    }
  }

  @Parcelize
  data class Result(
    val action: InAppPaymentProcessorAction,
    val inAppPaymentType: InAppPaymentType
  ) : Parcelable
}
