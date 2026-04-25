package com.servalabs.chat.libsignal.api.push.exceptions;

public class UsernameIsNotAssociatedWithAnAccountException extends NotFoundException {
  public UsernameIsNotAssociatedWithAnAccountException() {
    super("The given username is not associated with an account.");
  }
}
