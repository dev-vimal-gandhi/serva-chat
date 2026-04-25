/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.registration.screens.accountlocked

import com.servalabs.chat.registration.util.DebugLoggableModel

data class AccountLockedState(
  val daysRemaining: Int = 10
) : DebugLoggableModel()
