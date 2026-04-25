/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.registration.screens.accountlocked

import com.servalabs.chat.registration.util.DebugLoggableModel

sealed class AccountLockedScreenEvents : DebugLoggableModel() {
  data object Next : AccountLockedScreenEvents()
  data object LearnMore : AccountLockedScreenEvents()
}
