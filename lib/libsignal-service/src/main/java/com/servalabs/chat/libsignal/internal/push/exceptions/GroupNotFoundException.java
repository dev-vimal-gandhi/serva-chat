package com.servalabs.chat.libsignal.internal.push.exceptions;

import com.servalabs.chat.libsignal.api.push.exceptions.NonSuccessfulResponseCodeException;

public final class GroupNotFoundException extends NonSuccessfulResponseCodeException {
  public GroupNotFoundException() {
    super(404);
  }
}
