/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.archive.stream

import com.servalabs.chat.archive.proto.Frame

interface BackupImportStream {
  fun read(): Frame?
}
