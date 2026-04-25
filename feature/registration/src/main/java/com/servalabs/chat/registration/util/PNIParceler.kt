/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.registration.util

import android.os.Parcel
import kotlinx.parcelize.Parceler
import com.servalabs.chat.core.models.ServiceId

class PNIParceler : Parceler<ServiceId.PNI> {
  override fun ServiceId.PNI.write(parcel: Parcel, flags: Int) {
    parcel.writeByteArray(this.toByteArray())
  }

  override fun create(parcel: Parcel): ServiceId.PNI {
    return ServiceId.PNI.parseOrThrow(parcel.createByteArray())
  }
}
