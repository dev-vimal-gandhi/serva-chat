/**
 * Copyright (C) 2014-2016 Open Whisper Systems
 *
 * Licensed according to the LICENSE file in this repository.
 */

package com.servalabs.chat.libsignal.api.util;

import com.servalabs.chat.core.models.ServiceId.ACI;
import com.servalabs.chat.core.models.ServiceId.PNI;
import com.servalabs.chat.libsignal.api.push.SignalServiceAddress;

public interface CredentialsProvider {
  ACI getAci();
  PNI getPni();
  String getE164();
  int getDeviceId();
  String getPassword();

  default boolean isInvalid() {
    return getAci() == null || getPassword() == null;
  }

  default String getUsername() {
    StringBuilder sb = new StringBuilder();
    sb.append(getAci().toString());
    if (getDeviceId() != SignalServiceAddress.DEFAULT_DEVICE_ID) {
      sb.append(".");
      sb.append(getDeviceId());
    }
    return sb.toString();
  }
}
