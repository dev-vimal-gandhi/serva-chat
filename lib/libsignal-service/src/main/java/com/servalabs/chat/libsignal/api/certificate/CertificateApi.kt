/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.libsignal.api.certificate

import com.servalabs.chat.libsignal.api.NetworkResult
import com.servalabs.chat.libsignal.api.websocket.SignalWebSocket
import com.servalabs.chat.libsignal.internal.get
import com.servalabs.chat.libsignal.internal.push.SenderCertificate
import com.servalabs.chat.libsignal.internal.websocket.WebSocketRequestMessage

/**
 * Endpoints to get [SenderCertificate]s.
 */
class CertificateApi(private val authWebSocket: SignalWebSocket.AuthenticatedWebSocket) {

  /**
   * GET /v1/certificate/delivery
   * - 200: Success
   */
  fun getSenderCertificate(): NetworkResult<ByteArray> {
    val request = WebSocketRequestMessage.get("/v1/certificate/delivery")
    return NetworkResult.fromWebSocketRequest(authWebSocket, request, SenderCertificate::class)
      .map { it.certificate }
  }

  /**
   * GET /v1/certificate/delivery?includeE164=false
   * - 200: Success
   */
  fun getSenderCertificateForPhoneNumberPrivacy(): NetworkResult<ByteArray> {
    val request = WebSocketRequestMessage.get("/v1/certificate/delivery?includeE164=false")
    return NetworkResult.fromWebSocketRequest(authWebSocket, request, SenderCertificate::class)
      .map { it.certificate }
  }
}
