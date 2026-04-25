package com.servalabs.chat.libsignal.api.crypto

import com.servalabs.chat.core.models.ServiceId

class EnvelopeMetadata(
  val sourceServiceId: ServiceId,
  val sourceE164: String?,
  val sourceDeviceId: Int,
  val sealedSender: Boolean,
  val groupId: ByteArray?,
  val destinationServiceId: ServiceId,
  val ciphertextMessageType: Int
)
