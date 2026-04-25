/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.registration.screens.localbackuprestore

import com.servalabs.chat.registration.util.DebugLoggableModel

sealed class EnterAepEvents : DebugLoggableModel() {
  /** User changed the backup key text. */
  data class BackupKeyChanged(val value: String) : EnterAepEvents()

  /** User submitted the backup key. */
  data object Submit : EnterAepEvents()

  /** User wants to cancel / no recovery key. */
  data object Cancel : EnterAepEvents()
}
