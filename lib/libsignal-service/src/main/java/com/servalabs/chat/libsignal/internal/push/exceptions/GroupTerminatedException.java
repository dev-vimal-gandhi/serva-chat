package com.servalabs.chat.libsignal.internal.push.exceptions;

import com.servalabs.chat.libsignal.api.push.exceptions.NonSuccessfulResponseCodeException;

public final class GroupTerminatedException extends NonSuccessfulResponseCodeException {
  public GroupTerminatedException() {
    super(423);
  }
}
