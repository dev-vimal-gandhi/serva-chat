package com.servalabs.chat.util

import com.servalabs.chat.libsignal.api.crypto.EnvelopeMetadata
import com.servalabs.chat.libsignal.internal.push.Content
import com.servalabs.chat.libsignal.internal.push.Envelope

/**
 * The tuple of information needed to process a message. Used to in [EarlyMessageCache]
 * to store potentially out-of-order messages.
 */
data class EarlyMessageCacheEntry(
  val envelope: Envelope,
  val content: Content,
  val metadata: EnvelopeMetadata,
  val serverDeliveredTimestamp: Long
)
