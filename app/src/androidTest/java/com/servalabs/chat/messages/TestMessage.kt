package com.servalabs.chat.messages

import com.servalabs.chat.libsignal.api.crypto.EnvelopeMetadata
import com.servalabs.chat.libsignal.internal.push.Content
import com.servalabs.chat.libsignal.internal.push.Envelope

data class TestMessage(
  val envelope: Envelope,
  val content: Content,
  val metadata: EnvelopeMetadata,
  val serverDeliveredTimestamp: Long
)
