/**
 * Copyright (C) 2014-2016 Open Whisper Systems
 *
 * Licensed according to the LICENSE file in this repository.
 */
package com.servalabs.chat.libsignal.internal.push

import com.servalabs.chat.libsignal.api.messages.SignalServiceAttachment
import com.servalabs.chat.libsignal.internal.push.http.CancelationSignal
import com.servalabs.chat.libsignal.internal.push.http.OutputStreamFactory
import com.servalabs.chat.libsignal.internal.push.http.ResumableUploadSpec
import java.io.InputStream

/**
 * A bundle of data needed to start an attachment upload.
 */
data class PushAttachmentData(
  val contentType: String?,
  val data: InputStream,
  val dataSize: Long,
  val incremental: Boolean,
  val outputStreamFactory: OutputStreamFactory,
  val listener: SignalServiceAttachment.ProgressListener?,
  val cancelationSignal: CancelationSignal?,
  val resumableUploadSpec: ResumableUploadSpec? = null
)
