/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.libsignal.api.svr

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.servalabs.chat.libsignal.internal.push.AuthCredentials
import com.servalabs.chat.libsignal.internal.push.ByteArrayDeserializerBase64

/**
 * Response object when fetching SVR3 auth credentials. We also use it elsewhere as a convenient container
 * for the (username, password, shareset) tuple.
 */
class Svr3Credentials(
  @JsonProperty
  val username: String,

  @JsonProperty
  val password: String,

  @JsonProperty
  @JsonDeserialize(using = ByteArrayDeserializerBase64::class)
  val shareSet: ByteArray?
) {
  val authCredentials: AuthCredentials
    get() = AuthCredentials.create(username, password)
}
