/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.dependencies

import android.net.Uri
import com.servalabs.chat.glide.SignalGlideDependencies
import com.servalabs.chat.glide.common.io.InputStreamFactory
import com.servalabs.chat.glide.DecryptableStreamFactory

object SignalGlideDependenciesProvider : SignalGlideDependencies.Provider {
  override fun getUriInputStreamFactory(uri: Uri): InputStreamFactory {
    return DecryptableStreamFactory(uri)
  }
}
