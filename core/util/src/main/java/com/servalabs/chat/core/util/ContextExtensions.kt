/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.core.util

import android.app.DownloadManager
import android.content.Context

fun Context.getDownloadManager(): DownloadManager {
  return this.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
}
