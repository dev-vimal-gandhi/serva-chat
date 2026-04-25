/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.core.models.backup

import com.servalabs.chat.core.models.ServiceId
import org.signal.libsignal.protocol.ecc.ECPrivateKey

/**
 * Contains the common properties for all "backup keys", namely the [MessageBackupKey] and [org.signal.libsignal.api.backup.MediaRootBackupKey]
 */
interface BackupKey {

  val value: ByteArray

  /**
   * The private key used to generate anonymous credentials when interacting with the backup service.
   */
  fun deriveAnonymousCredentialPrivateKey(aci: ServiceId.ACI): ECPrivateKey
}
