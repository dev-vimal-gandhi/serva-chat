/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.libsignal.api.donations;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.servalabs.chat.libsignal.zkgroup.receipts.ReceiptCredentialRequest;
import com.servalabs.chat.core.util.Base64;

class ReceiptCredentialRequestJson {
  @JsonProperty("receiptCredentialRequest")
  private final String receiptCredentialRequest;

  ReceiptCredentialRequestJson(ReceiptCredentialRequest receiptCredentialRequest) {
    this.receiptCredentialRequest = Base64.encodeWithPadding(receiptCredentialRequest.serialize());
  }
}
