/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.components.settings.app.subscription.errors

import com.servalabs.chat.donations.PaymentSourceType
import com.servalabs.chat.donations.StripeDeclineCode
import com.servalabs.chat.donations.StripeFailureCode
import org.signal.libsignal.api.subscriptions.ActiveSubscription
import com.servalabs.chat.libsignal.internal.push.exceptions.InAppPaymentProcessorError

fun InAppPaymentProcessorError.toDonationError(
  source: DonationErrorSource,
  method: PaymentSourceType
): DonationError {
  return when (processor) {
    ActiveSubscription.Processor.STRIPE -> {
      check(method is PaymentSourceType.Stripe)
      val declineCode = StripeDeclineCode.getFromCode(chargeFailure.code)
      val failureCode = StripeFailureCode.getFromCode(chargeFailure.code)
      if (declineCode.isKnown()) {
        DonationError.PaymentSetupError.StripeDeclinedError(source, this, declineCode, method)
      } else if (failureCode.isKnown) {
        DonationError.PaymentSetupError.StripeFailureCodeError(source, this, failureCode, method)
      } else if (chargeFailure.code != null) {
        DonationError.PaymentSetupError.StripeCodedError(source, this, chargeFailure.code)
      } else {
        DonationError.PaymentSetupError.GenericError(source, this)
      }
    }
    ActiveSubscription.Processor.BRAINTREE -> {
      check(method is PaymentSourceType.PayPal)
      val code = chargeFailure.code
      if (code == null) {
        DonationError.PaymentSetupError.GenericError(source, this)
      } else {
        val declineCode = PayPalDeclineCode.KnownCode.fromCode(code.toInt())
        if (declineCode != null) {
          DonationError.PaymentSetupError.PayPalDeclinedError(source, this, declineCode)
        } else {
          DonationError.PaymentSetupError.PayPalCodedError(source, this, code.toInt())
        }
      }
    }
    ActiveSubscription.Processor.GOOGLE_PLAY_BILLING -> {
      check(method is PaymentSourceType.GooglePlayBilling)
      DonationError.PaymentSetupError.GenericError(source, this)
    }
  }
}
