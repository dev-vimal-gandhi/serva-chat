package com.servalabs.chat.libsignal.api.messages.multidevice

import com.servalabs.chat.core.models.ServiceId

class BlockedListMessage(
  @JvmField val individuals: List<Individual>,
  @JvmField val groupIds: List<ByteArray>
) {
  data class Individual(
    val aci: ServiceId.ACI?,
    val e164: String?
  ) {
    init {
      check(aci != null || e164 != null)
    }
  }
}
