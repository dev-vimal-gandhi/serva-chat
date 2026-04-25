/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.registration.screens.pincreation

import com.servalabs.chat.registration.util.DebugLoggableModel

sealed class PinCreationScreenEvents : DebugLoggableModel() {
  data class PinSubmitted(val pin: String) : PinCreationScreenEvents()
  data object ToggleKeyboard : PinCreationScreenEvents()
  data object LearnMore : PinCreationScreenEvents()
}
