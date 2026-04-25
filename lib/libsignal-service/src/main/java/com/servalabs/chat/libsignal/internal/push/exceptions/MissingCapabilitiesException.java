package com.servalabs.chat.libsignal.internal.push.exceptions;

import com.servalabs.chat.libsignal.api.push.exceptions.NonSuccessfulResponseCodeException;

public class MissingCapabilitiesException extends NonSuccessfulResponseCodeException {
  public MissingCapabilitiesException() {
    super(409);
  }
}
