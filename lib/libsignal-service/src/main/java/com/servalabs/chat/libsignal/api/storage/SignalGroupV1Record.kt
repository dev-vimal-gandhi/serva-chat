package com.servalabs.chat.libsignal.api.storage

import com.servalabs.chat.libsignal.internal.storage.protos.GroupV1Record
import java.io.IOException

/**
 * Wrapper around a [GroupV1Record] to pair it with a [StorageId].
 */
data class SignalGroupV1Record(
  override val id: StorageId,
  override val proto: GroupV1Record
) : SignalRecord<GroupV1Record> {

  companion object {
    fun newBuilder(serializedUnknowns: ByteArray?): GroupV1Record.Builder {
      return serializedUnknowns?.let { builderFromUnknowns(it) } ?: GroupV1Record.Builder()
    }

    private fun builderFromUnknowns(serializedUnknowns: ByteArray): GroupV1Record.Builder {
      return try {
        GroupV1Record.ADAPTER.decode(serializedUnknowns).newBuilder()
      } catch (e: IOException) {
        GroupV1Record.Builder()
      }
    }
  }
}
