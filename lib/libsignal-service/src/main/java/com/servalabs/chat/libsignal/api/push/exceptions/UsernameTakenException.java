package com.servalabs.chat.libsignal.api.push.exceptions;

public class UsernameTakenException extends NonSuccessfulResponseCodeException {
  public UsernameTakenException() {
    super(409);
  }
}
