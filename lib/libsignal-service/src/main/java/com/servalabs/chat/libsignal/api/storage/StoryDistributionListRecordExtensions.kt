/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.libsignal.api.storage

import com.servalabs.chat.core.models.ServiceId
import com.servalabs.chat.libsignal.api.push.SignalServiceAddress
import com.servalabs.chat.libsignal.internal.storage.protos.StoryDistributionListRecord

val StoryDistributionListRecord.recipientServiceAddresses: List<SignalServiceAddress>
  get() {
    val serviceIds = if (this.recipientServiceIdsBinary.isNotEmpty()) {
      this.recipientServiceIdsBinary.mapNotNull { ServiceId.parseOrNull(it) }
    } else {
      this.recipientServiceIds.mapNotNull { ServiceId.parseOrNull(it) }
    }
    return serviceIds.map { SignalServiceAddress(it) }
  }
