/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.libsignal.api.storage

import okio.ByteString
import okio.ByteString.Companion.toByteString
import com.servalabs.chat.core.models.ServiceId
import com.servalabs.chat.core.util.isNotEmpty
import com.servalabs.chat.libsignal.api.payments.PaymentsConstants
import com.servalabs.chat.libsignal.api.push.SignalServiceAddress
import com.servalabs.chat.libsignal.api.storage.IAPSubscriptionId.Companion.isNotNullOrBlank
import com.servalabs.chat.libsignal.api.storage.StorageRecordProtoUtil.defaultAccountRecord
import com.servalabs.chat.libsignal.internal.storage.protos.AccountRecord
import com.servalabs.chat.libsignal.internal.storage.protos.Payments

fun AccountRecord.Builder.safeSetPayments(enabled: Boolean, entropy: ByteArray?): AccountRecord.Builder {
  val paymentsBuilder = Payments.Builder()
  val entropyPresent = entropy != null && entropy.size == PaymentsConstants.PAYMENTS_ENTROPY_LENGTH

  paymentsBuilder.enabled(enabled && entropyPresent)

  if (entropyPresent) {
    paymentsBuilder.entropy(entropy!!.toByteString())
  }

  this.payments = paymentsBuilder.build()

  return this
}
fun AccountRecord.Builder.safeSetSubscriber(subscriberId: ByteString, subscriberCurrencyCode: String): AccountRecord.Builder {
  if (subscriberId.isNotEmpty() && subscriberId.size == 32 && subscriberCurrencyCode.isNotBlank()) {
    this.subscriberId = subscriberId
    this.subscriberCurrencyCode = subscriberCurrencyCode
  } else {
    this.subscriberId = defaultAccountRecord.subscriberId
    this.subscriberCurrencyCode = defaultAccountRecord.subscriberCurrencyCode
  }

  return this
}

fun AccountRecord.Builder.safeSetBackupsSubscriber(subscriberId: ByteString, iapSubscriptionId: IAPSubscriptionId?): AccountRecord.Builder {
  if (subscriberId.isNotEmpty() && subscriberId.size == 32 && iapSubscriptionId.isNotNullOrBlank()) {
    this.backupSubscriberData = AccountRecord.IAPSubscriberData(
      subscriberId = subscriberId,
      purchaseToken = iapSubscriptionId.purchaseToken,
      originalTransactionId = iapSubscriptionId.originalTransactionId
    )
  } else {
    this.backupSubscriberData = defaultAccountRecord.backupSubscriberData
  }

  return this
}

fun AccountRecord.PinnedConversation.Contact.toSignalServiceAddress(): SignalServiceAddress {
  val serviceId = ServiceId.parseOrNull(this.serviceId, this.serviceIdBinary)
  return SignalServiceAddress(serviceId, this.e164)
}
