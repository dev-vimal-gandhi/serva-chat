package com.servalabs.chat.libsignal.internal.push.exceptions;

import com.servalabs.chat.libsignal.api.push.exceptions.NonSuccessfulResponseCodeException;

public final class NotInGroupException extends NonSuccessfulResponseCodeException {
  public NotInGroupException() {
    super(403);
  }
}
