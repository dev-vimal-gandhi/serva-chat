package com.servalabs.chat.libsignal.api.push.exceptions;

public class UsernameMalformedException extends NonSuccessfulResponseCodeException {
  public UsernameMalformedException() {
    super(400);
  }
}
