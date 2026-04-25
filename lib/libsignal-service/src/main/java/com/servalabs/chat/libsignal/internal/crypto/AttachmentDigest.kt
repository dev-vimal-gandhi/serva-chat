/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.libsignal.internal.crypto

data class AttachmentDigest(
  val digest: ByteArray,
  val incrementalDigest: ByteArray?,
  val incrementalMacChunkSize: Int
)
