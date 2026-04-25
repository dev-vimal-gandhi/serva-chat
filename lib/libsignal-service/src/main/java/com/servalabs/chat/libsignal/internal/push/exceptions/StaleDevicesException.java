/**
 * Copyright (C) 2014-2016 Open Whisper Systems
 *
 * Licensed according to the LICENSE file in this repository.
 */

package com.servalabs.chat.libsignal.internal.push.exceptions;

import com.servalabs.chat.libsignal.api.push.exceptions.NonSuccessfulResponseCodeException;
import com.servalabs.chat.libsignal.internal.push.StaleDevices;

public class StaleDevicesException extends NonSuccessfulResponseCodeException {

  private final StaleDevices staleDevices;

  public StaleDevicesException(StaleDevices staleDevices) {
    super(410);
    this.staleDevices = staleDevices;
  }

  public StaleDevices getStaleDevices() {
    return staleDevices;
  }
}
