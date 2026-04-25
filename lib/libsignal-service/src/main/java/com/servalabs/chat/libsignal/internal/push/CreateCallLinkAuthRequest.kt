/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.libsignal.internal.push

import com.fasterxml.jackson.annotation.JsonCreator
import com.servalabs.chat.core.util.Base64
import com.servalabs.chat.libsignal.zkgroup.calllinks.CreateCallLinkCredentialRequest

/**
 * Request body to create a call link credential response.
 */
data class CreateCallLinkAuthRequest @JsonCreator constructor(
  val createCallLinkCredentialRequest: String
) {
  companion object {
    @JvmStatic
    fun create(createCallLinkCredentialRequest: CreateCallLinkCredentialRequest): CreateCallLinkAuthRequest {
      return CreateCallLinkAuthRequest(
        Base64.encodeWithPadding(createCallLinkCredentialRequest.serialize())
      )
    }
  }
}
