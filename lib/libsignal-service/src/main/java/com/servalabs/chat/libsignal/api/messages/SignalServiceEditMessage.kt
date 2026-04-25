package com.servalabs.chat.libsignal.api.messages

data class SignalServiceEditMessage(
  val targetSentTimestamp: Long,
  val dataMessage: SignalServiceDataMessage
)
