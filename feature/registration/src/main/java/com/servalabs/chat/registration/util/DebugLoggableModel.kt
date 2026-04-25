/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.registration.util

import com.servalabs.chat.registration.BuildConfig

open class DebugLoggableModel : DebugLoggable {
  override fun toString(): String {
    return if (BuildConfig.DEBUG) {
      toDebugString()
    } else {
      toSafeString()
    }
  }
}
