/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.libsignal.api.crypto

import com.servalabs.chat.libsignal.protocol.kdf.HKDF

/**
 * A collection of cryptographic functions in the same namespace for easy access.
 */
object Crypto {

  fun hkdf(inputKeyMaterial: ByteArray, info: ByteArray, outputLength: Int, salt: ByteArray? = null): ByteArray {
    return HKDF.deriveSecrets(inputKeyMaterial, salt ?: byteArrayOf(), info, outputLength)
  }
}
