/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.libsignal.api

import com.servalabs.chat.libsignal.api.push.exceptions.AuthorizationFailedException
import com.servalabs.chat.libsignal.api.push.exceptions.NonSuccessfulResponseCodeException
import com.servalabs.chat.libsignal.api.push.exceptions.NotFoundException
import com.servalabs.chat.libsignal.api.push.exceptions.ProofRequiredException
import com.servalabs.chat.libsignal.api.push.exceptions.RateLimitException
import com.servalabs.chat.libsignal.api.push.exceptions.ServerRejectedException
import com.servalabs.chat.libsignal.api.push.exceptions.UnregisteredUserException
import com.servalabs.chat.libsignal.api.websocket.WebSocketUnavailableException
import com.servalabs.chat.libsignal.internal.push.SendMessageResponse
import com.servalabs.chat.libsignal.internal.push.exceptions.InAppPaymentProcessorError
import com.servalabs.chat.libsignal.internal.push.exceptions.InAppPaymentReceiptCredentialError
import com.servalabs.chat.libsignal.internal.push.exceptions.MismatchedDevicesException
import com.servalabs.chat.libsignal.internal.push.exceptions.PaymentsRegionException
import com.servalabs.chat.libsignal.internal.push.exceptions.StaleDevicesException
import java.io.IOException
import java.util.Optional
import kotlin.time.Duration.Companion.seconds

/**
 * Bridge layer to convert [NetworkResult]s into the response data or thrown exceptions.
 */
object NetworkResultUtil {

  /**
   * Unwraps [NetworkResult] to a basic [IOException] or [NonSuccessfulResponseCodeException]. Should only
   * be used when you don't need a specific flavor of IOException for a specific response  of any kind.
   */
  @JvmStatic
  @Throws(IOException::class)
  fun <T> successOrThrow(result: NetworkResult<T>): T {
    return when (result) {
      is NetworkResult.Success -> result.result
      is NetworkResult.ApplicationError -> {
        throw when (val error = result.throwable) {
          is IOException, is RuntimeException -> error
          else -> RuntimeException(error)
        }
      }
      is NetworkResult.NetworkError -> throw result.exception
      is NetworkResult.StatusCodeError -> throw result.exception
    }
  }

  /**
   * Convert to a basic [IOException] or [NonSuccessfulResponseCodeException]. Should only be used when you don't
   * need a specific flavor of IOException for a specific response code.
   */
  @JvmStatic
  @Throws(IOException::class)
  fun <T> toBasicLegacy(result: NetworkResult<T>): T {
    return when (result) {
      is NetworkResult.Success -> result.result
      is NetworkResult.ApplicationError -> {
        throw when (val error = result.throwable) {
          is IOException, is RuntimeException -> error
          else -> RuntimeException(error)
        }
      }
      is NetworkResult.NetworkError -> throw result.exception
      is NetworkResult.StatusCodeError -> {
        when (result.code) {
          401, 403 -> throw AuthorizationFailedException(result.code, "Authorization failed!")
          413, 429 -> throw RateLimitException(result.code, "Rate Limited", Optional.ofNullable(result.header("retry-after")?.toLongOrNull()?.seconds?.inWholeMilliseconds))
          else -> throw result.exception
        }
      }
    }
  }

  /**
   * Convert [NetworkResult] into expected type exceptions for an individual message send.
   */
  @JvmStatic
  @Throws(
    AuthorizationFailedException::class,
    UnregisteredUserException::class,
    MismatchedDevicesException::class,
    StaleDevicesException::class,
    ProofRequiredException::class,
    WebSocketUnavailableException::class,
    ServerRejectedException::class,
    IOException::class
  )
  fun toMessageSendLegacy(destination: String, result: NetworkResult<SendMessageResponse>): SendMessageResponse {
    return when (result) {
      is NetworkResult.Success -> result.result
      is NetworkResult.ApplicationError -> {
        throw when (val error = result.throwable) {
          is IOException, is RuntimeException -> error
          else -> RuntimeException(error)
        }
      }
      is NetworkResult.NetworkError -> throw result.exception
      is NetworkResult.StatusCodeError -> {
        throw when (result.code) {
          401 -> AuthorizationFailedException(result.code, "Authorization failed!")
          404 -> UnregisteredUserException(destination, result.exception)
          409 -> MismatchedDevicesException(result.parseJsonBody())
          410 -> StaleDevicesException(result.parseJsonBody())
          413, 429 -> RateLimitException(result.code, "Rate Limited", Optional.ofNullable(result.header("retry-after")?.toLongOrNull()?.seconds?.inWholeMilliseconds))
          428 -> ProofRequiredException(result.parseJsonBody(), result.header("retry-after")?.toLongOrNull() ?: -1)
          508 -> ServerRejectedException()
          else -> result.exception
        }
      }
    }
  }

  @JvmStatic
  @Throws(IOException::class)
  fun <T> toPreKeysLegacy(result: NetworkResult<T>): T {
    return when (result) {
      is NetworkResult.Success -> result.result
      is NetworkResult.StatusCodeError -> {
        throw when (result.code) {
          400, 401 -> AuthorizationFailedException(result.code, "Authorization failed!")
          404 -> NotFoundException("Not found")
          429 -> RateLimitException(result.code, "Rate limit exceeded: ${result.code}", Optional.empty())
          508 -> ServerRejectedException()
          else -> result.exception
        }
      }
      is NetworkResult.NetworkError -> throw result.exception
      is NetworkResult.ApplicationError -> {
        throw when (val error = result.throwable) {
          is IOException, is RuntimeException -> error
          else -> RuntimeException(error)
        }
      }
    }
  }

  /**
   * Convert a [NetworkResult] into typed exceptions expected during setting the user's profile.
   */
  @JvmStatic
  @Throws(AuthorizationFailedException::class, PaymentsRegionException::class, RateLimitException::class, IOException::class)
  fun toSetProfileLegacy(result: NetworkResult<String?>): String? {
    return when (result) {
      is NetworkResult.Success -> result.result
      is NetworkResult.ApplicationError -> {
        throw when (val error = result.throwable) {
          is IOException, is RuntimeException -> error
          else -> RuntimeException(error)
        }
      }
      is NetworkResult.NetworkError -> throw result.exception
      is NetworkResult.StatusCodeError -> {
        throw when (result.code) {
          401 -> AuthorizationFailedException(result.code, "Authorization failed!")
          403 -> PaymentsRegionException(result.code)
          413, 429 -> RateLimitException(result.code, "Rate Limited", Optional.ofNullable(result.header("retry-after")?.toLongOrNull()))
          else -> result.exception
        }
      }
    }
  }

  /**
   * Convert a [NetworkResult] into typed exceptions expected during calls with IAP endpoints. Not all endpoints require
   * specific error parsing but if those errors do happen for them they'll fail to parse and get the normal status code
   * exception.
   */
  @JvmStatic
  @Throws(IOException::class)
  fun <T> toIAPBasicLegacy(result: NetworkResult<T>): T {
    return when (result) {
      is NetworkResult.Success -> result.result
      is NetworkResult.ApplicationError -> {
        throw when (val error = result.throwable) {
          is IOException, is RuntimeException -> error
          else -> RuntimeException(error)
        }
      }
      is NetworkResult.NetworkError -> throw result.exception
      is NetworkResult.StatusCodeError -> {
        throw when (result.code) {
          402 -> result.parseJsonBody<InAppPaymentReceiptCredentialError>() ?: result.exception
          440 -> result.parseJsonBody<InAppPaymentProcessorError>() ?: result.exception
          else -> result.exception
        }
      }
    }
  }
}
