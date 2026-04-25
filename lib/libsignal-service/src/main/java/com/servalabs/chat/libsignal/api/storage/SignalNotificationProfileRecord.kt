package com.servalabs.chat.libsignal.api.storage

import com.servalabs.chat.libsignal.internal.storage.protos.NotificationProfile
import java.io.IOException

/**
 * Wrapper around a [NotificationProfile] to pair it with a [StorageId].
 */
data class SignalNotificationProfileRecord(
  override val id: StorageId,
  override val proto: NotificationProfile
) : SignalRecord<NotificationProfile> {

  companion object {
    fun newBuilder(serializedUnknowns: ByteArray?): NotificationProfile.Builder {
      return serializedUnknowns?.let { builderFromUnknowns(it) } ?: NotificationProfile.Builder()
    }

    private fun builderFromUnknowns(serializedUnknowns: ByteArray): NotificationProfile.Builder {
      return try {
        NotificationProfile.ADAPTER.decode(serializedUnknowns).newBuilder()
      } catch (e: IOException) {
        NotificationProfile.Builder()
      }
    }
  }
}
