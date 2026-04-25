package com.servalabs.chat.libsignal.api.push.exceptions;

public class UsernameIsNotReservedException extends NonSuccessfulResponseCodeException {
  public UsernameIsNotReservedException() {
    super(409, "The given username is not associated with an account.");
  }
}
