package com.servalabs.chat.keyvalue

import com.servalabs.chat.core.util.ByteSerializer
import com.servalabs.chat.database.model.databaseprotos.PendingChangeNumberMetadata

/**
 * Serialize [PendingChangeNumberMetadata]
 */
object PendingChangeNumberMetadataSerializer : ByteSerializer<PendingChangeNumberMetadata> {
  override fun serialize(data: PendingChangeNumberMetadata): ByteArray = data.encode()
  override fun deserialize(data: ByteArray): PendingChangeNumberMetadata = PendingChangeNumberMetadata.ADAPTER.decode(data)
}
