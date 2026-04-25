package com.servalabs.chat.libsignal.internal.push;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response for {@link com.servalabs.chat.libsignal.api.push.exceptions.CdsiResourceExhaustedException}
 */
public class CdsiResourceExhaustedResponse {
  @JsonProperty("retry_after")
  private int retryAfter;

  public int getRetryAfter() {
    return retryAfter;
  }
}
