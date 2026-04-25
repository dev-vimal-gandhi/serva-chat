/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.backup.v2.processor

import com.servalabs.chat.archive.proto.ChatItem
import com.servalabs.chat.archive.proto.Frame
import com.servalabs.chat.archive.stream.BackupFrameEmitter
import com.servalabs.chat.core.util.logging.Log
import com.servalabs.chat.backup.v2.ExportState
import com.servalabs.chat.backup.v2.ImportState
import com.servalabs.chat.backup.v2.database.createChatItemInserter
import com.servalabs.chat.backup.v2.database.getMessagesForBackup
import com.servalabs.chat.backup.v2.importer.ChatItemArchiveImporter
import com.servalabs.chat.database.SignalDatabase
import com.servalabs.chat.recipients.RecipientId

/**
 * Handles importing/exporting [ChatItem] frames for an archive.
 */
object ChatItemArchiveProcessor {
  val TAG = Log.tag(ChatItemArchiveProcessor::class.java)

  fun export(db: SignalDatabase, exportState: ExportState, selfRecipientId: RecipientId, messageInclusionCutoffTime: Long, cancellationSignal: () -> Boolean, emitter: BackupFrameEmitter) {
    db.messageTable.getMessagesForBackup(db, exportState.backupTime, selfRecipientId, messageInclusionCutoffTime, exportState).use { chatItems ->
      var count = 0
      while (chatItems.hasNext()) {
        if (count % 1000 == 0 && cancellationSignal()) {
          return@use
        }

        val chatItem: ChatItem? = chatItems.next()
        if (chatItem != null) {
          if (exportState.threadIds.contains(chatItem.chatId)) {
            emitter.emit(Frame(chatItem = chatItem))
          }
        }
        count++
      }
    }
  }

  fun beginImport(importState: ImportState): ChatItemArchiveImporter {
    return SignalDatabase.messages.createChatItemInserter(importState)
  }
}
