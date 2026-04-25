/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.libsignal.api.payments

import com.servalabs.chat.libsignal.api.NetworkResult
import com.servalabs.chat.libsignal.api.websocket.SignalWebSocket
import com.servalabs.chat.libsignal.internal.get
import com.servalabs.chat.libsignal.internal.push.AuthCredentials
import com.servalabs.chat.libsignal.internal.websocket.WebSocketRequestMessage

/**
 * Provide payments specific network apis.
 */
class PaymentsApi(private val authWebSocket: SignalWebSocket.AuthenticatedWebSocket) {

  /**
   * GET /v1/payments/auth
   * - 200: Success
   */
  fun getAuthorization(): NetworkResult<AuthCredentials> {
    val request = WebSocketRequestMessage.get("/v1/payments/auth")
    return NetworkResult.fromWebSocketRequest(authWebSocket, request, AuthCredentials::class)
  }

  /**
   * GET /v1/payments/conversions
   * - 200: Success
   */
  fun getCurrencyConversions(): NetworkResult<CurrencyConversions> {
    val request = WebSocketRequestMessage.get("/v1/payments/conversions")
    return NetworkResult.fromWebSocketRequest(authWebSocket, request, CurrencyConversions::class)
  }
}
