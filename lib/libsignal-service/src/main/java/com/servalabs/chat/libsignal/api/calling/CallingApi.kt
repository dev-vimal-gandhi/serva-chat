/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.libsignal.api.calling

import org.signal.libsignal.zkgroup.calllinks.CreateCallLinkCredentialRequest
import org.signal.libsignal.zkgroup.calllinks.CreateCallLinkCredentialResponse
import com.servalabs.chat.libsignal.api.NetworkResult
import com.servalabs.chat.libsignal.api.messages.calls.CallingResponse
import com.servalabs.chat.libsignal.api.messages.calls.TurnServerInfo
import com.servalabs.chat.libsignal.api.websocket.SignalWebSocket
import com.servalabs.chat.libsignal.internal.get
import com.servalabs.chat.libsignal.internal.post
import com.servalabs.chat.libsignal.internal.push.CreateCallLinkAuthRequest
import com.servalabs.chat.libsignal.internal.push.CreateCallLinkAuthResponse
import com.servalabs.chat.libsignal.internal.push.GetCallingRelaysResponse
import com.servalabs.chat.libsignal.internal.push.PushServiceSocket
import com.servalabs.chat.libsignal.internal.putCustom
import com.servalabs.chat.libsignal.internal.websocket.WebSocketRequestMessage

/**
 * Provide calling specific network apis.
 */
class CallingApi(
  private val auth: SignalWebSocket.AuthenticatedWebSocket,
  private val unAuth: SignalWebSocket.UnauthenticatedWebSocket,
  private val pushServiceSocket: PushServiceSocket
) {

  /**
   * Get 1:1 relay addresses in IpV4, Ipv6, and URL formats.
   *
   * GET /v2/calling/relays
   * - 200: Success
   * - 400: Invalid request
   * - 422: Invalid request format
   * - 429: Rate limited
   */
  fun getTurnServerInfo(): NetworkResult<List<TurnServerInfo>> {
    val request = WebSocketRequestMessage.get("/v2/calling/relays")
    return NetworkResult.fromWebSocketRequest(auth, request, GetCallingRelaysResponse::class)
      .map { it.relays ?: emptyList() }
  }

  /**
   * Generate a call link credential.
   *
   * POST /v1/call-link/create-auth
   * - 200: Success
   * - 400: Invalid request
   * - 422: Invalid request format
   * - 429: Rate limited
   */
  fun createCallLinkCredential(request: CreateCallLinkCredentialRequest): NetworkResult<CreateCallLinkCredentialResponse> {
    val request = WebSocketRequestMessage.post("/v1/call-link/create-auth", body = CreateCallLinkAuthRequest.create(request))
    return NetworkResult.fromWebSocketRequest(auth, request, CreateCallLinkAuthResponse::class)
      .map { it.createCallLinkCredentialResponse }
  }

  /**
   * Send an http request on behalf of the calling infrastructure. Only returns [NetworkResult.Success] with the
   * wrapped [CallingResponse] wrapping the error which in practice should never happen.
   *
   * @param requestId Request identifier
   * @param url Fully qualified URL to request
   * @param httpMethod Http method to use (e.g., "GET", "POST")
   * @param headers Optional list of headers to send with request
   * @param body Optional body to send with request
   * @return
   */
  fun makeCallingRequest(
    requestId: Long,
    url: String,
    httpMethod: String,
    headers: List<Pair<String, String>>?,
    body: ByteArray?
  ): NetworkResult<CallingResponse> {
    return when (val result = NetworkResult.fromFetch { pushServiceSocket.makeCallingRequest(requestId, url, httpMethod, headers, body) }) {
      is NetworkResult.Success -> result
      else -> NetworkResult.Success(CallingResponse.Error(requestId, result.getCause()))
    }
  }
}
