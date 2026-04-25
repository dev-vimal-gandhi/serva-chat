/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.libsignal.internal.push

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.servalabs.chat.core.util.Base64

/**
 * JSON serializer to encode a ByteArray as a base64 string without padding.
 */
class ByteArraySerializerBase64NoPadding : JsonSerializer<ByteArray>() {
  override fun serialize(value: ByteArray, gen: JsonGenerator, serializers: SerializerProvider) {
    gen.writeString(Base64.encodeWithoutPadding(value))
  }
}
