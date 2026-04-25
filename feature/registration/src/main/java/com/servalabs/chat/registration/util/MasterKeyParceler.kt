/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.registration.util

import android.os.Parcel
import kotlinx.parcelize.Parceler
import com.servalabs.chat.core.models.MasterKey

object MasterKeyParceler : Parceler<MasterKey?> {
  override fun create(parcel: Parcel): MasterKey? {
    val bytes = parcel.createByteArray()
    return bytes?.let { MasterKey(it) }
  }

  override fun MasterKey?.write(parcel: Parcel, flags: Int) {
    parcel.writeByteArray(this?.serialize())
  }
}
