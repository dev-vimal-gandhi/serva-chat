/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.libsignal.api.archive

import com.servalabs.chat.core.util.Base64

/**
 * Acts as credentials for various archive operations.
 */
class ArchiveCredentialPresentation(
  val presentation: ByteArray,
  val signedPresentation: ByteArray
) {
  fun toHeaders(): MutableMap<String, String> {
    return mutableMapOf(
      "X-Signal-ZK-Auth" to Base64.encodeWithPadding(presentation),
      "X-Signal-ZK-Auth-Signature" to Base64.encodeWithPadding(signedPresentation)
    )
  }
}
