/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.registration.data.network

import com.servalabs.chat.pin.SvrWrongPinException
import com.servalabs.chat.registration.data.AccountRegistrationResult
import com.servalabs.chat.libsignal.api.NetworkResult
import com.servalabs.chat.libsignal.api.SvrNoDataException
import com.servalabs.chat.libsignal.api.push.exceptions.AuthorizationFailedException
import com.servalabs.chat.libsignal.api.push.exceptions.IncorrectRegistrationRecoveryPasswordException
import com.servalabs.chat.libsignal.api.push.exceptions.MalformedRequestException
import com.servalabs.chat.libsignal.api.push.exceptions.RateLimitException
import com.servalabs.chat.libsignal.api.svr.Svr3Credentials
import com.servalabs.chat.libsignal.internal.push.AuthCredentials
import com.servalabs.chat.libsignal.internal.push.LockedException
import com.servalabs.chat.libsignal.internal.push.VerifyAccountResponse

/**
 * This is a processor to map a [VerifyAccountResponse] to all the known outcomes.
 */
sealed class RegisterAccountResult(cause: Throwable?) : RegistrationResult(cause) {
  companion object {
    fun from(networkResult: NetworkResult<AccountRegistrationResult>): RegisterAccountResult {
      return when (networkResult) {
        is NetworkResult.Success -> Success(networkResult.result)
        is NetworkResult.ApplicationError -> UnknownError(networkResult.throwable)
        is NetworkResult.NetworkError -> UnknownError(networkResult.exception)
        is NetworkResult.StatusCodeError -> {
          when (val cause = networkResult.exception) {
            is IncorrectRegistrationRecoveryPasswordException -> IncorrectRecoveryPassword(cause)
            is AuthorizationFailedException -> AuthorizationFailed(cause)
            is MalformedRequestException -> MalformedRequest(cause)
            is RateLimitException -> createRateLimitProcessor(cause)
            is LockedException -> RegistrationLocked(cause = cause, timeRemaining = cause.timeRemaining, svr2Credentials = cause.svr2Credentials, svr3Credentials = cause.svr3Credentials)
            else -> {
              if (networkResult.code == 422) {
                ValidationError(cause)
              } else {
                UnknownError(cause)
              }
            }
          }
        }
      }
    }

    private fun createRateLimitProcessor(exception: RateLimitException): RegisterAccountResult {
      return if (exception.retryAfterMilliseconds.isPresent) {
        RateLimited(exception, exception.retryAfterMilliseconds.get())
      } else {
        AttemptsExhausted(exception)
      }
    }
  }
  class Success(val accountRegistrationResult: AccountRegistrationResult) : RegisterAccountResult(null)
  class IncorrectRecoveryPassword(cause: Throwable) : RegisterAccountResult(cause)
  class AuthorizationFailed(cause: Throwable) : RegisterAccountResult(cause)
  class MalformedRequest(cause: Throwable) : RegisterAccountResult(cause)
  class ValidationError(cause: Throwable) : RegisterAccountResult(cause)
  class RateLimited(cause: Throwable, val timeRemaining: Long) : RegisterAccountResult(cause)
  class AttemptsExhausted(cause: Throwable) : RegisterAccountResult(cause)
  class RegistrationLocked(cause: Throwable, val timeRemaining: Long, val svr2Credentials: AuthCredentials?, val svr3Credentials: Svr3Credentials?) : RegisterAccountResult(cause)
  class UnknownError(cause: Throwable) : RegisterAccountResult(cause)

  class SvrNoData(cause: SvrNoDataException) : RegisterAccountResult(cause)
  class SvrWrongPin(cause: SvrWrongPinException) : RegisterAccountResult(cause) {
    val triesRemaining = cause.triesRemaining
  }
}
