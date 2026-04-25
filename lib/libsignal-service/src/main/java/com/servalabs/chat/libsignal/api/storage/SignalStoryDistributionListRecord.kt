package com.servalabs.chat.libsignal.api.storage

import com.servalabs.chat.libsignal.internal.storage.protos.StoryDistributionListRecord
import java.io.IOException

data class SignalStoryDistributionListRecord(
  override val id: StorageId,
  override val proto: StoryDistributionListRecord
) : SignalRecord<StoryDistributionListRecord> {

  companion object {
    fun newBuilder(serializedUnknowns: ByteArray?): StoryDistributionListRecord.Builder {
      return serializedUnknowns?.let { builderFromUnknowns(it) } ?: StoryDistributionListRecord.Builder()
    }

    private fun builderFromUnknowns(serializedUnknowns: ByteArray): StoryDistributionListRecord.Builder {
      return try {
        StoryDistributionListRecord.ADAPTER.decode(serializedUnknowns).newBuilder()
      } catch (e: IOException) {
        StoryDistributionListRecord.Builder()
      }
    }
  }
}
