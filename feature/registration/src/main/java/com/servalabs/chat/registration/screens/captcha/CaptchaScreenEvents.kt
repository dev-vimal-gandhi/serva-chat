/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.registration.screens.captcha

import com.servalabs.chat.core.util.censor
import com.servalabs.chat.registration.util.DebugLoggableModel

sealed class CaptchaScreenEvents : DebugLoggableModel() {
  data class CaptchaCompleted(val token: String) : CaptchaScreenEvents() {
    override fun toSafeString(): String {
      return "CaptchaCompleted(token=${token.censor()})"
    }
  }
  data object Cancel : CaptchaScreenEvents()
}
