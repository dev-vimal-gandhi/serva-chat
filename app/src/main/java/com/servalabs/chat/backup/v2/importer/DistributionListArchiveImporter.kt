/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.backup.v2.importer

import com.servalabs.chat.archive.proto.DistributionList
import com.servalabs.chat.archive.proto.DistributionListItem
import com.servalabs.chat.core.util.UuidUtil
import com.servalabs.chat.core.util.logging.Log
import com.servalabs.chat.backup.v2.ImportState
import com.servalabs.chat.database.SignalDatabase
import com.servalabs.chat.database.model.DistributionListPrivacyMode
import com.servalabs.chat.recipients.RecipientId
import org.signal.libsignal.api.push.DistributionId

/**
 * Handles the importing of [DistributionListItem] models into the local database.
 */
object DistributionListArchiveImporter {

  private val TAG = Log.tag(DistributionListArchiveImporter.javaClass)

  fun import(dlistItem: DistributionListItem, importState: ImportState): RecipientId? {
    val deletionTimestamp = dlistItem.deletionTimestamp
    if (deletionTimestamp != null && deletionTimestamp > 0) {
      val dlistId = SignalDatabase.distributionLists.createList(
        name = "",
        members = emptyList(),
        distributionId = DistributionId.from(UuidUtil.fromByteString(dlistItem.distributionId)),
        allowsReplies = false,
        deletionTimestamp = deletionTimestamp,
        storageId = null,
        privacyMode = DistributionListPrivacyMode.ONLY_WITH
      )!!

      return SignalDatabase.distributionLists.getRecipientId(dlistId)!!
    }

    val dlist = dlistItem.distributionList ?: return null
    val members: List<RecipientId> = dlist.memberRecipientIds
      .mapNotNull { importState.remoteToLocalRecipientId[it] }

    if (members.size != dlist.memberRecipientIds.size) {
      Log.w(TAG, "Couldn't find some member recipients! Missing backup recipientIds: ${dlist.memberRecipientIds.toSet() - members.toSet()}")
    }

    val distributionId = DistributionId.from(UuidUtil.fromByteString(dlistItem.distributionId))
    val privacyMode = dlist.privacyMode.toLocalPrivacyMode()

    val dlistId = SignalDatabase.distributionLists.createList(
      name = dlist.name,
      members = members,
      distributionId = distributionId,
      allowsReplies = dlist.allowReplies,
      deletionTimestamp = dlistItem.deletionTimestamp ?: 0,
      storageId = null,
      privacyMode = privacyMode
    )!!

    return SignalDatabase.distributionLists.getRecipientId(dlistId)!!
  }
}

private fun DistributionList.PrivacyMode.toLocalPrivacyMode(): DistributionListPrivacyMode {
  return when (this) {
    DistributionList.PrivacyMode.UNKNOWN -> DistributionListPrivacyMode.ALL
    DistributionList.PrivacyMode.ONLY_WITH -> DistributionListPrivacyMode.ONLY_WITH
    DistributionList.PrivacyMode.ALL -> DistributionListPrivacyMode.ALL
    DistributionList.PrivacyMode.ALL_EXCEPT -> DistributionListPrivacyMode.ALL_EXCEPT
  }
}
