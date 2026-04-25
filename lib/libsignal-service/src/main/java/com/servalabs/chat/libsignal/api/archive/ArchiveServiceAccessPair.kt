/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.libsignal.api.archive

import com.servalabs.chat.core.models.backup.MediaRootBackupKey
import com.servalabs.chat.core.models.backup.MessageBackupKey

/**
 * A convenient container for passing around both a message and media archive service credential.
 */
data class ArchiveServiceAccessPair(
  val messageBackupAccess: ArchiveServiceAccess<MessageBackupKey>,
  val mediaBackupAccess: ArchiveServiceAccess<MediaRootBackupKey>
)
