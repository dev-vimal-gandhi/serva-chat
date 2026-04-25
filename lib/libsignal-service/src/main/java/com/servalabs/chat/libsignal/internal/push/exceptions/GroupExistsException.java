package com.servalabs.chat.libsignal.internal.push.exceptions;

import com.servalabs.chat.libsignal.api.push.exceptions.NonSuccessfulResponseCodeException;

public final class GroupExistsException extends NonSuccessfulResponseCodeException {
  public GroupExistsException() {
    super(409);
  }
}
