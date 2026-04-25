/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.libsignal.internal.push.exceptions

import com.servalabs.chat.libsignal.api.push.exceptions.NonSuccessfulResponseCodeException

/**
 * Indicates that the captcha we submitted was not accepted by the server.
 */
class CaptchaRejectedException : NonSuccessfulResponseCodeException(428, "Captcha rejected by server.")
