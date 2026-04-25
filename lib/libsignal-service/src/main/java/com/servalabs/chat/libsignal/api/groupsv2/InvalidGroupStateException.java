package com.servalabs.chat.libsignal.api.groupsv2;

import com.servalabs.chat.libsignal.zkgroup.InvalidInputException;

/**
 * Thrown when a group has some data that cannot be decrypted, or is in some other way in an
 * unexpected state.
 */
public final class InvalidGroupStateException extends Exception {

  InvalidGroupStateException(InvalidInputException e) {
    super(e);
  }

  InvalidGroupStateException(String message) {
    super(message);
  }

  InvalidGroupStateException() {
  }
}
