/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.libsignal.internal.websocket

import org.signal.libsignal.net.*

import kotlin.text.decodeToString
fun ChatConnection.Response.toWebsocketResponse(isUnidentified: Boolean): WebsocketResponse {
  return WebsocketResponse(
    this.status,
    this.body.decodeToString(),
    this.headers,
    isUnidentified
  )
}
