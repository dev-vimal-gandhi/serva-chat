/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.registration.util

import android.os.Parcel
import kotlinx.parcelize.Parceler
import com.servalabs.chat.core.models.ServiceId

class ACIParceler : Parceler<ServiceId.ACI> {
  override fun ServiceId.ACI.write(parcel: Parcel, flags: Int) {
    parcel.writeByteArray(this.toByteArray())
  }

  override fun create(parcel: Parcel): ServiceId.ACI {
    return ServiceId.ACI.parseOrThrow(parcel.createByteArray())
  }
}
