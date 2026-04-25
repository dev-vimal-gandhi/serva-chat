/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.registration.screens.pincreation

import com.servalabs.chat.core.models.AccountEntropyPool
import com.servalabs.chat.registration.util.DebugLoggableModel

data class PinCreationState(
  val isAlphanumericKeyboard: Boolean = false,
  val inputLabel: String? = null,
  val isConfirmEnabled: Boolean = false,
  val accountEntropyPool: AccountEntropyPool? = null
) : DebugLoggableModel()
