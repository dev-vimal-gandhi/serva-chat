/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.archive.stream

import com.servalabs.chat.archive.proto.BackupInfo
import com.servalabs.chat.archive.proto.Frame

interface BackupExportWriter : AutoCloseable {
  fun write(header: BackupInfo)
  fun write(frame: Frame)
}
