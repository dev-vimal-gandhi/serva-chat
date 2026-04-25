/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.libsignal.api.ratelimit

import com.servalabs.chat.libsignal.api.NetworkResult
import com.servalabs.chat.libsignal.api.websocket.SignalWebSocket
import com.servalabs.chat.libsignal.internal.post
import com.servalabs.chat.libsignal.internal.put
import com.servalabs.chat.libsignal.internal.websocket.WebSocketRequestMessage

/**
 * Calls for requesting and submitting rate limit triggered challenges.
 */
class RateLimitChallengeApi(private val authWebSocket: SignalWebSocket.AuthenticatedWebSocket) {

  /**
   * Request a push challenge for rate limits.
   *
   * PUT /v1/challenge/push
   * - 200: Success
   * - 404: No push token available
   * - 413: Submitted non-empty body
   * - 429: Too many attempts
   */
  fun requestPushChallenge(): NetworkResult<Unit> {
    val request = WebSocketRequestMessage.post("/v1/challenge/push", null)
    return NetworkResult.fromWebSocketRequest(authWebSocket, request)
  }

  /**
   * Submit a push token to reset rate limits.
   *
   * PUT /v1/challenge
   * - 200: Success
   * - 428: Challenge token is invalid
   * - 429: Too many attempts
   */
  fun submitPushChallenge(challenge: String): NetworkResult<Unit> {
    val request = WebSocketRequestMessage.put("/v1/challenge", SubmitPushChallengePayload(challenge))
    return NetworkResult.fromWebSocketRequest(authWebSocket, request)
  }

  /**
   * Submit a captcha token to reset rate limits.
   *
   * PUT /v1/challenge
   * - 200: Success
   * - 428: Challenge token is invalid
   * - 429: Too many attempts
   */
  fun submitCaptchaChallenge(challenge: String, token: String): NetworkResult<Unit> {
    val request = WebSocketRequestMessage.put("/v1/challenge", SubmitRecaptchaChallengePayload(challenge, token))
    return NetworkResult.fromWebSocketRequest(authWebSocket, request)
  }
}
