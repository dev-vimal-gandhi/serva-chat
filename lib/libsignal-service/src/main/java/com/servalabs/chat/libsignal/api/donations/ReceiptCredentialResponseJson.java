/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.libsignal.api.donations;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.servalabs.chat.libsignal.zkgroup.InvalidInputException;
import com.servalabs.chat.libsignal.zkgroup.receipts.ReceiptCredentialResponse;
import com.servalabs.chat.core.util.Base64;

import java.io.IOException;

import javax.annotation.Nullable;

class ReceiptCredentialResponseJson {

  private final ReceiptCredentialResponse receiptCredentialResponse;

  ReceiptCredentialResponseJson(@JsonProperty("receiptCredentialResponse") String receiptCredentialResponse) {
    ReceiptCredentialResponse response;
    try {
      response = new ReceiptCredentialResponse(Base64.decode(receiptCredentialResponse));
    } catch (IOException | InvalidInputException e) {
      response = null;
    }

    this.receiptCredentialResponse = response;
  }

  public @Nullable ReceiptCredentialResponse getReceiptCredentialResponse() {
    return receiptCredentialResponse;
  }
}
