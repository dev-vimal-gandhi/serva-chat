/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.storage

import okio.ByteString.Companion.EMPTY
import okio.ByteString.Companion.toByteString
import org.junit.Assert.assertFalse
import org.junit.BeforeClass
import org.junit.Test
import com.servalabs.chat.core.util.logging.Log
import com.servalabs.chat.service.webrtc.links.CallLinkCredentials
import com.servalabs.chat.testutil.EmptyLogger
import com.servalabs.chat.libsignal.api.storage.SignalCallLinkRecord
import com.servalabs.chat.libsignal.api.storage.StorageId
import com.servalabs.chat.libsignal.internal.storage.protos.CallLinkRecord

/**
 * See [CallLinkRecordProcessor]
 */
class CallLinkRecordProcessorTest {
  companion object {
    val STORAGE_ID: StorageId = StorageId.forCallLink(byteArrayOf(1, 2, 3, 4))

    @JvmStatic
    @BeforeClass
    fun setUpClass() {
      Log.initialize(EmptyLogger())
    }
  }

  private val testSubject = CallLinkRecordProcessor()
  private val mockCredentials = CallLinkCredentials(
    "root key".toByteArray(),
    "admin pass".toByteArray()
  )

  @Test
  fun `Given a valid proto with only an admin pass key and not a deletion timestamp, assert valid`() {
    // GIVEN
    val proto = CallLinkRecord.Builder().apply {
      rootKey = mockCredentials.linkKeyBytes.toByteString()
      adminPasskey = mockCredentials.adminPassBytes!!.toByteString()
      deletedAtTimestampMs = 0L
    }.build()

    val record = SignalCallLinkRecord(STORAGE_ID, proto)

    // WHEN
    val result = testSubject.isInvalid(record)

    // THEN
    assertFalse(result)
  }

  @Test
  fun `Given a valid proto with only a deletion timestamp and not an admin pass key, assert valid`() {
    // GIVEN
    val proto = CallLinkRecord.Builder().apply {
      rootKey = mockCredentials.linkKeyBytes.toByteString()
      adminPasskey = EMPTY
      deletedAtTimestampMs = System.currentTimeMillis()
    }.build()

    val record = SignalCallLinkRecord(STORAGE_ID, proto)

    // WHEN
    val result = testSubject.isInvalid(record)

    // THEN
    assertFalse(result)
  }

  @Test
  fun `Given a proto with neither an admin pass key nor a deletion timestamp, assert valid`() {
    // GIVEN
    val proto = CallLinkRecord.Builder().apply {
      rootKey = mockCredentials.linkKeyBytes.toByteString()
      adminPasskey = EMPTY
      deletedAtTimestampMs = 0L
    }.build()

    val record = SignalCallLinkRecord(STORAGE_ID, proto)

    // WHEN
    val result = testSubject.isInvalid(record)

    // THEN
    assertFalse(result)
  }
}
