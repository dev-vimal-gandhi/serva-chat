package com.servalabs.chat.libsignal.api.messages

import com.servalabs.chat.libsignal.internal.push.Envelope
import com.servalabs.chat.libsignal.internal.websocket.WebSocketRequestMessage

/**
 * Represents an envelope off the wire, paired with the metadata needed to process it.
 */
class EnvelopeResponse(
  val envelope: Envelope,
  val serverDeliveredTimestamp: Long,
  val websocketRequest: WebSocketRequestMessage
)
