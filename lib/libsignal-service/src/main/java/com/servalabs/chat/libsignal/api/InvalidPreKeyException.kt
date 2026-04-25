/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.libsignal.api

import com.servalabs.chat.libsignal.protocol.InvalidKeyException
import com.servalabs.chat.libsignal.protocol.SignalProtocolAddress
import java.io.IOException

/**
 * Wraps an [InvalidKeyException] in an [IOException] with a nicer message.
 */
class InvalidPreKeyException(
  address: SignalProtocolAddress,
  invalidKeyException: InvalidKeyException
) : IOException("Invalid prekey for $address", invalidKeyException)
