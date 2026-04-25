package com.servalabs.chat.libsignal.api.push.exceptions;

public class DeprecatedVersionException extends NonSuccessfulResponseCodeException {
  public DeprecatedVersionException() {
    super(499);
  }
}
