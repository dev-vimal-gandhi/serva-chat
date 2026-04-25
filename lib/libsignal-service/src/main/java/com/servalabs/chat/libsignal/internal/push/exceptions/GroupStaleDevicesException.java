package com.servalabs.chat.libsignal.internal.push.exceptions;

import com.servalabs.chat.libsignal.api.push.exceptions.NonSuccessfulResponseCodeException;
import com.servalabs.chat.libsignal.internal.push.GroupStaleDevices;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a 410 response from the service during a sender key send.
 */
public class GroupStaleDevicesException extends NonSuccessfulResponseCodeException {

  private final List<GroupStaleDevices> staleDevices;

  public GroupStaleDevicesException(GroupStaleDevices[] staleDevices) {
    super(410);
    this.staleDevices = Arrays.asList(staleDevices);
  }

  public List<GroupStaleDevices> getStaleDevices() {
    return staleDevices;
  }
}
