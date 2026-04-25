/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.registration.screens.quickrestore

import com.servalabs.chat.registration.util.DebugLoggableModel

sealed class QuickRestoreQrEvents : DebugLoggableModel() {
  data object RetryQrCode : QuickRestoreQrEvents()
  data object Cancel : QuickRestoreQrEvents()
  data object UseProxy : QuickRestoreQrEvents()
  data object DismissError : QuickRestoreQrEvents()
}
