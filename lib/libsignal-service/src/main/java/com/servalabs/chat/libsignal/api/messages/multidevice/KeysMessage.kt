package com.servalabs.chat.libsignal.api.messages.multidevice

import com.servalabs.chat.core.models.AccountEntropyPool
import com.servalabs.chat.core.models.backup.MediaRootBackupKey
import com.servalabs.chat.core.models.storageservice.StorageKey

data class KeysMessage(
  val storageService: StorageKey?,
  val accountEntropyPool: AccountEntropyPool?,
  val mediaRootBackupKey: MediaRootBackupKey?
)
