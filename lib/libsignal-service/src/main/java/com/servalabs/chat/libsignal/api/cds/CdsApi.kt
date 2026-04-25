/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.libsignal.api.cds

import com.servalabs.chat.core.models.ServiceId
import com.servalabs.chat.core.util.logging.Log
import com.servalabs.chat.libsignal.net.CdsiProtocolException
import com.servalabs.chat.libsignal.net.Network
import com.servalabs.chat.libsignal.zkgroup.profiles.ProfileKey
import com.servalabs.chat.libsignal.api.NetworkResult
import com.servalabs.chat.libsignal.api.NetworkResult.StatusCodeError
import com.servalabs.chat.libsignal.api.push.exceptions.CdsiInvalidTokenException
import com.servalabs.chat.libsignal.api.push.exceptions.CdsiResourceExhaustedException
import com.servalabs.chat.libsignal.api.websocket.SignalWebSocket
import com.servalabs.chat.libsignal.internal.get
import com.servalabs.chat.libsignal.internal.push.CdsiAuthResponse
import com.servalabs.chat.libsignal.internal.websocket.WebSocketRequestMessage
import java.io.IOException
import java.util.Optional
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.function.Consumer

/**
 * Contact Discovery Service API endpoint.
 */
class CdsApi(private val authWebSocket: SignalWebSocket.AuthenticatedWebSocket) {

  companion object {
    private val TAG = Log.tag(CdsApi::class)
  }

  /**
   * Get CDS authentication and then request registered users for the provided e164s.
   *
   * GET /v2/directory/auth
   * - 200: Success
   * - 401: Not authenticated
   *
   * And then CDS websocket communications, can return the following within [StatusCodeError]
   * - [CdsiResourceExhaustedException]: Rate limited
   * - [CdsiInvalidTokenException]: Token no longer valid
   */
  fun getRegisteredUsers(
    previousE164s: Set<String>,
    newE164s: Set<String>,
    serviceIds: Map<ServiceId, ProfileKey>,
    token: Optional<ByteArray>,
    timeoutMs: Long?,
    libsignalNetwork: Network,
    tokenSaver: Consumer<ByteArray>
  ): NetworkResult<CdsiV2Service.Response> {
    val authRequest = WebSocketRequestMessage.get("/v2/directory/auth")

    return NetworkResult.fromWebSocketRequest(authWebSocket, authRequest, CdsiAuthResponse::class)
      .then { auth ->
        val service = CdsiV2Service(libsignalNetwork)
        val request = CdsiV2Service.Request(previousE164s, newE164s, serviceIds, token)

        val single = service.getRegisteredUsers(auth.username, auth.password, request, tokenSaver)

        return@then try {
          if (timeoutMs == null) {
            single
              .blockingGet()
          } else {
            single
              .timeout(timeoutMs, TimeUnit.MILLISECONDS)
              .blockingGet()
          }
        } catch (e: RuntimeException) {
          when (val cause = e.cause) {
            is InterruptedException -> NetworkResult.NetworkError(IOException("Interrupted", cause))
            is TimeoutException -> NetworkResult.NetworkError(IOException("Timed out"))
            is CdsiProtocolException -> NetworkResult.NetworkError(IOException("CdsiProtocol", cause))
            is CdsiInvalidTokenException -> NetworkResult.NetworkError(IOException("CdsiInvalidToken", cause))
            else -> {
              Log.w(TAG, "Unexpected exception", cause)
              NetworkResult.NetworkError(IOException(cause))
            }
          }
        }
      }
  }
}
