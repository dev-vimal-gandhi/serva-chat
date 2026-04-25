/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.archive.stream

import com.servalabs.chat.archive.proto.BackupInfo
import com.servalabs.chat.archive.proto.Frame
import com.servalabs.chat.core.util.toJson
import java.io.IOException
import java.io.OutputStream

/**
 * Writes backup frames to the wrapped stream as newline-delimited JSON (JSONL).
 */
class JsonBackupWriter(private val outputStream: OutputStream) : BackupExportWriter {

  @Throws(IOException::class)
  override fun write(header: BackupInfo) {
    outputStream.write((header.toJson() + "\n").toByteArray())
  }

  @Throws(IOException::class)
  override fun write(frame: Frame) {
    outputStream.write((frame.toJson() + "\n").toByteArray())
  }

  override fun close() {
    outputStream.close()
  }
}
