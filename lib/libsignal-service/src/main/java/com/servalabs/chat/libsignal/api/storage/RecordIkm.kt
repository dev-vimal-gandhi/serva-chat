/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.libsignal.api.storage

import com.servalabs.chat.core.models.storageservice.StorageItemKey
import com.servalabs.chat.libsignal.api.crypto.Crypto
import com.servalabs.chat.libsignal.internal.storage.protos.ManifestRecord
import com.servalabs.chat.libsignal.internal.storage.protos.StorageItem
import com.servalabs.chat.libsignal.internal.util.Util

/**
 * A wrapper around a [ByteArray], just so the recordIkm is strongly typed.
 * The recordIkm comes from [ManifestRecord.recordIkm], and is used to encrypt [StorageItem.value_].
 */
@JvmInline
value class RecordIkm(val value: ByteArray) {

  companion object {
    fun generate(): RecordIkm {
      return RecordIkm(Util.getSecretBytes(32))
    }
  }

  fun deriveStorageItemKey(rawId: ByteArray): StorageItemKey {
    val key = Crypto.hkdf(
      inputKeyMaterial = this.value,
      info = "20240801_SIGNAL_STORAGE_SERVICE_ITEM_".toByteArray(Charsets.UTF_8) + rawId,
      outputLength = 32
    )

    return StorageItemKey(key)
  }
}
