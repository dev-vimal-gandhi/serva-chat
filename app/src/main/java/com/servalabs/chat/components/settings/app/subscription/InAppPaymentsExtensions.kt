/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.components.settings.app.subscription

import com.servalabs.chat.donations.IDEALPaymentSource
import com.servalabs.chat.donations.PayPalPaymentSource
import com.servalabs.chat.donations.PaymentSource
import com.servalabs.chat.donations.PaymentSourceType
import com.servalabs.chat.donations.SEPADebitPaymentSource
import com.servalabs.chat.donations.StripeApi
import com.servalabs.chat.donations.TokenPaymentSource
import com.servalabs.chat.jobs.protos.InAppPaymentSourceData

fun PaymentSourceType.toInAppPaymentSourceDataCode(): InAppPaymentSourceData.Code {
  return when (this) {
    PaymentSourceType.Unknown -> InAppPaymentSourceData.Code.UNKNOWN
    PaymentSourceType.GooglePlayBilling -> InAppPaymentSourceData.Code.GOOGLE_PLAY_BILLING
    PaymentSourceType.PayPal -> InAppPaymentSourceData.Code.PAY_PAL
    PaymentSourceType.Stripe.CreditCard -> InAppPaymentSourceData.Code.CREDIT_CARD
    PaymentSourceType.Stripe.GooglePay -> InAppPaymentSourceData.Code.GOOGLE_PAY
    PaymentSourceType.Stripe.IDEAL -> InAppPaymentSourceData.Code.IDEAL
    PaymentSourceType.Stripe.SEPADebit -> InAppPaymentSourceData.Code.SEPA_DEBIT
  }
}

fun PaymentSource.toProto(): InAppPaymentSourceData {
  return InAppPaymentSourceData(
    code = type.toInAppPaymentSourceDataCode(),
    idealData = if (this is IDEALPaymentSource) {
      InAppPaymentSourceData.IDEALData(
        name = idealData.name,
        email = idealData.email
      )
    } else null,
    sepaData = if (this is SEPADebitPaymentSource) {
      InAppPaymentSourceData.SEPAData(
        iban = sepaDebitData.iban,
        name = sepaDebitData.name,
        email = sepaDebitData.email
      )
    } else null,
  )
}

fun InAppPaymentSourceData.toPaymentSource(): PaymentSource {
  return when (code) {
    InAppPaymentSourceData.Code.CREDIT_CARD, InAppPaymentSourceData.Code.GOOGLE_PAY -> {
      TokenPaymentSource(
        type = if (code == InAppPaymentSourceData.Code.CREDIT_CARD) PaymentSourceType.Stripe.CreditCard else PaymentSourceType.Stripe.GooglePay,
        parameters = tokenData!!.parameters,
        token = tokenData.tokenId,
        email = tokenData.email
      )
    }
    InAppPaymentSourceData.Code.SEPA_DEBIT -> {
      SEPADebitPaymentSource(
        StripeApi.SEPADebitData(
          iban = sepaData!!.iban,
          name = sepaData.name,
          email = sepaData.email
        )
      )
    }
    InAppPaymentSourceData.Code.IDEAL -> {
      IDEALPaymentSource(
        StripeApi.IDEALData(
          name = idealData!!.name,
          email = idealData.email
        )
      )
    }
    InAppPaymentSourceData.Code.PAY_PAL -> {
      PayPalPaymentSource()
    }
    else -> error("Unexpected code $code")
  }
}
