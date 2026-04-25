/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.registration.data.network

import com.servalabs.chat.libsignal.api.NetworkResult
import com.servalabs.chat.libsignal.api.svr.Svr3Credentials
import com.servalabs.chat.libsignal.internal.push.AuthCredentials
import com.servalabs.chat.libsignal.internal.push.BackupV2AuthCheckResponse
import com.servalabs.chat.libsignal.internal.push.BackupV3AuthCheckResponse

/**
 * This is a processor to map a [BackupV2AuthCheckResponse] to all the known outcomes.
 */
sealed class BackupAuthCheckResult(cause: Throwable?) : RegistrationResult(cause) {
  companion object {
    @JvmStatic
    fun fromV2(networkResult: NetworkResult<BackupV2AuthCheckResponse>): BackupAuthCheckResult {
      return when (networkResult) {
        is NetworkResult.Success -> {
          val match = networkResult.result.match
          if (match != null) {
            SuccessWithCredentials(svr2Credentials = match, svr3Credentials = null)
          } else {
            SuccessWithoutCredentials()
          }
        }

        is NetworkResult.ApplicationError -> UnknownError(networkResult.throwable)
        is NetworkResult.NetworkError -> UnknownError(networkResult.exception)
        is NetworkResult.StatusCodeError -> UnknownError(networkResult.exception)
      }
    }

    @JvmStatic
    fun fromV3(networkResult: NetworkResult<BackupV3AuthCheckResponse>): BackupAuthCheckResult {
      return when (networkResult) {
        is NetworkResult.Success -> {
          val match = networkResult.result.match
          if (match != null) {
            SuccessWithCredentials(svr2Credentials = null, svr3Credentials = match)
          } else {
            SuccessWithoutCredentials()
          }
        }

        is NetworkResult.ApplicationError -> UnknownError(networkResult.throwable)
        is NetworkResult.NetworkError -> UnknownError(networkResult.exception)
        is NetworkResult.StatusCodeError -> UnknownError(networkResult.exception)
      }
    }
  }

  class SuccessWithCredentials(val svr2Credentials: AuthCredentials?, val svr3Credentials: Svr3Credentials?) : BackupAuthCheckResult(null)

  class SuccessWithoutCredentials : BackupAuthCheckResult(null)

  class UnknownError(cause: Throwable) : BackupAuthCheckResult(cause)
}
