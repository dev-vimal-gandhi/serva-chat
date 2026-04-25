/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.libsignal.api.account

import com.servalabs.chat.libsignal.protocol.IdentityKey
import com.servalabs.chat.libsignal.protocol.state.KyberPreKeyRecord
import com.servalabs.chat.libsignal.protocol.state.SignedPreKeyRecord

/**
 * Holder class to pass around a bunch of prekeys that we send off to the service during registration.
 * As the service does not return the submitted prekeys, we need to hold them in memory so that when
 * the service approves the keys we have a local copy to persist.
 */
data class PreKeyCollection(
  val identityKey: IdentityKey,
  val signedPreKey: SignedPreKeyRecord,
  val lastResortKyberPreKey: KyberPreKeyRecord
)
